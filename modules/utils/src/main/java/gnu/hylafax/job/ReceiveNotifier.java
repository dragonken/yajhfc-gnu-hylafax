// ReceiveNotifier.java - a HylaFAX Job representation
// $Id$
//
// Copyright 2003 Innovation Software Group, LLC - http://www.innovationsw.com
//                Joe Phillips <joe.phillips@innovationsw.com>
//
// for information on the HylaFAX FAX server see
//  http://www.hylafax.org/
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
package gnu.hylafax.job;

/**
 * This interface defines what a class should implement in order to notify
 * others of job related events.
 * 
 * @author $Author$
 * @version $Id$
 * @see gnu.hylafax.job.ReceiveListener
 * @see gnu.hylafax.job.ReceiveEvent
 */
public interface ReceiveNotifier {

    /**
     * This method is called when Job state changes.
     */
    public void notifyReceiveListeners(ReceiveEvent details);

    /**
     * This method is called to register a Job ReceiveListener.
     */
    public void addReceiveListener(ReceiveListener l);

    /**
     * This method is used to deregister a Job ReceiveListener.
     */
    public void removeReceiveListener(ReceiveListener l);

}// ReceiveNotifier class
// ReceiveNotifier.java
