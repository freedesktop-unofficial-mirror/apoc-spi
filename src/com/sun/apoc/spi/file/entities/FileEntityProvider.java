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

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.OpenConnectionException;
import java.util.Iterator;
import java.util.LinkedList;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.environment.InvalidParameterException;
import com.sun.apoc.spi.environment.MissingParameterException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


abstract public class FileEntityProvider implements EntityTreeProvider {
    protected PolicySource mPolicySource;
    protected boolean mDataIsLoaded;
    protected Node mRootNode;
    protected URL mLocation;
    
    public FileEntityProvider( PolicySource aPolicySource, String aUrl, String aContainerFile ) throws SPIException {
        if ( (aUrl == null) || (aUrl.length() == 0)) {
            throw new MissingParameterException("domain "+EnvironmentConstants.URL_KEY);
        }
        StringBuffer location;
        location = new StringBuffer(aUrl);
        if ( location.charAt(location.length() - 1) != '/' ) {
            location.append('/');
        }
        location.append(aContainerFile);
        try {
            mLocation = new URL( location.toString() );
        } catch ( MalformedURLException e ) {
            throw new InvalidParameterException(
                    "domain "+EnvironmentConstants.URL_KEY,
                    location.toString());
        }
        mPolicySource = aPolicySource;
        mRootNode = null;
        mDataIsLoaded = false;
        
    }    
    
    public void open() throws SPIException
    {
        File file;
        
        if ( mLocation.getProtocol().equals(EnvironmentConstants.FILE_URL_PROTOCOL) ) {
            try {
                file = new File( new URI( mLocation.toString() ) );                
                if ( !file.exists() ) {
                    throw new IllegalReadException(IllegalReadException.FILE_NAME_READ_KEY, file.getAbsolutePath());
                }                
            } catch ( URISyntaxException e ){
                throw new InvalidParameterException(
                        "organization "+EnvironmentConstants.URL_KEY,
                        mLocation.toString());
            }    
            
        } else {
            try {
                mLocation.openStream();
            } catch (IOException e) {
                // could not connect to the repository
                throw new OpenConnectionException(
                        mLocation.toString(), e);
            }
        }
    }
    
    public void close() throws SPIException
    {
        // Nothing to do
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.EntityProvider#getRootEntity()
     */
    public Node getRootEntity() throws SPIException {
        if ( !mDataIsLoaded ) {
            loadData();
            mDataIsLoaded = true;
        }
        return mRootNode;
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.EntityProvider#getEntity(java.lang.String)
     */
    public Entity getEntity(String aId) throws SPIException {
        if ( !mDataIsLoaded ) {
            loadData();
        }
        return internalGetEntity( aId );
    }
    
    protected  Entity internalGetEntity(String aId) throws SPIException {
        LinkedList toVisit;
        Entity entity;
        Iterator it;
        
        entity = mRootNode;
        
        if ( entity.getId().equals( aId ) ) {
            return entity;
        } else {
            toVisit = new LinkedList();
            addChilds( toVisit, (Node)entity );
            
            while ( !toVisit.isEmpty() ) {
                entity = (Entity) toVisit.removeFirst();
                if ( entity.getId().equals( aId ) ) {
                    return entity;
                }
                if ( entity instanceof Node ) {
                    addChilds( toVisit, (Node)entity );
                }
            }
        }
        // Not found
        return null;
    }
    
    private void addChilds( LinkedList aList, Node aNode ) throws SPIException {
        Iterator it;
        
        it = aNode.getChildren();
        while ( it.hasNext()) {
            aList.add( (Entity)it.next() );
        }
    }
    
    abstract protected void loadData() throws SPIException;
}
