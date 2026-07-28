package org.allservice.repositories;

import org.allservice.entities.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    // Útil se você precisar buscar o endereço diretamente pelo ID do cliente
    Optional<AddressEntity> findByClientId(Long clientId);
}