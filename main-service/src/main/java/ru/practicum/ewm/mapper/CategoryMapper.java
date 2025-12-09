package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(NewCategoryDto newCategoryDto);

    @Named("toCategoryDto")
    CategoryDto toCategoryDto(Category category);

    default Category mapIdToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return Category.builder()
                .id(categoryId)
                .build();
    }
}