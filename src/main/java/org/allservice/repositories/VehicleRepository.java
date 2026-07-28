package org.allservice.repositories;

import org.allservice.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    List<VehicleEntity> findByClientId(Long clientId);
    Optional<VehicleEntity> findByPlate(String plate);
    boolean existsByPlate(String plate);

}