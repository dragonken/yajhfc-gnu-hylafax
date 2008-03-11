/*******************************************************************************
 * $Id: ReceiveStatusEvent.java 84 2008-02-21 23:08:12Z sjardine $
 * 
 * Copyright 2008, Steven Jardine <steve@mjnservices.com>
 * Copyright 2008, MJN Services, Inc. - http://www.mjnservices.com
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser Public License v2.1 which 
 * accompanies this distribution, and is available at
 * 	http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the HylaFAX Fax Server please see
 * 	HylaFAX  - http://www.hylafax.org or 
 * 	Hylafax+ - http://hylafax.sourceforge.net
 * 
 * Contributors:
 * 	Steven Jardine - Initial API and implementation
 ******************************************************************************/
package gnu.hylafax.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a RECEIVE event sent by the fax server.
 * 
 * @version $Revision: 84 $
 * @author Steven Jardine <steve@mjnservices.com>
 */
public class ReceiveStatusEvent extends BaseStatusEvent {

    private static final Log log = LogFactory.getLog(ReceiveStatusEvent.class);

    private String bitRate = null;

    private String dataFormat = null;

    private String pageLength = null;

    private Integer pageNumber = null;

    private Integer pageSeconds = null;

    private String scanlineTime = null;

    private String sender = null;

    private Integer totalPages = null;

    private String verticalResolution = null;

    public ReceiveStatusEvent(Event event, String serverStr) {
	super(event, serverStr);
	if (serverStr != null) {
	    String details = null;
	    try {
		details = (serverStr.split("RECV FAX: ")[1]).trim()
			.toUpperCase();
	    } catch (Exception e) {
		log.warn(errorMsg("Cannot Parse Server String", serverStr, e));
	    }
	    if (details != null) {
		// Parse the details.
		if (event == Event.RECV_BEGIN || event == Event.RECV_END) {
		    description = details;
		} else {
		    sender = parseSender(details);
		    commId = parseCommId(details);
		    if (event == Event.RECV_START) {
			setParams(details);
		    } else if (event == Event.RECV_PAGE) {
			// Parse the page number.
			pageNumber = parsePageNumber(details);
			pageSeconds = parsePageSeconds(details);
			setParams(details);
		    } else if (event == Event.RECV_DOC) {
			totalPages = parseTotalPages(details);
			pageSeconds = parsePageSeconds(details);
		    }
		}
	    }
	}
    }

    public String getBitRate() {
	return bitRate;
    }

    public String getCommId() {
	return commId;
    }

    public String getDataFormat() {
	return dataFormat;
    }

    public String getPageLength() {
	return pageLength;
    }

    public Integer getPageNumber() {
	return pageNumber;
    }

    public Integer getPageSeconds() {
	return pageSeconds;
    }

    public String getScanlineTime() {
	return scanlineTime;
    }

    public String getSender() {
	return sender;
    }

    public Integer getTotalPages() {
	return totalPages;
    }

    public String getVerticalResolution() {
	return verticalResolution;
    }

    private Integer parsePageNumber(String details) {
	try {
	    return new Integer(Integer.parseInt(details.substring(details
		    .indexOf("), PAGE ") + 8, details.indexOf(" IN "))));
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Parse Total Pages", details, e));
	}
	return null;
    }

    /**
     * Parses the time to send a page or total time for a document.
     * 
     * @param details
     *                the details of the event.
     * @return the seconds it took to receive the fax.
     * 
     * @since HylaFAX v4.4.4
     * @since HylaFAX+ v5.2.2
     */
    private Integer parsePageSeconds(String details) {
	String[] time = null;
	try {
	    if (event == Event.RECV_PAGE) {
		time = details.substring(details.indexOf(" IN ") + 4,
			details.lastIndexOf(" <")).trim().split(":");
	    } else if (event == Event.RECV_DOC) {
		time = details.substring(details.indexOf(" PAGES IN ") + 10,
			details.indexOf(", FILE")).trim().split(":");
	    }
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Parse Time String", details, e));
	}

	try {
	    if (time != null && (time.length == 3 || time.length == 2)) {
		int seconds = 0;
		int index = 0;
		if (time.length == 3)
		    seconds += Integer.parseInt(time[index++]) * 60 * 60;
		seconds += Integer.parseInt(time[index++].trim()) * 60;
		seconds += Integer.parseInt(time[index++].trim());
		return new Integer(seconds);
	    }
	    throw new Exception("Time String is null or wrong size");
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Calculate Time", details, e));
	}
	return null;
    }

    private String parseSender(String details) {
	String result = null;
	try {
	    if (event == Event.RECV_START) {
		result = details.substring(details.indexOf("TSI \"") + 5,
			details.indexOf("\" <")).trim();
	    } else if (event == Event.RECV_PAGE || event == Event.RECV_DOC) {
		result = details.substring(details.indexOf("FROM ") + 5,
			details.indexOf(" (COM ")).trim();
	    }
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Parse Sender", details, e));
	}
	if (result != null) {
	    result = result.trim();
	    return result.equals("") ? null : result;
	}
	return null;
    }

    private Integer parseTotalPages(String details) {
	try {
	    return new Integer(Integer.parseInt(details.substring(details
		    .indexOf("), ") + 3, details.indexOf(" PAGES IN "))));
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Parse Total Pages", details, e));
	}
	return null;
    }

    private void setParams(String details) {
	String info = null;
	try {
	    info = details.substring(details.lastIndexOf("<") + 1, details
		    .lastIndexOf(">"));
	} catch (Exception e) {
	    log.warn(errorMsg("Cannot Parse Info From Details", details, e));
	    return;
	}

	if (info != null) {
	    try {
		String[] params = info.split(", ");
		pageLength = prepStr(params[0]);
		verticalResolution = prepStr(params[1]);
		dataFormat = prepStr(params[2]);
		bitRate = prepStr(params[3]);
		scanlineTime = prepStr(params[4]);
	    } catch (Exception e) {
		log.warn(errorMsg("Cannot Parse Info", info, e));
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see gnu.hylafax.status.BaseStatusEvent#toString()
     */
    public String toString() {
	String result = super.toString();
	result += "SENDER: " + sender + "; ";
	result += "TOT PAGES: " + totalPages + "; ";
	result += "PAGE: " + pageNumber + "; ";
	result += "PAGE TIME: " + pageSeconds + "; ";
	result += "PAGE LEN: " + pageLength + "; ";
	result += "BIT RATE: " + bitRate + "; ";
	result += "DATA FMT: " + dataFormat + "; ";
	result += "SCANLINE TIME: " + scanlineTime + "; ";
	result += "VERT RES: " + verticalResolution + "; ";
	return result;
    }
}
