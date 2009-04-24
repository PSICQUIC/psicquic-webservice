package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.model.PsicquicResultDataModel;

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

    private static final String INTACT_ENDPOINT = "http://www.ebi.ac.uk/intact/psicquic/webservices/psicquic";
    private static final String MINT_ENDPOINT = "http://mint.bio.uniroma2.it/mint/psicquic/webservices/psicquic";
    private static final String IREFINDEX_ENDPOINT = "http://biotin.uio.no:8080/psicquic-ws-1/webservices/psicquic";
    private static final String BIOGRID_ENDPOINT = "http://tyerslab.bio.ed.ac.uk:8080/psicquic-ws/webservices/psicquic";
    private static final String MPIDB_ENDPOINT = "http://www.jcvi.org/mpidb/servlet/webservices/psicquic";

    @Autowired
    private UserQuery userQuery;

    private int totalResults;

    private int intactCount;
    private int mintCount;
    private int irefindexCount;
    private int biogridCount;
    private int mpidbCount;

    // results
    private PsicquicResultDataModel intactResults;
    private PsicquicResultDataModel biogridResults;
    private PsicquicResultDataModel mintResults;
    private PsicquicResultDataModel irefindexResults;
    private PsicquicResultDataModel mpidbResults;

    private boolean showAlternativeIds;

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

            searchAndCreateResultModels();

            totalResults = intactCount + mintCount + biogridCount + mpidbCount + irefindexCount;

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

    private void searchAndCreateResultModels() {
        try {
            intactResults = psicquicResults(INTACT_ENDPOINT);
            intactCount = intactResults.getRowCount();
        } catch (PsicquicClientException e) {
            e.printStackTrace();
        }

        try {
            biogridResults = psicquicResults(BIOGRID_ENDPOINT);
            biogridCount = biogridResults.getRowCount();
        } catch (PsicquicClientException e) {
            e.printStackTrace();
        }

        try {
            mintResults = psicquicResults(MINT_ENDPOINT);
            mintCount = mintResults.getRowCount();
        } catch (PsicquicClientException e) {
            e.printStackTrace();
        }

        try {
            irefindexResults = psicquicResults(IREFINDEX_ENDPOINT);
            irefindexCount = irefindexResults.getRowCount();
        } catch (PsicquicClientException e) {
            e.printStackTrace();
        }

        try {
            mpidbResults = psicquicResults(MPIDB_ENDPOINT);
            mpidbCount = mpidbResults.getRowCount();
        } catch (PsicquicClientException e) {
            e.printStackTrace();
        }
    }

    private PsicquicResultDataModel psicquicResults(String endpoint) throws PsicquicClientException {
        return new PsicquicResultDataModel(new UniversalPsicquicClient(endpoint),
                    userQuery.getSearchQuery());
    }

    // Getters & Setters
    /////////////////////

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults( int totalResults ) {
        this.totalResults = totalResults;
    }

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }

    public int getIntactCount() {
        return intactCount;
    }

    public int getMintCount() {
        return mintCount;
    }

    public int getIrefindexCount() {
        return irefindexCount;
    }

    public int getBiogridCount() {
        return biogridCount;
    }

    public int getMpidbCount() {
        return mpidbCount;
    }

    public PsicquicResultDataModel getIntactResults() {
        return intactResults;
    }

    public void setIntactResults(PsicquicResultDataModel intactResults) {
        this.intactResults = intactResults;
    }

    public PsicquicResultDataModel getBiogridResults() {
        return biogridResults;
    }

    public void setBiogridResults(PsicquicResultDataModel biogridResults) {
        this.biogridResults = biogridResults;
    }

    public PsicquicResultDataModel getMintResults() {
        return mintResults;
    }

    public void setMintResults(PsicquicResultDataModel mintResults) {
        this.mintResults = mintResults;
    }

    public PsicquicResultDataModel getIrefindexResults() {
        return irefindexResults;
    }

    public void setIrefindexResults(PsicquicResultDataModel irefindexResults) {
        this.irefindexResults = irefindexResults;
    }

    public PsicquicResultDataModel getMpidbResults() {
        return mpidbResults;
    }

    public void setMpidbResults(PsicquicResultDataModel mpidbResults) {
        this.mpidbResults = mpidbResults;
    }
}