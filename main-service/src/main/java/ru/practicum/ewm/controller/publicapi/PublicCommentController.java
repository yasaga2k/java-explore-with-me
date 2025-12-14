package ru.practicum.ewm.controller.publicapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
@Tag(name = "Public: Комментарии", description = "Публичный API для работы с комментариями")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "Получение комментариев события")
    public List<CommentDto> getComments(@PathVariable Long eventId) {
        log.info("Получен запрос на получение комментариев для события id={}", eventId);
        return commentService.getComments(eventId);
    }
}