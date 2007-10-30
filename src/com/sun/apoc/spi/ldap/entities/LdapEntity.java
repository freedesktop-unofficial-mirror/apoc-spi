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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Vector;

import netscape.ldap.LDAPDN;
import netscape.ldap.LDAPException;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.AbstractEntity;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;

/**
  * Class for an LDAP entity.
  */
public abstract class LdapEntity extends AbstractEntity
{
    public static final String DOMAIN_TREE_INDICATOR = "1__";
    public static final boolean CHECK_ONLY = true;
    public static final boolean NOT_CHECK_ONLY = false;
    public static final boolean DN_REQUIRED = true;
    public static final boolean DN_NOT_REQUIRED = false;
    public static final String LDAP_SEPARATOR = ",";
    public static final char DN_SEPARATOR = '=';

    LdapDataStore mDataStore;
    LdapClientContext mContext;
    LdapEntityMapping mEntityMapping;
    String mLocation;
    int mParentIndex;
    boolean mIsDomainTree;
    protected String mDisplayName ;

    /**
      * Constructor for class.
      *
      * @param aId             id entry for the user
      * @param aParentIndex    index for parent entity within id
      * @param aDataStore      datastore object
      * @param aEntityMapping  mapping object
      * @param aContext        client context
      */
    protected LdapEntity(String aId, int aParentIndex, 
            LdapDataStore aDataStore, 
            LdapEntityMapping aEntityMapping, LdapClientContext aContext) {
        mId = aId;
        mDataStore = aDataStore;
        mContext = aContext;
        mEntityMapping = aEntityMapping;
        mParentIndex = aParentIndex;
        mLocation = aId;
    }

    /** 
     * Returns the datastore used by this entity.
     *
     * @return   the datastore object 
     */
    public LdapDataStore getDataStore() {
        return mDataStore;
    }

   /** 
     * Returns the context.
     *
     * @return   the context object 
     */
    public LdapClientContext getContext() {
        return mContext;
    }

    /**
     * Sets the context.
     *
     * @param aContext           the context 
     */
    public void setContext(LdapClientContext aContext) {
        mContext = aContext;
    }

    /**
      * Tests for equality with another LdapEntity.
      *
      * @param aEntity     other entity
      * @return            <code>true</code> if both entities are
      *                    equal, otherwise <code>false</code>
      */
    public abstract boolean equals (Object aEntity) ;


    /**
      * Returns the location for this entity.
      *
      * @return           the location  
      */
    public String getLocation() {
        return mLocation;
    }


    /**
      * Returns the display name for this entity.
      *
      * @return           the display name 
      */
    public String getDisplayName(Locale aLocale) {
        if (mDisplayName == null) {
            try {
                if (this.equals(mPolicySource.getRoot())) {
                    mDisplayName = mLocation;
                }
            } catch (SPIException ignore) {}
            if (mDisplayName == null) {
                mDisplayName = getDisplayNameFromLocation();
            }
        }
        return mDisplayName;
    }
    
    /**
     * Returns an interator over the display names of the ancestors
     * of this entity, starting from the root entity down to the direct
     * parent of this entity, reflecting the entity structure.
     * 
     * The User and Host containers need to be skipped because they're
     * not real entities.
     * 
     * The ancestor name for the root is the full DN while for the other
     * entities it is only the right part of the '=' sign.
     *
     * @return           display names of the ancestors
     */
    public Iterator getAncestorNames(Locale aLocale) {
        String userContainer = mEntityMapping.mUserMapping.getContainerEntry();
        String hostContainer = mEntityMapping.mHostMapping.getContainerEntry();
        String[] rootElements = null;
        String rootOrgId = null;
        try {
            rootOrgId = mPolicySource.getRoot().getId();
            rootElements = LDAPDN.explodeDN(rootOrgId, false);
        } catch (SPIException ignore) {};
        String[] elements = LDAPDN.explodeDN(mLocation, false);
        LinkedList ancestorNames = new LinkedList();
        if (elements != null) {
            int lastElement = elements.length-1;
            if (rootElements != null) {
                lastElement = elements.length-rootElements.length-1;
                if (elements.length > rootElements.length) {
                    // the current entity isn't a root entity
                    // so add root id as first ancestor
                    ancestorNames.add(LDAPDN.unEscapeRDN(rootOrgId));
                }
            }
            for (int i=lastElement; i>0; i--) {
                String ancestor = elements [i];
                if (!ancestor.equals(userContainer) && !ancestor.equals(hostContainer)) {
                    int index = ancestor.indexOf(DN_SEPARATOR);
                    if (index >= 0) {
                        ancestorNames.add(LDAPDN.unEscapeRDN(ancestor.substring(index+1)));
                    }
                }
            }
        }
        return ancestorNames.iterator();
    }

    /**
      * Returns parent entity. 
      * 
      * @return            parent entity 
      */
    public abstract Entity getParent() ;

    /**
      * Computes a default value for the display name from the id.
      */
    private String getDisplayNameFromLocation() {
        String displayName = null;
        String elements [] = LDAPDN.explodeDN(mLocation, true) ;
        if ((elements != null) && (elements.length > 0)) {
            displayName = LDAPDN.unEscapeRDN(elements [0]) ;
        }
        return displayName;
    }

    /**
     * Returns a boolean indicating if this entity is in the domain tree.
     *
     * @return         <code>true</code> is entity is in domain tree,
     *                 otherwise <code>false</code>
     */
    public boolean isDomainTree() { return mIsDomainTree; }

    /**
     * Sets the boolean member and the id member to indicate
     * this entity is in the domain tree.
     *
     */
    protected void setIsDomainTree() { 
        mIsDomainTree = true;
    }

    /**
      * Queries the necessary attributes to either do some guesswork on the 
      * type of an entity or to build the display name of fancy entries (such
      * as users).
      */
    private Hashtable fillAttributeValues(String aDn) {
        String []attributes = new String[2 + 
                    mEntityMapping.mUserMapping.getDisplayAttributes().length];

        attributes[0] = LdapDataStore.LDAP_OBJCLASS;
        attributes[1] = mEntityMapping.mUserMapping.getUniqueAttribute();
        int index = 2;
        for (int i = 0; i < 
                mEntityMapping.mUserMapping.getDisplayAttributes().length; ++i) {
            attributes[index++] = mEntityMapping.mUserMapping.getDisplayAttributes()[i];
        }
        try {
            return getDataStore().getAttributeValueTable(
                                        aDn, true, attributes, getContext());
        } catch (LDAPException e) { return null; }
    }
    /**
      * Builds an entity from a DN.
      *
      * @param aDN                       Distinguished Name
      * @param aEntityType               type of <code>Entity</code> to create
      * @param aIncludesServiceElements  <code>true</code> if this DN
      *                                  includes service elements
      * @param aAttrsValues              table of attribute/values mappings
      * @return                          new entity or null if an error occurs.
      */
    public Entity getEntityFromDN(String aDN, LdapEntityType aEntityType,
                      boolean aIncludesServiceElements, Hashtable aAttrsValues) {
        if (aDN == null) { return null ; }
        String [] components = null ;
        int start = 0 ;
        String dn = aDN ;

        if (aIncludesServiceElements) {
            components = LDAPDN.explodeDN(aDN, false) ;
            if (components == null) { return null ; }
            start = LdapDataStore.NUMBER_OF_SERVICE_MAPPING_ELEMENTS ;
            if (start >= components.length) { return null ; }
            StringBuffer buffer = new StringBuffer(components [start]) ;
            for (int i = start + 1 ; i < components.length ; ++ i) {
                buffer.append(LDAP_SEPARATOR).append(components [i]) ;
            }
            dn = buffer.toString() ;
        }
        if (isDomainTree()) {
            if ((((LdapEntity)getDataStore().getRootDomain()).getLocation().equalsIgnoreCase(dn))) {
                return getDataStore().getRootDomain();
            }
        } else {
            if ((((LdapEntity)getDataStore().getRootOrganization()).getLocation().equalsIgnoreCase(dn))) {
                return getDataStore().getRootOrganization();
            }
        }
        if (aEntityType == LdapEntityType.UNKNOWN) {
            if (components == null) {
                components = LDAPDN.explodeDN(aDN, false) ; 
            }
            if (aAttrsValues == null) {
                aAttrsValues = fillAttributeValues(dn) ;
                if (aAttrsValues == null) { return null ; }
            }
            aEntityType = detectEntityType(components[start], aAttrsValues) ;
            if (aEntityType == LdapEntityType.UNKNOWN) { return null ; }
            if (!isCompatible(aEntityType)) { return null ; }
        }
        String container = null ;
        int containerIndex = 1 ;
        int entityType = aEntityType.getIntValue() ;
        switch (entityType) {
            case LdapEntityType.INT_USERID: 
                container = mEntityMapping.mUserMapping.getContainerEntry() ; 
                break ;
            case LdapEntityType.INT_HOST: 
                container = mEntityMapping.mHostMapping.getContainerEntry() ; 
                break ;
            case LdapEntityType.INT_ROLE:
                container = mEntityMapping.mRoleMapping.getContainerEntry() ;
                containerIndex = -1 ;
                break ;
            default: 
                containerIndex = 0 ; 
                break ;
        }
        if (container != null && container.length() == 0) { container = null ; }
        if (containerIndex == -1) {
            if (components == null) {
                components = LDAPDN.explodeDN(aDN, false) ; 
            }
            containerIndex = findContainer(components, start, container) ;
            if (containerIndex == -1) { return null ; }
            // deal with service elements
            containerIndex -= start;
        }
        Entity retCode = null;
        String uniqueIdAttr = null;
        switch (entityType) {
            case LdapEntityType.INT_ORG:
                retCode = new LdapOrganization(dn, containerIndex + 1, 
                        getDataStore(), mEntityMapping, getContext());
                break;
            case LdapEntityType.INT_DOMAIN:
                retCode = new LdapDomain(dn, containerIndex + 1, 
                        getDataStore(), mEntityMapping, getContext());
                break;
            case LdapEntityType.INT_ROLE:
                retCode = new LdapRole(dn, containerIndex + 1, 
                        getDataStore(), mEntityMapping, getContext());
                if (isDomainTree()) {
                    ((LdapRole)retCode).setIsDomainTree();
                } 
                break;
            case LdapEntityType.INT_USERID:
                retCode = new LdapUser(dn, containerIndex + 1, 
                        getDataStore(), mEntityMapping,  getContext());
                if (aAttrsValues == null) {
                    aAttrsValues = fillAttributeValues(dn) ;
                    if (aAttrsValues == null) { return null ; }
                }
                ((LdapUser)retCode).setDisplayName(aAttrsValues,
                        mEntityMapping.mUserMapping.getDisplayAttributes(),
                        mEntityMapping.mUserMapping.getDisplayFormat()) ;
                break;
            case LdapEntityType.INT_HOST:
                retCode = new LdapHost(dn, containerIndex + 1, 
                        getDataStore(), mEntityMapping, getContext());
                break;
        }
        if (retCode != null) {
            ((AbstractEntity)retCode).setPolicySource(mPolicySource);
        }
        return retCode;
     }

    private boolean isCompatible(LdapEntityType aEntityType) {
        if (mPolicySource.getName().equals(EnvironmentConstants.USER_SOURCE)) {
            if (aEntityType.equals(LdapEntityType.DOMAIN) ||
                    aEntityType.equals(LdapEntityType.HOST)) {
                return false;
            } else {
                return true;
            }
        } else if (mPolicySource.getName().equals(EnvironmentConstants.HOST_SOURCE)) {
            if (aEntityType.equals(LdapEntityType.DOMAIN) ||
                    aEntityType.equals(LdapEntityType.HOST)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    
     /**
       * Finds the type of an entity by checking its objectclass. 
       * 
       * @param aRDN          RDN of the object
       * @param aAttrValues   table containing entity objectclasses
       * @return entity type
       */
     public LdapEntityType detectEntityType(String aRDN, Hashtable aAttrValues) {
        LdapEntityType returnType = LdapEntityType.UNKNOWN;
        Vector objClasses =
                (Vector)aAttrValues.get(LdapDataStore.LDAP_OBJCLASS);
        if (objClasses == null || objClasses.isEmpty()) {
            return returnType;
        }
        Enumeration classes = objClasses.elements();
        ArrayList classList = new ArrayList();
        while (classes.hasMoreElements()) {
            classList.add(classes.nextElement());
        }
        int numOfClasses = classList.size();
        int index = 0;
        while (index < numOfClasses &&
                returnType == LdapEntityType.UNKNOWN) {
            String objClass = (String)classList.get(index);
            if (objClass.equalsIgnoreCase(mEntityMapping.mUserMapping.getObjectClass())) {
                returnType = LdapEntityType.USERID;
                break;
            } else if (objClass.equalsIgnoreCase(mEntityMapping.mHostMapping.getObjectClass())) {
                returnType = LdapEntityType.HOST;
                break;
            } else {
                String orgObjClasses[] = 
                    mEntityMapping.mOrganizationMapping.getObjectClasses();
                for (int i = 0; i < orgObjClasses.length; ++i) {
                    if (objClass.equalsIgnoreCase(
                            orgObjClasses[i])) {
                        returnType = LdapEntityType.ORG;
                        break;
                    }
                }
                if (returnType == LdapEntityType.UNKNOWN) {
                    String roleObjClasses[] = 
                        mEntityMapping.mRoleMapping.getObjectClasses();
                    for (int i = 0; i < roleObjClasses.length; ++i) {
                        if (objClass.equalsIgnoreCase(
                             roleObjClasses[i])) {
                            returnType = LdapEntityType.ROLE;
                            break;
                        }
                    }
                }
                if (returnType == LdapEntityType.UNKNOWN) {
                    String domainObjClasses[] = 
                        mEntityMapping.mDomainMapping.getObjectClasses();
                    for (int i = 0; i < domainObjClasses.length; ++i) {
                        if (objClass.equalsIgnoreCase(
                            domainObjClasses[i])) {
                            returnType = LdapEntityType.DOMAIN;
                            break;
                        }
                    }
                }
            }
            ++index;
        }
        if (returnType == LdapEntityType.USERID) {
            // ensure not a host (for Active Directory)
            for (int i = 0; i < numOfClasses; ++i) {
                String objClass = (String)classList.get(i);
                if (objClass.equalsIgnoreCase(
                            mEntityMapping.mHostMapping.getObjectClass())) {
                    returnType = LdapEntityType.HOST;
                    break;
                }
            }
        }
        return returnType;
    }


     /**
       * Returns the index of the container entry in a DN.
       * If there should be a container and it cannot be found,
       * or there shouldn't be but a real parent (org or domain)
       * cannot be found either return -1.
       *
       * @param aComponents  dn components
       * @param aStart       where to start looking in the components
       * @param aContainer   container entry or null if doesn't exist
       * @return container index or -1 if erroneous situation
       */
    private static int findContainer(String [] aComponents,
                 int aStart, String aContainer) {
        String beginning = aComponents [aStart].substring(0,
        aComponents [aStart].indexOf("=")) ;
        for (int i = aStart + 1 ; i < aComponents.length ; ++ i) {
            if (aContainer != null) {
                if (aContainer.equalsIgnoreCase(aComponents [i])) {
                    return i ;
                }
            } else if (!aComponents [i].startsWith(beginning)) {
                return i - 1 ;
            }
         }
         return -1 ;
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
    public abstract Iterator getLayeredProfiles() 
        throws SPIException ;
    
}
