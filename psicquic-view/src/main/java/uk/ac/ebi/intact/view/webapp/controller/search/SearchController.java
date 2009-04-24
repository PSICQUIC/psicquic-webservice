package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.model.PsicquicResultDataModel;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * Search controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = {"/main.xhtml"})
public class SearchController extends BaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    public static final String QUERY_PARAM = "query";
    public static final String ONTOLOGY_QUERY_PARAM = "ontologyQuery";
    public static final String TERMID_QUERY_PARAM = "termId";

    // table IDs
    public static final String INTERACTIONS_TABLE_ID = "interactionResults";
    public static final String PROTEINS_TABLE_ID = "proteinListResults";
    public static final String COMPOUNDS_TABLE_ID = "compoundListResults";
    public static final String NUCLEIC_ACID_TABLE_ID = "nucleicAcidListResults";

    @Autowired
    private UserQuery userQuery;

    private int totalResults;

    // results
    private PsicquicResultDataModel results;


    private boolean showAlternativeIds;

    // io
    private String exportFormat;

    //sorting
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    public SearchController() {
    }

    public String doBinarySearchAction() {
        String searchQuery = userQuery.getSearchQuery();

        doBinarySearch( searchQuery );

        return "interactions";
    }

    public String doNewBinarySearch() {
        return doBinarySearchAction();
    }

    public void doBinarySearch(ActionEvent evt) {
        refreshComponent("mainPanels");
        doBinarySearchAction();
    }

    public void doClearFilterAndSearch(ActionEvent evt) {
        userQuery.clearFilters();
        doBinarySearch(evt);
    }

    public void doBinarySearch(String searchQuery) {
        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tquery:  "+ searchQuery );}

            results = new PsicquicResultDataModel(new UniversalPsicquicClient("http://www.ebi.ac.uk/intact/psicquic/webservices/psicquic"),
                    searchQuery);

            totalResults = results.getRowCount();

            if ( log.isDebugEnabled() ) log.debug( "\tResults: " + results.getRowCount() );

            if ( totalResults == 0 ) {
                addErrorMessage( "Your query didn't return any results", "Use a different query" );
            }

        } catch ( Throwable throwable ) {

            if ( searchQuery != null && ( searchQuery.startsWith( "*" ) || searchQuery.startsWith( "?" ) ) ) {
                userQuery.setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ searchQuery +"' is not correctly formatted",
                                 "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                 "However, wildcard characters can be used anywhere else in one's query (eg. g?vin or gav* for gavin). " +
                                 "Please do reformat your query." );
            } else {
                addErrorMessage("Psicquic problem", throwable.getMessage());
            }
        }
    }

    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());

        UIXTable table = (UIXTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(INTERACTIONS_TABLE_ID);
        table.setFirst(evt.getNewStart());

        refreshTable(INTERACTIONS_TABLE_ID, results);
    }

    // Getters & Setters
    /////////////////////


    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat( String exportFormat ) {
        this.exportFormat = exportFormat;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults( int totalResults ) {
        this.totalResults = totalResults;
    }

    public String getFirstResultIndex() {
        UIXTable table = (UIXTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(INTERACTIONS_TABLE_ID);
        return String.valueOf(table.getFirst());
    }

    public PsicquicResultDataModel getResults() {
        return results;
    }

    public void setResults(PsicquicResultDataModel results) {
        this.results = results;
    }

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }
}