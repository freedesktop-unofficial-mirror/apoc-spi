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
import java.util.ArrayList;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;
import com.sun.apoc.spi.profiles.Applicability;


public class ProfileImpl implements Profile {
    
    public static final String ID_SEPARATOR = "/";
    
    public static int m_idCounter = 0;
    
    private int       m_priority = 0;
    private String    m_id = "";
    private String    m_displayName = "";
    private Applicability    m_applicability = null;
    private String    m_comment = "";
    private String    m_author = "";
    private Hashtable m_policies = null;
    private Hashtable m_assignedEntities = null;
    
    private ProfileRepositoryImpl m_parentRepository = null;
    
    public ProfileImpl(ProfileRepositoryImpl parent, String displayName) {
        m_parentRepository = parent;
        m_policies = new Hashtable();
        m_assignedEntities = new Hashtable();
        setDisplayName(displayName);
        m_id = parent.getId() + ID_SEPARATOR + m_idCounter;
        m_idCounter++;
        m_author = "test";
    }

    public String getId() {
        return m_id;
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public int getPriority() {
        return m_priority;
    }

    public long lastModified() {
        return 0;
    }

    public void setPriority(int newPriority) {
        m_priority = newPriority;
    }

    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
 
    public Policy createPolicy(String id, String data) {
        Policy policy = new Policy(id, this.getId(), data);
        m_policies.put(id, policy);
        return policy; 
    }

    public void storePolicy(Policy aPolicy) {
        m_policies.put(aPolicy.getId(), aPolicy);
    }

    public void destroyPolicy(Policy policy) {
        m_policies.remove(policy.getId());
    }
 
    public Iterator getPolicies() {
        return m_policies.values().iterator();
    }
    
    public Iterator getPolicies(ArrayList list) {
        return m_policies.values().iterator();
    }
    
    public Iterator getPolicies(Iterator policyIds) throws SPIException {
        ArrayList policyList = new ArrayList();
        while(policyIds.hasNext()) {
            Policy policy = getPolicy((String) policyIds.next());
            policyList.add(policy);
        }
        return policyList.iterator();
    }

    public Iterator getPolicyInfos(Iterator policyIds) throws SPIException {
        // this shortcut is only allowed in the memory-based implementation!
        return getPolicies(policyIds);
    }
    
    public boolean hasPolicies() {
        return !m_policies.isEmpty();
    }

    public Policy getPolicy(String id) {
        return (Policy) m_policies.get(id);
    }

    public Iterator getAssignedEntities() throws SPIException {
        return m_parentRepository.getPolicySource().getAssignmentProvider().getAssignedEntities(this);
    }
    
    public boolean hasAssignedEntities() {
        return false;
    }

    public Applicability getApplicability() {
        return m_applicability;
    }

    public void setApplicability(Applicability applicability) {
        m_applicability = applicability;
    }

    public String getComment() {
        return m_comment;
    }

    public void setComment(String comment) {
        m_comment = comment;
    }

    public String getAuthor() {
        return m_author;
    } 

    public ProfileRepositoryImpl getParentProfileRepository() {
        return m_parentRepository;
    }
    
    public long getLastModified() {
        return 0;
    }
    
    public ProfileRepository getProfileRepository() {
        return m_parentRepository;
    }
}
