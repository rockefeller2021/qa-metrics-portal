package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.TestExecution;
import com.qametrics.portal.domain.port.inbound.ReportUseCase;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import com.qametrics.portal.domain.model.ClientDeliveryMetric;
import com.qametrics.portal.domain.model.ClientReturn;
import com.qametrics.portal.domain.port.outbound.ClientTrackingRepository;
import com.qametrics.portal.infrastructure.adapters.out.reporting.ExcelReportGenerator;
import com.qametrics.portal.infrastructure.adapters.out.reporting.PdfReportGenerator;
import com.qametrics.portal.infrastructure.adapters.out.reporting.PptxReportGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de Uso — Generación de Reportes Multi-Formato (PDF, XLSX, PPTX).
 */
@Service
@Transactional(readOnly = true)
public class ReportUseCaseImpl implements ReportUseCase {

    private final TestExecutionRepository executionRepository;
    private final BugRepository bugRepository;
    private final DeliverySlaRepository deliverySlaRepository;
    private final ClientTrackingRepository clientTrackingRepository;

    private final PdfReportGenerator pdfGenerator;
    private final ExcelReportGenerator excelGenerator;
    private final PptxReportGenerator pptxGenerator;

    public ReportUseCaseImpl(TestExecutionRepository executionRepository,
                             BugRepository bugRepository,
                             DeliverySlaRepository deliverySlaRepository,
                             ClientTrackingRepository clientTrackingRepository,
                             PdfReportGenerator pdfGenerator,
                             ExcelReportGenerator excelGenerator,
                             PptxReportGenerator pptxGenerator) {
        this.executionRepository = executionRepository;
        this.bugRepository = bugRepository;
        this.deliverySlaRepository = deliverySlaRepository;
        this.clientTrackingRepository = clientTrackingRepository;
        this.pdfGenerator = pdfGenerator;
        this.excelGenerator = excelGenerator;
        this.pptxGenerator = pptxGenerator;
    }

    @Override
    public byte[] generatePdfReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst) {
        ProjectType pType = parseProjectType(projectType);
        List<TestExecution> execs = filterExecs(executionRepository.findAll(pType, null, year, month), designerAnalyst);
        List<Bug> bugs           = filterBugs(bugRepository.findAll(pType, null, year, month), developerName);
        List<DeliverySla> s      = filterDeliveries(deliverySlaRepository.findAll(pType, null, null, year, month), designerAnalyst);
        List<ClientDeliveryMetric> metrics = clientTrackingRepository.findAllMetrics(pType, year, month);
        List<ClientReturn> returns         = clientTrackingRepository.findAllReturns(pType, year, month);

        return pdfGenerator.generatePdf(execs, bugs, s, metrics, returns, projectType);
    }

    @Override
    public byte[] generateExcelReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst) {
        ProjectType pType = parseProjectType(projectType);
        List<TestExecution> execs = filterExecs(executionRepository.findAll(pType, null, year, month), designerAnalyst);
        List<Bug> bugs           = filterBugs(bugRepository.findAll(pType, null, year, month), developerName);
        List<DeliverySla> s      = filterDeliveries(deliverySlaRepository.findAll(pType, null, null, year, month), designerAnalyst);
        List<ClientDeliveryMetric> metrics = clientTrackingRepository.findAllMetrics(pType, year, month);
        List<ClientReturn> returns         = clientTrackingRepository.findAllReturns(pType, year, month);

        return excelGenerator.generateExcel(execs, bugs, s, metrics, returns);
    }

    @Override
    public byte[] generatePptxReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst) {
        ProjectType pType = parseProjectType(projectType);
        List<TestExecution> execs = filterExecs(executionRepository.findAll(pType, null, year, month), designerAnalyst);
        List<Bug> bugs           = filterBugs(bugRepository.findAll(pType, null, year, month), developerName);
        List<DeliverySla> s      = filterDeliveries(deliverySlaRepository.findAll(pType, null, null, year, month), designerAnalyst);
        List<ClientDeliveryMetric> metrics = clientTrackingRepository.findAllMetrics(pType, year, month);
        List<ClientReturn> returns         = clientTrackingRepository.findAllReturns(pType, year, month);

        return pptxGenerator.generatePptx(execs, bugs, s, metrics, returns, projectType);
    }

    private List<TestExecution> filterExecs(List<TestExecution> list, String designerAnalyst) {
        if (designerAnalyst == null || designerAnalyst.isBlank()) return list;
        return list.stream().filter(e -> e.getDesignerAnalyst() != null && e.getDesignerAnalyst().toLowerCase().contains(designerAnalyst.toLowerCase())).toList();
    }

    private List<Bug> filterBugs(List<Bug> list, String developerName) {
        if (developerName == null || developerName.isBlank()) return list;
        return list.stream().filter(b -> b.getDeveloperName() != null && b.getDeveloperName().toLowerCase().contains(developerName.toLowerCase())).toList();
    }

    private List<DeliverySla> filterDeliveries(List<DeliverySla> list, String designerAnalyst) {
        if (designerAnalyst == null || designerAnalyst.isBlank()) return list;
        return list.stream().filter(d -> d.getDesignerAnalyst() != null && d.getDesignerAnalyst().toLowerCase().contains(designerAnalyst.toLowerCase())).toList();
    }

    private ProjectType parseProjectType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return ProjectType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
