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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.OpenConnectionException;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.environment.InvalidParameterException;
import com.sun.apoc.spi.environment.MissingParameterException;
import com.sun.apoc.spi.file.entities.FileEntity;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileComparatorProvider;
import com.sun.apoc.spi.profiles.ProfileProvider;
import com.sun.apoc.spi.profiles.ProfileRepository;

public class FileProfileProvider implements ProfileProvider, ProfileComparatorProvider
{
    private static final String DEFAULT_REP_CONTAINER = "profiles";
    private PolicySource mPolicySource;
    private boolean mRemoteRepository;
    private URL mLocation;

    public FileProfileProvider(PolicySource aPolicySource, String aUrl) throws SPIException
    {   
        if ( (aUrl == null) || (aUrl.length() == 0)) {
            throw new MissingParameterException("domain "+EnvironmentConstants.URL_KEY);
        }
        StringBuffer location;
        location = new StringBuffer(aUrl);        
        if ( location.charAt(location.length() - 1) != '/' ) {
            location.append('/');
        }
        if(!(location.toString().endsWith(DEFAULT_REP_CONTAINER + "/"))) {
            location.append(DEFAULT_REP_CONTAINER).append('/');
        }        
        
        try {
            mLocation = new URL( location.toString() );
        } catch ( MalformedURLException e ) {
            throw new InvalidParameterException(
                    "domain "+EnvironmentConstants.URL_KEY,
                    location.toString());
        }       
        mPolicySource = aPolicySource;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.profiles.ProfileProvider#getDefaultProfileRepository()
     */
    public ProfileRepository getDefaultProfileRepository() throws SPIException
    {
        return getProfileRepository(FileProfileRepository.DEFAULT_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.profiles.ProfileProvider#getProfileRepository(java.lang.String)
     */
    public ProfileRepository getProfileRepository(String aId) throws SPIException
    {
        ProfileRepository repository = null;
        StringBuffer path;
        URL profRepositoryURL;
        
        try {
            path = new StringBuffer( FileProfileRepository.PREFIX );                        
            path.append( aId.replace(' ', '+') );
            path.append( "/" );
            
            profRepositoryURL =  new URL(mLocation, path.toString() );
        } catch ( MalformedURLException e ) {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }

        repository = new FileProfileRepository(mPolicySource, aId, profRepositoryURL );

        return repository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.profiles.ProfileProvider#getProfile(java.lang.String)
     */
    public Profile getProfile(String aId) throws SPIException
    {
        // The Repository id in with a profile is stored is part of the profile
        // id.
        Profile profile = null;
        String[] ids;
        ids = aId.split("-");
        
        ProfileRepository repository = getProfileRepository(ids[0]);
        profile = repository.getProfile(aId);

        return profile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.profiles.ProfileProvider#open()
     */
    public void open() throws SPIException
    {
        File file;
        // checks if the repository exists
        if ( mLocation.getProtocol().equals(EnvironmentConstants.FILE_URL_PROTOCOL) ) {
            mRemoteRepository = false;
            try {
                file = new File( new URI(mLocation.toString()));
                if ( !file.exists() ) {
                    throw new IllegalReadException(IllegalReadException.FILE_NAME_READ_KEY, file.getAbsolutePath());
                } else  if ( !file.isDirectory() ) {
                    throw new InvalidParameterException(
                            "profile "+EnvironmentConstants.URL_KEY,
                            mLocation.toString());
                }
            } catch ( URISyntaxException e ) {
                // Should not happend
                throw new InvalidParameterException(
                        "profile "+EnvironmentConstants.URL_KEY,
                        mLocation.toString());
            }
        } else {
            mRemoteRepository = true;
            try {
                mLocation.openStream();
            } catch (IOException e) {
                // could not connect to the repository
                throw new OpenConnectionException(
                        mLocation.toString(), e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.profiles.ProfileProvider#close()
     */
    public void close() throws SPIException
    {
        // Nothing to do
    }


    /* (non-Javadoc)
     * @see com.sun.apoc.spi.profiles.ProfileProvider#getAllProfiles()
     */
    public Iterator getAllProfiles() throws SPIException {
        ProfileRepository repository;
        Iterator profFromRep;
        List profiles;
        String id;
        File file;
        
        if ( mRemoteRepository ) {
            // Operation not allowed in a remote repository
            throw new UnsupportedOperationException();
        }
        
        profiles = new LinkedList();
        try {
            File profileReps[];
            file = new File( new URI(mLocation.toString()));
            profileReps = file.listFiles();
            // Get profiles from each profile repository
            for ( int i = 0; i < profileReps.length; i++ ) {
                if ( profileReps[i].isDirectory() ) {
                    id = getRepIdFromFile( profileReps[i] );
                    if ( id != null ) {
                        repository = getProfileRepository( id );
                        profFromRep = repository.getProfiles(Applicability.getApplicability(mPolicySource.getName()));
                        addProfiles( profiles, profFromRep );
                    }
                }
            }
        } catch ( URISyntaxException e ) {         
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }

        return profiles.iterator();
    }
    
    public Iterator getAllProfiles(Entity startingEntity) throws SPIException {
        ProfileRepository repository;
        Iterator profFromRep;
        List profiles = new LinkedList();;
        String id;
        File file;
        
        if ( mRemoteRepository ) {
            // Operation not allowed in a remote repository
            throw new UnsupportedOperationException();
        }
        
        if ( (startingEntity instanceof LdapEntity) 
          && ((startingEntity.equals(mPolicySource.getRoot())))) {
            addProfiles(profiles, 
                    startingEntity.getProfileRepository().getProfiles(Applicability.getApplicability(mPolicySource.getName())));
            Iterator iterEntities = ((Node)startingEntity).getChildren();
            while (iterEntities.hasNext()) {
                Entity entity = (Entity)iterEntities.next();
                profiles.addAll(getAllProfilesInSubEntity(entity));
            }
        }
        else {
            profiles = getAllProfilesInSubEntity(startingEntity);
        }
        return profiles.iterator();
    }
    
    private List getAllProfilesInSubEntity (Entity entity) throws SPIException {
        List profiles = new LinkedList();
        ProfileRepository repository;
        Iterator profFromRep;
        String id;
        try {
            File profileReps[];
            File file = new File( new URI(mLocation.toString()));
            profileReps = file.listFiles();
            // Get profiles from each profile repository
            for ( int i = 0; i < profileReps.length; i++ ) {
                if ( profileReps[i].isDirectory() ) {
                    id = getRepIdFromFile( profileReps[i] );
                    if (( id != null ) && (isRepIdInSubEntity(id, entity))) {
                        repository = getProfileRepository( id );
                        profFromRep = repository.getProfiles(Applicability.getApplicability(mPolicySource.getName()));
                        addProfiles( profiles, profFromRep );
                    }
                }
            }
        } catch ( URISyntaxException e ) {         
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        return profiles;
    }
    
    public Comparator getProfileComparator() {
        return new FileProfileComparator();
    }
    
    private static void addProfiles(List aProfileList, Iterator aProfilesToAdd ) {
        
        while ( aProfilesToAdd.hasNext() ) {
            aProfileList.add((Profile)aProfilesToAdd.next() );
        }
    }
    
    private static String getRepIdFromFile( File aFile ) throws SPIException {
        String repId;
        String id = null;
        
        repId = aFile.getName();
        if ( repId.startsWith(FileProfileRepository.PREFIX ) ) {
            try {
                id = URLDecoder.decode(repId.substring( 
                        FileProfileRepository.PREFIX.length() ), 
                        System.getProperty("file.encoding"));
            } catch (UnsupportedEncodingException e) {
                // Shouldn't happend
                throw new IllegalReadException(
                        IllegalReadException.FILE_READ_KEY, e);
            }
        }
        
        return id;
    }
    
    private static boolean isRepIdInSubEntity (String repositoryId, Entity entity) 
            throws SPIException {
        boolean result = false;
        String entityRepositoryId = entity.getProfileRepository().getId();
        if (entity instanceof LdapEntity) {
            result = repositoryId.endsWith(entityRepositoryId);
        }
        else if (entity instanceof FileEntity) {
            result = repositoryId.startsWith(entityRepositoryId+FileEntity.ENTITY_SEPARATOR)
                  || repositoryId.equals(entityRepositoryId);
        }
        return result;
    }
}
