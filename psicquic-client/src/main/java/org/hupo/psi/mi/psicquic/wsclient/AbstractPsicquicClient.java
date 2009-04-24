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
package org.hupo.psi.mi.psicquic.wsclient;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.RequestInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass for the PSICQUIC clients.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractPsicquicClient<T> implements PsicquicClient<T> {

    private PsicquicService service;

    public AbstractPsicquicClient(String serviceAddress) {
        if (serviceAddress == null) return;

        ClientProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PsicquicService.class);
        factory.setAddress(serviceAddress);

        this.service = (PsicquicService) factory.create();
    }

    public PsicquicService getService() {
        return service;
    }

    protected RequestInfo createRequestInfo(String returnType, int firstResult, int maxResults) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setFirstResult(firstResult);
        requestInfo.setBlockSize(maxResults);
        requestInfo.setResultType(returnType);
        return requestInfo;
    }

    protected DbRef createDbRef(String identifier) {
        DbRef dbRef = new DbRef();
        dbRef.setId(identifier);
        return dbRef;
    }

    protected List<DbRef> createDbRefs(String ... identifiers) {
        List<DbRef> dbRefs = new ArrayList<DbRef>(identifiers.length);

        for (String identifier : identifiers) {
            DbRef dbRef = createDbRef(identifier);
            dbRefs.add(dbRef);
        }

        return dbRefs;
    }
}
