package ru.practicum.ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto addEvent(long userId, NewEventDto eventCreateDto);

    EventFullDto getEvent(long eventId, String uri, String ip);

    List<EventShortDto> getEvents(
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
    );

    EventFullDto getUserEvent(long userId, long eventId);

    List<EventShortDto> getUserEvents(long userId, Pageable pageable);

    List<EventFullDto> getAdminEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    );

    EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest eventUpdateUserDto);

    EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest eventUpdateAdminDto);
}