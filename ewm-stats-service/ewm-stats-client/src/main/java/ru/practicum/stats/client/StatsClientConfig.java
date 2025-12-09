package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ComponentScan(basePackages = "ru.practicum.stats.client")
public class StatsClientConfig {

    @Value("${stats-server.url:http://stats-server:9090}")
    private String statsServerUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(statsServerUrl)
                .build();
    }
}