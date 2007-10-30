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

package com.sun.apoc.spi.file.assignments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.apoc.spi.AbstractAssignmentProvider;
import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.IllegalWriteException;
import com.sun.apoc.spi.OpenConnectionException;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.environment.InvalidParameterException;
import com.sun.apoc.spi.environment.MissingParameterException;
import com.sun.apoc.spi.profiles.InvalidProfileException;
import com.sun.apoc.spi.profiles.Profile;

public class FileAssignmentProvider extends AbstractAssignmentProvider 
{
    private static final String DEFAULT_ASSIGN_CONTAINER = "assignments";
    private static final String DEFAULT_EXTENSION = ".dat";
    private URL mLocation;
    private Map mEntitiesAssign;
    private Map mProfilesAssign;
    private boolean mDataIsLoaded;
    private boolean mLoadingData;
    private boolean mReadOnly;
    private boolean mRemote;
    private File mFile;
    
    public FileAssignmentProvider( PolicySource aPolicySource, String aUrl ) throws SPIException {
        StringBuffer location;
        
        if ( (aUrl == null) || (aUrl.length() == 0)) {
            throw new MissingParameterException("domain "+EnvironmentConstants.URL_KEY);
        }
        mPolicySource = aPolicySource;
        location = new StringBuffer(aUrl);
        if ( location.charAt(location.length() - 1) != '/' ) {
            location.append('/');
        }
        if(!(location.toString().endsWith(DEFAULT_ASSIGN_CONTAINER + "/"))) {
            location.append(DEFAULT_ASSIGN_CONTAINER).append('/');
        }
        
        try {
            mLocation = new URL( location.toString() );
            if ( mLocation.getProtocol().equals(EnvironmentConstants.FILE_URL_PROTOCOL) ) {
                mRemote = false;
            } else {
                mRemote = true;
            }
        } catch (MalformedURLException e) {
            throw new InvalidParameterException(
                    "assignment "+EnvironmentConstants.URL_KEY,
                    location.toString());
        }
    }
    
    
    public Iterator getEntitiesAssignedToProfile(Profile aProfile ) throws SPIException {
        List entityList;
        String[] fileList;
        HashSet profileIDs;
        String entityId;
        int indx;
        
        if ( mRemote ) {
            throw new UnsupportedOperationException();
        }
        entityList = new LinkedList();
        // read list of files
        fileList = mFile.list();
        for ( int i = 0; i< fileList.length; i++ ) {
            try {
                indx = fileList[i].indexOf(DEFAULT_EXTENSION);
                if ( indx != -1 ) {
                    entityId = fileList[i].substring(0, indx).replace('+', ' ');
                    profileIDs = readAssignedProfileIDs( entityId );
                    if ( profileIDs.contains( aProfile.getId() ) ) {
                        entityList.add( getEntity( entityId ) );
                    }
                }
            } catch ( IOException e ) {
                // keep reading next file
            }
        }
        
        return entityList.iterator();
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.AssignmentProvider#assignProfile(com.sun.apoc.spi.entities.Entity, com.sun.apoc.spi.profiles.Profile)
     */
    public void assignProfileToEntity(Entity aEntity, Profile aProfile) throws SPIException {
        HashSet profileIDs = null;
        
        try {
            profileIDs = readAssignedProfileIDs( aEntity.getId() );
        } catch ( SPIException e ) {
            profileIDs = new HashSet();
        } catch ( IOException e ) {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        profileIDs.add( aProfile.getId() );
        writeAssignments( aEntity.getId(), profileIDs );
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.AssignmentProvider#unassignProfile(com.sun.apoc.spi.entities.Entity, com.sun.apoc.spi.profiles.Profile)
     */
    public void unassignProfile(Entity aEntity, Profile aProfile) throws SPIException {
        HashSet profileIDs = null;
        String profileId;
        
        profileId = aProfile.getId();
        
        // it the entity does not exists we get an exception here. It's ok can not unassign from an entity
        // who doesn't have profiles assigned
        try {
            profileIDs = readAssignedProfileIDs( aEntity.getId() );
        } catch ( SPIException e ) {
            // if the entity does not exists we get an exception here.
            // It's ok. Can not unassign from an entity
            // who doesn't have profiles assigned
            profileIDs = new HashSet();
        } catch ( IOException e ) {
            throw new InvalidProfileException(
                    InvalidProfileException.NO_EXIST_PROFILE_KEY,
                    profileId, e);
        }
        if ( profileIDs.contains( profileId ) ) {
            profileIDs.remove( profileId );
        } else {
            throw new InvalidProfileException(
                    InvalidProfileException.NO_EXIST_PROFILE_KEY,
                    profileId);
        }
        
        writeAssignments( aEntity.getId(), profileIDs );
    }
    
    public Iterator getProfilesAssignedToEntity( Entity aEntity ) throws SPIException {
        HashSet profileIDs;
        List profiles;
        try {
            profileIDs = readAssignedProfileIDs( aEntity.getId() );
        } catch ( SPIException e ) {
            profileIDs =  new HashSet();
        } catch (IOException e) {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        
        profiles = new LinkedList();
        
        Iterator it = profileIDs.iterator();
        
        while ( it.hasNext() ) {
            profiles.add(mPolicySource.getProfileProvider().getProfile( (String)it.next() ));
        }
        return profiles.iterator();
    }
    
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.AssignmentProvider#open()
     */
    public void open() throws SPIException {
        if ( mRemote ) {
            try {
                mLocation.openStream();
            } catch (IOException e) {
                // could not connect to the repository
                throw new OpenConnectionException(
                        mLocation.toString(), e);
            }
        } else {
            try {
                mFile = new File( new URI( mLocation.toString() ) );
                if ( !mFile.exists() ) {
                    throw new IllegalReadException(IllegalReadException.FILE_NAME_READ_KEY,mFile.getAbsolutePath());
                } else  if ( !mFile.isDirectory() ) {
                    throw new InvalidParameterException(
                            "profile "+EnvironmentConstants.URL_KEY,
                            mLocation.toString());
                }
                
                if ( mFile.canWrite() ) {
                    mReadOnly = false;
                } else {
                    mReadOnly = true;
                }
            } catch ( URISyntaxException e ){
                throw new InvalidParameterException(
                        "assignment "+EnvironmentConstants.URL_KEY,
                        mLocation.toString());
            }
        }
        
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.AssignmentProvider#close()
     */
    public void close() throws SPIException {
        // Nothing to do
        
    }
    
    private HashSet readAssignedProfileIDs( String aEntityId ) throws MalformedURLException, IOException, SPIException {
        BufferedReader reader = null;
        URL entityAssignments;
        HashSet profileIDs;
        String profileId;
        StringBuffer fileName;
        
        profileIDs = new HashSet();
        try {
            if ( mRemote ) {
                fileName = new StringBuffer(URLEncoder.encode(aEntityId,System.getProperty("file.encoding")));
                fileName.append(DEFAULT_EXTENSION);
                entityAssignments = new URL( mLocation, fileName.toString() );
                reader = new BufferedReader( new InputStreamReader( entityAssignments.openStream() ) );
            } else {
                fileName = new StringBuffer( aEntityId.replace(' ', '+') );
                fileName.append(DEFAULT_EXTENSION);
                reader = new BufferedReader( new FileReader( new File( mFile, fileName.toString() ) ) );
            }
        } catch ( MalformedURLException e ) {
            throw e;
        } catch (IOException e ) {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        try {
            while ( (profileId = reader.readLine() ) != null ) {
                // TODO check if the profile ID exists
                profileIDs.add( profileId );
            }
            reader.close();
        } catch (IOException e) {
            throw e;
        }
        
        return profileIDs;
    }
    
    private void writeAssignments( String aEntityId, HashSet aProfileIDs ) throws SPIException {
        PrintWriter writer;
        HashSet profileIDs;
        String profileId;
        Iterator it;
        StringBuffer fileName;
        
        
        try {
            fileName = new StringBuffer( aEntityId.replace(' ', '+') );
            fileName.append(DEFAULT_EXTENSION);
            writer = new PrintWriter( new FileWriter( new File( mFile, fileName.toString() )));
            it = aProfileIDs.iterator();
            while ( it.hasNext() ) {
                writer.println( (String)it.next() );
            }
            writer.close();
        } catch ( MalformedURLException e ) {
            throw new IllegalWriteException(
                    IllegalWriteException.FILE_WRITE_KEY, e);
        } catch (IOException e) {
            throw new IllegalWriteException(
                    IllegalWriteException.FILE_WRITE_KEY, e);
        }
    }
    
    private Entity getEntity( String aEntityId ) throws SPIException {
        Entity entity = null;
        
        entity = mPolicySource.getEntityProvider().getEntity(aEntityId);
        
        return entity;
    }
}
