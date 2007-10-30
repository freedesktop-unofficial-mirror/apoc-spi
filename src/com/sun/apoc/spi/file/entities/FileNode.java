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

package com.sun.apoc.spi.file.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Leaf;
import com.sun.apoc.spi.entities.Node;

abstract public class FileNode extends FileEntity implements Node
{
    Map mLeaves;
    Map mNodes;
    Map mRoles;

    /**
     * @param aDisplayName
     * @param aId
     * @param aParent
     * @param aPolicyManager
     */
    public FileNode(String aDisplayName, String aId, Entity aParent, PolicySource aPolicySource)
    {
        super(aDisplayName, aId, aParent, aPolicySource);
        mLeaves = Collections.synchronizedMap( new HashMap() );
        mNodes = Collections.synchronizedMap( new HashMap() );
        mRoles = Collections.synchronizedMap( new HashMap() );
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#findEntities(java.lang.String, boolean)
     */
    abstract public Iterator findEntities(String aFilter, boolean aIsRecursive)
            throws SPIException;  

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#getRoles()
     */
    public Iterator getRoles() throws SPIException
    {
        return mRoles.values().iterator();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#hasRoles()
     */
    public boolean hasRoles() throws SPIException
    {
        return !mRoles.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#getChildren()
     */
    public Iterator getChildren() throws SPIException
    {
        LinkedList children;
        Iterator it;
        
        children = new LinkedList();
        
        it = mLeaves.values().iterator();
        while ( it.hasNext() )
        {
            children.add( (Entity)it.next());
        }
        
        it = mNodes.values().iterator();
        while ( it.hasNext() )
        {
            children.add( (Node)it.next());
        }
        
        it = mRoles.values().iterator();
        while ( it.hasNext() )
        {
            children.add( (Entity)it.next() );    
        }
        

        return children.iterator();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#hasChildren()
     */
    public boolean hasChildren() throws SPIException
    {
        return ( hasLeaves() || hasNodes() || hasRoles() );
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#getLeaves()
     */
    public Iterator getLeaves() throws SPIException
    {
        return mLeaves.values().iterator();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#hasLeaves()
     */
    public boolean hasLeaves() throws SPIException
    {
        return !mLeaves.isEmpty();        
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#getNodes()
     */
    public Iterator getNodes() throws SPIException
    {
        return mNodes.values().iterator();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#hasNodes()
     */
    public boolean hasNodes() throws SPIException
    {     
        return !mNodes.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#getEntity(java.lang.String)
     */
    public Entity getEntity(String aId) throws SPIException
    {
        Entity entity;
        
        entity = depthSearch(this, aId);
        return entity;
    }
    
    private Entity depthSearch(FileNode aNode, String aId ) {
        FileNode node;
        Entity entity;
        Iterator it;
        
        entity = null;
        if ( aNode.getId().equals(aId)) {
            return aNode;
        }
        it = aNode.mLeaves.values().iterator();
        entity = searchForEntity( it, aId );
        it = aNode.mNodes.values().iterator();
        while ((it.hasNext()) && (entity == null)) {
            node = (FileNode)it.next();
            entity = depthSearch(node, aId);
        }
       
        return entity;
    }
    
    protected void addChildNode( Node aNode )
    {
        mNodes.put( aNode.getId(), aNode );
    }
    
    protected void addChildLeaf( Leaf aLeaf)
    {
        mLeaves.put( aLeaf.getId(), aLeaf );
        
    }
    private Entity searchForEntity( Iterator aIt, String aId )
    {
        Entity entity;
        
        while (aIt.hasNext())
        {
            entity = (Entity) aIt.next();
            if ( aId.indexOf(entity.getId()) != -1 )
            {
                return entity;
            }
        }

        return null;
    }
    
    protected String getIdFromFilter( String aFilter) {
        // aFilter:
        // (givenname="*userid*")
        // (cn="*userId*")
        // (sn="*userId*")
        // *userId*
        String uid;
        
        if ( aFilter == null ) {
            return null;
        }
        
        uid = aFilter;
        if ( aFilter.startsWith("(") && aFilter.endsWith(")")) {
            // remove ( & )
            uid = aFilter.substring(1, aFilter.length()-1);
            String[] tokens = uid.split("=");
            if ( tokens.length == 2 ) {
                uid=tokens[tokens.length-1];
            }
        }
        
        if ( uid.startsWith("*") && uid.endsWith("*") && uid.length() > 2) {
            // remove * at the begining and at the end
            uid = uid.substring(1,uid.length()-1);
        }
        
        if (uid.equals("*")) {
            uid ="";
        }
        
        return uid;
    }
}
