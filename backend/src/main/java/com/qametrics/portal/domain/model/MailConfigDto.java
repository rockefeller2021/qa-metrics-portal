package com.qametrics.portal.domain.model;

/**
 * DTO para la configuración dinámica del Servidor SMTP y Alertas por Correo.
 */
public class MailConfigDto {

    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String fromEmail;
    private String recipientEmail;
    private boolean authRequired;
    private boolean startTlsEnabled;

    private boolean notifyOnReinjection = true;
    private boolean notifyOnSlaDelay = true;

    public MailConfigDto() {}

    public MailConfigDto(boolean enabled, String host, int port, String username, String password,
                         String fromEmail, String recipientEmail, boolean authRequired, boolean startTlsEnabled) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.recipientEmail = recipientEmail;
        this.authRequired = authRequired;
        this.startTlsEnabled = startTlsEnabled;
        this.notifyOnReinjection = true;
        this.notifyOnSlaDelay = true;
    }

    public boolean isEnabled()                      { return enabled; }
    public void setEnabled(boolean enabled)          { this.enabled = enabled; }

    public String getHost()                         { return host; }
    public void setHost(String host)                { this.host = host; }

    public int getPort()                            { return port; }
    public void setPort(int port)                   { this.port = port; }

    public String getUsername()                     { return username; }
    public void setUsername(String username)        { this.username = username; }

    public String getPassword()                     { return password; }
    public void setPassword(String password)        { this.password = password; }

    public String getFromEmail()                    { return fromEmail; }
    public void setFromEmail(String fromEmail)      { this.fromEmail = fromEmail; }

    public String getRecipientEmail()               { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail){ this.recipientEmail = recipientEmail; }

    public boolean isAuthRequired()                 { return authRequired; }
    public void setAuthRequired(boolean authRequired){ this.authRequired = authRequired; }

    public boolean isStartTlsEnabled()              { return startTlsEnabled; }
    public void setStartTlsEnabled(boolean startTlsEnabled){ this.startTlsEnabled = startTlsEnabled; }

    public boolean isNotifyOnReinjection() { return notifyOnReinjection; }
    public void setNotifyOnReinjection(boolean notifyOnReinjection) { this.notifyOnReinjection = notifyOnReinjection; }

    public boolean isNotifyOnSlaDelay() { return notifyOnSlaDelay; }
    public void setNotifyOnSlaDelay(boolean notifyOnSlaDelay) { this.notifyOnSlaDelay = notifyOnSlaDelay; }
}
