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

package com.sun.apoc.spi.cfgtree;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.policynode.InvalidPolicyNodeException;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNode;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.policies.MismatchPolicyException;
import com.sun.apoc.spi.policies.Policy;

/**
  * Abstract class for a policy tree.
  *
  */
public abstract class PolicyTreeImpl implements PolicyTree { 
    private PolicyNodeImpl mRootNode;
    private Policy mTopPolicy;
    private static final String MODULE = "PolicyTreeImpl";

    /**
     * Checks the validity of a layer root node by ensuring
     * it has a name and the correct package name.
     * 
     * @param aLayerRootNode	the layer root node
     * @param aPolicyId			the policy id
     * @throws InvalidPolicyNodeException if node is invalid
     */
    public void checkLayerRootNodeValidity(
            PolicyNodeImpl aLayerRootNode, 
		    String aPolicyId) throws SPIException {
	    if (aLayerRootNode.getName() == null) {
	        throw new InvalidPolicyNodeException();
	    }
        /* the update layer should specify the correct package */
	    String packageName = aLayerRootNode.getPackage();
	    String name = aLayerRootNode.getName();
	    if (packageName == null || name == null ||
		    !aPolicyId.endsWith(packageName + "." + name)) {
	        throw new InvalidPolicyNodeException( 
	                packageName + "." + name, aPolicyId);
	    }
    }

    /**
      * Checks to see that the list of policies have the same
      * policy id. If not an exception is thrown.
      *
      * @param aPolicies list of policies
      * @throws MismatchPolicyException if don't match
      */
    public void checkPolicyIdsMatch(ArrayList aPolicies) 
        throws SPIException {
        String policyId = ((Policy)aPolicies.get(0)).getId();
        for (int i = 1; i < aPolicies.size(); ++i) {
            Policy policy = (Policy)aPolicies.get(i);
            if (!policy.getId().equalsIgnoreCase(policyId)) {
                throw new MismatchPolicyException();
            }
        }
    }

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the data path.
      *
      * @param aPath            path for node
      * @return                 the <code>PolicyNode</code> object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      *            
      */
    public abstract PolicyNode createNode(String aPath) 
        throws SPIException ;

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the list element for this data path.
      *
      * @param aPath            path for node
      * @return                 the <code>PolicyNode</code> object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      *            
      */
    public abstract PolicyNode createReplaceNode(String aPath) 
        throws SPIException ;

    /**
      * Returns the <code>PolicyNode</code> object representing
      * the data path.
      *
      * @param aPath  path for node
      * @return       the <code>PolicyNode</code> object
      * @throws       <code>SPIException</code> if error
      *               occurs
      */
    public PolicyNode getNode(String aPath) throws SPIException {
        if (aPath.equals(mRootNode.getAbsolutePath())) { return mRootNode; }
        StringTokenizer st = new StringTokenizer(aPath,
                PolicyTree.PATH_SEPARATOR);
        if (st.hasMoreTokens()) {
            String policyId = st.nextToken();
            if (!policyId.equals(mTopPolicy.getId())) {
                throw new MismatchPolicyException();
            }
        }
        PolicyNodeImpl retCode = mRootNode;
        while (st.hasMoreTokens()) {
            retCode = retCode.getChildNode(decodePath(st.nextToken()));
            if (retCode == null) {
                return null;
            } 
        }
        return retCode;
    }

    /**
      * Returns the <code>Policy</code> for this policy tree.
      *
      * @return  <code>Policy</code> corresponding to the 
      *          top most layer for this policy tree
      */
    public Policy getPolicy() {
        return mTopPolicy;
    }

    /**
      * Returns the Policy id for this PolicyTree.
      *
      * @return       the policy id 
      */
    public String getPolicyId() {
        return mTopPolicy.getId();
    }

    /**
      * Returns the <code>PolicyNode</code> object representing
      * the root node.
      *
      * @return    the <code>PolicyNode</code> object
      */
    public PolicyNode getRootNode() {
        return mRootNode;
    }

    /**
      * Returns the top policy for this policy tree.
      *
      * @return       the  top policy
      *            
      */
    public Policy getTopPolicy() { return mTopPolicy; }


    /**
      * Returns a boolean indicating if the tree has
      * been modified. 
      *
      * @return    <code>true</code> if the node has been
      *            modified, otherwise <code>false</code>
      *            
      */
    public boolean hasBeenModified() {
        return mRootNode.hasBeenModified();
    }

    /**
      * Returns a boolean indicating if the node for this
      * data path exists.
      *
      * @param aPath   node path
      * @return        <code>true</code> if the node exists, 
      *                otherwise <code>false</code>
      *            
      */
    public boolean nodeExists(String aPath) throws SPIException {
        if (aPath.equals(mRootNode.getAbsolutePath())) { return true; }
        StringTokenizer st = new StringTokenizer(aPath,
                PolicyTree.PATH_SEPARATOR);
        if (st.hasMoreTokens()) {
            String policyId = st.nextToken();
            if (!policyId.equals(mTopPolicy.getId())) {
                throw new MismatchPolicyException();
            }
        }
        boolean retCode = false;
        PolicyNodeImpl childNode = mRootNode;
        int tokens = st.countTokens();
        for (int i = 0; i < tokens; ++i) {
            childNode = childNode.getChildNode(decodePath(st.nextToken()));
            if (childNode != null) {
                retCode = true;
            } else {
                retCode = false;
                break; 
            }
        }
        return retCode;
    }

    /*
     * Sets the <code>PolicyNode</code> object representing
     * the root node.
     *
     * @param aRootNode    the <code>PolicyNode</code> object
     */
    public void setRootNode(PolicyNodeImpl aRootNode) {
         mRootNode = aRootNode;
    }

    /**
      * Sets the top policy for this policy tree.
      *
      * @param aTopPolicy       the  policy id
      *            
      */
    public void setTopPolicy(Policy aTopPolicy) { 
        mTopPolicy = aTopPolicy;
    }

    /**
     * Helper method for decoding path elements (via URLDecoder). 
     * 
     * At the moment we use the slash '/' to separate the elements of config node 
     * paths (e.g. 'com.sun.apoc/node1/node2'). However, node names might also 
     * include a slash. To correctly identify nodes within a path, we require
     * that node names with inluded slashes must be encoded/escaped (via 
     * URLEncoder). For example, a node with name 'text/html' might appear as
     * 'com.sun.apoc/node1/text%2Fhtml' within a path.
     *
     * @param pathElement  The part of the path that should be decoded.
     */
    public static String decodePath(String pathElement) throws SPIException {
        try {
            // decoding a string is a somewhat expensive operation. For 
            // performance reasons we therefore check, if decoding is required
            // at all.
            if (pathElement.indexOf('%') == -1) {
                return pathElement;
            } else {
                return URLDecoder.decode(pathElement, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            throw new SPIException(ex);
        }
    }
}
