package org.allservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateClientResponseDTO(Long id,
                                      String name,
                                      String email,
                                      String phone,
                                      LocalDate dateOfBirth,
                                      LocalDateTime createAt) {
}
