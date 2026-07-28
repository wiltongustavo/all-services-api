package org.allservice.dto.response;

import org.allservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal value,
        OrderStatus status,
        BigDecimal laborValue,
        LocalDateTime createdAt,
        String clientName,
        List<OrderItemResponseDTO> items,
        List<ClientPartResponseDTO> clientParts
) {}