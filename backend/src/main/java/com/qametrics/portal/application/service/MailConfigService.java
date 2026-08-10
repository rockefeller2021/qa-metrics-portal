package com.qametrics.portal.application.service;

import com.qametrics.portal.domain.model.MailConfigDto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio de Configuración Dinámica de Correo SMTP & Probador de Conexión en Tiempo Real.
 */
@Service
public class MailConfigService {

    private static final Logger log = LoggerFactory.getLogger(MailConfigService.class);

    private MailConfigDto currentConfig = new MailConfigDto(
            false,
            "smtp.gmail.com",
            587,
            "alertas@qametrics.com",
            "",
            "alertas@qametrics.com",
            "lideres_qa@qametrics.com",
            true,
            true
    );

    public MailConfigDto getConfig() {
        return currentConfig;
    }

    public MailConfigDto updateConfig(MailConfigDto dto) {
        this.currentConfig = dto;
        log.info("📧 Configuración SMTP actualizada: Host={}, Port={}, Enabled={}", dto.getHost(), dto.getPort(), dto.isEnabled());
        return this.currentConfig;
    }

    public Map<String, Object> sendTestEmail(String customRecipient) {
        Map<String, Object> result = new HashMap<>();
        String targetRecipient = (customRecipient != null && !customRecipient.isBlank())
                ? customRecipient.trim()
                : currentConfig.getRecipientEmail();

        log.info("🧪 Probando envío de correo SMTP hacia: {}", targetRecipient);

        try {
            JavaMailSenderImpl mailSender = createMailSender(currentConfig);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(currentConfig.getFromEmail() != null && !currentConfig.getFromEmail().isBlank()
                    ? currentConfig.getFromEmail() : "alertas@qametrics.com");
            helper.setTo(targetRecipient);
            helper.setSubject("🧪 PRUEBA EXITOSA DE CONEXIÓN SMTP — QA Metrics Portal");

            String htmlContent = """
                    <div style="font-family: 'Segoe UI', Arial, sans-serif; padding: 25px; background-color: #0a0a1a; color: #ffffff; border-radius: 16px;">
                        <h2 style="color: #10b981; margin-top: 0;">✅ Conexión SMTP Establecida Correctamente</h2>
                        <p style="color: #cbd5e1; font-size: 15px;">Este es un mensaje de prueba enviado desde el portal <strong>QA Metrics Portal</strong>.</p>
                        <hr style="border: 0; border-top: 1px solid rgba(255,255,255,0.1); margin: 20px 0;" />
                        <table style="width: 100%%; font-size: 13px; color: #94a3b8;">
                            <tr><td style="padding: 4px 0;"><strong>Servidor SMTP Host:</strong></td><td style="color: #ffffff;">%s</td></tr>
                            <tr><td style="padding: 4px 0;"><strong>Puerto:</strong></td><td style="color: #ffffff;">%d</td></tr>
                            <tr><td style="padding: 4px 0;"><strong>Usuario:</strong></td><td style="color: #ffffff;">%s</td></tr>
                            <tr><td style="padding: 4px 0;"><strong>Destinatario de Alertas:</strong></td><td style="color: #10b981; font-weight: bold;">%s</td></tr>
                        </table>
                        <p style="font-size: 12px; color: #64748b; margin-top: 20px;">Las alertas de desvíos SLA, Reinyecciones y Calidad < 95%% llegarán a esta casilla.</p>
                    </div>
                    """.formatted(currentConfig.getHost(), currentConfig.getPort(), currentConfig.getUsername(), targetRecipient);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            result.put("success", true);
            result.put("message", "✅ Correo de prueba enviado exitosamente a " + targetRecipient);
            log.info("✅ Envió de prueba exitoso a {}", targetRecipient);
        } catch (Exception e) {
            log.error("❌ Error enviando correo de prueba: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "❌ Falló el envío SMTP: " + e.getMessage());
        }

        return result;
    }

    public void sendReinjectionAlert(com.qametrics.portal.domain.model.Bug bug) {
        if (!currentConfig.isEnabled() || !currentConfig.isNotifyOnReinjection()) return;
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                JavaMailSenderImpl mailSender = createMailSender(currentConfig);
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String recipient = currentConfig.getRecipientEmail();
                helper.setFrom(currentConfig.getFromEmail() != null && !currentConfig.getFromEmail().isBlank() ? currentConfig.getFromEmail() : "alertas@qametrics.com");
                helper.setTo(recipient);
                helper.setSubject("🚨 ALERTA DE REINYECCIÓN (RF03) — " + bug.getBugJiraId());

                String htmlContent = """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; padding: 25px; background-color: #0a0a1a; color: #ffffff; border-radius: 16px;">
                            <h2 style="color: #ef4444; margin-top: 0;">🚨 ALERTA DE REINYECCIÓN DETECTADA (RF03)</h2>
                            <p style="color: #cbd5e1; font-size: 15px;">Se ha detectado una reinyección en el requerimiento <strong>%s</strong>.</p>
                            <hr style="border: 0; border-top: 1px solid rgba(255,255,255,0.1); margin: 20px 0;" />
                            <table style="width: 100%%; font-size: 13px; color: #94a3b8;">
                                <tr><td style="padding: 4px 0;"><strong>ID Bug Jira:</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Requerimiento (HU):</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Línea de Proyecto:</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Desarrollador Asignado:</strong></td><td style="color: #f59e0b; font-weight: bold;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Reportado Por (Analista):</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Sprint / PI:</strong></td><td style="color: #ffffff;">%s</td></tr>
                            </table>
                        </div>
                        """.formatted(
                        bug.getRequirementId(),
                        bug.getBugJiraId(),
                        bug.getRequirementId(),
                        bug.getProjectType() != null ? bug.getProjectType().name() : "N/A",
                        bug.getDeveloperName() != null ? bug.getDeveloperName() : "Sin Asignar",
                        bug.getReportedBy() != null ? bug.getReportedBy() : "Analista QA",
                        bug.getSprintOrPi() != null ? bug.getSprintOrPi() : "N/A"
                );

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("🚨 Alerta de reinyección enviada por correo a {}", recipient);
            } catch (Exception e) {
                log.warn("⚠️ No se pudo enviar alerta de reinyección por correo: {}", e.getMessage());
            }
        });
    }

    public void sendSlaDelayAlert(com.qametrics.portal.domain.model.DeliverySla delivery) {
        if (!currentConfig.isEnabled() || !currentConfig.isNotifyOnSlaDelay()) return;
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                JavaMailSenderImpl mailSender = createMailSender(currentConfig);
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String recipient = currentConfig.getRecipientEmail();
                helper.setFrom(currentConfig.getFromEmail() != null && !currentConfig.getFromEmail().isBlank() ? currentConfig.getFromEmail() : "alertas@qametrics.com");
                helper.setTo(recipient);
                helper.setSubject("⚠️ ALERTA DESVIACIÓN SLA (DELAYED) — " + delivery.getJiraId());

                String htmlContent = """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; padding: 25px; background-color: #0a0a1a; color: #ffffff; border-radius: 16px;">
                            <h2 style="color: #f59e0b; margin-top: 0;">⚠️ ALERTA DE DESVIACIÓN DE SLA (DELAYED)</h2>
                            <p style="color: #cbd5e1; font-size: 15px;">El hito de entrega <strong>%s</strong> se encuentra retrasado fuera de SLA.</p>
                            <hr style="border: 0; border-top: 1px solid rgba(255,255,255,0.1); margin: 20px 0;" />
                            <table style="width: 100%%; font-size: 13px; color: #94a3b8;">
                                <tr><td style="padding: 4px 0;"><strong>ID Hito Jira:</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Línea de Proyecto:</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Analista / Diseñador:</strong></td><td style="color: #ffffff;">%s</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Días de Retraso:</strong></td><td style="color: #ef4444; font-weight: bold;">%d días</td></tr>
                                <tr><td style="padding: 4px 0;"><strong>Fecha Compromiso:</strong></td><td style="color: #ffffff;">%s</td></tr>
                            </table>
                        </div>
                        """.formatted(
                        delivery.getJiraId(),
                        delivery.getJiraId(),
                        delivery.getProjectType() != null ? delivery.getProjectType().name() : "N/A",
                        delivery.getDesignerAnalyst() != null ? delivery.getDesignerAnalyst() : "Analista QA",
                        delivery.getDelayDays(),
                        delivery.getEstimatedDeliveryDate() != null ? delivery.getEstimatedDeliveryDate().toString() : "N/A"
                );

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("⚠️ Alerta de retraso SLA enviada por correo a {}", recipient);
            } catch (Exception e) {
                log.warn("⚠️ No se pudo enviar alerta de retraso SLA por correo: {}", e.getMessage());
            }
        });
    }

    private JavaMailSenderImpl createMailSender(MailConfigDto config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost() != null && !config.getHost().isBlank() ? config.getHost() : "smtp.gmail.com");
        mailSender.setPort(config.getPort() > 0 ? config.getPort() : 587);
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(config.isAuthRequired()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStartTlsEnabled()));
        props.put("mail.smtp.ssl.trust", config.getHost());
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        return mailSender;
    }
}
