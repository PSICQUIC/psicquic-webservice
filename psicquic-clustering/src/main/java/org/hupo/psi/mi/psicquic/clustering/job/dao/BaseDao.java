package org.hupo.psi.mi.psicquic.clustering.job.dao;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */
public interface BaseDao<T> {
    void save( T object );
    void update( T object );
    void delete( T object );
}
