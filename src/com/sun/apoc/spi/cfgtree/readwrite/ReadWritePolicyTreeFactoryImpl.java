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

package com.sun.apoc.spi.cfgtree.readwrite;

import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.XMLReader;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.PolicyTreeFactoryImpl;
import com.sun.apoc.spi.policies.InvalidPolicyException;
import com.sun.apoc.spi.policies.Policy;

/**
  * Implementation for a read write policy tree.
  *
  */
public class ReadWritePolicyTreeFactoryImpl extends PolicyTreeFactoryImpl {
    private static final String MODULE = "ReadWritePolicyTreeFactoryImpl";

    /**
      * Returns the read/write <code>PolicyTree</code> object  
      * derived from the <code>Iterator</code> of policies.
      *
      * @param aPolicies    policies
      * @return             the <code>PolicyTree</code> object
      * @throws SPIException if error occurs
      * @throws InvalidPolicyException if aPolicies is null or empty
      *            
      */
    public PolicyTree getPolicyTree(Iterator aPolicies) 
        	throws SPIException {
        if (aPolicies == null || !aPolicies.hasNext()) {
            throw new InvalidPolicyException(
                    InvalidPolicyException.NULL_POLICIES_KEY);
        }
        ArrayList policyList = new ArrayList();
        while (aPolicies.hasNext()) {
            policyList.add(aPolicies.next());
        }
        return createReadWritePolicyTree(policyList);
    }

    /**
      * Returns the <code>PolicyTree</code> object derived from the 
      * policy.
      *
      * @param aPolicy    policy object
      * @return           the <code>PolicyTree</code> object
      * @throws SPIException if error occurs
      * @throws InvalidPolicyException if aPolicy is null
      */
    public PolicyTree getPolicyTree(Policy aPolicy) 
        throws SPIException {
        if (aPolicy == null) {
            throw new InvalidPolicyException();
        }
        ArrayList policyList = new ArrayList();
        policyList.add(aPolicy);
        return createReadWritePolicyTree(policyList);
    }

    /**
      * Creates the read/write <code>PolicyTree</code> object derived from the 
      * list of policies.
      *
      * @param aPolicies    policies
      * @return             the <code>PolicyTree</code> object
      * @throws             <code>SPIException</code> if error
      *                     occurs
      *            
      */
    public PolicyTree createReadWritePolicyTree(ArrayList aPolicies) 
        throws SPIException {
        XMLReader reader = super.getXMLReader();
        return new ReadWritePolicyTreeImpl(aPolicies, reader);
    }

}

