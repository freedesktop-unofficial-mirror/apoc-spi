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

import netscape.ldap.LDAPException;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.entities.InvalidFilterException;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.util.BooleanReturnValue;

/**
  * Abstract class for an LDAP entity container.
  */
public abstract class  LdapNode extends LdapEntity 
	implements Node
{
    public static final String MODULE = "LdapNode";

    public static final String SEARCH_FILTER_DELIMITER = "(";

    /**
      * Constructor for class.
      *
      * @param aId             id entry for the user
      * @param aParentIndex    index for parent entity within id
      * @param aDataStore      datastore object
      * @param aEntityMapping  mapping object
      * @param aContext        client context
      */
    public LdapNode (String aId, int aParentIndex, 
            LdapDataStore aDataStore, LdapEntityMapping aEntityMapping, 
            LdapClientContext aContext) {
        super(aId, aParentIndex, aDataStore, aEntityMapping, aContext);
    }


    /**
      * Returns a list of children of the specified type.
      *
      * @param aSearchFilter filter for search
      * @param aChildType   type of child required
      * @param aContainer   name of container for children
      * @param aCheckOnly   <code>true</code> if just checking
      *                     there are children of the specified
      *                     type, otherwise <code>false</code>
      * @param aReturnValue used if just checking if there are children
      *                     of the specified type to
      *                     return a value of <code>true</code> or
      *                     <code>false</code>
      * @return            <code>Vector</code> of children
      * @throws            <code>SPIException</code> if error occurs
      */
    public Vector getChildrenList (String aSearchFilter, LdapEntityType aChildType,
                 String aContainer, boolean aCheckOnly,
                  BooleanReturnValue aReturnValue)
                throws SPIException {
        Vector childrenList = null;
        String []attributes = null;
        switch (aChildType.getIntValue()) {
            case LdapEntityType.INT_USERID:
                attributes =
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
                break;
            case LdapEntityType.INT_HOST:
                attributes = new String[2];
                attributes[0] = LdapEntityMapping.OBJCLASS;
                attributes[1] = 
                    mEntityMapping.mHostMapping.getUniqueAttribute();
                break;
             default:
                attributes = new String[1];
                attributes[0] = LdapEntityMapping.OBJCLASS;
        }
        try {
            childrenList = getDataStore().getListOfChildren(
                    this, aContainer,
                    aSearchFilter, LdapDataStore.NON_RECURSIVE_SEARCH, attributes,
                          aCheckOnly, getContext(), aReturnValue);
        } catch (SPIException spie) {
            int error = LdapDataStore.getLdapErrorCode(spie);
            if ( (error != LDAPException.NO_RESULTS_RETURNED)
              && (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
 	           throw spie;
	        }
        }
        if (aCheckOnly) { return childrenList ; }
        Vector retCode = new Vector();
        if (childrenList != null && !childrenList.isEmpty()) {
            int size = childrenList.size();
            Entity tmpEntity = null;
            for (int i = 0; i < size; i++) {
                try {
                    Hashtable attrsValues = (Hashtable)childrenList.get(i);
                    Vector values = (Vector)attrsValues.get(LdapDataStore.DN_KEY);
                    if (values != null && !values.isEmpty()) {
                        String dN = (String)values.get(0);
                        tmpEntity = getEntityFromDN(dN, aChildType,
                                false, attrsValues);
                        if (tmpEntity != null) {
                            retCode.add(tmpEntity);
                        }
                    }
                } catch (Exception ignore) {
                    /* problem converting this DN, so omit it */
                }
            }
         }
        return retCode == null ? new Vector() : retCode;
    }

    /**
      * Returns contained entities that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for entities
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            <code>Iterator</code> of entity objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findEntities(String aFilter, boolean aIsRecursive) throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findEntitiesByFilter(aFilter, aIsRecursive);
        } else {
            return findEntitiesByName(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns contained entities that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for entities
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of entity objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findEntitiesArray(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findEntitiesByFilterArray(aFilter, aIsRecursive);
        } else {
            return findEntitiesByNameArray(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns contained entities that match the specified name.
      *
      * @param aFilter     the filter to use in searching for entities
      * @param aIsRecursive <code>true</code> if recursive search, otherwise
      *                     <code>false</code>
      * @return            <code>Iterator</code> of entity objects
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator findEntitiesByName(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector entities = findEntitiesByNameList(aFilter, aIsRecursive);
        return entities.iterator();
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
    public Entity[] findEntitiesByNameArray(String aFilter, boolean aIsRecursive)
                  throws SPIException {
        Vector entities = findEntitiesByNameList(aFilter, aIsRecursive);
        int size = entities.size();
        Entity []entitiesArray = new Entity[size];
        for (int i = 0; i < size; ++i) {
            entitiesArray[i] = (Entity)entities.get(i);
        }
        return entitiesArray;
     }

    /**
      * Finds the roles within this container that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child roles 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findRolesByNameList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
       Vector subEntities = 
            subEntities = searchForRoles(
                buildNameFilter(aFilter, "=", 
                    mEntityMapping.mRoleMapping,  aIsRecursive),
                aIsRecursive);
        return subEntities;
    }
    /**
      * Finds the roles within this container that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child roles 
      * @throws             <code>SPIException</code> if error occurs
      */
    public Vector findRolesByFilterList(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector subEntities =  searchForRoles(
                mapFilter(aFilter,  
                    mEntityMapping.mRoleMapping,  aIsRecursive),
                aIsRecursive);
        return subEntities;
    }

    /**
      * Returns contained entities that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for entities
      * @param aIsRecursive <code>true</code> if recursive search, otherwise
      *                     <code>false</code>
      * @return            <code>Iterator</code> of entity objects
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator findEntitiesByFilter(String aFilter, boolean aIsRecursive) 
        throws SPIException {
        Vector entities = findEntitiesByFilterList(aFilter, aIsRecursive);
        return entities.iterator();
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
    public Entity[] findEntitiesByFilterArray(String aFilter, boolean aIsRecursive)
                  throws SPIException {
        Vector entities = findEntitiesByFilterList(aFilter, aIsRecursive);
        int size = entities.size();
        Entity []entitiesArray = new Entity[size];
        for (int i = 0; i < size; ++i) {
            entitiesArray[i] = (Entity)entities.get(i);
        }
        return entitiesArray;
    }

    /**
      * Finds the entities within this container entity that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities
      * @throws             <code>SPIException</code> if error occurs
      */
    public abstract Vector findEntitiesByNameList(String aFilter, 
            boolean aIsRecursive) throws SPIException ;

    /**
      * Finds the entities within this container entity that match the
      * specified filter. 
      *
      * @param aFilter      the search filter
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             <code>Iterator</code> of child entities
      * @throws             <code>SPIException</code> if error occurs
      */
    public abstract Vector findEntitiesByFilterList(String aFilter, 
            boolean aIsRecursive) throws SPIException ;
    /**
      * Returns roles that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for roles
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of roles objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Iterator findRoles(String aFilter, boolean aIsRecursive) 
        throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findRolesByFilter(aFilter, aIsRecursive);
        } else {
            return findRolesByName(aFilter, aIsRecursive);
        }
    }

    /**
      * Returns roles that match the specified filter.
      *
      * @param aFilter     the filter to use in searching for roles 
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of role objects
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public Entity[] findRolesArray(String aFilter, 
            boolean aIsRecursive) throws SPIException{
        if (aFilter == null) { 
            throw new InvalidFilterException();
        }
        if (aFilter.startsWith(LdapNode.SEARCH_FILTER_DELIMITER)) {
            return findRolesByFilterArray(aFilter, aIsRecursive);
        } else {
            return findRolesByNameArray(aFilter, aIsRecursive);
        }
    }

    /**
      * Finds the roles that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child roles
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findRolesByFilter (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findRolesByFilterList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }

    /**
      * Returns the roles that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of roles 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findRolesByFilterArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findRolesByFilterList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Finds the roles that match the filter.
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of child roles
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findRolesByName (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findRolesByNameList(aFilter, aIsRecursive);
        return childrenList.iterator();
    }


    /**
      * Returns the roles that match the filter. 
      * 
      * @param aFilter       the search filter
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return            array of roles 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] findRolesByNameArray (String aFilter,
            boolean aIsRecursive) throws SPIException {
        Vector childrenList = 
            findRolesByNameList (aFilter, aIsRecursive);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
     * Returns the entity for this id. Entity could be an organization,
     * a role, or a user.
     * 
     * @param aId         id string
     * @return            entity 
     * @throws            <code>SPIException</code> if error occurs
     */
    public Entity getEntity(String aId) throws SPIException {
        return getEntityFromDN(aId, LdapEntityType.UNKNOWN, false, null); 
    }

    /**
      * Returns child entities.
      * 
      * @return            <code>Iterator</code> of child entities
      * @throws            <code>SPIException</code> if error occurs
      */
    public abstract Iterator getChildren() throws SPIException;

    /**
      * Returns child entities.
      * 
      * @return            array of child entities
      * @throws            <code>SPIException</code> if error occurs
      */
    public abstract Entity[] getChildrenArray() throws SPIException;

    /**
     * Returns leaf entities.
     * 
     * @return            <code>Iterator</code> of leaf entities
     * @throws            <code>SPIException</code> if error occurs
     */
    public Iterator getLeaves() throws SPIException {
        return null;
    }

   /**
     * Returns a boolean indicating whether or not this entity
     * has leaves. 
     *
     * @return   <code>true</code> if there are leaves, otherwise
     *           <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean  hasLeaves() throws SPIException {
        return false;
    }

   /**
    * Returns node entities.
    * 
    * @return            <code>Iterator</code> of node entities
    * @throws            <code>SPIException</code> if error occurs
    */
    public Iterator getNodes() throws SPIException {
        return null;
    }

  /**
    * Returns a boolean indicating whether or not this entity
    * has nodes. 
    *
    * @return   <code>true</code> if there are nodes, otherwise
    *           <code>false</code>
    * @throws   <code>SPIException</code> if error occurs
    */
    public boolean  hasNodes() throws SPIException {
        return false;
    }

    /**
      * Returns the class filter for a container entity, i.e. the
      * union of all possible objectclasses.
      *
      * @param aContainerMapping container mapping object
      * @param aIsRecursive   <code>true</code> if recursive search,
      *                       other wise <code>false</code>
      * @return               filter string
      * @throws               <code>SPIException</code> if the type is invalid.
      */
    public String buildClassFilter(
             LdapEntityMapping.ContainerMapping aContainerMapping,
                                        boolean aIsRecursive) throws SPIException {
        StringBuffer retCode = buildClassFilterString(aContainerMapping.getObjectClasses(),
                aIsRecursive);
        if (aContainerMapping instanceof LdapEntityMapping.ListMapping) {
            addRoleFilterPlus(aIsRecursive, retCode);
        }
       return retCode.toString() ;
   }

    /**
      * Returns the class filter for a user.
      *
      * @param aUserMapping   mapping for user
      * @param aIsRecursive   <code>true</code> if recursive search,
      *                       otherwise <code>false</code>
      * @return               filter string
      * @throws               <code>SPIException</code> if the type is invalid.
      */
    public String buildClassFilter(
              LdapEntityMapping.UserMapping aUserMapping,
              boolean aIsRecursive) throws SPIException {
        String []objectClass = {aUserMapping.getObjectClass()};
        StringBuffer retCode = buildClassFilterString(objectClass,
                aIsRecursive);
        //exclude hosts from search (for Active Directory)
         retCode.insert(0, "(&(!(" + LdapDataStore.LDAP_OBJCLASS + "=" 
                    + mEntityMapping.mHostMapping.getObjectClass() +
                    "))");
         retCode.append(")");
        return retCode.toString();
    }

    /**
      * Returns the class filter for a host.
      *
      * @param aHostMapping   mapping for host
      * @param aIsRecursive   <code>true</code> if recursive search,
      *                       otherwise <code>false</code>
      * @return               filter string
      * @throws               <code>SPIException</code> if the type is invalid.
      */
    public String buildClassFilter(
              LdapEntityMapping.ItemMapping aHostMapping,
              boolean aIsRecursive) throws SPIException {
        String []objectClass = {aHostMapping.getObjectClass()};
        StringBuffer retCode = buildClassFilterString(objectClass,
                aIsRecursive);
        return retCode.toString();
    }

    /**
      * Returns the class filter string.
      *
      * @param aObjectClasses object classes 
      * @param aIsRecursive   <code>true</code> if recursive search,
      *                       otherwise <code>false</code>
      * @return               filter string
      * @throws               <code>SPIException</code> if error occurs 
      */
    private StringBuffer buildClassFilterString(String[] aObjectClasses,
              boolean aIsRecursive) throws SPIException {
        Vector subConditions = new Vector() ;
        StringBuffer retCode = new StringBuffer() ;
        boolean addExclusionFilter = false;
        for (int i = 0 ; i < aObjectClasses.length ; ++ i) {
            subConditions.add("(" + LdapDataStore.LDAP_OBJCLASS + "=" 
                    + aObjectClasses [i] + ")") ;
            if (aObjectClasses[i].equalsIgnoreCase(LdapDataStore.ORG_UNIT_OBJCLASS)) {
                addExclusionFilter = true; }
        }
        if (subConditions.size() > 1) {
            retCode.append("(|") ;
            int size = subConditions.size();
            for (int i = 0 ; i < size ; ++ i) {
                retCode.append(subConditions.get(i)) ;
            }
            retCode.append(")") ;
       } else { retCode.append(subConditions.get(0)) ; }
        if (addExclusionFilter) {
            excludeServiceEntriesFromSearchFilter(aIsRecursive, retCode);
        }
       return retCode ;
    }

    /**
      * Maps a filter to use the corresponding LDAP attributes.
      *
      * @param aFilter           search filter
      * @param aContainerMapping container mapping object
      * @param aIsRecursive      <code>true</code> if recursive search,
      *                          otherwise <code>false</code>
      * @return                  filter string
      * @throws                 <code>SPIException</code> if the type is invalid.
      */
    public String mapFilter(String aFilter,
             LdapEntityMapping.ContainerMapping aContainerMapping,
                                        boolean aIsRecursive) throws SPIException {
        String classFilter = buildClassFilter(aContainerMapping, aIsRecursive) ;
        if (aFilter == null || aFilter.length() == 0) {
            return classFilter ;
        }
        StringBuffer retCode = new StringBuffer() ;
        retCode.append("(&").append(classFilter) ;
        retCode.append(buildMappedFilter(aFilter, aContainerMapping));
        retCode.append(")") ;
        return retCode.toString() ;
   }

    /**
      * Returns the mapping of a filter from the SO attributes to
      * the LDAP attributes.
      * It is assumed that the type and filter are non-null and valid.
      *
      * @param aFilter            filter to map
      * @param aContainerMapping  container mapping object
      * @return                   mapped filter
      * @throws                  <code>SPIException</code> if error
      *         occurs
      */
    public String buildMappedFilter(String aFilter, 
            LdapEntityMapping.ContainerMapping aContainerMapping)
        throws SPIException {
        int lastChar = 0;
        boolean hasExtraParentheses = false;
        if (aFilter.charAt(0) == '(') {
            lastChar = 1;
            hasExtraParentheses = true;
        }
        ParsedAttribute attribute = new ParsedAttribute() ;
        int currentChar = parseNextAttribute(aFilter, lastChar,
                                        attribute) ;
        StringBuffer tmpBuffer = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        Vector subConditions = new Vector();
        while (currentChar != -1) {
            tmpBuffer.delete(0, tmpBuffer.length());
            String []attributes =
                    aContainerMapping.getNamingAttributes();
            for (int i = 0; i < attributes.length; ++i) {
                tmpBuffer.append("(");
                tmpBuffer.append(attributes[i]);
                tmpBuffer.append(attribute.mOperator).append(
                                     attribute.mValue) ;
                tmpBuffer.append(")");
            }
            subConditions.add(tmpBuffer.toString());
            lastChar = attribute.mLastChar ;
            currentChar = parseNextAttribute(aFilter, lastChar,
                    attribute) ;
        }
        StringBuffer retCode = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        if (subConditions.size() > 1) {
            retCode.append("(&") ;
            int size = subConditions.size();
            for (int i = 0 ; i < size ; ++ i) {
                retCode.append(subConditions.get(i)) ;
            }
            retCode.append(")") ;
        } else { retCode.append(subConditions.get(0)) ; }
        return retCode.toString() ;
    }

    /**
      * Builds an LDAP boolean condition involving the naming
      * attribute of a container entity.
      * Since container entities are allowed to have multiple objectclasses
      * and corresponding naming attributes, we have to build a
      * filter representing the union of these possible cases.
      *
      * @param aPattern     pattern that the attribute must match
      * @param aOperator    LDAP operator to be used for attribute matching
      * @param aContainerMapping container mapping object
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             filter string
      * @throws             <code>SPIException</code> if error occurs
      */
    public String buildNameFilter(String aPattern, String aOperator,
                   LdapEntityMapping.ContainerMapping aContainerMapping,
                   boolean aIsRecursive) throws SPIException {
        StringBuffer retCode = buildNameFilterString(aPattern, aOperator,
                aContainerMapping.getObjectClasses(), 
                aContainerMapping.getNamingAttributes(),
                aIsRecursive);
        return retCode.toString() ;
    }

    /**
      * Builds an LDAP boolean condition involving the naming
      * attribute of role entity.
      * Since role entities are allowed to have multiple objectclasses
      * and corresponding naming attributes, we have to build a
      * filter representing the union of these possible cases.
      *
      * @param aPattern     pattern that the attribute must match
      * @param aOperator    LDAP operator to be used for attribute matching
      * @param aListMapping list mapping object
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             filter string
      * @throws             <code>SPIException</code> if error occurs
      */
    public String buildNameFilter(String aPattern, String aOperator,
             LdapEntityMapping.ListMapping aRoleMapping,
             boolean aIsRecursive) throws SPIException {
        StringBuffer retCode = buildNameFilterString(
                aPattern, aOperator, aRoleMapping.getObjectClasses(),
                aRoleMapping.getNamingAttributes(),
                aIsRecursive);
        addRoleFilterPlus(aIsRecursive, retCode);
        return retCode.toString();
    }

    /**
      * Builds an LDAP boolean condition involving the naming
      * attributes of an entity.
      * Since container entities are allowed to have multiple objectclasses
      * and corresponding naming attributes, we have to build a
      * filter representing the union of these possible cases.
      *
      * @param aPattern       pattern that the attribute must match
      * @param aOperator      LDAP operator to be used for attribute matching
      * @param aObjectClasses object classes
      * @param aNamingAttributes    naming attributes
      * @param aIsRecursive   <code>true</code> if recursive search,
      *                       otherwise <code>false</code>
      * @return               filter string
      * @throws             <code>SPIException</code> if error occurs
      */
    private StringBuffer buildNameFilterString(String aPattern, String aOperator,
                   String[] aObjectClasses, String[] aNamingAttributes,
                   boolean aIsRecursive) throws SPIException {
        Vector subConditions = new Vector() ;
        boolean addExclusionFilter =
            addNameFilters(aObjectClasses, aNamingAttributes,
            aOperator, aPattern, subConditions) ;
        StringBuffer retCode = new StringBuffer() ;
        if (subConditions.size() > 1) {
            retCode.append("(|") ;
            int size = subConditions.size();
            for (int i = 0 ; i < size ; ++ i) {
                retCode.append(subConditions.get(i)) ;
            }
            retCode.append(")") ;
        } else { retCode.append(subConditions.get(0)) ; }
        if (addExclusionFilter) {
            excludeServiceEntriesFromSearchFilter(aIsRecursive, retCode);
        }
        return retCode ;
    }

    /**
      * Builds an LDAP boolean condition involving the unique
      * attribute of an item.
      *
      * @param aPattern     pattern that the attribute must match
      * @param aOperator    LDAP operator to be used for attribute matching
      * @param aItemMapping item mapping object
      * @param aIsRecursive <code>true</code> if recursive search,
      *                     otherwise <code>false</code>
      * @return             filter string
      * @throws             <code>SPIException</code> if error occurs
      */
    public String buildNameFilter(String aPattern, String aOperator,
                  LdapEntityMapping.ItemMapping aItemMapping,
                  boolean aIsRecursive) throws SPIException {
        Vector subConditions = new Vector() ;
        addNameFilter(aItemMapping.getObjectClass(), aItemMapping.getUniqueAttribute(),
                    aOperator, aPattern, subConditions) ;
        StringBuffer retCode = new StringBuffer() ;
        retCode.append(subConditions.get(0)) ; 
        if (aItemMapping instanceof LdapEntityMapping.UserMapping) {
            //exclude hosts from search (for Active Directory)
            retCode.insert(0, "(&(!(" + LdapDataStore.LDAP_OBJCLASS + "=" 
                    + mEntityMapping.mHostMapping.getObjectClass() +
                    "))");
            retCode.append(")");
        }
        return retCode.toString() ;
    }

    /**
      * Adds a series of name filters to vector thereof.
      *
      * @param aObjectClasses       object classes   
      * @param aNamingAttributes   naming attributes   
      * @param aOperator            matching operator
      * @param aPattern             matching pattern
      * @param aFilters             vector that will be updated on return
      * @return                    <code>true</code> if one of the 
      *                            objectclasses is organizationalunit
      */
    public boolean addNameFilters(String[] aObjectClasses, 
            String[] aNamingAttributes, String aOperator, 
            String aPattern, Vector aFilters) {
        boolean addExclusionFilter = false;
        for (int i = 0 ; i < aObjectClasses.length; ++ i) {
            addNameFilter(aObjectClasses [i], aNamingAttributes [i],
            aOperator, aPattern, aFilters) ;
            if (aObjectClasses[i].equalsIgnoreCase(LdapDataStore.ORG_UNIT_OBJCLASS)) {
                addExclusionFilter = true;
            }
        }
        return addExclusionFilter;
    }

    /**
      * Adds an individual name filter to a vector thereof.
      *
      * @param aObjectClass object class the entity must have
      * @param aAttribute   attribute to be matched
      * @param aOperator    matching operator
      * @param aPattern     matching pattern
      * @param aFilters     vector that will be updated with
      *                     the filter on return
      */
    public void addNameFilter(String aObjectClass, String aAttribute,
                              String aOperator, String aPattern,
                                            Vector aFilters) {
        StringBuffer filterBuffer = new StringBuffer("(&(");
        filterBuffer.append(LdapDataStore.LDAP_OBJCLASS).append("=");
        filterBuffer.append(aObjectClass).append(")(");
        filterBuffer.append(aAttribute).append(aOperator);
        if (aPattern == null) {
            filterBuffer.append(LdapDataStore.LDAP_WILDCARD);
        }
        else {
            filterBuffer.append(aPattern);
        }
        filterBuffer.append("))");
        aFilters.add(filterBuffer.toString());
    }

    /**
      * Returns the mapping of a filter from the SO attributes to
      * the LDAP attributes.
      * It is assumed that the type and filter are non-null and valid.
      *
      * @param aFilter       filter to map
      * @param aUserMapping  user mapping object
      * @return              mapped filter
      * @throws              SPIException if error occurs
      * @throws              InvalidFilterException if aFilter is null
      */
    public String buildMappedFilter(String aFilter,
            LdapEntityMapping.UserMapping aUserMapping) throws SPIException {
        int lastChar = 0;
        boolean hasExtraParentheses = false;
        if (aFilter.charAt(0) == '(') {
            lastChar = 1;
            hasExtraParentheses = true;
        }
        ParsedAttribute attribute = new ParsedAttribute() ;
        int currentChar = parseNextAttribute(aFilter, lastChar,
                           attribute) ;
        StringBuffer tmpBuffer = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        Vector subConditions = new Vector();
        while (currentChar != -1) {
            tmpBuffer.delete(0, tmpBuffer.length());
            if (attribute.mAttribute == null ||
                                attribute.mAttribute.length() == 0) {
                attribute.mAttribute = aUserMapping.getUniqueAttribute();
            } else {
                if (attribute.mAttribute == null) {
                    throw new InvalidFilterException(aFilter) ;
                }
            }
            tmpBuffer.append("(").append(attribute.mAttribute).append(
                       attribute.mOperator).append(attribute.mValue).append(")");
            subConditions.add(tmpBuffer.toString());
            lastChar = attribute.mLastChar ;
            currentChar = parseNextAttribute(aFilter, lastChar,
                 attribute) ;
        }
        StringBuffer retCode = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        if (subConditions.size() > 1) {
            retCode.append("(&") ;
            int size = subConditions.size();
            for (int i = 0 ; i < size ; ++ i) {
                retCode.append(subConditions.get(i)) ;
            }
            retCode.append(")") ;
         } else { retCode.append(subConditions.get(0)) ; }
         return retCode.toString() ;
    }

    /**
      * Returns the mapping of a filter from the SO attributes to
      * the LDAP attributes.
      * It is assumed that the type and filter are non-null and valid.
      *
      * @param aFilter         filter to map
      * @param aHostMapping    host mapping object
      * @return                mapped filter
      * @throws                <code>SPIException</code> if error
      *         occurs
      */
    public String buildMappedFilter(String aFilter, 
            LdapEntityMapping.ItemMapping aHostMapping) throws SPIException {
        int lastChar = 0;
        boolean hasExtraParentheses = false;
        if (aFilter.charAt(0) == '(') {
            lastChar = 1;
            hasExtraParentheses = true;
        }
        ParsedAttribute attribute = new ParsedAttribute() ;
        int currentChar = parseNextAttribute(aFilter, lastChar,
                            attribute) ;
        StringBuffer tmpBuffer = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        Vector subConditions = new Vector();
        while (currentChar != -1) {
            tmpBuffer.delete(0, tmpBuffer.length());
            if (attribute.mAttribute == null ||
                           attribute.mAttribute.length() == 0) {
                attribute.mAttribute = aHostMapping.getUniqueAttribute();
            }
            tmpBuffer.append("(").append(attribute.mAttribute).append(
                   attribute.mOperator).append(attribute.mValue).append(")");
            subConditions.add(tmpBuffer.toString());
            lastChar = attribute.mLastChar ;
            currentChar = parseNextAttribute(aFilter, lastChar,
                                      attribute) ;
        }
        StringBuffer retCode = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        if (subConditions.size() > 1) {
            retCode.append("(&") ;
            int size = subConditions.size();
            for (int i = 0 ; i < size ; ++ i) {
                retCode.append(subConditions.get(i)) ;
            }
            retCode.append(")") ;
         } else { retCode.append(subConditions.get(0)) ; }
         return retCode.toString() ;
    }

    /**
      * Maps a filter to use the corresponding LDAP attributes.
      *
      * @param aFilter           search filter
      * @param aUserMapping      user mapping object
      * @param aIsRecursive      <code>true</code> if recursive search,
      *                          otherwise <code>false</code>
      * @return                  filter string
      * @throws                 <code>SPIException</code> if the type is invalid.
      */
    public String mapFilter(String aFilter,
             LdapEntityMapping.UserMapping aUserMapping,
                                        boolean aIsRecursive) throws SPIException {
        String classFilter = buildClassFilter(aUserMapping, aIsRecursive) ;
        if (aFilter == null || aFilter.length() == 0) {
            return classFilter ;
        }
        StringBuffer retCode = new StringBuffer() ;
        retCode.append("(&").append(classFilter) ;
        retCode.append(buildMappedFilter(aFilter, aUserMapping));
        retCode.append(")") ;
        return retCode.toString() ;
   }

    /**
      * Maps a filter to use the corresponding LDAP attributes.
      *
      * @param aFilter           search filter
      * @param aHostMapping      host mapping object
      * @param aIsRecursive      <code>true</code> if recursive search,
      *                          otherwise <code>false</code>
      * @return                  filter string
      * @throws                 <code>SPIException</code> if the type is invalid.
      */
    public String mapFilter(String aFilter,
             LdapEntityMapping.ItemMapping aHostMapping,
                                        boolean aIsRecursive) throws SPIException {
        String classFilter = buildClassFilter(aHostMapping, aIsRecursive) ;
        if (aFilter == null || aFilter.length() == 0) {
            return classFilter ;
        }
        StringBuffer retCode = new StringBuffer() ;
        retCode.append("(&").append(classFilter) ;
        retCode.append(buildMappedFilter(aFilter, aHostMapping));
        retCode.append(")") ;
        return retCode.toString() ;
   }

    /**
      * Adds the role filter to the search filter.
      *
      * @param aIsRecursive    <code>true</code> if search is
      *                         recursive
      * @param aRetCode        the existing search filter
      */
    public void addRoleFilterPlus(
               boolean aIsRecursive, StringBuffer aRetCode) {
        if (aRetCode == null) { return; }
        aRetCode.insert(0, "(&");
        aRetCode.insert(2, LdapRole.ROLE_FILTER_PLUS);
        aRetCode.append(")");
    }

    /**
      * Excludes the service entries from the search filter.
      *
      * @param aIsRecursive    <code>true</code> if search is
      *                         recursive
      * @param aRetCode        the existing search filter
      */
    public void excludeServiceEntriesFromSearchFilter(
               boolean aIsRecursive, StringBuffer aRetCode) {
        if (aRetCode == null) { return; }
        StringBuffer filterBuf = new StringBuffer();
        filterBuf.append("(&");
        filterBuf.append("(&(!(");
        filterBuf.append(LdapDataStore.CONFIG_NAMING_ATTR +
                                       LdapDataStore.SERVICES);
        filterBuf.append("))");
        if (aIsRecursive) {
            filterBuf.append("(!(");
            filterBuf.append(LdapDataStore.CONFIG_NAMING_ATTR +
                               LdapDataStore.SERVICE_ORG_CONFIG);
            filterBuf.append("))");
            filterBuf.append("(!(");
            filterBuf.append(LdapDataStore.LDAP_OBJCLASS + "=" +
                               LdapDataStore.SUNSERVICE_COMPONENT_OBJCLASS);
            filterBuf.append("))");
        }
        filterBuf.append(")");
        aRetCode.insert(0, filterBuf.toString());
        aRetCode.append(")");
    }


    public static class ParsedAttribute {
        public String mAttribute ;
        public String mOperator = "="; //default
        public String mValue ;
        public int mLastChar ;
    }

    private static final String SKIPPABLES = "()|&! \t" ;

    /**
      * Parses an LDAP filter to the next attribute and fills a
      * structure with the contents of the attribute condition.
      *
      * @param aFilter      filter to be parsed
      * @param aStart       starting point of the search
      * @param aAttribute   attribute description filled on return
      * @return index of the beginning of the expression
      * @throws SPIException   if the format is invalid.
      */
    public int parseNextAttribute(String aFilter, int aStart,
           ParsedAttribute aAttribute) throws SPIException {
        aAttribute.mAttribute = null;
        int length = aFilter.length() ;
        int currentChar = aStart ;
        // Find the beginning of the attribute if specified
        while (currentChar < length) {
            if (SKIPPABLES.indexOf(aFilter.charAt(currentChar)) == -1) {
                // We found a non-skippable character, may be
                // the beginning of the attribute.
                 break ;
            }
            ++ currentChar ;
        }
        if (currentChar >= length) { return -1 ; }
            // Find the next closing parentheses, if there is one
            int closing = aFilter.indexOf(")", currentChar);
            int operation = aFilter.indexOf('=', currentChar) ;
            if (operation != -1 && (closing == -1 || operation < closing)) {
                char preOperation = aFilter.charAt(operation - 1) ;
                if (preOperation == '<' || preOperation == '>' ||
                                    preOperation == '~') {
                    aAttribute.mAttribute = aFilter.substring(currentChar,
                                                   operation - 1) ;
                    aAttribute.mOperator = aFilter.substring(operation - 1,
                                           operation + 1) ;
                } else {
                    if (currentChar < operation) {
                        aAttribute.mAttribute = aFilter.substring(
                                 currentChar, operation) ;
                    }
                    aAttribute.mOperator = "=" ;
               }
               aAttribute.mLastChar = operation + 1;
          } else {
              aAttribute.mLastChar = currentChar;
          }
          while (aAttribute.mLastChar < length) {
            if (aFilter.charAt(aAttribute.mLastChar) == ')' &&
                       aFilter.charAt(aAttribute.mLastChar - 1) != '\\') {
                break ;
            }
            ++ aAttribute.mLastChar ;
         }
         if (operation != -1 && (closing == -1 || operation < closing)) {
            aAttribute.mValue = aFilter.substring(operation + 1,
                                     aAttribute.mLastChar) ;
        } else {
            aAttribute.mValue = aFilter.substring(currentChar,
                                                  aAttribute.mLastChar) ;
        }
        // Now a bit of normalisation...
        if (aAttribute.mAttribute != null) {
            aAttribute.mAttribute = aAttribute.mAttribute.trim().toLowerCase() ;
        }
        aAttribute.mValue = aAttribute.mValue.trim() ;
        return currentChar ;
    }

    /**
     * Returns a boolean indicating whether or not this entity
     * has roles. 
     *
     * @return   <code>true</code> if there are roles, otherwise
     *           <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean  hasRoles() throws SPIException {
        return false;
    }

    /**
      * Returns the child roles. 
      * 
      * @return            <code>Iterator</code> of child roles
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getRoles () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mRoleMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.ROLE, mEntityMapping.mRoleMapping.getContainerEntry(),
                false, returnValue);
        return childrenList.iterator();
    }

    /**
      * Returns the child roles. 
      * 
      * @return            array of child roles 
      * @throws            <code>SPIException</code> if error occurs
      */
    public Entity[] getRolesArray () throws SPIException {
        BooleanReturnValue returnValue =
                    new BooleanReturnValue(false);
        String searchFilter = buildClassFilter(
                mEntityMapping.mRoleMapping, false);
        Vector childrenList = getChildrenList (searchFilter,
                LdapEntityType.ROLE, mEntityMapping.mRoleMapping.getContainerEntry(),
                false, returnValue);
        Entity[] entities = new Entity[childrenList.size()];
        for (int i = 0; i < childrenList.size(); ++i) {
            entities[i] = (Entity)childrenList.get(i);
        }
        return entities;
    }

    /**
      * Returns role entities whose attributes match
      * a given LDAP filter.
      *
      * @param aFilter      LDAP search filter
      * @param aRecursive   true if recursive search, false otherwise
      * @return             <code>Vector</code/> of <code>Entity</code>s representing 
      *                     the matching entities
      * @throws             SPIException if error occurs 
      * @throws             IllegalReadException if error occurs 
      */
    public Vector searchForRoles(String aFilter,
                            boolean aRecursive) throws SPIException {
	    StringBuffer baseDnBuf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        if (mEntityMapping.mRoleMapping.getContainerEntry() != null) {
            baseDnBuf.append(mEntityMapping.mRoleMapping.getContainerEntry());
            baseDnBuf.append(LDAP_SEPARATOR);
        }
        baseDnBuf.append(mLocation);
        String []attributes = { LdapDataStore.LDAP_OBJCLASS};
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
                                    LdapEntityType.ROLE, false, attrsValues);
                    }
                    if (tmpEntity != null) {
                        retCode.add(tmpEntity);
                    }
                } catch (Exception ignore) {
	                /* cannot convert DN to entity, so omit from list */
                }
             }
        }
        return retCode == null ? new Vector() : retCode;
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
        return getLayeredProfiles(parents, null);
    }


}
