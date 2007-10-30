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
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.DomainTreeProvider;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;

public class FileDomainProvider extends FileEntityProvider implements DomainTreeProvider {
    private static final String DEFAULT_DOMAIN_CONTAINER = "entities.txt";    
    
    
    public FileDomainProvider( PolicySource aPolicySource, String aUrl ) throws SPIException {
        super( aPolicySource, aUrl, DEFAULT_DOMAIN_CONTAINER );        
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.DomainTreeProvider#getRootDomain()
     */
    public Domain getRootDomain() throws SPIException {
        if ( !mDataIsLoaded ) {
            loadData();
        }
        return (Domain) mRootNode ;
    }
    
    protected void loadData() throws SPIException {
        String line;
        File file;
        
        if ( mDataIsLoaded ) { return ; }
        
        try {
            BufferedReader reader = new BufferedReader( new InputStreamReader( mLocation.openStream()));
            while ( ( line = reader.readLine() ) != null ) {
                if ( line.startsWith("h:") ) {
                    // Host
                    createEntities( true, line );
                } else if ( line.startsWith("d:") ) {
                    // Domain
                    createEntities( false, line );
                }
            }
            reader.close();
            if ( mRootNode == null ) { //empty file readed
                // Create an empty root node
                mRootNode = new FileDomain("", "fakeDomainRoot", null, mPolicySource );
            }
        } catch (FileNotFoundException e) {
            // Shouldn't happend
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        } catch (IOException e) {
            throw new IllegalReadException(
                    IllegalReadException.FILE_READ_KEY, e);
        }
        
        mDataIsLoaded = true;
    }
    
    private void createEntities(boolean aIsHost, String aLine)
    throws SPIException {
        String[] components;
        FileHost host;
        FileNode parent;
        Node node;
        Iterator it;
        StringBuffer id;
        int i;
        
        if (aLine.indexOf('/') != -1 ) {
            components = aLine.split("/");
            /*
             * Eliminate the possibility of having the Domain root and Organization
             * root with the sameid
             */
            id = new StringBuffer(components[1]).append(FileEntity.DOMAIN_TAG);
            
            if (mRootNode == null ) {
                mRootNode = new FileDomain(components[1], id.toString(), null,
                        mPolicySource);
            } else if (!mRootNode.getId().contentEquals(id)) {
                // Different root. ignore line
                return;
            }
            
            if ( components.length > 2 ) {
                parent = (FileNode) mRootNode;
                for (i = 2; i < components.length - 1; i++) // process nodes
                {
                    it = ((Node) parent).getChildren();
                    id.append(FileEntity.ENTITY_SEPARATOR);
                    id.append(components[i]);
                    if (!contains(it, id.toString())) {
                        node = new FileDomain( components[i], id.toString(),
                                parent, mPolicySource);
                        parent.addChildNode(node);
                        parent = (FileNode) node;
                    } else {
                        parent = (FileNode) internalGetEntity(id.toString());
                    }
                }
                
                id.append(FileEntity.ENTITY_SEPARATOR);
                id.append(components[i]);
                if ( aIsHost ) {
                    // leaf -> create host
                    host = new FileHost(components[i],id.toString(), parent,
                            mPolicySource);
                    parent.addChildLeaf( host );
                } else {
                    node = new FileDomain( components[i], id.toString(), parent,
                            mPolicySource);
                    parent.addChildNode( node );
                }
            }
        }
    }
    
    private boolean contains( Iterator it, String aId ) {
        Entity entity;
        while ( it.hasNext() ) {
            entity = (Entity) it.next();
            if ( entity.getId().equals( aId ) ) {
                return true;
            }
        }
        
        return false;
    }
}
