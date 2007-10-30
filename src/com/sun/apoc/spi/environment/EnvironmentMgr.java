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

package com.sun.apoc.spi.environment;

import com.sun.apoc.spi.AssignmentProvider;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import com.sun.apoc.spi.profiles.ProfileProvider;

/**
  * Handles the environmental information.
  *
  */
public class EnvironmentMgr
{
    /* old parameter names (from APOC 1.0)*/
    public static final String SERVER_KEY = "Server";
    public static final String PORT_KEY = "Port";
    public static final String BASE_DN_KEY = "BaseDn";
    public static final String TYPE_KEY = "DataStoreType";
    public static final String LDAP_TYPE = "LDAP";
    
    /* table containing environment settings */
    protected Hashtable mEnvironment;

    protected static HashSet sLdapProtocols = new HashSet();
    protected static HashSet sFileProtocols = new HashSet();
    
    private Hashtable mConnections = new Hashtable();
    
    /**
      * Static constructor, just puts the accepted protocol strings in the 
      * appropriate scheme sets.
      */
    static {
        sLdapProtocols.add(EnvironmentConstants.LDAP_URL_PROTOCOL);
        sLdapProtocols.add(EnvironmentConstants.LDAPS_URL_PROTOCOL);
        sFileProtocols.add(EnvironmentConstants.FILE_URL_PROTOCOL);
        sFileProtocols.add(EnvironmentConstants.HTTP_URL_PROTOCOL);
        sFileProtocols.add(EnvironmentConstants.HTTPS_URL_PROTOCOL);
    }
    /**
     * Constructor for class.
     *
     * @param aEnvironment   table of environmental information
     */
    public EnvironmentMgr(Hashtable aEnvironment) {
        mEnvironment  = aEnvironment;
        translateOldParameters() ;
    }
    
    public void translateOldParameters() {
        if ( (this.getOrganizationURLs().length == 0)
          && (this.getDomainURLs().length == 0)
          && (this.getProfileURLs().length == 0)
          && (this.getAssignmentURLs().length == 0) ) {
            String type = (String)mEnvironment.get(TYPE_KEY);
            if ( (type == null) || (type.length() == 0) ) {
                type = LDAP_TYPE;
            }
            if (type.equals(LDAP_TYPE)) {
	            String server = (String)mEnvironment.get(SERVER_KEY);
	            boolean noServer = (server == null) 
	            				|| (server.length() == 0);
	            String basedn = (String)mEnvironment.get(BASE_DN_KEY);
	            boolean noBasedn = (basedn == null) 
								|| (basedn.length() == 0);
	            if (!noServer && !noBasedn) {
	                StringBuffer url = new StringBuffer(
	                        EnvironmentConstants.LDAP_URL_PROTOCOL);
	                url.append("://").append(server);
	                String port = (String)mEnvironment.get(PORT_KEY);
		            boolean noPort = (port == null) 
    							  || (port.length() == 0);
		            if (!noPort) {
		                url.append(":").append(port);
		            }
		            url.append("/").append(basedn);
		            mEnvironment.put(EnvironmentConstants.URL_KEY, 
		                    		 url.toString());
	            }
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
    public void checkEnvironment( ) throws SPIException {
        // mandatory parameters
   /*     if (this.getOrganizationURLs().length == 0) {
            throw new MissingParameterException(
                    "organization "+EnvironmentConstants.URL_KEY);
        }
        if (this.getDomainURLs().length == 0) {
            throw new MissingParameterException(
                    "domain "+EnvironmentConstants.URL_KEY);
        }
        if (this.getProfileURLs().length == 0) {
            throw new MissingParameterException(
                    "profile "+EnvironmentConstants.URL_KEY);
        }
        if (this.getAssignmentURLs().length == 0) {
            throw new MissingParameterException(
                    "assignment "+EnvironmentConstants.URL_KEY);
        }*/
    }

    /**
     * Returns a copy of the environment table, excluding the user name and credentials.
     *
     * @return         <code>Hashtable</code> containing the environment settings, excluding
     *                 the user name and credentials
     */
   public Hashtable getEnvironment() {
       return mEnvironment;
   }

    /**
     * Accessor to a String parameter in the environment table
     * 
     * @param paramName 	the name of the parameter to return
     * @return    			the String value of that parameter
     */
   public String getStringParam(String paramName) {
        return (String)mEnvironment.get(paramName);
    }
   
   /**
    * Accessor to a String parameter in the environment table
    * depending on its prefix
    * 
    * @param prefix     the prefix to append at the beginning
    *                   of the parameter name
    * @param paramName  the name of the parameter to return
    * @return           the String value of that parameter
    */
   protected String getParam(String prefix, String paramName) {
       String paramValue = null;
       if (prefix != null) {
           paramValue = getStringParam(prefix + paramName);
       }
       if (paramValue == null) {
           paramValue = getStringParam(paramName);
       }
       return paramValue;
   }
   
   /**
    * Accessor to a String parameter in the environment table
    * depending on its prefix
    * 
    * @param aEntityPrefix the prefix denoting the entity type 
    *                   of the parameter
    * @param aProviderPrefix the prefix denoting the provider type
    *                   of the parameter 
    * @param paramName  the name of the parameter to return
    * @return           the String value of that parameter
    */
   protected String getParam(String aEntityPrefix, String aProviderPrefix, String paramName) {
       String paramValue = null;
       if (aEntityPrefix != null && aProviderPrefix!= null) {
           paramValue = getStringParam(aEntityPrefix + aProviderPrefix + paramName);
       }
       if (paramValue == null) {
           paramValue = getStringParam(aProviderPrefix + paramName);
       }
       if (paramValue == null) {
           paramValue = getStringParam(paramName);
       }
       return paramValue;
   }
   
   /**
    * Splits a String composed of URLs separated by white spaces
    * into a array of String
    * 
    * @param urlString the list of URLs
    * @return          an array of URLs
    */
   protected String[] getURLList(String urlString) {
       String[] urls = new String[0];
       if (urlString != null) {
           urls = urlString.split("\\s");
       }
       return urls;
   }
    
    /**
     * gets the value of a parameter representing a int.
     * 
     * @param stringValue 	the string value of the parametrer
     * @param defaultValue	the default value for that parameter
     * @return    			the int value of that parameter
     * 						or the defaultValue if there is an error
     */
    protected int getIntParamFromString(String stringValue, 
            						  int defaultValue) {
        int intValue = defaultValue;
        try {
            intValue = Integer.parseInt(stringValue);
        }
        catch (Exception e) {}
        return intValue;
    }
    
    /**
     * gets the password parameter value for the given prefix
     * and password name
     * 
     * @param prefix	 	prefix to search password param for
     * @param paramName		name of the password parameter
     * @return    			parameter value for the given prefix 
     * 						and parameter name
     */
    protected char[] getPasswordParam(String prefix, String paramName) {
        Object paramValue = null;
        char[] credentials = null;
        paramValue = mEnvironment.get(prefix+paramName);
        if (paramValue == null) {
            paramValue = mEnvironment.get(paramName);
        }
        if (paramValue != null) {
            if (paramValue instanceof String) {
                credentials = ((String)paramValue).toCharArray();
            }
            else {
                try {
                    credentials = (char[])paramValue;
                } catch (ClassCastException cce) {
                    credentials = null;
                }
            }
        }
        String passwordEncoding = getStringParam(
                prefix+paramName+EnvironmentConstants.ENCODING_SUFFIX);
        if (passwordEncoding == null) {
            passwordEncoding = getStringParam(
                    paramName+EnvironmentConstants.ENCODING_SUFFIX);
        }
        if ((passwordEncoding != null) 
         && passwordEncoding.equals(EnvironmentConstants.SCRAMBLE_ENCODING)) {
        }
        return credentials;
    }

    /**
     * gets the value of the user from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the user extracted from the URL
     * 						or null if error
     */
    public static String getUserFromURL(String stringURL) {
        String username  = null;
        try {
            username = new URI(stringURL).getUserInfo();
        } catch (Exception e) {}
        return username;
    }
    
    /**
     * gets the value the protocol from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the protocol extracted from the URL
     * 						or null if error
     */
    public static String getProtocolFromURL(String stringURL) {
        String protocol = null;
        try {
            protocol = new URI(stringURL).getScheme();
        } catch (Exception e) {}
        return protocol;
    }
    
    /**
     * gets the value of the host from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the host extracted from the URL
     * 						or null if error
     */
    public static String getHostFromURL(String stringURL) {
        String host = null;
        try {
            host = new URI(stringURL).getHost();
        } catch (Exception e) {}
        return host;
    }
    
    /**
     * gets the value of the port from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the port extracted from the URL
     * 						or -1 if error occurs
     */
    public static int getPortFromURL(String stringURL) {
        int port = -1;
        try {
            URI url = new URI(stringURL);
            port = url.getPort();
        } catch (Exception e) {}
        return port;
    }
    
    /**
     * gets the value of the path from a URL parameter
     * 
     * @param stringURL 	the string representing the URL
     * @return    			the path extracted from the URL
     * 						or null if error
     */
    public static String getPathFromURL(String stringURL) {
        String path = null;
        try {
            path = new URI(stringURL).getPath();
        } catch (Exception e) {}
        return path;
    }
    
    /**
     * Accessor for the list of URLs applicable 
     * to the organization datasource
     * 
     * @return  the URLs for the organization datasource
     */
    public String[] getOrganizationURLs() {
        String urls = getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                               EnvironmentConstants.URL_KEY);
        return getURLList(urls);
    }

    /**
     * Accessor for the list of URLs applicable 
     * to the domain datasource
     * 
     * @return  the URLs for the domain datasource
     */
    public String[] getDomainURLs() {
        String urls = getParam(EnvironmentConstants.DOMAIN_PREFIX, 
                               EnvironmentConstants.URL_KEY);
        return getURLList(urls);
    }

    /**
     * Accessor for the list of URLs applicable 
     * to the profile datasource
     * 
     * @return  the URLs for the profile datasource
     */
    public String[] getProfileURLs() {
        String urls = getParam(EnvironmentConstants.PROFILE_PREFIX, 
                               EnvironmentConstants.URL_KEY);
        return getURLList(urls);
    }

    /**
     * Accessor for the list of URLs applicable 
     * to the assignment datasource
     * 
     * @return  the URLs for the assignment datasource
     */
    public String[] getAssignmentURLs() {
        String urls = getParam(EnvironmentConstants.ASSIGNMENT_PREFIX, 
                               EnvironmentConstants.URL_KEY);
        return getURLList(urls);
    }

    /**
     * Accessor for the list of URLs applicable 
     * to the entity datasource
     * 
     * @return  the URLs for the entity datasource
     */
    public String[] getProviderURLs(String aEntityPrefix, String aProviderPrefix) {
        String urls = getParam(aEntityPrefix,
                               aProviderPrefix,
                               EnvironmentConstants.URL_KEY);
        return getURLList(urls);
    }
    
    /**
     * Accessor for the list of URLs applicable 
     * to the entity datasource
     * 
     * @return  the URLs for the entity datasource
     */
    public String getProviderClass(String aEntityPrefix, String aProviderPrefix) {
        return getParam(aEntityPrefix,
                        aProviderPrefix,
                        EnvironmentConstants.CLASS_KEY);
    }
    
    public String[] getProviderURLs(String aName, Class aProviderClass) {
        String aProviderPrefix = null;
        if (aProviderClass == EntityTreeProvider.class) {
            aProviderPrefix = "";
        } else if (aProviderClass == AssignmentProvider.class) {
            aProviderPrefix = EnvironmentConstants.ASSIGNMENT_PREFIX;
        } else if (aProviderClass == ProfileProvider.class) {
            aProviderPrefix = EnvironmentConstants.PROFILE_PREFIX;
        }
        aName = aName + EnvironmentConstants.SEPARATOR ;
        return getProviderURLs(aName, aProviderPrefix);
    }   
    
     public String getProviderClass(String aName, Class aProviderClass) {
        String aProviderPrefix = null;
        if (aProviderClass == EntityTreeProvider.class) {
            aProviderPrefix = "";
        } else if (aProviderClass == AssignmentProvider.class) {
            aProviderPrefix = EnvironmentConstants.ASSIGNMENT_PREFIX;
        } else if (aProviderClass == ProfileProvider.class) {
            aProviderPrefix = EnvironmentConstants.PROFILE_PREFIX;
        }
        aName = aName + EnvironmentConstants.SEPARATOR ;
        return getProviderClass(aName, aProviderPrefix);
    }     
     
     public String[] getSources() {
         return EnvironmentMgr.getSources(mEnvironment) ;
     }
     
     private static final String [] DEFAULT_SOURCES = {
         EnvironmentConstants.HOST_SOURCE, EnvironmentConstants.USER_SOURCE
     } ;
     
     public static String [] getSources(Hashtable aEnvironment) {
         String sourcesList = 
                    (String) aEnvironment.get(EnvironmentConstants.SOURCES_KEY);
         if (sourcesList == null) {
             return DEFAULT_SOURCES ;
         } else {
             return sourcesList.split(",");
         }
     }   
     
    /**
     * Accessor for the credentials applicable 
     * to the organization datasource
     * 
     * @return    	the credentials for the organization datasource
     */
    public char[] getOrganizationCredentials() {
        return getPasswordParam(EnvironmentConstants.ORGANIZATION_PREFIX,
                				EnvironmentConstants.CREDENTIALS_KEY);
    }

    /**
     * Accessor for the credentials applicable 
     * to the domain datasource
     * 
     * @return    	the credentials for the domain datasource
     */
    public char[] getDomainCredentials() {
        return getPasswordParam(EnvironmentConstants.DOMAIN_PREFIX,
								EnvironmentConstants.CREDENTIALS_KEY);
    }

    /**
     * Accessor for the credentials applicable 
     * to the profile datasource
     * 
     * @return    	the credentials for the profile datasource
     */
    public char[] getProfileCredentials() {
        return getPasswordParam(EnvironmentConstants.PROFILE_PREFIX,
								EnvironmentConstants.CREDENTIALS_KEY);
    }

    /**
     * Accessor for the credentials applicable 
     * to the assignment datasource
     * 
     * @return    	the credentials  for the assignment datasource
     */
    public char[] getAssignmentCredentials() {
        return getPasswordParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
								EnvironmentConstants.CREDENTIALS_KEY);
    }

    /**
     * Accessor for the provider class applicable 
     * to the organization datasource
     * 
     * @return    			the provider class for the 
     * 						organization datasource
     */
    public String getOrganizationClass() {
        return getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                        EnvironmentConstants.CLASS_KEY);
    }

    /**
     * Accessor for the provider applicable 
     * to the domain datasource
     * 
     * @return    			the provider class for the 
     * 						organization datasource
     */
    public String getDomainClass() {
        return getParam(EnvironmentConstants.DOMAIN_PREFIX,
                        EnvironmentConstants.CLASS_KEY);
    }

    /**
     * Accessor for the provider applicable to the profile datasource
     * 
     * @return    			the provider class for the 
     * 						organization datasource
     */
    public String getProfileClass() {
        return getParam(EnvironmentConstants.PROFILE_PREFIX,
                        EnvironmentConstants.CLASS_KEY);
    }

    /**
     * Accessor for the provider applicable 
     * to the assignment datasource
     * 
     * @return    			the provider class for the 
     * 						organization datasource
     */
    public String getAssignmentClass() {
        return getParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
                        EnvironmentConstants.CLASS_KEY);
    }
    
    /**
     * Accessor for the user applicable 
     * to the organization datasource
     * 
     * @param url   the url where the username can be declared
     * @return    	the user for the organization datasource
     */
    public String getOrganizationUser(String url) {
        String username = null;
        username = getUserFromURL(url);
        if (username == null) {
            username = getParam(EnvironmentConstants.ORGANIZATION_PREFIX, 
                                EnvironmentConstants.USER_KEY);
        }
        return username;
    }

    /**
     * Accessor for the user applicable 
     * to the domain datasource
     * 
     * @param url   the url where the username can be declared
     * @return    	the user for the domain datasource
     */
    public String getDomainUser(String url) {
        String username = null;
        if (url != null) {
            username = getUserFromURL(url);
        }
        if (username == null) {
            username = getParam(EnvironmentConstants.DOMAIN_PREFIX,
                                EnvironmentConstants.USER_KEY);
        }
        return username;
    }

    /**
     * Accessor for the user applicable 
     * to the profile datasource
     * 
     * @param url   the url where the username can be declared
     * @return    	the user for the Profile datasource
     */
    public String getProfileUser(String url) {
        String username = null;
        if (url != null) {
            username = getUserFromURL(url);
        }
        if (username == null) {
            username = getParam(EnvironmentConstants.PROFILE_PREFIX,
                                EnvironmentConstants.USER_KEY);
        }
        return username;
    }

    /**
     * Accessor for the user applicable 
     * to the assignment datasource
     * 
     * @param url   the url where the username can be declared
     * @return    	the user for the assignment datasource
     */
    public String getAssignmentUser(String url) {
        String username = null;
        if (url != null) {
            username = getUserFromURL(url);
        }
        if (username == null) {
            username = getParam(EnvironmentConstants.ASSIGNMENT_PREFIX,
                                EnvironmentConstants.USER_KEY);
        }
        return username;
    }

    /**
     * Indicates if the submitted protocol is a protocol 
     * for an Ldap connection
     * 
     * @param protocol a String identifying the protocol
     * @return true if protocol is ldap or ldaps
     */
    public static boolean isLdapProtocol(String protocol) {
        return sLdapProtocols.contains(protocol);
    }

    /**
     * Indicates if the submitted protocol is a protocol 
     * for an File or HTTP connection
     * 
     * @param protocol a String identifying the protocol
     * @return true if protocol is file, http or https
     */
    public static boolean isFileProtocol(String protocol) {
        return sFileProtocols.contains(protocol);
    }

    /**
     * returns a String listing all the supported connection protocols.
     * This list is to be used in error messages for example.
     * @return list of supported protocols
     */
    public static String getSupportedProtocolsString() {
        StringBuffer protocolsString = new StringBuffer("{");
        Iterator iterLdapProtocols = sLdapProtocols.iterator();
        while (iterLdapProtocols.hasNext()) {
            protocolsString.append((String)iterLdapProtocols.next()).append(",");
        }
        Iterator iterFileProtocols = sFileProtocols.iterator();
        while (iterFileProtocols.hasNext()) {
            protocolsString.append((String)iterFileProtocols.next()).append(",");
        }
        if (protocolsString.lastIndexOf(",") == (protocolsString.length()-1)) {
            protocolsString.delete(protocolsString.length()-1, protocolsString.length());
        }
        protocolsString.append("}");
        return protocolsString.toString();
    }

    /**
     * gets the object stored under the key url 
     * and managing the connection to that url
     * 
     * @param stringURL     the string representing the URL 
     *                      for the connection
     * @return              an object managing the connection 
     *                      or null if one does not exist
     */
    public synchronized Object getConnectionHandler(String stringURL) {
        Object connection = null;
        if (stringURL != null) {
            connection = mConnections.get(stringURL);
        }
        return connection;
    }
    
    /**
     * stores a connection handler under the key url
     * 
     * @param stringURL     the string representing the URL 
     * @param connection    the object managing the connection
     */
    public synchronized void setConnectionHandler(String stringURL, Object connection) {
        if (stringURL != null) {
            if (connection == null) {
                mConnections.remove(stringURL);
            }
            else {
                mConnections.put(stringURL, connection);
            }
        }
    }     
}
