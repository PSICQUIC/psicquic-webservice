package org.hupo.psi.mi.psicquic.model.server;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * solr jetty runner for testing purposes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/06/12</pre>
 */

public class SolrJettyRunner {

    private static Logger log = LoggerFactory.getLogger(SolrJettyRunner.class);

    private int port = 18080;
    private String host = "127.0.0.1";

    protected Server server;
    protected HttpSolrServer solrServer;

    protected File workingDir;
    protected File solrHome;

    public SolrJettyRunner() {
        this(new File(System.getProperty("java.io.tmpdir"), "solr-home-"+System.currentTimeMillis()));
    }

    public SolrJettyRunner(File workingDir) {
        this.workingDir = workingDir;

        if (log.isInfoEnabled()) log.info("Jetty working dir: "+workingDir);

    }

    public void start() throws Exception {
        File solrWar;

        if (workingDir.exists()) {
            solrHome = new File(workingDir, "solr-home");
            solrWar = new File(workingDir, "solr.war");

            if (!solrHome.exists()) {
                throw new IllegalStateException("Working dir "+workingDir+" exists, but no solr-home/ directory could be found");
            }

            if (!solrWar.exists()) {
                throw new IllegalStateException("Working dir "+workingDir+" exists, but no solr.war folder could be found");
            }

            if (log.isDebugEnabled()) log.debug("Using existing directory");

        } else {
            SolrHomeBuilder solrHomeBuilder = new SolrHomeBuilder();

            solrHomeBuilder.install(workingDir);

            solrHome = solrHomeBuilder.getSolrHomeDir();
            solrWar = solrHomeBuilder.getSolrWar();
        }

        // create index folder
        FSDirectory dir = FSDirectory.open(new File(solrHome.getAbsolutePath()+"/data/index"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, config);
        writer.commit();
        writer.close();

        System.setProperty("solr.solr.home", solrHome.getAbsolutePath());

        server = new Server();

        Connector connector=new SelectChannelConnector();
        connector.setPort(Integer.getInteger("jetty.port",port).intValue());
        server.setConnectors(new Connector[]{connector});

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/solr");
        webapp.setWar(solrWar.getAbsolutePath());
        webapp.setTempDirectory(workingDir);
        webapp.setVirtualHosts(new String[]{host});

        server.setHandler(webapp);

        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        if (solrServer != null){
            solrServer.shutdown();
        }
        if (server != null) server.stop();
    }

    public File getSolrHome() {
        return solrHome;
    }

    public String getSolrUrl() {
        return "http://"+host+":"+port+"/solr/";
    }

    public HttpSolrServer getSolrServer() {
        solrServer = new HttpSolrServer(getSolrUrl(), createHttpClient());

        return solrServer;
    }

    protected HttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(128);
        cm.setDefaultMaxPerRoute(24);
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(5000);
        requestBuilder = requestBuilder.setConnectionRequestTimeout(5000);

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(requestBuilder.build());
        builder.setConnectionManager(cm);

        return builder.build();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
