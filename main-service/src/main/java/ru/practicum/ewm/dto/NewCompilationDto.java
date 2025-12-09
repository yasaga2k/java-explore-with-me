package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    @JsonProperty("events")
    private List<Long> events;

    @JsonProperty("pinned")
    private Boolean pinned;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    @JsonProperty("title")
    private String title;
}