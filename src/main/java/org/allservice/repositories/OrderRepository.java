package org.allservice.repositories;

import org.allservice.entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(value = "SELECT o FROM OrderEntity o LEFT JOIN FETCH o.client",
            countQuery = "SELECT count(o) FROM OrderEntity o")
    Page<OrderEntity> findAllPaged(Pageable pageable);

    List<OrderEntity> findByStatus(String status);
}
