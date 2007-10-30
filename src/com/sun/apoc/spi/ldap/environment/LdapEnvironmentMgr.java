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

import java.util.Hashtable;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.environment.EnvironmentMgr;
import com.sun.apoc.spi.environment.InvalidParameterException;
import com.sun.apoc.spi.environment.MissingParameterException;
import com.sun.apoc.spi.ldap.LdapClientContext;

/**
 * Handles the LDAP specific environmental information.
 */
public class LdapEnvironmentMgr extends EnvironmentMgr {
    
    /** minimum port number allowed */
    private final static int MINPORTNUMBER = 0;
    /** maximum port number allowed */
    private final static int MAXPORTNUMBER = 65535;

    /**
     * Constructor for class.
     *
     * @param aEnvironment   table of environmental information
     */
    public LdapEnvironmentMgr(Hashtable aEnvironment) {
        super(aEnvironment);
    }
    
    private void checkURL(String url, String provider) throws SPIException {
        if (getProtocolFromURL(url).equals(EnvironmentConstants.LDAP_URL_PROTOCOL)) {
            int port = getPortFromURL(url);
            if (port < MINPORTNUMBER || port > MAXPORTNUMBER) {
                throw new InvalidParameterException(
                        provider+" "+EnvironmentConstants.URL_KEY+"#port",
                        String.valueOf(port), 
                        "["+MINPORTNUMBER+","+MAXPORTNUMBER+"]");
            }
            if (getBaseEntryFromURL(url) == null) {
                throw new MissingParameterException(
                        provider+" "+EnvironmentConstants.URL_KEY+"#BaseDN");
            }
        }
    }
   
   /**
    * Check the validity of the configuration data provided in the
    * environment table.
    *
     * @throws     SPIException if data missing 
     * 				or value not appropriate
     * @throws MissingParameterException
     * @throws InvalidParameterException
    */
    public void checkEnvironment() throws SPIException {
        // Organization
        String[] orgURLs = getOrganizationURLs();
        for (int i=0; i<orgURLs.length; i++) {
            checkURL(orgURLs[i], "organization");
            String user = getOrganizationUser(orgURLs[i]);
            boolean noUser = (user == null) || (user.length() == 0);
            char[] pwd = getOrganizationCredentials();
            boolean noPwd = (pwd == null) || (pwd.length == 0);
            if (noPwd && !noUser) {
                throw new MissingParameterException(
                        "organization "+EnvironmentConstants.CREDENTIALS_KEY);
            }
        }
        String authType = getOrganizationAuthType();
        if (!authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS)
         && !authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI)) {
            throw new InvalidParameterException(
		            "organization "
                    +EnvironmentConstants.LDAP_AUTH_TYPE_KEY,
		            authType, 
		            "{"+EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS
		            +","+EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI+"}");
        }
        // Domain
        String[] domURLs = getDomainURLs();
        for (int i=0; i<domURLs.length; i++) {
            checkURL(domURLs[i], "domain");
	        String user = getDomainUser(domURLs[i]);
	        boolean noUser = (user == null) || (user.length() == 0);
	        char[] pwd = getDomainCredentials();
	        boolean noPwd = (pwd == null) || (pwd.length == 0);
	        if (noPwd && !noUser) {
	            throw new MissingParameterException(
	                    "domain "+EnvironmentConstants.CREDENTIALS_KEY);
	        }
        }
        authType = getDomainAuthType();
        if (!authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS)
         && !authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI)) {
            throw new InvalidParameterException(
		            "domain "
                    +EnvironmentConstants.LDAP_AUTH_TYPE_KEY,
		            authType, 
		            "{"+EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS
		            +","+EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI+"}");
        }
        // Profile
        String[] profileURLs = getProfileURLs();
        for (int i=0; i<profileURLs.length; i++) {
            checkURL(profileURLs[i], "profile");
	        String user = getProfileUser(profileURLs[i]);
	        boolean noUser = (user == null) || (user.length() == 0);
	        char[] pwd = getProfileCredentials();
	        boolean noPwd = (pwd == null) || (pwd.length == 0);
	        if (noPwd && !noUser) {
	            throw new MissingParameterException(
	                    "profile "+EnvironmentConstants.CREDENTIALS_KEY);
	        }
        }
        authType = getProfileAuthType();
        if (!authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS)
         && !authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI)) {
            throw new InvalidParameterException(
		            "profile "
                    +EnvironmentConstants.LDAP_AUTH_TYPE_KEY,
		            authType, 
		            "{"+EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS
		            +","+EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI+"}");
        }
        // Assignment
        String[] assignmentURLs = getAssignmentURLs();
        for (int i=0; i<assignmentURLs.length; i++) {
            checkURL(assignmentURLs[i], "assignment");
	        String user = getAssignmentUser(assignmentURLs[i]);
	        boolean noUser = (user == null) || (user.length() == 0);
	        char[] pwd = getAssignmentCredentials();
	        boolean noPwd = (pwd == null) || (pwd.length == 0);
	        if (noPwd && !noUser) {
	            throw new MissingParameterException(
	                    "assignment "+EnvironmentConstants.CREDENTIALS_KEY);
	        }
        }
        authType = getAssignmentAuthType();
        if (!authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS)
         && !authType.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI)) {
            throw new InvalidParameterException(
		            "assignment "
                    +EnvironmentConstants.LDAP_AUTH_TYPE_KEY,
		            authType, 
		            "{"+EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS
		            +","+EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI+"}");
        }
        // Metaconfiguration
        String[] metaconfURLs = getMetaConfURLs();
        for (int i=0; i<metaconfURLs.length; i++) {
            checkURL(metaconfURLs[i], "metaconfiguration");
        }
    }
    
    /**
     * gets the value of the host from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the host extracted from the URL
     * 						or localhost si error or one does not exists
     */
    public static String getHostFromURL(String stringURL) {
        String host = EnvironmentMgr.getHostFromURL(stringURL);
        if ((host == null) || (host.equals(""))) {
            host = EnvironmentConstants.LDAP_DEFAULT_SERVER;
        }
        return host;
    }

    /**
     * gets the value of the port from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the port extracted from the URL
     * 						or 389 si error or one does not exists
     */
    public static int getPortFromURL(String stringURL) {
        int port = EnvironmentMgr.getPortFromURL(stringURL);
        if (port < 0) {
            port = EnvironmentConstants.LDAP_DEFAULT_PORT;
        }
        return port;
    }

    /**
     * Accessor for the metaconfiguration.
     *
     * @return    the metaconfiguration object 
     */
    public Hashtable getMetaConfiguration() {
        return (Hashtable)mEnvironment.get(
                EnvironmentConstants.LDAP_URL_PROTOCOL); 
    }

    /**
     * Accessor for the authorized user name applicable 
     * to the organization datasource
     * 
     * @return    			the authorized user name
     * 						for the organization datasource
     * 						or anonymous if not found
     */
    public String getOrganizationAuthUser() {
        String user =  getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                                EnvironmentConstants.LDAP_AUTH_USER_KEY);
        if (user == null) {
            user = EnvironmentConstants.LDAP_USER_ANONYMOUS;
        }
        return user;
    }

    /**
     * Accessor for the authorized user name applicable 
     * to the domain datasource
     * 
     * @return    			the authorized user name
     * 						for the domain datasource
     * 						or anonymous if not found
     */
    public String getDomainAuthUser() {
        String user =  getParam(EnvironmentConstants.DOMAIN_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_USER_KEY);
        if (user == null) {
            user = EnvironmentConstants.LDAP_USER_ANONYMOUS;
        }
        return user;
    }

    /**
     * Accessor for the authorized user name applicable 
     * to the profile datasource
     * 
     * @return    			the authorized user name
     * 						for the profile datasource
     * 						or anonymous if not found
     */
    public String getProfileAuthUser() {
        String user =  getParam(EnvironmentConstants.PROFILE_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_USER_KEY);
        if (user == null) {
            user = EnvironmentConstants.LDAP_USER_ANONYMOUS;
        }
        return user;
    }

    /**
     * Accessor for the authorized user name applicable 
     * to the assignment datasource
     * 
     * @return    			the authorized user name
     * 						for the assignment datasource
     * 						or anonymous if not found
     */
    public String getAssignmentAuthUser() {
        String user =  getParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_USER_KEY);
        if (user == null) {
            user = EnvironmentConstants.LDAP_USER_ANONYMOUS;
        }
        return user;
    }

    /**
     * Accessor for the authorized user name applicable 
     * to the metaconfiguration datasource
     * 
     * @return    			the authorized user name
     * 						for the metaconfiguration datasource
     * 						or anonymous if not found
     */
    public String getMetaConfAuthUser() {
        String user =  getParam(EnvironmentConstants.LDAP_META_CONF_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_USER_KEY);
        if (user == null) {
            user = EnvironmentConstants.LDAP_USER_ANONYMOUS;
        }
        return user;
    }

    /**
     * Accessor for the authorized password applicable 
     * to the organization datasource
     * 
     * @return    			the authorized password
     * 						for the organization datasource
     */
    public char[] getOrganizationAuthPassword() {
        return getPasswordParam(EnvironmentConstants.ORGANIZATION_PREFIX,
                EnvironmentConstants.LDAP_AUTH_PASSWORD_KEY);
    }

    /**
     * Accessor for the authorized password applicable 
     * to the domain datasource
     * 
     * @return    			the authorized password
     * 						for the domain datasource
     */
    public char[] getDomainAuthPassword() {
        return getPasswordParam(EnvironmentConstants.DOMAIN_PREFIX,
    			EnvironmentConstants.LDAP_AUTH_PASSWORD_KEY);
    }

    /**
     * Accessor for the authorized password applicable 
     * to the profile datasource
     * 
     * @return    			the authorized password
     * 						for the profile datasource
     */
    public char[] getProfileAuthPassword() {
        return getPasswordParam(EnvironmentConstants.PROFILE_PREFIX,
    			EnvironmentConstants.LDAP_AUTH_PASSWORD_KEY);
    }

    /**
     * Accessor for the authorized password applicable 
     * to the assignment datasource
     * 
     * @return    			the authorized password
     * 						for the assignment datasource
     */
    public char[] getAssignmentAuthPassword() {
        return getPasswordParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
    			EnvironmentConstants.LDAP_AUTH_PASSWORD_KEY);
    }

    /**
     * Accessor for the authorized password applicable 
     * to the metaconfiguration datasource
     * 
     * @return    			the authorized password
     * 						for the metaconfiguration datasource
     */
    public char[] getMetaConfAuthPassword() {
        return getPasswordParam(EnvironmentConstants.LDAP_META_CONF_PREFIX,
    			EnvironmentConstants.LDAP_AUTH_PASSWORD_KEY);
    }

    /**
     * Accessor for the authentication type applicable 
     * to the organization datasource
     * 
     * @return    			the authentication type
     * 						for the organization datasource
     */
    public String getOrganizationAuthType() {
        String param = getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                                EnvironmentConstants.LDAP_AUTH_TYPE_KEY);
        if ( (param == null) || (param.length() == 0) ) {
            param = EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS;
        }
        return param;
    }

    /**
     * Accessor for the authentication type applicable 
     * to the domain datasource
     * 
     * @return    			the authentication type
     * 						for the domain datasource
     */
    public String getDomainAuthType() {
        String param = getParam(EnvironmentConstants.DOMAIN_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_TYPE_KEY);
        if ( (param == null) || (param.length() == 0) ) {
            param = EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS;
        }
        return param;
    }

    /**
     * Accessor for the authentication type applicable 
     * to the profile datasource
     * 
     * @return    			the authentication type
     * 						for the profile datasource
     */
    public String getProfileAuthType() {
        String param = getParam(EnvironmentConstants.PROFILE_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_TYPE_KEY);
        if ( (param == null) || (param.length() == 0) ) {
            param = EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS;
        }
        return param;
    }

    /**
     * Accessor for the authentication type applicable 
     * to the assignment datasource
     * 
     * @return    			the authentication type
     * 						for the assignment datasource
     */
    public String getAssignmentAuthType() {
        String param = getParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
                                EnvironmentConstants.LDAP_AUTH_TYPE_KEY);
        if ( (param == null) || (param.length() == 0) ) {
            param = EnvironmentConstants.LDAP_AUTH_TYPE_ANONYMOUS;
        }
        return param;
    }

    /**
     * Accessor for the connection timeout applicable 
     * to the organization datasource
     * 
     * @return    			the connection timeout 
     * 						for the organization datasource
     */
    public int getOrganizationTimeout() {
        String param = getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                                EnvironmentConstants.LDAP_TIMEOUT_KEY);
        return getIntParamFromString(param, 
                EnvironmentConstants.LDAP_DEFAULT_TIMEOUT);
    }

    /**
     * Accessor for the connection timeout applicable 
     * to the domain datasource
     * 
     * @return    			the connection timeout 
     * 						for the domain datasource
     */
    public int getDomainTimeout() {
        String param = getParam(EnvironmentConstants.DOMAIN_PREFIX,
                                EnvironmentConstants.LDAP_TIMEOUT_KEY);
        return getIntParamFromString(param, 
                EnvironmentConstants.LDAP_DEFAULT_TIMEOUT);
    }

    /**
     * Accessor for the connection timeout applicable 
     * to the assignment datasource
     * 
     * @return    			the connection timeout 
     * 						for the assignment datasource
     */
    public int getAssignmentTimeout() {
        String param = getParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
                                EnvironmentConstants.LDAP_TIMEOUT_KEY);
        return getIntParamFromString(param, 
                EnvironmentConstants.LDAP_DEFAULT_TIMEOUT);
    }

    /**
     * Accessor for the connection timeout applicable 
     * to the profile datasource
     * 
     * @return    			the connection timeout 
     * 						for the profile datasource
     */
    public int getProfileTimeout() {
        String param = getParam(EnvironmentConstants.PROFILE_PREFIX,
                                EnvironmentConstants.LDAP_TIMEOUT_KEY);
        return getIntParamFromString(param, 
                EnvironmentConstants.LDAP_DEFAULT_TIMEOUT);
    }

    /**
     * Accessor for the connection timeout applicable 
     * to the metaconfiguration datasource
     * 
     * @return    			the connection timeout 
     * 						for the metaconfiguration datasource
     */
    public int getMetaConfTimeout() {
        String param = getParam(EnvironmentConstants.LDAP_META_CONF_PREFIX,
                                EnvironmentConstants.LDAP_TIMEOUT_KEY);
        return getIntParamFromString(param, 
                EnvironmentConstants.LDAP_DEFAULT_TIMEOUT);
    }

    /**
     * gets the value the LDAP Base Entry from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the base entry extracted from the URL
     * 						or empty string if one does not exist
     */
    public static String getBaseEntryFromURL(String url) {
        String baseEntry = null;
        baseEntry = getPathFromURL(url);
        if (baseEntry != null) {
            if (baseEntry.startsWith(EnvironmentConstants.URL_SEPARATOR)) {
                baseEntry = baseEntry.substring(1);
            }
        }
        return baseEntry; 
    }

    /**
     * Accessor for the URL applicable 
     * to the metaconfiguration datasource
     * 
     * @return    			the URL for the metaconfiguration datasource
     */
    public String[] getMetaConfURLs() {
        String urls = getParam(EnvironmentConstants.LDAP_META_CONF_PREFIX, 
                               EnvironmentConstants.URL_KEY);

        // It's assumed that if there's no explicit mention of the meta-conf
        // provider, the organisation entity provider is the winner.
        if (urls == null || urls.length() == 0) {
            urls = getParam(EnvironmentConstants.ORGANIZATION_PREFIX,
                            EnvironmentConstants.URL_KEY) ;
        }
        return getURLList(urls);
    }

    /**
     * Accessor for the authentication callback handler applicable
     * to the organization datasource
     *
     * @return		the callback handler object
     * 				for the organization datasource
     */
    public Object getOrganizationCallbackHandler() {
        Object cbh = null;
        cbh = mEnvironment.get(
                		EnvironmentConstants.ORGANIZATION_PREFIX
                		+EnvironmentConstants.LDAP_AUTH_CBH);
        if (cbh == null) {
            cbh = mEnvironment.get(EnvironmentConstants.LDAP_AUTH_CBH);
        }
        return cbh;
    }
    
    /**
     * Accessor for the authentication callback handler applicable
     * to the domain datasource
     *
     * @return		the callback handler object
     * 				for the domain datasource
     */
    public Object getDomainCallbackHandler() {
        Object cbh = null;
        cbh = mEnvironment.get(
                		EnvironmentConstants.DOMAIN_PREFIX
                		+EnvironmentConstants.LDAP_AUTH_CBH);
        if (cbh == null) {
            cbh = mEnvironment.get(EnvironmentConstants.LDAP_AUTH_CBH);
        }
        return cbh;
    }
    
    /**
     * Accessor for the authentication callback handler applicable
     * to the profile datasource
     *
     * @return		the callback handler object
     * 				for the profile datasource
     */
    public Object getProfileCallbackHandler() {
        Object cbh = null;
        cbh = mEnvironment.get(
                		EnvironmentConstants.PROFILE_PREFIX
                		+EnvironmentConstants.LDAP_AUTH_CBH);
        if (cbh == null) {
            cbh = mEnvironment.get(EnvironmentConstants.LDAP_AUTH_CBH);
        }
        return cbh;
    }
    
    /**
     * Accessor for the authentication callback handler applicable
     * to the assignment datasource
     *
     * @return		the callback handler object
     * 				for the assignment datasource
     */
    public Object getAssignmentCallbackHandler() {
        Object cbh = null;
        cbh = mEnvironment.get(
                		EnvironmentConstants.ASSIGNMENT_PREFIX
                		+EnvironmentConstants.LDAP_AUTH_CBH);
        if (cbh == null) {
            cbh = mEnvironment.get(EnvironmentConstants.LDAP_AUTH_CBH);
        }
        return cbh;
    }
    
    /**
     * Accessor fot the Max Search Results parameter
     * @return the value of the parameter for max search results
     */
    public int getSearchResultSizeLimit() {
        String stSize = this.getStringParam(EnvironmentConstants.MAX_SEARCH_RESULTS);
        int size = this.getIntParamFromString(stSize, LdapClientContext.DEFAULT_MAX_SEARCH_RESULTS);
        return size;
    }
}
