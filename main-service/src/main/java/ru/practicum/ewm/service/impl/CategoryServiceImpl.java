package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.mapper.CategoryMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding category: {}", newCategoryDto);

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category name already exists: " + newCategoryDto.getName());
        }

        Category category = categoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        CategoryDto result = categoryMapper.toCategoryDto(savedCategory);
        log.info("Added category: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        long eventCount = eventRepository.countByCategoryId(catId);
        if (eventCount > 0) {
            throw new ConflictException("The category is not empty. It is linked to " + eventCount + " event(s).");
        }

        categoryRepository.deleteById(catId);
        log.info("Deleted category with id: {}", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with id {} with  {}", catId, categoryDto);

        Category existingCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Category name already exists: " + categoryDto.getName());
        }

        existingCategory.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(existingCategory);
        CategoryDto result = categoryMapper.toCategoryDto(updatedCategory);
        log.info("Updated category: {}", result);
        return result;
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Getting categories with from={}, size={}", from, size);

        PageRequest page = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(page).getContent();

        List<CategoryDto> result = categories.stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());

        log.debug("Found {} categories", result.size());
        return result;
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Getting category with id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        CategoryDto result = categoryMapper.toCategoryDto(category);
        log.debug("Found category: {}", result);
        return result;
    }
}