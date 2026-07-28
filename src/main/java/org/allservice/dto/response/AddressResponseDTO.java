package org.allservice.dto.response;

public record AddressResponseDTO(
        Long id,
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state) {
}
