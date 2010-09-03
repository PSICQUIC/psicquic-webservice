package org.hupo.psi.mi.psicquic.clustering;

/**
 * Basic description of a PSICQUIC service.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class Service implements Comparable<Service> {

    String name;
    String restUrl;

    public Service( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl( String restUrl ) {
        this.restUrl = restUrl;
    }

    //////////////////
    // Comparable

    public int compareTo( Service s ) {
        return this.getName().compareTo( s.getName() );
    }
}
