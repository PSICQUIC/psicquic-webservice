/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.util;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.view.webapp.controller.application.XrefLinkContext;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class XrefLinkGenerator
{

    public static String generateUrl(XrefLinkContext linkContext, CrossReference xref) {
        if (linkContext == null) return null;
        if (xref == null) return null;

        return generateUrl(linkContext, xref.getDatabase(), xref.getIdentifier());
    }

    public static String generateUrl(XrefLinkContext linkContext, String database, String identifier) {
        if (database.equals("taxid")) {
            return replacePlaceholderWithId(linkContext.getOlsUrl(), identifier);
        } else if (database.equals("MI")) {
            return replacePlaceholderWithId(linkContext.getOlsUrl(), "MI:" + identifier);
        } else if (database.equals("pubmed")) {
            return replacePlaceholderWithId(linkContext.getCitexploreUrl(), identifier);
        } else if (database.equals("hierarchView")) {
            return replacePlaceholderWithId(linkContext.getHierarchViewUrl(), identifier);
        } else if (linkContext.containsKey(database)) {
            // here, the database is the same as the key in the props file (e.g. uniprotkb, intact...)
            return replacePlaceholderWithId(linkContext.getUrl(database), identifier);
        }

        return null;
    }

    private static String replacePlaceholderWithId(String url, String identifier) {
        return url.replaceAll("\\{0\\}", identifier);
    }
}