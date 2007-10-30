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

package com.sun.apoc.spi.ldap ;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import netscape.ldap.LDAPConnection;

public class LdapConnectionPool {
    private Set mUsedConnections = new HashSet() ;
    private Stack mFreeConnections = new Stack() ;
    private String mServer = null ;
    private int mPort = 389 ;

    private static final int MAX_CONN = 50 ;

    public LdapConnectionPool(String aServer, int aPort) {
        mServer = aServer ; 
        mPort = aPort ;
    }
    public synchronized LDAPConnection getConnection() {
        LDAPConnection retCode = null ;

        if (!mFreeConnections.empty()) {
            retCode = (LDAPConnection) mFreeConnections.pop() ;
            if (retCode.isConnected()) { mUsedConnections.add(retCode) ; }
            else { retCode = null ; }
        }
        return retCode ;
    }
    public synchronized void addConnection(LDAPConnection aConnection) {
        if (mUsedConnections.size() + mFreeConnections.size() < MAX_CONN) {
            mUsedConnections.add(aConnection) ;
        }
    }
    public synchronized boolean closeConnection(LDAPConnection aConnection) {
        if (mUsedConnections.contains(aConnection)) {
            mUsedConnections.remove(aConnection) ;
            mFreeConnections.push(aConnection) ;
            return true ;
        }
        return false ;
    }
}
