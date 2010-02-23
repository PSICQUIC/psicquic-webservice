package org.hupo.psi.mi.psicquic.stats;

/**
 * Basic configuration holder.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class Config {

    public static final String DEFAULT_REGISTRY_URL = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/";
    public static final String DEFAULT_SMTP_CONFIG = "/META-INF/smtp.properties";

    private String psicquicRegistryUrl = DEFAULT_REGISTRY_URL;
    private String smtpConfigFile = DEFAULT_SMTP_CONFIG;

    public Config() {
    }

    public String getPsicquicRegistryUrl() {
        return psicquicRegistryUrl;
    }

    public void setPsicquicRegistryUrl( String psicquicRegistryUrl ) {
        this.psicquicRegistryUrl = psicquicRegistryUrl;
    }

    public String getSmtpConfigFile() {
        return smtpConfigFile;
    }

    public void setSmtpConfigFile( String smtpConfigFile ) {
        this.smtpConfigFile = smtpConfigFile;
    }

    public boolean isDefaultSmtpConfigFile() {
        return DEFAULT_SMTP_CONFIG.equals( smtpConfigFile );
    }
}
