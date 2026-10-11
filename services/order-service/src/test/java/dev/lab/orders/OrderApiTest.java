package dev.lab.orders;

import static org.assertj.core.api.Assertions.*;

import dev.lab.orders.OrderController.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class OrderApiTest {

    @LocalServerPort int port;
    RestClient client;

    @BeforeEach
    void setUp() { client = RestClient.create("http://localhost:" + port); }

    @Test
    void createsAndFetchesAnOrder() {
        var created = client.post().uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateOrderRequest("customer-1", new BigDecimal("49.90")))
                .retrieve().toEntity(OrderResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var fetched = client.get().uri("/orders/{id}", created.getBody().id())
                .retrieve().body(OrderResponse.class);
        assertThat(fetched.status()).isEqualTo("CREATED");
    }

    @Test
    void unknownOrderReturns404() {
        assertThatThrownBy(() -> client.get().uri("/orders/{id}", UUID.randomUUID())
                .retrieve().toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void rejectsInvalidOrder() {
        assertThatThrownBy(() -> client.post().uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateOrderRequest("", new BigDecimal("-1")))
                .retrieve().toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }
}