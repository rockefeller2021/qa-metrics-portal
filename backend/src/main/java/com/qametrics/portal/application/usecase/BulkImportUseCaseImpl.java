package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.inbound.BulkImportUseCase;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Caso de Uso — Importación Masiva de Datos vía Excel / CSV.
 */
@Service
@Transactional
public class BulkImportUseCaseImpl implements BulkImportUseCase {

    private static final Logger log = LoggerFactory.getLogger(BulkImportUseCaseImpl.class);

    private final TestExecutionRepository executionRepository;
    private final BugRepository bugRepository;
    private final DeliverySlaRepository deliverySlaRepository;

    public BulkImportUseCaseImpl(TestExecutionRepository executionRepository,
                                 BugRepository bugRepository,
                                 DeliverySlaRepository deliverySlaRepository) {
        this.executionRepository = executionRepository;
        this.bugRepository = bugRepository;
        this.deliverySlaRepository = deliverySlaRepository;
    }

    @Override
    public Map<String, Object> importExecutions(MultipartFile file) {
        int importedCount = 0;
        List<String> errors = new ArrayList<>();
        String filename = file != null ? file.getOriginalFilename() : "desconocido";
        log.info("Iniciando importación masiva de ejecuciones desde archivo: {}", filename);

        try {
            if (isExcel(file)) {
                try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    if (sheet.getLastRowNum() < 1) {
                        errors.add("El archivo Excel no contiene filas de datos.");
                        return Map.of("importedCount", 0, "errors", errors);
                    }

                    // 1. Mapeo dinámico de columnas por inspección de encabezados en la Fila 0
                    Row headerRow = sheet.getRow(0);
                    int colJira = -1, colType = -1, colAnalyst = -1, colDesigned = -1;
                    int colTotal = -1, colOk = -1, colFail = -1, colBlock = -1, colSprint = -1, colDate = -1;

                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            String h = getCellValue(headerRow.getCell(c)).toLowerCase().trim();
                            if (h.contains("jira") || h.contains("id")) {
                                if (colJira == -1) colJira = c;
                            } else if (h.contains("línea") || h.contains("linea") || h.contains("proyecto") || h.contains("tipo")) {
                                if (colType == -1) colType = c;
                            } else if (h.contains("analista") || h.contains("qa") || h.contains("responsable")) {
                                if (colAnalyst == -1) colAnalyst = c;
                            } else if (h.contains("total")) {
                                colTotal = c;
                            } else if (h.contains("diseñad") || h.contains("diseño")) {
                                colDesigned = c;
                            } else if (h.contains("ok") || h.contains("exito")) {
                                colOk = c;
                            } else if (h.contains("fallid") || h.contains("fail") || h.contains("error")) {
                                colFail = c;
                            } else if (h.contains("bloquead") || h.contains("block")) {
                                colBlock = c;
                            } else if (h.contains("sprint") || h.contains("pi")) {
                                colSprint = c;
                            } else if (h.contains("fecha") || h.contains("date")) {
                                colDate = c;
                            }
                        }
                    }

                    // Fallbacks por defecto si no se detectaron por texto
                    colJira     = colJira != -1 ? colJira : 0;
                    colType     = colType != -1 ? colType : 1;
                    colAnalyst  = colAnalyst != -1 ? colAnalyst : 2;
                    colDesigned = colDesigned != -1 ? colDesigned : 3;

                    if (colTotal != -1) {
                        // Plantilla con columna "Total Ejecuciones"
                        colOk     = colOk != -1 ? colOk : 5;
                        colFail   = colFail != -1 ? colFail : 6;
                        colBlock  = colBlock != -1 ? colBlock : 7;
                        colSprint = colSprint != -1 ? colSprint : 8;
                        colDate   = colDate != -1 ? colDate : 9;
                    } else {
                        // Plantilla sin columna "Total Ejecuciones" (9 columnas)
                        colOk     = colOk != -1 ? colOk : 4;
                        colFail   = colFail != -1 ? colFail : 5;
                        colBlock  = colBlock != -1 ? colBlock : 6;
                        colSprint = colSprint != -1 ? colSprint : 7;
                        colDate   = colDate != -1 ? colDate : 8;
                    }

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String jiraId = "";
                        try {
                            jiraId = getCellValue(row.getCell(colJira));
                            if (jiraId.isBlank()) continue;

                            String typeStr = getCellValue(row.getCell(colType));
                            ProjectType projectType = "MINOR_DEMAND".equalsIgnoreCase(typeStr) ? ProjectType.MINOR_DEMAND : ProjectType.FABRICA;

                            String analyst = getCellValue(row.getCell(colAnalyst));
                            int designed = (int) parseDouble(getCellValue(row.getCell(colDesigned)), 0);
                            int ok = (int) parseDouble(getCellValue(row.getCell(colOk)), 0);
                            int fail = (int) parseDouble(getCellValue(row.getCell(colFail)), 0);
                            int block = (int) parseDouble(getCellValue(row.getCell(colBlock)), 0);

                            int totalExecutions;
                            if (colTotal != -1) {
                                totalExecutions = (int) parseDouble(getCellValue(row.getCell(colTotal)), ok + fail + block);
                            } else {
                                totalExecutions = ok + fail + block;
                            }
                            int actualExecutions = Math.max(totalExecutions, ok + fail + block);

                            String sprint = getCellValue(row.getCell(colSprint));
                            LocalDate execDate = getDateCellValue(row.getCell(colDate), LocalDate.now());

                            TestExecution exec = new TestExecution(
                                    null, jiraId, projectType, execDate, execDate,
                                    analyst.isBlank() ? "QA Analyst" : analyst,
                                    null, null, null,
                                    sprint.isBlank() ? "Sprint General" : sprint,
                                    "Importación Masiva Excel", totalOrDefault(designed, ok, fail, block), ok, fail, block,
                                    execDate.atStartOfDay(), new ArrayList<>()
                            );

                            ExecutionRun run = new ExecutionRun();
                            run.setRunNumber(1);
                            run.setExecutionDate(execDate);
                            run.setExecutedByAnalyst(analyst.isBlank() ? "QA Analyst" : analyst);
                            run.setStatus(fail > 0 ? RunStatus.FAILED : (block > 0 ? RunStatus.BLOCKED : RunStatus.SUCCESSFUL));
                            run.setCasesExecuted(actualExecutions);
                            run.setCasesPassed(ok);
                            run.setCasesFailed(fail);
                            run.setCasesBlocked(block);
                            run.setNotes("Importación Masiva Excel | Fecha: " + execDate);
                            exec.getRuns().add(run);

                            executionRepository.save(exec);
                            importedCount++;
                        } catch (Exception e) {
                            String errDetail = "Fila " + (i + 1) + " [" + (jiraId.isBlank() ? "ID no definido" : jiraId) + "]: " + e.getMessage();
                            log.warn(errDetail, e);
                            errors.add(errDetail);
                        }
                    }
                }
            } else {
                // Parse CSV
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    int lineNum = 0;
                    int colJira = 0, colType = 1, colAnalyst = 2, colDesigned = 3;
                    int colTotal = -1, colOk = 4, colFail = 5, colBlock = 6, colSprint = 7, colDate = 8;

                    while ((line = reader.readLine()) != null) {
                        lineNum++;
                        String[] parts = line.split(",");
                        if (parts.length < 4) continue;

                        if (lineNum == 1) { // Encabezados CSV
                            for (int c = 0; c < parts.length; c++) {
                                String h = parts[c].toLowerCase().trim();
                                if (h.contains("jira") || h.contains("id")) colJira = c;
                                else if (h.contains("línea") || h.contains("linea") || h.contains("tipo")) colType = c;
                                else if (h.contains("analista") || h.contains("qa")) colAnalyst = c;
                                else if (h.contains("total")) colTotal = c;
                                else if (h.contains("diseñad")) colDesigned = c;
                                else if (h.contains("ok") || h.contains("exito")) colOk = c;
                                else if (h.contains("fallid") || h.contains("fail")) colFail = c;
                                else if (h.contains("bloquead") || h.contains("block")) colBlock = c;
                                else if (h.contains("sprint") || h.contains("pi")) colSprint = c;
                                else if (h.contains("fecha") || h.contains("date")) colDate = c;
                            }
                            if (colTotal == -1 && parts.length >= 10) {
                                colTotal = 4; colOk = 5; colFail = 6; colBlock = 7; colSprint = 8; colDate = 9;
                            }
                            continue;
                        }

                        String jiraId = "";
                        try {
                            jiraId = parts.length > colJira ? parts[colJira].trim() : "";
                            if (jiraId.isBlank()) continue;

                            String typeStr = parts.length > colType ? parts[colType].trim() : "";
                            ProjectType pType = typeStr.toUpperCase().contains("MINOR") ? ProjectType.MINOR_DEMAND : ProjectType.FABRICA;

                            String analyst = parts.length > colAnalyst ? parts[colAnalyst].trim() : "QA Analyst";
                            int designed = parts.length > colDesigned ? (int) parseDouble(parts[colDesigned].trim(), 0) : 0;
                            int ok       = parts.length > colOk ? (int) parseDouble(parts[colOk].trim(), 0) : 0;
                            int fail     = parts.length > colFail ? (int) parseDouble(parts[colFail].trim(), 0) : 0;
                            int block    = parts.length > colBlock ? (int) parseDouble(parts[colBlock].trim(), 0) : 0;

                            int totalExecs;
                            if (colTotal != -1 && parts.length > colTotal) {
                                totalExecs = (int) parseDouble(parts[colTotal].trim(), ok + fail + block);
                            } else {
                                totalExecs = ok + fail + block;
                            }
                            int actualExecutions = Math.max(totalExecs, ok + fail + block);

                            String sprint = parts.length > colSprint ? parts[colSprint].trim() : "Sprint General";
                            LocalDate execDate = parts.length > colDate ? parseDate(parts[colDate].trim(), LocalDate.now()) : LocalDate.now();

                            TestExecution exec = new TestExecution(
                                    null, jiraId, pType, execDate, execDate, analyst,
                                    null, null, null,
                                    sprint.isBlank() ? "Sprint General" : sprint, "Importación CSV", designed, ok, fail, block,
                                    execDate.atStartOfDay(), new ArrayList<>()
                            );

                            ExecutionRun run = new ExecutionRun();
                            run.setRunNumber(1);
                            run.setExecutionDate(execDate);
                            run.setExecutedByAnalyst(analyst.isBlank() ? "QA Analyst" : analyst);
                            run.setStatus(fail > 0 ? RunStatus.FAILED : (block > 0 ? RunStatus.BLOCKED : RunStatus.SUCCESSFUL));
                            run.setCasesExecuted(actualExecutions);
                            run.setCasesPassed(ok);
                            run.setCasesFailed(fail);
                            run.setCasesBlocked(block);
                            run.setNotes("Importación CSV | Fecha: " + execDate);
                            exec.getRuns().add(run);

                            executionRepository.save(exec);
                            importedCount++;
                        } catch (Exception e) {
                            String errDetail = "Línea " + lineNum + " [" + (jiraId.isBlank() ? "Línea " + lineNum : jiraId) + "]: " + e.getMessage();
                            log.warn(errDetail, e);
                            errors.add(errDetail);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error crítico procesando archivo de importación de ejecuciones: {}", filename, e);
            errors.add("Error crítico al procesar archivo: " + e.getMessage());
        }

        log.info("Importación de ejecuciones finalizada. Registros procesados: {}, Errores: {}", importedCount, errors.size());

        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", importedCount);
        response.put("errors", errors);
        return response;
    }


    @Override
    public Map<String, Object> importBugs(MultipartFile file) {
        int importedCount = 0;
        List<String> errors = new ArrayList<>();
        String filename = file != null ? file.getOriginalFilename() : "desconocido";
        log.info("Iniciando importación masiva de bugs desde archivo: {}", filename);

        try {
            if (isExcel(file)) {
                try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    if (sheet.getLastRowNum() < 1) {
                        errors.add("El archivo Excel no contiene filas de datos.");
                        return Map.of("importedCount", 0, "errors", errors);
                    }

                    // Mapeo dinámico de columnas por inspección de la Fila 0 (Header Row)
                    Row headerRow = sheet.getRow(0);
                    int colBugJira = -1, colReq = -1, colType = -1, colDefect = -1;
                    int colReporter = -1, colDev = -1, colSprint = -1, colDate = -1;

                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            String h = getCellValue(headerRow.getCell(c)).toLowerCase().trim();
                            if (h.contains("bug") || (h.contains("jira") && !h.contains("req") && !h.contains("hu"))) {
                                if (colBugJira == -1) colBugJira = c;
                            } else if (h.contains("req") || h.contains("hu") || h.contains("requerimiento")) {
                                if (colReq == -1) colReq = c;
                            } else if (h.contains("línea") || h.contains("linea") || h.contains("proyecto")) {
                                if (colType == -1) colType = c;
                            } else if (h.contains("defecto") || h.contains("tipo")) {
                                if (colDefect == -1) colDefect = c;
                            } else if (h.contains("reportado") || h.contains("autor") || h.contains("reporter")) {
                                if (colReporter == -1) colReporter = c;
                            } else if (h.contains("desarrollador") || h.contains("dev") || h.contains("asignado")) {
                                if (colDev == -1) colDev = c;
                            } else if (h.contains("sprint") || h.contains("pi")) {
                                if (colSprint == -1) colSprint = c;
                            } else if (h.contains("fecha") || h.contains("date") || h.contains("reporte") || h.contains("creac")) {
                                if (colDate == -1) colDate = c;
                            }
                        }
                    }

                    // Fallbacks por defecto si no se detectaron por texto
                    colBugJira  = colBugJira  != -1 ? colBugJira  : 0;
                    colReq      = colReq      != -1 ? colReq      : 1;
                    colType     = colType     != -1 ? colType     : 2;
                    colDefect   = colDefect   != -1 ? colDefect   : 3;
                    colReporter = colReporter != -1 ? colReporter : 4;
                    colDev      = colDev      != -1 ? colDev      : 5;
                    colSprint   = colSprint   != -1 ? colSprint   : 6;
                    colDate     = colDate     != -1 ? colDate     : 7;

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String bugJiraId = "";
                        try {
                            bugJiraId = getCellValue(row.getCell(colBugJira));
                            String reqId = getCellValue(row.getCell(colReq));
                            if (bugJiraId.isBlank() && reqId.isBlank()) continue;
                            if (bugJiraId.isBlank()) bugJiraId = "BUG-" + System.currentTimeMillis();

                            String typeStr = getCellValue(row.getCell(colType));
                            ProjectType pType = "MINOR_DEMAND".equalsIgnoreCase(typeStr) ? ProjectType.MINOR_DEMAND : ProjectType.FABRICA;

                            String defectStr = getCellValue(row.getCell(colDefect));
                            DefectType defectType = parseDefectType(defectStr);

                            String reporter = getCellValue(row.getCell(colReporter));
                            String developer = getCellValue(row.getCell(colDev));
                            String sprint = getCellValue(row.getCell(colSprint));
                            if (sprint.isBlank() && !developer.isBlank() && (developer.startsWith("Sprint") || developer.startsWith("PI"))) {
                                sprint = developer;
                                developer = "";
                            }
                            LocalDate reportedDate = getDateCellValue(row.getCell(colDate), LocalDate.now());

                            // Auto-detección RF03
                            boolean isReinjection = bugRepository.existsByRequirementIdAndStatusResolved(reqId);

                            Bug bug = new Bug();
                            bug.setBugJiraId(bugJiraId);
                            bug.setRequirementId(reqId);
                            bug.setProjectType(pType);
                            bug.setDefectType(defectType);
                            bug.setReinjectionFlag(isReinjection);
                            bug.setStatus(BugStatus.OPEN);
                            bug.setReportedBy(reporter.isBlank() ? "Analista QA" : reporter);
                            bug.setDeveloperName(developer.isBlank() ? "Dev Pendiente" : developer);
                            bug.setReportedDate(reportedDate);
                            bug.setSprintOrPi(sprint.isBlank() ? "Sprint General" : sprint);
                            bug.setDescription("Importación Masiva Excel | Fecha Reporte: " + reportedDate);
                            bug.setCreatedAt(reportedDate.atStartOfDay());

                            bugRepository.save(bug);
                            importedCount++;
                        } catch (Exception e) {
                            String errDetail = "Fila " + (i + 1) + " [" + (bugJiraId.isBlank() ? "S/N" : bugJiraId) + "]: " + e.getMessage();
                            log.warn(errDetail, e);
                            errors.add(errDetail);
                        }
                    }
                }
            } else {
                // Parse CSV para Bugs
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    int lineNum = 0;
                    int colBugJira = 0, colReq = 1, colType = 2, colDefect = 3;
                    int colReporter = 4, colDev = 5, colSprint = 6, colDate = 7;

                    while ((line = reader.readLine()) != null) {
                        lineNum++;
                        String[] parts = line.split(",");
                        if (parts.length < 2) continue;

                        if (lineNum == 1) { // Encabezados CSV
                            for (int c = 0; c < parts.length; c++) {
                                String h = parts[c].toLowerCase().trim();
                                if (h.contains("bug") || (h.contains("jira") && !h.contains("req"))) colBugJira = c;
                                else if (h.contains("req") || h.contains("hu")) colReq = c;
                                else if (h.contains("línea") || h.contains("linea")) colType = c;
                                else if (h.contains("defecto") || h.contains("tipo")) colDefect = c;
                                else if (h.contains("reportado") || h.contains("qa")) colReporter = c;
                                else if (h.contains("dev") || h.contains("desarrollador")) colDev = c;
                                else if (h.contains("sprint") || h.contains("pi")) colSprint = c;
                                else if (h.contains("fecha") || h.contains("date")) colDate = c;
                            }
                            continue;
                        }

                        String bugJiraId = "";
                        try {
                            bugJiraId = parts.length > colBugJira ? parts[colBugJira].trim() : "";
                            String reqId = parts.length > colReq ? parts[colReq].trim() : "";
                            if (bugJiraId.isBlank() && reqId.isBlank()) continue;

                            String typeStr = parts.length > colType ? parts[colType].trim() : "";
                            ProjectType pType = typeStr.toUpperCase().contains("MINOR") ? ProjectType.MINOR_DEMAND : ProjectType.FABRICA;

                            DefectType defectType = parseDefectType(parts.length > colDefect ? parts[colDefect].trim() : "");
                            String reporter = parts.length > colReporter ? parts[colReporter].trim() : "Analista QA";
                            String developer = parts.length > colDev ? parts[colDev].trim() : "Dev Pendiente";
                            String sprint = parts.length > colSprint ? parts[colSprint].trim() : "Sprint General";
                            LocalDate reportedDate = parts.length > colDate ? parseDate(parts[colDate].trim(), LocalDate.now()) : LocalDate.now();

                            boolean isReinjection = bugRepository.existsByRequirementIdAndStatusResolved(reqId);

                            Bug bug = new Bug();
                            bug.setBugJiraId(bugJiraId);
                            bug.setRequirementId(reqId);
                            bug.setProjectType(pType);
                            bug.setDefectType(defectType);
                            bug.setReinjectionFlag(isReinjection);
                            bug.setStatus(BugStatus.OPEN);
                            bug.setReportedBy(reporter.isBlank() ? "Analista QA" : reporter);
                            bug.setDeveloperName(developer.isBlank() ? "Dev Pendiente" : developer);
                            bug.setReportedDate(reportedDate);
                            bug.setSprintOrPi(sprint.isBlank() ? "Sprint General" : sprint);
                            bug.setDescription("Importación CSV | Fecha Reporte: " + reportedDate);
                            bug.setCreatedAt(reportedDate.atStartOfDay());

                            bugRepository.save(bug);
                            importedCount++;
                        } catch (Exception e) {
                            String errDetail = "Línea " + lineNum + " [" + (bugJiraId.isBlank() ? "Línea " + lineNum : bugJiraId) + "]: " + e.getMessage();
                            log.warn(errDetail, e);
                            errors.add(errDetail);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error crítico al importar bugs desde archivo: {}", filename, e);
            errors.add("Error crítico al procesar archivo de bugs: " + e.getMessage());
        }

        log.info("Importación de bugs finalizada. Registros procesados: {}, Errores: {}", importedCount, errors.size());

        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", importedCount);
        response.put("errors", errors);
        return response;
    }

    @Override
    public Map<String, Object> importDeliveries(MultipartFile file) {
        int importedCount = 0;
        List<String> errors = new ArrayList<>();
        String filename = file != null ? file.getOriginalFilename() : "desconocido";
        log.info("Iniciando importación masiva de entregas SLA desde archivo: {}", filename);

        try {
            if (isExcel(file)) {
                try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    if (sheet.getLastRowNum() < 1) {
                        errors.add("El archivo Excel no contiene filas de datos.");
                        return Map.of("importedCount", 0, "errors", errors);
                    }

                    // Mapeo dinámico de columnas por inspección de la Fila 0
                    Row headerRow = sheet.getRow(0);
                    int colJira = -1, colType = -1, colAnalyst = -1, colEstDate = -1, colSprint = -1, colRegDate = -1;

                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            String h = getCellValue(headerRow.getCell(c)).toLowerCase().trim();
                            if (h.contains("jira") || h.contains("req")) {
                                if (colJira == -1) colJira = c;
                            } else if (h.contains("línea") || h.contains("linea") || h.contains("proyecto")) {
                                if (colType == -1) colType = c;
                            } else if (h.contains("analista") || h.contains("qa")) {
                                if (colAnalyst == -1) colAnalyst = c;
                            } else if (h.contains("estimad") || h.contains("compromiso")) {
                                if (colEstDate == -1) colEstDate = c;
                            } else if (h.contains("sprint") || h.contains("pi")) {
                                if (colSprint == -1) colSprint = c;
                            } else if (h.contains("registro") || h.contains("fecha")) {
                                if (colRegDate == -1) colRegDate = c;
                            }
                        }
                    }

                    colJira    = colJira    != -1 ? colJira    : 0;
                    colType    = colType    != -1 ? colType    : 1;
                    colAnalyst = colAnalyst != -1 ? colAnalyst : 2;
                    colEstDate = colEstDate != -1 ? colEstDate : 3;
                    colSprint  = colSprint  != -1 ? colSprint  : 4;
                    colRegDate = colRegDate != -1 ? colRegDate : 5;

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String jiraId = "";
                        try {
                            jiraId = getCellValue(row.getCell(colJira));
                            if (jiraId.isBlank()) continue;

                            String typeStr = getCellValue(row.getCell(colType));
                            ProjectType pType = "MINOR_DEMAND".equalsIgnoreCase(typeStr) ? ProjectType.MINOR_DEMAND : ProjectType.FABRICA;

                            String analyst = getCellValue(row.getCell(colAnalyst));
                            LocalDate estDate = getDateCellValue(row.getCell(colEstDate), LocalDate.now().plusDays(7));
                            String sprintStr = getCellValue(row.getCell(colSprint));
                            LocalDate regDate = getDateCellValue(row.getCell(colRegDate), LocalDate.now());

                            DeliverySla sla = new DeliverySla();
                            sla.setJiraId(jiraId);
                            sla.setProjectType(pType);
                            sla.setDesignerAnalyst(analyst.isBlank() ? "Analista QA" : analyst);
                            sla.setEstimatedDeliveryDate(estDate);
                            sla.setEstimatedQaDate(estDate.minusDays(2));
                            sla.setSprintOrPi(sprintStr.isBlank() ? "Sprint General" : sprintStr);
                            sla.setNotes("Importación Masiva SLA");
                            sla.setCreatedAt(regDate.atStartOfDay());
                            sla.recalculateSlaStatus();

                            deliverySlaRepository.save(sla);
                            importedCount++;
                        } catch (Exception e) {
                            String errDetail = "Fila " + (i + 1) + " [" + (jiraId.isBlank() ? "S/N" : jiraId) + "]: " + e.getMessage();
                            log.warn(errDetail, e);
                            errors.add(errDetail);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error crítico al importar entregas SLA desde archivo: {}", filename, e);
            errors.add("Error crítico al procesar archivo de entregas SLA: " + e.getMessage());
        }

        log.info("Importación de entregas SLA finalizada. Registros procesados: {}, Errores: {}", importedCount, errors.size());

        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", importedCount);
        response.put("errors", errors);
        return response;
    }

    @Override
    public byte[] generateSampleTemplate(String type) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Plantilla Importación");
            Row headerRow = sheet.createRow(0);

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers;
            if ("bugs".equalsIgnoreCase(type)) {
                headers = new String[]{"ID Bug Jira *", "Requerimiento (HU) *", "Línea (FABRICA/MINOR_DEMAND)", "Tipo Defecto (FUNCTIONAL/UI_UX)", "Reportado Por", "Desarrollador Asignado", "Sprint/PI", "Fecha de Reporte (YYYY-MM-DD)"};
            } else if ("deliveries".equalsIgnoreCase(type)) {
                headers = new String[]{"ID Jira Requerimiento *", "Línea (FABRICA/MINOR_DEMAND)", "Analista QA *", "Fecha Estimada Entrega (YYYY-MM-DD) *", "Sprint/PI", "Fecha de Registro (YYYY-MM-DD)"};
            } else {
                headers = new String[]{"ID Jira *", "Línea (FABRICA/MINOR_DEMAND)", "Analista QA *", "Casos Diseñados *", "Total Ejecuciones *", "Casos OK Exitosos", "Casos Fallidos", "Casos Bloqueados", "Sprint/PI", "Fecha de Ejecución (YYYY-MM-DD)"};
            }

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // Fila de ejemplo
            Row exampleRow = sheet.createRow(1);
            if ("bugs".equalsIgnoreCase(type)) {
                exampleRow.createCell(0).setCellValue("BUG-901");
                exampleRow.createCell(1).setCellValue("JIRA-101");
                exampleRow.createCell(2).setCellValue("FABRICA");
                exampleRow.createCell(3).setCellValue("FUNCTIONAL");
                exampleRow.createCell(4).setCellValue("Maria Analista");
                exampleRow.createCell(5).setCellValue("Pedro Developer");
                exampleRow.createCell(6).setCellValue("Sprint 15");
                exampleRow.createCell(7).setCellValue(LocalDate.now().minusMonths(1).toString());
            } else if ("deliveries".equalsIgnoreCase(type)) {
                exampleRow.createCell(0).setCellValue("JIRA-202");
                exampleRow.createCell(1).setCellValue("MINOR_DEMAND");
                exampleRow.createCell(2).setCellValue("Carlos QA");
                exampleRow.createCell(3).setCellValue(LocalDate.now().plusDays(10).toString());
                exampleRow.createCell(4).setCellValue("Sprint 15");
                exampleRow.createCell(5).setCellValue(LocalDate.now().minusMonths(1).toString());
            } else {
                exampleRow.createCell(0).setCellValue("JIRA-101");
                exampleRow.createCell(1).setCellValue("FABRICA");
                exampleRow.createCell(2).setCellValue("Juan Perez");
                exampleRow.createCell(3).setCellValue(20);
                exampleRow.createCell(4).setCellValue(20);
                exampleRow.createCell(5).setCellValue(18);
                exampleRow.createCell(6).setCellValue(2);
                exampleRow.createCell(7).setCellValue(0);
                exampleRow.createCell(8).setCellValue("Sprint 15");
                exampleRow.createCell(9).setCellValue(LocalDate.now().minusMonths(1).toString());
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error creando plantilla muestra: " + e.getMessage(), e);
        }
    }

    private boolean isExcel(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && (name.endsWith(".xlsx") || name.endsWith(".xls"));
    }

    /**
     * Obtiene el valor de texto de una celda.
     * Para celdas numéricas con formato de fecha, retorna YYYY-MM-DD.
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Celda con formato de fecha de Excel → devolver ISO para que parseDate la procese
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    /**
     * Lee una celda como LocalDate, gestionando los tres posibles formatos:
     * 1. Celda con formato de fecha de Excel (número serial)
     * 2. Celda de texto en formato YYYY-MM-DD u otros formatos soportados
     * 3. Fallback al valor por defecto si la celda está vacía o no se puede parsear
     */
    private LocalDate getDateCellValue(Cell cell, LocalDate defaultValue) {
        if (cell == null) return defaultValue;

        // Caso 1: la celda ya es una fecha Excel (tipo numérico con formato fecha)
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            try {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } catch (Exception ignored) {}
        }

        // Caso 2: texto o número sin formato → convertir a String y parsear
        String strValue = getCellValue(cell);
        return parseDate(strValue, defaultValue);
    }

    private double parseDouble(String str, double def) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return def;
        }
    }

    private int totalOrDefault(int designed, int ok, int fail, int block) {
        int sum = ok + fail + block;
        return Math.max(designed, sum);
    }

    private DefectType parseDefectType(String str) {
        try {
            return DefectType.valueOf(str.toUpperCase().trim());
        } catch (Exception e) {
            return DefectType.FUNCTIONAL;
        }
    }

    private LocalDate parseDate(String str, LocalDate def) {
        if (str == null || str.isBlank()) return def;
        String s = str.trim();

        // 1. ISO format YYYY-MM-DD (el más común)
        try { return LocalDate.parse(s); } catch (Exception ignored) {}

        // 2. Multi-pattern parsing (incluye formatos con día/mes de 1 dígito)
        String[] formats = {
            "d/M/yyyy", "dd/MM/yyyy", "d-M-yyyy", "dd-MM-yyyy",
            "yyyy/MM/dd", "yyyy/M/d", "yyyy.MM.dd",
            "MM/dd/yyyy", "M/d/yyyy",
            "dd/MM/yy", "d/M/yy", "dd-MM-yy", "yyyyMMdd"
        };
        for (String fmt : formats) {
            try {
                return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception ignored) {}
        }

        // 3. Fallback manual por separadores
        try {
            String[] parts = s.split("[/. -]");
            if (parts.length == 3) {
                int p1 = Integer.parseInt(parts[0]);
                int p2 = Integer.parseInt(parts[1]);
                int p3 = Integer.parseInt(parts[2]);
                if (p1 > 1000)      return LocalDate.of(p1, p2, p3); // YYYY-M-D
                else if (p3 > 1000) return LocalDate.of(p3, p2, p1); // D-M-YYYY
            }
        } catch (Exception ignored) {}

        return def;
    }
}
