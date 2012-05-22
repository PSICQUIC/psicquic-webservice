package uk.ac.ebi.intact.view.webapp.controller.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.clustering.ClusteringServiceException;
import org.hupo.psi.mi.psicquic.clustering.InteractionClusteringService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to download the data stored in the local index holding clustered data.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.2
 */
@Controller
public class ClusteringDownloadServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ClusteringDownloadServlet.class);

    private InteractionClusteringService clusteringService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        clusteringService = (InteractionClusteringService) ctx.getBean( "defaultInteractionClusteringService" );
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String jobId = request.getParameter( "jobId" );
        String query = request.getParameter( "query" );
        if( query == null) {
            query = "*";
        }
        String format = request.getParameter( "format" );
        if( format == null) {
            format = InteractionClusteringService.RETURN_TYPE_MITAB25;
        }

        response.setContentType( "text/plain" );
        final ServletOutputStream stream = response.getOutputStream();

        try {
            int current = 0;
            final int batchSize = 200;
            QueryResponse resp;
            do {
                // query local data
                resp = clusteringService.query(jobId, query, current, batchSize, format );

                // Write MITAB on the output stream
                String mitab = resp.getResultSet().getMitab();

                // the job could be found/and and some results could be found associated with this job.
                if (mitab != null){
                    stream.write(mitab.getBytes());
                    stream.flush();
                }

                current += batchSize;
            } while ( current < resp.getResultInfo().getTotalResults() );

            // convert to a list of BinaryInteraction
            if( log.isInfoEnabled() ) log.info("Total MITAB lines: " + resp.getResultInfo().getTotalResults() );

        } catch ( ClusteringServiceException e ) {
            throw new RuntimeException( "Failed to query local clustered data", e );
        } finally {
            stream.flush();
            stream.close();
        }
    }
}
