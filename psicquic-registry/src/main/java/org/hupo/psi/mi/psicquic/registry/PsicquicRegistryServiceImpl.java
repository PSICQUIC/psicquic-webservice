/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package org.hupo.psi.mi.psicquic.registry;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryConfig;
import org.hupo.psi.mi.psicquic.registry.util.RegistryStreamingOutput;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.ArrayList;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.search.SearchResult;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class PsicquicRegistryServiceImpl implements PsicquicRegistryService{

    @Autowired
    private PsicquicRegistryConfig config;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    public Object executeAction(String action, String name, String url, String format) throws IllegalActionException {
        if ("STATUS".equalsIgnoreCase(action)) {

            Registry registry = new Registry();

            if (name != null) {
                for (ServiceType service : config.getRegisteredServices()) {
                    if (name.equalsIgnoreCase(service.getName())) {
                        registry.getServices().add(service);
                    }
                }
            } else {
                registry.getServices().addAll(config.getRegisteredServices());
            }

            for (ServiceType serviceStatus : registry.getServices()) {
                checkStatus(serviceStatus);
            }

            if ("xml".equals(format)) {
                return Response.ok(registry).build();
            }

            final Configuration freemarkerCfg = freeMarkerConfigurer.getConfiguration();

            return Response.ok(new RegistryStreamingOutput(registry, freemarkerCfg)).build();

        } else {
            throw new IllegalActionException("Action not defined: "+action);
        }
    }

    private void checkStatus(ServiceType serviceStatus) {
        final String version;

        try {
            final UniversalPsicquicClient client = new UniversalPsicquicClient(serviceStatus.getUrl());
            version = client.getService().getVersion();

            final SearchResult<BinaryInteraction> result = client.getByQuery("*:*", 0, 0);
            serviceStatus.setCount(result.getTotalCount());
        } catch (Throwable e) {
            serviceStatus.setActive(false);
            return;
        }

        serviceStatus.setActive(true);
        serviceStatus.setVersion(version);
    }

    public String getVersion() {
        return config.getVersion();
    }
}
