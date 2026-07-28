package org.allservice.dto.request;

import jakarta.validation.constraints.*;


import java.time.LocalDate;
import java.util.List;

public record ClientRequestDTO(
        @NotBlank(message = "O nome do cliente é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        String phone,

        LocalDate dateOfBirth,


        AddressRequestDTO address,


        List<VehicleRequestDTO> vehicles
) {
}