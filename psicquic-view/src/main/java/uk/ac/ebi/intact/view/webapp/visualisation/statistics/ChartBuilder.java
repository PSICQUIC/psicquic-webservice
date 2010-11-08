package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import org.jfree.chart.JFreeChart;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * Contracts for classes in charge of building the data underlying the charts.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public interface ChartBuilder {

    void append( BinaryInteraction interaction );
    
    JFreeChart build();
}
