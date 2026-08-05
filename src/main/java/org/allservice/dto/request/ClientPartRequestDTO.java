package org.allservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.allservice.enums.PartCondition;

import java.math.BigDecimal;

public record ClientPartRequestDTO(

        @NotBlank(message = "O nome da peça é obrigatório.")
        @Size(max = 100, message = "O nome da peça não pode passar de 100 caracteres.")
        String name,

        @Size(max = 50, message = "A marca não pode passar de 50 caracteres.")
        String brand,

        @Size(max = 50, message = "O número de série não pode passar de 50 caracteres.")
        String serialNumber,

        @NotNull(message = "A condição da peça é obrigatória.")
        PartCondition condition,

        @NotNull(message = "esse campo e obrigatorio.")
        Boolean isClientPart,

        String description,

        @DecimalMin(value = "0.0", inclusive = true, message = "O valor declarado não pode ser negativo.")
        BigDecimal declaredValue
) {
}