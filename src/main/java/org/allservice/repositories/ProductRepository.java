package org.allservice.repositories;

import org.allservice.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByName(String name);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ProductEntity> searchByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:id IS NULL OR p.id = :id) AND " +
            "(:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ProductEntity> findFilteredProducts(
            @Param("id") Long id,
            @Param("name") String name,
            Pageable pageable
    );
}
