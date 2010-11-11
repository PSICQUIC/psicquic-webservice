package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A specific ChartBuilder that is based on ontology terms (CrossReference).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public abstract class AbstractMiTermPieChartBuilder implements ChartBuilder {

    private static final Log log = LogFactory.getLog(AbstractMiTermPieChartBuilder.class);

    /**
     * Percentage of data above which pie section are collapsed into a section called 'Other'.
     */
    public static final int DEFAULT_COLLAPSING_THRESHOLD = 95;

    /**
     * Number of pie section above which others are being collapsed into a section called 'Other'.
     */
    private static final int MAX_PIE_SECTION = 16;

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
        final Collection<CrossReference> terms = getTerms(interaction);
        for (CrossReference term : terms) {
            countTerm(term);
        }
    }

    public JFreeChart build() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Integer> entry : mi2count.entrySet()) {
            final String taxid = entry.getKey();
            final Integer count = entry.getValue();
            String termName = mi2name.get(taxid);

            dataset.setValue(termName, count);
        }

        dataset.sortByValues(SortOrder.DESCENDING);

        // Attempt to collapse sections above the set collapsingThreshold.
        collapseChartSections(dataset);

        final boolean hasLegend = true;
        final boolean hasTooltips = false;
        final boolean hasUrls = true;

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                hasLegend, hasTooltips, hasUrls
        );

        return chart;
    }

    /**
     * Looks into the categories and given a set threshold, collapse smaller categories into 'Other'.
     *
     * @param dataset
     */
    private void collapseChartSections(DefaultPieDataset dataset) {

        final int sumOfAll = calculateSumOfAll(dataset);
        if(log.isDebugEnabled()) log.debug("sumOfAll = " + sumOfAll);
        final List<Comparable> keys = dataset.getKeys();
        final Collection<Comparable> keysToRemove = Lists.newArrayList();
        int sum = 0;
        boolean aboveThreshold = false;
        int otherSum = 0;
        int categoryCount = 0;

        final int totalCategoryCount = dataset.getKeys().size();
        if (totalCategoryCount > MAX_PIE_SECTION) {
            for (Comparable key : keys) {
                categoryCount++;
                final Number value = dataset.getValue(key);
                int count = value.intValue();

                if (!aboveThreshold) {
                    final int percentage = (int) (((float) (sum + count) / sumOfAll) * 100);
                    aboveThreshold = (percentage > collapsingThreshold) || categoryCount >= MAX_PIE_SECTION;
                    if(log.isDebugEnabled())log.debug("value = " + value + "  --  " + percentage + "%" + "  --  aboveThreshold:" + aboveThreshold);
                    sum += count;
                }

                if (aboveThreshold) {
                    otherSum += value.intValue();
                    if (categoryCount > 1) { // never remove the first category, even if > collapsingThreshold
                        keysToRemove.add(key);
                    }
                }
            }

            if (aboveThreshold) {
                // remove other keys
                if(log.isDebugEnabled()) log.debug("Above threshold, removing " + keysToRemove.size() + " items...");
                for (Comparable key : keysToRemove) {
                    dataset.remove(key);
                }

                // adding 'Other' category
                dataset.setValue("Other", otherSum);
            }
        } // above MAX_PIE_SECTION
    }

    protected int calculateSumOfAll(DefaultPieDataset dataset) {
        int sum = 0;
        final List<Comparable> keys = dataset.getKeys();
        for (Comparable key : keys) {
            final Number value = dataset.getValue(key);
            sum += value.intValue();
        }
        return sum;
    }

    private void countTerm(final CrossReference term) {

        if (term == null) {
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

    /**
     * Method that allows extraction of a collection of cross reference from the given interaction.
     *
     * @param interaction
     * @return a non null collection of <code>CrossReference</code>.
     */
    public abstract Collection<CrossReference> getTerms(BinaryInteraction interaction);

    public void setChartTitle(String title) {
        this.title = title;
    }

    public void setCollapsingThreshold(int collapsingThreshold) {
        this.collapsingThreshold = collapsingThreshold;
    }
}
