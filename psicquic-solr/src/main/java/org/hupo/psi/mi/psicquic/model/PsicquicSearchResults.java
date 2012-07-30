package org.hupo.psi.mi.psicquic.model;

import org.apache.solr.common.SolrDocumentList;
import org.hupo.psi.calimocho.io.DocumentConverter;
import org.hupo.psi.calimocho.model.DocumentDefinition;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.hupo.psi.calimocho.xgmml.XGMMLDocumentDefinition;
import org.hupo.psi.mi.rdf.PsimiRdfConverter;
import org.hupo.psi.mi.rdf.RdfFormat;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.converter.tab2xml.XmlConversionException;
import psidev.psi.mi.tab.io.PsimiTabReader;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.*;

/**
 * Contains the results of a psicquic search. It can create XML, XGMML or any RDF/Biopax format from these results
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/07/12</pre>
 */

public class PsicquicSearchResults {

    private long numberResults;
    private SolrDocumentList results;
    private String [] fieldNames;

    private PsimiTabReader mitabReader;
    private PsimiRdfConverter rdfConverter;

    public PsicquicSearchResults(SolrDocumentList results, String [] fieldNames){
        this.results = results;
        this.fieldNames = fieldNames;
        mitabReader = new PsimiTabReader();
        rdfConverter = new PsimiRdfConverter();

        if (results != null){
            this.numberResults = results.getNumFound();
        }
        else {
            this.numberResults = 0;
        }
    }

    public InputStream getMitab() {
        if (this.fieldNames == null || this.fieldNames.length == 0){
            return null;
        }

        return new MitabInputStream(this.results, this.fieldNames);
    }

    public long getNumberResults() {
        return numberResults;
    }

    public EntrySet createEntrySet() throws ConverterException, IOException, XmlConversionException, IllegalAccessException {
        if (this.numberResults == 0 || this.results.size() == 0) {
            return new EntrySet();
        }

        Tab2Xml tab2Xml = new Tab2Xml();
        return tab2Xml.convert(mitabReader.read(getMitab()));
    }

    public InputStream createXGMML() throws IOException {
        InputStream mitabOs = getMitab();
        InputStream xgmml = null;

        try{
            DocumentDefinition mitab25Definition = MitabDocumentDefinitionFactory.mitab25();
            DocumentDefinition xgmmlDefinition = new XGMMLDocumentDefinition("PSICQUIC", "Generated from MITAB 2.5", "http://psicquic.googlecode.com");

            Reader mitabReader = new BufferedReader(new InputStreamReader(mitabOs));

            ByteArrayOutputStream xgmmlOutput = new ByteArrayOutputStream();
            Writer xgmmlWriter = new BufferedWriter(new OutputStreamWriter(xgmmlOutput));

            try{
                DocumentConverter converter = new DocumentConverter(mitab25Definition, xgmmlDefinition);
                converter.convert(mitabReader, xgmmlWriter);

                xgmml = new ByteArrayInputStream(xgmmlOutput.toByteArray());

                // close stringWriter now
                xgmmlWriter.close();

            }
            finally {
                // close mitab reader now
                mitabReader.close();
                // close stringWriter now
                xgmmlWriter.close();
                xgmmlOutput.close();
            }
        }
        finally {
            // close mitabOs now
            mitabOs.close();
        }

        return xgmml;
    }

    protected String getRdfFormatName(String format) {
        if (format.equalsIgnoreCase("biopax") || format.equalsIgnoreCase("biopax-L3")) {
            return RdfFormat.BIOPAX_L3.getName();
        } else if (format.equalsIgnoreCase("biopax-L2")) {
            return RdfFormat.BIOPAX_L2.getName();
        }

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

    public InputStream createRDFOrBiopax(String format) throws ConverterException, IOException, XmlConversionException, IllegalAccessException {

        if (format == null){
            return null;
        }

        String rdfFormat = getRdfFormatName(format);

        psidev.psi.mi.xml.model.EntrySet entrySet = createEntrySet();

        ByteArrayOutputStream rdfOutput = new ByteArrayOutputStream();
        Writer rdfWriter = new BufferedWriter(new OutputStreamWriter(rdfOutput));
        InputStream rdf = null;

        try{
            // only convert when having some results
            if (entrySet != null && !entrySet.getEntries().isEmpty()){
                rdfConverter.convert(entrySet, rdfFormat , rdfWriter);
                rdf = new ByteArrayInputStream(rdfOutput.toByteArray());
            }
        }
        finally {
            // close writer
            rdfWriter.close();
            rdfOutput.close();
        }

        return rdf;
    }
}
