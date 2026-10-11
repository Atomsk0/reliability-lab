package dev.lab.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    public enum Status { CREATED, PAID, CANCELLED }

    @Id private UUID id;
    private String customerId;
    @Enumerated(EnumType.STRING) private Status status;
    private BigDecimal totalAmount;
    private Instant createdAt;

    protected Order() {}

    public static Order create(String customerId, BigDecimal totalAmount) {
        var o = new Order();
        o.id = UUID.randomUUID();
        o.customerId = customerId;
        o.status = Status.CREATED;
        o.totalAmount = totalAmount;
        o.createdAt = Instant.now();
        return o;
    }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public Status getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
}