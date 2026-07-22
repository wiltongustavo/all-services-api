package org.allservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(Long id, String name, String description, BigDecimal value, LocalDateTime createdAt ) {
}
