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
public class EventRequestStatusUpdateResult {

    @JsonProperty("confirmedRequests")
    private List<ParticipationRequestDto> confirmedRequests;

    @JsonProperty("rejectedRequests")
    private List<ParticipationRequestDto> rejectedRequests;
}