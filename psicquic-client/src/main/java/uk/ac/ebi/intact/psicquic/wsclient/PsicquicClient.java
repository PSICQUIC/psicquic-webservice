/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.PsicquicService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.frontend.ClientProxyFactoryBean;

/**
 * Simple client for PSICQUIC web services.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicClient {

    private PsicquicService service;

    public PsicquicClient(String serviceAddress) {
        ClientProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PsicquicService.class);
        factory.setAddress(serviceAddress);

        this.service = (PsicquicService) factory.create();
    }

    public PsicquicService getService() {
        return service;
    }
}
