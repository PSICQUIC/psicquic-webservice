package uk.ac.ebi.intact.view.webapp.visualisation;

import com.google.common.collect.Lists;
import psidev.psi.mi.tab.model.Alias;

import java.util.Comparator;
import java.util.List;

/**
 * A comparator for <code>Alias</code> based on a preference list of alias types.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.2
 */
public class AliasByIncreasingLengthComparator implements AliasComparator {

    ////////////////////
    // Comparator

    public int compare(Alias a1, Alias a2) {
        final int len1 = a1.getName().length();
        final int len2 = a2.getName().length();
        return len1 - len2; // shortest names will be first in the list
    }

    public boolean hasMatchedAny() {
        return true;
    }

    public boolean matches(Alias a) {
        return true;
    }
}
