package org.hupo.psi.mi.psicquic.model;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hupo.psi.calimocho.io.DocumentConverter;
import org.hupo.psi.calimocho.model.DocumentDefinition;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.hupo.psi.calimocho.xgmml.XGMMLDocumentDefinition;
import org.hupo.psi.mi.rdf.PsimiRdfConverter;
import org.hupo.psi.mi.rdf.RdfFormat;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.converter.tab2xml.XmlConversionException;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * Contains the results of a psicquic search. It can create XML, XGMML or any RDF/Biopax format from these results
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/07/12</pre>
 */

public class PsicquicSearchResults {

    private String mitab;
    private long numberResults;
    private SolrDocumentList results;
    private String [] fieldNames;

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String COLUMN_SEPARATOR = "\t";
    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_EMPTY = "-";

    private PsimiTabReader mitabReader;
    private PsimiRdfConverter rdfConverter;

    public PsicquicSearchResults(SolrDocumentList results, String [] fieldNames){
        this.results = results;
        this.fieldNames = fieldNames;
        mitabReader = new PsimiTabReader(false);
        rdfConverter = new PsimiRdfConverter();

        if (results != null){
            this.numberResults = results.getNumFound();
            this.mitab = extractMitabFromSolrResults();
        }
        else {
            this.numberResults = 0;
            this.mitab = null;
        }
    }

    public String getMitab() {
        return mitab;
    }

    public long getNumberResults() {
        return numberResults;
    }

    private String extractMitabFromSolrResults(){

        if (results != null){

            if (fieldNames == null || fieldNames.length == 0){
                return null;
            }

            StringBuilder sb = new StringBuilder(1064);

            Iterator<SolrDocument> iter = results.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();

                int size = fieldNames.length;
                int index = 0;
                // one field name is one column
                for (String fieldName : fieldNames){
                    // only one value is expected because it should not be multivalued
                    Collection<Object> fieldValues = doc.getFieldValues(fieldName);

                    if (fieldValues == null || fieldValues.isEmpty()){
                        sb.append(FIELD_EMPTY);
                    }
                    else {
                        Iterator<Object> valueIterator = fieldValues.iterator();
                        while (valueIterator.hasNext()){
                            sb.append(String.valueOf(valueIterator.next()));

                            if (valueIterator.hasNext()){
                                sb.append(FIELD_SEPARATOR);
                            }
                        }
                    }

                    if (index < size){
                        sb.append(COLUMN_SEPARATOR);
                    }
                    index++;
                }

                if (iter.hasNext()){
                    sb.append(NEW_LINE);
                }
            }
            return sb.toString();
        }
        return null;
    }

    public EntrySet createEntrySet() throws ConverterException, IOException, XmlConversionException, IllegalAccessException {
        if (mitab == null) {
            return new EntrySet();
        }

        Tab2Xml tab2Xml = new Tab2Xml();
        return tab2Xml.convert(mitabReader.read(mitab));
    }

    public String createXGMML() throws IOException {
        ByteArrayOutputStream mitabOs = new ByteArrayOutputStream();
        String xgmml = null;

        try{
            DocumentDefinition mitab25Definition = MitabDocumentDefinitionFactory.mitab25();
            DocumentDefinition xgmmlDefinition = new XGMMLDocumentDefinition("PSICQUIC", "Generated from MITAB 2.5", "http://psicquic.googlecode.com");

            Reader mitabReader = new StringReader(mitabOs.toString());
            Writer xgmmlWriter = new StringWriter();

            try{
                DocumentConverter converter = new DocumentConverter(mitab25Definition, xgmmlDefinition);
                converter.convert(mitabReader, xgmmlWriter);

                xgmml = xgmmlWriter.toString();

                // close stringWriter now
                xgmmlWriter.close();

            }
            finally {
                // close mitab reader now
                mitabReader.close();
                // close stringWriter now
                xgmmlWriter.close();
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

    public String createRDFOrBiopax(String format) throws ConverterException, IOException, XmlConversionException, IllegalAccessException {

        if (format == null){
            return null;
        }

        String rdfFormat = getRdfFormatName(format);

        psidev.psi.mi.xml.model.EntrySet entrySet = createEntrySet();
        StringWriter sw = new StringWriter();

        try{
            // only convert when having some results
            if (entrySet != null && !entrySet.getEntries().isEmpty()){
                rdfConverter.convert(entrySet, rdfFormat , sw);
            }
        }
        finally {
            // close writer
            sw.close();
        }

        return sw.toString();
    }
}
