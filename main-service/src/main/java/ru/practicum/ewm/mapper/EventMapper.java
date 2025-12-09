package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, CategoryMapper.class}
)
public interface EventMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "location", source = "location")
    EventFullDto toEventFullDto(Event event);

    @Named("toEventShortDto")
    EventShortDto toEventShortDto(Event event);

    default Location map(ru.practicum.ewm.dto.Location dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    default ru.practicum.ewm.dto.Location map(Location entity) {
        if (entity == null) {
            return null;
        }
        return ru.practicum.ewm.dto.Location.builder()
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }
}