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

package com.sun.apoc.spi.file.entities;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.entities.User;

public class FileOrganization extends FileNode implements Organization {
    
    /**
     * @param aDisplayName
     * @param aId
     * @param aParent
     * @param aAssignment
     */
    public FileOrganization(String aDisplayName, String aId, Entity aParent, PolicySource aPolicySource) {
        super(aDisplayName, aId, aParent, aPolicySource);
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Organization#getUsers()
     */
    public Iterator getUsers() throws SPIException {
        return getLeaves();
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Organization#getSubOrganizations()
     */
    public Iterator getSubOrganizations() throws SPIException {
        return getNodes();
    }
    
    public Iterator findEntities(String aFilter, boolean aIsRecursive)
    throws SPIException {
        Iterator nodeIt;
        Iterator leafIt;
        Vector entities = new Vector();                
        
        nodeIt = ((Organization)this).findSubOrganizations(aFilter, aIsRecursive);
        leafIt = ((Organization)this).findUsers(aFilter, aIsRecursive);
        while (nodeIt.hasNext()) {
            entities.add(nodeIt.next());
        }
        while (leafIt.hasNext()) {
            entities.add(leafIt.next());
        }
        
        return entities.iterator();        
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Organization#findSubOrganizations(java.lang.String, boolean)
     */
    public Iterator findSubOrganizations(String aFilter, boolean aIsRecursive)
    throws SPIException {
        Iterator domainIt;
        Organization subOrg;
        Vector foundOrgs;
        Vector orgs;
        Iterator it;
        String orgId = getIdFromFilter(aFilter);
        
        foundOrgs = new Vector();
        orgs = getOrganizations(this,aIsRecursive);
        domainIt = orgs.iterator();
        while (domainIt.hasNext()) {
            Organization org = (Organization)domainIt.next();
            if (org.getId().indexOf(orgId) != -1 || orgId.equals(""))  {
                foundOrgs.add(org);
            }
        }
        return foundOrgs.iterator();
    }
    
    private Vector getOrganizations(Organization aOrg, boolean aIsRecursive)
    throws SPIException {
        Vector organizations;
        Iterator orgIt;
        
        organizations = new Vector();
        orgIt = aOrg.getSubOrganizations();
        while (orgIt.hasNext()) {
            Organization org = (Organization)orgIt.next();
            organizations.add(org);
            if (aIsRecursive) {
                organizations.addAll(getOrganizations(org, aIsRecursive));
            }
        }
        
        return organizations;
        
    }
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Organization#findUsers(java.lang.String, boolean)
     */
    public Iterator findUsers(String aFilter, boolean aIsRecursive)
    throws SPIException {
        String userId;
        
        userId = getIdFromFilter(aFilter);
        if (userId == null ) {
            return Collections.EMPTY_LIST.iterator();
        }
        
        return findUsersRecursive(userId, aIsRecursive);
    }
    
    private Iterator findUsersRecursive(String aUserId, boolean aIsRecursive)
    throws SPIException {
        FileOrganization subOrg;
        Iterator userIt;
        Vector foundUsers;
        Iterator it;
        
        foundUsers = new Vector();        
        userIt = findInLeafs( aUserId, this);
        if ( userIt.hasNext() || !aIsRecursive ) {
            return userIt;
        } else {
            it = getSubOrganizations();
            while ( it.hasNext() ) {
                subOrg = (FileOrganization)it.next();
                userIt = subOrg.findUsersRecursive( aUserId, aIsRecursive );
                while( userIt.hasNext() ) {
                    foundUsers.add(userIt.next());
                }
            }
        }
        
        return foundUsers.iterator();
    }
    
    private Iterator findInLeafs( String aUserId, Organization aOrg ) throws SPIException {
        Iterator it;
        User user;
        Vector v;
        
        v = new Vector();
        it = aOrg.getUsers();
        while ( it.hasNext() ) {
            user = (User)it.next();
            if ( user.getId().indexOf(aUserId) != -1  || aUserId.equals("") ) {
                v.add(user);
            }
        }        
        return v.iterator();
    }
    
    
}
