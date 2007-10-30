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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import netscape.ldap.LDAPDN;
import netscape.ldap.LDAPException;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.InvalidFilterException;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.util.BooleanReturnValue;

/**
  * Class for an LDAP organization entity.
  *
  */
public class LdapOrganization extends LdapNode implements Organization
{ 
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
    public LdapOrganization (String aId, int aParentIndex, LdapDataStore aDataStore,
               LdapEntityMapping aEntityMapping, LdapClientContext aContext) {
        super(aId, aParentIndex, aDataStore, aEntityMapping, aContext);
    }

    /**
      * Tests for equality with another Entity.
      *
      * @param aEntity     other entity
      * @return            <code>true</code> if both entities are
      *                    equal, otherwise <code>false</code>
      */
    public boolean equals (Object aEntity) {
        if (aEntity instanceof LdapOrganization) {
            return LDAPDN.equals(mLocation.toLowerCase(),
                ((LdapOrganization) aEntity).mLocation.toLowerCase()) ;
        }
        return false;
    }

    /**
      * Returns the child organizations, roles and users. 
      * 
      * @return            <code>Iterator</code> of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getChildren() throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        return getChildren(LdapEntity.NOT_CHECK_ONLY, returnValue);
    }

    /**
      * Returns the child organizations, roles and users. 
      * 
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are children, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return            <code>Iterator</code> of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getChildren(boolean aCheckOnly, 
        BooleanReturnValue aReturnValue) throws SPIException {
        Vector allChildren = getAllChildrenList(aCheckOnly, aReturnValue);
        return allChildren.iterator();
    }

    /**
      * Returns the child organizations, roles and users. 
      * 
      * @return            array of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getChildrenArray() throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        return getChildrenArray(LdapEntity.NOT_CHECK_ONLY, returnValue);
    }

    /**
      * Returns the child organizations, roles and users. 
      * 
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are children, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return             array of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getChildrenArray(boolean aCheckOnly, 
        BooleanReturnValue aReturnValue) throws SPIException {
        Vector allChildren = getAllChildrenList(aCheckOnly, aReturnValue);
        Entity[] entities = new Entity[allChildren.size()];
        for (int i = 0; i < allChildren.size(); ++i) {
            Object tester = allChildren.get(i);
            entities[i] = (Entity)allChildren.get(i);
        }
        return entities;
    }

    /**
      * Returns a boolean indicating whether or not this entity
      * has children. 
      *
      * @return   <code>true</code> if there are children, otherwise
      *           <code>false</code>
      * @throws   <code>SPIException</code> if error occurs
      */
    public boolean  hasChildren() throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        getChildren(LdapEntity.CHECK_ONLY, returnValue);
        return returnValue.getReturnValue();
    }


    /**
      * Returns the list child organizations, roles and users. 
      * 
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are children, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return            <code>Vector</code> of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Vector getAllChildrenList(boolean aCheckOnly, 
        BooleanReturnValue aReturnValue) throws SPIException {
        Vector allChildren = new Vector();
        String container = null;
        String searchFilter = buildClassFilter(
                mEntityMapping.mOrganizationMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.ORG, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        allChildren.addAll(childrenList);
        searchFilter = buildClassFilter(
                mEntityMapping.mRoleMapping,
                false);
        childrenList = getChildrenList (searchFilter,
                LdapEntityType.ROLE, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        allChildren.addAll(childrenList);
        searchFilter = buildClassFilter(
                mEntityMapping.mUserMapping,
                false);
        container = 
            mEntityMapping.mUserMapping.getContainerEntry();
        childrenList = getChildrenList (searchFilter,
                LdapEntityType.USERID, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        if (childrenList!=null) {
            allChildren.addAll(childrenList);
        }
        return allChildren;
    }


    /**
      * Returns the suborganizations. 
      * 
      * @return            <code>Iterator</code> of child organizations
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getSubOrganizations () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mOrganizationMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.ORG, null, false, returnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the suborganizations. 
      * 
      * @return            array of suborganizations 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getSubOrganizationsArray () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mOrganizationMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.ORG, null, false, returnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Returns the users. 
      * 
      * @return            <code>Iterator</code> of child users
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getUsers () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mUserMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.USERID, 
                mEntityMapping.mUserMapping.getContainerEntry(),
                false, returnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the users. 
      * 
      * @return            array of child users 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getUsersArray () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mUserMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.USERID, 
                mEntityMapping.mUserMapping.getContainerEntry(),
                false, returnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Finds the suborganizations that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child organizations
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findSubOrganizationsByName (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubOrganizationsByNameList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the suborganizations that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of suborganizations 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findSubOrganizationsByNameArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubOrganizationsByNameList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }
    /**
      * Finds the suborganizations that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child organizations
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findSubOrganizationsByFilter (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubOrganizationsByFilterList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the suborganizations that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of suborganizations 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findSubOrganizationsByFilterArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubOrganizationsByFilterList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Finds the users that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child users
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findUsersByName (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findUsersByNameList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the users that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of users 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findUsersByNameArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findUsersByNameList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }
    /**
      * Finds the users that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child users
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findUsersByFilter (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findUsersByFilterList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the users that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of users 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findUsersByFilterArray (String aFilter, 
            boolean aIsRecursive) 
        throws SPIException {
        Vector childrenList = 
            findUsersByFilterList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Returns the parent organization. 
      * 
      * @return            parent organization 
      */
    public Entity getParent() {
        if (mParent == null && mParentIndex != -1) {
            String [] elements = LDAPDN.explodeDN(mLocation, false) ;
            if (elements == null ||
                    mParentIndex >= elements.length) { 
                return null; 
            }
            StringBuffer dn = new StringBuffer(elements [mParentIndex]) ;
            for (int i = mParentIndex + 1 ; i < elements.length ; ++ i) {
                dn.append(LDAP_SEPARATOR).append(elements [i]) ;
            }
            mParent = (LdapOrganization) getEntityFromDN(dn.toString(), LdapEntityType.ORG, false,
                        null);
        }
        return mParent;
   }

    /**
      * Finds the entities within this organization that match the
      * specified filter. The entities can be organizations, roles, or
      * users.
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
       Vector allEntities = findUsersByNameList(aFilter, aIsRecursive);
       Vector orgEntities = findSubOrganizationsByNameList(aFilter, 
               				aIsRecursive);
       for (int i = 0; i < orgEntities.size(); ++i) { 
            allEntities.add(orgEntities.get(i));
        }
       Vector roleEntities = findRolesByNameList(aFilter, aIsRecursive);
       for (int i = 0; i < roleEntities.size(); ++i) { 
            allEntities.add(roleEntities.get(i));
        }
        return allEntities;
    }

    /**
      * Finds the organizations within this organization that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findSubOrganizationsByNameList(String aFilter, 
            boolean aIsRecursive) throws SPIException {
        String nameFilter = buildNameFilter(aFilter, "=",
                mEntityMapping.mOrganizationMapping, aIsRecursive);
        Vector subEntities = searchForOrganizations(nameFilter,
                									aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the users within this organization that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findUsersByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String nameFilter = buildNameFilter(aFilter, "=", 
                mEntityMapping.mUserMapping,  aIsRecursive);
        Vector subEntities = searchForUsers(nameFilter, aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the entities within this organization that match the
      * specified filter. The entities can be organizations, roles, or
      * users.
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector allEntities = findUsersByFilterList(aFilter, aIsRecursive);
        Vector orgEntities =
             findSubOrganizationsByFilterList(aFilter, aIsRecursive);
        for (int i = 0; i < orgEntities.size(); ++i) {
            allEntities.add(orgEntities.get(i));
        }
        Vector roleEntities = findRolesByFilterList(aFilter, aIsRecursive);
        for (int i = 0; i < roleEntities.size(); ++i) {
            allEntities.add(roleEntities.get(i));
        }
        return allEntities;
    }

    /**
      * Finds the suborganizations within this organization that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child organizations 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findSubOrganizationsByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String mapFilter = mapFilter(aFilter,  
                mEntityMapping.mOrganizationMapping,  aIsRecursive);
        Vector subEntities = searchForOrganizations(mapFilter,
                									aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the users within this organization that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child users 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findUsersByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String mapFilter = mapFilter(aFilter,  
                mEntityMapping.mUserMapping,  aIsRecursive);
        Vector subEntities = searchForUsers(mapFilter, aIsRecursive);
        return subEntities;
    }

    /**
      * Returns organization entities whose attributes match
      * a given LDAP filter.
      *
      * @param aFilter      LDAP search filter
      * @param aRecursive   true if recursive search, false otherwise
      * @return             <code>Vector</code> of <code>EntityId</code>s representing 
      *                     the matching entities
      * @throws             SPIException if error occurs 
      * @throws             IllegalReadException if error occurs 
      */
    private Vector searchForOrganizations(String aFilter,
                            boolean aRecursive) throws SPIException {
	    StringBuffer baseDnBuf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        String []attributes = { LdapDataStore.LDAP_OBJCLASS};
        Vector results =null;
        try {
            results = getDataStore().performSearch(mLocation,
                        aRecursive, aFilter,
                        attributes, LdapEntity.DN_REQUIRED, 
                        LdapEntity.NOT_CHECK_ONLY,
                        null, false, getContext()) ;
        } catch (IllegalReadException ire) {
 	        if (LdapDataStore.getLdapErrorCode(ire)
	                == LDAPException.NO_SUCH_OBJECT) {
		        return new Vector();
	        }
	        throw ire;
        }
        // We have to ensure every result is actually mappable
        // into a proper entity. What can happen is that we get
        // entries that conform to the filter but that for instance
        // are not stored under the proper container (role, user or host)
        // and so must be ignored by us.
        Vector retCode = new Vector();
        if (!results.isEmpty()) { 
	        int size = results.size();
	        Entity tmpEntity = null;
            for (int i = 0 ; i < size ; i++) {
	            try {
                    Hashtable attrsValues = (Hashtable)results.get(i);
                    Vector values = 
			            (Vector)attrsValues.get(LdapDataStore.DN_KEY);
                    if (values != null && !values.isEmpty()) {
		                tmpEntity = getEntityFromDN((String) values.get(0),
                                    LdapEntityType.ORG, false, attrsValues);
                    }
                    if (tmpEntity != null) {
	                    /* if it is a recursive search then need
                            to exclude the start organization from 
                            the results */
                        boolean excludeThisEntity = aRecursive;
		                if (excludeThisEntity) {
		                    if (!this.equals(tmpEntity)) {
                                retCode.add(tmpEntity) ;
		                    } else {
		                        // have excluded the start entity
		                        excludeThisEntity = false;
		                    }
		                } else {
                            retCode.add(tmpEntity) ;
		                }
                    }
                } catch (Exception ignore) {
	                /* cannot convert DN to entity, so omit from list */
                }
             }
        }
        return retCode;
    }

    /**
      * Returns user entities whose attributes match
      * a given LDAP filter.
      *
      * @param aFilter      LDAP search filter
      * @param aRecursive   true if recursive search, false otherwise
      * @return             <code>Vector</code/> of <code>Entity</code>s representing 
      *                     the matching entities
      * @throws             SPIException if error occurs 
      * @throws             IllegalReadException if error occurs 
      */
    private Vector searchForUsers(String aFilter,
                            boolean aRecursive) throws SPIException {
	    StringBuffer baseDnBuf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        String [] attributes =
        new String[2 + mEntityMapping.mUserMapping.getDisplayAttributes().length];
        attributes[0] = LdapDataStore.LDAP_OBJCLASS;
        attributes[1] = 
             mEntityMapping.mUserMapping.getUniqueAttribute();
        int index = 2;
        for (int i = 0; i < 
            mEntityMapping.mUserMapping.getDisplayAttributes().length; ++i) {
            attributes[index++] = 
             mEntityMapping.mUserMapping.getDisplayAttributes()[i];
        }
        if (!aRecursive) {
            String container = mEntityMapping.mUserMapping.getContainerEntry();
            if (container != null && container != "") {
                baseDnBuf.append(container).append(LdapEntity.LDAP_SEPARATOR);
            }
        }
        baseDnBuf.append(mLocation);
        Vector results = null;
        try {
            results = getDataStore().performSearch(
                        baseDnBuf.toString(),
                        aRecursive, aFilter,
                        attributes, LdapEntity.DN_REQUIRED, 
                        LdapEntity.NOT_CHECK_ONLY,
                        null, false, getContext()) ;
        } catch (IllegalReadException ire) {
 	        if (LdapDataStore.getLdapErrorCode(ire)
	                == LDAPException.NO_SUCH_OBJECT) {
		        return new Vector();
	        }
	        throw ire;
        }
        // We have to ensure every result is actually mappable
        // into a proper entity. What can happen is that we get
        // entries that conform to the filter but that for instance
        // are not stored under the proper container (role, user or host)
        // and so must be ignored by us.
        Vector retCode = new Vector();
        if (!results.isEmpty()) { 
	        int size = results.size();
	        Entity tmpEntity = null;
            for (int i = 0 ; i < size ; i++) {
	            try {
                    Hashtable attrsValues = (Hashtable)results.get(i);
                    Vector values = 
			            (Vector)attrsValues.get(LdapDataStore.DN_KEY);
                    if (values != null && !values.isEmpty()) {
                        tmpEntity = getEntityFromDN((String) values.get(0),
                           LdapEntityType.USERID, false, attrsValues);
                    }
                    if (tmpEntity != null) {
                        retCode.add(tmpEntity);
                    }
                } catch (Exception ignore) {
                    /* cannot convert DN to entity, so omit from list */
                }
           }
       }
       return retCode;
   }  

    /**
      * Returns organizations that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for organizations
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of organization objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findSubOrganizations(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findSubOrganizationsByFilter(aFilter, aIsRecursive);
        } else {
            return findSubOrganizationsByName(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns organizations that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for organizations 
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of organizations objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findSubOrganizationsArray(String aFilter, boolean aIsRecursive) 
        throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findSubOrganizationsByFilterArray(aFilter, aIsRecursive);
        } else {
            return findSubOrganizationsByNameArray(aFilter, aIsRecursive);
        }
    }


    /**
      * Returns users that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for users
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of user objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findUsers(String aFilter, boolean aIsRecursive) throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findUsersByFilter(aFilter, aIsRecursive);
        } else {
            return findUsersByName(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns users that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for users 
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of user objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findUsersArray(String aFilter, boolean aIsRecursive) throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findUsersByFilterArray(aFilter, aIsRecursive);
        } else {
            return findUsersByNameArray(aFilter, aIsRecursive);
        }
    }
}
