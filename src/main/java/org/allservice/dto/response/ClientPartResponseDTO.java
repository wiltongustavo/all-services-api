package org.allservice.dto.response;

import org.allservice.enums.PartCondition;
import java.math.BigDecimal;

public record ClientPartResponseDTO(
        Long id,
        String name,
        String brand,
        String serialNumber,
        PartCondition condition, // O Jackson (@JsonValue) cuida de mandar isso certinho (ex: "NEW")
        String description,
        BigDecimal declaredValue
) {}