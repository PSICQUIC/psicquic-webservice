package uk.ac.ebi.intact.view.webapp.visualisation;

import psidev.psi.mi.xml.converter.ConverterException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Servlet enabling the conversion of a MITAB Stream into GraphML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.2
 */
public class GraphmlServlet extends HttpServlet {

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

        final String mitabUrl = request.getParameter("mitabUrl");
        final URL inputUrl = new URL( mitabUrl );
        final InputStream is = inputUrl.openStream();

        ServletOutputStream stream = null;

        stream = response.getOutputStream();
        response.setContentType("text/plain");

        final GraphmlBuilder builder = new GraphmlBuilder();
        String output = null;
        try {
            output = builder.build( is );
        } catch (ConverterException e) {
            throw new IllegalStateException( "Could not parse input MITAB.", e );
        }
        stream.write( output.getBytes() );

        stream.flush();
        stream.close();
    }
}
