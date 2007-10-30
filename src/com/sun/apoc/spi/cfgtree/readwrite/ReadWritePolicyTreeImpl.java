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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.NodeParsing;
import com.sun.apoc.spi.cfgtree.OperationType;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.PolicyTreeImpl;
import com.sun.apoc.spi.cfgtree.XMLPolicyTreeException;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNode;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.policynode.ReadWritePolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.property.PropertyImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Interface for a read write policy tree.
  *
  */
public class ReadWritePolicyTreeImpl extends PolicyTreeImpl {
    private XMLReader mReader;
    private NodeParsing mParser = new NodeParsing();
    private static final String MODULE = "PolicyTreeImpl";

    /**
      * Constructor for the class.
      *
      * @param aPolicies  list of policies
      * @throws           <code>SPIException</code> if error
      *                   occurs
      */
    public ReadWritePolicyTreeImpl(ArrayList aPolicies, XMLReader aReader) 
        throws SPIException{
        mReader = aReader;
        mParser.setPolicyTree(this);
        mReader.setContentHandler(mParser);
        mReader.setErrorHandler(mParser);
        ReadWritePolicyNodeImpl rootNode = 
            createRootNode(aPolicies);
        super.setRootNode(rootNode);
    }

    /**
      * Constructor for the class.
      *
      * @param aPolicy          policy object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      */
    public ReadWritePolicyTreeImpl(Policy aPolicy, XMLReader aReader) 
        throws SPIException{
        mReader = aReader;
        mParser.setPolicyTree(this);
        mReader.setContentHandler(mParser);
        mReader.setErrorHandler(mParser);
        ArrayList policyList = new ArrayList();
        policyList.add(aPolicy);
        ReadWritePolicyNodeImpl rootNode = 
            createRootNode(policyList);
        super.setRootNode(rootNode);
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
    public PolicyNode createNode(String aPath) throws SPIException {
        return createNode(aPath, false);
    }

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the data path.
      *
      * @param aPath            path for node
      * @param aIsReplaceOp     <code>true</code> if new node should have
      *                         attribute "op=replace", otherwise
      *                         <code>false</code>
      * @return                 the <code>PolicyNode</code> object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      *            
      */
    public PolicyNode createNode(String aPath, boolean aIsReplaceOp) 
        throws SPIException {
        PolicyNodeImpl root = (PolicyNodeImpl)getRootNode();
        root.checkIfReadOnly();
	    if (aPath == null) {  return null; }
	    StringTokenizer st =
		   new StringTokenizer(aPath, PolicyTree.PATH_SEPARATOR);
        if (st.hasMoreTokens()) {
            String policyId = st.nextToken();
            /* TO FIX
            if (!policyId.equals(getTopPolicy().getId())) {
                throw new SPIException("Invalid path specified.",
                        SPIException.ERROR_OCCURRED,
                        MODULE, 0);
            }
            */
        }
        int numOfNodes = st.countTokens();
        PolicyNodeImpl newNode = null, childNode = null;
        String childName = null;
        if (numOfNodes != 0) {
            /* create the intervening nodes if necessary */
            for (int i = 1; i < numOfNodes ; ++i) {
                childName = decodePath(st.nextToken());
                childNode = root.getChildNode(childName);
                if (childNode == null) {
                    root.addNode(childName);
                }
                root = root.getChild(childName);
            }
            childName = decodePath(st.nextToken());
            /* check if the node to be created already exists, if it
               does, then just return that node */
            newNode = root.getChild(childName);
            if (newNode != null &&
                  (newNode.getOperationType() != OperationType.OP_REMOVE)) {
                return newNode;
            }
            newNode = (PolicyNodeImpl)((ReadWritePolicyNodeImpl)root).addNode(childName, aIsReplaceOp);
        }
        /* if no valid child then return the component node, otherwise
           return the requested node */
        return (childName == null) ? root : newNode;
    }

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
    public PolicyNode createReplaceNode(String aPath) throws SPIException {
        return createNode(aPath, true);
    }

    /**
      * Creates and returns the <code>PolicyNode</code> object 
      * representing the root object.
      *
      * @param aPolicies        <code>ArrayList</code> of policies
      * @return                 the <code>ReadWritePolicyNodeImpl</code> object
      *                         representing the root object
      * @throws                 <code>SPIException</code> if error
      *                         occurs
      *            
      */
    private ReadWritePolicyNodeImpl createRootNode(ArrayList aPolicies) 
        throws SPIException {
        if (aPolicies.isEmpty()) { return null; }
        checkPolicyIdsMatch(aPolicies);
        int numOfPolicies = aPolicies.size();
        Policy topPolicy = (Policy)aPolicies.get(numOfPolicies - 1);
        super.setTopPolicy(topPolicy);
	    NodeKey nodeKey = new NodeKey();
	    nodeKey.mPolicyId = topPolicy.getId();
	    nodeKey.mLocale = PolicyTree.DEFAULT_LOCALE_NAME;
        ReadWritePolicyNodeImpl rootNode = null, tmpNode = null;
	    boolean isParentUpdateLayer = true;
	    /* if only one policy then no merging is required,
           however would still have to do some processing
           of the node */
	    for (int i = 0; i < numOfPolicies ; ++i) {
            Policy policy = (Policy)aPolicies.get(i);
            nodeKey.mLayer = policy;
            if (policy.getData() != null) {
                tmpNode = parseData(policy.getData(), nodeKey);
            }
            if (tmpNode != null) {
		       checkLayerRootNodeValidity(tmpNode, topPolicy.getId());
		        if (isParentUpdateLayer && i == numOfPolicies - 1) {
                    isParentUpdateLayer = false;
                    /* set the default nodes, that is the
                       nodes as they would be if this
                       layer was not read */
                    if (rootNode != null) {
                        rootNode.addNodeDefaults();
                    }
		        }
                if (rootNode == null) {
                    rootNode = tmpNode;
                    if (numOfPolicies == 1) {
                        rootNode.setSettingsForAddedNode(
					        rootNode.getPackage() +
					        "." + rootNode.getName(),
					          nodeKey.mLayer,
					    isParentUpdateLayer);
                    } else {
                        rootNode.expand(rootNode.getPackage() +
					    "." + rootNode.getName(),
					    isParentUpdateLayer);
                    }
                    if (isParentUpdateLayer) {
                        /* then go through node tree, setting
                           to readonly any nodes that are finalized */
                        rootNode.setReadOnly();
                    }
		        } else {
                    readMergeUpdateLayer(rootNode, tmpNode,
                          nodeKey, isParentUpdateLayer);
                }
                /* if the component node is marked readonly, then return */
		        if (rootNode.isReadOnly()) { break ; }
            }
	    }
        if (rootNode == null) {
            rootNode = createRootNode(topPolicy);
        }
        if (isParentUpdateLayer) {
            /* there is no data for this entity layer,
               so set the default nodes */
            rootNode.addNodeDefaults() ;
	    }
	    return rootNode;
    }

   /** 
    * Parses the XMLblob and returns a <code>ReadWritePolicyNodeImpl</code> 
    * representing this data. 
    *  
    * @param aPolicyData    data to be parsed
    * @param aNodeKey       the <code>NodeKey</code> specifying the layer 
    * @return               a <code>ReadWritePolicyNodeImpl</code> 
    *                       representing the data in the XMLblob
    * @throws               <code>SPIException</code> if error occurs
    *                       when parsing data
    */
   private ReadWritePolicyNodeImpl parseData(String aPolicyData, 
           NodeKey aNodeKey) throws SPIException{
	    if (aPolicyData == null) { return null; }
	    StringReader input = new StringReader(aPolicyData);
	    InputSource source = new InputSource(input) ;
	    try {
	        mParser.setSourceLayer(aNodeKey.mLayer);
	        mParser.setLocale(aNodeKey.mLocale);
	        mParser.setPolicyId(aNodeKey.mPolicyId);
 	        mReader.parse(source) ;
	        return (ReadWritePolicyNodeImpl)mParser.getRoot(); 
	    } catch (Exception e) {
	        throw new XMLPolicyTreeException(e);
	    }
    }

    /**
     * Carries out the merge operation between the source
     * layer and the next update layer. 
     *
     * @param aResultNode           the base node     
     * @param aUpdateLayer          the update layer 
     * @param aUpdateNodeKey        information on update node
     * @param aIsParentUpdateLayer  boolean indicating if parent layer
     * @throws                      <code>SPIException</code> 
     * 			                    if error occurs
     */ 
    private void readMergeUpdateLayer(PolicyNodeImpl aResultNode, 
		PolicyNodeImpl aUpdateLayer, NodeKey aUpdateNodeKey, 
		boolean aIsParentUpdateLayer) throws SPIException {
	    String updatePath = aUpdateLayer.getPackage() + "." + 
				aResultNode.getName();
        /* Check if this update node is protected. If it is, and this
           is a parent layer being processed, then set the readonly
           attribute to true for this node and its children. If it is,
           finalized and this is not a parent layer, then set the finalized 
	       attribute to true */
	    if (aUpdateLayer.isProtected()) {
	        aResultNode.setFinalized(true, aResultNode.getAbsolutePath(),
	                				 aUpdateLayer.getOriginOfProtection());
	        if (aIsParentUpdateLayer) { 
	            aResultNode.setReadOnly();
	        }
	    }
	    /* process nodes from aUpdateLayer */
	    Vector updateChildrenNames = aUpdateLayer.getAllChildrenNames();
	    if (updateChildrenNames != null) {
            /* path to current node in update layer, used for
		        exception messages */
            updatePath += PolicyTree.PATH_SEPARATOR;
	        int size = updateChildrenNames.size();
	        for (int i = 0; i < size; i++) {
                String name = (String)updateChildrenNames.get(i);
                PolicyNodeImpl updateNode = null;
                if (aUpdateLayer.getChildNodeTable() != null) {
		            updateNode = 
                        (PolicyNodeImpl)aUpdateLayer.getChildNodeTable().get(name);
		            if (updateNode != null &&
			            updateNode.getName() != null) {
                        String childUpdatePath = updatePath +
			            updateNode.getName();
		                updateNode.processReadOperation(
			                aResultNode, aUpdateNodeKey,
			                childUpdatePath, aIsParentUpdateLayer);
		            }
	            }
                if (updateNode == null && 
                        aUpdateLayer.getPropertyTable() != null) {
                    PropertyImpl updateProperty = 
                        (PropertyImpl)aUpdateLayer.getPropertyTable().get(name);
		            if (updateProperty != null &&
			            updateProperty.getName() != null) {
                        String childUpdatePath = updatePath +
			            updateProperty.getName();
		                updateProperty.processReadOperation(
			                aResultNode, aUpdateNodeKey,
			                childUpdatePath, aIsParentUpdateLayer);
		            }
	            }
            }

	    }
    }

    /**
     * Creates a root node where no policy data exists.
     *
     * @param aTopPolicy   policy object 
     * @return             root node
     * @throws             <code>SPIException</code> if
     *			           error occurs
     */
    private ReadWritePolicyNodeImpl createRootNode(Policy aPolicy)
			throws SPIException {
        ReadWritePolicyNodeImpl returnNode = 
            new ReadWritePolicyNodeImpl();
        String policyId = aPolicy.getId();
        String packageName = null;
        String name = null;
        String separator = ReadWritePolicyNodeImpl.ID_SEPARATOR;
        int lastIndex = policyId.lastIndexOf(separator);
        if (lastIndex >= 0) {
            packageName = policyId.substring(0, lastIndex);
            name = policyId.substring(
                    lastIndex+separator.length());
        }
        returnNode.setPackage(packageName);
        returnNode.setName(name);
        returnNode.setPolicyTree(this);
        returnNode.setOrigin(aPolicy);
	    return returnNode;
    }
}
