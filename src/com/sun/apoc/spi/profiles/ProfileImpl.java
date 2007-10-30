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

package com.sun.apoc.spi.profiles;

import java.util.Iterator;
import java.util.Vector;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.policies.Policy;

/**
  * Implementation for an LDAP profile.
  *
  */
public abstract class ProfileImpl implements Profile
{
    /** undefined priority */
    public static final int UNDEFINED_PRIORITY = 0 ;

    protected String mId;
    protected ProfileRepositoryImpl mRepository;
    protected String mDisplayName;
    protected Applicability mApplicability;
    protected int mPriority;
    protected Vector mPolicies;
    protected String mComment;
    protected boolean mNoCommentFound = false;
    protected Entity mAuthor;

    /**
      * Constructor.
      * 
      * @param aId            id
      * @param aRepository    <code>ProfileRepository</code> object
      * @param aDisplayName   display name
      * @param aApplicability scope for the profile
      * @param aPriority      priority
      */
    public ProfileImpl(String aId, ProfileRepositoryImpl aRepository,
            		   String aDisplayName, Applicability aApplicability,
            		   int aPriority) {
        mId = aId;
        mRepository = aRepository;
        mDisplayName = aDisplayName;
        mApplicability = aApplicability;
        mPriority = aPriority;
    }
    
    /**
     * Returns the id for this profile.
     *
     * @return             id for this profile
     */
    public String getId() { return mId; }

    /**
     * Returns the <code>ProfileRepository</code> for this profile.
     *
     * @return             <code>ProfileRepository</code> 
     */
    public ProfileRepository getProfileRepository() { return mRepository; }

    /**
     * Returns the scope for this profile.
     *
     * @return             scope for this profile
     */
    public Applicability getApplicability() { return mApplicability; }

    /**
     * Returns the priority for this profile.
     *
     * @return             priority for this profile
     */
    public int getPriority() { return mPriority; }

    /**
     * Sets the priority for this profile.
     *
     * @param aPriority     priority for this profile
     * @throws             <code>SPIException</code> if error occurs 
     */
    public abstract void setPriority(int aPriority) 
    	throws SPIException;

    /**
     * Sets the display name for this profile.
     *
     * @param aDisplayName  new display name for this profile
     * @throws              <code>SPIException</code> if error occurs 
     */
    public abstract void setDisplayName(String aDisplayName) 
    	throws SPIException;
 
    /**
     * Stores a <code>Policy</code>.
     *
     * @param aPolicy        the policy to store
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract void storePolicy(Policy aPolicy) 
    	throws SPIException;

    /**
     * Destroys a <code>Policy</code>.
     *
     * @param aPolicy            the policy object
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract void destroyPolicy(Policy aPolicy) 
    	throws SPIException;
 
    /**
     * Returns a boolean indicating whether or not this profile
     * has policies. 
     *
     * @return   <code>true</code> if there are policies, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public abstract boolean hasPolicies() throws SPIException;

    /**
     * Returns the policies for this profile.
     *
     * @return               <code>Iterator</code> of all the policies for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Iterator getPolicies() throws SPIException;

    /**
     * Returns the policies for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Iterator getPolicies(Iterator aPolicyIdList) 
        throws SPIException;

    /**
     * Returns the PolicyInfos for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the PolicyInfos 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Iterator getPolicyInfos(Iterator aPolicyIdList) 
        throws SPIException;

    /**
     * Returns the requested policy object.
     *
     * @param aId   the id for the required policy
     * @return      the policy object 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Policy getPolicy(String aId) throws SPIException;

    /**
     * Returns a boolean indicating whether or not this profile
     * has been assigned to entities. 
     *
     * @return   <code>true</code> if there are entities, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public abstract boolean hasAssignedEntities() throws SPIException;

    /**
     * Returns the entities to which this profile has been assigned.
     *
     * @return               <code>Iterator</code> of entities 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Iterator getAssignedEntities() 
    	throws SPIException;

    /**
     * Sets the scope for this profile.
     *
     * @param aApplicabiltiy  scope 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract void setApplicability(Applicability aApplicability) 
    	throws SPIException;
    
    /**
      * Returns the time in milliseconds of the most 
      * recent modification of the profile entry and
      * its policies.
      *
      * @return    time of the last modification in milliseconds 
      * @throws               <code>SPIException</code> if error occurs 
      */
    public abstract long getLastModified() throws SPIException;

    /**
     * Returns the comment for this profile.
     *
     * @return          comment 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public abstract String getComment() throws SPIException;

    /**
     * Sets the comment for this profile object.
     *
     * @param aComment  description 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public void setExistingComment(String aComment) {
        if (aComment == null) { mNoCommentFound = true; }
        mComment = aComment;
    }

    /**
     * Sets the comment for this profile. If the new
     * comment is null then the existing comment is removed.
     *
     * @param aComment  comment as a String 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public abstract void setComment(String aComment) 
    	throws SPIException;

    /** 
      * Sets the author for the last modification of this 
      * profile entry.
      *
      * @param aAuthor   entity that last modified this profile entry
      */
    public void setAuthor(Entity aAuthor) {
        mAuthor = aAuthor;
    }

    /** 
      * Returns the author for the last modification of this 
      * profile.
      *
      * @return    name of the user that last modified this profile 
      * @throws    <code>SPIException</code> if error occurs 
      */
    public abstract String getAuthor() throws SPIException; 
}
