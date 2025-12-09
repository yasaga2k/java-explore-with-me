package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ru.practicum.ewm.mapper.RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user with id: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        List<ParticipationRequestDto> result = requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());

        log.debug("Found {} requests", result.size());
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Adding participation request for user {} and event {}", userId, eventId);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event.");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Cannot request participation in an unpublished event.");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Participation request already exists for user " + userId + " and event " + eventId);
        }

        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit has been reached for event " + eventId);
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(requester);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        ParticipationRequestDto result = requestMapper.toRequestDto(savedRequest);
        log.info("Added participation request: {}", result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request with id {} for user {}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found or does not belong to user " + userId);
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);
        ParticipationRequestDto result = requestMapper.toRequestDto(updatedRequest);
        log.info("Cancelled request: {}", result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsForInitiator(Long userId, Long eventId) {
        log.info("Getting participants for event with id {} and initiator id {}", eventId, userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found or does not belong to user " + userId);
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        List<ParticipationRequestDto> result = requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());

        log.debug("Found {} participant requests", result.size());
        return result;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Changing status of requests for event with id {} and initiator id {}", eventId, userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found or does not belong to user " + userId);
        }

        List<Long> requestIds = statusUpdateRequest.getRequestIds();
        List<ParticipationRequest> requests = requestRepository.findByIdIn(requestIds);

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request with id=" + request.getId() + " does not belong to event " + eventId);
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request with id=" + request.getId() + " is not in PENDING status");
            }
        }

        String newStatus = statusUpdateRequest.getStatus();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if ("CONFIRMED".equals(newStatus)) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

            if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit has been reached for event " + eventId);
            }

            if (event.getParticipantLimit() > 0 && (confirmedCount + requests.size()) > event.getParticipantLimit()) {
                throw new ConflictException("Cannot confirm all requests: would exceed participant limit for event " + eventId);
            }

            long availableSlots = event.getParticipantLimit() - confirmedCount;

            for (ParticipationRequest request : requests) {
                if (event.getParticipantLimit() == 0 || availableSlots > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(requestMapper.toRequestDto(requestRepository.save(request)));
                    if (event.getParticipantLimit() > 0) {
                        availableSlots--;
                    }
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toRequestDto(requestRepository.save(request)));
                }
            }
        } else if ("REJECTED".equals(newStatus)) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toRequestDto(requestRepository.save(request)));
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        log.info("Status update result: confirmed={}, rejected={}", confirmedRequests.size(), rejectedRequests.size());
        return result;
    }
}