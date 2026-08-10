package com.qametrics.portal.domain.port.inbound;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Puerto de entrada (Inbound Port) para la importación masiva de datos (Excel / CSV).
 */
public interface BulkImportUseCase {

    Map<String, Object> importExecutions(MultipartFile file);

    Map<String, Object> importBugs(MultipartFile file);

    Map<String, Object> importDeliveries(MultipartFile file);

    byte[] generateSampleTemplate(String type);
}
