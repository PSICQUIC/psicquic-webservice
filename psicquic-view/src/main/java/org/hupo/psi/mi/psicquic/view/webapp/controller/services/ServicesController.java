package org.hupo.psi.mi.psicquic.view.webapp.controller.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.view.webapp.application.PsicquicThreadConfig;
import org.hupo.psi.mi.psicquic.view.webapp.controller.BaseController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.QueryHits;
import org.hupo.psi.mi.psicquic.view.webapp.controller.search.UserQuery;
import org.hupo.psi.mi.psicquic.view.webapp.model.PsicquicResultDataModel;
import org.hupo.psi.mi.psicquic.view.webapp.util.ServiceByNameComparator;
import org.hupo.psi.mi.psicquic.view.webapp.util.UrlUtils;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 30/01/2013
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
@Controller("servicesTable")
@Scope("conversation.access")
@ConversationName("general")
public class ServicesController extends BaseController implements java.io.Serializable {

	private static final long serialVersionUID = 6955236745608138415L;
	private static final Log log = LogFactory.getLog(ServicesController.class);
	private static final int UNEXPECTED_ERROR = -2;
	private static final int TIME_OUT_EXCEPTION = -1;

	@Autowired
	private transient ApplicationContext applicationContext;

    @Autowired
    private UserQuery userQuery;


    private List<QueryHits> services = null;
    private int activeServiceCount = 0;

    private int selectedResults = 0;
	private int totalResults = 0;

    private List<Future<?>> runningTasks;
    private QueryHits selectedService;
    private String mitabUrl;


    public ServicesController() {

	}

	public void initialize(List<ServiceType> serviceTypeArrayList) {

		services = new ArrayList<QueryHits>();
		activeServiceCount = 0;

		for (int i = 0; i < serviceTypeArrayList.size(); i++) {
			QueryHits service = new QueryHits(serviceTypeArrayList.get(i), 0, serviceTypeArrayList.get(i).isActive());

			if (service.isActive()) {

				PsicquicSimpleClient psicquicSimpleClient;
				psicquicSimpleClient = new PsicquicSimpleClient(service.getRestUrl());
				psicquicSimpleClient.setReadTimeout(5000);

				activeServiceCount++;

				try {
					List<String> formats = psicquicSimpleClient.getFormats();
					service.setFormats(formats);
				} catch (IOException e) {
					//TODO Add a different color for this services and provide a message
					log.error(service.getName() + ": The formats can not be retrieved." + e.getMessage());
					service.setActive(false);
					service.setChecked(false);
					activeServiceCount--;
				}
			}
			services.add(i, service);

		}
		Collections.sort(services, new ServiceByNameComparator());
	}

	public void reset() {
		for (QueryHits service : services) {
			service.setHits(0);
		}
	}

    public void doPsicquicBinarySearch(String searchQuery) {

        if (searchQuery == null) {
            log.warn("The query is null");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Query:  " + searchQuery);
        }

        // A minimum checking of the query
        if (searchQuery.length() > 1 && (searchQuery.trim().startsWith("*") || searchQuery.startsWith("?"))) {
            userQuery.setSearchQuery("*:*");
            final String msg = "Your query '" + searchQuery + "' is not correctly formatted";
            final String detail = "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                    "However, wildcard characters can be used anywhere else in a query (eg. g?vin or gav* for gavin). " +
                    "Please do reformat your query.";
            addErrorMessage(msg, detail);
            log.error(msg);
        } else {
            if (userQuery.isIncludeNegativeQuery()) {
                final String msg = "Your results may contain negative interactions.";
                final String detail = "These results will appear in red rows and they will be excluded from the the " +
                        "cluster algorithm and the graph. They can only be exported in MITAB 2.6, MITAB 2.7 and XML 2.5";
                addInfoMessage(msg, detail);
                log.info(msg);
            }

            reset();
            searchAndCreateResultModels();
            setSelectedService(null);
        }
    }


	public void searchAndCreateResultModels() {

		final String filteredSearchQuery = userQuery.getFilteredSearchQuery();

		// count the results
		PsicquicThreadConfig threadConfig = (PsicquicThreadConfig) applicationContext.getBean("psicquicThreadConfig");

		ExecutorService executorService = threadConfig.getExecutorService();

		if (runningTasks == null) {
			runningTasks = new ArrayList<Future<?>>();
		} else {
			runningTasks.clear();
		}

		for (final QueryHits service : services) {
			if (service.isActive() && service.isChecked()) {
				Runnable runnable = new Runnable() {
					public void run() {
						int count = countInPsicquicService(service, filteredSearchQuery);
						service.setHits(count);
						if (count < 0) {
							service.setChecked(false);
						}
					}
				};
				runningTasks.add(executorService.submit(runnable));
			}
		}

		checkAndResumePsicquicTasks();
	}

    public void loadTableResults() {
        QueryHits service = getSelectedService();
        generateMitabUrl(service);
        SortableModel results = null;
        try {
            PsicquicSimpleClient psicquicSimpleClient;
            psicquicSimpleClient = new PsicquicSimpleClient(service.getRestUrl());

            results = new PsicquicResultDataModel(
                    psicquicSimpleClient,
                    userQuery.getFilteredSearchQuery(),
                    getLatestMitabVersion(service.getFormats()));

            service.setResults(results);
        } catch (IOException e) {
            log.error("Error while building results", e);
        }

    }

    //TODO Improve the way of obtaing the latest version, it is not too flexible.
    private String getLatestMitabVersion(List<String> formats) {

        //By default the version is tab25
        String sVersion = PsicquicSimpleClient.MITAB25;
        int version = 25;

        for (String format : formats) {
            if (format.contentEquals(PsicquicSimpleClient.MITAB26) && (26 > version)) {
                sVersion = PsicquicSimpleClient.MITAB26;
                version = 26;
            } else if (format.contentEquals(PsicquicSimpleClient.MITAB27) && (27 > version)) {
                sVersion = PsicquicSimpleClient.MITAB27;
                version = 27;
            }
        }

        return sVersion;
    }


    private void generateMitabUrl(QueryHits service) {
        final String query = UrlUtils.encodeUrl(userQuery.getFilteredSearchQuery());
        final String restUrl = service.getRestUrl();
        final String queryUrl;


            boolean endWithSlash = restUrl.endsWith("/");
            queryUrl = restUrl + (endWithSlash ? "" : "/") + "query/" + query;


        if (log.isDebugEnabled()) log.debug("Reading data from: " + queryUrl);


        mitabUrl = queryUrl;
    }

	private int countInPsicquicService(ServiceType service, String query) {

		int psicquicCount = 0;

		PsicquicSimpleClient psicquicSimpleClient;

		psicquicSimpleClient = new PsicquicSimpleClient(service.getRestUrl());
		psicquicSimpleClient.setReadTimeout(5000);


		try {
			psicquicCount = Long.valueOf(psicquicSimpleClient.countByQuery(query)).intValue();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				psicquicCount = TIME_OUT_EXCEPTION;
			} else {
				psicquicCount = UNEXPECTED_ERROR;
			}
			//TODO Check if it is a 2.7 query and explain
			log.warn("Error while building results based on the query: '"
					+ query + "'" + " SERVICE (" + service.getName() + ") RESPONSE: " + e.getMessage());

		}

		return psicquicCount;

	}

	private void checkAndResumePsicquicTasks() {

		for (Future f : runningTasks) {
            int threadTimeOut = 5;
            try {
				f.get(threadTimeOut, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error("The psicquic task was interrupted, we cancel the task.", e);
				if (!f.isCancelled()) {
					f.cancel(true);
				}
			} catch (ExecutionException e) {
				log.error("The psicquic task could not be executed, we cancel the task.", e);
				if (!f.isCancelled()) {
					f.cancel(true);
				}
			} catch (TimeoutException e) {
				log.error("Service task stopped because of time out " + threadTimeOut + "seconds.", e);

				if (!f.isCancelled()) {
					f.cancel(true);
				}
			}
		}

		runningTasks.clear();
	}

	public List<QueryHits> getServices() {
		return services;
	}

	public void setServices(ArrayList<QueryHits> services) {
		this.services = services;
	}

	public int getSelectedServicesCount() {
		int count = 0;

		for (QueryHits service : services) {
			if (service.isChecked()) count++;
		}
		return count;
	}

	//TODO think in to move it to a postconstructor initialization
	public int getSelectedResults() {
		selectedResults = 0;

		for (QueryHits service : services) {
			if (service != null) {
				if (service.isChecked() && service.getHits() >= 0) {
					selectedResults += service.getHits();
				}
			}
		}
		return selectedResults;
	}

	public void setSelectedResults(int selectedResults) {
		this.selectedResults = selectedResults;
	}

	public int getTotalResults() {

		totalResults = 0;

		for (QueryHits service : services) {
			if (service != null) {
				if (service.isActive() && service.getHits() >= 0) {
					totalResults += service.getHits();
				}
			}
		}

		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public int getActiveServicesCount() {
		return activeServiceCount;
	}

	public void setActiveServicesCount(int activeServiceCount) {
		this.activeServiceCount = activeServiceCount;
	}

    public void setSelectedService(QueryHits selectedService) {
        this.selectedService = selectedService;
    }

    public QueryHits getSelectedService() {
        return selectedService;
    }

    public String getMitabUrl() {
        return mitabUrl;
    }
}
