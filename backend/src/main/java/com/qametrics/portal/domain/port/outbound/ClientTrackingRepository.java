package com.qametrics.portal.domain.port.outbound;

import com.qametrics.portal.domain.model.ClientDeliveryMetric;
import com.qametrics.portal.domain.model.ClientReturn;
import com.qametrics.portal.domain.model.ProjectType;

import java.util.List;
import java.util.Optional;

public interface ClientTrackingRepository {
    List<ClientDeliveryMetric> findAllMetrics(ProjectType projectType, Integer year, Integer month);
    Optional<ClientDeliveryMetric> findMetricById(Long id);
    Optional<ClientDeliveryMetric> findMetricByPeriod(ProjectType projectType, int year, int month, String sprintOrPeriod);
    ClientDeliveryMetric saveMetric(ClientDeliveryMetric metric);
    void deleteMetricById(Long id);
    void deleteAllMetrics();

    List<ClientReturn> findAllReturns(ProjectType projectType, Integer year, Integer month);
    Optional<ClientReturn> findReturnById(Long id);
    ClientReturn saveReturn(ClientReturn clientReturn);
    void deleteReturnById(Long id);

    int countIblReturns(String ibl);
}
