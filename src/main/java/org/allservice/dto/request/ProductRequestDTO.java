package org.allservice.dto.request;

import java.math.BigDecimal;

public record ProductRequestDTO(String name,
                                String description,
                                BigDecimal value,
                                Integer stock) {

}
