package com.example.orderservice.service;

import com.example.orderservice.client.BookClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.BookDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final UserClient userClient;
    private final BookClient bookClient;

    public OrderService(OrderRepository repository, UserClient userClient, BookClient bookClient) {
        this.repository = repository;
        this.userClient = userClient;
        this.bookClient = bookClient;
    }

    public List<Order> getAll() {
        return repository.findAll();
    }

    public List<Order> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public Order getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    public Order create(OrderRequest request) {
        Order order = new Order();
        apply(order, request);
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PLACED);
        }
        return repository.save(order);
    }

    @Transactional
    public Order update(Long id, OrderRequest request) {
        Order order = getById(id);
        apply(order, request);
        return repository.save(order);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    /** Validates the user & book through their services and computes the total price. */
    private void apply(Order order, OrderRequest request) {
        userClient.getUser(request.getUserId());                    // throws 404 if the user does not exist
        BookDto book = bookClient.getBook(request.getBookId());     // throws 404 if the book does not exist

        order.setUserId(request.getUserId());
        order.setBookId(request.getBookId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(book.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
    }
}
