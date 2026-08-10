package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.SlaStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Puerto de entrada (Inbound Port) para el Módulo de Entregas y Gobierno SLA.
 */
public interface DeliverySlaUseCase {

    List<DeliverySla> findAll(ProjectType projectType, SlaStatus status, String sprintOrPi, Integer year, Integer month);

    Optional<DeliverySla> findById(Long id);

    DeliverySla create(DeliverySla deliverySla);

    DeliverySla update(Long id, DeliverySla deliverySla);

    void delete(Long id);

    /** Elimina todos los hitos SLA — solo ADMIN */
    void deleteAll();

    /** Elimina hitos SLA por lista de IDs — solo ADMIN */
    void deleteByIds(List<Long> ids);

    Map<String, Object> getDeliverySummary(ProjectType projectType);
}
