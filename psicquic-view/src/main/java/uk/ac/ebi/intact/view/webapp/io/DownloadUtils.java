package uk.ac.ebi.intact.view.webapp.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 18/09/2012
 * Time: 10:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class DownloadUtils {

	public abstract String getExtension(String format);

	public abstract String getContentType(String format);

	public abstract String getFileName(String query);

	public String getDateTime() {
		DateFormat df = new SimpleDateFormat("_ddMMyyyy_hhmm");
		return df.format(new Date());
	}

	public String truncateFileName(String fileName, int length) {
		if (fileName != null) {
			if (fileName.length() > length) {
				fileName = fileName.substring(0, length);
			}
		}
		return fileName;
	}
}
