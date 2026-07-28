package org.allservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.allservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequestDTO(@NotNull(message = "O ID do cliente é obrigatório.")
                              Long clientId,

                              @NotBlank(message = "O nome do pedido é obrigatório.")
                              @Size(max = 100, message = "O nome do pedido não pode passar de 100 caracteres.")
                              String name,

                              String description,

                              @NotNull(message = "O status do pedido é obrigatório.")
                              OrderStatus status,

                              @DecimalMin(value = "0.0", inclusive = true, message = "O valor da mão de obra não pode ser negativo.")
                              BigDecimal laborValue,


                              @NotNull(message = "O valor total é obrigatório.")
                              @DecimalMin(value = "0.0", inclusive = false, message = "O valor total deve ser maior que zero.")
                              BigDecimal totalValue,

                              Long vehicleId,

                              @NotEmpty(message = "O pedido deve conter pelo menos um produto.")
                              @Valid //
                              List<OrderItemRequestDTO> items,
                              @Valid
                              List<ClientPartRequestDTO> clientParts) {
}
