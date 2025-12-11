package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.Valid;
import ru.practicum.ewm.validation.EventDateConstraint;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    @JsonProperty("annotation")
    private String annotation;

    @NotNull
    @Positive
    @JsonProperty("category")
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @EventDateConstraint
    @JsonProperty("eventDate")
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    @JsonProperty("location")
    private Location location;

    @JsonProperty("paid")
    private Boolean paid;

    @Min(0)
    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @JsonProperty("requestModeration")
    private Boolean requestModeration;

    @NotBlank
    @Size(min = 3, max = 120)
    @JsonProperty("title")
    private String title;
}