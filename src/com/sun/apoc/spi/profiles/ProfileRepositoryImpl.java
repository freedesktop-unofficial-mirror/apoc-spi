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

import com.sun.apoc.spi.PolicySource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.TreeSet;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.util.ImporterExporter;
import com.sun.apoc.spi.util.ZipImporterExporter;
/**
  * Abstract class for a profile repository.
  *
  */
public abstract class ProfileRepositoryImpl implements ProfileRepository
{
    protected String mEntityId;
    protected String mId;
    protected PolicySource  mPolicySource;
    
    public PolicySource getPolicySource() {
        return mPolicySource;
    }
   
    /**
     * returns the maximum priority assigned to the profiles
     * contained in this repository and with the specified scope
     * 
     * @param aApplicability scope of the profiles
     * @return maximum priority in use
     * @throws SPIException if error occurs
     */
    public int getMaxPriority (Applicability aApplicability) 
		throws SPIException {
        int highestPriority = 0;
        Iterator profiles = getProfiles(aApplicability);
        while (profiles.hasNext()) {
            Profile profile = (Profile)profiles.next();
            int priority = profile.getPriority();
            if (priority > highestPriority) {
                highestPriority = priority;
            }
        }
        return highestPriority;
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
    public  abstract Profile findProfile(String aDisplayName)
            throws SPIException; 

    /**
     * Creates and returns a <code>Profile</code> object.
     * 
     * @param aDisplayName   the display name for the profile
     * @param aApplicability applicablity of profile
     * @return               <code>Profile</code> object 
     * @throws               SPIException if error occurs 
     * @throws InvalidDisplayNameException if aDisplayName is null
     * @throws UnknownApplicabilityException if aApplicability is unknown
     */
    public Profile createProfile(String aDisplayName, 
							     Applicability aApplicability)
    		throws SPIException {
        if ((aDisplayName == null) || (aDisplayName.length() == 0)) {
            throw new InvalidDisplayNameException();
        }
        if (aApplicability.equals(Applicability.UNKNOWN)) { 
            throw new UnknownApplicabilityException();
        }
        int priority = getMaxPriority(aApplicability) + 1;
        return createTheProfile(aDisplayName, aApplicability, priority);
    }
    
    protected abstract Profile createTheProfile (String aDisplayName,
		     							Applicability aApplicability,
		     							int priority)
			throws SPIException;
 
    /**
     * Deletes a <code>Profile</code> object.
     *
     * @param aProfile       the <code>Profile</code> object
     * @throws               SPIException if error occurs 
     * @throws InvalidProfileException if aProfile 
     * 			is assigned to entities
     */
    public void destroyProfile(Profile aProfile) 
        throws SPIException {
        // Check if the profile is assigned to an entity.
        if (aProfile.hasAssignedEntities()) {
            throw new InvalidProfileException(
                    InvalidProfileException.ASSIGNED_PROFILE_KEY);
        }
        deleteProfile( aProfile );
    }

    /**
     * Exports a <code>Profile</code> object.
     *
     * @param aProfile   the <code>Profile</code> object
     * @param aOutput        output stream
     * @throws               SPIException if error occurs 
     * @throws NullProfileException if aProfile is null
     * @throws ProfileStreamException if aOutput is null
     */
    public void exportProfile(Profile aProfile,
            				  OutputStream aOutput) 
    	throws SPIException {
        if (aProfile == null) {
            throw new NullProfileException();
        }
        if (aOutput == null) {
            throw new ProfileStreamException(
                    ProfileStreamException.NULL_STREAM_KEY);
        }
        ImporterExporter exporter = new ZipImporterExporter();
        exporter.exportProfile(aProfile, aOutput);
    }

    /**
     * Imports a profile from a stream in default format.
     *
     * @param aDisplayName     display name for profile
     * @param aApplicability   scope
     * @param aInput           stream from which the profile will
     * 						   be imported
     * @throws SPIException if an error occurs.
     * @throws InvalidDisplayNameException if aDisplayName is null
     * @throws UnknownApplicabilityException if aApplicability is unknown
     * @throws ProfileStreamException if aInput is null
     */
    public void importProfile(String aDisplayName, 
            				  Applicability aApplicability,
             				  InputStream aInput)
     		throws SPIException {
        if ((aDisplayName == null) || (aDisplayName.length() == 0)) {
            throw new InvalidDisplayNameException();
        }
        if (aApplicability != null && aApplicability.equals(Applicability.UNKNOWN)) {
            throw new UnknownApplicabilityException();
        }
        if (aInput == null) {
            throw new ProfileStreamException(
                    ProfileStreamException.NULL_STREAM_KEY);
        }
        ImporterExporter importer = new ZipImporterExporter();
        importer.importProfile(this, aDisplayName, aApplicability, 
                			   aInput);
     }

    /**
     * Returns the requested profile or null if it does
     * not exist. 
     *
     * @param aId            id for the required profile
     * @return               object representing profile 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public abstract Profile getProfile(String aId) 
    	throws SPIException;

    /**
     * Returns the profiles that match the filter string. 
     *
     * @param aApplicability scope of profiles required
     * @return             <code>Iterator</code> of profiles matching
     *                     the filter
     * @throws             SPIException if error occurs 
     * @throws UnknownApplicabilityException if aApplicability is unknown
     */
    public Iterator getProfiles(Applicability aApplicability) 
    		throws SPIException {
        if (aApplicability.equals(Applicability.UNKNOWN)) { 
            throw new UnknownApplicabilityException();
        }
        return getTheProfiles(aApplicability).iterator();
    }
    
    protected abstract TreeSet getTheProfiles (Applicability aApplicability) 
			throws SPIException;

    /**
     * Returns a boolean indicating if the current user has read only
     * access for this profile repository.
     *
     * @return             <code>true</code> if the current user has
     * 						read only access
     *                     otherwise <code>false</code>
     * @throws             <code>SPIException</code> if error occurs 
     */
    public abstract boolean isReadOnly() throws SPIException;

    /**
     * Returns the id for this profile repository.
     *
     * @return             id for this repository
     */
    public String getId() {
        return mId;
    }

    /**
      * Returns the container entity.
      *
      * @return    the container entity
     * @throws     <code>SPIException</code> if error occurs 
      */
    public abstract Entity getEntity() throws SPIException;
    
        
    /**
     * Deletes the profile <code>aProfile</code> from the repository
     *
     * @param aProfile      the <code>Profile</code> object
     * @throws              <code>SPIException</code> if error occurs 
     */
    protected abstract void deleteProfile( Profile aProfile ) 
        throws SPIException;
}
