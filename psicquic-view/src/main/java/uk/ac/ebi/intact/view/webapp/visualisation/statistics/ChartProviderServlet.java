package uk.ac.ebi.intact.view.webapp.visualisation.statistics;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import psidev.psi.mi.xml.converter.ConverterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet enabling the PNG export of a chart (JFreeChart).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public class ChartProviderServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ChartProviderServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String url = request.getParameter("url"); // REST URL of the data to analyse
        final String chartName = request.getParameter("name");
        final String height = request.getParameter("height");
        final String width = request.getParameter("width");

        PsicquicChartFactory factory = PsicquicChartFactory.createInstance(url);
        try {
            final long start = System.currentTimeMillis();
            factory.build();
            final long stop = System.currentTimeMillis();
            System.out.println( "Time to build charts: " + (stop - start) + "ms" );
        } catch (ConverterException e) {
            throw new RuntimeException( "Could not parse MITAB data.", e );
        }
        final JFreeChart chart = factory.getChart( chartName );

        OutputStream out = response.getOutputStream();

        response.setContentType("image/png");
        ChartUtilities.writeChartAsPNG(out, chart, 500, 400);

        out.flush();
        out.close();
    }
}
