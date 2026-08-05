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

    @EntityGraph(attributePaths = {"orders", "address", "vehicles"})
    @Query("SELECT DISTINCT c FROM ClientEntity c WHERE " +
            "(:id IS NULL OR c.id = :id) AND " +
            "(:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR :email = '' OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phone IS NULL OR :phone = '' OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :phone, '%')))")
    Page<ClientEntity> findFilteredClients(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            Pageable pageable
    );

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