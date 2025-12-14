package ru.practicum.ewm.controller.privateapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@Tag(name = "Private: Комментарии", description = "Закрытый API для работы с комментариями")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Добавление нового комментария")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto) {

        log.info("Получен запрос пользователя id={} на добавление комментария к событию id={}: {}",
                userId, eventId, newCommentDto);
        return commentService.createComment(eventId, userId, newCommentDto);
    }
}