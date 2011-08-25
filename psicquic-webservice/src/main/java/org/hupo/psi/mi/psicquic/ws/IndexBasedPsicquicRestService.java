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
package org.hupo.psi.mi.psicquic.ws;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.model.BioPAXLevel;
import org.hupo.psi.mi.psicquic.*;
import org.hupo.psi.mi.psicquic.ws.config.PsicquicConfig;
import org.hupo.psi.mi.psicquic.ws.utils.BioPaxUriFixer;
import org.hupo.psi.mi.psicquic.ws.utils.PsicquicStreamingOutput;
import org.mskcc.psibiopax.converter.PSIMIBioPAXConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlWriterException;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.converter.impl254.EntrySetConverter;
import psidev.psi.mi.xml.dao.inMemory.InMemoryDAOFactory;
import psidev.psi.mi.xml254.jaxb.EntrySet;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This web service is based on a PSIMITAB SOLR index to search and return the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicService.java 12873 2009-03-18 02:51:31Z baranda $
 */
@Controller
public class IndexBasedPsicquicRestService implements PsicquicRestService {

     @Autowired
    private PsicquicConfig config;

    @Autowired
    private PsicquicService psicquicService;

    public Object getByInteractor(String interactorAc, String db, String format, String firstResult, String maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "identifier:"+createQueryValue(interactorAc, db);
        return getByQuery(query, format, firstResult, maxResults);
    }

    public Object getByInteraction(String interactionAc, String db, String format, String firstResult, String maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "interaction_id:"+createQueryValue(interactionAc, db);
        return getByQuery(query, format, firstResult, maxResults);
    }

    public Object getByQuery(String query, String format,
                                                 String firstResultStr,
                                                 String maxResultsStr) throws PsicquicServiceException,
                                                                 NotSupportedMethodException,
                                                                 NotSupportedTypeException {
        // apply any filter
        if (config.getQueryFilter() != null && !config.getQueryFilter().isEmpty()) {
            if ("*".equals(query) || query.trim().isEmpty()) {
                query = config.getQueryFilter();
            } else {
                query = query + " "+config.getQueryFilter();
                query = query.trim();
            }
        }

        int firstResult;
        int maxResults;

        try {
            firstResult =Integer.parseInt(firstResultStr);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("firstResult parameter is not a number: "+firstResultStr);
        }

        try {
            if (maxResultsStr == null) {
                maxResults = Integer.MAX_VALUE;
            } else {
                maxResults = Integer.parseInt(maxResultsStr);
            }
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("maxResults parameter is not a number: "+maxResultsStr);
        }

        format = format.toLowerCase();

        try {
            if (strippedMime(IndexBasedPsicquicService.RETURN_TYPE_XML25).equalsIgnoreCase(format)) {
                final EntrySet entrySet = getByQueryXml(query, firstResult, maxResults);
                return Response.status(200).type(MediaType.APPLICATION_XML).entity(entrySet).build();
            } else if (format.toLowerCase().startsWith("rdf")) {
                String rdfFormat = getRdfFormatName(format);
                String mediaType = format.contains("xml")? MediaType.APPLICATION_XML : MediaType.TEXT_PLAIN;

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                convertToBioPAX(os, BioPAXLevel.L3, query, firstResult, maxResults);

                final String baseUri = "http://www.ebi.ac.uk/intact/";

                OntModel jenaModel = createJenaModel(new StringReader(os.toString()), baseUri);

                final RDFWriter rdfWriter = jenaModel.getWriter(rdfFormat);
                rdfWriter.setProperty("xmlbase", baseUri);
                jenaModel.setNsPrefix("", baseUri);

                Writer writer = new StringWriter();
                rdfWriter.write(jenaModel, writer, baseUri);

                // fix mappings
                Reader reader = new StringReader(writer.toString());
                Writer resultRdfWriter = new StringWriter();

                BioPaxUriFixer fixer = new BioPaxUriFixer();
                Map<String, String> uriMappings = fixer.findMappings(jenaModel, BioPAXLevel.L3);
                fixer.fixBioPaxUris(reader, resultRdfWriter, uriMappings);

                return Response.status(200).type(mediaType).entity(resultRdfWriter.toString()).build();

            } else if (format.startsWith(IndexBasedPsicquicService.RETURN_TYPE_BIOPAX)) {
                String[] formatElements = format.split("-");
                String biopaxLevel = "L3";

                if (formatElements.length == 2) {
                    biopaxLevel = formatElements[1].toUpperCase();
                    biopaxLevel = biopaxLevel.replaceAll("LEVEL", "L");
                }

                BioPAXLevel bioPAXLevel = BioPAXLevel.valueOf(biopaxLevel);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                convertToBioPAX(os, bioPAXLevel, query, firstResult, maxResults);

                final String biopaxOutput = os.toString();

                String output = "";

                if (!biopaxOutput.isEmpty()) {
                    final String baseUri = "http://www.ebi.ac.uk/intact/";

                    // fix the biopax non-dereferenciable URIs
                    OntModel jenaModel = createJenaModel(new StringReader(biopaxOutput), baseUri);

                    Reader reader = new StringReader(biopaxOutput);
                    Writer writer = new StringWriter();

                    BioPaxUriFixer fixer = new BioPaxUriFixer();
                    final Map<String, String> mappings = fixer.findMappings(jenaModel, bioPAXLevel);
                    fixer.fixBioPaxUris(reader, writer, mappings);

                    output = writer.toString();
                }

                return Response.status(200).type(MediaType.APPLICATION_XML_TYPE).entity(output).build();

            } else if (IndexBasedPsicquicService.RETURN_TYPE_COUNT.equalsIgnoreCase(format)) {
                return count(query);
            } else if (strippedMime(IndexBasedPsicquicService.RETURN_TYPE_MITAB25_BIN).equalsIgnoreCase(format)) {
                PsicquicStreamingOutput result = new PsicquicStreamingOutput(psicquicService, query, firstResult, maxResults, true);
                return Response.status(200).type("application/x-gzip").entity(result).build();
            } else if (strippedMime(IndexBasedPsicquicService.RETURN_TYPE_MITAB25).equalsIgnoreCase(format) || format == null) {
                PsicquicStreamingOutput result = new PsicquicStreamingOutput(psicquicService, query, firstResult, maxResults);
                return Response.status(200).type(MediaType.TEXT_PLAIN).entity(result).build();
            } else {
                return Response.status(406).type(MediaType.TEXT_PLAIN).entity("Format not supported").build();
            }
        } catch (Throwable e) {
            throw new PsicquicServiceException("Problem creating output", e);
        }


    }

    private OntModel createJenaModel(Reader reader, String baseUri) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        final RDFReader rdfReader = new JenaReader();
        rdfReader.read(model, reader, baseUri);
        return model;
    }

    protected void convertToBioPAX(OutputStream os, BioPAXLevel biopaxLevel, String query, int firstResult, int maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException, ConverterException, PsimiXmlWriterException, IOException, PsimiXmlReaderException {
        final EntrySet entrySet254 = getByQueryXml(query, firstResult, maxResults);

        if (entrySet254.getEntries().isEmpty()) {
            return;
        }

        EntrySetConverter converter254 = new EntrySetConverter();
        converter254.setDAOFactory(new InMemoryDAOFactory());

        final psidev.psi.mi.xml.model.EntrySet entrySet = converter254.fromJaxb(entrySet254);

        PSIMIBioPAXConverter biopaxConverter = new PSIMIBioPAXConverter(biopaxLevel);

        biopaxConverter.convert(entrySet, os);
    }

    private String getRdfFormatName(String format) {
        format = format.substring(4);

        String rdfFormat;

        if ("xml".equalsIgnoreCase(format)) {
            rdfFormat = "RDF/XML";
        } else if ("xml-abbrev".equalsIgnoreCase(format)) {
            rdfFormat = "RDF/XML-ABBREV";
        } else {
            rdfFormat = format.toUpperCase();
        }

        return rdfFormat;
    }

    private psidev.psi.mi.xml.model.EntrySet createEntrySet(String query, int firstResult, int maxResults) throws ConverterException, PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        EntrySetConverter converter = new EntrySetConverter();
        converter.setDAOFactory(new InMemoryDAOFactory());
        psidev.psi.mi.xml.model.EntrySet entrySet = converter.fromJaxb(getByQueryXml(query, firstResult, maxResults));
        return entrySet;
    }

    public Object getSupportedFormats() throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        List<String> formats = new ArrayList<String>(IndexBasedPsicquicService.SUPPORTED_RETURN_TYPES.size()+1);
        formats.add(strippedMime(IndexBasedPsicquicService.RETURN_TYPE_MITAB25_BIN));

        for (String mime : IndexBasedPsicquicService.SUPPORTED_RETURN_TYPES) {
            formats.add(strippedMime(mime));
        }

        return Response.status(200)
                .type(MediaType.TEXT_PLAIN)
                .entity(StringUtils.join(formats, "\n")).build();
    }

    public Object getProperty(String propertyName) {
        final String val = config.getProperties().get(propertyName);

        if (val == null) {
            return Response.status(404)
                .type(MediaType.TEXT_PLAIN)
                .entity("Property not found: "+propertyName).build();
        }

         return Response.status(200)
                .type(MediaType.TEXT_PLAIN)
                .entity(val).build();
    }

    public Object getProperties() {
        StringBuilder sb = new StringBuilder(256);

        for (Map.Entry entry : config.getProperties().entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        return Response.status(200)
                .type(MediaType.TEXT_PLAIN)
                .entity(sb.toString()).build();
    }

    public String getVersion() {
        return config.getVersion();
    }

    public EntrySet getByQueryXml(String query,
                                  int firstResult,
                                  int maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("psi-mi/xml25");

        try {
            reqInfo.setFirstResult(firstResult);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("firstResult parameter is not a number: "+firstResult);
        }

        try {
            reqInfo.setBlockSize(maxResults);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("maxResults parameter is not a number: "+maxResults);
        }

        QueryResponse response = psicquicService.getByQuery(query, reqInfo);

        return response.getResultSet().getEntrySet();
    }

    private int count(String query) throws NotSupportedTypeException, NotSupportedMethodException, PsicquicServiceException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("count");
        QueryResponse response = psicquicService.getByQuery(query, reqInfo);
        return response.getResultInfo().getTotalResults();
    }

    private String createQueryValue(String interactorAc, String db) {
        StringBuilder sb = new StringBuilder(256);
        if (db.length() > 0) sb.append('"').append(db).append(':');
        sb.append(interactorAc);
        if (db.length() > 0) sb.append('"');

        return sb.toString();
    }

    private String strippedMime(String mimeType) {
        if (mimeType.indexOf("/") > -1) {
            return mimeType.substring(mimeType.indexOf("/")+1);
        } else {
            return mimeType;
        }
    }
}
