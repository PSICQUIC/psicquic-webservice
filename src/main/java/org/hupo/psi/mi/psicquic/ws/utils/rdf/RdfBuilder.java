/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import psidev.psi.mi.xml.model.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RdfBuilder {
    private static final String BIOPAX_URI = "http://www.biopax.org/release/biopax-level3.owl#";
    private static final String INTACT_URI = "http://purl.uniprot.org/intact/";
    private static final String OWL_MI_URI = "http://purl.org/obo/owl/MI#";

    public Model createModel(EntrySet entrySet) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("bp", BIOPAX_URI);
        model.setNsPrefix("owlmi", OWL_MI_URI);

        model.add(OWL.Ontology, OWL.imports, "http://www.biopax.org/release/biopax-level3.owl");

        final Property biopaxNameProp = model.createProperty(BIOPAX_URI + "name");
        final Property commentProp = model.createProperty(BIOPAX_URI + "comment");

        for (Entry entry : entrySet.getEntries()) {

            for (Interaction interaction : entry.getInteractions()) {

                final String primaryId = interaction.getXref().getPrimaryRef().getId();

                Resource interactionRes = model.createResource(INTACT_URI + primaryId);
                interactionRes.addProperty(RDF.type, model.createResource(BIOPAX_URI+"MolecularInteraction"));
                interactionRes.addLiteral(RDFS.label, interaction.getNames().getShortLabel());
                interactionRes.addLiteral(biopaxNameProp, interaction.getNames().getShortLabel());

                for (InteractionType interactionType : interaction.getInteractionTypes()) {
                    String interactionTypeAc = interactionType.getXref().getPrimaryRef().getId();

                    Resource interactionTypeRes = model.createResource(OWL_MI_URI+processId(interactionTypeAc));
                    interactionTypeRes.addProperty(RDF.type, model.createResource(BIOPAX_URI+"InteractionVocabulary"));
                    interactionTypeRes.addProperty(RDFS.label, interactionType.getNames().getShortLabel());
                    interactionTypeRes.addProperty(biopaxNameProp, interactionType.getNames().getShortLabel());

                    if (interactionType.getNames().getFullName() != null) {
                        interactionTypeRes.addProperty(commentProp, interactionType.getNames().getFullName());
                    }

                    interactionRes.addProperty(model.createProperty(BIOPAX_URI+"interactorType"), interactionTypeRes);
                }

                Property participantProp = model.createProperty(BIOPAX_URI, "participant");

                for (Participant participant : interaction.getParticipants()) {
                    final Interactor interactor = participant.getInteractor();
                    Xref interactorXref = interactor.getXref();

                    DbReference mainDbRef = getMainDbRef(interactorXref, "MI:0486", "MI:0474", "MI:0469");
                    String dbAc = mainDbRef.getDbAc();
                    String interactorUri;
                    String rdfType;

                    if ("MI:0486".equals(dbAc)) {
                        interactorUri = "http://purl.uniprot.org/uniprot/";
                        rdfType = BIOPAX_URI+"Protein";
                    } else if ("MI:0474".equals(dbAc)) {
                        interactorUri = "http://purl.org/obo/owl/CHEBI#";
                        rdfType = BIOPAX_URI+"SmallMolecule";
                    } else if ("MI:0469".equals(dbAc)) {
                        interactorUri = INTACT_URI;
                        rdfType = BIOPAX_URI+"PhysicalEntity";
                    } else {
                        interactorUri = "#_UNKNOWN_";
                        rdfType = BIOPAX_URI+"PhysicalEntity";
                    }
                    
                    Resource proteinRes = model.createResource(interactorUri + processId(mainDbRef.getId()));
                    proteinRes.addProperty(RDF.type, model.createResource(rdfType));
                    proteinRes.addLiteral(RDFS.label, interactor.getNames().getShortLabel());                                

                    interactionRes.addProperty(participantProp, proteinRes);
                }
            }
        }

        return model;
    }

    private String processId(String interactionTypeAc) {
        return interactionTypeAc.replaceAll(":", "_");
    }

    private DbReference getMainDbRef(Xref xref, String ... priorityDbAcs) {
        DbReference mainDbref = null;

        for (String dbAc : priorityDbAcs) {
            if (dbAc.equals(xref.getPrimaryRef().getDbAc())) {
                mainDbref = xref.getPrimaryRef();
            } else {
                for (DbReference ref : xref.getSecondaryRef()) {
                    if (dbAc.equals(ref.getDbAc())) {
                        mainDbref = ref;
                    }
                }
            }
        }

        if (mainDbref == null) {
            mainDbref = xref.getPrimaryRef();
        }

        return mainDbref;
    }
}
