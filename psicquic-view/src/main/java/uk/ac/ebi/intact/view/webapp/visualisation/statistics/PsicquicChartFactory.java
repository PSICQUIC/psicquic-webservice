package uk.ac.ebi.intact.view.webapp.visualisation.statistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jfree.chart.JFreeChart;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Responsible for loading data and generating the Charts.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.3
 */
public class PsicquicChartFactory {

    // Design note: we are creating one Factory per REST URL, that way we can read the MITAB data once and build all
    //              necessary charts in one pass.

    private Collection<ChartBuilder> chartBuilders = Lists.newArrayList();
    private Map<String, ChartBuilder> name2chart = Maps.newHashMap();

    private boolean built = false;

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
        method.setChartTitle( "Interaction detection method representation" );
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
        return builder.build();
    }

    public synchronized void dispose() {
        url2factory.remove( url );
    }
}
