package org.allservice.dto;

import java.math.BigDecimal;

public record OrderItem(Long productId, Integer quantity, BigDecimal soldPrice) {
}
