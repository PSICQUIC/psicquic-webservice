/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.SortCriterion;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * DataModel for dar retreived from PSICQUIC services.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicResultDataModel extends SortableModel implements Serializable {

    private static final Log log = LogFactory.getLog(PsicquicResultDataModel.class);

    private static String DEFAULT_SORT_COLUMN = "rigid";

    private String query;
    private PsicquicSimpleClient psicquicClient;

    private SearchResult<BinaryInteraction> result;
    private int rowIndex = -1;
    private int firstResult = 0;
    private int maxResults = 50;

    private String sortColumn = DEFAULT_SORT_COLUMN;
    private Boolean ascending = true;

    private Map<String,Boolean> columnSorts;
    
    private DocumentDefinition mitabDocumentDefinition;

    public PsicquicResultDataModel(PsicquicSimpleClient psicquicClient, String query) throws IOException {
        if (psicquicClient == null) {
            throw new IllegalArgumentException("Trying to create data model with a null Psicquic client");
        }
        if (query == null) {
            throw new IllegalArgumentException("Trying to create data model with a null query");
        }

        this.psicquicClient = psicquicClient;
        this.query = query;
        this.mitabDocumentDefinition = new MitabDocumentDefinition();

        columnSorts = new HashMap<String, Boolean>(16);

        setRowIndex(0);
        fetchResults();

        setWrappedData(result);
    }

    protected void fetchResults() throws IOException {
        if (query == null) {
            throw new IllegalStateException("Trying to fetch results for a null query");
        }


        if (log.isDebugEnabled()) log.debug("Fetching results: "+ query);

        int totalCount = Long.valueOf(psicquicClient.countByQuery(query)).intValue();

        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>(maxResults);

        InputStream gzippedStream = psicquicClient.getByQuery(query, PsicquicSimpleClient.MITAB25_COMPRESSED, firstResult, maxResults);

        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(gzippedStream)));
        String str;
        while ((str = in.readLine()) != null) {
            BinaryInteraction interaction = toBinaryInteraction(str);
            binaryInteractions.add(interaction);
        }
        in.close();
        gzippedStream.close();
        
        result = new SearchResult<BinaryInteraction>(binaryInteractions, totalCount, firstResult, maxResults, null);
    }

    private BinaryInteraction toBinaryInteraction(String str) {
        return mitabDocumentDefinition.interactionFromString(str);
    }

    public int getRowCount() {
        return Long.valueOf(result.getTotalCount()).intValue();
    }

    public Object getRowData() {
        if (result == null) {
            return null;
        }

        if (!isRowWithinResultRange()) {
            firstResult = getRowIndex();
            try {
                fetchResults();
            } catch (IOException e) {
                throw new IllegalStateException("Problem running psicquic", e);
            }
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        final BinaryInteraction binaryInteraction = result.getData().get(rowIndex - firstResult);
        return binaryInteraction;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object getWrappedData() {
        return result;
    }

    public boolean isRowAvailable() {
        if (result == null) {
            return false;
        }

        return rowIndex >= 0 && rowIndex < result.getTotalCount();
    }

    protected boolean isRowWithinResultRange() {
        return (getRowIndex() >= firstResult) && (getRowIndex() < (firstResult+maxResults));
    }

    public void setRowIndex(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException("illegal rowIndex " + rowIndex);
        }
        int oldRowIndex = rowIndex;
        this.rowIndex = rowIndex;
        if (result != null && oldRowIndex != this.rowIndex) {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, this.rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].rowSelected(event);
            }
        }
    }

    @Override
       public void setSortCriteria(List<SortCriterion> criteria) {
           if ((criteria == null) || (criteria.isEmpty())) {
               this.sortColumn = DEFAULT_SORT_COLUMN;
               columnSorts.clear();
           }
           else {
               // only use the first criterion
               SortCriterion criterion = criteria.get(0);

               this.sortColumn = criterion.getProperty();

               if (columnSorts.containsKey(sortColumn)) {
                   this.ascending = !columnSorts.get(sortColumn);
               } else {
                   this.ascending = criterion.isAscending();
               }
               columnSorts.put(sortColumn, ascending);

               if (log.isDebugEnabled())
                   log.debug("\tSorting by '" + criterion.getProperty() + "' " + (criterion.isAscending() ? "ASC" : "DESC"));
           }

        try {
            fetchResults();
        } catch (IOException e) {
            throw new RuntimeException("Problem fetching results using psicquic", e);
        }
    }


    /**
     * Checks to see if the underlying collection is sortable by the given property.
     *
     * @param property The name of the property to sort the underlying collection by.
     * @return true, if the property implements java.lang.Comparable
     */
    @Override
    public boolean isSortable(String property) {
        return true;
    }

    @Override
    public Object getRowKey() {
        return isRowAvailable()
               ? getRowIndex()
               : null;
    }

    @Override
    public void setRowKey(Object key) {
        if (key == null) {
            setRowIndex(-1);
        } else {
            setRowIndex((Integer) key);
        }
    }


    public SearchResult<BinaryInteraction> getResult() {
        return result;
    }

    public String getSearchQuery() {
        return query;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public boolean isAscending() {
        return ascending;
    }
}