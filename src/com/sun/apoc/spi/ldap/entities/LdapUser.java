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

package com.sun.apoc.spi.ldap.entities;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import netscape.ldap.LDAPDN;
import netscape.ldap.LDAPException;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.User;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.ldap.profiles.LdapUserProfile;
import com.sun.apoc.spi.profiles.Profile;

/**
  * Interface for an entity.
  *
  */
public class LdapUser extends LdapEntity implements User
{
    private static final String USER_ID_KEY = "uid";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private LdapOrganization mParent;

    /**
      * Constructor for class.
      *
      * @param aId             id entry for the user
      * @param aParentIndex    index for parent entity within id
      * @param aDataStore      datastore object
      * @param aEntityMapping  mapping object
      * @param aContext        client context
      */
    public LdapUser (String aId, int aParentIndex, LdapDataStore aDataStore,
                 LdapEntityMapping aEntityMapping, LdapClientContext aContext) {
        super(aId, aParentIndex, aDataStore, aEntityMapping, aContext);
    }

    /**
     * Returns the userid for this user.
     *
     * @return           the userid 
     */
    public String getUserId() {
        String uniqueIdKey = mEntityMapping.mUserMapping.getUniqueAttribute();
        if ((uniqueIdKey == null) || (uniqueIdKey.equals(""))) {
            uniqueIdKey = USER_ID_KEY;
        }
        uniqueIdKey += KEY_VALUE_SEPARATOR;
        String uid = new String("");
        String [] elements = LDAPDN.explodeDN(mLocation, true);
        if ((elements != null) && (elements.length > 0)) {
            uid = LDAPDN.unEscapeRDN(elements[0]) ;
        }
        return uid;
    }

    /**
      * Tests for equality with another Entity.
      *
      * @param aEntity     other entity
      * @return            <code>true</code> if both entities are
      *                    equal, otherwise <code>false</code>
      */
    public boolean equals (Object aEntity) {
        if (aEntity instanceof LdapUser) {
            return LDAPDN.equals(mLocation.toLowerCase(),
                    ((LdapUser) aEntity).mLocation.toLowerCase()) ;
        }
        return false;
    }       

    /**
      * Computes and sets the display name from a list of attributes and
      * their values.
      *
      * @param aValues      attribute to value hashtable
      * @param aAttributes  attributes to look for
      * @param aFormat      display format to use
      */
    public void setDisplayName(Hashtable aValues, String [] aAttributes,
                                               String aFormat) {
        if (aValues == null || aAttributes == null || aFormat == null) {
            return ;
        }
        StringBuffer displayName = new StringBuffer() ;
        boolean appended = false;
        for (int i = 0 ; i < aAttributes.length ; ++ i) {
            Vector values = (Vector)aValues.get(aAttributes[i]);
            String value = null;
            if (values != null && ! values.isEmpty()) {
                value = (String) values.get(0) ;
            }
            if (value != null) {
                if (appended)  { displayName.append(", ") ; }
                displayName.append(value) ;
                appended = true;
            }
        }
        if (displayName.length() > 0) {
            mDisplayName = displayName.toString() ;
        }
    }

    /**
      * Returns the parent organization entity. 
      * 
      * @return            parent organization entity 
      */
    public Entity getParent() {
        if (mParent == null) {
            String [] elements = LDAPDN.explodeDN(mLocation, false) ;
            if (elements == null || mParentIndex >= elements.length) { 
                return null;
            }
            StringBuffer dn = new StringBuffer(elements [mParentIndex]) ;
            for (int i = mParentIndex + 1 ; i < elements.length ; ++ i) {
                dn.append(LDAP_SEPARATOR).append(elements [i]) ;
            }
            mParent = (LdapOrganization) getEntityFromDN(dn.toString(), 
                                                         LdapEntityType.ORG, 
                                                         false, null) ;

        }
        return mParent;
   }

    /**
      * Return list of roles of which this user is a member.
      *
      * @return   <code>Iterator</code> listing roles
      * @throws   <code>SPIException</code> if error occurs
      */
    public Iterator getMemberships() throws SPIException {
        return getListOfRoles().iterator();
    }

    /**
      * Return list of roles of which this user is a member.
      *
      * @return   array listing roles
      * @throws   <code>SPIException</code> if error occurs
      */
    public Entity[] getMembershipsArray()  throws SPIException {
        Vector roleList = getListOfRoles();
	    Entity[] entities = new Entity[roleList.size()];
	    for (int i = 0; i < entities.length; ++i) {
		    entities[i] = (Entity)roleList.get(i);
	    } 
	    return entities;
    }

    /**
     * Returns the roles of which this user is a member. Roles 
     * are found by reading the computed user attribute 
     *
     * @return                <code>Vector</code> of roles 
     * @throws                <code>SPIException</code> if error occurs
     */
    private Vector getListOfRoles() throws SPIException {
	    Vector roleList = getDataStore().getListedRolesForEntity(
			this, mEntityMapping.mRoleMapping.getListingAttribute(), 
			getContext());
        Vector retCode = new Vector();
	    if (!roleList.isEmpty()) { 
	        int size = roleList.size();
	        Entity tmpEntity = null;
            for (int i = 0; i < size; i++) {
	            try {
		            /* read the entry for this role in order
                       to obtain corret display name for Entity */
		            String dn = getDataStore().readEntryDN(
	                    		(String)roleList.get(i), getContext());
	                tmpEntity = getEntityFromDN((String)roleList.get(i),
				        LdapEntityType.ROLE, false, null);
                    if (tmpEntity != null) {
                        retCode.add(tmpEntity);
                    }
                } catch (SPIException ignore) {
	                /* problem converting this DN, so omit it */
	            }
	        }
        }
        return retCode;
	}

    /**
     * Returns an iterator of profiles 
     * that contribute to this entity's configuration data. 
     * The first element of the iterator has the lowest priority, 
     * the last element has the highest priority.
     *
     * @return                  iterator of hierarchical profiles
     * @throws                  <code>SPIException</code> if error
     *                          occurs
     */
    public Iterator getLayeredProfiles() 
        throws SPIException {
        Iterator parents = getAllParents();
        Iterator roles = getMemberships();
        return getLayeredProfiles(parents, roles);
    }

    /**
     * Returns the virtual user profile for this user
     * containing the values of the user's ldap attributes
     *
     * @return           the user profile 
     * @throws           <code>SPIException</code> if error occurs
     */
    public Profile getUserProfile() throws SPIException {
        LdapUserProfile userProfile = null;
        Hashtable ldapAttributeTable = null;
        try {
            ldapAttributeTable = 
                    getDataStore().readAllAttributes(mLocation, getContext());
        } catch (LDAPException ldape) {
            throw new IllegalReadException(
                    IllegalReadException.LDAP_READ_KEY, ldape);
        }

        if (ldapAttributeTable != null) {
            userProfile = new LdapUserProfile(this, ldapAttributeTable);
        }
        return userProfile;
    }
}
