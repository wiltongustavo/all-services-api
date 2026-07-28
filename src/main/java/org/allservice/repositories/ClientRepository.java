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

    // Adicionado "address" e "vehicles" no EntityGraph para carregar tudo junto na listagem paginada
    @Override
    @EntityGraph(attributePaths = {"orders", "address", "vehicles"})
    Page<ClientEntity> findAll(Pageable pageable);

    // Adicionado "address" e "vehicles" aqui também para a busca por nome
    @EntityGraph(attributePaths = {"orders", "address", "vehicles"})
    @Query(value = "SELECT DISTINCT c FROM ClientEntity c LEFT JOIN c.orders o WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c) FROM ClientEntity c WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ClientEntity> searchByName(@Param("name") String name, Pageable pageable);

    @EntityGraph(attributePaths = {"orders", "orders.items", "orders.items.product", "vehicles", "address"})
    Optional<ClientEntity> findWithDetailsById(Long id);

    @Query("SELECT c FROM ClientEntity c LEFT JOIN FETCH c.vehicles WHERE c.id = :id")
    Optional<ClientEntity> findByIdWithVehicles(@Param("id") Long id);
}