/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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

import freemarker.template.Configuration;
import org.apache.commons.io.IOUtils;
import org.hupo.psi.mi.psicquic.expressiontree.ExpressionTree;
import org.hupo.psi.mi.psicquic.expressiontree.ParseExpressionException;
import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryConfig;
import org.hupo.psi.mi.psicquic.registry.util.FreemarkerStreamingOutput;
import org.hupo.psi.mi.psicquic.registry.util.RawTextStreamingOutput;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class PsicquicRegistryServiceImpl implements PsicquicRegistryService{

    private static final String ACTION_STATUS = "STATUS";
    private static final String ACTION_ACTIVE = "ACTIVE";
    private static final String ACTION_INACTIVE = "INACTIVE";

    private static final String YES = "y";
    private static final String NO = "n";

    @Autowired
    private PsicquicRegistryConfig config;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    
    @Autowired
    private SelfDiscoveringOntologyTree miOntologyTree;





    public Object executeAction(String action, String name, String url, String format, String showRestricted, String tags, String excluded) throws IllegalActionException {
        Registry registry;

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags),
                                      new ExclusionFilter(excluded.split(",")));



        } else if (ACTION_ACTIVE.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new ActiveFilter(),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags),
                                      new ExclusionFilter(excluded.split(",")));
        } else if (ACTION_INACTIVE.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new InactiveFilter(),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags),
                                      new ExclusionFilter(excluded.split(",")));
        } else {
            throw new IllegalActionException("Action not defined: "+action);
        }

        if ("xml".equals(format)) {
            return Response.ok(registry, MediaType.APPLICATION_XML_TYPE).build();
        }

        if ("txt".equals(format)) {
            return Response.ok(new RawTextStreamingOutput(registry), MediaType.TEXT_PLAIN_TYPE).build();
        }

        if ("count".equals(format)) {
            return Response.ok(registry.getServices().size(), MediaType.TEXT_PLAIN_TYPE).build();
        }

        final Configuration freemarkerCfg = freeMarkerConfigurer.getConfiguration();

        return Response.ok(new FreemarkerStreamingOutput(registry, miOntologyTree ,freemarkerCfg), MediaType.TEXT_HTML_TYPE).build();
    }

    private Registry createRegistry(final ServiceFilter ... filters) {
       Registry registry = new Registry();

        final List<ServiceType> acceptedServices = Collections.synchronizedList(new ArrayList<ServiceType>(16));

        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (final ServiceType service : config.getRegisteredServices()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    boolean accept = true;

                    for (ServiceFilter filter : filters) {
                        if (!filter.accept(service)) {
                            accept = false;
                        }
                    }

                    if (accept) {
                        acceptedServices.add(service);
                    }

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

        registry.getServices().addAll(acceptedServices);

        return registry;
    }

    private boolean stringToBoolean(String str) {
        if (YES.equalsIgnoreCase(str)) {
            return true;
        }

        return false;
    }

    public String getVersion() {
        return config.getVersion();
    }

    private class NameFilter implements ServiceFilter {

        private String name;

        private NameFilter(String name) {
            this.name = name;
        }

        public boolean accept(ServiceType service) {
            return name == null || name.equalsIgnoreCase(service.getName());
        }
    }

    private class ActiveFilter implements ServiceFilter {

        public boolean accept(ServiceType service) {
            return service.isActive();
        }
    }

    private class InactiveFilter implements ServiceFilter {

        public boolean accept(ServiceType service) {
            return !(service.isActive());
        }
    }

    private class RestrictedFilter implements ServiceFilter {

        private boolean restricted = false;

        public RestrictedFilter(boolean restricted) {
            this.restricted = restricted;
        }

        public boolean accept(ServiceType service) {
            if (service.isRestricted() && !restricted) {
                return false;
            }
            return true;
        }

    }
    
    private class TagsFilter implements ServiceFilter {
        
        private String expression;
        

        public TagsFilter(String expression) {
            this.expression = expression;
		}

		public boolean accept(ServiceType service) {
			boolean accept;
			
			if(expression != null && expression.trim().length() > 0) {
				try {
                    ExpressionTree exTree = new ExpressionTree(expression, miOntologyTree,false);
					accept = exTree.evaluate(service);
				} catch (ParseExpressionException e) {
					throw new IllegalArgumentException("Cannot parse expression: "+expression, e);
				}
			} else {
                accept = true;
            }
			
			return accept;
		}
    }

    private class ExclusionFilter implements ServiceFilter {

        private String[] excludedUrls;

        private ExclusionFilter(String[] excludedUrls) {
            this.excludedUrls = excludedUrls;
        }

        public boolean accept(ServiceType service) {
            for (String excludedUrl : excludedUrls) {
                if (service.getSoapUrl() != null) {
                    if (excludedUrl.trim().equals(service.getSoapUrl())) {
                        return false;
                    }
                } else if (service.getRestUrl() != null) {
                    if (excludedUrl.trim().equals(service.getRestUrl())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
