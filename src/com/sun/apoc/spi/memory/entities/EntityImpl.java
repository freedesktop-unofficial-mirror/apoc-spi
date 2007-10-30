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

package com.sun.apoc.spi.memory.entities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale ;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;


public class EntityImpl implements Entity {
    
    private String m_displayName = "";
    private String m_id = "";
    private Entity m_parent = null;
    private PolicySource m_mgr = null;
    
    public EntityImpl(PolicySource mgr, Entity parent, String id, String displayName) {
        m_mgr = mgr;
        m_parent = parent;
        m_displayName = displayName;
        m_id = id;
    }
    
    public String getDisplayName(Locale aLocale) {
        return m_displayName;
    }

    public Iterator getAncestorNames(Locale aLocale) {
        LinkedList ancestorNames = new LinkedList();
        Entity parent = this.getParent();
        while (parent != null) {
            ancestorNames.add(0, parent.getDisplayName(aLocale));
            parent = parent.getParent();
        }
        return ancestorNames.iterator();
    }

    public String getId() {
        return m_id;
    }

    public Entity getParent() {
        return m_parent;
    }

    public Iterator getAssignedProfiles() throws SPIException {
        return m_mgr.getAssignmentProvider().getAssignedProfiles(this);
    }

    public void assignProfile(Profile profile) throws SPIException {
        m_mgr.getAssignmentProvider().assignProfile(this, profile);
    }

    public void unassignProfile(Profile profile) throws SPIException {
        m_mgr.getAssignmentProvider().unassignProfile(this, profile);
    }
    
    public Iterator getLayeredProfiles() throws SPIException {
        // not implemented yet
        return null;
    }

    public ProfileRepository getProfileRepository() throws SPIException {
        if (getParent() != null) {
            return m_mgr.getProfileProvider().getProfileRepository(getId());
        } else {
            return m_mgr.getProfileProvider().getDefaultProfileRepository();
        }
    }
    
    public String getPolicySourceName() {
        return m_mgr.getName();
    }
}
