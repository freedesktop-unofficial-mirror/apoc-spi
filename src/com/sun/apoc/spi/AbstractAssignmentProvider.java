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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.NullProfileException;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileComparatorProvider;

public abstract class AbstractAssignmentProvider 
	implements AssignmentProvider {

    protected PolicySource mPolicySource;

    /**
     * Assigns a profile to an entity.
     * Verify if the Applicability of the Profile is
     * compatible with the type of Entity:
     * <li>A profile with Applicabity.USER can be assigned
     * to a Organization, User or Role defined in an Organization.</li>
     * <li>A profile with Applicabity.HOST can be assigned
     * to a Domain, Host or Role defined in Domain.</li>
     * <li>A profile with Applicabity.ALL can be assigned
     * to any entity</li>
     *
	 * @param entity	entity to assign the profile to
	 * @param profile   profile to assign to the entity
     * @throws          <code>SPIException</code> if error occurs
     * @throws          <code>IllegalAssignmentException</code>
     * 					if Applicability of the profile is not
     * 					compatible with the type of entity
     */
    public void assignProfile(Entity entity, Profile profile)
            throws SPIException {
        // check that profile is not already assigned
        Iterator profiles  = getAssignedProfiles(entity);
        while (profiles.hasNext()) {
            Profile assignedProfile = (Profile)profiles.next();
            if (profile.equals(assignedProfile)) {
                return;
            }
        }
        if (!isCompatible(entity, profile)){
            throw new IllegalAssignmentException (
                    profile.getApplicability().getStringValue(), "") ;
        }
        assignProfileToEntity(entity, profile);
    }
    
    /**
     * Assigns a profile to an entity.
     *
	 * @param entity	entity to assign the profile to
	 * @param profile   profile to assign to the entity
     * @throws          <code>SPIException</code> if error occurs
     */
    protected abstract void assignProfileToEntity(Entity entity, Profile profile)
    	throws SPIException;

	/**
	 * Unassigns the specified profile from the entity.
	 *
	 * @param entity	entity to unassign the profile to
	 * @param profile   profile to unassign to the entity
	 * @throws          <code>SPIException</code> if error occurs
	 */
    public abstract void unassignProfile(Entity entity, Profile profile)
            throws SPIException;

	/**
	 * returns the profiles assigned to the entity.
	 * Returns only the profiles that have an Applicability
	 * compatible with the entity type.
	 * The profiles are ordered occording to their priority.
	 *
	 * @param entity	entity to get assigned profiles from
	 * @return			Iterator on the assigned Profiles
	 * @throws          <code>SPIException</code> if error occurs
	 * @see AssignmentProvider#assignProfile(Entity, Profile)
	 */
    public Iterator getAssignedProfiles(Entity entity) 
    		throws SPIException {
        Comparator comparator = 
            ((ProfileComparatorProvider)mPolicySource.getProfileProvider())
            		.getProfileComparator();
        TreeSet assignedProfiles = new TreeSet(comparator);
        Iterator iterProfiles = getProfilesAssignedToEntity(entity);
        while (iterProfiles.hasNext()) {
            Profile profile = (Profile)iterProfiles.next();
            if (isCompatible(entity, profile)) {
                assignedProfiles.add(profile);
            }
        }
        return assignedProfiles.iterator();
    }

	/**
	 * returns the profiles assigned to the entity.
	 *
	 * @param entity	entity to get assigned profiles from
	 * @return			Iterator on the assigned Profiles
	 * @throws          <code>SPIException</code> if error occurs
	 */
    protected abstract Iterator getProfilesAssignedToEntity(Entity entity)
    		throws SPIException;
    
	/**
	 * returns the entities the profile is assigned to.
	 * Returns only the entities that have a type 
	 * compatible with the profile Applicability.
	 *
	 * @param profile	profile to get assigned Entities from
	 * @return			Iterator on the assigned Entities
	 * @throws          <code>SPIException</code> if error occurs
	 * @see AssignmentProvider#assignProfile(Entity, Profile)
	 */
    public Iterator getAssignedEntities(Profile profile) throws SPIException {
	    if (profile == null) {
	        throw new NullProfileException();
        }
        HashSet assignedEntities = new HashSet();
        Iterator iterEntities = getEntitiesAssignedToProfile(profile);
        while (iterEntities.hasNext()) {
            Entity entity = (Entity)iterEntities.next();
            if (isCompatible(entity, profile)) {
                assignedEntities.add(entity);
            }
        }
        return assignedEntities.iterator();
    }

	/**
	 * returns the entities the profile is assigned to.
	 *
	 * @param profile	profile to get assigned Entities from
	 * @return			Iterator on the assigned Entities
	 * @throws          <code>SPIException</code> if error occurs
	 */
    protected abstract Iterator getEntitiesAssignedToProfile(Profile profile)
			throws SPIException;

    /**
     * Opens the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     */
    public abstract void open() throws SPIException;

    /**
     * Closes the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     */
    public abstract void close() throws SPIException;

    /**
     * Verify if the profile Applicability is compatible with
     * the type of entity:
     * <li>A profile with Applicabity.USER is compatible with
     * a Organization, User or Role defined in an Organization.</li>
     * <li>A profile with Applicabity.HOST is compatible with
     * a Domain, Host or Role defined in Domain.</li>
     * <li>A profile with Applicabity.ALL is compatible with
     * any entity</li>
     * 
     * @param entity	Entity to verify
     * @param profile	Profile to verify
     * @return			true if profile Applicability is compatible
     * 					with entity, false otherwise.
     */
    private boolean isCompatible(Entity entity, Profile profile) {
        boolean compatible = false;
        if ((profile != null) && (entity != null)){
            Applicability profileApp = profile.getApplicability();
            if (profileApp.equals(Applicability.ALL)){
                compatible = true;
            }
            else if (profileApp.equals(Applicability.HOST)){
                compatible =   entity.getPolicySourceName().equals(EnvironmentConstants.HOST_SOURCE);
            }
            else if (profileApp.equals(Applicability.USER)){
                compatible =   entity.getPolicySourceName().equals(EnvironmentConstants.USER_SOURCE);
            }
        }
        return compatible;
    }
}
