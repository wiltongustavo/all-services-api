package org.allservice.dto.request;

public record AddressRequestDTO(String cep,
                                String street,
                                String number,
                                String complement,
                                String neighborhood,
                                String city,
                                String state) {
}
