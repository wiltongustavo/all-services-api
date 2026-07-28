package org.allservice.dto.request;

public record VehicleRequestDTO(Long id,
                                String plate,
                                String brand,
                                String model,
                                Integer year,
                                String color) {
}
