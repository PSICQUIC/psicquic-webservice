package uk.ac.ebi.intact.view.webapp.visualisation;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.Comparator;

/**
 * Alias comparator.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.2
 */
public interface AliasComparator extends Comparator<Alias> {

    int compare(Alias a1, Alias a2);

    boolean hasMatchedAny();

    boolean matches( Alias a );
}
