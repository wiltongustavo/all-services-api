package org.allservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PartCondition {
    NEW("NEW", "Nova"),
    USED("USED", "Usada"),
    REFURBISHED("REFURBISHED", "Recondicionada");

    private final String chave;
    private final String descricao;

    PartCondition(String chave, String descricao) {
        this.chave = chave;
        this.descricao = descricao;
    }

    @JsonValue
    public String getChave() {
        return chave;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static PartCondition fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (PartCondition condition : PartCondition.values()) {
            if (condition.name().equalsIgnoreCase(value) || condition.chave.equalsIgnoreCase(value)) {
                return condition;
            }
        }
        throw new IllegalArgumentException("Condição da peça inválida: " + value);
    }
}