/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.*;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml254.jaxb.Entry;
import psidev.psi.mi.xml254.jaxb.EntrySet;

import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class PsicquicServiceImpl implements PsicquicService {


    public QueryResponse getByInteractor(DbRefRequest dbRef) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        QueryResponse response = new QueryResponse();

        // TODO: this is all mockup logic. Here you would use the query and your data source to get whatever is needed
        // and for instance, generate a PSI-MI XML like below (of course, more data in it - it is empty now)

        ResultInfo info = new ResultInfo();
        info.setFirstResult(dbRef.getFirstResult());
        info.setBlockSize(dbRef.getBlockSize());
        info.setTotalResults(12345);

        response.setResultInfo(info);

        ResultSet rs = new ResultSet();

        EntrySet entrySet = new EntrySet();
        entrySet.setLevel(2);
        entrySet.setVersion(5);
        entrySet.setMinorVersion(4);

        Entry entry = new Entry();
        entrySet.getEntries().add(entry);

        rs.setEntrySet(entrySet);

        response.setResultSet(rs);

        return response;
    }

    public QueryResponse getByInteraction(DbRefRequest dbRef) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        QueryResponse response = new QueryResponse();

        // TODO: mockup logic for a PSI-MI TAB response

         ResultInfo info = new ResultInfo();
        info.setFirstResult(dbRef.getFirstResult());
        info.setBlockSize(dbRef.getBlockSize());
        info.setTotalResults(12345);

        response.setResultInfo(info);
        ResultSet rs = new ResultSet();
        //rs.setMitab(mitab);
        response.setResultSet(rs);

        return response;
    }

    public QueryResponse getByInteractorList(DbRefListRequest dbRefListRequest, String operand) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByInteractionList(DbRefList dbRefList) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        return null;
    }

    public QueryResponse getByQuery(QueryRequest query) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        return null;
    }

    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    public List<String> getSupportedReturnTypes()  {
        return null;
    }

    public List<String> getSupportedDbAcs() {
        return null;
    }
}
