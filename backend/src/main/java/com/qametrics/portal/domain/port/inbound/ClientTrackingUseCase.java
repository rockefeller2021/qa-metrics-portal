package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface ClientTrackingUseCase {
    List<ClientDeliveryMetric> findAllMetrics(ProjectType projectType, Integer year, Integer month);
    Optional<ClientDeliveryMetric> findMetricById(Long id);
    ClientDeliveryMetric createMetric(ClientDeliveryMetric metric);
    ClientDeliveryMetric updateMetric(Long id, ClientDeliveryMetric metric);
    void deleteMetric(Long id);
    void deleteAllMetrics();

    List<ClientReturn> findAllReturns(ProjectType projectType, Integer year, Integer month);
    ClientReturn createReturn(ClientReturn clientReturn);
    void deleteReturn(Long id);

    ClientTrackingSummary getSummary(ProjectType projectType, Integer year, Integer month);
}
