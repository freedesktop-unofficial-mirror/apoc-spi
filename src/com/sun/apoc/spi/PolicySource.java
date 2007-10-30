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
import com.sun.apoc.spi.entities.EntityTreeProvider ;
import com.sun.apoc.spi.environment.EnvironmentMgr;
import com.sun.apoc.spi.file.FileProviderFactory;
import com.sun.apoc.spi.ldap.LdapProviderFactory;
import com.sun.apoc.spi.profiles.ProfileProvider ;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
  * Class representing a policy source, i.e an entity provider, a profile 
  * provider and an assignment provider binding entities to profiles.
  */
public class PolicySource {
    /** Name of the source. */
    private String mName = null ;
    /** General environment. */
    private EnvironmentMgr mEnvironment = null ;
    /** Entity provider. */
    private EntityTreeProvider mEntities = null ;
    /** Profile provider. */
    private ProfileProvider mProfiles = null ;
    /** Assignment provider. */
    private AssignmentProvider mAssignments = null ;
    /** Failed URLs */
    private Set mFailedURLs;
    
    /**
      * Constructor from environment and name. Based on the name provided,
      * will extract from the environment the provider informations.
      *
      * @param aName        source name
      * @param aEnvironment bootstrap environment
      * @throws SPIException if an error occurs.
      */
    public PolicySource(String aName, 
                        EnvironmentMgr aEnvironment) throws SPIException {
        mName = aName ;
        mEnvironment = aEnvironment ;
        mFailedURLs = new HashSet();
        mEntities = (EntityTreeProvider) getProvider(EntityTreeProvider.class) ;
        mProfiles = (ProfileProvider) getProvider(ProfileProvider.class) ;
        mAssignments = 
                    (AssignmentProvider) getProvider(AssignmentProvider.class) ;
    }
    
    /**
      * Returns a copy of the environment table used to establish this object.
      *
      * @return     <code>Hashtable</code> containing environmental settings
      */
    public Hashtable getEnvironment() {
        return mEnvironment.getEnvironment();
    }
    
    /**
      * Returns root entity for this <code>PolicySource</code>.
      *
      * @return     <code>Entity</code> that is the root for this source
      * @throws     <code>SPIException</code> if error occurs 
     */
    public Entity getRoot() throws SPIException {
        return mEntities.getRootEntity();
    }
    
    /**
      * Returns the entity with the given EntityId in this <code>PolicySource</code>.
      *
      * @param aId  the EntityId to look for
      * @return     <code>Entity</code> that is the root for this source
      * @throws     <code>SPIException</code> if error occurs 
      */
    public Entity getEntity(String aId) throws SPIException {
        return mEntities.getEntity(aId);
    }
    
    /**
      * Returns the EntityProvider object for this <code>PolicySource</code>.
      *
      * @return     <code>EntityProvider</code> for this source
      */    
    public EntityTreeProvider getEntityProvider() {
        return mEntities;
    }
    
    /**
      * Returns the AssignmentProvider object for this <code>PolicySource</code>.
      *
      * @return     <code>AssignmentProvider</code> for this source
      */    
    public AssignmentProvider getAssignmentProvider() {
        return mAssignments;
    }
    
    /**
      * Returns the ProfileProvider object for this <code>PolicySource</code>.
      *
      * @return     <code>ProfileProvider</code> for this source
      */        
    public ProfileProvider getProfileProvider() {
        return mProfiles;
    }
 
    /**
      * Returns the name for this policy <code>PolicySource</code>.
      *
      * @return  name of this policy source
      */     
    public String getName() {
        return mName;
    }
    
     /**
     * Closes the sessions on the different providers
     *
     * @throws             <code>SPIException</code> if error occurs 
     */
    public void close() throws SPIException {
        HashSet providerSet = new HashSet();
        if (mEntities != null) {
            providerSet.add(mEntities);
        }
        if (mAssignments != null) {
            providerSet.add(mAssignments);
        }
        if (mProfiles != null) {
            providerSet.add(mProfiles);
        }
        Iterator iterProviderSet = providerSet.iterator();
        while (iterProviderSet.hasNext()) {
            ((Provider)iterProviderSet.next()).close();
        }
    }
    
    /**
     * Indicates if the url has been referenced as a failing URL
     * by a previous connection attempt
     * @param url   the URL to check
     * @return      true if it is a failed URL
     */
    public synchronized boolean isFailedURL(String url) {
        return mFailedURLs.contains(url);
    }
    
    /**
     * Add the url in the list referencing the failing URLs
     * @param url   failing url to reference
     */
    public synchronized void addFailedURL(String url) {
        if (!isFailedURL(url)) {
            mFailedURLs.add(url);
        }
    }
    
    /**
     * gets the object stored under the key url 
     * and managing the connection to that url
     * 
     * @param stringURL     the string representing the URL 
     *                      for the connection
     * @return              an object managing the connection 
     *                      or null if one does not exist
     */
    public synchronized Object getConnectionHandler(String stringURL) {
        return mEnvironment.getConnectionHandler(stringURL);
    }
    
    /**
     * stores a connection handler under the key url
     * 
     * @param stringURL     the string representing the URL 
     * @param connection    the object managing the connection
     */
    public synchronized void setConnectionHandler(String stringURL, Object connection) {
        mEnvironment.setConnectionHandler(stringURL, connection);
    }
    
    /**
      * Gets the provider associated with a particular type of data (entities,
      * assignments or profiles). Uses the environment to find either a class
      * name or a list of URLs describing the provider parameters. The only
      * supported schemes for providers are LDAP or file-based ones, the actual 
      * generation of the appropriate provider is delegated to a factory in the 
      * backend-specific packages.
      *
      * @param aProviderClass   expected provider class
      * @return provider instance
      * @throws SPIException if an error occurs.
      */
    private Provider getProvider(Class aProviderClass) throws SPIException {
        Provider retCode = null ;
        String providerClass = mEnvironment.getProviderClass(mName, 
                                                             aProviderClass) ;

        if (providerClass != null) { return loadProvider(providerClass) ; }
        String [] urls = mEnvironment.getProviderURLs(mName, aProviderClass) ;

        for (int i = 0 ; retCode == null && i < urls.length ; ++ i) {
            String url = urls [i] ;

            if (isFailedURL(url)) { continue ; }
            try {
                String protocol = url.substring(0, url.indexOf(":"));
                if (mEnvironment.isLdapProtocol(protocol)) {
                    retCode = LdapProviderFactory.get(url, aProviderClass, 
                                                      this) ;
                    if (retCode != null) { retCode.open(); }
                }
                else if (mEnvironment.isFileProtocol(protocol)) {
                    retCode = FileProviderFactory.get(url, aProviderClass,
                                                      this) ;
                    if (retCode != null) { retCode.open(); }
                }
                else {
                    // Unsupported type, forget about this one.
                    continue ;
                }
            }
            catch (SPIException exception) {
                if (i == urls.length -1) {
                    // They've all failed, not good.
                    throw exception ;
                }
                // Let's not repeat the same mistake twice.
                addFailedURL(url) ;
            }
        }
        return retCode ;
    }

    /**
      * Loads a provider from its class name. It's expected said class has a 
      * constructor taking a PolicySource as its sole parameter.
      *
      * @param aProviderClass   provider class name
      * @return provider instance
      * @throws SPIException if the class cannot be loaded or its constructor
      *                      fails to execute.
      */
    private Provider loadProvider(String aProviderClass) throws SPIException {
        try {
            Class providerClass = 
                this.getClass().getClassLoader().loadClass(aProviderClass) ;
            Class [] parameterClasses = { PolicySource.class } ;
            Constructor providerConstructor = 
                                providerClass.getConstructor(parameterClasses) ;
            Object [] parameters = { this } ;

            return (Provider)providerConstructor.newInstance(parameters) ;
        }
        catch (Exception exception) {
            throw new ProviderLoadingException(aProviderClass, exception) ;
        }
    }

}

