package org.allservice.dto.response;

import org.allservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponseDTO(Long id,
                                      String name,
                                      String description,
                                      BigDecimal totalValue,
                                      OrderStatus status,
                                      BigDecimal laborValue,
                                      LocalDateTime createdAt,
                                      String clientName) {
}
