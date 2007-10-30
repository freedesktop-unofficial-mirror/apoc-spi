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

/**
  * Lists environmental constants.
  */
public interface EnvironmentConstants
{
    /** Character used to separate paths in URLs.
     * value is "/" */
    public static final String URL_SEPARATOR = "/";
    
    /** parameter name for the hosts/domains source.
     * value is "HOST" */    
    public static final String HOST_SOURCE = "HOST";
    /** parameter name for the users/orgs source.
     * value is "USER" */ 
    public static final String USER_SOURCE = "USER";
    public static final String SEPARATOR = "_" ;
    // prefixes for the parameters
    /** prefix to specify a parameter for the organization datasource.
     * value is "USER_" */
    public static final String ORGANIZATION_PREFIX = USER_SOURCE + SEPARATOR ;
    /** prefix to specify a parameter for the domain datasource
     * value is "HOST_" */
    public static final String DOMAIN_PREFIX = HOST_SOURCE + SEPARATOR ;
    /** prefix for a generic entity datasource parameter. */
    public static final String ENTITY_PREFIX = "ENTITY_" ;
    /** prefix to specify a parameter for the profile datasource
     * value is "PROFILE_" */
    public static final String PROFILE_PREFIX = "PROFILE_";
    /** prefix to specify a parameter for the assignment datasource 
     * value is "ASSIGNMENT_" */
    public static final String ASSIGNMENT_PREFIX = "ASSIGNMENT_";
    // suffixes for the parameters
    /** suffix for the parameter indicating what encoding is used for the password.
     * value is "_ENCODING" */
    public static final String ENCODING_SUFFIX = "_ENCODING";

    // parameter names
    /** parameter name for the username in case of an authenticated
     * connection to the datasource.
     * value is "SECURITY_PRINCIPAL" */
    public static final String USER_KEY = "SECURITY_PRINCIPAL";
    /** parameter name for the password in case of an authenticated
     * connection to the datasource.
     * value is "SECURITY_CREDENTIALS" */
    public static final String CREDENTIALS_KEY = "SECURITY_CREDENTIALS";
    /** pamarmeter name for the type of encoding used for the password
     *  in case of an authenticated connection to the datasource.
     * value is "SECURITY_CREDENTIALS_ENCODING" */
    public static final String CREDENTIALS_ENCODING_KEY = CREDENTIALS_KEY+ENCODING_SUFFIX;
    /** parameter name for the URL specifying the
     * connection to the datasource.
     * The expected URL is of the form:
     * &lt;protocol&gt;://&lt;username&gt;@&lt;host&gt;:&lt;port&gt;/&lt;path&gt;.
     * value is "PROVIDER_URL" */
    public static final String URL_KEY = "PROVIDER_URL";
    /** parameter name for the java object providing access to the
     * datasource.
     * value is "PROVIDER_CLASS" */
    public static final String CLASS_KEY = "PROVIDER_CLASS";
    /** parameter name for the list of available sources
     * value is "Sources" */
    public static final String SOURCES_KEY = "Sources";    
    /** parameter name for the maximum number of results given
     * by any search in any of the datasources.
     * value is "SizeLimit" */
    public static final String MAX_SEARCH_RESULTS = "SizeLimit";
    
    // parameter values
    /** indicates that the password has been scrambled.
     * value is "scramble".
     */
    public static final String SCRAMBLE_ENCODING = "scramble";
    /** indicates that the password appears in clear text.
     * value is "none".
     */
    public static final String NONE_ENCODING = "none";
     
    // protocol defining the URLs
    /** protocol to define a LDAP URL.
     * The expected LDAP URL is of the form:
     * ldap://&lt;username&gt;@&lt;host&gt;:&lt;port&gt;/&lt;ldap_baseDN&gt;
     */
    public static final String LDAP_URL_PROTOCOL = "ldap";
     
    /** protocol to define a secure LDAP URL.
     * The expected LDAP URL is of the form:
     * ldaps://&lt;username&gt;@&lt;host&gt;:&lt;port&gt;/&lt;ldap_baseDN&gt;
     */
    public static final String LDAPS_URL_PROTOCOL = "ldaps";
     
    /** protocol to define a file URL.
     * The expected file URL is of the form:
     * file:///&lt;path&gt;
     */
    public static final String FILE_URL_PROTOCOL = "file";
     
    /** protocol to define an HTTP URL.
     * The expected file URL is of the form:
     * http://&lt;host&gt;[:]&lt;port&gt;/&lt;path&gt;
     */
    public static final String HTTP_URL_PROTOCOL = "http";
     
    /** protocol to define an HTTPS URL.
     * The expected file URL is of the form:
     * https://&lt;host&gt;[:]&lt;port&gt;/&lt;path&gt;
     */
    public static final String HTTPS_URL_PROTOCOL = "https";
    
    
    // Ldap specific keys
    /** Ldap specific prefix to specify a parameter 
     *  for the Ldap metaconfiguration datasource.
     * value is  "LDAP_META_CONF_"*/
    public static final String LDAP_META_CONF_PREFIX = "LDAP_META_CONF_";
    /** Ldap specific key for AuthDn.
     * value is "AuthDn"*/
    public static final String LDAP_AUTH_USER_KEY = "AuthDn" ;
    /** Ldap specific key for authentication password.
     * value is "Password"*/
    public static final String LDAP_AUTH_PASSWORD_KEY = "Password" ;
    /** Ldap specific key for the encoding type for authentication password.
     * value is "Password_ENCODING"*/
    public static final String LDAP_AUTH_PWD_ENCODING_KEY = LDAP_AUTH_PASSWORD_KEY+ENCODING_SUFFIX ;
    /** Ldap specific key for authentication type.
     * value is "AuthType"*/
    public static final String LDAP_AUTH_TYPE_KEY  = "AuthType";
    /** Ldap specific key for connection creation timeout.
     * value is "ConnectTimeout" */
    public static final String LDAP_TIMEOUT_KEY = "ConnectTimeout";
    /** Ldap specific key for authentication callback handler.
     * value is "AuthCallbackHandler" */
    public static final String LDAP_AUTH_CBH = "AuthCallbackHandler";

    // Ldap specific values
    /** Ldap specific value for Anonymous authentication type.
     * value is "Anonymous" */
    public static final String LDAP_AUTH_TYPE_ANONYMOUS = "Anonymous";
    /** Ldap specific value for GSSAPI authentication type.
     * value is "GSSAPI" */
    public static final String LDAP_AUTH_TYPE_GSSAPI = "GSSAPI";
    /** Ldap specific value for default connection timeout.
     * value is 1 */
    public static final int LDAP_DEFAULT_TIMEOUT = 1;
    /** Ldap specific value for default server.
     * value is "localhost" */
    public static final String LDAP_DEFAULT_SERVER = "localhost";
    /** Ldap specific value for default port.
     * value is 389 */
    public static final int LDAP_DEFAULT_PORT = 389;
    /** Ldap specific value for anonymous authentication user name .
     * value is "Anonymous" */
    public static final String LDAP_USER_ANONYMOUS = "Anonymous";
     
    // File specific constants
    /** Name of the Ldap Organization Mapping file.
     * value is "OrganizationMapping.properties" */
    public static String ORG_MAP_FILE = "OrganizationMapping.properties";
}
