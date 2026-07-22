package org.allservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal value,
        String status,
        LocalDateTime createdAt,
        String clientName,
        List<OrderItemResponseDTO> items
) {}