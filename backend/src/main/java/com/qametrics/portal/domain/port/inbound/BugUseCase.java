package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ProjectType;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para la gestión del BugTracker.
 */
public interface BugUseCase {

    List<Bug> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month);

    Optional<Bug> findById(Long id);

    Bug create(Bug bug);

    Bug update(Long id, Bug bug);

    void delete(Long id);

    /** Elimina todos los bugs sin filtro — solo ADMIN */
    void deleteAll();

    /** Elimina bugs por lista de IDs — solo ADMIN */
    void deleteByIds(List<Long> ids);

    /** Retorna sólo bugs con reinjection_flag = true */
    List<Bug> findReinjections(ProjectType projectType);
}
