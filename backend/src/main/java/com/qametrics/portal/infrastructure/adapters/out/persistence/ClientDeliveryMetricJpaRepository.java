package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ClientDeliveryMetricJpaRepository extends JpaRepository<ClientDeliveryMetricEntity, Long> {

    @Query("SELECT m FROM ClientDeliveryMetricEntity m WHERE " +
           "(:projectType IS NULL OR m.projectType = :projectType) AND " +
           "(:year IS NULL OR m.year = :year) AND " +
           "(:month IS NULL OR m.month = :month) " +
           "ORDER BY m.year DESC, m.month DESC, m.id DESC")
    List<ClientDeliveryMetricEntity> findByFilters(
            @Param("projectType") String projectType,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT m FROM ClientDeliveryMetricEntity m WHERE " +
           "m.projectType = :projectType AND m.year = :year AND m.month = :month AND " +
           "((:sprintOrPeriod IS NULL AND (m.sprintOrPeriod IS NULL OR m.sprintOrPeriod = '')) OR UPPER(TRIM(m.sprintOrPeriod)) = UPPER(TRIM(:sprintOrPeriod)))")
    List<ClientDeliveryMetricEntity> findByPeriod(
            @Param("projectType") String projectType,
            @Param("year") int year,
            @Param("month") int month,
            @Param("sprintOrPeriod") String sprintOrPeriod);
}
