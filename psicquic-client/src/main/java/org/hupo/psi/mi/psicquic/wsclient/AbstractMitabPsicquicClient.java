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

import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.hupo.psi.mi.psicquic.ResultInfo;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractMitabPsicquicClient<T extends BinaryInteraction> extends AbstractPsicquicClient<SearchResult<T>> {
    
    private static final String RETURN_TYPE = "psi-mi/tab25";

    public AbstractMitabPsicquicClient(String serviceAddress) {
        super(serviceAddress);
    }

    public SearchResult<T> getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException {
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByQuery(query, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public SearchResult<T> getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        DbRef dbRef = createDbRef(identifier);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractor(dbRef, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public SearchResult<T> getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        DbRef dbRef = createDbRef(identifier);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteraction(dbRef, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public SearchResult<T> getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException {
        List<DbRef> dbRefs = createDbRefs(identifiers);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractionList(dbRefs, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public SearchResult<T> getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException {
        List<DbRef> dbRefs = createDbRefs(identifiers);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractorList(dbRefs, requestInfo, operand.toString());
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    private SearchResult<T> createSearchResult(QueryResponse response) throws PsicquicClientException {
        String mitab = response.getResultSet().getMitab();

        PsimiTabReader reader = newPsimiTabReader();
        List<T> interactions = null;
        try {
            Collection<BinaryInteraction> interactionCollection = reader.read(mitab);
            interactions = new ArrayList<T>();

            for (BinaryInteraction binaryInteraction : interactionCollection) {
                interactions.add((T)binaryInteraction);
            }

        } catch (Exception e) {
            throw new PsicquicClientException("Problem converting the results to BinaryInteractions", e);
        }

        ResultInfo resultInfo = response.getResultInfo();

        return new SearchResult<T>(interactions, resultInfo.getTotalResults(),
                                                 resultInfo.getFirstResult(),
                                                 resultInfo.getBlockSize(), null);
    }

    public abstract PsimiTabReader newPsimiTabReader();
    
}
