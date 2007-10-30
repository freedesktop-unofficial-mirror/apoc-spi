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

import java.util.Hashtable;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.environment.ConfigurationProvider;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.environment.EnvironmentMgr;
import com.sun.apoc.spi.file.environment.FileConfigurationProvider;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.ldap.environment.LdapConfigurationProvider;
import com.sun.apoc.spi.ldap.environment.LdapEnvironmentMgr;
import com.sun.apoc.spi.util.MetaConfiguration;

/**
 * manages the objects giving access to the Ldap data
 */
public class LdapConnectionHandler {
    
    private String mConnectionUrl = null;
    private LdapClientContext mAuthorizedContext = null;
    private LdapClientContext mAuthenticatedContext = null;
    private LdapDataStore mDataStore = null;
    private Organization mRootOrganization = null;
    private Domain mRootDomain = null;
    private int mMaxSearchResults;
    
    /**
     * Opens an authorized connection to the Ldap server
     * 
     * @param host			name of the Ldap server to connect to
     * @param port			port of the Ldap server to connect to
     * @param timeout		timeout on the connection to open
     * @param authUser		authorized user on the Ldap server
     * @param authPwd		password of the authorised user
     * @throws SPIException	if error occurs
     */
    public void openAuthorizedContext(
            String protocol, String host, int port, int timeout, 
            String authUser, char[] authPwd) throws SPIException {
        if (mAuthorizedContext == null) {
	        mAuthorizedContext = new LdapClientContext(protocol, host, port, timeout);
	        mAuthorizedContext.connect(authUser, authPwd);
	        mAuthorizedContext.setConnectionSizeLimit(mMaxSearchResults);
        }
    }
    
    /**
     * Closes the current authorized connection
     * @throws SPIException
     */
    public void closeAuthorizedContext() throws SPIException {
        if (mAuthorizedContext != null) {
            mAuthorizedContext.close();
            mAuthorizedContext = null;
        }
    }
    
    /**
     * Created an LdapDataStore object using the authorized connection
     * already opened
     * @param baseEntry		base entry on the Ldap server
     * @throws SPIException is error occurs
     */
    public void createDataStore(String baseEntry) throws SPIException {
		mDataStore = new LdapDataStore(baseEntry, mAuthorizedContext);
    }
    
    /**
     * Opens an authorized connection to the Ldap server,
     * opens a second connection to the Ldap server, to be authenticated,
     * and prepares the internal datastructures
     * 
     * @param url			url describing the Ldap connection to make
     * @param timeout		timeout on the connection to open
     * @param authUser		authorized user on the Ldap server
     * @param authPwd		password of the authorised user
     * @param envMgr		environment manager, used to get the
     * 						parameters describing the access to
     * 						the MetaConfiguration
     * @throws SPIException	if error occurs
     */
    public void connect(String url, int timeout, String authUser, 
            			char[] authPwd, LdapEnvironmentMgr envMgr)
    		throws SPIException {
        mConnectionUrl = url;
        mMaxSearchResults = envMgr.getSearchResultSizeLimit();
        String protocol = LdapEnvironmentMgr.getProtocolFromURL(mConnectionUrl);
        String host = LdapEnvironmentMgr.getHostFromURL(mConnectionUrl);
        int port = LdapEnvironmentMgr.getPortFromURL(mConnectionUrl);
        String baseEntry = 
            LdapEnvironmentMgr.getBaseEntryFromURL(mConnectionUrl);
        openAuthorizedContext(protocol, host, port, timeout, authUser, authPwd);
        createDataStore(baseEntry);
		MetaConfiguration metaConfData = getMetaConfiguration(envMgr);
		mDataStore.setVersion(metaConfData);
        LdapEntityMapping entityMapping = new LdapEntityMapping(metaConfData);
        mAuthenticatedContext = new LdapClientContext(protocol, host, port, timeout);
        mRootOrganization = mDataStore.createRootOrganization(
                entityMapping, mAuthorizedContext, mAuthenticatedContext);
        mRootDomain = mDataStore.createRootDomain(
                entityMapping, mAuthorizedContext, mAuthenticatedContext);
    }
    
    /**
     * Authenticate the second connection to the Ldap server
     * using the username and credentials
     * 
     * @param userName		user name of the user to authenticate
     * @param credentials	credentials of the user
     * @throws SPIException	if error occurs
     */
    public void authenticate(String userName, char[] credentials)
    		throws SPIException {
        mAuthenticatedContext.authenticate(userName, credentials,
	            mRootOrganization, mAuthorizedContext);
        mAuthenticatedContext.setConnectionSizeLimit(mMaxSearchResults);
    }
    
    /**
     * Authenticate the second connection to the Ldap server
     * using GSSAPI and the callbackHandler
     * 
     * @param callbackHandler	callbackHandler for the 
     * 							GSSAPI authentication
     * @throws SPIException		if error occurs
     */
    public void authenticate(Object callbackHandler)
			throws SPIException {
		mAuthenticatedContext.authenticate(callbackHandler);
		mAuthenticatedContext.setConnectionSizeLimit(mMaxSearchResults);
    }
    
    /**
     * closes the authenticated (second) connection
     * @throws SPIException
     */
    public void disconnect() throws SPIException {
        if (mAuthenticatedContext != null) {
            mAuthenticatedContext.close();
        }
    }

    /**
     * Connects to the specified datasource 
     * to get the Ldap Metaconfiguration data
     * 
     * @param envMgr	EnvironmentManager providing access to parameters
     * 					defining the Metaconfiguration datasource
     * @return			the metaconfiguration data
     * @throws SPIException	if error occurs
     */
    public MetaConfiguration getMetaConfiguration(LdapEnvironmentMgr envMgr) 
    		throws SPIException {
        Hashtable metaConfData = envMgr.getMetaConfiguration();
        if (metaConfData == null) {
            metaConfData = new Hashtable();
	        ConfigurationProvider metaConfProvider = null;
	        String urls[] = envMgr.getMetaConfURLs();
            for (int i=0; i<urls.length; i++) {
                String url = urls[i];
                try {
        	        String protocol = EnvironmentMgr.getProtocolFromURL(url);
        	        // Ldap
                    if (envMgr.isLdapProtocol(protocol)) {
                        metaConfProvider = getLdapMetaConfProvider(envMgr, url);
        	        } // File or Http
        	        else if (envMgr.isFileProtocol(protocol)) {
                        metaConfProvider = getFileMetaConfProvider(envMgr, url);
        	        }
        	        if (metaConfProvider == null) {
                     continue;   
                    }
                    else {
        	            metaConfData = metaConfProvider.loadData();
        	        }
                } catch (SPIException spie) {
                    // if an exception is thrown, 
                    // the connection couldn't be opened correctly
                    // so continue the loop to the next URL
                    if (metaConfProvider != null) {
                        metaConfProvider = null;
                    }
                    // if last iteration of the loop, throw the exception
                    if (i == (urls.length-1)) {
                        throw spie;
                    }
                    continue;
                }
                // no exception thrown, the connection was successful, 
                // no need to continue the loop further
                break;
            }
        }
        return new MetaConfiguration(metaConfData);
    }
    
    private LdapConfigurationProvider getLdapMetaConfProvider(
            LdapEnvironmentMgr envMgr, String url) 
            throws SPIException {
        LdapConfigurationProvider metaConfProvider = null;
        String[] attributesToGet = new String[] {LdapDataStore.ORG_MAP_KEY,
                                                 LdapDataStore.LDAP_ATTR_MAP_KEY};
        if (url.equals(mConnectionUrl)) {
            metaConfProvider = new LdapConfigurationProvider(
                    url, mAuthorizedContext, mDataStore, attributesToGet);
        }
        else {
            LdapConnectionHandler metaConfConnection = new LdapConnectionHandler();
            metaConfConnection.openAuthorizedContext(
                    LdapEnvironmentMgr.getProtocolFromURL(url),
                    LdapEnvironmentMgr.getHostFromURL(url),
                    LdapEnvironmentMgr.getPortFromURL(url),
                    envMgr.getMetaConfTimeout(),
                    envMgr.getMetaConfAuthUser(),
                    envMgr.getMetaConfAuthPassword());
            metaConfConnection.createDataStore(
                    LdapEnvironmentMgr.getBaseEntryFromURL(url));
            metaConfProvider = new LdapConfigurationProvider(
                    url, metaConfConnection.getAuthorizedContext(), 
                    metaConfConnection.getDataStore(), 
                    attributesToGet);
            metaConfConnection.closeAuthorizedContext();
        }
        return metaConfProvider;
    }
        
    private FileConfigurationProvider getFileMetaConfProvider(
            LdapEnvironmentMgr envMgr, String url) 
            throws SPIException {
        FileConfigurationProvider metaConfProvider = null;
        StringBuffer rootURL = new StringBuffer(url);
        if (!url.endsWith(EnvironmentConstants.URL_SEPARATOR)) {
            rootURL.append(EnvironmentConstants.URL_SEPARATOR);
        }
        StringBuffer orgMapFile = new StringBuffer();
        orgMapFile.append(rootURL).append(EnvironmentConstants.ORG_MAP_FILE);
        String[] filesToGet = new String[] {orgMapFile.toString()};
        metaConfProvider = new FileConfigurationProvider(filesToGet);
        return metaConfProvider;
    }
    
    /**
     * @return Returns the mAuthenticatedContext.
     */
    public LdapClientContext getAuthenticatedContext() {
        return mAuthenticatedContext;
    }
    /**
     * @return Returns the mAuthorizedContext.
     */
    public LdapClientContext getAuthorizedContext() {
        return mAuthorizedContext;
    }
    /**
     * @return Returns the mDataStore.
     */
    public LdapDataStore getDataStore() {
        return mDataStore;
    }
    /**
     * @return Returns the mRootDomain.
     */
    public Domain getRootDomain() {
        return mRootDomain;
    }
    /**
     * @return Returns the mRootOrganization.
     */
    public Organization getRootOrganization() {
        return mRootOrganization;
    }
}
