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

package com.sun.apoc.spi.file.profiles;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.IllegalWriteException;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepositoryImpl;

public class FileProfileRepository extends ProfileRepositoryImpl
{
    public final static String PREFIX = "PROFILE_REPOSITORY_";
    public final static String DEFAULT_ID = "default";
    
    private boolean mProfilesLoaded;
    private File mFile;
    boolean mReadOnly;
    boolean mRemote;
    URL mRootLocation;
    
    
    public FileProfileRepository( PolicySource aPolicySource, String aId, URL aRootPath ) throws SPIException
    {
        mId = aId;
        mPolicySource = aPolicySource;  
        mProfilesLoaded = false;
        mRootLocation = aRootPath;
        
        mReadOnly = true;
        if ( !aRootPath.getProtocol().equals("file") ) {
            mRemote = true;            
        } else {
            mRemote = false;            
            try {
                mFile = new File( new URI( mRootLocation.toString() ) );        
            } catch (URISyntaxException e) {
                throw new IllegalReadException(
                        IllegalReadException.FILE_NAME_READ_KEY,
                        mRootLocation.toString(), e);
            }
            
            mReadOnly = isRepositoryReadOnly();
        }
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepositoryImpl#findProfile(java.lang.String, com.sun.apoc.spi.profiles.Applicability)
     */
    public Profile findProfile( String aDisplayName ) throws SPIException
    {
        List profileList;
        Profile profile;
        Profile actual;
        Iterator it;
        
        if ( mRemote ) {
            throw new UnsupportedOperationException();
        }
        profileList = loadAllProfiles();
        profile = null;
        it = profileList.iterator();
        while ( it.hasNext() ) {
            actual = (Profile) it.next();            
            if ( actual.getDisplayName().equals(aDisplayName) ) {
                profile = actual;
            }
        }
        
        return profile;
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#getProfilePriorities(com.sun.apoc.spi.profiles.Applicability)
     */
    public Iterator getProfilePriorities(Applicability aApplicability)
    throws SPIException
    {
        List selectedProfileList;
        List profileList;
        Profile profile;
        Iterator it;
        
        profile = null;
        if ( mRemote ) {
            throw new UnsupportedOperationException();
        }
        profileList = loadAllProfiles();
        selectedProfileList = new LinkedList();
        it = profileList.iterator();
        while ( it.hasNext() )
        {
            profile = (Profile)it.next();
            if ( profile.getApplicability() == aApplicability )
            {
                selectedProfileList.add( profile );
            }
        }
        
        return selectedProfileList.iterator();
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#createProfile(java.lang.String, int, com.sun.apoc.spi.profiles.Applicability)
     */
    protected Profile createTheProfile(String aDisplayName, Applicability aApplicability, int aPriority) throws SPIException {
        Profile profile;
        
        if ( mReadOnly ) {
            throw new IllegalWriteException();
        }
        
        profile = FileProfile.createNewProfile( this, aDisplayName, aApplicability, aPriority, mPolicySource );
        return profile;
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#destroyProfile(com.sun.apoc.spi.profiles.Profile)
     */
    public void deleteProfile(Profile aProfile) throws SPIException
    {
        String profileId;
        profileId = aProfile.getId();
        
        if ( mReadOnly ) {
            throw new IllegalWriteException();
        }
        
        try {
            URI fileURI;
            File file;
            // get the FileName
            fileURI = new URI( FileProfile.getProfileURL(profileId, this).toString() );
            
            file = new File( fileURI.getPath() );
            if ( !file.delete() ) 
            {
                // Can not delete the profile
                throw new IllegalWriteException(
                        IllegalWriteException.FILE_WRITE_KEY);
            }
        } catch (URISyntaxException e) {                
            throw new IllegalWriteException(
                    IllegalWriteException.FILE_WRITE_KEY, e);
        }

        aProfile = null;        
    }
    
    
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#getProfile(java.lang.String)
     */
    public Profile getProfile(String aId) throws SPIException
    {
        return FileProfile.loadProfile(aId, this, mPolicySource );        
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#getProfiles(com.sun.apoc.spi.profiles.Applicability)
     */
    protected TreeSet getTheProfiles(Applicability aApplicability)
    throws SPIException
    {
        String id;
        Iterator it;
        TreeSet profileApplicableList;
        List profileList;
        Profile profile;
        
        
        if ( mRemote ) {
            throw new UnsupportedOperationException();
        }
        profileApplicableList = new TreeSet(new FileProfileComparator());         
        profileList = loadAllProfiles();
        it = profileList.iterator();
        while ( it.hasNext() )
        {
            profile = (Profile) it.next();
            if ( aApplicability.equals( Applicability.ALL ) ) {
                profileApplicableList.add( profile );
            } else if ( profile.getApplicability().equals( aApplicability ) ) {
                profileApplicableList.add( profile );
            }          
        }
        
        return profileApplicableList;       
    }
    
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#getId()
     */
    public String getId()
    {
        return mId;
    }
    
    private List readProfileList() throws SPIException {
        List profileIdList;
        String[] fileList;
        String fileName;
        URL profileURL;
        String id;
        int end;
        
        profileIdList = new LinkedList();
        
        // Read the contents file and add the profile URLs to the profile list
        String profileName;
        fileList = mFile.list();
        for ( int i = 0 ; i < fileList.length; i++) {
            profileName = fileList[i];
            // get the ID of the profile
            end = profileName.lastIndexOf(".zip");
            if ( end == -1 ) { continue; } // TODO log this
            id = profileName.substring(0, end);
            profileIdList.add( id );
        }
        
        return profileIdList;        
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#isReadOnly()
     */
    public boolean isReadOnly() throws SPIException
    {
        return mReadOnly;
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileRepository#getEntity()
     */
    public Entity getEntity() throws SPIException
    {
        Entity entity = mPolicySource.getEntityProvider().getEntity(mId); 
        
        return entity;
    }
    
    protected String getLocation()
    {
        return mRootLocation.toString();
    }
    
    protected boolean priorityExists( int aPriority ) throws SPIException {
        Iterator profiles = getProfiles(Applicability.ALL );        
        
        while ( profiles.hasNext() ) {
            if ( ( (Profile)profiles.next()).getPriority() == aPriority ) {
                return true;
            }
        }
        
        return false;
    }
    
    protected void createStorage() {
        if ( !mFile.exists() ) {
            mFile.mkdirs();
        }
    }
    
    private List loadAllProfiles() throws SPIException
    {
        List profileIdList;
        List profilesList;
        String profileId;
        Profile profile;
        Iterator it;
        String id;
        
        profilesList = new LinkedList();        
        if ( mFile.exists() ) {
            // only read the profiles if the directory exists
            profileIdList = readProfileList();
            it = profileIdList.iterator();
            while( it.hasNext() )
            {
                id = (String)it.next();
                profileId = FileProfile.createId( id, mId );
                profile = getProfile( profileId );
                profilesList.add( profile );            
            }
        }
        return profilesList;
    }
    
    private boolean isRepositoryReadOnly()
    {
        File parent;
        parent = mFile;
        
        do {         
            if (parent.exists() ) {
                return !parent.canWrite();
            }
        } while ( (parent = parent.getParentFile() ) != null );

        /* should not happend */
        return false;
    }
}
