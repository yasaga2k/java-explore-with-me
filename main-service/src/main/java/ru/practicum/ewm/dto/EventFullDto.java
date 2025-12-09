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
public class EventFullDto {

    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private CategoryDto category;

    @JsonProperty("confirmedRequests")
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("createdOn")
    private LocalDateTime createdOn;

    @JsonProperty("description")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("eventDate")
    private LocalDateTime eventDate;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("initiator")
    private UserShortDto initiator;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("publishedOn")
    private LocalDateTime publishedOn;

    @JsonProperty("requestModeration")
    private Boolean requestModeration;

    @JsonProperty("state")
    private String state;

    @JsonProperty("title")
    private String title;

    @JsonProperty("views")
    private Long views;
}