package org.allservice.repositories;

import org.allservice.entities.ClientPartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientPartRepository extends JpaRepository<ClientPartEntity, Long> {

    @EntityGraph(attributePaths = {"order"})
    List<ClientPartEntity> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order"})
    List<ClientPartEntity> findAll();
}