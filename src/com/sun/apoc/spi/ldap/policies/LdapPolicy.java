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

package com.sun.apoc.spi.ldap.policies;

import java.util.ArrayList;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.ldap.util.Timestamp;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileImpl;

/**
  * Class for a policy.
  *
  */
public  class LdapPolicy extends Policy {
    protected String mLocation;
    protected ProfileImpl mProfile;
    protected Entity mAuthor;


    /**
      * Constructor for class.
      * 
      * @param aId          identifier
      * @param aProfile     profile object
      * @param aData        data stored
      */
    public LdapPolicy(String aId, String aData, String aLocation, 
            		  ProfileImpl aProfile) {
        super(aId, aProfile.getId(), aData);
        mProfile = aProfile;
        mLocation = aLocation;
    }

    /**
     * Constructor for class.
     * 
     * @param aId          identifier
     * @param aProfile     profile object
     * @param aData        data stored
     */
    public LdapPolicy(String aId, String aData, long aLastModified,
            		  String aLocation, ProfileImpl aProfile) {
        super(aId, aProfile.getId(), aData, aLastModified);
        mProfile = aProfile;
        mLocation = aLocation;
    }

    /**
      * Returns the location for this policy.
      *
      * @return    location for the policy
      */
    public String getLocation() { return mLocation; }

    /**
      * Sets the location for this policy.
      *
      * @param aLocation    location for the policy
      */
    public void setLocation(String aLocation) { 
        mLocation = aLocation; 
    }
    
    /**
      * Sets the data XMLBlob for the policy object.
      *
      * @param     policy data 
      */
    public void setData(String aData) {
        mData = aData;
    }
    
    /** 
     * Returns the profile to which this policy belongs.
     *
     * @return    profile
     */
    public Profile getProfile() { return mProfile; }

    /** 
     * Sets the author for the last modification of this policy.
     *
     * @param aAuthor   entity that last modified this policy 
     */
    public void setAuthor(Entity aAuthor) { 
        mAuthor = aAuthor;
    }

    /**
      * Returns the author of the most recent modification 
      * of this policy. 
      *
      * @return    author of the last modification 
      * @throws    <code>SPIException</code> if error occurs 
      */
    public Entity getAuthor()  throws SPIException {
        if (mAuthor == null) {
            LdapEntity entity = 
            (LdapEntity)getProfile().getProfileRepository().getEntity();
            ArrayList retValues = entity.getDataStore()
            							.getModificationDetails(this);
            setLastModified((String)retValues.get(0));
            if (retValues.size() > 1) {
                mAuthor = (Entity)retValues.get(1);
            }
        }
        return mAuthor;
    }

    /**
      * Sets the time in milliseconds for the last
      * modification of the policy.
      *
      * @param aLastModified the String value for the modification
      *                      time as returned by the database
      */
    public void setLastModified(String aLastModified) {
        mLastModified = Timestamp.getMillis(aLastModified);
    }

    /**
      * Sets the id for the policy. 
      *
      * @param aId    the id
      */
    public void setId(String aId) {
        mId = aId;
    }
}
