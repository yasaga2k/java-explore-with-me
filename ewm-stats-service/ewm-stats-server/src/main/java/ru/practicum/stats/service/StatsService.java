package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHitEntity;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;

    public void saveHit(EndpointHitDto endpointHitDto) {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setApp(endpointHitDto.getApp());
        entity.setUri(endpointHitDto.getUri());
        entity.setIp(endpointHitDto.getIp());
        entity.setTimestamp(endpointHitDto.getTimestamp());
        statsRepository.save(entity);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return statsRepository.findStatsUnique(start, end, uris);
        } else {
            return statsRepository.findStats(start, end, uris);
        }
    }
}