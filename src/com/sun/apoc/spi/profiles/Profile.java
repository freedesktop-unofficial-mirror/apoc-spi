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

import com.sun.apoc.spi.AssignmentProvider;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.policies.Policy;

/**
  * Interface for a profile.
  *
  */
public interface Profile
{
    /**
     * Returns the id for this profile.
     *
     * @return             id for this profile
     */
    public String getId() ;

    /**
     * Returns the display name for this profile.
     *
     * @return             display name for this profile
     */
    public String getDisplayName() ;

    /**
     * Returns the priority for this profile.
     *
     * @return             priority for this profile
     */
    public int getPriority() ;

    /**
      * Returns the time of the last modification of the profile.
      *
      * @return    time of the last modification in milliseconds 
      * @throws    <code>SPIException</code> if error occurs           
      */
    public long getLastModified() throws SPIException;

    /**
     * Sets the priority for this profile.
     *
     * @param aPriority     priority for this profile
     * @throws             <code>SPIException</code> if error occurs 
     */
    public void setPriority(int aPriority) throws SPIException;

    /**
     * Sets the display name for this profile.
     *
     * @param aDisplayName  new display name for this profile
     * @throws              <code>SPIException</code> if error occurs 
     */
    public void setDisplayName(String aDisplayName) 
    	throws SPIException;
 
    /**
     * Stores a <code>Policy</code>.
     *
     * @param aPolicy        the policy to store
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void storePolicy(Policy aPolicy) throws SPIException;

    /**
     * Destroys a <code>Policy</code>.
     *
     * @param aPolicy            the policy object
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void destroyPolicy(Policy aPolicy) throws SPIException;
 
    /**
     * Returns a boolean indicating whether or not this profile
     * has policies. 
     *
     * @return   <code>true</code> if there are policies, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean hasPolicies() throws SPIException;

    /**
     * Returns the policies for this profile.
     *
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicies() throws SPIException;

    /**
     * Returns the policies for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicies(Iterator aPolicyIdList) 
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
    public Iterator getPolicyInfos(Iterator aPolicyIdList) 
        throws SPIException;

    /**
     * Returns the requested policy object.
     *
     * @param aId   the id for the required policy
     * @return      the policy object or null if no policy
     * 				with aId exists
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Policy getPolicy(String aId) throws SPIException;

    /**
     * Returns a boolean indicating whether or not this profile
     * has been assigned to entities. 
     *
     * @return   <code>true</code> if there are entities, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean hasAssignedEntities() throws SPIException;

    /**
     * Returns the entities to which this profile has been assigned.
	 * Returns only the entities that have a type 
	 * compatible with the profile Applicability.
     *
     * @return               <code>Iterator</code> of entities 
     * @throws               <code>SPIException</code> if error occurs 
	 * @see AssignmentProvider#assignProfile(Entity, Profile)
     */
    public Iterator getAssignedEntities() throws SPIException;

    /**
     * Returns the scope for this profile.
     *
     * @return               scope 
     */
    public Applicability getApplicability() ;

    /**
     * Sets the scope for this profile.
     *
     * @param aApplicability  scope 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void setApplicability(Applicability aApplicability) 
    	throws SPIException;

    /**
     * Returns the comment for this profile.
     *
     * @return          comment 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public String getComment() throws SPIException;

    /**
     * Sets the comment for this profile. If the new 
     * comment is null then the existing comment is removed.
     *
     * @param aComment  comment as a String 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public void setComment(String aComment) throws SPIException;

    /** 
      * Returns the author for the last modification of this 
      * profile.
      *
      * @return    name of the user that last modified this profile 
      * @throws    <code>SPIException</code> if error occurs 
      */
    public String getAuthor() throws SPIException; 

    /**
     * Returns the <code>ProfileRepository</code> for this profile.
     *
     * @return             <code>ProfileRepository</code> 
     */
    public ProfileRepository getProfileRepository();
}
