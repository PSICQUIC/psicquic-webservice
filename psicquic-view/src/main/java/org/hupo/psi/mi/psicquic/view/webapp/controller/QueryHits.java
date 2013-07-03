package org.hupo.psi.mi.psicquic.view.webapp.controller;

import org.apache.myfaces.trinidad.model.SortableModel;
import org.hupo.psi.mi.psicquic.registry.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 31/01/2013
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class QueryHits extends ServiceType {

	private Integer hits ;
	private Boolean  checked = true;
	private List<String> formats = new ArrayList<String>();
	private SortableModel results;

	public QueryHits(ServiceType serviceType, Integer hits, Boolean checked) {
		this.name  = serviceType.getName();
		this.active = serviceType.isActive();
		this.comments = serviceType.getComments();
		this.count = serviceType.getCount();
		this.restUrl = serviceType.getRestUrl();
		this.organizationUrl = serviceType.getOrganizationUrl();
		this.soapUrl = serviceType.getSoapUrl();
		this.restExample = serviceType.getRestExample();
		this.version = serviceType.getVersion();
		this.restricted = serviceType.isRestricted();
		this.tags = serviceType.getTags();
		this.hits = hits;
		this.checked = checked;
	}

	public QueryHits(ServiceType serviceType, Integer hits, Boolean checked, List<String> formats) {
		this(serviceType, hits, checked);
		this.formats = formats;
	}
	public Integer getHits() {
		return hits;
	}

	public void setHits(Integer hits) {
		this.hits = hits;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public List<String> getFormats() {
		return formats;
	}

	public void setFormats(List<String> formats) {
		this.formats = formats;
	}

	public SortableModel getResults() {
		return results;
	}

	public void setResults(SortableModel results) {
		this.results = results;
	}

}
