package org.allservice.dto.response;

public record VehicleResponseDTO(Long id,
                                 String plate,
                                 String brand,
                                 String model,
                                 Integer year,
                                 String color) {
}
