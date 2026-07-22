package org.allservice.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ClientRequestDTO(@NotBlank(message = "O nome é obrigatório.")
                               @Size(max = 150, message = "O nome não pode passar de 150 caracteres.")
                               String name,

                               @NotBlank(message = "O email é obrigatório.")
                               @Email(message = "O formato do email é inválido.")
                               String email,

                               String phone,

                               @NotNull(message = "A data de nascimento é obrigatória.")
                               @Past(message = "A data de nascimento deve ser uma data no passado.")
                               LocalDate dateOfBirth) {
}


