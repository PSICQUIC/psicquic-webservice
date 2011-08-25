/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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

import com.google.common.collect.Maps;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.*;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioPaxUriFixer {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String BIOPAX_NS = "http://www.biopax.org/release/biopax-level3.owl";

    public BioPaxUriFixer() {

    }

    public Map<String,String> findMappings(OntModel model) {
        Map<String,String> uriMappings = Maps.newHashMap();

        final Resource unificationXrefRes = model.getOntResource(BIOPAX_NS+"#UnificationXref");
        final Resource relationshipXrefRes = model.getOntResource(BIOPAX_NS+"#RelationshipXref");
        final Resource pubXrefRes = model.getOntResource(BIOPAX_NS+"#PublicationXref");

        uriMappings.putAll(findMappingsForResource(unificationXrefRes, model));
        uriMappings.putAll(findMappingsForResource(relationshipXrefRes, model));
        uriMappings.putAll(findMappingsForResource(pubXrefRes, model));

        return uriMappings;

    }

    private Map<String,String> findMappingsForResource(Resource unificationXrefRes, OntModel model) {
        Map<String,String> uriMappings = Maps.newHashMap();

        final ExtendedIterator<Individual> iterator = model.listIndividuals(unificationXrefRes);

        final Property idProp = model.getProperty(BIOPAX_NS + "#id");
        final Property dbProp = model.getProperty(BIOPAX_NS + "#db");


        while (iterator.hasNext()) {
            Individual xrefIndividual = iterator.next();
            String nodeUri = xrefIndividual.asNode().getURI();

                String id = xrefIndividual.getProperty(idProp).getLiteral().getString();
                String db = xrefIndividual.getProperty(dbProp).getLiteral().getString();

                if (db.equals("uniprotkb")) {
                    final String newUri = "http://purl.uniprot.org/uniprot/" + id;
                    uriMappings.put(nodeUri, newUri);
                } else {
                    if ("go".equals(db)) {
                        db = "obo.go";
                    } else if ("psi-mi".equals(db)) {
                        db = "obo.mi";
                    }

                    final String newUri = "http://identifiers.org/"+db+"/" + id;
                    uriMappings.put(nodeUri, newUri);
                }

        }

        return uriMappings;
    }

    public void fixBioPaxUris(Reader reader, Writer writer, Map<String,String> uriMappings) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String str;
        while ((str = in.readLine()) != null) {
            if (str.contains("bp:xref")) {
                for (Map.Entry<String, String> entry : uriMappings.entrySet()) {
                    final String oldUri = entry.getKey();
                    final String newUri = entry.getValue();

                    if (str.contains("HTTP://PATHWAYCOMMONS.ORG/")) {
                        str = str.replaceAll(oldUri, newUri);
                    } else {
                        String uriFromHash = oldUri.substring(oldUri.indexOf("#"));
                        str = str.replaceAll(uriFromHash, newUri);
                    }
                }
            } else if (str.contains("rdf:ID")) {
                for (Map.Entry<String, String> entry : uriMappings.entrySet()) {
                    final String oldUri = entry.getKey();
                    final String newUri = entry.getValue();

                    String uriFromHashWithout = oldUri.substring(oldUri.indexOf("#") + 1);
                    str = str.replaceAll(uriFromHashWithout, newUri);
                }
            } else if (str.contains("rdf:about")) {
               for (Map.Entry<String, String> entry : uriMappings.entrySet()) {
                    final String oldUri = entry.getKey();
                    final String newUri = entry.getValue();

                   str = str.replaceAll(oldUri, newUri);
                }
            }

            writer.write(str + NEW_LINE);
        }
    }

}
