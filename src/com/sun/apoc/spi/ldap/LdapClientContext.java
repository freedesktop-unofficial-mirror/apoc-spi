/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either
 * the GNU General Public License Version 2 only ("GPL") or
 * the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file
 * except in compliance with the License. You can obtain a copy
 * of the License at www.sun.com/CDDL or at COPYRIGHT. See the
 * License for the specific language governing permissions and
 * limitations under the License. When distributing the software,
 * include this License Header Notice in each file and include
 * the License file at /legal/license.txt. If applicable, add the
 * following below the License Header, with the fields enclosed
 * by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your
 * decision by adding "[Contributor] elects to include this
 * software in this distribution under the [CDDL or GPL
 * Version 2] license." If you don't indicate a single choice
 * of license, a recipient has the option to distribute your
 * version of this file under either the CDDL, the GPL Version
 * 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the
 * option applies only if the new code is made subject to such
 * option by the copyright holder.
 */

package com.sun.apoc.spi.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;
import netscape.ldap.factory.JSSESocketFactory;

import com.netscape.sasl.Sasl;
import com.sun.apoc.spi.AuthenticationException;
import com.sun.apoc.spi.CloseConnectionException;
import com.sun.apoc.spi.ConnectionSizeLimitException;
import com.sun.apoc.spi.OpenConnectionException;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.ldap.authentication.SaslFactory;
import com.sun.apoc.spi.ldap.entities.LdapEntity;

/**
  * Encapsulates the information related to a particular user of 
  * the API in an LDAP environment.
  */
public class LdapClientContext
{
    /** default maximum number of search results to return */
    public static final int DEFAULT_MAX_SEARCH_RESULTS = 100;
    
    /** character indicating that a userName contains an absolute uid **/
    public static final char DN_INDICATOR = '=';
    
	/**
	 * Socket factory ( with timeout ) used to build sockets
	 * for LDAPConnection
	 */
	private static final JSSESocketFactory sSocketFactory = new JSSESocketFactory(null);
	private static final Integer sLDAPVersion3 = new Integer(3);
	/** Caching of insecure servers. */
	private static Set sInsecureServers = new HashSet() ;
	private static final int NB_INSECURE_SERVERS = 20 ;
	/** Caching of anonymous connection pools. */
	private static Map sAnonymousPools = new HashMap() ;
	private static final int NB_ANONYMOUS_POOLS = 20 ;
    /** Connection to the datastore, put here to be
     * associated to a user and not the datastore
     * itself, which could serve multiple users. */
    private LDAPConnection mConnection = null ;
    /** protocol specified in the url */
    private String mProtocol;
    /** name of server */
    private String mServer;
    /** port number for LDAP server */
    private int mPort ;
    /** connection timeout */
    private int mConnectTimeout ;
    /** maximum number of search results to return */
    private int mMaxSearchResults = DEFAULT_MAX_SEARCH_RESULTS;
    
	static
	{
		Sasl.setSaslClientFactory( new SaslFactory() );
	}

    /**
     * Constructor. 
     *
     * @param aServer	host of the LDAP connection
     * @param aPort		port of the LDAP connection
     * @param aTimeout	timeout to initiate the LDAP connection
     */
    public LdapClientContext(String aProtocol, String aServer, 
                             int aPort, int aTimeout) {
        mProtocol = aProtocol;
        mServer = aServer;
        mPort = aPort;
        mConnectTimeout = aTimeout;
    }
    
    /**
     * creates and opens an Ldap connection, 
     * and stores it as mConnection
     * The connection is authenticated using aUserDN and aPwd.
     * If aUserDN is null, empty or Anonymous, 
     * it opens an anonymous connection.
     * 
     * @param aUserDN	full DN of the user for the authentication
     * @param aPwd 		password for the authentication
     * @throws 			SPIException if error occurs
     * @throws 			OpenConnectionException if error occurs
     * @throws 			AuthenticateException if error occurs
     */
    public void connect(String aUserDN, char[] aPwd)
			throws SPIException { 
        try { 
            if (isAnonymous(aUserDN, aPwd)) {
                mConnection = getAnonymousConnection(mServer, mPort);
                if (mConnection == null) {
                    mConnection = prepareConnection();
                    mConnection.authenticate(null,null);
                    addAnonymousConnection(mServer, mPort, mConnection);
                }
            } else {
                mConnection = prepareConnection();
	            mConnection.authenticate(aUserDN, new String(aPwd));
            }
        } catch (LDAPException ldape) {
            throw new AuthenticationException(
                    getConnectionURL(), aUserDN, ldape);
        }
    }

    /**
     * creates and opens an Ldap connection, 
     * and stores it as mConnection
     * The full DN corresponding to the username is
     * retrieved from the root Organization using the
     * authorized connection.
     * The connection is authenticated using the full DN and aPwd.
     * If aUserName is null, empty or Anonymous, 
     * it opens an anonymous connection.
     * 
     * @param aUserName       user name
     * @param aCredentials    user credentials
     * @param aRootOrg        the RootOrganization
     * @param aAuthorized     authorized connection to the datasource
     * @throws                 <code>SPIException</code> if
     *                         an error occurs
     * @throws 			OpenConnectionException if error occurs
     * @throws 			AuthenticateException if error occurs
     */
    public void authenticate(String aUserName, char[] aCredentials, 
                             Organization aRootOrg,
                             LdapClientContext aAuthorized)
    throws SPIException {
        String userId = null;
        if ((aUserName != null) && (aUserName.length() != 0)
         && (!aUserName.equalsIgnoreCase(
                    EnvironmentConstants.LDAP_USER_ANONYMOUS))) {
            if (aUserName.indexOf(DN_INDICATOR) >= 0) {
                userId = aUserName;
            }
            else {
                Entity userEntity = null;
                LdapClientContext rootOrgContext = 
                    ((LdapEntity)aRootOrg).getContext();
                ((LdapEntity)aRootOrg).setContext(aAuthorized);
                Iterator entities = aRootOrg.findUsers(aUserName, true);
                ((LdapEntity)aRootOrg).setContext(rootOrgContext);
                if (entities.hasNext()) {
                    userEntity = (Entity)entities.next();
                }
                if (userEntity == null) {
                    throw new AuthenticationException(
                            getConnectionURL(), aUserName);
                }
                userId = ((LdapEntity)userEntity).getLocation();
            }
        }
        connect(userId, aCredentials);
    }

    /**
     * creates and opens an Ldap connection, 
     * and stores it as mConnection
     * The connection is authenticated using SASL
     * and the provided callback handler.
     * 
     * @param aCallbackHandler	callback handler to use 
     * 							for authentication
     * @throws					<code>SPIException</code> if
     * 							an error occurs
     * @throws 			OpenConnectionException if error occurs
     * @throws 			AuthenticateException if error occurs
     */
    public void authenticate(Object aCallbackHandler) 
    throws SPIException {
        try {
            mConnection = prepareConnection();
            mConnection.authenticate(null, SaslFactory.sMechs,
                                     null, aCallbackHandler);
        } catch (LDAPException ldape) {
            throw new AuthenticationException(
                    getConnectionURL(), ldape);
        }
    }
    
    /**
     * Close the context, ie release all resources associated with
     * one.
     *
     * @throws  <code>SPIException</code> if LDAP 
     *			error occurs
     * @throws 			CloseConnectionException if error occurs
     */
    public void close() {
		if ( mConnection != null )
		{
			if ( ! closeAnonymousConnection(mServer, mPort, mConnection ) )
			{
				closeConnection( mConnection );
			}
			mConnection = null;
		}
    }
    
	private void closeConnection( LDAPConnection inConnection )
	{
		if ( inConnection.isConnected() )
		{
			new LDAPConnectionDisconnector( inConnection ).start();
		}
	}

    /**
     * creates an Ldap connection and connects it to the
     * mServer and mPort.
     * @return	the connection created
     * @throws SPIException
     * @throws 			OpenConnectionException if error occurs
     */
    private LDAPConnection prepareConnection () throws SPIException {
        LDAPConnection connection = null;
		if (!isInsecureServer(mServer, mPort)) {
        	try
        	{
            	// First try an SSL connection
            	connection = new LDAPConnection(sSocketFactory);
            	setupConnection(connection);
        	}
        	catch(LDAPException ldape)
        	{
            	// OTHER can be thrown if an error occurred during SSL handshake
            	// CONNECT_ERROR can be thrown if LDAP is not configured for SSL
            	if ( ((ldape.getLDAPResultCode() == LDAPException.OTHER)
             	   || (ldape.getLDAPResultCode() == LDAPException.CONNECT_ERROR))
                  && (!mProtocol.equalsIgnoreCase(EnvironmentConstants.LDAPS_URL_PROTOCOL)) ) {
					addInsecureServer(mServer, mPort) ;
					closeConnection( connection );	
					connection = null ;
				}
				else {
					throw new OpenConnectionException(
						getConnectionURL(), ldape);
				}
			}
		}
		if (connection == null ) {
        	try {
            	//Try a non SSL connection
                connection = new LDAPConnection();
                setupConnection(connection);
            }
            catch (LDAPException ldape2) {
				closeConnection( connection );
				connection = null;
                throw new OpenConnectionException(
                	getConnectionURL(), ldape2);
            }
        }
        return connection;
    }
    
    private void setupConnection (LDAPConnection connection) 
    throws LDAPException{
        try {
            // Set the protocol version to v3 (or at least try)
            connection.setOption(LDAPv3.PROTOCOL_VERSION, sLDAPVersion3) ;
            connection.setOption(LDAPv3.REFERRALS, Boolean.TRUE) ;
        }
        catch (LDAPException ignored) { }
        connection.setConnectTimeout(mConnectTimeout);
        connection.connect(mServer, mPort);
    }
    
    /**
     * Returns the connection object to be used 
     * to access the datastore.
     *
     * @return	     connection to the datastore
     */
    public LDAPConnection getConnection() { 
        return mConnection;
    }
    
    /**
     * Returns a String describing the server as an URL
     * 
     * @return	ldap://[hostname]:[port]
     */
    public String getConnectionURL() {
        return mProtocol+"://"+mServer+":"+String.valueOf(mPort);
    }
    
    /**
     * Sets the maximum number of search results returned by the Ldap connection
     * 
     * @param sizeLimit the maximum number of results
     * @throws SPIException if error occurs when setting the limit
     */
    public void setConnectionSizeLimit(int sizeLimit) throws SPIException {
        try {
            mConnection.setOption(LDAPv3.SIZELIMIT, new Integer(sizeLimit));
            mMaxSearchResults = sizeLimit;
        }
        catch (LDAPException ldape) {
            throw new ConnectionSizeLimitException(
                    getConnectionURL(), sizeLimit, ldape);
        }       
    }
    
    /**
     * Get the maximum number of search results returned by the Ldap connection 
     * @return the maximum number of results
     */
    public int getConnectionSizeLimit() {
        return mMaxSearchResults;
    }

    private static String getServerPortKey(String aServer, int aPort) {
        return aServer + ":" + aPort ;
    }
    private static boolean isInsecureServer(String aServer, int aPort) {
        return sInsecureServers.contains(getServerPortKey(aServer, aPort)) ;
    }
    private static void addInsecureServer(String aServer, int aPort) {
        if (sInsecureServers.size() < NB_INSECURE_SERVERS) {
            sInsecureServers.add(getServerPortKey(aServer, aPort)) ;
        }
    }
    private static LDAPConnection getAnonymousConnection(String aServer, 
                                                         int aPort) {
        LdapConnectionPool pool = 
            (LdapConnectionPool) sAnonymousPools.get(getServerPortKey(aServer,
                                                                      aPort)) ;

        return pool != null ? pool.getConnection() : null ;
    }
    private static void addAnonymousConnection(String aServer, int aPort,
                                               LDAPConnection aConnection) {
        LdapConnectionPool pool = null ;

        synchronized (sAnonymousPools) {
            pool = (LdapConnectionPool) sAnonymousPools.get(
                                            getServerPortKey(aServer, aPort)) ;

            if (pool == null && sAnonymousPools.size() < NB_ANONYMOUS_POOLS) {
                pool = new LdapConnectionPool(aServer, aPort) ;
                sAnonymousPools.put(getServerPortKey(aServer, aPort), pool) ;
            }
        }
        if (pool != null) { pool.addConnection(aConnection) ; }
    }
    private static boolean closeAnonymousConnection(
                                                String aServer, int aPort,
                                                LDAPConnection aConnection) {
        LdapConnectionPool pool = 
            (LdapConnectionPool) sAnonymousPools.get(getServerPortKey(aServer,
                                                                      aPort)) ;

        return pool != null ? pool.closeConnection(aConnection) : false ;
    }
    // If the user or the password are null or empty, 
    // or user is anonymous,
    // this amounts to an anonymous connection
    private static boolean isAnonymous(String aUser, char[] aCredentials) {
        return ( (aCredentials == null) || (aCredentials.length == 0) 
              || (aUser == null) || (aUser.length() == 0)
              || aUser.equalsIgnoreCase(EnvironmentConstants.LDAP_USER_ANONYMOUS) );
    }
    class LDAPConnectionDisconnector extends Thread
    {
        LDAPConnection mConnection;
        
        LDAPConnectionDisconnector( LDAPConnection inConnection ) {
            mConnection = inConnection;
        }

        public void run() {
            try {
                mConnection.disconnect();
            }
            catch( Exception theException ){}
        }
    }
}
