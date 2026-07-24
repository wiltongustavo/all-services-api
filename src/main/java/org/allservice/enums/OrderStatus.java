package org.allservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING("PENDING", "Pendente"),
    IN_PROGRESS("IN_PROGRESS", "Em Andamento"),
    COMPLETED("COMPLETED", "Concluído");

    private final String chave;
    private final String descricao;

    OrderStatus(String chave, String descricao) {
        this.chave = chave;
        this.descricao = descricao;
    }

    @JsonValue // Define que o JSON usará a chave em inglês (ex: "PENDING") para comunicação com a API/Frontend
    public String getChave() {
        return chave;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    // Converte de forma segura qualquer variação enviada pelo client (Angular) de volta para o Enum correto
    public static OrderStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || status.chave.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de pedido inválido: " + value);
    }
}
