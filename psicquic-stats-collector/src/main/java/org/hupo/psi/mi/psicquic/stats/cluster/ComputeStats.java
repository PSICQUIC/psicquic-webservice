package org.hupo.psi.mi.psicquic.stats.cluster;

import uk.ac.ebi.enfin.mi.cluster.ClusterContext;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.cache.CacheManager;
import uk.ac.ebi.enfin.mi.cluster.cache.CacheStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */
public class ComputeStats {

    public static void main( String[] args ) {

        final ClusterContext context = ClusterContext.getInstance();
        context.setCacheStrategy( CacheStrategy.ON_DISK );
        final CacheManager cacheManager = context.getCacheManager();
        cacheManager.setResetCache( false );

        final File cacheLocation = new File( "all_psicquic.cache" );
        ClusterContext.getInstance().getCacheManager().setCacheStorage( cacheLocation );
        System.out.println( "Processing cache stored in: " + cacheLocation.getAbsolutePath() );

        final Map<Integer, EncoreInteraction> cache = cacheManager.getInteractionCache();
        System.out.println( "cluster count: " + cache.size() );

        final Map<String, ServiceStat> service2stats = new HashMap<String, ServiceStat>();

        // clusters are stored iusing a cluster id ( 1 .. cache.size() )
        for ( int i = 1; i <= cache.size(); i++ ) {

            EncoreInteraction interaction = cache.get( i );

            System.out.println( i );

            final Map<String, Integer> service2count = new HashMap<String, Integer>();

            for ( Map.Entry<String, List<String>> dbEntry : interaction.getExperimentToDatabase().entrySet() ) {
                // TODO why is that a list ???
                final String interactionAc = dbEntry.getKey();
                final List<String> serviceName = dbEntry.getValue();
                System.out.println( "\t" + interactionAc + " -> " + serviceName );
                for ( String name : serviceName ) {

                    if ( !service2count.containsKey( name ) ) {
                        service2count.put( name, 0 );
                    }
                    int count = service2count.get( name );
                    Integer newCount = count + 1;
                    service2count.put( name, newCount );
                    System.out.println( "\t\t" + name + ":" + newCount );
                }
            } // evidences

            // update the services' stats
            for ( Map.Entry<String, Integer> serviceEntry : service2count.entrySet() ) {
                String name = serviceEntry.getKey();
                int count = serviceEntry.getValue();

                if ( !service2stats.containsKey( name ) ) {
                    service2stats.put( name, new ServiceStat( name ) );
                }
                final ServiceStat stat = service2stats.get( name );
                switch ( count ) {
                    case 0:
                        throw new IllegalStateException( "service: " + name );

                    case 1:
                        if ( service2count.size() > 1 ) {
                            stat.incrementMultipleEvidenceWithOtherCount();
                        } else {
                            stat.incrementUniqueCount();
                        }
                        break;

                    default:
                        // more than 1 evidence in this service
                        if ( service2count.size() > 1 ) {
                            stat.incrementMultipleEvidenceWithOtherCount();
                        } else {
                            stat.incrementMultipleEvidenceWithinCount();
                        }
                } // evidence count per service
            } // services' stats

            System.out.println( "--- Stats ---" );
            for ( Map.Entry<String, ServiceStat> serviceStatEntry : service2stats.entrySet() ) {
                final ServiceStat stat = serviceStatEntry.getValue();
                System.out.println( stat );
            }

        } // clusters

        System.out.println( "Collected stats about " + service2stats.size() + " services" );

        ClusterContext.getInstance().getCacheManager().shutdown();
    }
}
