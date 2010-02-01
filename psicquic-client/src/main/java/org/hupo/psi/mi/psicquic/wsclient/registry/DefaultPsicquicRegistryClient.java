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
package org.hupo.psi.mi.psicquic.wsclient.registry;

import org.hupo.psi.mi.psicquic.registry.ServiceType;

import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DefaultPsicquicRegistryClient implements PsicquicRegistryClient {

    private static final String REGISTRY_URL_DEFAULT = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/";

    private String registryBaseUrl;

    public DefaultPsicquicRegistryClient() {
        registryBaseUrl = REGISTRY_URL_DEFAULT;
    }

    public DefaultPsicquicRegistryClient(String registryBaseUrl) {
        this.registryBaseUrl = registryBaseUrl;
    }

    public ServiceType getService(String serviceName) {
        List<ServiceType> serv
    }

    public List<ServiceType> listActiveServices() {
        return listServices("ACTIVE");
    }

    public List<ServiceType> listAllServices() {
        return listServices("STATUS");
    }

    public List<ServiceType> listInactiveServices() {
        return listServices("INACTIVE");
    }

    public List<ServiceType> listServices(String action, boolean restricted) {
        String url = createServiceUrl(registryBaseUrl, "registry?action="+action.toUpperCase()+"&restricted="+(restricted? "y" : "n"));

        JAXBCo
    }

    public List<ServiceType> listServicesWithTags(String[] tags, boolean onlyActive) {
        return null;
    }

    private String createServiceUrl(String base, String paramUrl) {
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base+paramUrl;
    }
}
