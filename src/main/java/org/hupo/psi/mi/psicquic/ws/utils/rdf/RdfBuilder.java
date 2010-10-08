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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import psidev.psi.mi.xml.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RdfBuilder {
    private static final String BIOPAX_URI = "http://www.biopax.org/release/biopax-level3.owl#";
    private static final String INTACT_URI = "http://purl.uniprot.org/intact/";
    private static final String PSIMI_URI = "http://www.ebi.ac.uk/~intact/psimi.owl#";
    private static final String OWL_MI_URI = "http://purl.org/obo/owl/MI#";

    private int xrefCounter = 0;

    private boolean develMode;

    public RdfBuilder() {
        this(false);
    }

    public RdfBuilder(boolean develMode) {
        this.develMode = develMode;
    }

    public Model createModel(EntrySet entrySet, String baseURI) {

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("bp", BIOPAX_URI);
        model.setNsPrefix("psimi", PSIMI_URI);
        model.setNsPrefix("owlmi", OWL_MI_URI);

        Ontology ontology = model.createOntology(baseURI);
        ontology.addImport(model.createResource("http://www.biopax.org/release/biopax-level3.owl"));

//        if (develMode) {
//            Ontology ontologyPsi = model.createOntology(baseURI);
//            ontologyPsi.addImport(model.createResource("http://www.ebi.ac.uk/~intact/psimi.owl"));
//        }

        final Property biopaxNameProp = model.createProperty(BIOPAX_URI + "name");
        final Property commentProp = model.createProperty(BIOPAX_URI + "comment");
        final Property xrefProp = model.createProperty(BIOPAX_URI + "xref");
        final Property idProp = model.createProperty(BIOPAX_URI + "id");
        final Property dbProp = model.createProperty(BIOPAX_URI + "db");
//        final Property dbAcProp = model.createProperty(PSIMI_URI + "dbAc");
//        final Property refTypeAcProp = model.createProperty(PSIMI_URI + "refTypeAc");
//        final Property refTypeProp = model.createProperty(PSIMI_URI + "refType");

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
                        interactorUri = "http://UNKNOWN.org/#_UNKNOWN_";
                        rdfType = BIOPAX_URI+"PhysicalEntity";
                    }

                    String proteinUri = interactorUri + processId(mainDbRef.getId());
                    Resource interactorRes = model.createResource(proteinUri);
                    interactorRes.addProperty(RDF.type, model.createResource(rdfType));
                    interactorRes.addLiteral(RDFS.label, interactor.getNames().getShortLabel());

                    // participant xref

                    if (develMode) {  // not working quite right
                        Collection<DbReference> allDbRefs = new ArrayList<DbReference>(interactor.getXref().getSecondaryRef());
                        allDbRefs.add(interactor.getXref().getPrimaryRef());

                        Resource unificationXref = model.createResource(BIOPAX_URI+"UnificationXref");
                        Resource relationshipXref = model.createResource(BIOPAX_URI+"RelationshipXref");

                        for (DbReference dbReference : allDbRefs) {
                            String refTypeAc = dbReference.getRefTypeAc();

                            Resource xrefRes;

                            if (refTypeAc != null && refTypeAc.equals("MI:0356")) { // identity
                                xrefRes = unificationXref;
                            } else {
                                xrefRes = relationshipXref;
                            }

                            String id = dbReference.getId();

                            Individual xref = model.createIndividual( baseURI+"#xref-"+ xrefCounter++ +"-"+ id.replaceAll(":", "_"), xrefRes);
    //
                            xref.addLiteral(idProp, id);
                            xref.addLiteral(dbProp, dbReference.getDb());
//                            xref.addLiteral(dbAcProp, dbReference.getDbAc());
//                            xref.addLiteral(refTypeAcProp, dbReference.getRefTypeAc());
//                            xref.addLiteral(refTypeProp, dbReference.getRefType());

                            interactorRes.addProperty(xrefProp, xref);
                        }
                    }

                    interactionRes.addProperty(participantProp, interactorRes);
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

