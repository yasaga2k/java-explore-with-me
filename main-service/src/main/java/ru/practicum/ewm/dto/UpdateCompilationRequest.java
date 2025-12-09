package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    @JsonProperty("events")
    private List<Long> events;

    @JsonProperty("pinned")
    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    @JsonProperty("title")
    private String title;

    public boolean isPinned() {
        return pinned != null && pinned;
    }
}