package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.model.Compilation;

@Mapper(
        componentModel = "spring",
        uses = {EventMapper.class}
)
public interface CompilationMapper {

    CompilationDto toCompilationDto(Compilation compilation);
}