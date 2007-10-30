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

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNode;

/**
  * Interface for a policy tree.
  *
  */
public interface PolicyTree {
    public static final int BUFFER_SIZE = 200;
    public static final String DEFAULT_LOCALE_NAME="default";
    /** tab length */
    public static final char TAB = 0x9;
    /** path name separator */
    public static final String PATH_SEPARATOR = "/"; 

    /*
     * Returns the <code>PolicyNode</code> object representing
     * the root node.
     *
     * @return    the <code>PolicyNode</code> object
     */
    public PolicyNode getRootNode() ;

    /**
      * Returns a boolean indicating if the tree has
      * been modified. 
      *
      * @return    <code>true</code> if the node has been
      *            modified, otherwise <code>false</code>
      */
    public boolean hasBeenModified() ;

    /**
      * Returns the <code>PolicyNode</code> object representing
      * the data path.
      *
      * @param aPath  path for node
      * @return       the <code>PolicyNode</code> object
      * @throws       <code>SPIException</code> if error
      *               occurs
      */
    public PolicyNode getNode(String aPath) throws SPIException ;

    /**
      * Returns the Policy id for this PolicyTree.
      *
      * @return       the policy id 
      */
    public String getPolicyId();

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the data path.
      *
      * @param aPath            path for node
      * @return                 the <code>PolicyNode</code> object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      */
    public PolicyNode createNode(String aPath) throws SPIException ;

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the list element for this data path.
      *
      * @param aPath            path for node
      * @return                 the <code>PolicyNode</code> object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      */
    public PolicyNode createReplaceNode(String aPath) 
        throws SPIException ;

    /**
      * Returns a boolean indicating if the node for this
      * data path exists.
      *
      * @param aPath   node path
      * @return        <code>true</code> if the node exists, 
      *                otherwise <code>false</code>
      * @throws    <code>SPIException</code> if error
      *            occurs
      */
    public boolean nodeExists(String aPath) throws SPIException;

}
