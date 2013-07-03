package org.hupo.psi.mi.psicquic.view.webapp.util;

import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 01/07/2013
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class CvTerms {

    private static final ResourceBundle rb = ResourceBundle.getBundle("org.hupo.psi.mi.psicquic.RegistryTags");

    public static String cvTermToName(String ac) {
        return rb.getString(ac);
    }
}
