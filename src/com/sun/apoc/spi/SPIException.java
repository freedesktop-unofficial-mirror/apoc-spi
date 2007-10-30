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
package com.sun.apoc.spi;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
  * Common Exception class for the registry objects.
  */
public class SPIException extends Exception
{
    /** string that maps to localized error message */
    private String mLocalizedErrorMessageCode = ERROR_OCCURRED;
    /** Static string for codes that map to localized error strings
     * @deprecated
     *  */
    public static final String ERROR_OCCURRED = "error.occurred";
    /**@deprecated */
    public static final String ERROR_OPEN_CONNECTION = 
		"error.open.connection";
    /**@deprecated */
    public static final String ERROR_CLOSE_CONNECTION = 
		"error.close.connection";
    /**@deprecated */
    public static final String ERROR_USER_INVALID = "error.user.invalid";
    /**@deprecated */
    public static final String ERROR_HOST_INVALID = "error.host.invalid";
    /**@deprecated */
    public static final String ERROR_UNSUPPORTED_DATASTORE = 
		"error.unsupported.datastore";
    /**@deprecated */
    public static final String ERROR_APPLICATION_INIT = 
		"error.application.initialize";
    /**@deprecated */
    public static final String ERROR_BOOTSTRAP_INCOMPLETE = 
		"error.bootstrap.incomplete";
    /**@deprecated */
    public static final String ERROR_BOOTSTRAP_UNAVAILABLE = 
		"error.bootstrap.unavailable";
    /**@deprecated */
    public static final String ERROR_LDAP_BOOTSTRAP = 
		"error.ldap.bootstrap";
    /**@deprecated */
    public static final String ERROR_ORG_SETTINGS = "error.org.settings";
    /**@deprecated */
    public static final String ERROR_DOMAIN_SETTINGS = "error.domain.settings";
    /**@deprecated */
    public static final String ERROR_ROLE_SETTINGS = "error.role.settings";
    /**@deprecated */
    public static final String ERROR_USER_SETTINGS = "error.user.settings";
    /**@deprecated */
    public static final String ERROR_HOST_SETTINGS = "error.host.settings";
    /**@deprecated */
    public static final String ERROR_CREATE_PARSER = "error.create.parser";
    /**@deprecated */
    public static final String ERROR_FILTER = "error.FILTER";
    /**@deprecated */
    public static final String ERROR_POLICY_READ = "error.policy.read";
    /**@deprecated */
    public static final String ERROR_POLICY_DATA_INVALID = 
			"error.policy.data.invalid";
    /**@deprecated */
    public static final String ERROR_POLICY_INVALID = 
			"error.policy.invalid";
    /**@deprecated */
    public static final String ERROR_PATH_INVALID = 
			"error.path.invalid";
    /**@deprecated */
    public static final String ERROR_USERPROFILE_ATTR = 
			"error.userprofile.attribute";
    /**@deprecated */
    public static final String ERROR_LOCALE_SUPPORT = 
			"error.locale.support";
    /**@deprecated */
    public static final String ERROR_LDAP_READ = 
			"error.ldap.read";
    /**@deprecated */
    public static final String ERROR_LDAP_WRITE = 
			"error.ldap.write";
    /**@deprecated */
    public static final String ERROR_LDAP_SEARCH = 
			"error.ldap.search";
    /**@deprecated */
    public static final String ERROR_LDAP_RESULTS_SIZE = 
			"error.ldap.results.size";
    /**@deprecated */
    public static final String ERROR_LDAP_PROFILE_CREATION = 
			"error.ldap.profile.creation";
    /**@deprecated */
    public static final String ERROR_LDAP_PROFILE_DELETION = 
			"error.ldap.profile.deletion";
    /**@deprecated */
    public static final String ERROR_LDAP_PROFILE_RENAME = 
			"error.ldap.profile.rename";
    /**@deprecated */
    public static final String ERROR_UNPROTECT_ITEM = 
			"error.unprotect.item";
    /**@deprecated */
    public static final String ERROR_READONLY_ITEM = 
			"error.readonly.item";
    /**@deprecated */
    public static final String ERROR_MANDATORY_SETTING = 
			"error.mandatory.setting";
    /**@deprecated */
    public static final String ERROR_MANDATORY_REMOVE = 
			"error.mandatory.remove";
    /**@deprecated */
    public static final String ERROR_COMPLY_CONSTRAINTS = 
			"error.comply.constraints";
    /**@deprecated */
    public static final String ERROR_NILLABLE_ITEM = 
			"error.nillable.item";
    /**@deprecated */
    public static final String ERROR_PRIORITY_EXISTS = 
			"error.profile.priority.exists";
    /**@deprecated */
    public static final String ERROR_PROFILE_NAME_EXISTS = 
			"error.profile.name.exists";
    /**@deprecated */
    public static final String ERROR_PROFILE_DOESNT_EXIST = 
			"error.profile.not.exists";
    /**@deprecated */
    public static final String ERROR_PRIORITIES_INCORRECT = 
			"error.priorities.incorrect";
    /**@deprecated */
    public static final String ERROR_PRIORITIES_INVALID = 
			"error.priorities.invalid";
    /**@deprecated */
    public static final String ERROR_EXPORT_PROFILE =
			"error.export.profile";
    /**@deprecated */
    public static final String ERROR_IMPORT_PROFILE =
			"error.import.profile";
    /**@deprecated */
    public static final String ERROR_EXPORT_PROFILE_DATA =
			"error.export.profile.data";
    /**@deprecated */
    public static final String ERROR_IMPORT_PROFILE_DATA =
			"error.import.profile.data";


    /** Registry module where the exception occurred */
    private String mModule = null ;
    /** Qualified error code depending on the module */
    private int mErrorCode = 0 ;

    /**
      * Only constructor. To avoid bogus, under-qualified
      * exceptions, all members are required.
      *
      * @param aErrorMessage     detailed exception message
      * @param aLocalizedErrorMessageCode  maps to localized error string 
      * @param aModule      module where the exception happened
      * @param aErrorCode   qualified error code for the module
      * @deprecated
      */
    public SPIException(String aErrorMessage, 
		String aLocalizedErrorMessageCode,
		String aModule, 
		int aErrorCode) {
        super(aErrorMessage) ;
	mLocalizedErrorMessageCode = aLocalizedErrorMessageCode;
        mModule = aModule ;
        mErrorCode = aErrorCode ;
    }

    /**
     * Returns the string that maps to the localized error message
     * in the resources file.
     * 
     * @return     code that maps to error message in localization
     *		   database
      * @deprecated
     */
    public String getLocalizedErrorMessageCode() { 
	return mLocalizedErrorMessageCode ; 
    }

    /**
      * Provides the name of the module where the exception occurred.
      *
      * @return module name
      * @deprecated
      */
    public String getModule() { return mModule ; }

    /**
      * Provides the qualified error code depending on the module.
      *
      * @return error code
      * @deprecated
      */
    public int getErrorCode() { return mErrorCode ; }
    
    // new stuff
    private static final String RESOURCE_BUNDLE = 
        "com.sun.apoc.spi.resources.SPIErrors";
    private static final String DEFAULT_KEY = 
        "error.spi.default";
    protected String mMessageKey = null;
    protected Object[] mMessageParams = null;
    
    /**
     * Constructs a new SPIException with null as its detail message.
     */
    public SPIException() {
        super();
        mMessageKey = DEFAULT_KEY;
    }
    
    /**
     * Constructs a new SPIException which detail message will
     * be retrieved in the error message ressource bundles
     * using the messageKey and messageParams if any.
     * 
     * @param messageKey	the key to retrieve the SPIException
     * 						message in the error message ressource 
     * 						bundles
     * @param messageParams	objects to replace corresponding patterns
     * 						in the message
     */
    public SPIException (String messageKey, Object[] messageParams) {
        mMessageKey = messageKey;
        mMessageParams = messageParams;
    }

    /**
     * Constructs a new SPIException which detail message will
     * be retrieved in the error message ressource bundles
     * using the messageKey and messageParams if any.
     * 
     * @param messageKey	the key to retrieve the SPIException
     * 						message in the error message ressource 
     * 						bundles
     * @param messageParams	objects to replace corresponding patterns
     * 						in the message
     * @param cause			the cause (which is saved for later retrieval 
     * 						by the Throwable.getCause() method). 
     * 						(A null value is permitted, and indicates 
     * 						that the cause is nonexistent or unknown.)
     */
    public SPIException (String messageKey, Object[] messageParams, 
            			 Throwable cause) {
        super(cause);
        mMessageKey = messageKey;
        mMessageParams = messageParams;
    }
    
    /**
     * Constructs a new SPIException with the specified cause
     *
     * @param cause			the cause (which is saved for later retrieval 
     * 						by the Throwable.getCause() method). 
     * 						(A null value is permitted, and indicates 
     * 						that the cause is nonexistent or unknown.)
     */
    public SPIException (Throwable cause) {
		super(cause);
	}

    /**
     * Returns the localized detail message string
     * of this SPIException, with the locale provided.
     * 
     * @param locale Locale to use to retrieve the message
     * 				 in the resource bundles.
     * @return		the localized detail message string
     * 				of this SPIException instance 
     * 				(which may be null if the messageKey was
     * 				null in the constructor or not found 
     * 				in the resource bundle).
     */
    public String getLocalizedMessage(Locale locale) {
        Locale localeToUse = locale;
        if (localeToUse == null) {
            localeToUse = Locale.getDefault();
        }
        String message = null;
        if (mMessageKey != null) {
            // get the message from the resource bundle
            ResourceBundle resBundle = ResourceBundle.getBundle(
                    						RESOURCE_BUNDLE, locale);
            message = resBundle.getString(mMessageKey);
            if (mMessageParams != null) {
                // insert params in message
                MessageFormat formatter = new MessageFormat(message,
                        									locale);
                message = formatter.format(mMessageParams).toString();
            }
        }
        return message;
    }

    /**
     * Returns the localized detail message string
     * of this SPIException, with the default Locale.
     * 
     * @return		the localized detail message string
     * 				of this SPIException instance 
     * 				(which may be null if the messageKey was
     * 				null in the constructor or not found 
     * 				in the resource bundle).
     */
    public String getLocalizedMessage() {
        Locale locale = Locale.getDefault();
        return getLocalizedMessage(locale);
    }

    /**
     * Returns the localized detail message string
     * of this SPIException, with the default Locale.
     * 
     * @return		the localized detail message string
     * 				of this SPIException instance 
     * 				(which may be null if the messageKey was
     * 				null in the constructor or not found 
     * 				in the resource bundle).
     */
    public String getMessage() {
        return getLocalizedMessage();
    }
    
    private String getDebugMessage() {
        String message = "";
        Throwable cause = this.getCause();
        if (cause != null) {
            message += "\ncause:"+cause.toString();
        }
        StackTraceElement[] stack = this.getStackTrace();
        for (int i=0; i<stack.length; i++) {
            message+="\n"+stack[i].toString();
        }
        return message;
    }
}
