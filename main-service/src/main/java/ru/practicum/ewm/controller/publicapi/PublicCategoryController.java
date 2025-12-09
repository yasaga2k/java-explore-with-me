package ru.practicum.ewm.controller.publicapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Public: Категории", description = "Публичный API для работы с категориями")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Получение категорий")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Получен запрос на получение категорий с from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    @Operation(summary = "Получение информации о категории по её идентификатору")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Получен запрос на получение категории с id={}", catId);
        return categoryService.getCategoryById(catId);
    }
}