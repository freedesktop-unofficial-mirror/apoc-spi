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
import com.sun.apoc.spi.entities.AbstractEntity;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.DomainTreeProvider;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.ldap.LdapConnectionHandler;

/**
 * Provides access to a domain tree stored in a LDAP backend
 * 
 */
public class LdapDomainProvider 
	extends LdapEntityProvider implements DomainTreeProvider {
    public LdapDomainProvider(PolicySource aPolicySource, String url) 
            throws SPIException {
	    super(aPolicySource, url);
	}

    /**
     * Opens the connection to the datasource
     * 
     * @throws            <code>SPIException</code> if error occurs
     * @throws 			OpenConnectionException if connection error occurs
     * @throws 			CloseConnectionException if connection error occurs
     * @throws 			AuthenticateException if  connection error occurs
     */
    public void open() throws SPIException {
        if ((mConnection == null) && (mEnvironmentMgr != null)) {
            mConnection = new LdapConnectionHandler();
            mConnection.connect(mURL, 
    		        mEnvironmentMgr.getDomainTimeout(),
    		        mEnvironmentMgr.getDomainAuthUser(), 
	        		mEnvironmentMgr.getDomainAuthPassword(),
	        		mEnvironmentMgr);
            if (isGSSAPIAuthentication()){
                Object callbackHandler = 
                    mEnvironmentMgr.getDomainCallbackHandler();
                mConnection.authenticate(callbackHandler);
            }
    		else {
    		    mConnection.authenticate(mEnvironmentMgr.getDomainUser(mURL),
    		            mEnvironmentMgr.getDomainCredentials());
    		}
            mConnection.closeAuthorizedContext();

            // store the new LdapConnectionHandler in the PolicyManager
            mPolicySource.setConnectionHandler(mURL, mConnection);
        }
        mDataStore = mConnection.getDataStore();
        mRootNode = mDataStore.getRootDomain();
        ((AbstractEntity)mRootNode).setPolicySource(mPolicySource);
    }
    
    protected boolean isGSSAPIAuthentication() {
        return mEnvironmentMgr.getDomainAuthType()
        		.equals(EnvironmentConstants.LDAP_AUTH_TYPE_GSSAPI);
    }

    public Domain getRootDomain() throws SPIException
    {
        return (Domain)super.getRootEntity();
    }
}
