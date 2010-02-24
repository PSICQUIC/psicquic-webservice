package org.hupo.psi.mi.psicquic.stats;

/**
 * Basic configuration holder.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class Config {

    public static final String DEFAULT_SMTP_CONFIG = "/META-INF/smtp.properties";

    private String psicquicRegistryUrl; // = DEFAULT_REGISTRY_URL;

    private String smtpConfigFile; // = DEFAULT_SMTP_CONFIG;

    private String miqlQuery;

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

    public boolean hasSmtpConfigFile() {
        return smtpConfigFile != null;
    }

    public String getMiqlQuery() {
        return miqlQuery;
    }

    public void setMiqlQuery( String miqlQuery ) {
        this.miqlQuery = miqlQuery;
    }
}
