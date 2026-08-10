package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.BugStatus;
import com.qametrics.portal.domain.model.DefectType;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.port.inbound.BugUseCase;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import com.qametrics.portal.application.service.MailConfigService;

/**
 * Caso de uso del BugTracker con detección automática de reinyecciones.
 */
@Service
@Transactional
public class BugUseCaseImpl implements BugUseCase {

    private final BugRepository bugRepository;
    private final MailConfigService mailConfigService;

    public BugUseCaseImpl(BugRepository bugRepository, MailConfigService mailConfigService) {
        this.bugRepository = bugRepository;
        this.mailConfigService = mailConfigService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bug> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month) {
        return bugRepository.findAll(projectType, sprintOrPi, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bug> findById(Long id) {
        return bugRepository.findById(id);
    }

    @Override
    public Bug create(Bug bug) {
        // Detección automática de reinyección:
        // Si el mismo requerimiento ya tuvo un bug RESUELTO previamente → es reinyección
        boolean wasResolved = bugRepository.existsByRequirementIdAndStatusResolved(bug.getRequirementId());
        if (wasResolved) {
            bug.setReinjectionFlag(true);
            bug.setDefectType(DefectType.REINJECTION);
        }
        Bug saved = bugRepository.save(bug);
        if (saved.isReinjectionFlag()) {
            mailConfigService.sendReinjectionAlert(saved);
        }
        return saved;
    }

    @Override
    public Bug update(Long id, Bug bug) {
        bugRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Bug no encontrado con ID: " + id));
        bug.setId(id);
        return bugRepository.save(bug);
    }

    @Override
    public void delete(Long id) {
        bugRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        bugRepository.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        bugRepository.deleteByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bug> findReinjections(ProjectType projectType) {
        return bugRepository.findReinjections(projectType);
    }
}
