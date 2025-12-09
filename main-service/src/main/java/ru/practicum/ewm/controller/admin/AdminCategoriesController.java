package ru.practicum.ewm.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Tag(name = "Admin: Категории", description = "API для работы с категориями")
public class AdminCategoriesController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Добавление новой категории")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Получен запрос на добавление новой категории: {}", newCategoryDto);
        return categoryService.addCategory(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @Operation(summary = "Удаление категории")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории с id={}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    @Operation(summary = "Изменение категории")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Получен запрос на обновление категории с id={} данными: {}", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }
}