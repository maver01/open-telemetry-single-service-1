package vn.cloud.orderservice;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;


import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    Long id;
    Long customerId;
    ZonedDateTime orderDate;
    BigDecimal totalAmount;
}