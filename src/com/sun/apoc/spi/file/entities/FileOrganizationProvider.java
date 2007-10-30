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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.entities.OrganizationTreeProvider;

public class FileOrganizationProvider extends FileEntityProvider implements
OrganizationTreeProvider
{
    private static final String DEFAULT_ORG_CONTAINER = "entities.txt";    
    
    public FileOrganizationProvider( PolicySource aPolicySource, String aUrl ) throws SPIException
    {
        super(aPolicySource,aUrl, DEFAULT_ORG_CONTAINER);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.sun.apoc.spi.entities.OrganizationTreeProvider#getRootOrganization()
     */
    public Organization getRootOrganization() throws SPIException
    {   
        if (!mDataIsLoaded) {
            loadData();            
        }
        return (Organization) mRootNode;
    }
    
    protected void loadData() throws SPIException
    {
        String line;
        File file;
        
        if (mRootNode != null)
        {
            return;
        }
        
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( mLocation.openStream()));
            while ((line = reader.readLine()) != null)
            {
                if (( line.startsWith("u:")) && !line.endsWith("/") ) {
                    // User
                    createEntities(true, line);
                } else if ( line.startsWith("o:") ) {
                    createEntities(false, line);
                }
            }
            reader.close();
            if ( mRootNode == null ) { //empty file readed
                // Create an empty root node
                mRootNode = new FileOrganization("", "fakeOrgRoot", null, mPolicySource );
            }
        }
        catch (FileNotFoundException e)
        {        
            // Shouldn't happend. the existence of the file is checked in the
            // open method
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        catch (IOException e)
        {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        
        mDataIsLoaded = true;        
    }
    
    private void createEntities(boolean aIsUser, String aLine) throws SPIException
    {
        String[] components;
        FileUser user;
        FileNode parent;
        Node node;
        Iterator it;
        StringBuffer id;
        int i;
        
        if (aLine.indexOf('/') != -1 ) {
            components = aLine.split("/");
            /* 
             * Eliminate the possibility of having the Domain root and Organization
             * root with the same Id 
             */
            id = new StringBuffer(components[1]).append(FileEntity.ORGANIZATION_TAG);
            
            if (mRootNode == null) {                
                mRootNode = new FileOrganization(components[1], id.toString(),
                        null, mPolicySource);
            } else if (!mRootNode.getId().contentEquals(id)) {
                // Differnet root. ignore line
                return;
            }
            
            if ( components.length > 2 ) {
                parent = (FileNode) mRootNode;
                for (i = 2; i < components.length - 1; i++) // process nodes
                {
                    it = ((Node) parent).getChildren();
                    id.append(FileEntity.ENTITY_SEPARATOR);
                    id.append(components[i]);
                    if (!contains(it, id.toString()))
                    {
                        node = new FileOrganization(components[i], id.toString(), parent,
                                mPolicySource);
                        parent.addChildNode(node);
                        parent = (FileNode) node;
                    }
                    else
                    {
                        parent = (FileNode) internalGetEntity(id.toString());
                    }
                }
                id.append(FileEntity.ENTITY_SEPARATOR);
                id.append(components[i]);
                if ( aIsUser ) {
                    // leaf -> create user
                    user = new FileUser(components[i], id.toString(), parent, mPolicySource);
                    parent.addChildLeaf(user);
                } else {
                    node = new FileOrganization( components[i], id.toString(), parent, mPolicySource);
                    parent.addChildNode( node );
                }
            }
        }
    }
    
    private boolean contains(Iterator it, String aId)
    {
        Entity entity;
        while (it.hasNext())
        {
            entity = (Entity) it.next();
            if (entity.getId().equals(aId))
            {
                return true;
            }
        }
        
        return false;
    }
}
