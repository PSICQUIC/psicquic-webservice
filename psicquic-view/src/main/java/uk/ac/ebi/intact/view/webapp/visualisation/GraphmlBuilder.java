package uk.ac.ebi.intact.view.webapp.visualisation;

import com.google.common.collect.Maps;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts MITAB stream into graphml.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.1
 *        <p/>
 *        see http://graphml.graphdrawing.org/primer/graphml-primer.html
 *        <p/>
 *        http://graphml.graphdrawing.org/primer/graphml-primer.html#Graph
 *        "In GraphML there is no order defined for the appearance of node and edge elements."
 */
public class GraphmlBuilder {

    private static final Log log = LogFactory.getLog(GraphmlBuilder.class);

    public static final String NEW_LINE = "\\\n";

    public static final String GRAPHML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NEW_LINE +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  " + NEW_LINE +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NEW_LINE +
            "    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns" + NEW_LINE +
            "     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NEW_LINE +
            "  <key id=\"label\" for=\"all\" attr.name=\"label\" attr.type=\"string\"/>" + NEW_LINE +
            "  <graph id=\"G\" edgedefault=\"undirected\">" + NEW_LINE;

    public static final String GRAPHML_FOOTER = "  </graph>" + NEW_LINE +
            "</graphml>";

    private SearchController searchController;

    private int nextNodeId = 1;

    /**
     * Maps the identifier of a molecule to the node.id in GraphML.
     * TODO Should we want to convert large volumes of data, we could use ehcache instead of a HashMap.
     */
    private Map<String, Integer> molecule2node = Maps.newHashMap();

    public GraphmlBuilder(SearchController searchController) {
        this.searchController = searchController;
    }

    protected Iterator<BinaryInteraction> getMitabIterator( InputStream is ) throws ConverterException, IOException {
        PsimiTabReader reader = new PsimiTabReader( false );
        return reader.iterate( is );
    }

    public String build( InputStream is ) throws IOException, ConverterException {

        // TODO use node shapes to differenciate proteins from small molecules
        // TODO Create a download servlet for the clustered datasets so that we can have a graph for them too

        StringBuilder sb = new StringBuilder( 4096 );
        
        // get MITAB data from the current service
        final Iterator<BinaryInteraction> iterator;
        int interactionCount = 0;
        try {
            iterator = getMitabIterator( is );

            // create header of GraphML
            sb.append(GRAPHML_HEADER);

            // convert each lines into nodes and edges
            while (iterator.hasNext()) {
                BinaryInteraction interaction = iterator.next();

                final Node nodeA = buildNode(interaction.getInteractorA());
                if (nodeA.hasXml()) {
                    sb.append(nodeA.getXml());
                }

                final Node nodeB = buildNode(interaction.getInteractorB());
                if (nodeB.hasXml()) {
                    sb.append(nodeB.getXml());
                }

                sb.append(buildEdge(nodeA.getId(), nodeB.getId()));

                interactionCount++;
            }

            log.info("Processed " + interactionCount + " binary interactions.");

            // create footer of GraphML
            sb.append(GRAPHML_FOOTER);

        } catch (ConverterException e) {

            sb.append( "Failed to parse MITAB data" );
            sb.append(NEW_LINE);
            sb.append( ExceptionUtils.getFullStackTrace(e) );

        } finally {

            molecule2node.clear();

        }

        return sb.toString();
    }

    private String buildEdge(int ida, int idb) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("     <edge source=\"" + ida + "\" target=\"" + idb + "\" />").append(NEW_LINE);
        return sb.toString();
    }

    private class Node {
        int id;
        String xml;

        private Node(int id, String xml) {
            this.id = id;
            this.xml = xml;
        }

        public int getId() {
            return id;
        }

        public String getXml() {
            return xml;
        }

        public boolean hasXml() {
            return xml != null;
        }
    }

    /**
     * This method converts a MITAB interactor into a node. Case must be taken not to repeat the same node if it has
     * already been processed.
     *
     * @param interactor
     * @return
     */
    private Node buildNode(Interactor interactor) {

        final String id = pickIdentifier(interactor);
        if (molecule2node.containsKey(id)) {
            return new Node(molecule2node.get(id), null); // the node was already exported, return its id instead
        }

        StringBuilder sb = new StringBuilder(128);
        String label = pickLabel(interactor);
        final int nodeId = getNextNodeId();
        sb.append("     <node id=\"").append(nodeId).append("\">").append(NEW_LINE)
          .append("        <data key=\"label\">").append(label).append("</data>").append(NEW_LINE)
          .append("     </node>").append(NEW_LINE);

        molecule2node.put(id, nodeId);
        return new Node(nodeId, sb.toString());
    }

    private int getNextNodeId() {
        return nextNodeId++;
    }

    /**
     * Quick and dirty: pick the first identifier available.
     * Later: will look through an ordered list of databases.
     *
     * @param interactor
     * @return
     */
    protected String pickIdentifier(Interactor interactor) {

        String identifier = null;

        // BIND has multiple uniprotkb identifiers ?

//        List<String> databases = Lists.newArrayList();
//        databases.add( "uniprotkb" );
//        databases.add( "chebi" );
//        databases.add( "intact" );
//        databases.add( "chembl" );
//        databases.add( "genbank_protein_gi" );
//        databases.add( "entrez gene/locuslink" );

        if (!interactor.getIdentifiers().isEmpty()) {
            identifier = interactor.getIdentifiers().iterator().next().getIdentifier();
        }

        if (identifier == null) {
            throw new IllegalStateException("Can't find an Identifier for interactor " + interactor);
        }

        return identifier;
    }

    /**
     * Quick and Dirty: pick the first alias or if not present, the first identifier.
     *
     * @param interactor
     * @return
     */
    protected String pickLabel(Interactor interactor) {

        String label = null;

        if (!interactor.getAliases().isEmpty()) {
            Alias alias = interactor.getAliases().iterator().next();
            label = alias.getName();
        } else if (!interactor.getIdentifiers().isEmpty()) {
            final CrossReference cr = interactor.getIdentifiers().iterator().next();
            label = cr.getIdentifier();
        }

        if (label == null) {
            throw new IllegalStateException("Can't find a label for interactor " + interactor);
        }

        return label;
    }
}