/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class SearchCache {

    private Map<String,Map<String,Integer>> queryCache;

    public SearchCache() {
        queryCache = new WeakHashMap<String, Map<String, Integer>>(128);
    }

    public boolean contains(String query) {
        return queryCache.containsKey(query);
    }

    public Map<String,Integer> get(String query) {
        return queryCache.get(query);
    }

    public void put(String query, Map<String,Integer> counts) {
        queryCache.put(query, new HashMap<String, Integer>(counts));
    }

    public void clearCache() {
        queryCache.clear();
    }
}
