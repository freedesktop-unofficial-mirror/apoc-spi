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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import netscape.ldap.LDAPDN;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.ldap.policies.LdapPolicy;
import com.sun.apoc.spi.ldap.util.Timestamp;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.InvalidDisplayNameException;
import com.sun.apoc.spi.profiles.ProfileImpl;
import com.sun.apoc.spi.profiles.ProfileRepositoryImpl;
import com.sun.apoc.spi.profiles.UnknownApplicabilityException;

/**
  * Implementation for an LDAP profile.
  *
  */
public class LdapProfile extends ProfileImpl
{
    private static final String DEFAULT_DISPLAY_NAME = "Settings for ";
    private String mLocation;
    private long mLastModified = 0;
    private Entity mAuthor;

    /**
      * Constructor.
      * 
      * @param aId             id
      * @param aRepository     <code>ProfileRepository</code> object
      * @param aDisplayName    display name
      * @param aApplicability  scope for the profile
      * @param aPriority       priority
      */
    public LdapProfile(String aId, ProfileRepositoryImpl aRepository,
            String aDisplayName, Applicability aApplicability,
            int aPriority) {
        super(aId, aRepository, aDisplayName, aApplicability, aPriority);
        mLocation = aId;
    }

    /**
     * Returns the display name for this profile.
     *
     * @return             display name for this profile
     */
    public String getDisplayName() {
        if ( (mDisplayName == null) || (mDisplayName.length() == 0)
          || (mDisplayName.equals(LdapProfileRepository.DEFAULT_HOST_PROFILE_NAME))
          || (mDisplayName.equals(LdapProfileRepository.DEFAULT_USER_PROFILE_NAME)) ) {
            String entityName = "";
            try {
                entityName = 
                    getProfileRepository().getEntity().getDisplayName(null);
            }
            catch (SPIException ignore) {}
            return DEFAULT_DISPLAY_NAME + entityName;
        }
        return mDisplayName; 
    }

    /**
     * Sets the priority for this profile.
     *
     * @param aPriority    priority for this profile
     * @throws             <code>SPIException</code> if error occurs 
     */
    public void setPriority(int aPriority) 
    	throws SPIException {
        if (this.isLocal()) {
            LdapProfileRepository rep = (LdapProfileRepository)
            					this.getProfileRepository();
            rep.migrateProfile(this);
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.setProfilePriority(this, aPriority);
        mPriority = aPriority;
    }

    /**
     * Sets the display name for this profile.
     *
     * @param aDisplayName  new display name for this profile
     * @throws              <code>SPIException</code> if error occurs 
     */
    public void setDisplayName(String aDisplayName) 
    	throws SPIException {
        if ((aDisplayName == null) || (aDisplayName.length() == 0)) {
            throw new InvalidDisplayNameException();
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.setProfileDisplayName(this, aDisplayName);
        mDisplayName = aDisplayName;
    }

    /**
     * Sets the comment for this profile. If the new
     * comment is null then the existing comment is removed.
     *
     * @param aComment  comment as a String 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public void setComment(String aComment) 
    	throws SPIException {
        if (aComment == null) {
            throw new IllegalArgumentException();
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.setProfileComment(this, aComment);
        mComment = aComment;
        if (aComment == null) { mNoCommentFound = true; }
    }

    /**
     * Sets the scope for this profile.
     *
     * @param aApplicability new scope for this profile
     * @throws UnknownApplicabilityException if applicability unknown
     * @throws SPIException if error occurs 
     */
    public void setApplicability(Applicability aApplicability) 
    	throws SPIException {
        if (aApplicability == null) {
            throw new IllegalArgumentException();
        }
        if (mApplicability.equals(aApplicability)) {
            return;
        }
        if (aApplicability.equals(Applicability.UNKNOWN)) {
            throw new UnknownApplicabilityException();
        }
        if (this.isLocal()) {
            LdapProfileRepository rep = (LdapProfileRepository)
            					this.getProfileRepository();
            rep.migrateProfile(this);
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.setProfileApplicability(this, aApplicability);
        mApplicability = aApplicability;
    }

    /**
      * Returns the datastore location.
      *
      * @return    the location
      */
    public String getLocation() { return mLocation; }
 
    /**
     * Stores a <code>Policy</code>.
     *
     * @param aPolicy        the policy to store
     * @throws               <code>SPIException</code> if error occurs
     */
    public void storePolicy(Policy aPolicy) 
    	throws SPIException {
        if (aPolicy == null) {
            throw new IllegalArgumentException();
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.storePolicy((ProfileImpl)this, aPolicy);
    }

    /**
     * Destroys a <code>Policy</code>.
     *
     * @param aPolicy        the policy object
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void destroyPolicy(Policy aPolicy) 
    	throws SPIException {
        if (aPolicy == null) {
            throw new IllegalArgumentException();
        }
        ((LdapEntity)getProfileRepository().getEntity()).getDataStore()
        		.destroyPolicy(aPolicy);
    }
 
    /**
     * Returns a boolean indicating whether or not this profile
     * has policies. 
     *
     * @return   <code>true</code> if there are policies, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean hasPolicies() throws SPIException {
        return ((LdapEntity)getProfileRepository().getEntity())
					.getDataStore().hasPolicies(this);
    }

    /**
     * Returns the policies for this profile.
     *
     * @return 	<code>Iterator</code> of all the policies for this profile
     * @throws 	<code>SPIException</code> if error occurs 
     */
    public Iterator getPolicies() 
    	throws SPIException {
        mPolicies = ((LdapEntity)getProfileRepository().getEntity())
        					.getDataStore().getPolicies(this);
        return mPolicies.iterator();
    }

    /**
     * Returns the policies for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs
     */
    public  Iterator getPolicies(Iterator aPolicyIdList) 
        throws SPIException {
        if (aPolicyIdList == null) {
            throw new IllegalArgumentException();
        }
        return (((LdapEntity)getProfileRepository().getEntity())
                .getDataStore().getPolicies(this, aPolicyIdList))
                .iterator();
    }

    /**
     * Returns the PolicyInfos for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the PolicyInfos 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicyInfos(Iterator aPolicyIdList) 
        throws SPIException {
        if (aPolicyIdList == null) {
            throw new IllegalArgumentException();
        }
        return (((LdapEntity)getProfileRepository().getEntity())
                .getDataStore().getPolicyInfos(this, aPolicyIdList))
                .iterator();
    }

    /**
     * Returns the requested policy object.
     *
     * @param aId   the id for the required policy
     * @return      the policy object 
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Policy getPolicy(String aId) 
    	throws SPIException {
        if (aId == null) {
            throw new IllegalArgumentException();
        }
        return ((LdapEntity)getProfileRepository().getEntity())
        				.getDataStore().getPolicy(this, aId);
    }

    /**
     * Returns a boolean indicating whether or not this profile
     * has been assigned to entities. 
     *
     * @return   <code>true</code> if there are entities, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean hasAssignedEntities() throws SPIException {
        LdapDataStore dataStore = 
            ((LdapEntity)getProfileRepository().getEntity())
            		.getDataStore();
        if (dataStore.isVersion1() && this.isLocal()) {
            return true;
        }
        Vector entities = dataStore.getListOfEntitiesForProfile(this);
        return ((entities!=null) && (!entities.isEmpty()));
    }

    /**
     * Returns the entities to which this profile has been assigned.
     *
     * @return 	<code>Iterator</code> of entities 
     * @throws 	<code>SPIException</code> if error occurs 
     */
    public Iterator getAssignedEntities() 
    	throws SPIException {
        PolicySource aPolicySource = 
          ((ProfileRepositoryImpl)getProfileRepository()).getPolicySource();
        return aPolicySource.getAssignmentProvider()
        				.getAssignedEntities(this);
    }

    /**
     * Returns the time in milliseconds of the most 
     * recent modification of the profile entry and
     * its policies.
     *
     * @return    time of the last modification in milliseconds 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public long getLastModified()  
    		throws SPIException {
        long lastModified = getLastModifiedForEntry();
        getPolicies();
        if (mPolicies != null) {
            Iterator iterPolicies = mPolicies.iterator();
            while (iterPolicies.hasNext()) {
                Policy policy = (Policy)iterPolicies.next();
                long modified = policy.getLastModified();
                if (modified > lastModified) {
                    lastModified = modified;
                }
            }
        }
        return lastModified;
    }

    /**
      * Returns the time in milliseconds of the most 
      * recent modification of the profile entry. 
      *
      * @return    time of the last modification in milliseconds 
      * @throws    <code>SPIException</code> if error occurs 
      */
    private long getLastModifiedForEntry()  
    	throws SPIException {
        if (mLastModified == 0) {
            LdapEntity entity = 
                (LdapEntity)getProfileRepository().getEntity();
            ArrayList retValues = entity.getDataStore()
            							.getModificationDetails(this);
            setLastModified((String)retValues.get(0));
            if (retValues.size() > 1) {
                mAuthor = (Entity)retValues.get(1);
            }
        }
        return mLastModified;
    }
    
    /**
      * Returns the author of the most recent modification 
      * of the profile. 
      *
      * @return    name of the author of the last modification 
      * @throws    <code>SPIException</code> if error occurs 
      */
    public String getAuthor()  
    	throws SPIException {
        Entity author = null;
        String authorName = new String("");
        if (mPolicies == null) { getPolicies(); }
        Iterator iterPolicies = mPolicies.iterator();
        LdapPolicy lastModifiedPolicy = null;
        long lastModified = 0;
        while (iterPolicies.hasNext()) {
            LdapPolicy policy = (LdapPolicy)iterPolicies.next();
            long modified = policy.getLastModified();
            if (modified > lastModified) {
                lastModified = modified;
                lastModifiedPolicy = policy;
            }
        }
        if (getLastModifiedForEntry() >= lastModified) {
            author = getAuthorForEntry();
        }
        else {
            author = lastModifiedPolicy.getAuthor();
        }
        if (author != null) {
            authorName = author.getDisplayName(null);
        }
        return authorName;
    }

    /**
      * Returns the author of the most recent modification 
      * of the profile entry. 
      *
      * @return    author of the last modification 
      * @throws    <code>SPIException</code> if error occurs 
      */
    public Entity getAuthorForEntry()  
    	throws SPIException {
        if (mAuthor == null) {
            LdapEntity entity =
            (LdapEntity)getProfileRepository().getEntity();
            ArrayList retValues = entity.getDataStore()
            							.getModificationDetails(this);
            setLastModified((String)retValues.get(0));
            if (retValues.size() > 1) {
                mAuthor = (Entity)retValues.get(1);
            }
        }
        return mAuthor;
    }

    /**
      * Sets the time in milliseconds for the last
      * modification of the profile entry.
      *
      * @param aLastModified the String value for the modification
      *                      time as returned by the database
      */
    public void setLastModified(String aLastModified) {
        if (aLastModified == null) {
            throw new IllegalArgumentException();
        }
        mLastModified = Timestamp.getMillis(aLastModified);
    }

    /**
     * Returns the comment for this profile.
     *
     * @return          comment 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public String getComment() 
    	throws SPIException {
        if (mComment == null && !mNoCommentFound) {
            mComment = findComment();
        }
        if (mComment == null) {
            return new String("");
        }
        return mComment;
    }

    /**
      * Finds the comment for the profile.
      *
      * @return      comment for this profile
      * @throws      <code>SPIException</code> if error occurs 
      */
    private String findComment() 
    	throws SPIException {
        return ((LdapEntity)getProfileRepository().getEntity())
        			.getDataStore().getProfileComment(this);
    }

    /**
      * Tests for equality with another Profile.
      *
      * @param aProfile    other profile
      * @return            <code>true</code> if both profiles are
      *                    equal, otherwise <code>false</code>
      */
    public boolean equals (Object aProfile) {
        if (aProfile instanceof LdapProfile) {
            return LDAPDN.equals(mLocation.toLowerCase(),
                ((LdapProfile) aProfile).mLocation.toLowerCase()) ;
        }
        return false;
    }
    
    /**
     * Test if the profile is a Local Profile
     * ie. its DN contains _defaultuserpolicygroup_
     * or _defaulthostpolicygroup_
     *
     * @return true if profile is local,
     * 			otherwise false
     */
    public boolean isLocal() {
        return isLocalProfileDN(this.getId());
    }

    /**
     * Test if a DN is a Local Profile DN
     * ie. it contains _defaultuserpolicygroup_
     * or _defaulthostpolicygroup_
     *
     * @param dn	dn to test
     * @return true if dn is a Local Profile DN,
     * 			otherwise false
     */
    public static boolean isLocalProfileDN (String dn) {
        String hostStart = LdapDataStore.CONFIG_NAMING_ATTR
        				  +LdapProfileRepository.DEFAULT_HOST_PROFILE_DN;
        String userStart = LdapDataStore.CONFIG_NAMING_ATTR
		  				  +LdapProfileRepository.DEFAULT_USER_PROFILE_DN;
        return (dn.startsWith(hostStart) || dn.startsWith(userStart));
    }

    /**
     * copy each attribute from profile to the current 
     * Profile attributes
     * Used to modify a LdapProfile passed as a parameter 
     * to a method.
     * 
     * @param profile	profile to copy into current profile
     */
    public void copy(LdapProfile profile) {
        mLocation = profile.getLocation();
        mLastModified = 0;
        mAuthor = null;
        mId = profile.getId();
        mRepository = (ProfileRepositoryImpl)profile.getProfileRepository();
        mDisplayName = profile.getDisplayName();
        mApplicability = profile.getApplicability();
        mPriority = profile.getPriority();
        mComment = null;
    }
}
