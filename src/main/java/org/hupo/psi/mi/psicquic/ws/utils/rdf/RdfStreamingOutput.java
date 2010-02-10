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
package org.hupo.psi.mi.psicquic.ws.utils.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import psidev.psi.mi.xml.model.EntrySet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * TODO write description of the class.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RdfStreamingOutput implements StreamingOutput {

    private PsicquicService psicquicService;
    private EntrySet entrySet;
    private String rdfFormat;

    public RdfStreamingOutput(PsicquicService psicquicService, EntrySet entrySet, String rdfFormat) {
        this.psicquicService = psicquicService;
        this.entrySet = entrySet;
        this.rdfFormat = rdfFormat;
    }

    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        final RdfBuilder rdfBuilder = new RdfBuilder();
        final Model model = rdfBuilder.createModel(entrySet);

        model.write(outputStream, rdfFormat);
    }
}