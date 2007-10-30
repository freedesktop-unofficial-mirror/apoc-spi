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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import netscape.ldap.LDAPDN;
import netscape.ldap.LDAPException;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Role;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.util.BooleanReturnValue;

/**
  * Class for an LDAP role entity.
  *
  */
public class LdapRole extends LdapNode implements Role
{
    private Entity mParent;
    private Entity mParentOrgOrDomain; 

    public static final String ROLE_FILTER_PLUS = 
        "(" + LdapDataStore.LDAP_OBJCLASS + "=ldapsubentry)";

    /**
      * Constructor for class.
      *
      * @param aId             id entry for the user
      * @param aParentIndex    index for parent entity within id
      * @param aDataStore      datastore object
      * @param aEntityMapping  mapping object
      * @param aContext        client context
      */
    public LdapRole (String aId, int aParentIndex, LdapDataStore aDataStore,
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
        if (aEntity instanceof LdapRole) {
            return LDAPDN.equals(mLocation.toLowerCase(),
                ((LdapRole) aEntity).mLocation.toLowerCase()) ;
        }
        return false;
    }

    /**
      * Returns a boolean indicating whether or not this role
      * has members. 
      *
      * @return   <code>true</code> if there are members, otherwise
      *           <code>false</code>
      * @throws   <code>SPIException</code> if error occurs
      */
    public boolean  hasMembers() throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        getMembers(LdapEntity.CHECK_ONLY, returnValue);
        return returnValue.getReturnValue();
    }

    /**
      * Returns the members for this role.
      *
      * @return   <code>Iterator</code> of member entities
      * @throws   <code>SPIException</code> if error occurs
      */
    public Iterator getMembers() throws SPIException{
        return getMembers(LdapEntity.NOT_CHECK_ONLY, null);
    }

    /**
      * Returns the members for this role.
      *
      * @return   array of member entities
      * @throws   <code>SPIException</code> if error occurs
      */
    public Entity[] getMembersArray() throws SPIException{
        return getMembersArray(LdapEntity.NOT_CHECK_ONLY, null);
    }

    /**
      * Returns the members for this role.
      *
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are members, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to 
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return             <code>Iterator</code> of member entities
      * @throws             <code>SPIException</code> if error occurs
      */
    private Iterator getMembers(boolean aCheckOnly, BooleanReturnValue aReturnValue) 
                    throws SPIException{
        Vector entityList = null;
        entityList = searchForMembers(aCheckOnly, aReturnValue);
        return entityList.iterator();
    }

    /**
      * Returns the members for this role.
      *
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are members, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to 
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return             array of member entities
      * @throws             <code>SPIException</code> if error occurs
      */
    private Entity[] getMembersArray(boolean aCheckOnly, 
            BooleanReturnValue aReturnValue) 
                    throws SPIException{
        Vector entityList = null;
        Entity[] entities = null;
        entityList = searchForMembers(aCheckOnly, aReturnValue);
        if (!aCheckOnly && entityList != null) {
            int numOfMembers = entityList.size();
            entities = new Entity[numOfMembers];
            for (int i = 0; i < numOfMembers; ++i) {
                entities[i] = (Entity)entityList.get(i);
            }
        }
        return (entities == null) ? new Entity[0] : entities;
    }

    /**
      * Returns the child roles. 
      * 
      * @return            <code>Iterator</code> of child roles
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getChildren() throws SPIException {
        return getChildren(LdapEntity.NOT_CHECK_ONLY, null);
    }

    /**
      * Returns the child roles. 
      * 
      * @return            array of child roles
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getChildrenArray() throws SPIException {
        return getChildrenArray(LdapEntity.NOT_CHECK_ONLY, null);
    }

    /**
      * Returns the child roles. 
      * 
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are members, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to 
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return            <code>Iterator</code> of child roles
      * @throws            <code>SPIException</code> if error occurs
      */
    private Iterator getChildren (boolean aCheckOnly,
               BooleanReturnValue aReturnValue) throws SPIException {
        Vector childrenList = getChildrenList(
                buildClassFilter(mEntityMapping.mRoleMapping, false),
                LdapEntityType.ROLE, mEntityMapping.mRoleMapping.getContainerEntry(),
                aCheckOnly, aReturnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the child roles. 
      * 
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are members, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children to 
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return             array of child roles
      * @throws            <code>SPIException</code> if error occurs
      */
    private Entity[] getChildrenArray (boolean aCheckOnly,
               BooleanReturnValue aReturnValue) throws SPIException {
        Vector childrenList = getChildrenList(
                buildClassFilter(mEntityMapping.mRoleMapping, false),
                LdapEntityType.ROLE, mEntityMapping.mRoleMapping.getContainerEntry(),
                aCheckOnly, aReturnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Returns a boolean indicating whether or not this role
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
      * Returns the parent entity. The parent could be a role, an
      * organization or a domain entity.
      * 
      * @return            parent entity 
      */
    public Entity getParent() {
        if (mParent == null) {
            mParent = getParentRole();
            if (mParent == null) { 
                String [] elements = LDAPDN.explodeDN(mLocation, false) ;
                if (elements == null || mParentIndex >= elements.length) {
                    return null;
                }
                StringBuffer dn = new StringBuffer(elements [mParentIndex]) ;
                for (int i = mParentIndex + 1 ; i < elements.length ; ++ i) {
                    dn.append(LDAP_SEPARATOR).append(elements [i]) ;
                }
                mParent = getEntityFromDN(dn.toString(), LdapEntityType.UNKNOWN, false,
                        null);
            }
        }
        return mParent;
   }

    /**
      * Returns the parent organization or domain entity. 
      * 
      * @return            parent organization or domain entity 
      */
    public Entity getParentOrgOrDomain() {
        if (mParentOrgOrDomain == null) {
            mParentOrgOrDomain = getParent();
            while (mParentOrgOrDomain instanceof LdapRole) {
                mParentOrgOrDomain = mParentOrgOrDomain.getParent();
            }
        }
        return mParentOrgOrDomain;
   }

    /**
      * Returns the entity parent located between this entity and its
      * container entry (either dedicated one or marked by the parent
      * index). It will be by definition of the same type as this
      * entity.
      *
      * @return entity id representing the sub-container parent or null
      */
    private LdapRole getParentRole() {
        if (mParentIndex == -1) { return null ; }
        // The parent has to be located below the parent index.
        int potential = mParentIndex - 1 ;
        // And below the container if it exists.
        if (mEntityMapping.mRoleMapping.getContainerEntry() != null) {
            -- potential ;
        }
        // If we're back to the entry itself we lose.
        if (potential < 1) { return null ; }
        String [] elements = LDAPDN.explodeDN(mLocation, false) ;
        if (elements == null || elements.length == 0) { return null; }
        StringBuffer dn = new StringBuffer(elements [1]) ;
        for (int i = 2 ; i < elements.length ; ++ i) {
            dn.append(LDAP_SEPARATOR).append(elements [i]) ;
        }
        LdapRole retCode = new LdapRole(dn.toString(), mParentIndex - 1, 
                getDataStore(), mEntityMapping, getContext());
        return retCode;
    }

    /**
      * Searches for members of the role. Depending on the type
      * on the type of search specified by the parameter aCheckOnly,
      * the method will either just ascertain if the role has members,
      * or else return a list of <code>Entity</code>s for these children.
      *
      * @param aCheckOnly     if <code>true</code> then just checks if
      *                          there are members, otherwise returns the members
      * @param aReturnValue      if just checking if there are members then
      *                          this will indicate if there are members,
      *                          <code>true</code>, or if there are none,
      *                          <code>false</code>
      * @return                  list of member <code>Entity</code>s
      * @throws                  <code>SPIException</code> if
      *                          error occurs
      */
    private Vector searchForMembers(boolean aCheckOnly, 
            BooleanReturnValue aReturnValue)
                    throws SPIException {
        Entity startEntity = getParentOrgOrDomain();
        Vector returnList = null;
        try {
            returnList = (getDataStore()).getRoleMembers(this,
                    mEntityMapping.mUserMapping.getObjectClass(), 
                    mEntityMapping.mRoleMapping.getMemberAttribute(),
                    mEntityMapping.mUserMapping.getDisplayAttributes(),
                       aCheckOnly, aReturnValue);
            /* if just checking if role has members, and it does, then
               can return here */
            if (aCheckOnly == LdapEntity.CHECK_ONLY) {
                return new Vector();
            }
        } catch (SPIException spie) {
            int error = LdapDataStore.getLdapErrorCode(spie);
            if ( (error != LDAPException.NO_RESULTS_RETURNED)
              && (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
 	           throw spie;
	        }
        }
        Vector retCode = new Vector();
        if (returnList != null && !returnList.isEmpty()) {
            int size = returnList.size();
            Hashtable nameValues = null;
            Entity tmpEntity = null;
            for (int i = 0; i < size; i++) {
                Hashtable attrsValues = (Hashtable)returnList.get(i);
                try {
                    Vector values =
                    (Vector)attrsValues.get(LdapDataStore.DN_KEY);
                    String dN = (String)values.get(0);
                    tmpEntity = getEntityFromDN(dN,
                        LdapEntityType.USERID, false,
                        attrsValues);
                    if (tmpEntity != null) {
                        retCode.add(tmpEntity);
                    }
                    tmpEntity = null;
                } catch (Exception ignore) {
                    /* problem with id, so ignore */
                }
            }
        }
        return retCode == null ? new Vector() : retCode;
    }

    /**
      * Finds the roles within this role that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector subEntities;
        subEntities = searchForRoles(
                buildNameFilter(aFilter,
                    "=", mEntityMapping.mRoleMapping,
                    aIsRecursive),
                aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the roles within this role that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector subEntities;
        subEntities= searchForRoles(
                mapFilter(aFilter, mEntityMapping.mRoleMapping,
                    aIsRecursive), aIsRecursive);
        return subEntities;
    }

    /**
      * Returns the entity for this id. Entity can only be a role. 
      * 
      * @param aId         id string
      * @return            entity 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity getEntity(String aId) throws SPIException {
        return getEntityFromDN(aId, LdapEntityType.UNKNOWN, false, null); 
    }

    /**
     * Returns the child role for this id. 
     * 
     * @param aId         id string
     * @return            child <code>Role</code> with this id
     * @throws            <code>SPIException</code> if error occurs
     */
    public Role getRole(String aId) throws SPIException {
        return null;
    }

    /**
      * Returns an iterator over all the role parents for this role.
      * 
      * @return   iterator over all the role parents for this role.
      */
    public Iterator getAllParentRoles() {
        ArrayList parents = new ArrayList();
        LdapRole parent = getParentRole();
        while (parent != null) {
            parents.add(parent);
            parent = parent.getParentRole();
        }
        return parents.iterator();
    }

    /**
      * Returns an iterator over all the organization or
      * domain parents for this role.
      * 
      * @return   iterator over all the non-role parents for this role.
      */
    public Iterator getAllOrgOrDomainParents() {
        ArrayList parents = new ArrayList();
        Entity parent = getParentOrgOrDomain();
        while (parent != null) {
            parents.add(parent);
            parent = parent.getParent();
        }
        return parents.iterator();
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
        Iterator parents = getAllOrgOrDomainParents();
        Iterator roles = getAllParentRoles();
        return getLayeredProfiles(parents, roles);
    }

}
