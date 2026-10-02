package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer stock;
}
