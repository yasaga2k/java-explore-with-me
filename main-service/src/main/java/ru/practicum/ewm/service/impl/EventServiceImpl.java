package ru.practicum.ewm.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EventServiceImpl implements EventService {

    @Value("${app.name:ewm-main-service}")
    private String appName;

    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto eventCreateDto) {
        log.info("Adding event for user ID: {} with data: {}", userId, eventCreateDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        Category category = categoryRepository.findById(eventCreateDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", eventCreateDto.getCategory())));

        if (eventCreateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        Event event = eventMapper.toEvent(eventCreateDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(eventCreateDto.getPaid() != null ? eventCreateDto.getPaid() : false);
        event.setParticipantLimit(eventCreateDto.getParticipantLimit() != null ? eventCreateDto.getParticipantLimit() : 0);
        event.setRequestModeration(eventCreateDto.getRequestModeration() != null ? eventCreateDto.getRequestModeration() : true);

        if (eventCreateDto.getLocation() != null) {
            event.setLocation(eventMapper.map(eventCreateDto.getLocation()));
        }

        Event savedEvent = eventRepository.save(event);
        EventFullDto result = eventMapper.toEventFullDto(savedEvent);
        result.setViews(0L); // При создании просмотров нет
        log.info("Added event: {}", result);
        return result;
    }

    @Override
    public EventFullDto getEvent(long eventId, String uri, String ip) {
        log.info("Getting public event with id: {}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found or is not published", eventId)));

        addHit(uri, ip);

        long views = getEventView(eventId);
        EventFullDto result = eventMapper.toEventFullDto(event);
        result.setViews(views);
        log.debug("Found public event: {}", result);
        return result;
    }

    @Override
    public List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            String uri,
            String ip
    ) {
        log.info("Getting public events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }

        LocalDateTime finalRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();


        Pageable pageable;
        if (sort == null || sort.equalsIgnoreCase("VIEWS")) {
            pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        } else if (sort.equalsIgnoreCase("EVENT_DATE")) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
        } else {
            throw new ValidationException("Invalid sort option. Must be EVENT_DATE or VIEWS");
        }

        addHit(uri, ip);

        List<Event> events = eventRepository.findEvents(
                text == null ? null : text.toLowerCase(),
                categories,
                paid,
                finalRangeStart,
                rangeEnd,
                onlyAvailable,
                State.PUBLISHED,
                pageable
        );

        Map<Long, Long> viewsMap = getEventsView(events.stream().map(Event::getId).collect(Collectors.toList()));

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        log.debug("Found {} public events", result.size());
        return result;
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        log.info("Getting event with id {} for initiator with id {}", eventId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found or does not belong to user %d", eventId, userId)));

        long views = getEventView(eventId);
        EventFullDto result = eventMapper.toEventFullDto(event);
        result.setViews(views);
        log.debug("Found event: {}", result);
        return result;
    }

    @Override
    public List<EventShortDto> getUserEvents(long userId, Pageable pageable) {
        log.info("Getting events for initiator with id: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        Map<Long, Long> viewsMap = getEventsView(events.stream().map(Event::getId).collect(Collectors.toList()));

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        log.debug("Found {} events", result.size());
        return result;
    }

    @Override
    public List<EventFullDto> getAdminEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    ) {
        log.info("Getting events for admin with filters: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);

        List<State> eventStates = null;
        if (states != null && !states.isEmpty()) {
            try {
                eventStates = states.stream()
                        .map(State::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid state value provided");
            }
        }

        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd, pageable);

        Map<Long, Long> views = getEventsView(events.stream().map(Event::getId).collect(Collectors.toList()));

        List<EventFullDto> result = events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        log.debug("Found {} events", result.size());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest eventUpdateUserDto) {
        log.info("Updating event with id {} by user {} with data: {}", eventId, userId, eventUpdateUserDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found or does not belong to user %d", eventId, userId)));

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (eventUpdateUserDto.getEventDate() != null) {
            LocalDateTime newEventDate = eventUpdateUserDto.getEventDate();
            if (newEventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours from now");
            }
        } else if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Event updatedEvent = updateEventByUserData(event, eventUpdateUserDto);
        Event savedEvent = eventRepository.save(updatedEvent);

        long views = getEventView(eventId);
        EventFullDto result = eventMapper.toEventFullDto(savedEvent);
        result.setViews(views);
        log.info("Updated event: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest eventUpdateAdminDto) {
        log.info("Updating event with id {} by admin with data: {}", eventId, eventUpdateAdminDto);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (eventUpdateAdminDto.getStateAction() != null &&
                "PUBLISH_EVENT".equals(eventUpdateAdminDto.getStateAction())) {
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Cannot publish event because it starts in less than 1 hour");
            }
        }

        Event updatedEvent = updateEventByAdminData(event, eventUpdateAdminDto);
        Event savedEvent = eventRepository.save(updatedEvent);

        long views = getEventView(eventId);
        EventFullDto result = eventMapper.toEventFullDto(savedEvent);
        result.setViews(views);
        log.info("Updated event: {}", result);
        return result;
    }

    private Event updateEventByUserData(Event event, UpdateEventUserRequest updateData) {
        log.debug("Updating event {} with user data", event.getId());

        if (updateData.getCategory() != null) {
            Category category = categoryRepository.findById(updateData.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", updateData.getCategory())));
            event.setCategory(category);
        }

        String stateAction = updateData.getStateAction();
        if (stateAction != null) {
            if ("SEND_TO_REVIEW".equals(stateAction)) {
                event.setState(State.PENDING);
            } else if ("CANCEL_REVIEW".equals(stateAction)) {
                event.setState(State.CANCELED);
            } else {
                throw new ValidationException("Invalid stateAction for user update. Must be SEND_TO_REVIEW or CANCEL_REVIEW");
            }
        }

        if (updateData.getEventDate() != null) {
            if (updateData.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Event date cannot be in the past");
            }
            event.setEventDate(updateData.getEventDate());
        }

        if (updateData.getTitle() != null) {
            event.setTitle(updateData.getTitle());
        }
        if (updateData.getAnnotation() != null) {
            event.setAnnotation(updateData.getAnnotation());
        }
        if (updateData.getDescription() != null) {
            event.setDescription(updateData.getDescription());
        }
        if (updateData.getParticipantLimit() != null) {
            event.setParticipantLimit(updateData.getParticipantLimit());
        }
        if (updateData.getLocation() != null) {
            event.setLocation(Location.builder()
                    .lon(updateData.getLocation().getLon())
                    .lat(updateData.getLocation().getLat())
                    .build());
        }
        if (updateData.getPaid() != null) {
            event.setPaid(updateData.getPaid());
        }
        if (updateData.getRequestModeration() != null) {
            event.setRequestModeration(updateData.getRequestModeration());
        }

        return event;
    }

    private Event updateEventByAdminData(Event event, UpdateEventAdminRequest updateData) {
        log.debug("Updating event {} with admin data", event.getId());

        if (updateData.getCategory() != null) {
            Category category = categoryRepository.findById(updateData.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateData.getCategory() + " was not found"));
            event.setCategory(category);
        }

        State currentState = event.getState();
        String updateStateAction = updateData.getStateAction();
        if (updateStateAction != null) {
            if ("PUBLISH_EVENT".equals(updateStateAction)) {
                if (currentState != State.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + currentState);
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equals(updateStateAction)) {
                if (currentState == State.PUBLISHED) {
                    throw new ConflictException("Cannot reject an already published event");
                }
                event.setState(State.CANCELED);
            } else {
                throw new ValidationException("Invalid stateAction for admin update. Must be PUBLISH_EVENT or REJECT_EVENT");
            }
        }

        if (updateData.getTitle() != null) {
            event.setTitle(updateData.getTitle());
        }
        if (updateData.getAnnotation() != null) {
            event.setAnnotation(updateData.getAnnotation());
        }
        if (updateData.getDescription() != null) {
            event.setDescription(updateData.getDescription());
        }
        if (updateData.getEventDate() != null) {
            if (updateData.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Event date cannot be in the past");
            }
            event.setEventDate(updateData.getEventDate());
        }
        if (updateData.getParticipantLimit() != null) {
            event.setParticipantLimit(updateData.getParticipantLimit());
        }
        if (updateData.getLocation() != null) {
            event.setLocation(Location.builder()
                    .lon(updateData.getLocation().getLon())
                    .lat(updateData.getLocation().getLat())
                    .build());
        }
        if (updateData.getPaid() != null) {
            event.setPaid(updateData.getPaid());
        }
        if (updateData.getRequestModeration() != null) {
            event.setRequestModeration(updateData.getRequestModeration());
        }

        return event;
    }

    private void addHit(String uri, String ip) {
        log.debug("Sending hit to stats service: uri={}, ip={}", uri, ip);
        try {
            statsClient.addHit(
                    EndpointHitDto.builder()
                            .uri(uri)
                            .app(appName)
                            .ip(ip)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to send hit to stats service: {}", e.getMessage());
        }
    }

    private long getEventView(long eventId) {
        log.debug("Getting views for event ID: {}", eventId);
        List<String> uris = List.of("/events/" + eventId);

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now(),
                    uris,
                    true
            );

            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            log.warn("Failed to get stats for event {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }

    private Map<Long, Long> getEventsView(List<Long> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        log.debug("Getting views for event IDs: {}", ids);

        List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now(),
                    uris,
                    false
            );

            if (stats.isEmpty()) {
                return new HashMap<>();
            }

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> extractEventIdFromUri(stat.getUri()),
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.warn("Failed to get stats for events: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String idStr = uri.substring("/events/".length());
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.warn("Failed to extract event ID from URI: {}", uri);
            return -1L;
        }
    }
}