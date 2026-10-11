package dev.lab.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
class OrderController {

    record CreateOrderRequest(@NotBlank String customerId, @NotNull @Positive BigDecimal totalAmount) {}

    record OrderResponse(UUID id, String customerId, String status, BigDecimal totalAmount, Instant createdAt) {
        static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getCustomerId(), o.getStatus().name(),
                    o.getTotalAmount(), o.getCreatedAt());
        }
    }

    private final OrderRepository orders;

    OrderController(OrderRepository orders) { this.orders = orders; }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        var saved = orders.save(Order.create(req.customerId(), req.totalAmount()));
        return ResponseEntity.created(URI.create("/orders/" + saved.getId()))
                .body(OrderResponse.from(saved));
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return orders.findById(id).map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}