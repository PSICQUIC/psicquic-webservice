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
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Checks the state of the services every 5 mins
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class PsicquicRegistryStatusChecker {

    @Autowired
    private PsicquicRegistryConfig config;

    private Date lastRefreshed = new Date();

    @Scheduled(fixedDelay = 5 * 60 * 1000) // every 5 mins
    public void refreshServices() {
        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (final ServiceType serviceStatus : config.getRegisteredServices()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    checkStatus(serviceStatus);
                }
            };
            executorService.submit(runnable);

        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lastRefreshed = new Date();
    }

    private void checkStatus(ServiceType serviceStatus) {
        try {
            final URL versionUrl = new URL(serviceStatus.getRestUrl() + "version");
            final HttpURLConnection urlConnection = (HttpURLConnection) versionUrl.openConnection();
            int code = urlConnection.getResponseCode();

            urlConnection.connect();

            if (HttpURLConnection.HTTP_OK == code) {
                serviceStatus.setActive(true);
                final String version = IOUtils.toString((InputStream) urlConnection.getContent());

                serviceStatus.setVersion(version);

                final URL countURL = new URL(serviceStatus.getRestUrl() + "query/*?format=count");
                final String strCount = IOUtils.toString(countURL.openStream());
                serviceStatus.setCount(Long.valueOf(strCount));
            } else {
                serviceStatus.setActive(false);
            }


        } catch (Throwable e) {
            serviceStatus.setActive(false);
        }

    }

    public Date getLastRefreshed() {
        return lastRefreshed;
    }
}
