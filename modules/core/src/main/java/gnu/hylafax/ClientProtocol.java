//$Id: ClientProtocol.java,v 1.2 2006/02/20 04:52:11 sjardine Exp $
//
//Copyright 2005 Steven Jardine <steve@mjnservices.com>
//Copyright 2005 MJN Services, Inc - http://www.mjnservices.com
//
//for information on the HylaFAX FAX server see
//http://www.hylafax.org/
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Library General Public
//License as published by the Free Software Foundation; either
//version 2 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Library General Public License for more details.
//
//You should have received a copy of the GNU Library General Public
//License along with this library; if not, write to the Free
//Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//

package gnu.hylafax;

import gnu.inet.ftp.ServerResponseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:steve@mjnservices.com">Steven Jardine </a>
 */
public interface ClientProtocol {

    /**
     * Notify on all job state changes. Used with the JPARM NOTIFY command.
     */
    public static final String NOTIFY_ALL = "DONE+REQUEUE";

    /**
     * Notify when the job is done. Used with the JPARM NOTIFY command.
     */
    public static final String NOTIFY_DONE = "DONE";

    /**
     * Do not notify when the job is done or requeued. Used with the JPARM
     * NOTIFY command.
     */
    public static final String NOTIFY_NONE = "NONE";

    /**
     * Notify when the job is requeued. Used with the JPARM NOTIFY command.
     */
    public static final String NOTIFY_REQUEUE = "REQUEUE";

    /**
     * use the GMT timezone for date fields.
     */
    public static final String TZONE_GMT = "GMT";

    /**
     * use the local timezone for date fields.
     */
    public static final String TZONE_LOCAL = "LOCAL";

    /**
     * establish administrator privileges given password
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param password
     *            administrator password
     */
    public void admin(String password) throws IOException,
            ServerResponseException;

    /**
     * get the FILEFMT string value. The FILEFMT string specifies how file
     * status information is formatted when returned by the LIST and STAT
     * commands. Refer to the HylaFAX man pages, hfaxd(8c), for information on
     * the formatting codes that can be used in this string.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @return the current FILEFMT value
     */
    public String filefmt() throws IOException, ServerResponseException;

    /**
     * set the FILEFMT string value. the FILEFMT string specifies how file
     * status information is returned when the LIST and STAT commands are used.
     * Refer to the HylaFAX man pages, hfaxd(8c), for the formatting codes.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @param value
     *            the new value of the FILEFMT string
     */
    public void filefmt(String value) throws IOException,
            ServerResponseException;

    /**
     * get the current idle timeout in seconds
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @return server's idle timeout in seconds
     */
    public long idle() throws IOException, ServerResponseException;

    /**
     * set the idle timeout value to the given number of seconds
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param timeout
     *            new timeout value in seconds (MAX = 7200)
     */
    public void idle(long timeout) throws IOException, ServerResponseException;

    /**
     * delete the given job this can be called on a suspended or done job.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                server replied with error code
     * @param jobid
     *            id of the job to delete
     */
    public void jdele(long jobid) throws IOException, ServerResponseException;

    /**
     * interrupt the given job id
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @param jobid
     *            id of the job to interrupt
     */
    public void jintr(long jobid) throws IOException, ServerResponseException;

    /**
     * kill the job with the given job id
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @param jobid
     *            the id of the job to kill
     */
    public void jkill(long jobid) throws IOException, ServerResponseException;

    /**
     * create a new job. get the new job id using the job() method. The new job
     * is the current job.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     */
    public void jnew() throws IOException, ServerResponseException;

    /**
     * get the current job id 0 indicates the current job id is "default" value
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     */
    public long job() throws IOException, ServerResponseException;

    /**
     * set the current job id
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param value
     *            new current job id
     */
    public void job(long value) throws IOException, ServerResponseException;

    /**
     * get the job format string. read the HylaFAX man pages, hfaxd(8c), for
     * format codes.
     * 
     * @exception IOException
     *                a socket IO error occurred.
     * @exception ServerResponseException
     *                the server responded with an error code
     */
    public String jobfmt() throws IOException, ServerResponseException;

    /**
     * set the job format string. read the HylaFAX man pages, hfaxd(8c), for
     * format codes.
     * 
     * @exception IOException
     *                a socket IO error occurred.
     * @exception ServerResponseException
     *                the server responded with an error
     * @param value
     *            new job format string
     */
    public void jobfmt(String value) throws IOException,
            ServerResponseException;

    /**
     * get job parameters of the current job
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param parm
     *            the name of the job parameter to change
     * @return value of the named job parameter
     */
    public String jparm(String parm) throws IOException,
            ServerResponseException;

    /**
     * set job parameters on the current job
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param parm
     *            the name of the job parameter to change
     * @param value
     *            the value of the given parameter
     */
    public void jparm(String parm, int value) throws IOException,
            ServerResponseException;

    /**
     * set job parameters on the current job
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param parm
     *            the name of the job parameter to change
     * @param value
     *            the value of the given parameter
     */
    public void jparm(String parm, long value) throws IOException,
            ServerResponseException;

    /**
     * set job parameters on the current job
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param parm
     *            the name of the job parameter to change
     * @param value
     *            the value of the given parameter as an Object
     */
    public void jparm(String parm, Object value) throws IOException,
            ServerResponseException;

    /**
     * set job parameters on the current job
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param parm
     *            the name of the job parameter to change
     * @param value
     *            the value of the given parameter
     */
    public void jparm(String parm, String value) throws IOException,
            ServerResponseException;

    /**
     * reset the state of the current job. get/set the current job id via the
     * 'job' method
     * 
     * @exception IOException
     *                an IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     */
    public void jrest() throws IOException, ServerResponseException;

    /**
     * submit the current job to the scheduler
     * 
     * @return the job id
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     */
    public long jsubm() throws IOException, ServerResponseException;

    /**
     * submit the given job to the scheduler
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param jobid
     *            the id of the job to submit
     * @return the submitted job id, should match jobid passed in
     */
    public int jsubm(long jobid) throws IOException, ServerResponseException;

    /**
     * Suspend the job with the given job id.
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param jobid
     *            id of the job to suspend
     */
    public void jsusp(long jobid) throws IOException, ServerResponseException;

    /**
     * Wait for the job with the given job id to complete.
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param jobid
     *            id of the job to wait for
     */
    public void jwait(long jobid) throws IOException, ServerResponseException;

    /**
     * get the modem format string value. the modem format string specifies how
     * modem status information should be displayed. refer to the HylaFAX man
     * pages, hfaxd(8c), for the format string codes.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server responded with an error code
     */
    public String mdmfmt() throws IOException, ServerResponseException;

    /**
     * set the modem format string. the modem format string is used to format
     * the modem status information. Refer to the HylaFAX man pages, hfaxd(8c),
     * for formatting codes.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server responded with an error code
     * @param value
     *            the new modem format string to use
     */
    public void mdmfmt(String value) throws IOException,
            ServerResponseException;

    /**
     * perform server No Operation could be used as a keep-alive
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     */
    public void noop() throws IOException, ServerResponseException;

    /**
     * open a connection to the localhost on the default port
     * 
     * @exception UnknownHostException
     *                cannot resolve the given hostname
     * @exception IOException
     *                IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     */
    public void open() throws UnknownHostException, IOException,
            ServerResponseException;

    /**
     * open a connection to a given server at default port this is an alias for
     * connect()
     * 
     * @exception UnknownHostException
     *                cannot resolve the given hostname
     * @exception IOException
     *                IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @param host
     *            the hostname of the HylaFAX server
     */
    public void open(String host) throws UnknownHostException, IOException,
            ServerResponseException;

    /**
     * send the password for this username and session
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param password
     *            the password to login with
     */
    public void pass(String password) throws IOException,
            ServerResponseException;

    /**
     * end session
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     */
    public void quit() throws IOException, ServerResponseException;

    /**
     * get the received file output format string. The rcvfmt string specifies
     * how received faxes (files in the rcvq directory) are displayed. Refer to
     * the HylaFAX man pages, hfaxd(8c), for the format string codes.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server responded with an error code
     */
    public String rcvfmt() throws IOException, ServerResponseException;

    /**
     * set the receive file output format string. The rcvfmt string specifies
     * how received faxes (files in the rcvq directory) are displayed. refer to
     * the HylaFAX man pages, hfaxd(8c), for the format string codes.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server responded with an error code
     * @param value
     *            the new format string
     */
    public void rcvfmt(String value) throws IOException,
            ServerResponseException;

    /**
     * Returns the size (in bytes) of the given regular file. This is the size
     * on the server and may not accurately represent the file size once the
     * file has been transferred (particularly via ASCII mode)
     * 
     * @exception IOException
     *                caused by a socket IO error
     * @exception ServerResponseException
     *                caused by a server response indicating an error
     * @exception FileNotFoundException
     *                the given path does not exist
     * @param pathname
     *            the name of the file to get the size for
     */
    public long size(String pathname) throws IOException,
            FileNotFoundException, ServerResponseException;

    /**
     * store temp file, the file is stored in a uniquely named file on the
     * server. The remote temp file is deleted when the connection is closed.
     * 
     * @exception IOException
     *                io error occurred talking to the server
     * @exception ServerResponseException
     *                server replied with error code
     * @return the filename of the temp file
     */
    public String stot(InputStream data) throws IOException,
            ServerResponseException;

    /**
     * set the timezone display format valid tzone values are TZONE_GMT and
     * TZONE_LOCAL
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param value
     *            new timezone display setting
     */
    public void tzone(String value) throws IOException, ServerResponseException;

    /**
     * send the user name for this session
     * 
     * @exception IOException
     *                io error occurred
     * @exception ServerResponseException
     *                server replied with an error code
     * @param username
     *            name of the user to login as
     * @return true if a password is required, false if no password is required
     */
    public boolean user(String username) throws IOException,
            ServerResponseException;

    /**
     * verify dialstring handling and/or least-cost routing.
     * 
     * @exception IOException
     *                a socket IO error occurred
     * @exception ServerResponseException
     *                the server replied with an error code
     * @return the InetAddress of the server that will handle the given
     *         dialstring
     * @param dialstring
     *            the dialstring to verify
     */
    public InetAddress vrfy(String dialstring) throws IOException,
            ServerResponseException;

}