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

import org.junit.Test;
import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.QueryResponse;

/**
 * Example on how to use the PsicquicClient.
 */
public class PsicquicClientExample {

    public static void main(String[] args) throws Exception {

        PsicquicClient client = new PsicquicClient("http://localhost:8080/psicquic-ws/webservices/psicquic");

        PsicquicService service = client.getService();

        // preparing a query: searching for brca2 and returning MITAB
        RequestInfo info = new RequestInfo();
        info.setResultType("psi-mi/tab25");
        info.setBlockSize(50);

        DbRef dbRef = new DbRef();
        dbRef.setId("brca2");

        final QueryResponse queryResponse = service.getByInteractor(dbRef, info);

        // printing the results
        String mitab = queryResponse.getResultSet().getMitab();

        System.out.println(mitab);

    }
}
