package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ClientReturnJpaRepository extends JpaRepository<ClientReturnEntity, Long> {

    @Query("SELECT r FROM ClientReturnEntity r LEFT JOIN r.clientDeliveryMetric m WHERE " +
           "(:projectType IS NULL OR r.projectType = :projectType OR (m IS NOT NULL AND m.projectType = :projectType)) AND " +
           "(:year IS NULL OR r.year = :year) AND " +
           "(:month IS NULL OR r.month = :month) " +
           "ORDER BY r.year DESC, r.month DESC, r.id DESC")
    List<ClientReturnEntity> findByFilters(
            @Param("projectType") String projectType,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT COUNT(r) FROM ClientReturnEntity r WHERE UPPER(r.ibl) = UPPER(:ibl)")
    int countByIbl(@Param("ibl") String ibl);
}
