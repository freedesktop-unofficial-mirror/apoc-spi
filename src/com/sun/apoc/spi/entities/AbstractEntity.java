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

package com.sun.apoc.spi.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale ;
import java.util.TreeSet;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileComparatorProvider;
import com.sun.apoc.spi.profiles.ProfileRepository;

/**
  * Abstract class for an entity.
  */
public abstract class AbstractEntity implements Entity
{
    protected String mId;
    protected PolicySource mPolicySource;


    public void setPolicySource(PolicySource aPolicySource) {
        mPolicySource = aPolicySource;
    }
   
    /**
      * Returns the id for this entity.
      *
      * @return           the id 
      */
    public String getId() {
        return mId;
    }

    /**
      * Returns parent entity. 
      * 
      * @return            parent entity 
      */
    public abstract Entity getParent() ;

    /**
     * Returns the profile repository for this entity. 
     *
     * @return            the profile repository for this entity 
     * @throws            <code>SPIException</code> if error occurs
     */
    public ProfileRepository getProfileRepository() throws SPIException {
        return mPolicySource.getProfileProvider()
        			.getProfileRepository(this.getId());
    }


    /**
     * Returns the profiles assigned to this entity. 
     *
     * @return                 <code>Iterator</code> of profiles assigned 
     *                         to this entity
     * @throws            <code>SPIException</code> if error occurs
     */
    public Iterator getAssignedProfiles() throws SPIException {
        return mPolicySource.getAssignmentProvider()
       				 .getAssignedProfiles(this);
    }

    /**
     * Assigns a profile to this entity.
     *
     * @param aProfile   profile to assign
     * @throws            <code>SPIException</code> if error occurs
     */
    public void assignProfile(Profile aProfile) throws SPIException {
        mPolicySource.getAssignmentProvider().assignProfile(this, aProfile);
    }

    /**
     * Unassigns specified profile from this entity.
     *
     * @param aProfile   profile to uassign
     * @throws            <code>SPIException</code> if error occurs
     */
    public void unassignProfile(Profile aProfile) throws SPIException {
        mPolicySource.getAssignmentProvider()
        			.unassignProfile(this, aProfile);
    }

    /**
      * Returns an iterator over all the parents for this entity.
      * 
      * @return   iterator over all the parents for this entity.
      */
    public Iterator getAllParents() {
        ArrayList parents = new ArrayList();
        Entity parent = getParent();
        while (parent != null) {
            parents.add(0, parent);
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
    public abstract Iterator getLayeredProfiles() throws SPIException;

    /**
     * Returns an iterator of profiles assigned to the parents
     * given in parameter and roles given in parameter 
     * and to the current entity.
     * The first element of the iterator has the lowest priority, 
     * the last element has the highest priority.
     *
     * @param	parents		iterator over the entities representing
     * 						the parents of the current entity
     * @param	roles		iterator over the entities representing
     * 						the roles this entity is member of	
     * @return              iterator of hierarchical profiles
     * @throws              <code>SPIException</code> if error occurs
     */
    public Iterator getLayeredProfiles(Iterator parents, Iterator roles) 
    	throws SPIException {
        ArrayList profiles = new ArrayList();
        
        /* Add the parents profiles */
        Iterator iterParentProfiles = null;
        if (parents != null) {
            while (parents.hasNext()) {
                Entity parent = (Entity)parents.next();
                iterParentProfiles = parent.getAssignedProfiles();
                while (iterParentProfiles.hasNext()) {
                    profiles.add((Profile)iterParentProfiles.next());
                }
            }
        }
        /* get the s for the roles of which
           this entity is a member */
        if (roles != null) {
            Comparator comparator = 
                ((ProfileComparatorProvider)mPolicySource.getProfileProvider())
                		.getProfileComparator();
            TreeSet roleProfilesTree = new TreeSet(comparator);
            while (roles.hasNext()) {
                Entity role = (Entity)roles.next();
                Iterator iterCurrentRole = role.getAssignedProfiles();
	            while (iterCurrentRole.hasNext()) {
	                roleProfilesTree.add(iterCurrentRole.next());
	            }
            }
            Iterator iterRoleProfilesTree = roleProfilesTree.iterator();
            while (iterRoleProfilesTree.hasNext()) {
                profiles.add((Profile)iterRoleProfilesTree.next());
            }
        }
        /* get the s for the entity itself */
        Iterator iterEntityProfiles = this.getAssignedProfiles();
        while (iterEntityProfiles.hasNext()) {
            profiles.add((Profile)iterEntityProfiles.next());
        }
        return profiles.iterator();
    }
    
    /**
     * Returns the name of <code>PolicySource</code> for this entity
     * @return                  the name of the <code>PolicySource</code>
     */    
    public String getPolicySourceName() {
        return mPolicySource.getName();
    }
}
