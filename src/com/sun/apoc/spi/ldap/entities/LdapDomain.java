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
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.InvalidFilterException;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.util.BooleanReturnValue;

/**
  * Class for an LDAP domain entity.
  *
  */
public class LdapDomain extends LdapNode 
	implements Domain
{
    private LdapDomain mParent;

    /**
      * Constructor for class.
      *
      * @param aId             id entry for the host
      * @param aParentIndex    index for parent entity within id
      * @param aDataStore      datastore object
      * @param aEntityMapping  mapping object
      * @param aContext        client context
      */
    public LdapDomain (String aId, int aParentIndex, LdapDataStore aDataStore,
              LdapEntityMapping aEntityMapping, LdapClientContext aContext) {
        super(aId, aParentIndex, aDataStore, aEntityMapping, aContext);
        setIsDomainTree();
        if (aId.startsWith(LdapEntity.DOMAIN_TREE_INDICATOR)) {
            mLocation = 
            aId.substring(LdapEntity.DOMAIN_TREE_INDICATOR.length());
        }
    }

    /**
      * Tests for equality with another Entity.
      *
      * @param aEntity     other entity
      * @return            <code>true</code> if both entities are
      *                    equal, otherwise <code>false</code>
      */
    public boolean equals (Object aEntity) {
        if (aEntity instanceof LdapDomain) {
            return LDAPDN.equals(mLocation.toLowerCase(),
                ((LdapDomain) aEntity).mLocation.toLowerCase()) ;
        }
        return false;
    }

    /**
      * Returns the child domains, roles and hosts. 
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
      * Returns the child domains, roles and hosts. 
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
      * Returns the child domains, roles and hosts. 
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
            entities[i] = (Entity)allChildren.get(i);
        }
        return entities;
    }

    /**
      * Returns the list child domains, roles and hosts. 
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
        String searchFilter = buildClassFilter(mEntityMapping.mDomainMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.DOMAIN, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        allChildren.addAll(childrenList);
        /*
        // Commented out because there should be any role in the domain tree
        searchFilter = buildClassFilter(mEntityMapping.mRoleMapping,
                false);
        childrenList = getChildrenList (searchFilter,
                LdapEntityType.ROLE, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        allChildren.addAll(childrenList);
        */
        searchFilter = buildClassFilter(mEntityMapping.mHostMapping,
                false);
        container = mEntityMapping.mHostMapping.getContainerEntry();
        childrenList = getChildrenList (searchFilter,
                LdapEntityType.HOST, container, aCheckOnly, aReturnValue);
        if (aCheckOnly && aReturnValue.getReturnValue()) { return allChildren; }
        if (childrenList!=null) {
            allChildren.addAll(childrenList);
        }
        return allChildren;
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
      * Returns the child domains, roles and hosts. 
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
      * Returns the subdomains. 
      * 
      * @return            <code>Iterator</code> of child domains
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getSubDomains () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(mEntityMapping.mDomainMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.DOMAIN, null, false, returnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the subdomains. 
      * 
      * @return            array of subdomains 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getSubDomainsArray () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(mEntityMapping.mDomainMapping,
                false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.DOMAIN, null, false, returnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }
    /**
      * Finds the domains that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child domains
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findSubDomainsByName (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubDomainsByNameList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the subdomains that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of subdomains 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findSubDomainsByNameArray (String aFilter, 
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubDomainsByNameList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }
    /**
      * Finds the subdomains that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child domains
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findSubDomainsByFilter (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubDomainsByFilterList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the subdomains that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of subdomains 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findSubDomainsByFilterArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findSubDomainsByFilterList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Finds the hosts that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child hosts
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findHostsByName (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findHostsByNameList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the hosts that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of hosts 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findHostsByNameArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findHostsByNameList(aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }
    /**
      * Finds the hosts that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child hosts
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findHostsByFilter (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findHostsByFilterList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the hosts that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of hosts 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findHostsByFilterArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findHostsByFilterList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }


    /**
      * Returns the child hosts. 
      * 
      * @return            <code>Iterator</code> of child hosts
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getHosts () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mHostMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.HOST, mEntityMapping.mHostMapping.getContainerEntry(),
                false, returnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the child hosts. 
      * 
      * @return            array of child hosts 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getHostsArray () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mHostMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.HOST, mEntityMapping.mHostMapping.getContainerEntry(),
                false, returnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Returns the parent domain. 
      * 
      * @return            parent domain 
      */
    public Entity getParent() {
        if (mParent == null && mParentIndex != -1) {
            String [] elements = LDAPDN.explodeDN(mLocation, false) ;
            if (mParentIndex >= elements.length) { return null; }
            StringBuffer dn = new StringBuffer(elements [mParentIndex]) ;
            for (int i = mParentIndex + 1 ; i < elements.length ; ++ i) {
                dn.append(LDAP_SEPARATOR).append(elements [i]) ;
            }
            mParent= (LdapDomain)getEntityFromDN(dn.toString(), LdapEntityType.DOMAIN, false,
                        null);
        } 
        return mParent;
   }
    /**
      * Returns domains that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for domains
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of domain objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findSubDomains(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findSubDomainsByFilter(aFilter, aIsRecursive);
        } else {
            return findSubDomainsByName(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns domains that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for domains 
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of domains objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findSubDomainsArray(String aFilter, boolean aIsRecursive) 
        throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findSubDomainsByFilterArray(aFilter, aIsRecursive);
        } else {
            return findSubDomainsByNameArray(aFilter, aIsRecursive);
        }
    }


    /**
      * Finds the entities within this domain that match the
      * specified filter. The entities can be domains, roles, or
      * hosts.
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector allEntities = findHostsByNameList(aFilter, aIsRecursive);
        Vector domainEntities = findSubDomainsByNameList(aFilter, aIsRecursive);
        for (int i = 0; i < domainEntities.size(); ++i) {
            allEntities.add(domainEntities.get(i));
        }
        Vector roleEntities = findRolesByNameList(aFilter, aIsRecursive);
        for (int i = 0; i < roleEntities.size(); ++i) {
            allEntities.add(roleEntities.get(i));
        }
        return allEntities;
    }
    /**
      * Finds the domains within this domain that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child domains 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findSubDomainsByNameList(String aFilter, 
            boolean aIsRecursive) throws SPIException {
        String nameFilter = buildNameFilter(aFilter, "=", 
                mEntityMapping.mDomainMapping,  aIsRecursive);
        Vector subEntities = searchForDomains(nameFilter, aIsRecursive);
        return subEntities;
    }

    /**
      * Returns hosts that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for hosts
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of hosts objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findHosts(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findHostsByFilter(aFilter, aIsRecursive);
        } else {
            return findHostsByName(aFilter, aIsRecursive);
        }
     }

    /**
      * Returns hosts that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for hosts
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of hosts objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findHostsArray(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findHostsByFilterArray(aFilter, aIsRecursive);
        } else {
            return findHostsByNameArray(aFilter, aIsRecursive);
        }
     }

    /**
      * Finds the hosts within this domain that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child hosts 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findHostsByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String nameFilter = buildNameFilter(aFilter, "=", 
                mEntityMapping.mHostMapping, aIsRecursive);
        Vector subEntities = searchForHosts(nameFilter,aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the entities within this domain that match the
      * specified filter. The entities can be domains, roles, or
      * hosts.
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findEntitiesByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector allEntities = findHostsByFilterList(aFilter, aIsRecursive);
        Vector domainEntities = findSubDomainsByFilterList(aFilter, aIsRecursive);
        for (int i = 0; i < domainEntities.size(); ++i) {
            allEntities.add(domainEntities.get(i));
        }
        Vector roleEntities = findRolesByFilterList(aFilter, aIsRecursive);
        for (int i = 0; i < roleEntities.size(); ++i) {
            allEntities.add(roleEntities.get(i));
        }
        return allEntities;
    }

    /**
      * Finds the subdomains within this domain that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child domains 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findSubDomainsByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String mapFilter = mapFilter(aFilter,  
                mEntityMapping.mDomainMapping,  aIsRecursive);
        Vector subEntities = searchForDomains(mapFilter, aIsRecursive);
        return subEntities;
    }

    /**
      * Finds the hosts within this domain that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child hosts 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findHostsByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        String mapFilter = mapFilter(aFilter,  
                mEntityMapping.mHostMapping,  aIsRecursive);
        Vector subEntities =  searchForHosts(mapFilter, aIsRecursive);
        return subEntities;
    }

    /**
      * Returns domain entities whose attributes match
      * a given LDAP filter.
      *
      * @param aFilter      LDAP search filter
      * @param aRecursive   true if recursive search, false otherwise
      * @return             <code>Vector</code> of <code>Entity</code>s representing 
      *                     the matching domain entities
      * @throws             SPIException if error occurs 
      * @throws             IllegalReadException if error occurs 
      */
    private Vector searchForDomains(String aFilter,
                            boolean aRecursive) throws SPIException {
	    StringBuffer baseDnBuf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        String []attributes = { LdapDataStore.LDAP_OBJCLASS};
        Vector results = null;
        try {
            results = (getDataStore()).performSearch(mLocation,
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
        // are not stored under the proper container (role, or host)
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
                                    LdapEntityType.DOMAIN, false, attrsValues);
                    }
                    if (tmpEntity != null) {
	                    /* if it is a recursive search then need
                            to exclude the start domain from the results */
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
      * Returns host entities whose attributes match
      * a given LDAP filter.
      *
      * @param aFilter      LDAP search filter
      * @param aRecursive   true if recursive search, false otherwise
      * @return             <code>Vector</code/> of <code>Entity</code>s representing 
      *                     the matching entities
      * @throws             <code>SPIException</code> if error occurs 
      */
    private Vector searchForHosts(String aFilter,
                            boolean aRecursive) throws SPIException {
	    StringBuffer baseDnBuf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        String [] attributes = new String[2];
        attributes[0] = LdapEntityMapping.OBJCLASS;
        attributes[1] = 
                    mEntityMapping.mHostMapping.getUniqueAttribute();
        if (!aRecursive) {
            String container = mEntityMapping.mHostMapping.getContainerEntry();
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
        // are not stored under the proper container
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
                           LdapEntityType.HOST, false, attrsValues);
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
}
