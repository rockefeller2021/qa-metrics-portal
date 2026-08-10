package com.qametrics.portal.infrastructure.adapters.in.rest;

import java.util.List;

/**
 * DTO compartido para peticiones de eliminación masiva.
 * Usado por BugController, TestExecutionController y DeliverySlaController.
 */
public record BatchDeleteRequest(boolean all, List<Long> ids) {}
