package org.allservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateClientResponseDTO(Long id,
                                      String name,
                                      String email,
                                      String phone,
                                      LocalDate dateOfBirth,
                                      LocalDateTime createAt,
                                      AddressResponseDTO address,
                                      List<VehicleResponseDTO> vehicles) {
}
