package uk.ac.ebi.intact.view.webapp.io;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import java.util.HashMap;
import java.util.Map;

public class FileDownloadUtils extends DownloadUtils {

	private static Map<String, String> contentTypeByFormat;
	private static HashMap<String, String> extensionByFormat;
	private static final String TEXT_PLAIN = "text/plain";
	private static final String TEXT_XML = "text/xml";
	private static final String TEXT_HTML = "text/html";
	private static final String APPLICATION_RDF_XML = "application/rdf+xml";
	private static final String APPLICATION_X_GZIP = "application/x-gzip";
	private static final String APPLICATION_XGMML = "application/xgmml";
	private static final String TXT = "txt";
	private static final String XML = "xml";
	private static final String HTML = "html";
	private static final String RDF = "rdf";
	private static final String GZIP = "gzip";
	private static final String XGMML = "xgmml";

	private static final int FILE_NAME_LENGHT = 20;

	public FileDownloadUtils() {
	}

	@Override
	public String getExtension(String format) {
		if (extensionByFormat == null) {
			initializeExtensionByFormat();
		}

		return extensionByFormat.get(format);

	}

	@Override
	public String getContentType(String format) {
		if (contentTypeByFormat == null) {
			initializeContentTypeByFormat();
		}

		return contentTypeByFormat.get(format);
	}

	@Override
	public String getFileName(String query) {
		return "query_" + truncateFileName(query, FILE_NAME_LENGHT) + getDateTime();
	}

	static void initializeContentTypeByFormat() {

		contentTypeByFormat = new HashMap<String, String>();

		contentTypeByFormat.put(PsicquicSimpleClient.XML25, TEXT_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB25, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB25_COMPRESSED, APPLICATION_X_GZIP);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB26, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB26_COMPRESSED, APPLICATION_X_GZIP);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB27, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSimpleClient.MITAB27_COMPRESSED, APPLICATION_X_GZIP);
		contentTypeByFormat.put(PsicquicSimpleClient.XGMML, APPLICATION_XGMML);
		contentTypeByFormat.put(PsicquicSimpleClient.BIOPAX, TEXT_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.BIOPAX_L2, TEXT_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.BIOPAX_L3, TEXT_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.RDF_XML, APPLICATION_RDF_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.RDF_XML_ABBREV, APPLICATION_RDF_XML);
		contentTypeByFormat.put(PsicquicSimpleClient.RDF_N3, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSimpleClient.RDF_TURTLE, TEXT_PLAIN);
		contentTypeByFormat.put(PsicquicSimpleClient.COUNT, TEXT_PLAIN);

	}

	static void initializeExtensionByFormat() {

		extensionByFormat = new HashMap<String, String>();

		extensionByFormat.put(PsicquicSimpleClient.XML25, XML);
		extensionByFormat.put(PsicquicSimpleClient.MITAB25, TXT);
		extensionByFormat.put(PsicquicSimpleClient.MITAB25_COMPRESSED, GZIP);
		extensionByFormat.put(PsicquicSimpleClient.MITAB26, TXT);
		extensionByFormat.put(PsicquicSimpleClient.MITAB26_COMPRESSED, GZIP);
		extensionByFormat.put(PsicquicSimpleClient.MITAB27, TXT);
		extensionByFormat.put(PsicquicSimpleClient.MITAB27_COMPRESSED, GZIP);
		extensionByFormat.put(PsicquicSimpleClient.XGMML, XGMML);
		extensionByFormat.put(PsicquicSimpleClient.BIOPAX, XML);
		extensionByFormat.put(PsicquicSimpleClient.BIOPAX_L2, XML);
		extensionByFormat.put(PsicquicSimpleClient.BIOPAX_L3, XML);
		extensionByFormat.put(PsicquicSimpleClient.RDF_XML, RDF);
		extensionByFormat.put(PsicquicSimpleClient.RDF_XML_ABBREV, RDF);
		extensionByFormat.put(PsicquicSimpleClient.RDF_N3, TXT);
		extensionByFormat.put(PsicquicSimpleClient.RDF_TURTLE, TXT);
		extensionByFormat.put(PsicquicSimpleClient.COUNT, TXT);

	}
}
