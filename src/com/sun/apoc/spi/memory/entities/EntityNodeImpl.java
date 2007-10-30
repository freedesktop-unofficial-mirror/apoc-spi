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
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Collections;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.entities.Leaf;

public class EntityNodeImpl extends EntityImpl implements Node {
    
    private Hashtable m_children = null;
    private HashSet m_nodes = null;
    private HashSet m_leaves = null;
    
    public EntityNodeImpl(PolicySource mgr, Entity parent, String id, String displayName) {
        super(mgr, parent, id, displayName);
        m_children = new Hashtable();
        m_nodes = new HashSet();
        m_leaves = new HashSet();
    }

    public Iterator findEntities(String aFilter, boolean aIsRecursive) {
        return null; // not implemented yet
    }

    public Iterator getChildren() {
        return m_children.values().iterator();
    }

    public boolean hasChildren() {
        return !m_children.isEmpty();
    }
    
    public Iterator getNodes() {
        return m_nodes.iterator();
    }

    public boolean hasNodes() {
        return !m_nodes.isEmpty();
    }
    
    public Iterator getLeaves() {
        return m_leaves.iterator();
    }

    public boolean hasLeaves() {
        return !m_leaves.isEmpty();
    }
    
    public Iterator getRoles() {
        return Collections.EMPTY_LIST.iterator(); 
    }
    
    public boolean hasRoles() {
        return false; 
    }

    public Entity getEntity(String id) {
        return (Entity) m_children.get(id);
    }
    
    public void addChild(Entity entity) {
        m_children.put(entity.getId(), entity);
        if (entity instanceof Leaf) {
            m_leaves.add(entity);
        } else {
            m_nodes.add(entity);
        }
    }
}