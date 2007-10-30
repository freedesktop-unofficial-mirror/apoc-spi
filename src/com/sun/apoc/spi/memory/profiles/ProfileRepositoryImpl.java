/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either
 * the GNU General Public License Version 2 only (\"GPL\") or
 * the Common Development and Distribution License(\"CDDL\")
 * (collectively, the \"License\"). You may not use this file
 * except in compliance with the License. You can obtain a copy
 * of the License at www.sun.com/CDDL or at COPYRIGHT. See the
 * License for the specific language governing permissions and
 * limitations under the License. When distributing the software,
 * include this License Header Notice in each file and include
 * the License file at /legal/license.txt. If applicable, add the
 * following below the License Header, with the fields enclosed
 * by brackets [] replaced by your own identifying information:
 * \"Portions Copyrighted [year] [name of copyright owner]\"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your
 * decision by adding \"[Contributor] elects to include this
 * software in this distribution under the [CDDL or GPL
 * Version 2] license.\" If you don't indicate a single choice
 * of license, a recipient has the option to distribute your
 * version of this file under either the CDDL, the GPL Version
 * 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the
 * option applies only if the new code is made subject to such
 * option by the copyright holder.
 */

package com.sun.apoc.spi.memory.profiles;

import java.util.Iterator;
import java.util.Hashtable;
import java.io.OutputStream;
import java.io.InputStream;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;
import com.sun.apoc.spi.entities.Entity;

public class ProfileRepositoryImpl implements ProfileRepository {
    
    private String m_id = "";
    private Hashtable m_profiles = null; 
    private PolicySource m_mgr = null;
    
    public ProfileRepositoryImpl(PolicySource mgr, String id) {
        m_id = id;
        m_mgr = mgr;
        m_profiles = new Hashtable();
    }
 
    public Profile createProfile(String displayName, Applicability applicability)
            throws SPIException {
        Profile newProfile = new ProfileImpl(this, displayName);
        newProfile.setApplicability(applicability);
        m_profiles.put(newProfile.getId(), newProfile);
        return newProfile;   
    }
 
    public void destroyProfile(Profile profile) {
        m_profiles.remove(profile.getId());
    }

    public Profile getProfile(String id) {
        return (Profile) m_profiles.get(id);
    }
    
    public Profile findProfile(String aDisplayName) {
        return null;
    }

    public Iterator getProfiles(Applicability applicability) {
        return m_profiles.values().iterator();
    }

    public boolean isReadOnly() {
        return false;   
    }

    public String getId() {
        return m_id;
    }
    
    public PolicySource getPolicySource() {
        return m_mgr;
    }
    
    public Entity getEntity() {
        return null;
    }
    
    public void exportProfile(Profile profile, OutputStream out) {
    }
    
    public void importProfile(String name, Applicability app, InputStream out) {
    }
}
