package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataDeliverySlaRepository extends JpaRepository<DeliverySlaEntity, Long> {

    @Query("SELECT d FROM DeliverySlaEntity d WHERE " +
           "(:projectType IS NULL OR d.projectType = :projectType) AND " +
           "(:requestType IS NULL OR d.requestType = :requestType) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:sprintOrPi IS NULL OR LOWER(d.sprintOrPi) LIKE LOWER(CONCAT('%', :sprintOrPi, '%'))) AND " +
           "(:year IS NULL OR YEAR(d.estimatedDeliveryDate) = :year) AND " +
           "(:month IS NULL OR MONTH(d.estimatedDeliveryDate) = :month)")
    List<DeliverySlaEntity> findAllFiltered(String projectType, String requestType, String status, String sprintOrPi, Integer year, Integer month);
}
