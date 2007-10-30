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

package com.sun.apoc.spi.ldap.entities.mapping;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.environment.InvalidParameterException;
import com.sun.apoc.spi.environment.MissingParameterException;
import com.sun.apoc.spi.environment.RemoteEnvironmentException;
import com.sun.apoc.spi.util.MetaConfiguration;

/**
  * Class for mapping LDAP identifiers for entities.
  */
public class LdapEntityMapping
{
    public static final String VALUE_SEPARATOR = "," ;
    public static final String NAME_SEPARATOR = "/" ;

    /** key to Organization information in MetaConfiguration */
    private static final String ORG_KEY = "Organization/";
    /** key to Domain information in MetaConfiguration */
    private static final String DOMAIN_KEY = "Domain/";
    /** key to User information in MetaConfiguration */
    private static final String USER_KEY = "User/";
    /** key to Host information in MetaConfiguration */
    private static final String HOST_KEY = "Host/";
    /** key to role information in MetaConfiguration */
    private static final String ROLE_KEY = "Role/";

    public static final String OBJCLASS = "ObjectClass" ;
    public static final String NAMING_ATTR = "NamingAttribute" ;
    public static final String CONTAINER = "Container" ;
    public static final String UNIQUE_ATTR = "UniqueIdAttribute" ;
    public static final String MEMBER_ATTR = "MemberAttribute" ;
    public static final String LISTING_ATTR = "VirtualMemberAttribute" ;
    public static final String DISPLAY = "DisplayNameFormat" ;

    public ContainerMapping mOrganizationMapping = null;
    public ContainerMapping mDomainMapping = null;
    public ListMapping mRoleMapping = null;
    public UserMapping mUserMapping = null;
    public ItemMapping mHostMapping = null;

    public LdapEntityMapping(MetaConfiguration aMetaConf) 
        throws SPIException {
         mOrganizationMapping = new ContainerMapping(ORG_KEY, aMetaConf);
         mDomainMapping = new ContainerMapping(DOMAIN_KEY, aMetaConf);
         mRoleMapping = new ListMapping(ROLE_KEY, aMetaConf);
         mUserMapping = new UserMapping(USER_KEY, aMetaConf);
         mHostMapping = new ItemMapping(HOST_KEY, aMetaConf);
    }

    public static class ContainerMapping {
        String [] mObjectClasses;
        String [] mNamingAttributes;

        ContainerMapping(String aPrefix,
                        MetaConfiguration aMetaConf) throws SPIException {
            mObjectClasses = aMetaConf.getStrings(aPrefix + OBJCLASS) ;
            mNamingAttributes = aMetaConf.getStrings(aPrefix + NAMING_ATTR) ;
            if (mObjectClasses == null) {
                throw new MissingParameterException(aPrefix + OBJCLASS);
            }
            if (mNamingAttributes == null) {
                throw new MissingParameterException(aPrefix + NAMING_ATTR);
            }
            if (mObjectClasses.length != mNamingAttributes.length) {
                StringBuffer paramValue = new StringBuffer();
                for (int i=0 ; i<mNamingAttributes.length ; i++) {
                    paramValue.append(mNamingAttributes[i]);
                }
                throw new InvalidParameterException(
                        aPrefix + NAMING_ATTR, paramValue.toString());
            }
        }
        public String[] getObjectClasses() { return mObjectClasses; }
        public String[] getNamingAttributes() { return mNamingAttributes; }
    }

    public static class ListMapping extends ContainerMapping {
        String mMemberAttribute;
        String mListingAttribute;
        String mContainerEntry;

        ListMapping(String aPrefix, MetaConfiguration aMetaConf) throws SPIException {
            super(aPrefix, aMetaConf) ;
            mMemberAttribute = aMetaConf.getString(aPrefix + MEMBER_ATTR) ;
            mListingAttribute = aMetaConf.getString(aPrefix + LISTING_ATTR) ;
            if (mMemberAttribute == null) {
                throw new MissingParameterException(aPrefix + MEMBER_ATTR);
            }
            if (mListingAttribute == null) {
                throw new MissingParameterException(aPrefix + LISTING_ATTR);
            }
            mContainerEntry = aMetaConf.getString(aPrefix + CONTAINER) ;
            if (mContainerEntry != null && mContainerEntry.length() == 0) {
                mContainerEntry = null ;
            }
        }
        public String getContainerEntry() { return mContainerEntry; }
        public String getMemberAttribute() { return mMemberAttribute; }
        public String getListingAttribute() { return mListingAttribute; }
    }

    public static class ItemMapping {

        String mObjectClass = null ;
        String mUniqueAttribute = null ;
        String mContainerEntry = null ;
            
        ItemMapping(String aPrefix, MetaConfiguration aMetaConf) throws SPIException {
            mObjectClass = aMetaConf.getString(aPrefix + OBJCLASS) ;
            mUniqueAttribute = aMetaConf.getString(aPrefix + UNIQUE_ATTR) ;
            if (mObjectClass == null) {
                throw new MissingParameterException(aPrefix + OBJCLASS);
            }
            if (mUniqueAttribute == null) {
                throw new MissingParameterException(aPrefix + UNIQUE_ATTR);
            }
            mContainerEntry = aMetaConf.getString(aPrefix + CONTAINER) ;
            if (mContainerEntry != null && mContainerEntry.length() == 0) {
                mContainerEntry = null ;
            }
        }
        public String getObjectClass() { return mObjectClass; }
        public String getUniqueAttribute() { return mUniqueAttribute; }
        public String getContainerEntry() { return mContainerEntry; }
    }

    public static class UserMapping extends ItemMapping {
        String mDisplayFormat = null ;
        String [] mDisplayAttributes = null ;
        String []mSearchAttributes = null;
        static final String DEFAULT_DISPLAY_FORMAT = "sn, givenname";

        UserMapping(String aPrefix, MetaConfiguration aMetaConf) 
                throws SPIException {
            super(aPrefix, aMetaConf) ;
            
            mDisplayFormat = aMetaConf.getString(aPrefix + DISPLAY) ;
            if (mDisplayFormat == null || mDisplayFormat.length() == 0) {
                mDisplayFormat = DEFAULT_DISPLAY_FORMAT ;
            }
            StringTokenizer tokens = new StringTokenizer(mDisplayFormat, ",") ;
            int i = 0 ;
            mDisplayAttributes = new String [tokens.countTokens()] ;
            while (tokens.hasMoreTokens()) {
                mDisplayAttributes [i ++] = tokens.nextToken().trim() ;
            }
        }

        public String getDisplayFormat() {
            return mDisplayFormat;
        }

        public String[] getDisplayAttributes() {
            return mDisplayAttributes;
        }

        public String[] getSearchAttributes() {
            if (mSearchAttributes == null) {
                mSearchAttributes = new String[1];
                mSearchAttributes[0] =  mUniqueAttribute ;
            }
            return mSearchAttributes;
        }
    }
}
