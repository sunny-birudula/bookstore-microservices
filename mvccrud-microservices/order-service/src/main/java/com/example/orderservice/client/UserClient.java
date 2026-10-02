package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate, @Value("${services.user-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public UserDto getUser(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/api/users/" + id, UserDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("User not found with id " + id);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("user-service is not reachable");
        }
    }
}
