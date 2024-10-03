package vn.cloud.orderservice;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "orders", schema = "orders_schema")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    Long id;
    @Column(name = "customer_id")
    Long customerId;
    @Column(name = "order_date")
    ZonedDateTime orderDate;
    @Column(name = "total_amount")
    BigDecimal totalAmount;
}