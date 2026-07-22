package org.allservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponseDTO(Long id,
                                 String name,
                                 String description,
                                 BigDecimal value,
                                 LocalDateTime createdAt,
                                 Integer stock) {
}
