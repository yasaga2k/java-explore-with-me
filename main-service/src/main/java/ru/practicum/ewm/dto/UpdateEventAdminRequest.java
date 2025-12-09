package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000)
    @JsonProperty("annotation")
    private String annotation;

    @Positive
    @JsonProperty("category")
    private Long category;

    @Size(min = 20, max = 7000)
    @JsonProperty("description")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("eventDate")
    private LocalDateTime eventDate;

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

    @JsonProperty("stateAction")
    private String stateAction;

    @Size(min = 3, max = 120)
    @JsonProperty("title")
    private String title;
}