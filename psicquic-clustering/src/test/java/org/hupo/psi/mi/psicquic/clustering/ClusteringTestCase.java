package org.hupo.psi.mi.psicquic.clustering;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class for clustering test case.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
@ContextConfiguration( locations = "classpath:job-clustering.xml" )
@RunWith( SpringJUnit4ClassRunner.class )
public abstract class ClusteringTestCase {


}
