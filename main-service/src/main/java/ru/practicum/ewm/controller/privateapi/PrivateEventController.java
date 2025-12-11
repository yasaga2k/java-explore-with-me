package ru.practicum.ewm.controller.privateapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Tag(name = "Private: События", description = "Закрытый API для работы с событиями")
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    @Operation(summary = "Получение событий, добавленных текущим пользователем")
    public List<EventShortDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Получен запрос пользователя id={} на получение своих событий, from={}, size={}", userId, from, size);
        return eventService.getUserEvents(
                userId,
                PageRequest.of(from / size, size, Sort.by("id").ascending())
        );
    }

    @PostMapping
    @Operation(summary = "Добавление нового события")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        log.info("Получен запрос пользователя id={} на добавление события: {}", userId, newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Получение полной информации о событии добавленном текущим пользователем")
    public EventFullDto getEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Получен запрос пользователя id={} на получение события id={}", userId, eventId);
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "Изменение события добавленного текущим пользователем")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest) {

        log.info("Получен запрос пользователя id={} на обновление события id={} данными: {}", userId, eventId, updateRequest);
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }

    @GetMapping("/{eventId}/requests")
    @Operation(summary = "Получение информации о запросах на участие в событии текущего пользователя")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Получен запрос пользователя id={} на получение запросов участия в событии id={}", userId, eventId);
        return requestService.getEventRequestsForInitiator(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @Operation(summary = "Изменение статуса заявок на участие в событии текущего пользователя")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {

        log.info("Получен запрос пользователя id={} на изменение статуса запросов для события id={} данными: {}",
                userId, eventId, statusUpdateRequest);
        return requestService.updateEventRequestStatus(userId, eventId, statusUpdateRequest);
    }
}