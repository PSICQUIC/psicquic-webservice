/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hupo.psi.mi.psicquic.registry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryConfig;
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryThreadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Checks the state of the services every 5 mins
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class PsicquicRegistryStatusChecker {

    private static final Log log = LogFactory.getLog(PsicquicRegistryStatusChecker.class);

    @Autowired
    private PsicquicRegistryConfig config;

    @Autowired
    PsicquicRegistryThreadConfig threadConfig;

    private Date lastRefreshed = new Date();

    private int threadTimeOut = 10;

    private List<Future> runningTasks;

    public PsicquicRegistryStatusChecker() {
        this.runningTasks = new ArrayList<Future>(40);
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000) // every 5 mins
    public void refreshServices() {

        ExecutorService executorService = threadConfig.getExecutorService();

        runningTasks.clear();

        for (final ServiceType serviceStatus : config.getRegisteredServices()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    checkStatus(serviceStatus);
                }
            };
            runningTasks.add(executorService.submit(runnable));
        }

        checkAndResumeRegistryTasks();

        lastRefreshed = new Date();
    }

    private void checkAndResumeRegistryTasks() {

        for (Future f : runningTasks) {
            try {
                f.get(threadTimeOut, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("The registry task was interrupted, we cancel the task.", e);
                if (!f.isCancelled()) {
                    f.cancel(false);
                }
            } catch (ExecutionException e) {
                log.error("The registry task could not be executed, we cancel the task.", e);
                if (!f.isCancelled()) {
                    f.cancel(false);
                }
            } catch (TimeoutException e) {
                log.error("Service task stopped because of time out " + threadTimeOut + " seconds.");

                if (!f.isCancelled()) {
                    f.cancel(false);
                }
            }catch (Throwable e) {
                log.error("Service task stopped.",e);
                if (!f.isCancelled()) {
                    f.cancel(false);
                }
            }
        }

        runningTasks.clear();
    }

    private void checkStatus(ServiceType serviceStatus) {
        HttpURLConnection urlConnection=null;
        HttpURLConnection urlConnection2=null;
        InputStream contentStream = null;
        InputStream countStream = null;
        try {

            final URL versionUrl = new URL(serviceStatus.getRestUrl() + "version");
            final URL countURL = new URL(serviceStatus.getRestUrl() + "query/*?format=count");

            urlConnection = (HttpURLConnection) versionUrl.openConnection();
            urlConnection.setConnectTimeout(threadTimeOut);
            urlConnection.setReadTimeout(threadTimeOut);

            urlConnection.connect();

            urlConnection2 = (HttpURLConnection) countURL.openConnection();
            urlConnection2.setConnectTimeout(threadTimeOut);
            urlConnection2.setReadTimeout(threadTimeOut);

            urlConnection2.connect();

            int code = urlConnection.getResponseCode();
            int code2 = urlConnection2.getResponseCode();

            if (HttpURLConnection.HTTP_OK == code && HttpURLConnection.HTTP_OK == code2) {

                serviceStatus.setActive(true);

                final String version;
                final String strCount;

                //TODO Add a double check to know if the service is active
                // or not add a catch block for the exceptions

                contentStream = (InputStream) urlConnection.getContent();
                version = IOUtils.toString(contentStream);
                serviceStatus.setVersion(version);

                countStream = (InputStream) urlConnection2.getContent();
                strCount = IOUtils.toString(countStream);
                serviceStatus.setCount(Long.valueOf(strCount));
            } else {
                serviceStatus.setActive(false);
            }

        } catch (Throwable e) {
            serviceStatus.setActive(false);
        }

        if(contentStream!=null){
            try {
                contentStream.close();
            } catch (IOException e) {
                log.error("Cannot close psicquic content stream", e);
            }
        }
        if(countStream!=null){
            try {
                countStream.close();
            } catch (IOException e) {
                log.error("Cannot close psicquic count stream", e);
            }
        }
        if (urlConnection != null){
            urlConnection.disconnect();
        }
        if (urlConnection2 != null){
            urlConnection2.disconnect();
        }
    }

    public Date getLastRefreshed() {
        return lastRefreshed;
    }
}
