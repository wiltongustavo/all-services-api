package org.allservice.repositories;

import org.allservice.entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByClientId(Long clientId);

    @Query("SELECT DISTINCT obj FROM OrderEntity obj " +
            "JOIN FETCH obj.client " +
            "LEFT JOIN FETCH obj.vehicle " +
            "LEFT JOIN FETCH obj.items i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH obj.clientParts " +
            "WHERE obj.id = :id")
    Optional<OrderEntity> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT obj FROM OrderEntity obj JOIN FETCH obj.client LEFT JOIN FETCH obj.vehicle",
            countQuery = "SELECT COUNT(obj) FROM OrderEntity obj")
    Page<OrderEntity> findAllSummaryPaged(Pageable pageable);

    List<OrderEntity> findByStatus(String status);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "JOIN FETCH o.vehicle v " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH o.clientParts cp " +
            "WHERE v.id = :vehicleId " +
            "ORDER BY o.createdAt DESC") // <- Garantindo que a mais recente venha primeiro
    List<OrderEntity> findByVehicleIdWithDetails(@Param("vehicleId") Long vehicleId);
}