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
package com.sun.apoc.spi.ldap.entities;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.ldap.LdapConnectionHandler;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore;
import com.sun.apoc.spi.ldap.environment.LdapEnvironmentMgr;
/**
 * Provides (abstract) access to an entity tree stored 
 * in a LDAP backend
 * 
 */
public abstract class LdapEntityProvider implements EntityTreeProvider {

    protected Node mRootNode;
    protected PolicySource mPolicySource;
    protected LdapConnectionHandler mConnection;
    protected LdapDataStore mDataStore;
    protected LdapEnvironmentMgr mEnvironmentMgr;
    protected String mURL;
    
    public LdapEntityProvider(PolicySource aPolicySource, String url) 
            throws SPIException {
        mPolicySource = aPolicySource;
        mURL = url;
        mEnvironmentMgr = new LdapEnvironmentMgr(mPolicySource.getEnvironment());
        mConnection = (LdapConnectionHandler) mPolicySource.getConnectionHandler(mURL);
        if (mConnection == null) {
            // if mConnection is null, no Ldap connection has been
            // established yet, the environment hasn't been checked
            mEnvironmentMgr.checkEnvironment();
        }
	}

    /**
     * Closes the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     * @throws 			CloseConnectionException if connection error occurs
     */
    public void close() throws SPIException {
        if (mPolicySource.getConnectionHandler(mURL) != null) {
            mPolicySource.setConnectionHandler(mURL, null);
        }
        mConnection.disconnect();
    }

    /**
     * Returns the root node of the Entity Tree
     * 
     * @return            root node 
     * @throws            <code>SPIException</code> if error occurs
     */
    public Node getRootEntity() throws SPIException {
        return mRootNode;
    }

    /**
     * Returns the entity for this id in the Entity Tree
     * 
     * @param id         id string
     * @return            entity 
     * @throws            <code>SPIException</code> if error occurs
     */
    public Entity getEntity(String id) throws SPIException {
        Entity entity = null;
        if (id.equals(mRootNode.getId())) {
            entity = mRootNode;
        }
        else {
            entity = mRootNode.getEntity(id);
        }
        return entity;
    }
}
