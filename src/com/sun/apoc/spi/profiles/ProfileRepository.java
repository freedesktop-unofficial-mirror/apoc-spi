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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;

/**
  * Interface for a profile repository.
  *
  */
public interface ProfileRepository
{
    /** Possible formats for import/export. */
    public static final int FORMAT_ZIP = 1 ;
    
    /**
     * Creates and returns a <code>Profile</code> object.
     * 
     * @param aDisplayName   the display name for the profile
     * @param aApplicability applicablity of profile
     * @return               <code>Profile</code> object 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Profile createProfile(String aDisplayName, 
            					 Applicability aApplicability) 
    	throws SPIException;
 
    /**
     * Deletes a <code>Profile</code> object.
     *
     * @param aProfile   the <code>Profile</code> object
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void destroyProfile(Profile aProfile) 
        throws SPIException;

    /**
     * Exports a <code>Profile</code> object.
     *
     * @param aProfile   the <code>Profile</code> object
     * @param aOutput        output stream
     * @throws               <code>SPIException</code> if error occurs 
     */
    public void exportProfile(Profile aProfile,
            				  OutputStream aOutput)
    	throws SPIException;

     /**
      * Imports a profile from a stream in default format.
      *
      * @param aDisplayName     display name for profile
      * @param aApplicability   scope
      * @param aInput           stream from which the profile will be imported
      * @throws SPIException if an error occurs.
      */
     public void importProfile(String aDisplayName, 
             				   Applicability  aApplicability, 
             				   InputStream aInput)
     	throws SPIException;

    /**
     * Returns the requested profile or null if it does
     * not exist. 
     *
     * @param aId            id for the required profile
     * @return               object representing profile 
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Profile getProfile(String aId) throws SPIException;

    /**
     * Returns the profiles that match the filter string. 
     *
     * @param aApplicability scope of profiles required
     * @return             <code>Iterator</code> of profiles matching
     *                     the filter
     * @throws             <code>SPIException</code> if error occurs 
     */
    public Iterator getProfiles(Applicability aApplicability) 
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
    public boolean isReadOnly() throws SPIException;

    /**
     * Returns the id for this profile repository.
     *
     * @return             id for this repository
     */
    public String getId();
    
    /**
      * Finds a profile object given its displayname 
      *
      * @param aDisplayName     display name for profile
      * @return                 object representing the profile
      * 						or null if not found
      * @throws                 <code>SPIException/code> if error
      *                         occurs
      */
    public Profile findProfile(String aDisplayName) throws SPIException; 

    /**
      * Returns the container entity.
      *
      * @return    the container entity
     * @throws     <code>SPIException</code> if error occurs 
      */
    public Entity getEntity() throws SPIException;
}
