package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long eventId, Long userId, NewCommentDto dto);

    List<CommentDto> getComments(Long eventId);

    void deleteComment(Long commentId);
}