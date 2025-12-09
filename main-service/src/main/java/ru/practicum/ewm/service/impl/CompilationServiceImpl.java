package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CompilationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ru.practicum.ewm.mapper.CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        log.info("Adding compilation: {}", newCompilationDto);

        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            if (events.size() != newCompilationDto.getEvents().size()) {
                throw new NotFoundException("Some events were not found.");
            }
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto result = compilationMapper.toCompilationDto(savedCompilation);
        log.info("Added compilation: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
        log.info("Deleted compilation with id: {}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id {} with {}", compId, updateRequest);

        Compilation existingCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (updateRequest.getTitle() != null) {
            existingCompilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            existingCompilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
            if (events.size() != updateRequest.getEvents().size()) {
                throw new NotFoundException("Some events were not found.");
            }
            existingCompilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(existingCompilation);
        CompilationDto result = compilationMapper.toCompilationDto(updatedCompilation);
        log.info("Updated compilation: {}", result);
        return result;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Getting compilations with filters: pinned={}, from={}, size={}", pinned, from, size);

        PageRequest page = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, page);
        } else {
            compilations = compilationRepository.findAll(page).getContent();
        }

        List<CompilationDto> result = compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());

        log.debug("Found {} compilations", result.size());
        return result;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        CompilationDto result = compilationMapper.toCompilationDto(compilation);
        log.debug("Found compilation: {}", result);
        return result;
    }
}