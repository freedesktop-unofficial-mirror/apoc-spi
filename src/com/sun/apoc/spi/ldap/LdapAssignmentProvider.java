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
package com.sun.apoc.spi.ldap;

import java.util.Iterator;

import com.sun.apoc.spi.AbstractAssignmentProvider;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.environment.LdapEnvironmentMgr;
import com.sun.apoc.spi.ldap.profiles.LdapProfile;
import com.sun.apoc.spi.ldap.profiles.LdapProfileRepository;
import com.sun.apoc.spi.profiles.InvalidProfileException;
import com.sun.apoc.spi.profiles.Profile;
/**
 *
 */
public class LdapAssignmentProvider extends AbstractAssignmentProvider {

    protected LdapConnectionHandler mConnection;
    protected LdapDataStore mDataStore;
    protected LdapEnvironmentMgr mEnvironmentMgr;
    protected String mURL;

    public LdapAssignmentProvider(PolicySource aPolicySource, String url)
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
    		        mEnvironmentMgr.getAssignmentTimeout(),
    		        mEnvironmentMgr.getAssignmentAuthUser(), 
	        		mEnvironmentMgr.getAssignmentAuthPassword(),
	        		mEnvironmentMgr);
            if (isGSSAPIAuthentication()){
                Object callbackHandler = 
                    mEnvironmentMgr.getAssignmentCallbackHandler();
                mConnection.authenticate(callbackHandler);
            }
    		else {
    		    mConnection.authenticate(mEnvironmentMgr.getAssignmentUser(mURL),
    		            mEnvironmentMgr.getAssignmentCredentials());
    		}
            mConnection.closeAuthorizedContext();

            // store the new LdapConnectionHandler in the PolicyManager
            mPolicySource.setConnectionHandler(mURL, mConnection);
        }
        mDataStore = mConnection.getDataStore();
    }
    
    /**
     * Closes the connection to the datasource
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
        return mEnvironmentMgr.getAssignmentAuthType()
        		.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI);
    }

    /**
     * Assigns a profile to an entity.
     *
	 * @param entity	entity to assign the profile to
	 * @param profile   profile to assign to the entity
     * @throws          <code>SPIException</code> if error occurs
     */
    protected void assignProfileToEntity(Entity entity, Profile profile)
            throws SPIException {
        if (!(profile instanceof LdapProfile)) {
            throw new InvalidProfileException();
        }
        // section to ensure migration of APOC 1 Local Profiles
        LdapProfile ldapProfile = (LdapProfile)profile;
        if (ldapProfile.isLocal()) {
            LdapProfileRepository rep = (LdapProfileRepository)
            			ldapProfile.getProfileRepository();
            rep.migrateProfile(ldapProfile);
        }
        mDataStore.assignProfile(profile, entity);
    }

	/**
	 * Unassigns the specified profile from the entity.
	 *
	 * @param entity	entity to unassign the profile to
	 * @param profile   profile to unassign to the entity
	 * @throws          <code>SPIException</code> if error occurs
	 */
    public void unassignProfile(Entity entity, Profile profile)
            throws SPIException {
        if (!(profile instanceof LdapProfile)) {
            throw new InvalidProfileException();
        }
        // section to ensure migration of APOC 1 Local Profiles
        LdapProfile ldapProfile = (LdapProfile)profile;
        if (ldapProfile.isLocal()) {
            LdapProfileRepository rep = (LdapProfileRepository)
            			ldapProfile.getProfileRepository();
            rep.migrateProfile(ldapProfile);
        }
        mDataStore.unassignProfile(profile, entity);
    }

	/**
	 * returns the profiles assigned to the entity.
	 *
	 * @param entity	entity to get assigned profiles from
	 * @return			Iterator on the assigned Profiles
	 * @throws          <code>SPIException</code> if error occurs
	 */
    protected Iterator getProfilesAssignedToEntity(Entity entity)
    		throws SPIException {
        return mDataStore.getAssignedProfiles(entity);
    }

	/**
	 * returns the entities the profile is assigned to.
	 *
	 * @param profile	profile to get assigned Entities from
	 * @return			Iterator on the assigned Entities
	 * @throws          <code>SPIException</code> if error occurs
	 */
    public Iterator getEntitiesAssignedToProfile(Profile profile) 
    		throws SPIException {
        return mDataStore.getAssignedEntities(profile);
    }
}
