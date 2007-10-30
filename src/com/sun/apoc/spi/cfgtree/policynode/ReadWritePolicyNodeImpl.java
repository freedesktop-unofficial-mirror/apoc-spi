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

package com.sun.apoc.spi.cfgtree.policynode;

import java.util.Enumeration;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.ConfigElementImpl;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.OperationType;
import com.sun.apoc.spi.cfgtree.property.InvalidPropertyNameException;
import com.sun.apoc.spi.cfgtree.property.Property;
import com.sun.apoc.spi.cfgtree.property.PropertyImpl;
import com.sun.apoc.spi.cfgtree.property.ReadWritePropertyImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Class for a read write policy node.
  *
  */
public class ReadWritePolicyNodeImpl extends PolicyNodeImpl {

    public static final String ID_SEPARATOR = ".";
    
    /** default node - used for reset */
    public ReadWritePolicyNodeImpl mDefaultNode;
    private boolean mHasBeenModified = false;

    /**
      * Adds a new node.
      *
      * @param aName    name of the node to be added
      * @return         the newly added <code>ReadWritePolicyNodeImpl</code>
      * @throws         <code>SPIException</code> if error occurs
      */
    public PolicyNode addNode(String aName) throws SPIException {
        return addNode(aName, false);
    }

    /**
      * Adds a new node. Operation attribute is set to true if
      * appropriate.
      *
      * @param aName          name of the node to be added
      * @param aIsReplaceOp   <code>true</code> if operation attribute
      *                       should be set, otherwise <code>false</code>
      * @return              the newly added <code>ReadWritePolicyNodeImpl</code>
      * @throws SPIException if error occurs
      * @throws InvalidPolicyNodeNameException if aName is null
      */
    public PolicyNode addNode(String aName, boolean aIsReplaceOp) 
        throws SPIException {
	    if (aName == null) {
	        throw new InvalidPolicyNodeNameException(aName);
	    }   
        checkIfReadOnly();
	    ReadWritePolicyNodeImpl newNode = new ReadWritePolicyNodeImpl();
	    newNode.setName(aName);
	    newNode.setSettingsForAddedNode(appendToPath(getAbsolutePath(), aName),
		    mPolicyTree.getPolicy(), false);
        newNode.mPolicyTree = mPolicyTree;
        if (aIsReplaceOp) {
            newNode.setOperationType(OperationType.OP_REPLACE);
        }
   	    addChildNode(newNode);
        mHasBeenModified = true;
	    return newNode;
    }

    /**
     * Sets default nodes for nodes in the tree. Used for the
     * base source layer during a read merge.
     *
     * @throws       <code>SPIException</code> if error
     * 	             occurs
     */
    public void addNodeDefaults() throws SPIException {
        setDefaultNode((ReadWritePolicyNodeImpl)shallowCopy()) ;
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                ReadWritePolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (ReadWritePolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.addNodeDefaults();
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    ReadWritePropertyImpl property =
                        (ReadWritePropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.setDefaultProperty(
                                (ReadWritePropertyImpl)property.shallowCopy()) ;
                    }
                }

	        }
        }
    }

    /**
     * Creates and adds a property node to this node.
     *
     * @param aPropertyName   name of new property
     * @return                the new node
     * @throws	SPIException  if error occurs
     * @throws InvalidPropertyNameException if aName is null
     */
     public Property addProperty(String aPropertyName)
             throws SPIException{
	    if (aPropertyName == null) {  
	        throw new InvalidPropertyNameException(aPropertyName);
	    }
	    /* if this node is marked readonly then the property 
           cannot be added */
	    checkIfReadOnly();
	    ReadWritePropertyImpl property = new ReadWritePropertyImpl();
	    property.setName(aPropertyName);
	    property.setAddedAtTopLayer();
	    property.setPath(appendToPath(getAbsolutePath(), aPropertyName));
	    if (isProtected()) {
	        property.setFinalized(true, getAbsolutePath(),
	                			  getOriginOfProtection());
	    }
        property.setPolicyTree(mPolicyTree);
	    addProperty(property);
        mHasBeenModified = true;
	    return property;
    }

    /**
     * Creates and adds a property node to this node.
     *
     * @param aPropertyName   name of new property
     * @param aOriginLayer    layer for which a
     *                        node is being added
     * @return                the new node
     * @throws SPIException   if error occurs
     * @throws InvalidPropertyNameException if aName is null
     */
     public ReadWritePropertyImpl addProperty(String aPropertyName, 
             Policy aOriginLayer) throws SPIException{
	    if (aPropertyName == null) {  
	        throw new InvalidPropertyNameException(aPropertyName);
	    }
	    /* if this node is marked readonly then the property 
           cannot be added */
	    checkIfReadOnly();
	    ReadWritePropertyImpl property = new ReadWritePropertyImpl();
	    property.setName(aPropertyName);
	    property.setAddedAtTopLayer();
	    property.setOrigin(aOriginLayer);
	    property.setPath(appendToPath(getAbsolutePath(), aPropertyName));
	    if (isProtected()) {
	        property.setFinalized(true, getAbsolutePath(),
	                			  getOriginOfProtection());
	    }
	    addProperty(property);
        mHasBeenModified = true;
	    return property;
    }

    /**
      * Adds a node with attribute "op=replace".
      *
      * @param aName      name of the node to be replaced
      * @return           the newly replaced <code>ReadWritePolicyNodeImpl</code>
      * @throws           <code>SPIException</code> if error occurs
      */
    public PolicyNode addReplaceNode(String aName) throws SPIException {
        return addNode(aName, true);
    }


    /**
     * Clears the settings added at this layer.
     *
     * @throws   <code>SPIException</code> if 
     *           error occurs
     */
    public void clear() throws SPIException {
	    /* cannot apply reset operation if node or one
	       of its children is readonly */ 
       	checkIfNodeOrChildrenReadOnly();
	    ReadWritePolicyNodeImpl defaultNode = mDefaultNode;
	    PolicyNodeImpl parent = (PolicyNodeImpl)getParent();
	    if (defaultNode != null) {
	        setNodeAndChildrenToDefault(defaultNode);
	        defaultNode.setOperationType(OperationType.OP_RESET);
            /* replace the node in the policy tree with
	           the default node.  */
	        if (parent != null) {
	            if (parent.isProtected()) {
	                defaultNode.setFinalized(true, 
	                        		getNameOfElementWhereProtectionSet(),
	                        		getOriginOfProtection());
		        }
                parent.addChildNode(defaultNode);
	        } 
	    } else {
	        /* we are dealing with a node added at this layer */
	        checkIfMandatory(); 
	        setOperationType(OperationType.OP_REMOVE);
	    }
        mHasBeenModified = true;
    }

    /**
      * Clears the properties. 
      *
      * @throws        <code>SPIException</code> if error occurs
      */
    public void clearProperties() throws SPIException {
	    checkIfReadOnly();
	    checkIfPropertiesReadOnly();
        if (mPropertyTable == null || mPropertyTable.isEmpty()) { return; }
        Enumeration values = mPropertyTable.elements();
        while (values.hasMoreElements()) {
            ReadWritePropertyImpl property = (ReadWritePropertyImpl)values.nextElement();
            PropertyImpl defaultProperty = property.getDefaultProperty();
            if (defaultProperty != null) {
                addProperty(defaultProperty);
            } else {
                removeProperty(property.getName());
            }
        }
        mHasBeenModified = true;
    }


    /**
     * Returns the default node, that is the node as it was in the
     * default layer. 
     *
     * @return    the default node
     * @throws    <code>SPIException</code> if error occurs
     */
    public ReadWritePolicyNodeImpl getDefaultNode() throws SPIException {
	    return ( (mDefaultNode != null) ? (ReadWritePolicyNodeImpl)mDefaultNode.shallowCopy() : null);
    }

    /**
      * Returns a boolean indicating if the node has
      * been modified. 
      *
      * @return    <code>true</code> if the node has been
      *            modified, otherwise <code>false</code>
      */
    public boolean hasBeenModified() {
        if (mHasBeenModified) { return mHasBeenModified; }
        if (mAllChildrenNames != null &&
                !mAllChildrenNames.isEmpty()) {
            for (int i = 0 ; i < mAllChildrenNames.size(); ++i) {
                String name = (String)mAllChildrenNames.get(i);
                ReadWritePolicyNodeImpl policyNode = null;
                if (mChildNodeTable != null && !mChildNodeTable.isEmpty()) {
                    policyNode = 
                        (ReadWritePolicyNodeImpl)mChildNodeTable.get(name);
                    if (policyNode != null) {
                        if (policyNode.hasBeenModified()) {
                            return true;
                        }
                    }
                }
                if (policyNode == null && mPropertyTable != null &&
                        !mPropertyTable.isEmpty()) {
                    ReadWritePropertyImpl property = 
                        (ReadWritePropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        if (property.hasBeenModified()) {
                            return true;
                        }
                    }
                }
            }
        }
        return mHasBeenModified;
    }


    /**
     * Carries out the update merge operations specified in
     * this node's children.
     *
     * @param aOutputNode	        the equivalent node for the new
     *				        update layer
     * @param aLayer		        layer to update
     * @param aUpdateNodePath	        the path to this result node (used
     *				        for exception messages
     * @return			        <code>true</code> if any node children
     *				        are required for the new update layer, 
     * 				        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> if 
     *				        error occurs
     */
    public boolean processUpdateNodeChildren(PolicyNodeImpl aOutputNode,
            								 Policy aLayer, 
            								 String aUpdateNodePath)
    	throws SPIException {
	    boolean update = false;
	    /* if this layer removed any elements,
	        then add these nodes (with op=remove) to the 
	        output child node */
	    if (mRemovedChildren != null) {
	        for (int i = 0; i < mRemovedChildren.size(); i++) { 
	            Object removedChild = 
		            mRemovedChildren.get(i);
	            if (removedChild != null) {
                    if (removedChild instanceof PolicyNodeImpl) {
	                    aOutputNode.addChildNode(
	                    (PolicyNodeImpl)((PolicyNodeImpl)removedChild).shallowCopy());
	                    update = true;
                    } else if (removedChild instanceof PropertyImpl) {
	                    aOutputNode.addProperty(
	                    (PropertyImpl)((PropertyImpl)removedChild).shallowCopy());
	                    update = true;
                    }
		        }
	        }
	    }
	    boolean childUpdate = false;
	    if (mAllChildrenNames != null) {
	        int size = mAllChildrenNames.size();
	        for (int i = 0; i < size; i++) {
                String name = (String)mAllChildrenNames.get(i);
		        String updatePath = null;
		        PolicyNodeImpl updateNode = null;
                if (mChildNodeTable != null) {
                    updateNode = 
		            (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (updateNode != null) {
                        PolicyNodeImpl outputChildNode = 
                            (PolicyNodeImpl)updateNode.shallowCopy();
                        aOutputNode.addChildNode(outputChildNode);
                        updatePath = appendToPath(aUpdateNodePath, updateNode.getName());
                        childUpdate = ((ReadWritePolicyNodeImpl)updateNode)
                            	.processUpdateOperation(outputChildNode, 
                            	        				aLayer, 
                            	        				updatePath);
	                    /* if one or more children are required, then update
	                        required is set to true */
	                    if (!childUpdate) {
	                        ((PolicyNodeImpl)aOutputNode).deleteChildNode(
			                outputChildNode.getName());
	                    } else {
	                        update = true;
		                }
                     }
                }
                if (updateNode == null && mPropertyTable != null) {
                    PropertyImpl property = (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
		                updatePath = appendToPath(aUpdateNodePath, property.getName());
                        PropertyImpl outputProperty =
                             (PropertyImpl)property.shallowCopy();
                        aOutputNode.addProperty(outputProperty);
                        childUpdate = ((ReadWritePropertyImpl)property)
                        	.processUpdateOperation(outputProperty, aLayer,
                        	        				updatePath);
	                    /* if one or more children are required, then update
	                        required is set to true */
	                    if (!childUpdate) {
	                        ((PolicyNodeImpl)aOutputNode).deleteProperty(
			                outputProperty);
	                    } else {
	                        update = true;
		                }
                    }
                }
            }
        }
	    return update || childUpdate;
    }

  
    /**
     * Determines from this update node which operation is to
     * be carried out during the read merge, and then invokes
     * the corresponding operation method.
     *
     * @param aOutputNode	        the equivalent node for the new
     *				        update layer
     * @param aLayer			layer to update
     * @param aUpdateNodePath	        the path to this update node (used
     *				        for exception messages
     * @return			        <code>true</code> if this node is 
     *                                  required for the new update layer,
     *				        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> 
     *                                  if error occurs     
     */
    public boolean processUpdateOperation(PolicyNodeImpl aOutputNode, 
            							  Policy aLayer,
            							  String aUpdateNodePath) 
		throws SPIException {   
	    boolean update = false;
	    switch (mOperationType) {
            case OperationType.OP_REPLACE:
		        update = updateReplaceNode(aOutputNode, aLayer,
				aUpdateNodePath);
		        break;
            case OperationType.OP_REMOVE:
		        update = updateRemoveNode(aOutputNode, aLayer, 
					aUpdateNodePath);
		        break;
            case OperationType.OP_RESET:
		        update = updateResetNode(aOutputNode, aLayer,
					aUpdateNodePath);
		        break;
            case OperationType.OP_UNKNOWN: //defaults to OP_MODIFY
            case OperationType.OP_MODIFY:
                update = updateModifyNode(aOutputNode, aLayer,
					aUpdateNodePath);
       		    break;
    	    };
	    return update;
    }

    /**
     * Carries out the read merge modification operation specified in
     * this update node.
     *
     * @param aResultNode	  node that will be the result of
     *				  the read merge process
     * @param aUpdateNodeKey	  information on update node
     * @param aUpdateNodePath	  the path to this update node (used
     *				  for exception messages
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *				  layer (needed for handling
     *				  finalized attribute): <code>true</code>
     *				  if final layer, otherwise <code>false</code>
     * @throws			  <code>SPIException</code> if error 
     *                            occurs	
     */
    public void readModifyNode(PolicyNodeImpl aResultNode,
	    NodeKey aUpdateNodeKey, String aUpdateNodePath,
	    boolean aIsParentUpdateLayer) throws SPIException {
	    /* If the node exists in the source layer and is
	        marked readonly there then no processing takes place.*/
	    PolicyNodeImpl resultNode =
	        (PolicyNodeImpl)aResultNode.getChild(getName());
	    if (resultNode != null) {
	        if ( resultNode.isReadOnly()) { return; } 
	        /* if the update node is mandatory, then set
	           this flag (and source) on the resulting node */
	        if (isMandatory()) {
                resultNode.setMandatoryFlag();
		        resultNode.setOriginOfMandatory(getOriginOfMandatory());
	        }
	    } else {
	        /* node is introduced by this update layer */
	        resultNode = this;
	        aResultNode.addChildNode(resultNode);
	        if (!aIsParentUpdateLayer) {
		        resultNode.setAddedAtTopLayer();
	        }
        }   
	    resultNode.setPath(aUpdateNodePath);
	    if (mAllChildrenNames != null) {
	        int size = mAllChildrenNames.size();
	        for (int i = 0; i < size; i++) {
                String name = (String)mAllChildrenNames.get(i);
		        String updateNodePath = null;
		        PolicyNodeImpl updateNode = null;
                if (mChildNodeTable != null) {
                    updateNode = 
		            (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (updateNode != null) {
                        updateNodePath = appendToPath(aUpdateNodePath, updateNode.getName());
                        updateNode.processReadOperation(resultNode,
                        aUpdateNodeKey, updateNodePath, aIsParentUpdateLayer);
                    }
                }
                if (updateNode == null && mPropertyTable != null) {
                    PropertyImpl property = (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
		        updateNodePath = appendToPath(aUpdateNodePath, property.getName());
                        property.processReadOperation(resultNode,
                                aUpdateNodeKey, updateNodePath,
                                aIsParentUpdateLayer);
                    }
                }
            }
	    }
	    /* Check if this update node is finalized. If it is, and this
	        is a parent update layer being processed, then set the readonly
	        attribute to true for this node and its children. If it is
	        finalized and this is a source layer, or is not a parent layer,
	        then set the finalized attribute to true */
	    if (isProtected()) {
            try {
		        resultNode.checkIfNodeOrChildrenReadOnly();
		        resultNode.setFinalized(true, resultNode.getAbsolutePath(),
		                				getOriginOfProtection());
		        if (aIsParentUpdateLayer) {
		            resultNode.setReadOnly();
	           	}
	        } catch (SPIException ignore) {}
	    }
    }

    /**
     * Carries out the removal operation specified in
     * this update node. For a read merge this operation 
     * results in the deletion of specified subnodes of a
     * set.
     *
     * @param aResultNode	  node that will be the result of
     *				  the merge process
     * @param aUpdateNodePath	  the path to this update node (used
     *				  for exception messages)
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *				  layer (needed for handling
     *				  finalized attribute): <code>true</code>
     *				  if final layer, otherwise <code>false</code>
     * @throws			  <code>SPIException</code> if 
     * 				  error occurs	
     */
    public void readRemoveNode(PolicyNodeImpl aResultNode,
		String aUpdateNodePath, boolean aIsParentUpdateLayer)
                                        throws SPIException {
	    PolicyNodeImpl childNode = 
		    aResultNode.getChild(getName());
        if (childNode != null) {
	        if (!childNode.isMandatory()) {
	            /* need to keep track of nodes deleted by the final
	                update layer, in order to include them in the update
		            layer written by updateComponent */
		        if (!aIsParentUpdateLayer) {
		            /* set the operation attribute to "remove" */
		            childNode.setOperationType(OperationType.OP_REMOVE);
		            aResultNode.addRemovedChild(childNode);
		        }
		        aResultNode.deleteChildNode(childNode.getName());
	        }
        }
    }

   
    /**
     * Carries out the replacement operation specified in
     * this update node. This operation can only be applied
     * to dynamic elements. The replace operation can either
     * insert a new node or replace a node that already exists.
     * The corresponding node from the source layer is ignored.  
     *
     * @param aResultNode	  node that will be the result of
     *				  the merge process
     * @param aUpdateNodeKey      information on update node
     * @param aUpdateNodePath	  the path to this update node (used
     *				  for exception messages)
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *				  layer (needed for handling
     *				  finalized attribute): <code>true</code>
     *				  if final layer, otherwise <code>false</code>
     * @throws			  <code>SPIException</code> if error occurs   
     */
    public void readReplaceNode(PolicyNodeImpl aResultNode, 
	    NodeKey aUpdateNodeKey, String aUpdateNodePath, 
	    boolean aIsParentUpdateLayer) throws SPIException {

        /* if the corresponding node exists in the source
           layer, and it is mandatory then the replace operation
           is ignored */
	    PolicyNodeImpl sourceLayerNode =
	        aResultNode.getChild(getName());
     	if (sourceLayerNode != null && 
                sourceLayerNode.isMandatory()) {
	        return;
	    }
     	String path = appendToPath(aResultNode.getAbsolutePath(), getName());
	    setSettingsForAddedNode(path, getOrigin(), aIsParentUpdateLayer);

	    /* Check if this update node is finalized. If it is, and this
	        is a parent layer being processed, then set the readonly
	        attribute to true for this node and its children. If it is
	        finalized and this is a source layer, or if this is not
	        a parent layer, then set the finalized attribute to true */
	    if (isProtected()) {
             setFinalized(true, getAbsolutePath(),getOriginOfProtection());
             if (aIsParentUpdateLayer) { 
	             setReadOnly();
	        }
	    }
	    aResultNode.addChildNode(this);
    }

    /**
     * Removes a child node.
     *
     * @param aNodeName    name of node to be removed
     * @throws	           <code>SPIException</code>
     *			           if error occurs
     */
    public void removeNode(String aNodeName)
			throws SPIException {
	    if (aNodeName == null) { return; }
        PolicyNodeImpl childNode = getChildNode(aNodeName);
        removeNode(childNode);
    }

    /**
     * Removes a child node.
     *
     * @param aNode    node to be removed
     * @throws	       <code>SPIException</code>
     *			if error occurs
     */
    public void removeNode(PolicyNodeImpl aNode)
			throws SPIException {
	    if (aNode == null) { return; }
	    /* if the set is marked readonly, then the
	       operation cannot proceed */
	    checkIfReadOnly();
	    /* if the node or its children are readonly
	       or mandatory then the node cannot be removed */
	    aNode.checkIfNodeOrChildrenReadOnlyOrMandatory();
	    /* If the node was added at a parent layer, then
	        need to write this node to the update layer
	        with "op=remove" attribute set */
	    if (!aNode.isAddedAtTopLayer()) {
            /* set the operation attribute to "remove" */
            aNode.setOperationType(OperationType.OP_REMOVE);
            addRemovedChild((PolicyNodeImpl)aNode);
	    }
        deleteChildNode(aNode.getName());
        mHasBeenModified = true;
    }

    /**
     * Removes a property.
     *
     * @param aPropertyName    name of property to be removed
     * @throws	               <code>SPIException</code>
     *			               if error occurs
     */
    public void removeProperty(String aPropertyName)
			throws SPIException {
	    if (aPropertyName == null) { return; }
        PropertyImpl property = (PropertyImpl)getProperty(aPropertyName);
        removeProperty(property);
    }

    /**
     * Removes a property.
     *
     * @param aProperty    property to be removed
     * @throws	           <code>SPIException</code>
     *			            if error occurs
     */
    public void removeProperty(PropertyImpl aProperty)
			throws SPIException {
	    if (aProperty == null) { return; }
	    /* if the set is marked readonly, then the
	       operation cannot proceed */
	    checkIfReadOnly();
	    /* if the node or its children are readonly
	       or mandatory then the node cannot be removed */
	    aProperty.checkIfReadOnly();
	    aProperty.checkIfMandatory();
	    /* If the node was added at a parent layer, then
	        need to write this node to the update layer
	        with "op=remove" attribute set */
	    if (!aProperty.isAddedAtTopLayer()) {
            /* set the operation attribute to "remove" */
            aProperty.setOperationType(OperationType.OP_REMOVE);
            addRemovedChild(aProperty);
	    }
        deleteProperty(aProperty);
        mHasBeenModified = true;
    }

    /**
     * Sets the default node, that is the node as it was in the
     * default layer. 
     *
     * @param aDefaultNode   the default node
     * @throws 		     <code>SPIException</code> if error
     *			     occurs
     */
    public void setDefaultNode(ReadWritePolicyNodeImpl aDefaultNode) 
				throws SPIException {
	    mDefaultNode = aDefaultNode;
    }

    /**
     * Sets the node and its children to default.
     *
     * @param aDefaultNode  the default for the node on which
     *			    the setDefault() method was called
     * @throws   	    <code>SPIException</code> if error occurs
     */
    private void setNodeAndChildrenToDefault(
		ReadWritePolicyNodeImpl aDefaultNode) throws SPIException {
        if (mChildNodeTable != null) {
            Enumeration childNodes = mChildNodeTable.elements();
            while (childNodes.hasMoreElements()) {
                ReadWritePolicyNodeImpl childNode = 
                    (ReadWritePolicyNodeImpl)childNodes.nextElement();
		        ReadWritePolicyNodeImpl defaultChildNode = 
			      childNode.mDefaultNode;
		        if (defaultChildNode != null) {
	                childNode.setNodeAndChildrenToDefault(
			           defaultChildNode);
		             aDefaultNode.addChildNode(defaultChildNode);
	            }
            }
        }
        if (mPropertyTable != null) {
            Enumeration properties = mPropertyTable.elements();
            while (properties.hasMoreElements()) {
                ReadWritePropertyImpl property = 
                    (ReadWritePropertyImpl)properties.nextElement();
		        ReadWritePropertyImpl defaultProperty = 
			      property.getDefaultProperty();
		        if (defaultProperty != null) {
		             aDefaultNode.addProperty(defaultProperty);
	            }
            }
        }
	    if (mRemovedChildren != null) {
		    for (int i = 0; i < mRemovedChildren.size(); i++) {
                Object removedChild = mRemovedChildren.get(i);
                if (removedChild instanceof PolicyNodeImpl) {
		            PolicyNodeImpl defaultRemovedChildNode =
			            ((ReadWritePolicyNodeImpl)removedChild).getDefaultNode();
		            if (defaultRemovedChildNode != null) {
	                    aDefaultNode.addChildNode(defaultRemovedChildNode);
		            }
                } else {
                    ReadWritePropertyImpl defaultRemovedProperty = 
                        ((ReadWritePropertyImpl)removedChild).getDefaultProperty();
                    if (defaultRemovedProperty != null) {
                        aDefaultNode.addProperty(defaultRemovedProperty);
		            }   
	            }
	        }
	    }   
        aDefaultNode.setDefaultNode((ReadWritePolicyNodeImpl)aDefaultNode.shallowCopy());
    }

    /**
     * Sets the value of the finalized property of the node,
     * and its children. 
     *
     * @param aIsProtected  <code>true</code> if the node 
     *					    is finalized, <code>false</code> 
     *					    otherwise
     * @throws              <code>SPIException</code> if operation 
     *                      is not permitted
     */
    public void setProtected(boolean aIsProtected) 
                        throws SPIException {
        setProtected(aIsProtected, getAbsolutePath(),
                	 mPolicyTree.getPolicy());
    }

    /**
     * Sets the value of the finalized property of the node,
     * and its children. 
     *
     * @param aIsProtected  <code>true</code> if the node 
     *					    is finalized, <code>false</code> 
     *					    otherwise
     * @param aNameOfElementWhereProtectionSet  name of the node where 
     *                                      the flag was set 
     *                                      (null if aIsProtected is 
     *				                        <code>false</code>
     * @param aOriginOfProtection       layer where the flag was set 
     *                                 (null if aIsProtected is 
     *                                  <code>false</code>
     * @throws              <code>SPIException</code> if operation 
     *                      is not permitted
     */
    public void setProtected(boolean aIsProtected, 
							 String aNameOfElementWhereProtectionSet,
							 Policy aOriginOfProtection) 
    	throws SPIException {
        checkIfNodeOrChildrenReadOnly();
        mIsProtected = aIsProtected ;
	    mNameOfElementWhereProtectionSet = aNameOfElementWhereProtectionSet;
	    mOriginOfProtection = aOriginOfProtection;
        mHasBeenModified = true;
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.setFinalized(aIsProtected,
                                aNameOfElementWhereProtectionSet,
                                aOriginOfProtection);
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.setFinalized(aIsProtected,
                                aNameOfElementWhereProtectionSet,
                                aOriginOfProtection);
                    }
                }

	        }
	    }
    }

    /**
      * Sets the node name. 
      */
    public void setName(String aName)  {
        mHasBeenModified = true;
        mName = aName;
    }
    
    /**
     * Returns a shallow copy of the node (excludes child nodes).
     *
     * @return      copy of the node
     * @throws      <code>SPIException</code> if cannot 
     *		    create copy
     */
    public ConfigElementImpl shallowCopy() throws SPIException {
        ConfigElementImpl retNode = super.shallowCopy();
        ((ReadWritePolicyNodeImpl)retNode).mDefaultNode = mDefaultNode;
        return retNode;
    }

    /**
     * Carries out the update merge modification operation specified in
     * this result node.
     *
     * @param aOutputNode              the equivalent node for the new
     * 				       update layer
     * @param aLayer		       layer to update
     * @param aUpdateNodePath	       the path to this result node (used
     *				       for exception messages
     * @return                         <code>true</code> if this node 
     *				       is required for the new update layer, 
     *				       otherwise <code>false</code>
     * @throws			       <code>SPIException</code> if 
     *				       error occurs     
     */
    public boolean updateModifyNode(PolicyNodeImpl aOutputNode, 
            						Policy aLayer, 
            						String aUpdateNodePath) 
		throws SPIException{
	    boolean update = false;
        /* should be included if the protect function was 
	        applied directly to this node. */
	    if (mIsProtected) {
	        if (getParent() == null || !
                    ((PolicyNodeImpl)getParent()).isProtected()) { 
	            update = true;
	        }
        } else if (mIsMandatory && 
		    aLayer.equals(mOriginOfMandatory) ) {
	        update = true;
	    }
        
	    /* if this layer removed any inherited dynamic members,
	        then add these nodes (with op=remove) to the
	        output child node */
	    if (mRemovedChildren != null) { 
	        for (int i = 0; i < mRemovedChildren.size(); i ++) {
	            Object removedChild =
			        mRemovedChildren.get(i);
	            if (removedChild != null) {
                    if (removedChild instanceof PolicyNodeImpl) {
	                    aOutputNode.addChildNode(
			                (PolicyNodeImpl)((PolicyNodeImpl)removedChild).shallowCopy());
	                    update = true;
                    } else if (removedChild instanceof PropertyImpl) {
	                    aOutputNode.addProperty(
			                (PropertyImpl)((PropertyImpl)removedChild).shallowCopy());
	                    update = true;
                    }
		        }
	        }
	    }
	    boolean childrenUpdate =
		    processUpdateNodeChildren(aOutputNode, 
			aLayer, aUpdateNodePath);
	    return update || childrenUpdate;
    }

    /**
     * Carries out the update merge remove operation specified in
     * this result node.
     *
     * @param aOutputNode     the equivalent node for the new
     *                        update layer          
     * @param aLayer		  layer to be updated
     * @param aUpdateNodePat  the path to this result node (used
     *                        for exception messages
     * @return                <code>true</code> if this node is 
     *					      required for the new update layer, 
     *					      otherwise <code>false</code>
     * @throws                <code>SPIException</code> if 
     *                        error occurs     
     */
    public boolean updateRemoveNode(PolicyNodeImpl aOutputNode, 
            						Policy aLayer, 
            						String aUpdateNodePath) 
    	throws SPIException {
	    PolicyNodeImpl parentNode = (PolicyNodeImpl)getParent();
	    if (!mIsAddedAtTopLayer) {
	       parentNode.addRemovedChild(this);
	    }
	    // delete the node from the cached node
        parentNode.deleteChildNode(mName);
	    return mIsAddedAtTopLayer ? false : true;
    }


    /**
     * Carries out the update merge replacement operation specified in
     * this result node.
     *
     * @param aOutputNode               the equivalent node for the new
     *                                  update layer          
     * @param aLayer			layer to be updated
     * @param aUpdateNodePath           the path to this result node (used
     *                                  for exception messages
     * @return                          <code>true</code> if this node 
     *                                  is required for the new update layer, 
     *				        otherwise <code>false</code>
     * @throws                          <code>SPIException</code> if 
     *                                  error occurs     
     */
    public boolean updateReplaceNode(PolicyNodeImpl aOutputNode, 
            						 Policy aLayer,
            						 String aUpdateNodePath) 
		throws SPIException {
	    boolean update = false;
	    boolean childrenUpdate = false;
	    if (mIsAddedAtTopLayer) {
	        update = true;
	    } else if (mIsProtected) { 
	        if (getParent() == null || 
                    !((PolicyNodeImpl)getParent()).isProtected()) {
                /* should be included if the protect or setMandatory
	                function was applied directly to this node. */
	            update = true;
	        }
	    } else if (mIsMandatory && 
		    mOriginOfMandatory.equals(aLayer)) {
	        update = true;
	    }
	    /* if this layer removed any inherited set members,
	        then add these nodes (with op=remove) to the
	        output child node */
	    if (mRemovedChildren != null) {
	        for (int i = 0; i < mRemovedChildren.size(); i++) {
	            Object removedChild =
			            mRemovedChildren.get(i);
	            if (removedChild != null) {
                    if (removedChild instanceof PolicyNodeImpl) {
	                    aOutputNode.addChildNode(
			                (PolicyNodeImpl)((PolicyNodeImpl)removedChild).shallowCopy());
		                update = true;
                     } else if (removedChild instanceof PropertyImpl) {
	                    aOutputNode.addProperty(
			               (PropertyImpl)((PropertyImpl)removedChild).shallowCopy());
		                update = true;
                     }
		        }
	        }
	    }
	    childrenUpdate = processUpdateNodeChildren(
				aOutputNode, aLayer, aUpdateNodePath);
   	    return update || childrenUpdate;
    }

    /**
     * Carries out the update merge reset operation specified in
     * this result node.
     *
     * @param aOutputNode              the equivalent node for the new
     *                                 update layer          
     * @param aLayer		       layer to be updated
     * @param aUpdateNodePath          the path to this result node (used
     *                                 for exception messages
     * @return                         <code>true</code> if this node is 
     *				       required for the new update layer, 
     *				       otherwise <code>false</code>
     * @throws                         <code>SPIException</code> if 
     *				       error occurs     
     */
    public boolean updateResetNode(PolicyNodeImpl aOutputNode, 
            					   Policy aLayer,
            					   String aUpdateNodePath) 
    	throws SPIException {
	     /* a node with the operation attribute reset will 
	        have been "rolled back" to the default node. However,
	        it may have nested operations */
	    boolean update = false;
	    if (mIsProtected) {
	        if (getParent() == null || 
                    !((PolicyNodeImpl)getParent()).isProtected()) {
                /* should be included if the protect or setMandatory
	            function was applied directly to this node. */
	            update = true;
	        }
	    } else if (mIsMandatory && 
		    mOriginOfMandatory.equals(aLayer)) {
	        update = true;
	    }
        boolean childrenUpdate =
		processUpdateNodeChildren(aOutputNode, aLayer, 
			aUpdateNodePath);
	    if (!update && !childrenUpdate) {
            return false;
	    } else {
	        /* Change the operation attribute to "modify" */ 
	        aOutputNode.setOperationType(OperationType.OP_MODIFY);
            return true;
	    }
    }

}
