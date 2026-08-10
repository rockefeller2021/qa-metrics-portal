package com.qametrics.portal.domain.port.outbound;

import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.SlaStatus;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (Outbound Port) para la persistencia de Entregas SLA.
 */
public interface DeliverySlaRepository {

    List<DeliverySla> findAll(ProjectType projectType, SlaStatus status, String sprintOrPi, Integer year, Integer month);

    Optional<DeliverySla> findById(Long id);

    DeliverySla save(DeliverySla deliverySla);

    void deleteById(Long id);

    void deleteAll();

    void deleteByIds(List<Long> ids);
}
