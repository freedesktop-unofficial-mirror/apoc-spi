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

package com.sun.apoc.spi.ldap.profiles;

import java.util.Iterator;
import java.util.TreeSet;

import com.sun.apoc.spi.AssignmentProvider;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Host;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileImpl;
import com.sun.apoc.spi.profiles.ProfileRepositoryImpl;

/**
  * Class for an LDAP profile repository.
  *
  */
public class LdapProfileRepository extends ProfileRepositoryImpl
{
    /**  name for global profile container */
    public  static final String GLOBAL_PROFILE_CONTAINER = 
    	"_GlobalPolicyGroups_";
    /** Default name for entity (local) profile with user applicability */
    public static final String DEFAULT_USER_PROFILE_NAME = 
    	"_DefaultUserPolicyGroup_";
    /** Default dn entry for local profile with user applicability */
    public static final String DEFAULT_USER_PROFILE_DN = 
    	"_defaultuserpolicygroup_";
    /** Default name for entity (local) profile with host applicability */
    public static final String DEFAULT_HOST_PROFILE_NAME = 
    	"_DefaultHostPolicyGroup_";
    /** Default dn entry for local profile with host applicability */
    public static final String DEFAULT_HOST_PROFILE_DN = 
    	"_defaulthostpolicygroup_";

    protected Entity mRepositoryEntity;
    private String mLocation;

    /**
     * Constructor.
     *
     * @param id      	entity id of this repository
     * @param PolicyManager 
     * @throws  		<code>SPIException</code> if error occurs
     */
    public LdapProfileRepository(String id, PolicySource aPolicySource) 
    		throws SPIException {
        mPolicySource = aPolicySource;
        mEntityId = id;
        setId();
        mLocation = mId;
    }

    /**
      * Finds a profile object given its displayname 
      *
      * @param aDisplayName     display name for profile
      * @return                 object representing the profile
      * 						or null if not found
      * @throws                 <code>SPIException/code> if error
      *                         occurs
      */
    public  Profile findProfile(String aDisplayName)
    	throws SPIException {
        return getDataStore().findProfile(this,aDisplayName);
    }
    
    /**
      * Returns the location of the repository.
      *
      * @return    the location
      */
    public String getLocation() { 
        return mLocation; 
    }

    /**
     * Sets the id to
     * ou=_GlobalPolicyGroups_,ou=ApocRegistry,ou=default,
     * ou=OrganizationConfig,ou=1.0,ou=ApocService,ou=services,<EntityId>
     * 
     * @throws  <code>SPIException</code> if error occurs
     */
    public void setId() throws SPIException {
        StringBuffer buf = new StringBuffer(LdapDataStore.BUFFER_LENGTH);
        buf.append(LdapDataStore.CONFIG_NAMING_ATTR);
        buf.append(LdapProfileRepository.GLOBAL_PROFILE_CONTAINER);
        buf.append(LdapEntity.LDAP_SEPARATOR) ;
        for (int i = 0 ; 
                i < LdapDataStore.NUMBER_OF_SERVICE_MAPPING_ELEMENTS ; ++ i) {
            buf.append(LdapDataStore.SERVICE_MAPPING_ELEMENTS [i].getRDN()) ;
            buf.append(LdapEntity.LDAP_SEPARATOR) ;
        }
        buf.append(((LdapEntity)getEntity()).getLocation());
        mId = buf.toString();
    }

    /**
     * Creates and returns a <code>Profile</code> object.
     * 
     * @param aDisplayName   the display name for the profile
     * @param aApplicability applicablity of profile
     * @param aPriority		 priority of the profile
     * @return               <code>Profile</code> object 
     * @throws SPIException if error occurs
     */
    protected Profile createTheProfile(String aDisplayName,
            					 Applicability aApplicability,
            					 int aPriority)
    		throws SPIException {
        return getDataStore().createProfile(this, aDisplayName,
                							aApplicability, aPriority);
    }
 
    /**
     * Deletes the profile <code>aProfile</code> from the repository
     *
     * @param aProfile      the <code>Profile</code> object
     * @throws              <code>SPIException</code> if error occurs 
     */    
    public void deleteProfile(Profile aProfile) 
        	throws SPIException { 
        getDataStore().destroyProfile(this, (ProfileImpl)aProfile);
    }

    /**
     * Returns the requested profile or null if it does
     * not exist. 
     *
     * @param aId          id for the required profile
     * @return             object representing profile 
     * 					   or null if does not exist
     * @throws             <code>SPIException</code> if error occurs
     */
    public Profile getProfile(String aId) 
    		throws SPIException {
        return getDataStore().getProfile(this, aId);
    }

    /**
     * Returns the profiles of the specified scope. 
     *
     * @param aApplicability scope of profiles required
     * @return             TreeSet of profiles matching
     *                     the filter
     * @throws             SPIException if error occurs 
     */
    protected TreeSet getTheProfiles(Applicability aApplicability) 
    		throws SPIException {
        return getDataStore().getProfiles(this, aApplicability);
    }

    /**
     * Returns a boolean indicating if the current user has read only
     * access for this profile repository.
     *
     * @return             <code>true</code> if the current user has
     * 						read only access
     *                     otherwise <code>false</code>
     * @throws             <code>SPIException</code> if error occurs 
     */
    public boolean isReadOnly() throws SPIException {
        Entity repositoryEntity = this.getEntity();
        return !getDataStore().hasWriteAccess(this, repositoryEntity);
    }

    /**
     * Returns the container entity.
     *
     * @return    the container entity
     * @throws    <code>SPIException</code> if error occurs 
     */
    public Entity getEntity() throws SPIException {
        if (mRepositoryEntity == null) {
            try {
                mRepositoryEntity = mPolicySource.getEntity(mEntityId);
            } catch (SPIException spie) { }
        }
        return mRepositoryEntity;
    }

    /**
      * Returns the datastore.
      *
      * @return    the datastore
      */
    public LdapDataStore getDataStore() { 
        return ((LdapProfileProvider)mPolicySource.getProfileProvider())
        			.getDataStore();
    }
    
    /**
     * calculates the id of the Local Profile that would 
     * be stored in this repository if there was any
     * Does not provide any garantee that such a profile
     * exists in the repository.
     * A Local Profile is a notion specific to APOC 1,
     * this method enters in the process of supporting
     * APOC 1 LDAP repositories.
     * 
     * @return id of the Local Profile
     * @throws SPIException if error occurs
     */
    public String getLocalProfileId () throws SPIException {
        StringBuffer location = new StringBuffer();
        // globalId: ou=_GlobalPolicyGroups_,
        StringBuffer globalId = new StringBuffer();
        globalId.append(LdapDataStore.CONFIG_NAMING_ATTR);
        globalId.append(LdapProfileRepository.GLOBAL_PROFILE_CONTAINER);
        globalId.append(LdapEntity.LDAP_SEPARATOR);
        StringBuffer localId = new StringBuffer();
        Entity entity = getEntity();
        if ((entity instanceof Domain) || (entity instanceof Host)) {
	        // localId: ou=_defaulthostpolicygroup_,
	        localId.append(LdapDataStore.CONFIG_NAMING_ATTR);
	        localId.append(LdapProfileRepository.DEFAULT_HOST_PROFILE_DN);
	        localId.append(LdapEntity.LDAP_SEPARATOR) ;
        }
        else {
	        // localId: ou=_defaultuserpolicygroup_,
	        localId.append(LdapDataStore.CONFIG_NAMING_ATTR);
	        localId.append(LdapProfileRepository.DEFAULT_USER_PROFILE_DN);
	        localId.append(LdapEntity.LDAP_SEPARATOR) ;
        }
        String sGlobalId = globalId.toString();
        if (mLocation.startsWith(sGlobalId)) {
            location.append(localId.toString());
            location.append(mLocation.substring(sGlobalId.length()));
            return location.toString();
        }
        return mLocation;
    }
    
    /**
     * Migrate a Local Profile from APOC 1 to APOC 2 format
     * ie. from _defaultxxxpolicygroup_ to _GlobalPolicyGroups_/profileId
     * As the profile is a Local Profile from APOC 1, we can
     * safely assume that there is no entity explicitly assigned to it.
     * 
     * @param profile	profile to migrate
     *
     */
    public void migrateProfile(LdapProfile profile) throws SPIException {
        // create new profile
        LdapProfile newProfile = (LdapProfile)this.createProfile(
                profile.getDisplayName(), profile.getApplicability());
        // add the profile policies to new profile
        Iterator policies = profile.getPolicies();
        while(policies.hasNext()) {
            Policy policy = (Policy)policies.next();
            newProfile.storePolicy(policy);
        }
        // assign the containing entity to the profile
        AssignmentProvider assignmentProvider =
            mPolicySource.getAssignmentProvider();
        assignmentProvider.assignProfile(getEntity(), newProfile);
        // low-level destruction so that we don't check 
        // if Entities are assigned which would return true all the time
        this.deleteProfile(profile);
        // copy the attributes of new profile into profile
        // to midify the object referenced by the argument profile
        profile.copy(newProfile);
    }
}
