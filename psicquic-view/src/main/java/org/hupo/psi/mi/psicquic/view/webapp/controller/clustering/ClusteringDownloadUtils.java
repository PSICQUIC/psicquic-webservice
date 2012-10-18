package org.hupo.psi.mi.psicquic.view.webapp.controller.clustering;

import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import org.hupo.psi.mi.psicquic.view.webapp.io.DownloadUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 18/09/2012
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public class ClusteringDownloadUtils extends DownloadUtils {

	private static Map<String, String> contentTypeByFormat;
	private static HashMap<String, String> extensionByFormat;

	private static final String TEXT_PLAIN = "text/plain";
	private static final String TEXT_XML = "text/xml";
	private static final String TXT = "txt";
	private static final String XML = "xml";

	private static final int FILE_NAME_LENGHT = 20;


	public ClusteringDownloadUtils() {
	}

	public String getExtension(String format) {
		if (extensionByFormat == null) {
			initializeExtensionByFormat();
		}

		return extensionByFormat.get(format);

	}

	public String getContentType(String format) {
		if (contentTypeByFormat == null) {
			initializeContentTypeByFormat();
		}

		return contentTypeByFormat.get(format);
	}

	public String getFileName(String query) {
		return "clusteredQuery_" + truncateFileName(query, FILE_NAME_LENGHT) + getDateTime();
	}

	static void initializeContentTypeByFormat() {

		contentTypeByFormat = new HashMap<String, String>();

		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_XML25, TEXT_XML);
		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB25, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB26, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB27, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_COUNT, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSolrServer.RETURN_TYPE_DEFAULT, TEXT_PLAIN);

	}

	static void initializeExtensionByFormat() {

		extensionByFormat = new HashMap<String, String>();

		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_XML25, XML);
		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB25, TXT);
		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB26, TXT);
		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_MITAB27, TXT);
		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_COUNT, TXT);
		extensionByFormat.put(PsicquicSolrServer.RETURN_TYPE_DEFAULT, TXT);

	}
}
