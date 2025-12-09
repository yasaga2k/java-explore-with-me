package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    @JsonProperty("events")
    private List<EventShortDto> events;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("pinned")
    private Boolean pinned;

    @JsonProperty("title")
    private String title;
}