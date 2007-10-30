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

package com.sun.apoc.spi ;

import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import java.util.HashMap ;
import java.util.Hashtable ;
import java.util.Map ;

import com.sun.apoc.spi.environment.EnvironmentMgr ;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileProvider;
import com.sun.apoc.spi.profiles.ProfileRepository;
import java.util.ArrayList;
import java.util.Iterator;

/**
  * Main access point to the policy management objects, basically maps to an
  * instance of the meta-configuration file.
  */
public class PolicyManager {
    /** Meta-configuration of the policy management. */
    private EnvironmentMgr mEnvironment = null ;
    /** Ordered list of sources (by order of use). */
    private PolicySource [] mSources = null ;
    /** Map between source names and sources (for access to source by type). */
    private Map mSourcesByName = new HashMap() ;

    
    /**
      * Constructor from a hashtable providing values for the meta-configuration
      * of the policy access (location of entity trees, storage of profiles and
      * their assignments).
      *
      * @param aBootstrapInfo   hashtable with bootstrap information
      * @throws SPIException if an error occurs.
      */
    public PolicyManager(Hashtable aBootstrapInfo) throws SPIException {
        mEnvironment = new EnvironmentMgr(aBootstrapInfo) ;
        mEnvironment.checkEnvironment();
        String [] sources = mEnvironment.getSources() ;

        mSources = new PolicySource [sources.length] ;
        for (int i = 0 ; i < sources.length ; ++ i) {
            mSources [i] = new PolicySource(sources [i], mEnvironment) ;
            mSourcesByName.put(sources [i], mSources [i]) ;
        }
    }
    
    /**
      * Returns the entity provider for a given source.
      *
      * @param aSource  source name
      * @return entity provider or null if it doesn't exist
      */
    public EntityTreeProvider getEntityProvider(String aSource) {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;

        return source != null ? source.getEntityProvider() : null ;
    }
    
    /**
      * Returns the assignment provider for a given source.
      *
      * @param aSource  source name
      * @return assignment provider or null if it doesn't exist
      */
    public AssignmentProvider getAssignmentProvider(String aSource) {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;

        return source != null ? source.getAssignmentProvider() : null ;
    }
    
    /**
      * Returns the profile provider for a given source.
      *
      * @param aSource source name
      * @return profile provider or null if it doesn't exist
      */
    public ProfileProvider getProfileProvider(String aSource) {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;

        return source != null ? source.getProfileProvider() : null ;
    }
    
    /**
      * Returns the root entity for a given source.
      *
      * @param aSource source name
      * @return root entity or null if it doesn't exist
      */
    public Entity getRootEntity(String aSource) throws SPIException {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;

        return source != null ? source.getRoot() : null ;
    }    

    /**
      * Returns the entity with given id for a given source.
      *
      * @param aSource source name
      * @param aId entity id
      * @return entity or null if it doesn't exist
      */
    public Entity getEntity(String aSource, String aId) throws SPIException {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;

        return source != null ? source.getEntity(aId) : null ;
    }  

    /**
      * Returns the entity with given id from all sources.
      *
      * @param aId entity id
      * @return entity or null if it doesn't exist
      */    
    public Entity getEntity(String aId) throws SPIException {
        Entity sEntity = null;
        for (int i = 0 ; i < mSources.length && sEntity == null; ++ i) {
            sEntity = getEntity(mSources[i].getName(), aId);
        }

        return sEntity ;
    }     
    
    /**
      * Returns the profile with given id for a given source.
      *
      * @param aSource source name
      * @param aId profile id
      * @return profile or null if it doesn't exist
      */
    public Profile getProfile(String aSource, String aId) throws SPIException {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;
        ProfileProvider provider = source.getProfileProvider();
        Profile profile = null;
        if (provider != null) {
            try {
                profile = provider.getProfile(aId);
                if (profile != null) {
                    if (!profile.getApplicability().getStringValue().equals(source.getName())) {
                       profile = null;
                    }
                }
            } catch (SPIException e) {
            }
        }
        return profile;
    }  

    /**
      * Returns the profile with given id from all sources.
      *
      * @param aId profile id
      * @return profile or null if it doesn't exist
      */    
    public Profile getProfile(String aId) throws SPIException {
        Profile sProfile = null;
        for (int i = 0 ; i < mSources.length && sProfile == null; ++ i) {
            sProfile = getProfile(mSources[i].getName(), aId);
        }
        return sProfile ;
    }

    /**
     * Returns all the <code>Profile</code>s.
     *
     * @return      an Iterator over all the <code>Profile</code> objects 
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Iterator getAllProfiles() throws SPIException {
        ArrayList sProfileList = new ArrayList();
        for (int i = 0 ; i < mSources.length; ++ i) {
            ProfileProvider sProvider = mSources[i].getProfileProvider();
            Iterator it = sProvider.getAllProfiles();
            while (it.hasNext()) {
                Profile sProfile = (Profile)it.next();
          //      if (!sProfileList.contains(sProfile)) {
                    sProfileList.add(sProfile);
         //       }
            }
        }
        return sProfileList.iterator() ;       
    }    
    
    /**
      * Returns the profile repository with given id for a given source.
      *
      * @param aSource source name
      * @param aId profile repository id
      * @return profile repository or null if it doesn't exist
      */
    public ProfileRepository getProfileRepository(String aSource, String aId) throws SPIException {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;
        ProfileProvider provider = source.getProfileProvider();
        return provider != null ? provider.getProfileRepository(aId) : null ;
    }     

    /**
      * Returns the default profile repository for a given source.
      *
      * @param aSource source name
      * @return default profile repository or null if it doesn't exist
      */
    public ProfileRepository getDefaultProfileRepository(String aSource) throws SPIException {
        PolicySource source = (PolicySource)mSourcesByName.get(aSource) ;
        ProfileProvider provider = source.getProfileProvider();
        return provider != null ? provider.getDefaultProfileRepository() : null ;
    }   
    
    /**
      * Returns a copy of the environment table used to establish this object.
      *
      * @return     <code>Hashtable</code> containing environmental settings
      */
    public Hashtable getEnvironment() throws SPIException {
        return mEnvironment.getEnvironment();
    }
   
    /**
      * Returns the array of policy source names used by this policy manager instance.
      *
      * @return     <code>Hashtable</code> containing environmental settings
      */
    public String[] getSources() throws SPIException {
        return mEnvironment.getSources();
    }
    
    /**
     * Closes the sessions on the different providers
     *
     * @throws             <code>SPIException</code> if error occurs 
     */
    public void close() throws SPIException {
        for (int i = 0 ; i < mSources.length ; ++ i) {
            mSources[i].close();
        }
    }
}

