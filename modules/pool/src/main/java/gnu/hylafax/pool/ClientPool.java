//$Id: ClientPool.java,v 1.6 2007/05/07 18:26:55 sjardine Exp $
//
//Copyright 2006 Steven Jardine <steve@mjnservices.com>
//Copyright 2006 MJN Services, Inc - http://www.mjnservices.com
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
package gnu.hylafax.pool;

import gnu.inet.logging.Logger;
import gnu.inet.logging.LoggingFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

public class ClientPool {

	private static final Logger log = LoggingFactory
			.getLogger(ClientPool.class);

	private boolean blocked = false;

	private boolean stopped = false;

	private HashMap clientMap;

	private LinkedQueue clients;

	private ClientPoolConfiguration configuration;

	private ArrayList creationTimes;

	private boolean logClientCreationTimes = true;

	private Object mutex = new Object();

	private int size = 0;

	private int totalSize = 0;

	private HashSet workingClients;

	private HashSet workingClientsToClose;

	private int workingSize = 0;

	public ClientPool(ClientPoolConfiguration configuration) {

		this.configuration = configuration;

		clients = new LinkedQueue();
		clientMap = new HashMap();
		workingClients = new HashSet();
		workingClientsToClose = new HashSet();
		creationTimes = new ArrayList();

	}

	private synchronized boolean addClient() throws ClientPoolException {

		log.debug("Trying To Create Client, Total Connections: " + totalSize
				+ ", Max Allowed: " + getConfiguration().getMaxPoolSize());

		boolean maximumCapacityReached = getConfiguration().getMaxPoolSize() <= totalSize
				&& getConfiguration().getMaxPoolSize() != 0
				&& getConfiguration().isPooling();

		if (maximumCapacityReached) {

			log.debug("Maximum Clients Reached.");
			return false;
		}

		PooledClient client = createClient();
		log.debug("Client Created.");

		put(client);
		clientMap.put(client, client);
		totalSize++;

		return true;
	}

	private PooledClient createClient() throws ClientPoolException {

		return openClient(new HylaFAXPooledClient(this));

	}

	private void destroyClient(PooledClient client) throws ClientPoolException {

		try {

			client.destroy();

			totalSize--;
			workingClients.remove(client);
			clientMap.remove(client);

		} catch (Exception e) {

			throw new ClientPoolException("Could Not Destroy Client: "
					+ e.getMessage());

		}

	}

	public PooledClient get() throws ClientPoolException {

		long startTime = System.currentTimeMillis();

		log.debug("Wants A Client.");

		PooledClient client = null;

		try {

			synchronized (mutex) {
				if (clients.isEmpty()) {

					while (client == null) {

						if (!keepBlocking(startTime)) {
							ClientPoolException e = new ClientPoolException(
									"Could Not Obtain Client During Blocking Timeout ("
											+ getConfiguration()
													.getBlockingTimeout()
											+ " ms)");
							throw e;
						}

						boolean clientAdded = false;

						try {
							clientAdded = addClient();
						} catch (ClientPoolException e) {
							log.warn("Could Not Create Connection: "
									+ e.getMessage());
						}

						if (!clientAdded) {
							log.warn("Pool Is Empty And Will Block Here.");
							blocked = true;
						}

						client = (PooledClient) clients.poll(getConfiguration()
								.getRetryInterval());

						if (client == null)
							log.warn("No Clients Available.");
						else if (client != null && !clientAdded)
							log.info("Obtained Connection.");
					}

				} else {
					client = (PooledClient) clients.take();
				}
			}

		} catch (InterruptedException e) {
			throw new ClientPoolException(
					"Interrupted Thread and No Free Connection Available.");
		}

		size--;

		((HylaFAXPooledClient) client).setWorking(true);
		workingClients.add(client);

		log.debug("Got Client.");

		return client;
	}

	public long getAverageClientCreationTime() {

		if (creationTimes.size() > 0) {

			long average = 0;
			for (int count = 0; count < creationTimes.size(); count++) {
				average += ((Long) creationTimes.get(count)).longValue();
			}
			return average / creationTimes.size();
		}

		return -1;

	}

	public HashMap getClientMap() {
		return clientMap;
	}

	public ClientPoolConfiguration getConfiguration() {

		return configuration;

	}

	public int getSize() {

		return size;

	}

	public int getTotalSize() {

		return totalSize;

	}

	public int getWorkingSize() {

		return workingSize;

	}

	public boolean isLogClientCreationTimes() {

		return logClientCreationTimes;

	}

	public boolean keepBlocking(long startTime) {
		return System.currentTimeMillis() - startTime < getConfiguration()
				.getBlockingTimeout();
	}

	PooledClient openClient(HylaFAXPooledClient client)
			throws ClientPoolException {

		try {

			long startTime = System.currentTimeMillis();

			ClientPoolConfiguration config = getConfiguration();

			if (config.getHost() != null && config.getPort() != -1)
				client.poolOpen(config.getHost(), config.getPort());
			else if (config.getHost() != null)
				client.poolOpen(config.getHost());
			else
				client.poolOpen();

			if (config.getUserName() != null)
				client.poolUser(config.getUserName());

			if (config.getPassword() != null)
				client.poolPass(config.getPassword());

			if (config.getAdminPassword() != null)
				client.poolAdmin(config.getAdminPassword());

			if (config.getTimeZone() != null)
				client.poolTzone(config.getTimeZone());

			if (isLogClientCreationTimes())
				creationTimes.add(new Long(System.currentTimeMillis()
						- startTime));

			client.setPassive(true);

			return client;

		} catch (Exception e) {

			throw new ClientPoolException(e.getMessage());

		}

	}

	public void put(PooledClient client) throws ClientPoolException {
		try {

			if (!client.isValid())
				workingClientsToClose.add(client);
			((HylaFAXPooledClient) client).setWorking(false);

			if (blocked)
				log.warn("Will Be Unblocked");

			if (getConfiguration().isPooling()) {

				if (workingClientsToClose.remove(client)) {

					destroyClient(client);
					addClient();

				} else {

					clients.put(client);
					size++;

				}
			}
			blocked = false;

			workingClients.remove(client);

			// Desctoy client if pooling is not enabled.
			if (!getConfiguration().isPooling())
				destroyClient(client);

			log.debug("Released Client.");

		} catch (InterruptedException e) {
			log.warn("Was Interrupted.", e);
			destroyClient(client);
		}
	}

	public void restart() {
		// Flag working clients for destruction when returned to the stack.
		workingClientsToClose.addAll(workingClients);

		// Close all free clients
		while (getSize() > 0)
			try {
				PooledClient client = get();
				destroyClient(client);
			} catch (ClientPoolException e) {
				log.warn("Could Not Close Connection.", e);
			}
		// Create enough clients to restore the stack to minPoolSize.
		while (getTotalSize() < getConfiguration().getMinPoolSize())
			try {
				addClient();
			} catch (Exception e) {
				log.warn("Could Not Add Connection.", e);
			}
	}

	public void setClientMap(HashMap clientMap) {
		this.clientMap = clientMap;
	}

	public void setConfiguration(ClientPoolConfiguration configuration) {
		this.configuration = configuration;
	}

	public void setLogClientCreationTimes(boolean logClientCreationTimes) {

		this.logClientCreationTimes = logClientCreationTimes;

	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public void setWorkingSize(int workingSize) {
		this.workingSize = workingSize;
	}

	public void start() throws ClientPoolException {
		stopped = false;
		for (int i = 0; i < getConfiguration().getMinPoolSize(); i++) {
			addClient();
		}
	}

	public void stop() {
		stopped = true;
		// Close all working connections.
		Iterator iter = workingClients.iterator();
		while (iter.hasNext()) {
			try {
				PooledClient item = (PooledClient) iter.next();
				destroyClient(item);
			} catch (ClientPoolException e) {
				log.warn("Could Not Close Connection.", e);
			}
		}

		// Close all free connections
		while (getSize() > 0)
			try {
				PooledClient item = get();
				destroyClient(item);
			} catch (ClientPoolException e) {
				log.warn("Could Not Close Connection.", e);
			}
		totalSize = 0;
	}

	public boolean isStopped() {
		return stopped;
	}
}
