package ru.practicum.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClient {

    private final RestClient restClient;

    public StatsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void addHit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri("/hit")
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates must be provided");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris);
        }

        if (unique != null) {
            uriBuilder.queryParam("unique", unique);
        }

        return restClient.get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
    }
}