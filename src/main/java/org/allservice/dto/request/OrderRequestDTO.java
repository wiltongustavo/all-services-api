package org.allservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(@NotNull(message = "O ID do cliente é obrigatório.")
                              Long clientId,

                              @NotBlank(message = "O nome do pedido é obrigatório.")
                              @Size(max = 100, message = "O nome do pedido não pode passar de 100 caracteres.")
                              String name,

                              String description,

                              @NotEmpty(message = "O pedido deve conter pelo menos um produto.")
                              @Valid //
                              List<OrderItemRequestDTO> items ){
}
