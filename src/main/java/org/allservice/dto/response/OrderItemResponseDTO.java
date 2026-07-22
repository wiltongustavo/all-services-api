package org.allservice.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDTO(Long id,
                                   Integer quantity,
                                   BigDecimal soldPrice,
                                   Long productId,
                                   String productName) {
}
