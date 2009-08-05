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
package org.hupo.psi.mi.psicquic.ws.utils;

import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * TODO write description of the class.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicStreamingOutput implements StreamingOutput {

    private PsicquicService psicquicService;
    private String query;
    private QueryResponse response;
    private int firstResult;
    private int maxResults;

    public PsicquicStreamingOutput(PsicquicService psicquicService, String query, int firstResult, int maxResults) {
        this.psicquicService = psicquicService;
        this.query = query;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("psi-mi/tab25");

        PrintWriter out = new PrintWriter(outputStream);

        int max = firstResult+maxResults;

        reqInfo.setBlockSize(maxResults);

        do {
            reqInfo.setFirstResult(firstResult);

            try {
                response = psicquicService.getByQuery(query, reqInfo);

                out.write(response.getResultSet().getMitab());
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }

            out.flush();

            firstResult = firstResult + response.getResultInfo().getBlockSize();

        } while (firstResult < max);

        out.close();
    }

    public QueryResponse getQueryResponse() {
        return response;
    }
}
