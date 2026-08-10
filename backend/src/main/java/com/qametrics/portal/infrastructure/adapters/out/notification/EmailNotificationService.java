package com.qametrics.portal.infrastructure.adapters.out.notification;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.DeliverySla;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Servicio de Alertas Automatizadas & Notificaciones por Correo.
 * Envía correos HTML profesionales ante reinyecciones, incumplimientos SLA y alertas de calidad.
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:alertas@qametrics.com}")
    private String fromEmail;

    @Value("${app.mail.recipient:lideres_qa@qametrics.com}")
    private String defaultRecipient;

    @Async
    public void sendReinjectionAlert(Bug bug) {
        String subject = "🚨 ALERTA REINYECCIÓN DETECTADA — Bug " + bug.getBugJiraId() + " (HU: " + bug.getRequirementId() + ")";
        String htmlBody = """
                <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #0a0a1a; color: #ffffff;">
                    <h2 style="color: #f43f5e;">🚨 Detección Automática de Reinyección (RF03)</h2>
                    <p>Se ha registrado un defecto en un requerimiento que previamente ya contaba con incidencias resueltas:</p>
                    <ul>
                        <li><strong>Bug Jira ID:</strong> %s</li>
                        <li><strong>Requerimiento (HU):</strong> %s</li>
                        <li><strong>Línea de Operación:</strong> %s</li>
                        <li><strong>Tipo de Defecto:</strong> %s</li>
                        <li><strong>Reportado Por:</strong> %s</li>
                    </ul>
                    <p style="color: #cbd5e1;">Por favor revisar con el equipo de desarrollo la re-introducción del fallo.</p>
                </div>
                """.formatted(bug.getBugJiraId(), bug.getRequirementId(), bug.getProjectType(), bug.getDefectType(), bug.getReportedBy());

        sendEmail(defaultRecipient, subject, htmlBody);
    }

    @Async
    public void sendSlaBreachAlert(DeliverySla delivery) {
        String subject = "⚠️ ALERTA DE ATRASO SLA — Requerimiento " + delivery.getJiraId();
        String htmlBody = """
                <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #0a0a1a; color: #ffffff;">
                    <h2 style="color: #f43f5e;">⚠️ Alerta de Incumplimiento de Entrega (RF04)</h2>
                    <p>El requerimiento ha superado la fecha comprometida con el cliente:</p>
                    <ul>
                        <li><strong>ID Jira:</strong> %s</li>
                        <li><strong>Analista:</strong> %s</li>
                        <li><strong>Fecha Estimada:</strong> %s</li>
                        <li><strong>Días de Desviación:</strong> +%d Días</li>
                    </ul>
                </div>
                """.formatted(delivery.getJiraId(), delivery.getDesignerAnalyst(), delivery.getEstimatedDeliveryDate(), delivery.getDelayDays());

        sendEmail(defaultRecipient, subject, htmlBody);
    }

    @Async
    public void sendQualityWarningAlert(double currentQualityPct) {
        String subject = "🚨 ALERTA DE CALIDAD CRÍTICA — Porcentaje debajo del Target 95%";
        String htmlBody = """
                <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #0a0a1a; color: #ffffff;">
                    <h2 style="color: #f43f5e;">🚨 Porcentaje de Calidad por Debajo del Target 95%%</h2>
                    <p>El indicador consolidado de calidad ha bajado a: <strong style="font-size: 20px; color: #f43f5e;">%.2f%%</strong></p>
                    <p>Target de Calidad Esperado: 95.00%% [(1 - (Bugs / Casos OK)) * 100]</p>
                </div>
                """.formatted(currentQualityPct);

        sendEmail(defaultRecipient, subject, htmlBody);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        log.info("📧 [NOTIFICACIÓN ALERTA QA] Para: {} | Asunto: {}", to, subject);

        if (!mailEnabled || mailSender == null) {
            log.info("ℹ️ Alerta registrada en logs (SMTP desactivado o en modo simulador local).");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("✅ Correo enviado exitosamente a {}", to);
        } catch (Exception e) {
            log.error("❌ Error enviando correo electrónico: {}", e.getMessage());
        }
    }
}
