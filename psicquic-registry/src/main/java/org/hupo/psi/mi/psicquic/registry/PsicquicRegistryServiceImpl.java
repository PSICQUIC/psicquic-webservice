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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import freemarker.template.Configuration;

import org.apache.commons.lang.StringUtils;
import org.hupo.psi.mi.psicquic.expressionTree.ExpressionTree;
import org.hupo.psi.mi.psicquic.expressionTree.ParseExpressionException;
import org.hupo.psi.mi.psicquic.ols.client.OLSClient;
import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.ols.soap.OntologyQuerySoapBindingStub;
import org.hupo.psi.mi.psicquic.registry.config.PsicquicRegistryConfig;
import org.hupo.psi.mi.psicquic.registry.util.FreemarkerStreamingOutput;
import org.hupo.psi.mi.psicquic.registry.util.RawTextStreamingOutput;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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


    public Object executeAction(String action, String name, String url, String format, String showRestricted, String tags) throws IllegalActionException {
        Registry registry;

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags));

            for (ServiceType serviceStatus : registry.getServices()) {
                checkStatus(serviceStatus);
            }
        } else if (ACTION_ACTIVE.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new ActiveFilter(),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags));
        } else if (ACTION_INACTIVE.equalsIgnoreCase(action)) {
            registry = createRegistry(new NameFilter(name),
                                      new InactiveFilter(),
                                      new RestrictedFilter(stringToBoolean(showRestricted)),
            						  new TagsFilter(tags));
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

        //return Response.ok(new FreemarkerStreamingOutput(registry,freemarkerCfg), MediaType.TEXT_HTML_TYPE).build();
        
        
//        for (ServiceType s : registry.getServices()){
//        	System.out.println(s.getName()+" : RestURL="+s.getRestUrl()+" RestEXAMPLE="+s.getRestExample());
//        }
//        
        return Response.ok(new FreemarkerStreamingOutput(registry, miOntologyTree ,freemarkerCfg), MediaType.TEXT_HTML_TYPE).build();
    }

    private Registry createRegistry(ServiceFilter ... filters) {
       Registry registry = new Registry();

       for (ServiceType service : config.getRegisteredServices()) {
            boolean accept = true;

            for (ServiceFilter filter : filters) {
                if (!filter.accept(service)) {
                    accept = false;
                }
            }

            if (accept) {
                registry.getServices().add(service);
            }
        }

        return registry;
    }

    private void checkStatus(ServiceType serviceStatus) {
        String version = null;

        try {
            final UniversalPsicquicClient client = new UniversalPsicquicClient(serviceStatus.getSoapUrl(), config.getServiceCheckTimeout());
            version = client.getService().getVersion();

            final SearchResult<BinaryInteraction> result = client.getByQuery("*:*", 0, 0);
            serviceStatus.setCount(result.getTotalCount());
        } catch (Throwable e) {
            serviceStatus.setActive(false);
            return;
        } finally {
            serviceStatus.setVersion(version);
        }

        serviceStatus.setActive(true);

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
            checkStatus(service);
            return service.isActive();
        }
    }

    private class InactiveFilter implements ServiceFilter {

        public boolean accept(ServiceType service) {
            checkStatus(service);
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
        
        private ExpressionTree exTree;
        

        public TagsFilter(String expression) {
        	try {
        		
				exTree = new ExpressionTree(expression, miOntologyTree,false);
			} catch (ParseExpressionException e) {
				exTree = null;
			}
		}
    



		@Override
		public boolean accept(ServiceType service) {
			boolean result = false;
			
			if(exTree == null){
				result = false;
			}else {
				try {
					if(miOntologyTree != null){
						System.out.println("PSICQUICREGISTRYSERVICEIMPL: miOntologyTree is NOT null");
					} else {
						System.out.println("PSICQUICREGISTRYSERVICEIMPL: miOntologyTree IS null");

					}
					
					result = exTree.evaluate(service);
				} catch (ParseExpressionException e) {
					result = false;
				}
			}
			
			return result;

		}
        
    }


}
