package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Responsible for loading data and generating the Charts.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public class PsicquicChartFactory {

    private static List<Color> colorScheme1 = Lists.newArrayList(); // pastels

    private static List<Color> colorScheme2 = Lists.newArrayList(); // stronger colors

    private static List<List<Color>> schemes = Lists.newArrayList();

    static {
        // scheme generated with http://www.colorschemer.com/online.html

        /////////////
        // Scheme 1


        colorScheme1.add( new Color(217, 255, 179) ); // green
        colorScheme1.add( new Color(179, 255, 179) ); // green
        colorScheme1.add( new Color(179, 255, 217) ); // blue

        colorScheme1.add( new Color(255, 217, 179) ); // salmon
        colorScheme1.add( new Color(255, 255, 117) ); // light yellow
        colorScheme1.add( new Color(255, 255, 56) );  // darker yellow
        colorScheme1.add( new Color(179,255,255) );   // cyan

        colorScheme1.add( new Color(255, 179, 179) ); // pink
        colorScheme1.add( new Color(107, 107, 255) ); // blue
        colorScheme1.add( new Color(168, 168, 255) ); // blue
        colorScheme1.add( new Color(179, 217, 255) ); // light blue

        colorScheme1.add( new Color(255, 179, 217) ); // pink
        colorScheme1.add( new Color(255, 179, 255) ); // pink
        colorScheme1.add( new Color(217, 179, 255) ); // purple
        colorScheme1.add( new Color(179, 179, 255) ); // blue

        colorScheme1.add( new Color(255, 255, 179) ); // yellow

        schemes.add( colorScheme1 );

        /////////////
        // Scheme 2

        colorScheme2.add( new Color(197, 255, 61) ); // ?
        colorScheme2.add( new Color(100, 255, 61) ); // ?
        colorScheme2.add( new Color(61, 255, 119) ); // ?
        colorScheme2.add( new Color(61,255,216) ); // ?

        colorScheme2.add( new Color(255, 216, 61) ); // ?
        colorScheme2.add( new Color(179, 255, 0) ); // ?
        colorScheme2.add( new Color(136, 194, 0) ); // ?
        colorScheme2.add( new Color(61, 197, 255) ); // ?

        colorScheme2.add( new Color(255, 119, 61) ); // ?
        colorScheme2.add( new Color(58, 0, 194) ); // ?
        colorScheme2.add( new Color(77, 0, 255) ); // ?
        colorScheme2.add( new Color(61, 100, 255) ); // ?

        colorScheme2.add( new Color(255, 61, 100) ); // ?
        colorScheme2.add( new Color(255, 61, 197) ); // ?
        colorScheme2.add( new Color(216, 61, 255) ); // ?
        colorScheme2.add( new Color(119, 61, 255) ); // ?

        schemes.add( colorScheme2 );
    }

    // Design note: we are creating one Factory per REST URL, that way we can read the MITAB data once and build all
    //              necessary charts in one pass.

    private Collection<ChartBuilder> chartBuilders = Lists.newArrayList();
    private Map<String, ChartBuilder> name2chart = Maps.newHashMap();

    private boolean built = false;

    private boolean buildStarted = false;

    private final String url;

    public PsicquicChartFactory( String url ) {
        this.url = url;

        final ChartBuilder organism = new OrganismPieChartBuilder();
        chartBuilders.add(organism);
        name2chart.put( "species", organism );

        final AbstractMiTermPieChartBuilder type = new InteractionTypePieChartBuilder();
        type.setChartTitle( "Interaction type representation" );
        chartBuilders.add(type);
        name2chart.put( "type", type );

        final AbstractMiTermPieChartBuilder method = new DetectionMethodPieChartBuilder();
        method.setChartTitle("Interaction detection method representation");
        chartBuilders.add(method);
        name2chart.put( "detMethod", method );

        // Add more charts here ...
    }

    // TODO FIXME handling instances this way will lead to memory leak.

    private static Map<String, PsicquicChartFactory> url2factory = Maps.newHashMap();

    public synchronized static PsicquicChartFactory createInstance(String url) {
        if( ! url2factory.containsKey( url ) ) {
            url2factory.put( url, new PsicquicChartFactory( url ) );
        }
        return url2factory.get( url );
    }

    public synchronized void build() throws ConverterException, IOException {

        if( built ) {
            System.out.println( "PsicquicChartFactory.build: Charts already built, skip" );
            return;
        }

        buildStarted = true;

        System.out.println( "PsicquicChartFactory.build: Building charts ( "+url+" )..." );

        PsimiTabReader mitabReader = new PsimiTabReader( false );
        final URL mitabUrl = new URL(url);
        final InputStream is = mitabUrl.openStream();
        final Iterator<BinaryInteraction> iterator = mitabReader.iterate(is);

        while (iterator.hasNext()) {
            BinaryInteraction interaction = iterator.next();

            for (ChartBuilder builder : chartBuilders) {
                builder.append( interaction );
            }
        }

        is.close();

        built = true;
    }

    public JFreeChart getChart( String name ) {
        final ChartBuilder builder = name2chart.get(name);
        if( builder == null ) {
            throw new IllegalArgumentException( "Could not find a chart by name: " + name );
        }
        final JFreeChart chart = builder.build();

        // Apply general customization
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        final Plot plot = chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint( Color.WHITE );
        plot.setNoDataMessage( "No data available" );
        if( plot instanceof PiePlot ) {
            final PiePlot pp = (PiePlot) plot;
            pp.setLabelGenerator(null);

            // set custom section color
            setPieChartSectionColors(pp, schemes.get( (int)Math.random() % schemes.size() ) );
        }

        final LegendTitle chartLegend = chart.getLegend();
        chartLegend.setPosition(RectangleEdge.RIGHT);
        chartLegend.setHorizontalAlignment(HorizontalAlignment.LEFT);
        chartLegend.setBackgroundPaint( new Color( 245, 245, 245 ) ); // very light grey

        return chart;
    }

    private void setPieChartSectionColors(PiePlot pp, List<Color> colorScheme) {
        final List<Comparable> keys = pp.getDataset().getKeys();
        int idx = 0;
        for (Comparable key : keys) {
            final Color color = colorScheme.get(idx % colorScheme.size());
            pp.setSectionPaint( key, color );
            idx++;
        }
    }

    public synchronized boolean isChartReady() {
        return built;
    }

    public synchronized void dispose() {
        url2factory.remove( url );
    }
}
