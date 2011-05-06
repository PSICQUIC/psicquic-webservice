/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.PsicquicViewConfig;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User query object wrapper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("userQuery")
@Scope("conversation.access")
@ConversationName("general")
public class UserQuery extends BaseController {

    public static final String STAR_QUERY = "*:*";

    @Autowired
    private PsicquicViewConfig config;

    private String searchQuery = "*";

    private Map<String, String> termMap = new HashMap<String,String>();

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    private boolean userSortOrder = DEFAULT_SORT_ORDER;

    private int pageSize = 30;

    private boolean showNewFieldPanel;
    private QueryToken newQueryToken;


    private SearchField[] searchFields;

    private List<SelectItem> searchFieldSelectItems;
    private Map<String,SearchField> searchFieldsMap;

    public UserQuery() {
    }

    @PostConstruct
    public void reset() {
        this.searchQuery = "*";
        this.userSortColumn = DEFAULT_SORT_COLUMN;
        this.userSortOrder = DEFAULT_SORT_ORDER;

        clearFilters();

        initSearchFields();


    }

    public void clearFilters() {
        termMap.clear();
    }

    private void initSearchFields() {

        searchFields = new SearchField[]{
                new SearchField("", "All"),
                new SearchField("id", "Participant Id"),
                new SearchField("interaction_id", "Interaction Id"),
                new SearchField("detmethod", "Detection method"),
                new SearchField("type", "Interaction type"),
                new SearchField("species", "Organism"),
                new SearchField("pubid", "Pubmed Id"),
                new SearchField("pubauth", "Author")
        };

        searchFieldSelectItems = new ArrayList<SelectItem>(searchFields.length);

        for (SearchField searchField : searchFields) {
            searchFieldSelectItems.add(new SelectItem(searchField.getName(), searchField.getDisplayName()));
        }

        searchFieldsMap = new HashMap<String, SearchField>();

        for (SearchField field : searchFields) {
            searchFieldsMap.put(field.getName(), field);
        }
    }

    public void clearSearchFilters(ActionEvent evt) {
        clearFilters();
    }


    public void doShowAddFieldPanel(ActionEvent evt) {
        showAddFieldsPanel();
        newQueryToken = new QueryToken("");
    }

    public void doAddFieldToQuery(ActionEvent evt) {
        doAddFieldToQuery(newQueryToken);
    }

    public void doAddFieldToQuery(QueryToken queryToken) {
        if (!isWildcardQuery(queryToken.getQuery())) {

            if (isWildcardQuery(searchQuery)) {
                searchQuery = queryToken.toQuerySyntax(true);
            } else {
                searchQuery = surroundByBracesIfNecessary(searchQuery);
                searchQuery = searchQuery + " "+queryToken.toQuerySyntax();
            }
        }

        hideAddFieldsPanel();
    }

    public boolean isWildcardQuery() {
        return isWildcardQuery(searchQuery);
    }

    private boolean isWildcardQuery(String query) {
        return (query == null || query.trim().length() == 0 || "*".equals(query) || "*:*".equals(query));
    }

    private String surroundByBracesIfNecessary(String query) {
        if (query.contains(" AND ") || query.contains(" OR ")) {
            query = "("+query+")";
        }

        return query;
    }

    public void doCancelAddField(ActionEvent evt) {
        hideAddFieldsPanel();
    }

    private void showAddFieldsPanel() {
        showNewFieldPanel = true;
    }

    private void hideAddFieldsPanel() {
        newQueryToken = null;
        showNewFieldPanel = false;
    }

    public String getSearchQuery() {
        if ("".equals(searchQuery) || "*:*".equals(searchQuery)) {
            searchQuery = "*";
        }
        return searchQuery;
    }

    public String getFilteredSearchQuery() {
        String query = getSearchQuery();
        String miqlFilterQuery = config.getMiqlFilterQuery();

         if (miqlFilterQuery == null || miqlFilterQuery.length() == 0) {
            return query;
        } else {
            if (query.equals("*") || query.length() == 0) {
                query = miqlFilterQuery;
            } else {
                query = "("+query+") AND ("+miqlFilterQuery+")";
            }
        }

        return query;

    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void resetSearchQuery(){
        this.searchQuery = null;
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }
            
    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean getUserSortOrder() {
        return userSortOrder;
    }

    public void setUserSortOrder( boolean userSortOrder ) {
        this.userSortOrder = userSortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, String> getTermMap() {
        return termMap;
    }

    public void setTermMap( Map<String, String> termMap ) {
        this.termMap = termMap;
    }

       public List<SelectItem> getSearchFieldSelectItems() {
        return searchFieldSelectItems;
    }

    public boolean isShowNewFieldPanel() {
        return showNewFieldPanel;
    }

    public QueryToken getNewQueryToken() {
        return newQueryToken;
    }

    public void setNewQueryToken(QueryToken newQueryToken) {
        this.newQueryToken = newQueryToken;
    }

    public Map<String, SearchField> getSearchFieldsMap() {
        return searchFieldsMap;
    }
}