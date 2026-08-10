package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.SlaStatus;
import com.qametrics.portal.domain.port.inbound.DeliverySlaUseCase;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.qametrics.portal.application.service.MailConfigService;

/**
 * Caso de Uso — Seguimiento de Entregas y Cálculo de Cumplimiento de SLA.
 */
@Service
@Transactional
public class DeliverySlaUseCaseImpl implements DeliverySlaUseCase {

    private final DeliverySlaRepository repository;
    private final MailConfigService mailConfigService;

    public DeliverySlaUseCaseImpl(DeliverySlaRepository repository, MailConfigService mailConfigService) {
        this.repository = repository;
        this.mailConfigService = mailConfigService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverySla> findAll(ProjectType projectType, SlaStatus status, String sprintOrPi, Integer year, Integer month) {
        List<DeliverySla> list = repository.findAll(projectType, status, sprintOrPi, year, month);
        // Recalcular SLA dinámicamente según la fecha actual
        list.forEach(DeliverySla::recalculateSlaStatus);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeliverySla> findById(Long id) {
        return repository.findById(id).map(item -> {
            item.recalculateSlaStatus();
            return item;
        });
    }

    @Override
    public DeliverySla create(DeliverySla deliverySla) {
        deliverySla.recalculateSlaStatus();
        DeliverySla saved = repository.save(deliverySla);
        if (SlaStatus.DELAYED.equals(saved.getStatus())) {
            mailConfigService.sendSlaDelayAlert(saved);
        }
        return saved;
    }

    @Override
    public DeliverySla update(Long id, DeliverySla deliverySla) {
        repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Hito de entrega no encontrado con ID: " + id));
        deliverySla.setId(id);
        deliverySla.recalculateSlaStatus();
        DeliverySla saved = repository.save(deliverySla);
        if (SlaStatus.DELAYED.equals(saved.getStatus())) {
            mailConfigService.sendSlaDelayAlert(saved);
        }
        return saved;
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDeliverySummary(ProjectType projectType) {
        List<DeliverySla> list = findAll(projectType, null, null, null, null);

        long total = list.size();
        long onTime = list.stream().filter(d -> SlaStatus.ON_TIME.equals(d.getStatus())).count();
        long delayed = list.stream().filter(d -> SlaStatus.DELAYED.equals(d.getStatus())).count();
        long pending = list.stream().filter(d -> SlaStatus.PENDING.equals(d.getStatus())).count();

        double slaComplianceRatio = (total > 0) ? ((double) onTime / total) * 100.0 : 100.0;
        slaComplianceRatio = Math.round(slaComplianceRatio * 10.0) / 10.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("onTime", onTime);
        summary.put("delayed", delayed);
        summary.put("pending", pending);
        summary.put("slaComplianceRatio", slaComplianceRatio);
        return summary;
    }
}
