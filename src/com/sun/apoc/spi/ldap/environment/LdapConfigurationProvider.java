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
package com.sun.apoc.spi.ldap.environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import netscape.ldap.LDAPException;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.environment.ConfigurationProvider;
import com.sun.apoc.spi.environment.RemoteEnvironmentException;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;

/**
 *
 */
public class LdapConfigurationProvider implements ConfigurationProvider {
    
    // static Map url-properties to avoid connecting to Ldap server
    // multiple times for the same url, therefore, same data
    private static Map mContentMap = null;

    private String mUrl = null;
    private LdapClientContext mContext = null;
    private LdapDataStore mDataStore = null;
    private String[] mAttributesToGet = null;
    
    /**
     * Constructor
     * 
     * @param context			connection to the Ldap server
     * @param dataStore			datastore providing access
     * 							to the Ldap server
     * @param attributesToGet	attributes to read data from,
     * 							under the service entry of the Ldap server
     */
    public LdapConfigurationProvider(String url,
                                     LdapClientContext context, 
            						 LdapDataStore dataStore,
            						 String[] attributesToGet) {
        if (mContentMap == null) {
            mContentMap = new HashMap();
        }
        mUrl = url;
        mContext = context;
        mDataStore = dataStore;
        mAttributesToGet = attributesToGet;
    }

    /**
     * Loads the configuration data from the different attributes 
     * and put them all in the same Hashtable returned
     * 
     * @return	the Hashtable containing all the parameters
     * 			read from the different attributes
     * @throws SPIException	if error occurs
     * @see com.sun.apoc.spi.environment.ConfigurationProvider#loadData()
     */
    public Hashtable loadData() throws SPIException {
        Properties propertyList = null;
        synchronized (mContentMap) {
            propertyList = (Properties) mContentMap.get(mUrl);
        }
        //Properties propertyList = null;
        if (propertyList == null) {
            Vector settings = null;
    	    String [] attributes = {LdapDataStore.KEYVALUE_ATTR};
    	    try {
    	        settings = mDataStore.getAttributeValueList(
    							mDataStore.getRootServiceEntryDN(),
    							attributes, mContext);
    	    } catch (LDAPException ldape) {
    	        throw new RemoteEnvironmentException(
    	                RemoteEnvironmentException.LDAP_CONF_KEY,
    	                new String[] {mContext.getConnectionURL()}, ldape);
    	    }
    	    StringBuffer data = new StringBuffer();
    	    for (int i=0; i<mAttributesToGet.length; i++) {
    	        data.append(loadAttributeData(mAttributesToGet[i], settings));
    	    }
    	    ByteArrayInputStream input = new ByteArrayInputStream(
    				data.toString().getBytes());
    	    propertyList = new Properties();
    	    try {
    	         propertyList.load(input);
    	    } catch (IOException ioe) {
    	        throw new RemoteEnvironmentException(
    	                RemoteEnvironmentException.LDAP_CONF_KEY,
    	                new String[] {mContext.getConnectionURL()}, ioe);
            }
            synchronized (mContentMap) {
                mContentMap.put(mUrl, propertyList);
            }
        }
	    return propertyList;
    }

    /**
     * Loads the configuration data from the specified attribute
     * and return a StringBuffer containing the data
     * 
     * @param attributeName	attribute to read data from on the Ldap server
     * @param settings		settings for the Ldap server
     * @return				StringBuffer containing the data 
     * 						read in the attribute
     * @throws RemoteEnvironmentException	if data has incorrect format
     */
    private StringBuffer loadAttributeData(String attributeName, 
            							   Vector settings) 
    		throws RemoteEnvironmentException {
	    StringBuffer data = new StringBuffer();
	    String [] values = LdapDataStore.getValuesForKey(settings, attributeName);
	    for (int i = 0; i < values.length; i++) {
	        String line = values[i];
	        if (data != null) {
	            data.append(line).append("\n");
            } else {
    	        throw new RemoteEnvironmentException(
    	                RemoteEnvironmentException.INVALID_LDAP_CONF_KEY,
    	                new String[] {mContext.getConnectionURL()});
	        }
	    }
	    return data;
    }
    
}
