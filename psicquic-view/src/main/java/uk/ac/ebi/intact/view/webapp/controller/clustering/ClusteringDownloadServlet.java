package uk.ac.ebi.intact.view.webapp.controller.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.NotSupportedTypeException;
import org.hupo.psi.mi.psicquic.PsicquicServiceException;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.clustering.ClusteringServiceException;
import org.hupo.psi.mi.psicquic.clustering.DefaultInteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.InteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.job.JobNotCompletedException;
import org.springframework.beans.factory.annotation.Autowired;
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

        final String jobId = request.getParameter("jobId");
        String query = request.getParameter("query");
        if( query == null) {
            query = "*";
        }
        String format = request.getParameter("format");
        if( format == null) {
            format = InteractionClusteringService.RETURN_TYPE_MITAB25;
        }

        ServletOutputStream stream = null;

        stream = response.getOutputStream();
        response.setContentType("text/plain");

        try {

            // TODO chunk the data instead of throwing it all at once

            final QueryResponse resp = clusteringService.query(jobId,
                                                               query,
                                                               0,
                                                               Integer.MAX_VALUE,
                                                               format);

            // convert to a list of BinaryInteraction
            if( log.isInfoEnabled() ) log.info("Total MITAB lines: " + resp.getResultInfo().getTotalResults() );

            String mitab = resp.getResultSet().getMitab();
            stream.write(mitab.getBytes());

        } catch ( ClusteringServiceException e ) {
            throw new RuntimeException( "Failed to query local clustered data", e );
        }

        stream.flush();
        stream.close();
    }
}
