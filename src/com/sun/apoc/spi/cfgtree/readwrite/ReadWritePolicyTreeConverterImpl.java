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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.PolicyTreeConverter;
import com.sun.apoc.spi.cfgtree.PolicyTreeImpl;
import com.sun.apoc.spi.cfgtree.XMLStreamable;
import com.sun.apoc.spi.cfgtree.policynode.ReadWritePolicyNodeImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Implementation for a read/write PolicyTree Converter.
  *
  */
public class ReadWritePolicyTreeConverterImpl implements PolicyTreeConverter{

    /**
      * Returns the <code>Policy</code> object derived from the 
      * <code>PolicyTree</code>.
      *
      * @param aPolicyTree  the policy tree 
      * @return             the <code>Policye</code> object
      * @throws           <code>SPIException</code> if error
      *                   occurs
      *            
      */
    public Policy getPolicy(PolicyTree aPolicyTree) 
        throws SPIException {
	    NodeKey nodeKey = new NodeKey();
	    nodeKey.mLayer = ((PolicyTreeImpl)aPolicyTree).getTopPolicy();
	    ReadWritePolicyNodeImpl updateNode = 
            (ReadWritePolicyNodeImpl)aPolicyTree.getRootNode();
	    ReadWritePolicyNodeImpl newNode = null;
	    /* find the updates specific to this layer */
	    newNode = createUpdateNode(updateNode, nodeKey.mLayer);
	    String outputData = null;
	    /* the newNode may be null if no update is required */
	    if (newNode != null) {
	        ByteArrayOutputStream streamOutput =
			new ByteArrayOutputStream();
	        PrintStream output = new PrintStream(streamOutput);
	        newNode.printToStream("", output, XMLStreamable.UPDATE_SCHEMA);
	        outputData = streamOutput.toString();
        }
	    Policy topPolicy = ((PolicyTreeImpl)aPolicyTree).getTopPolicy();
	    Policy newPolicy = new Policy(topPolicy.getId(), 
	            topPolicy.getProfileId(), outputData);
        return newPolicy;
    }

    /**
     * Creates the new update node to write to the backend.
     * 
     * @param aUpdateNode	    the updated node 
     * @param aLayer		    the layer to update
     * @return			        the new update layer node, or null
     *					        if no updates were detected 
     * @throws			        <code>SPIException</code> if
     *				            error occurs	
     */
    private ReadWritePolicyNodeImpl createUpdateNode(
            ReadWritePolicyNodeImpl aUpdateNode, Policy aLayer)
		throws SPIException {
	    ReadWritePolicyNodeImpl outputNode = 
            (ReadWritePolicyNodeImpl)aUpdateNode.shallowCopy();
	    String updatePath = (String)aUpdateNode.getPackage() + "." +
		aUpdateNode.getName() + PolicyTree.PATH_SEPARATOR;
	    /* process nodes from update node.*/ 
	    boolean childrenUpdate =
	        aUpdateNode.processUpdateNodeChildren(outputNode, 
		    aLayer, updatePath);
	    if (!childrenUpdate) {
	        /* as there are no updates for the child nodes, a
	           write will only be required if the component node
	           has been marked as finalized */
	        if (aUpdateNode.isProtected()) {
	            outputNode = 
                    (ReadWritePolicyNodeImpl)aUpdateNode.shallowCopy();
	        } else {
		        outputNode = null;
	        }
	    }
        return outputNode; 
    }
}
