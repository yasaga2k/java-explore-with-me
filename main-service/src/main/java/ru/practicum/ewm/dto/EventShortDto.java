package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {

    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private CategoryDto category;

    @JsonProperty("confirmedRequests")
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("eventDate")
    private LocalDateTime eventDate;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("initiator")
    private UserShortDto initiator;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("views")
    private Long views;
}