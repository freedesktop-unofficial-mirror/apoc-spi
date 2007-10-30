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

import java.util.Comparator;
import java.util.Iterator;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.ldap.LdapConnectionHandler;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.ldap.environment.LdapEnvironmentMgr;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileComparatorProvider;
import com.sun.apoc.spi.profiles.ProfileProvider;
import com.sun.apoc.spi.profiles.ProfileRepository;
/**
 * Provides access to the profiles stored in a LDAP backend
 * 
 */
public class LdapProfileProvider 
	implements ProfileProvider, ProfileComparatorProvider {
    
    protected PolicySource mPolicySource;
    protected LdapConnectionHandler mConnection;
    protected LdapDataStore mDataStore;
    protected LdapEnvironmentMgr mEnvironmentMgr;
    protected String mURL;

    public LdapProfileProvider(PolicySource aPolicySource, String url)
            throws SPIException {
        mPolicySource = aPolicySource;
        mEnvironmentMgr = new LdapEnvironmentMgr(mPolicySource.getEnvironment());
        String mURL = url;
        mConnection = (LdapConnectionHandler) mPolicySource.getConnectionHandler(mURL);
        if (mConnection == null) {
            // if mConnection is null, no Ldap connection has been
            // established yet, the environment hasn't been checked
            mEnvironmentMgr.checkEnvironment();
        }
    }

    /**
     * Returns a Comparator object used to order the Profiles
     * @return	a comparator to order Profiles
     */
    public Comparator getProfileComparator() {
        return new LdapProfileComparator();
    }
    
    /**
     * Opens the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     * @throws 			OpenConnectionException if connection error occurs
     * @throws 			CloseConnectionException if connection error occurs
     * @throws 			AuthenticateException if  connection error occurs
     */
    public void open() throws SPIException {
        if ((mConnection == null) && (mEnvironmentMgr != null)) {
            mConnection = new LdapConnectionHandler();
            mConnection.connect(mURL, 
    		        mEnvironmentMgr.getProfileTimeout(),
    		        mEnvironmentMgr.getProfileAuthUser(), 
	        		mEnvironmentMgr.getProfileAuthPassword(),
	        		mEnvironmentMgr);
            if (isGSSAPIAuthentication()){
                Object callbackHandler = 
                    mEnvironmentMgr.getProfileCallbackHandler();
                mConnection.authenticate(callbackHandler);
            }
    		else {
    		    mConnection.authenticate(mEnvironmentMgr.getProfileUser(mURL),
    		            mEnvironmentMgr.getProfileCredentials());
    		}
            mConnection.closeAuthorizedContext();

            // store the new LdapConnectionHandler in the PolicySource
            mPolicySource.setConnectionHandler(mURL, mConnection);
        }
        mDataStore = mConnection.getDataStore();
    }
    
    /**ClosesOpens the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     * @throws 			CloseConnectionException if connection error occurs
     */
    public void close() throws SPIException {
        if (mPolicySource.getConnectionHandler(mURL) != null) {
            mPolicySource.setConnectionHandler(mURL, null);
        }
        mConnection.disconnect();
    }
    
    protected boolean isGSSAPIAuthentication() {
        return mEnvironmentMgr.getProfileAuthType()
        		.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI);
    }

    /**
     * Returns the default <code>ProfileRepository</code>
     *
     * @return		the default <code>ProfileRepository</code>
     * @throws		<code>SPIException</code> if error occurs 
     */
   public ProfileRepository getDefaultProfileRepository() 
   			throws SPIException {
        Entity entity = mPolicySource.getRoot();
        if (entity != null) {
            return getProfileRepository(entity.getId());  
        }
        return null;
   }

   /**
    * Returns the requested <code>ProfileRepository</code>
    *
    * @param	id	the id for the required <code>ProfileRepository</code>
    * @return		the <code>ProfileRepository</code> object
    * @throws		<code>SPIException</code> if error occurs 
    */
    public ProfileRepository getProfileRepository(String id)
            throws SPIException {
        return (ProfileRepository)new LdapProfileRepository(
                		id, mPolicySource);     
    }

    /**
     * Returns the requested <code>Profile</code>.
     *
     * @param id   the id for the required <code>Profile</code>
     * @return      the <code>Profile</code> object 
     * 				or null if does not exist
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Profile getProfile(String id) throws SPIException {
        return mDataStore.getProfile(id);
    }
    
    /**
     * Returns all the Profiles.
     *
     * @return      an Iterator over all the Profile objects 
     * @throws      SPIException if error occurs 
     */
    public Iterator getAllProfiles() throws SPIException {
        return mDataStore.getAllProfiles(
                mPolicySource, (LdapEntity)mPolicySource.getRoot(), Applicability.getApplicability(mPolicySource.getName()));
    }
    
    /**
     * Returns all the Profiles stored in the startingEntity
     * and all of its sub-entities.
     *
     * @return      an Iterator over all the Profile objects 
     * @throws      SPIException if error occurs 
     */
    public Iterator getAllProfiles(Entity startingEntity) throws SPIException {
        return mDataStore.getAllProfiles(
                mPolicySource, (LdapEntity)startingEntity, Applicability.getApplicability(mPolicySource.getName()));
    }
    
    public LdapDataStore getDataStore() {
        return mDataStore;
    }
}
