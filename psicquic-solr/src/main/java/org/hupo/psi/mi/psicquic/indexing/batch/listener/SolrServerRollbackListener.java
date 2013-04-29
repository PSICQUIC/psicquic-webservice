package org.hupo.psi.mi.psicquic.indexing.batch.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.retry.RetryCallback;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.RetryListener;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * This listener will rollback any added documents to the solr server that have not been commited by a SolrItemWriter so in the retry process
 * we don't add the same documents twice in the solr server when a SolrServerException occured
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/04/13</pre>
 */

public class SolrServerRollbackListener implements RetryListener {

    private static final Log log = LogFactory.getLog(SolrServerRollbackListener.class);

    protected String solrUrl;
    protected HttpSolrServer solrServer;

    // settings SOLRServer
    private int maxTotalConnections = 128;
    private int defaultMaxConnectionsPerHost = 24;
    private boolean allowCompression = true;
    int numberOfRetries = 5;

    public <T> boolean open(RetryContext context, RetryCallback<T> callback) {
        if (solrUrl != null && solrServer == null) {
            try {
                createSolrServer();
                return true;
            } catch (SAXException e) {
                log.error("Impossible to create a new HTTP solr server",e);
            } catch (ParserConfigurationException e) {
                log.error("Impossible to create a new HTTP solr server",e);
            }
            catch (IOException e) {
                log.error("Cannot connect to solr server: "+ solrUrl, e);
            }
        }

        return solrServer != null;
    }

    public <T> void close(RetryContext context, RetryCallback<T> callback, Throwable throwable) {
        // do noting
    }

    public <T> void onError(RetryContext context, RetryCallback<T> callback, Throwable throwable) {

        if (solrServer != null){
            try {
                solrServer.rollback();
            } catch (SolrServerException e) {
                retryRollback(context, e);
            } catch (IOException e) {
                retryRollback(context, e);
            }
        }
    }

    private void retryRollback(RetryContext context, Exception e) {
        int number = 1;
        boolean didRollback = false;
        while (number < numberOfRetries && !didRollback){
            try {
                solrServer.rollback();
                didRollback = true;
            } catch (SolrServerException e1) {
                log.error(e);
                number++;
            } catch (IOException e1) {
                log.error(e);
                number++;
            }
        }
        if (!didRollback){
            // stop the job here
            context.setExhaustedOnly();
            log.error("Impossible to rollback added documents while retrying the indexing step.", e);
        }
    }

    public SolrServer createSolrServer() throws IOException, SAXException, ParserConfigurationException {
        if (solrServer == null) {
            if (solrUrl == null) {
                throw new NullPointerException("No 'solr url' configured for SolrItemWriter");
            }

            solrServer = new HttpSolrServer(solrUrl, createHttpClient());
            solrServer.setMaxRetries(0);
            solrServer.setAllowCompression(allowCompression);

        }

        return solrServer;
    }

    protected HttpClient createHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(maxTotalConnections);
        cm.setDefaultMaxPerRoute(defaultMaxConnectionsPerHost);

        HttpClient httpClient = new DefaultHttpClient(cm);

        return httpClient;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getDefaultMaxConnectionsPerHost() {
        return defaultMaxConnectionsPerHost;
    }

    public void setDefaultMaxConnectionsPerHost(int defaultMaxConnectionsPerHost) {
        this.defaultMaxConnectionsPerHost = defaultMaxConnectionsPerHost;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }

    public boolean isAllowCompression() {
        return allowCompression;
    }

    public void setAllowCompression(boolean allowCompression) {
        this.allowCompression = allowCompression;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }
}
