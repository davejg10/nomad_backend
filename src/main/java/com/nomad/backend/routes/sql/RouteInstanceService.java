package com.nomad.backend.routes.sql;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomad.backend.domain.ApiJobProducerRequest;
import com.nomad.backend.domain.CityDTO;
import com.nomad.backend.exceptions.NotFoundRequestException;
import com.nomad.data_library.domain.sql.RouteInstance;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RouteInstanceService {

    private final RouteInstanceRepository routeInstanceRepository;
    private final ObjectMapper objectMapper;
    
    public RouteInstanceService(RouteInstanceRepository routeInstanceRepository, ObjectMapper objectMapper) {
        this.routeInstanceRepository = routeInstanceRepository;
        this.objectMapper = objectMapper;
    }

    Optional<List<RouteInstance>> findByRouteDefinitionIdInAndSearchDate(CityDTO sourceCity, CityDTO targetCity, List<UUID> routeDefinitionIds, LocalDate searchDate, int attempt) {
        log.info("Fetched all route instances with searchDate: {}, for the following routeDefinitionIds: {}", searchDate, routeDefinitionIds);

        List<RouteInstance> routeInstances = routeInstanceRepository.findByRouteDefinitionIdInAndSearchDate(routeDefinitionIds, searchDate);

        if (routeInstances.isEmpty()) {
            log.warn("Route instances for given date {} could not be found.");
            if (attempt != 0) {
                log.warn("Scraper request for this date has already been made. Attempt: {}", attempt);
            } else {
                log.warn("This is the first attempt, therefore creating apiJobProducer request.");
                try {
                    requestScrape(sourceCity, targetCity, searchDate);
                } catch(IOException | InterruptedException e) {
                    log.error("fuck all");
                    throw new NotFoundRequestException("Route instances not found");
                }
            }
            return Optional.empty();    
        }
        
        log.info("Route instances found, returning instances");
        return Optional.of(routeInstances);
    }


    void requestScrape(CityDTO sourceCity, CityDTO targetCity, LocalDate searchDate) throws IOException, InterruptedException {
        // String url = "http://localhost:7072/api/apiJobProducer";
        String url = "https://fa-dev-uks-nomad-02-job-orchestrator.azurewebsites.net/api/apiJobProducer";
        HttpClient httpClient = HttpClient.newHttpClient();

        ApiJobProducerRequest requestBody = new ApiJobProducerRequest(sourceCity, targetCity, searchDate);

        log.info("Producing scrape request for apiJobProducer. Payload: {}", requestBody);

        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        log.info(response.body());

    }

}
