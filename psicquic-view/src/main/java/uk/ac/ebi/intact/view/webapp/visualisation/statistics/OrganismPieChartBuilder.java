package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import com.google.common.collect.Maps;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Organism;

import java.util.Map;

/**
 * Builds a pie chart based on the species represented in the given MITAB data.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public class OrganismPieChartBuilder implements ChartBuilder {

    private Map<String, Integer> taxid2count = Maps.newHashMap();
    private Map<String, String> taxid2name = Maps.newHashMap();

    public void append(BinaryInteraction interaction) {
        final Organism organismA = interaction.getInteractorA().getOrganism();

        countTaxid(organismA);

        final Organism organismB = interaction.getInteractorA().getOrganism();
        if (organismA != null && !organismA.equals(organismB)) {
            countTaxid(organismB);
        }
    }

    public JFreeChart build() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Integer> entry : taxid2count.entrySet()) {
            final String taxid = entry.getKey();
            final Integer count = entry.getValue();
            String specieName = taxid2name.get( taxid );

            dataset.setValue( specieName, count );
        }

        dataset.sortByValues( SortOrder.DESCENDING );

        final boolean hasLegend = true;
        final boolean hasTooltips = true;
        final boolean hasUrls = true;

        JFreeChart chart = ChartFactory.createPieChart(
                "Species representation",
                dataset,
                hasLegend, hasTooltips, hasUrls
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator( new StandardPieSectionLabelGenerator() );

        return chart;
    }

    private void countTaxid(final Organism organism) {

        if (organism == null || organism.getTaxid() == null) {
            return;
        }

        // update count
        final String taxid = organism.getTaxid();
        Integer countTaxid = taxid2count.get(taxid);
        if (countTaxid == null) {
            taxid2count.put(taxid, 1);
        } else {
            countTaxid++;
            taxid2count.put(taxid, countTaxid);
        }

        // update name
        if (!taxid2name.containsKey(taxid)) {
            String name = null;
            for (CrossReference ref : organism.getIdentifiers()) {
                final String text = ref.getText();
                if (text != null && text.length() > 0) {
                    name = text;
                    break; // stop after first.
                }
            }

            if (name != null) {
                taxid2name.put(taxid, name);
            } else {
                taxid2name.put(taxid, "unknown");
//                TaxonomyService taxonomy = new OLSTaxonomyService();
                // TODO query uniprot taxonomy to find out and cache the information
            }
        }
    }
}
