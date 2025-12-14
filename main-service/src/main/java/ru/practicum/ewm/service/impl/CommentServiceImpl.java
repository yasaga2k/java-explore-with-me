package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.State;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.mapper.CommentMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long eventId, Long userId, NewCommentDto dto) {
        log.info("Creating comment for event {} by user {}", eventId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Cannot add comment to unpublished event");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        CommentDto result = commentMapper.toCommentDto(savedComment);
        log.info("Created comment: {}", result);
        return result;
    }

    @Override
    public List<CommentDto> getComments(Long eventId) {
        log.info("Getting comments for event {}", eventId);

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        List<Comment> comments = commentRepository.findByEventId(eventId);

        List<CommentDto> result = comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.debug("Found {} comments for event {}", result.size(), eventId);
        return result;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deleting comment {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        commentRepository.deleteById(commentId);
        log.info("Deleted comment {}", commentId);
    }
}