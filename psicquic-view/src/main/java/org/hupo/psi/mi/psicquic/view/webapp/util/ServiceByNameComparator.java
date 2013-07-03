package org.hupo.psi.mi.psicquic.view.webapp.util;

import org.hupo.psi.mi.psicquic.view.webapp.controller.QueryHits;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 19/06/2013
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class ServiceByNameComparator implements Comparator<QueryHits> {

    @Override
    public int compare(QueryHits queryHits, QueryHits queryHits2) {
            return queryHits.getName().toLowerCase().compareTo(queryHits2.getName().toLowerCase());
    }

}
