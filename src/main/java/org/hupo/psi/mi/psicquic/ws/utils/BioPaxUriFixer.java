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
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.utils.Normalizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioPaxUriFixer {

    private static final String NEW_LINE = System.getProperty("line.separator");

    public BioPaxUriFixer() {

    }

    public String fixBioPaxUris(Model model) throws IOException {
        model.getNameSpacePrefixMap().clear();

        Map<String,String> mappings = Maps.newHashMap();
        mappings.put("psi-mi", "mi");
        mappings.put("rcsb pdb", "pdb");

        fixXrefs(model, mappings);

        Map<String, String> idMappings = calculateIdMapings(model);

        new Normalizer().normalize(model);

        OutputStream baos = new ByteArrayOutputStream();

        new SimpleIOHandler().convertToOWL(model, baos);

        return fixURIs(idMappings, baos.toString());
    }

    private String fixURIs(Map<String, String> idMappings, String output) {
        String[] lines = output.split("\n");

        List<String> modifiedLines = new ArrayList<String>();

        for (String line : lines) {
            if (line.contains("Reference")) {
               for (Map.Entry<String,String> protMapEntry : idMappings.entrySet()) {
                   if (line.contains(protMapEntry.getKey())) {
                       line = line.replaceAll(protMapEntry.getKey(), protMapEntry.getValue());
                   }
               }
            }

            if (line.contains("urn:miriam:")) {
                String[] tokens = line.split("urn:miriam:");
                tokens[1] = StringUtils.replaceOnce(tokens[1], ":", "/");

                line = StringUtils.join(tokens, "http://identifiers.org/");
            }

            modifiedLines.add( line );
        }

        return StringUtils.join(modifiedLines, "\n");
    }

    private Map<String, String> calculateIdMapings(Model model) {
        Map<String,String> idMappings = Maps.newHashMap();

        final Set<SimplePhysicalEntity> proteins = model.getObjects(SimplePhysicalEntity.class);

        for (SimplePhysicalEntity protein : proteins) {
            final String protRefId = protein.getEntityReference().getRDFId();

            for (Xref xref : protein.getXref()) {
                if ("uniprotkb".equals(xref.getDb())) {
                    String newId = "http://identifiers.org/uniprot/"+xref.getId();
                    idMappings.put(protRefId, newId);
                    break;
                } else if ("chebi".equals(xref.getDb())) {
                    String newId = "http://identifiers.org/chebi/"+xref.getId();
                    idMappings.put(protRefId, newId);
                    break;
                }
            }
        }
        return idMappings;
    }

    private void fixXrefs(Model model, Map<String, String> mappings) {
        final Set<Xref> xrefs = model.getObjects(Xref.class);

        for (Xref xref : xrefs) {
            if (mappings.containsKey(xref.getDb())) {
                xref.setDb(mappings.get(xref.getDb()));
            }
        }
    }

}
