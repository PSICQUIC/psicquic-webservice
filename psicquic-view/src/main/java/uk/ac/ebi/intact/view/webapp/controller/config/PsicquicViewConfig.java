/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;

import javax.faces.event.ActionEvent;
import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("config")
public class PsicquicViewConfig extends BaseController implements InitializingBean {

    private static final Log log = LogFactory.getLog( PsicquicViewConfig.class );

    private String configFile;
    private String title;
    private String logoUrl;
    private String bannerBackgroundUrl;
    private String registryTagsAsString;
    private String miqlFilterQuery;
    private String includedServices;
    private String excludedServices;

    public PsicquicViewConfig() {
    }

    public void afterPropertiesSet() throws Exception {
        if (configFile == null || configFile.length() == 0) {
            final String tempFile = File.createTempFile("psicquic-view", ".config.properties").toString();
            log.warn("No config file defined. Created new temporary configuration file: "+tempFile);
            configFile = tempFile;
        }

        configFile = configFile.replaceAll("\\\\:", ":");

        File file = new File(configFile);

        if (!file.exists()) {
            saveConfigToFile();
        }

        loadConfigFromFile(null);
    }

    public void loadConfigFromFile(ActionEvent evt) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));

        title = properties.getProperty("title");
        logoUrl = properties.getProperty("logo.url");
        bannerBackgroundUrl = properties.getProperty("banner.background.url");
        registryTagsAsString = properties.getProperty("registry.tags");
        miqlFilterQuery = properties.getProperty("query.filter");
        includedServices = properties.getProperty("services.included");
        excludedServices = properties.getProperty("services.excluded");
    }

    public void saveConfigToFile() throws IOException {
        Properties properties = new Properties();

        if (new File(configFile).exists()) {
            properties.load(new FileInputStream(configFile));
        }

        setProperty(properties, "title", title);
        setProperty(properties, "logo.url", logoUrl);
        setProperty(properties, "banner.background.url", bannerBackgroundUrl);
        setProperty(properties, "registry.tags", registryTagsAsString);
        setProperty(properties, "query.filter", miqlFilterQuery);
        setProperty(properties, "services.included", includedServices);
        setProperty(properties, "services.excluded", excludedServices);

        OutputStream outputStream = new FileOutputStream(configFile);
        properties.store(outputStream, "Configuration updated: "+new Date());
        outputStream.close();
    }

    private void setProperty(Properties props, String key, String value) {
        if (value != null) {
            props.put(key, value);
        }
    }

    public void store(ActionEvent evt) {
        try {
            saveConfigToFile();
            super.addInfoMessage("File saved successfully", configFile);
        } catch (IOException e) {
            e.printStackTrace();
            super.addErrorMessage("Problem saving configuration", e.getMessage());
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getRegistryTagsAsString() {
        return registryTagsAsString;
    }

    public void setRegistryTagsAsString(String registryTagsAsString) {
        this.registryTagsAsString = registryTagsAsString;
    }

    public String getMiqlFilterQuery() {
        return miqlFilterQuery;
    }

    public void setMiqlFilterQuery(String miqlFilterQuery) {
        this.miqlFilterQuery = miqlFilterQuery;
    }

    public String getIncludedServices() {
        return includedServices;
    }

    public void setIncludedServices(String includedServices) {
        this.includedServices = includedServices;
    }

    public String getExcludedServices() {
        return excludedServices;
    }

    public void setExcludedServices(String excludedServices) {
        this.excludedServices = excludedServices;
    }

    public String getBannerBackgroundUrl() {
        return bannerBackgroundUrl;
    }

    public void setBannerBackgroundUrl(String bannerBackgroundUrl) {
        this.bannerBackgroundUrl = bannerBackgroundUrl;
    }
}
