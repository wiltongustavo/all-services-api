package org.allservice.repositories;

import org.allservice.entities.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"orders"})
    Page<ClientEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"orders"})
    @Query(value = "SELECT DISTINCT c FROM ClientEntity c LEFT JOIN c.orders o WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c) FROM ClientEntity c WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ClientEntity> searchByName(@Param("name") String name, Pageable pageable);
}
