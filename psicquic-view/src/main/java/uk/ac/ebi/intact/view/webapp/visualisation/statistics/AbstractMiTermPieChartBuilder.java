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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A specific ChartBuilder that is based on ontology terms (CrossReference).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public abstract class AbstractMiTermPieChartBuilder implements ChartBuilder {

    public static final int DEFAULT_COLLAPSING_THRESHOLD = 95;

    /**
     * Chart title.
     */
    private String title;

    /**
     * Percentage of the counts above which we collapse categories into 'Other'.
     */
    private int collapsingThreshold = DEFAULT_COLLAPSING_THRESHOLD;

    private Map<String, Integer> mi2count = Maps.newHashMap();
    private Map<String, String> mi2name = Maps.newHashMap();

    public void append(BinaryInteraction interaction) {
        final Collection<CrossReference> terms = getTerms( interaction );
        for (CrossReference term : terms) {
            countTerm(term);
        }
    }

    public JFreeChart build() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Integer> entry : mi2count.entrySet()) {
            final String taxid = entry.getKey();
            final Integer count = entry.getValue();
            String termName = mi2name.get( taxid );

            dataset.setValue( termName, count );
        }

        dataset.sortByValues( SortOrder.DESCENDING );

        // Attempt to collapse sections above the set collapsingThreshold.
        collapseChartSections(dataset);

        final boolean hasLegend = true;
        final boolean hasTooltips = true;
        final boolean hasUrls = true;

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                hasLegend, hasTooltips, hasUrls
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator( new StandardPieSectionLabelGenerator() );

        return chart;
    }

    private void collapseChartSections(DefaultPieDataset dataset) {

        // TODO FIXME if only 1 category, it falls into 'Other'

        final int sumOfAll = calculateSumOfAll( dataset );
        System.out.println("sumOfAll = " + sumOfAll);
        final List<Comparable> keys = dataset.getKeys();
        int sum = 0;
        boolean aboveThreshold = false;
        int otherSum = 0;
        int categoryCount = 0;
        for (Comparable key : keys) {
            categoryCount++;
            final Number value = dataset.getValue( key );
            int count = value.intValue();

            if( ! aboveThreshold ) {
                System.out.println( "value = " + value );
                final int percentage = (int)(((float)(sum + count) / sumOfAll) * 100);
                System.out.println("percentage = " + percentage);
                aboveThreshold = percentage > collapsingThreshold;
                sum += count;
            }

            if( aboveThreshold ) {
                System.out.println("Above threshold, removing item and collapsing it...");
                otherSum += value.intValue();
                dataset.remove( key );
            }
        }

        if( aboveThreshold && categoryCount > 8 ) {
            dataset.setValue( "Other", otherSum );
        }
    }

    protected int calculateSumOfAll(DefaultPieDataset dataset) {
        int sum = 0;
        final List<Comparable> keys = dataset.getKeys();
        for (Comparable key : keys) {
            final Number value = dataset.getValue( key );
            sum += value.intValue();
        }
        return sum;
    }

    private void countTerm(final CrossReference term ) {

        if ( term == null ) {
            return;
        }

        // update count
        final String id = term.getIdentifier();
        Integer countTaxid = mi2count.get(id);
        if (countTaxid == null) {
            mi2count.put(id, 1);
        } else {
            countTaxid++;
            mi2count.put(id, countTaxid);
        }

        // update name
        if (!mi2name.containsKey(id)) {
            String name = term.getText();

            if (name != null) {
                mi2name.put(id, name);
            } else {
                mi2name.put(id, "unknown");
//                TaxonomyService taxonomy = new OLSTaxonomyService();
                // TODO query OLS to find out and cache the information
            }
        }
    }

    public abstract Collection<CrossReference> getTerms( BinaryInteraction interaction );

    public void setChartTitle(String title) {
        this.title = title;
    }

    public void setCollapsingThreshold() {

    }
}
