package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import com.google.common.collect.Lists;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.Collection;
import java.util.List;

/**
 * Builds a pie chart based on the interaction type represented in the given MITAB data.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public class InteractionTypePieChartBuilder extends AbstractMiTermPieChartBuilder {

    @Override
    public Collection<CrossReference> getTerms(BinaryInteraction interaction) {
        final List types = interaction.getInteractionTypes();
        Collection<CrossReference> terms = Lists.newArrayListWithExpectedSize(types.size());
        terms.addAll(types);
        return terms;
    }
}
