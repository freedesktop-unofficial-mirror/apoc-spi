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

package com.sun.apoc.spi.memory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.sun.apoc.spi.AssignmentProvider;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.profiles.Profile;

public class AssignmentProviderImpl implements AssignmentProvider {
    
    private Hashtable m_assignments = null;
    private PolicySource m_mgr = null;
    
    public AssignmentProviderImpl(PolicySource mgr) {
        m_assignments = new Hashtable();
        m_mgr = mgr;
    }
    
    public void open() {
        
    }
    
    public void close() {
        m_mgr = null;
        m_assignments.clear();
    }
    
    public void assignProfile(Entity entity, Profile profile) {
        HashSet assignedProfiles = (HashSet) m_assignments.get(entity);
        if (assignedProfiles == null) {
            assignedProfiles = new HashSet();
            m_assignments.put(entity, assignedProfiles);
        }
        assignedProfiles.add(profile);
    }
    
    public void unassignProfile(Entity entity, Profile profile) {
        HashSet assignedProfiles = (HashSet) m_assignments.get(entity);
        if (assignedProfiles != null) {
            assignedProfiles.remove(profile);
        }
    }
    
    public Iterator getAssignedProfiles(Entity entity) {
        HashSet assignedProfiles = (HashSet) m_assignments.get(entity);
        if (assignedProfiles != null) {
            return assignedProfiles.iterator();
        }
        else {
            return Collections.EMPTY_SET.iterator();
        }
    }
    
    public Iterator getAssignedEntities(Profile profile) {
        HashSet assignedEntities = new HashSet();
        Iterator it = m_assignments.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Entity entity = (Entity) entry.getKey();
            HashSet profiles = (HashSet) entry.getValue();
            if (profiles.contains(profile)) {
                assignedEntities.add(entity);
            }
        }
        return assignedEntities.iterator();
    }
}
