package ru.practicum.ewm.controller.publicapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
@Tag(name = "Public: Подборки событий", description = "Публичный API для работы с подборками событий")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    @Operation(summary = "Получение подборок событий")
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Получен запрос на получение подборок с фильтрами: pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @Operation(summary = "Получение подборки событий по его id")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("Получен запрос на получение подборки с id={}", compId);
        return compilationService.getCompilationById(compId);
    }
}