package com.example.orderservice.client;

import com.example.orderservice.dto.BookDto;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class BookClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BookClient(RestTemplate restTemplate, @Value("${services.book-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public BookDto getBook(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/api/books/" + id, BookDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Book not found with id " + id);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("book-service is not reachable");
        }
    }
}
