// Getter.java
// $Id$
//
// Copyright 2000, Joe Phillips <jaiger@innovationsw.com>
// Copyright 2001, 2002 Innovation Software Group, LLC - http://www.innovationsw.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Library General Public License for more details.
//
// You should have received a copy of the GNU Library General Public
// License along with this library; if not, write to the Free
// Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
package gnu.inet.ftp;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class serves as a superclass to the ActiveGetter and PassiveGetter
 * classes, providing a common interface and data members.
 * <P>
 * This class is used internally to the FtpClient class.
 */
public class Getter extends Thread implements ConnectionEventSource,
		TransferEventSource {

	public static final int BUFFER_SIZE = 1024;

	protected OutputStream ostream;

	protected boolean cancelled = false;

	protected Vector connectionListeners;

	protected Vector transferListeners;

	protected char mode;

	protected char type;

	/**
	 * set the Getter initial state with debugging disabled.
	 */
	public Getter() {
		super();
		this.cancelled = false;
		this.connectionListeners = new Vector();
		this.transferListeners = new Vector();
		this.mode = FtpClientProtocol.MODE_STREAM;
		this.type = FtpClientProtocol.TYPE_IMAGE;
	}

	//
	// public methods
	//

	/**
	 * set the OutputStream to be used for data storage
	 * 
	 * @param ostream
	 *            the OutputStream to write data to
	 */
	public synchronized void setOutputStream(OutputStream ostream) {
		this.ostream = ostream;
	}

	/**
	 * set the mode value
	 * 
	 * @param mode
	 *            the new mode value. Valid values (MODE_*) can be found in the
	 *            FtpClientProtocol class.
	 * @see FtpClientProtocol
	 */
	public synchronized void setMode(char mode) {
		this.mode = mode;
	}

	/**
	 * set the type value
	 * 
	 * @param type
	 *            the new type value. Valid values (TYPE_*) can be found in the
	 *            FtpClientProtocol class.
	 * @see FtpClientProtocol
	 */
	public synchronized void setType(char type) {
		this.type = type;
	}

	/**
	 * cancel a running transfer sets a flag and calls interrupt() can only be
	 * called once
	 */
	public void cancel() {
		if (!cancelled) {
			cancelled = true;
			interrupt();
		}
	}

	public synchronized void start() {
		this.cancelled = false; // Reset cancelled flag here and not in run!
		super.start();
	}

	/**
	 * add a ConnectionListener to the list of connectionListeners
	 * 
	 * @param listener
	 *            the ConnectionListener to add to the list
	 */
	public void addConnectionListener(ConnectionListener listener) {
		connectionListeners.addElement(listener);
	}

	/**
	 * add a set of ConnectionListener to the list of connectionListeners
	 * 
	 * @param listeners
	 *            the ConnectionListeners to add to the list
	 */
	public void addConnectionListeners(Vector listeners) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			ConnectionListener listener = (ConnectionListener) e.nextElement();
			connectionListeners.addElement(listener);
		}
	}

	/**
	 * De-register a ConnectionListener from the event source. The listener
	 * should not receive any more events.
	 * 
	 * @param listener
	 *            the ConnectionListener to de-register
	 */
	public void removeConnectionListener(ConnectionListener listener) {
		connectionListeners.removeElement(listener);
	}

	/**
	 * add a TransferListener to the list of transfer listeners. All
	 * TransferListeners registered with this Getter will be notified when a
	 * transfer event occurs.
	 * 
	 * @param listener
	 *            the TransferListener to add to the list
	 */
	public void addTransferListener(TransferListener listener) {
		transferListeners.addElement(listener);
	}

	/**
	 * add a set of TransferListener to the list of transfer listeners
	 * 
	 * @param listeners
	 *            the TransferListeners to add to the list
	 */
	public void addTransferListeners(Vector listeners) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			TransferListener listener = (TransferListener) e.nextElement();
			transferListeners.addElement(listener);
		}
	}

	/**
	 * De-register a TransferListener from the event source. The listener should
	 * not receive any more events.
	 * 
	 * @param listener
	 *            the TransferListener to de-register
	 */
	public void removeTransferListener(TransferListener listener) {
		transferListeners.removeElement(listener);
	}

	/**
	 * signal that a connection has been opened
	 * 
	 * @param event
	 *            the event to distribute to each ConnectionListener
	 */
	protected void signalConnectionOpened(ConnectionEvent event) {
		Enumeration listeners = connectionListeners.elements();
		while (listeners.hasMoreElements()) {
			ConnectionListener listener = (ConnectionListener) listeners
					.nextElement();
			listener.connectionOpened(event);
		}
	}

	/**
	 * signal that a connection has been closed
	 * 
	 * @param event
	 *            the event to distribute to each ConnectionListener
	 */
	protected void signalConnectionClosed(ConnectionEvent event) {
		Enumeration listeners = connectionListeners.elements();
		while (listeners.hasMoreElements()) {
			ConnectionListener listener = (ConnectionListener) listeners
					.nextElement();
			listener.connectionClosed(event);
		}
	}

	/**
	 * signal that a connection has encountered an error
	 * 
	 * @param exception
	 *            the exception that was thrown
	 */
	protected void signalConnectionFailed(Exception exception) {
		Enumeration listeners = connectionListeners.elements();
		while (listeners.hasMoreElements()) {
			ConnectionListener listener = (ConnectionListener) listeners
					.nextElement();
			listener.connectionFailed(exception);
		}
	}

	/**
	 * signal that a transfer has begun
	 */
	protected void signalTransferStarted() {
		Enumeration listeners = transferListeners.elements();
		while (listeners.hasMoreElements()) {
			TransferListener listener = (TransferListener) listeners
					.nextElement();
			listener.transferStarted();
		}
	}

	/**
	 * signal that a transfer has completed
	 */
	protected void signalTransferCompleted() {
		Enumeration listeners = transferListeners.elements();
		while (listeners.hasMoreElements()) {
			TransferListener listener = (TransferListener) listeners
					.nextElement();
			listener.transferCompleted();
		}
	}

	/**
	 * signal that a transfer has completed
	 * 
	 * @param amount
	 *            the amount of data (in octets) transfered
	 */
	protected void signalTransfered(long amount) {
		Enumeration listeners = transferListeners.elements();
		while (listeners.hasMoreElements()) {
			TransferListener listener = (TransferListener) listeners
					.nextElement();
			listener.transfered(amount);
		}
	}

}

// Getter.java
