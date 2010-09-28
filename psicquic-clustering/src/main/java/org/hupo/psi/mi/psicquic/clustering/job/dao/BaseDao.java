package org.hupo.psi.mi.psicquic.clustering.job.dao;

import java.util.Collection;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */
public interface BaseDao<T> {
    int countAll();
    Collection<T> getAll();
    void save( T object );
    void update( T object );
    void delete( T object );
}
