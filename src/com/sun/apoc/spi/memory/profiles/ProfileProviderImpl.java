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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileProvider;
import com.sun.apoc.spi.profiles.ProfileRepository;


public class ProfileProviderImpl implements ProfileProvider {
    
    private static final String DEFAULT_REPOSITORY_ID = "default";
    
    private static ProfileRepository m_defaultRepository = null;
    private Hashtable m_repositories = null;
    private PolicySource m_mgr = null;
    
    public ProfileProviderImpl(PolicySource mgr) {
        m_mgr = mgr;
    }
    
    public void open() {
        m_defaultRepository = new ProfileRepositoryImpl(m_mgr, DEFAULT_REPOSITORY_ID);
        m_repositories = new Hashtable();
        m_repositories.put(DEFAULT_REPOSITORY_ID, m_defaultRepository);
        initializeSampleData();
    }

    public void close() {
        m_mgr = null;
        m_repositories = null;
        m_defaultRepository = null;
    }

    public ProfileRepository getDefaultProfileRepository() {
        return m_defaultRepository;
    }
    
    public ProfileRepository getProfileRepository(String id) {
        if (m_repositories.containsKey(id)) {
            return (ProfileRepository) m_repositories.get(id);
        } else {
            ProfileRepository repository = new ProfileRepositoryImpl(m_mgr, id);
            m_repositories.put(id, repository);
            return repository;
        }
    }
    
    public Iterator getProfileRepositories() {
        return m_repositories.values().iterator();
    }
    
    public Profile getProfile(String id) throws SPIException {
        int separatorPos = id.indexOf(ProfileImpl.ID_SEPARATOR);
        if (separatorPos > 0) {
            String repositoryId = id.substring(separatorPos);
            ProfileRepository repository = getProfileRepository(repositoryId);
            return repository.getProfile(id);
        } else {
            return null;
        }
    }
    
    public Iterator getAllProfiles() throws SPIException {
        ArrayList profileList = new ArrayList();

        //first iterate over all repositories
        Iterator it = getProfileRepositories();
        while(it.hasNext()) {
            ProfileRepository rep = (ProfileRepository) it.next();
            
            // then iterate over all profiles stored in each repository
            Iterator profileIt = rep.getProfiles(Applicability.ALL);
            while(profileIt.hasNext()) {
                Profile profile = (Profile) profileIt.next();
                profileList.add(profile);
            }
        }
        return profileList.iterator();
    }
    
    // not implemented
    public Iterator getAllProfiles(Entity StartingEntity) throws SPIException {
        throw new UnsupportedOperationException();
    }
    
    private void initializeSampleData() {
        try {
            Profile profile1 = m_defaultRepository.createProfile("StarOffice Novice", Applicability.ALL);
            profile1.storePolicy(new Policy("com.sun.apoc.test", profile1.getId(), "Just some dummy data"));
            profile1.storePolicy(new Policy("com.sun.apoc.test2", profile1.getId(), "and some more data"));
            
            Profile profile2 = m_defaultRepository.createProfile("APOC Agent Settings", Applicability.ALL);
            profile2.storePolicy(new Policy("com.sun.apoc.apocd", profile2.getId(), "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                    + "<oor:component-data xmlns:oor=\"http://openoffice.org/2001/registry\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" oor:name=\"apocd\" oor:package=\"com.sun.apoc\">"
                    + "<prop oor:name=\"DaemonChangeDetectionInterval\" oor:finalized=\"true\" oor:type=\"xs:int\">"
                    + "<value>10</value></prop></oor:component-data>"));
            
            
            Profile profile3 = m_defaultRepository.createProfile("Desktop Appearance", Applicability.ALL);
            profile3.storePolicy(new Policy("org.gnome.desktop.gnome", profile2.getId(), "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                    + "<oor:component-data xmlns:oor=\"http://openoffice.org/2001/registry\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" oor:name=\"gnome\" oor:package=\"org.gnome.desktop\">"
                    + "<node oor:name=\"background\">"
                    + "<prop oor:name=\"primary_color\" oor:type=\"xs:string\">"
                    + "<value>#c06600</value>"
                    + "</prop></node></oor:component-data>"));
        } catch (SPIException ex) {
            ex.printStackTrace();
        }
    }
}
