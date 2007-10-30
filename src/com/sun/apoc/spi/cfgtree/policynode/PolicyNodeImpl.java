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

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.ConfigElementImpl;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.NodeParsing;
import com.sun.apoc.spi.cfgtree.OperationType;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.ProtectedElementImpl;
import com.sun.apoc.spi.cfgtree.XMLStreamable;
import com.sun.apoc.spi.cfgtree.property.Property;
import com.sun.apoc.spi.cfgtree.property.PropertyImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Abstract class for a policy node.
  *
  */
public abstract class PolicyNodeImpl extends ProtectedElementImpl 
	implements PolicyNode, MergedPolicyNode, XMLStreamable {

    /** list of children names */
    public Vector mAllChildrenNames  ;
    /** hashtable for child nodes */
    public Hashtable mChildNodeTable ;
    /** hashtable for child properties */
    public Hashtable mPropertyTable ;
    /** vector of child nodes removed during final layer read */
    public Vector mRemovedChildren ;
    /** the layer where the node originated */
    protected Policy mOriginLayer;
    /** the name of the element, including path, where the finalized
        attribute was set to true */
    protected String mNameOfElementWhereProtectionSet;
    /** the layer where the finalized attribute was set to true */
    protected Policy mOriginOfProtection;
    /** Module name for exceptions */
    private static final String MODULE = "PolicyNodeImpl";


    /**
      * Adds a new child node to the node and 
      * sets the child node's parent.
      *
      * @param aChild   child node to add
      */
    public void addChildNode(PolicyNodeImpl aChild) {
	    /* need to keep a list of all children in order as they 
	       appear in the parent node, so use a vector for this. But
	       also need to be able to access a child node by its name, so
	        maintaing a Hashtable in conjunction with the list. */
	    if (aChild == null) { return; }
        if (mAllChildrenNames == null) { mAllChildrenNames = new Vector() ; }
        if (mChildNodeTable == null) { mChildNodeTable = new Hashtable() ; }
	    /* need to check if updating an existing child node, 
           or adding a new child node */
	    PolicyNodeImpl originalNode = 
            (PolicyNodeImpl)mChildNodeTable.get(
					aChild.getName());
	    if (originalNode != null) {
	        int index = mAllChildrenNames.indexOf(originalNode.getName());
	        if (index > -1) {
		        mAllChildrenNames.setElementAt(aChild.getName(), index);
	        } else {
		        mAllChildrenNames.add(aChild.getName());
	        }
	    } else {
	        mAllChildrenNames.add(aChild.getName());
	    }
        mChildNodeTable.put(aChild.getName(), aChild) ;
        aChild.setParent(this) ;
    }

    /**
      * Adds a new node.
      *
      * @param aName    name of the node to be added
      * @return         the newly added <code>PolicyNodeImpl</code>
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract PolicyNode addNode(String aName) throws SPIException ;

    /**
      * Adds a new <code>Property</code>.
      *
      * @param aName    name of the property to be added
      * @return         the newly added <code>Property</code>
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract Property addProperty(String aName) throws SPIException;


    /**
      * Adds a new property to the node and 
      * sets the property's parent.
      *
      * @param aProperty   property to add
      */
    public void addProperty(PropertyImpl aProperty) {
	    /* need to keep a list of all children in order as they 
	       appear in the parent node, so use a vector for this. But
	       also need to be able to access a child property by its name, so
	        maintaing a Hashtable in conjunction with the list. */
	    if (aProperty == null) { return; }
        if (mAllChildrenNames == null) { mAllChildrenNames = new Vector() ; }
        if (mPropertyTable == null) { mPropertyTable = new Hashtable() ; }
	    /* need to check if updating an existing child property, 
           or adding a new child property */
	    PropertyImpl originalProperty = 
            (PropertyImpl)mPropertyTable.get(
					aProperty.getName());
	    if (originalProperty != null) {
	        int index = mAllChildrenNames.indexOf(originalProperty.getName());
	        if (index > -1) {
		        mAllChildrenNames.setElementAt(aProperty.getName(), index);
	        } else {
		        mAllChildrenNames.add(aProperty.getName());
	        }
	    } else {
	        mAllChildrenNames.add(aProperty.getName());
	    }
        mPropertyTable.put(aProperty.getName(), aProperty) ;
        aProperty.setParent(this) ;
    }


    /**
     * Adds a new removed child to the node.
     *
     * @param aChild	removed child to add
     */
    public void addRemovedChild(Object aRemovedChild) {
	if (aRemovedChild == null) { return; }
	    if (mRemovedChildren == null) { 
            mRemovedChildren = new Vector() ; 
        }
	    mRemovedChildren.add(aRemovedChild);
    }


    /**
     * Traverses a node and changes the finalized settings to 
     * readonly. This is a utility function used in reading 
     * data from the default layers.
     */
    public void changeProtectedNodesToReadOnly() {
	    if (isProtected()) { 
	        mIsReadOnly = true;
	        mIsProtected = false;
	    }
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.changeProtectedNodesToReadOnly();
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.setReadOnly();
                    }
                }

	        }
        }
    }

    /**
     * Used when doing an edit to check if a node or any of its
     * children are readonly, and if so an exception is thrown. 
     *
     * @throws         <code>SPIException</code> if node 
     *		       or its children are readonly
     */ 
    public void checkIfNodeOrChildrenReadOnly() 
        throws SPIException {
	    checkIfReadOnly();
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.checkIfNodeOrChildrenReadOnly();
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.checkIfReadOnly();
                    }
                }

	        }
        }
    }

    /**
     * Used when doing an edit to check if any of its
     * properties are readonly, and if so an exception is thrown. 
     *
     * @throws         <code>SPIException</code> if node 
     *		       or its children are readonly
     */ 
    public void checkIfPropertiesReadOnly() 
        throws SPIException {
	    if (mAllChildrenNames != null && mPropertyTable != null &&
                !mPropertyTable.isEmpty()) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                if (property != null) {
                    property.checkIfReadOnly();
                }

	        }
        }
    }

    /**
     * Used prior to a remove operation to check if a node or any of its
     * children are readonly or mandatory, and if so an exception is thrown. 
     *
     * @throws         <code>SPIException</code> if node or its
     *		       children are readonly or mandatory
     */ 
    public void checkIfNodeOrChildrenReadOnlyOrMandatory() 
		throws SPIException {
	    checkIfReadOnly();
	    checkIfMandatory();
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.checkIfNodeOrChildrenReadOnlyOrMandatory();
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.checkIfReadOnly();
                        property.checkIfMandatory();
                    }
                }

	        }
        }
    }


    /**
      * Clears the properties. 
      *
      * @throws        <code>SPIException</code> if error occurs
      */
    public abstract void clearProperties() throws SPIException ;

    /**
     * Utility function for creating a copy of a 
     * <code>Vector</code> of nodes. 
     *
     * @param aOriginalList    the <code>Vector</code> to be copied
     * @return                  new <code>Vector</code>
     * @throws			        <code>SPIException</code> if
     *				            error occurs
     */
    public Vector copyNodeVector(Vector aOriginalList) 
				throws SPIException {
        Vector newList = new Vector();
	    for (int i = 0; i < aOriginalList.size(); i ++) {
	        PolicyNodeImpl newNode =    
                (PolicyNodeImpl)aOriginalList.get(i);
	        if (newNode != null) {
		        newList.add(newNode.deepCopy());
	        }
	    }
	    return newList;
    }


    /**
     * Returns a deep copy of the node (including child 
     * nodes for hierarchical nodes). 
     *
     * @return      copy of the node
     * @throws      <code>SPIException</code> if cannot 
     *		    create copy
     */
    public PolicyNodeImpl deepCopy() throws SPIException {
	    PolicyNodeImpl returnNode = (PolicyNodeImpl)shallowCopy();
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                if (name != null) {
                    PolicyNodeImpl childNode = null;
                    if (mChildNodeTable != null) {
                        childNode = (PolicyNodeImpl)mChildNodeTable.get(name);
                        if (childNode != null) {
	                        returnNode.addChildNode(childNode.deepCopy());
                        }
                    }
                    if (childNode == null && mPropertyTable != null) {
                        PropertyImpl property =
                            (PropertyImpl)mPropertyTable.get(name);
                        if (property != null) {
                            returnNode.addProperty(
                                (PropertyImpl)property.shallowCopy());
                        }
                    }
                }
            }
        }
        return returnNode;
    }

    /**
     * Deletes this node.
     *
     * @throws	       <code>SPIException</code>
     *			if error occurs
     */
    public void delete() throws SPIException {
	    /* if the set is marked readonly, then the
	        operation cannot proceed */
	    checkIfReadOnly();
	    /* if the node or its children are readonly
	        or mandatory then the node cannot be removed */
	    checkIfNodeOrChildrenReadOnlyOrMandatory();
        /* set the operation attribute to "remove" */
        setOperationType(OperationType.OP_REMOVE);
    }

    /**
     * Utility function for deleting a child node.
     *
     * @param aChildName       name of node to be deleted
     */
    public void deleteChildNode(String aChildName) {
	    PolicyNodeImpl childNode = null;
	    if (mChildNodeTable != null) {
	        childNode = (PolicyNodeImpl)mChildNodeTable.get(aChildName);
	        if (childNode != null) {
	            mChildNodeTable.remove(aChildName);
	        }
	    }
	    if (childNode != null && mAllChildrenNames != null) {
	        mAllChildrenNames.remove(aChildName);
	    }
    }

    /**
     * Utility function for deleting a property.
     *
     * @param aProperty        property to be deleted
     */
    public void deleteProperty(PropertyImpl aProperty) {
        if (aProperty == null) { return; }
	    if (mPropertyTable != null) {
	        mPropertyTable.remove(aProperty.getName());
	    }
	    if (mAllChildrenNames != null) {
	        mAllChildrenNames.remove(aProperty.getName());
	    }
    }

    /**
     * Expands the base source layer node.
     *
     * @param aNodePath	    the name of this node, including path
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *                            layer (needed for handling
     *				              finalized attribute): <code>true</code>
     *				              if final layer, otherwise <code>false</code> 
     * @throws		    <code>SPIException</code> if
     *                      error occurs
     */
    public void expand(String aNodePath, boolean aIsParentUpdateLayer)
 			throws SPIException {
	    mPath = aNodePath;    
	    if (isProtected()) {
	        if (getParent() == null ||
                       !getParent().isProtected()){
	            setFinalized(true, getAbsolutePath(),
	                    	 getOriginOfProtection());
	        }
	        if (aIsParentUpdateLayer) {
		        setReadOnly();
            }
	    }
        if (mPropertyTable != null) {
            Enumeration properties = mPropertyTable.elements();
            while (properties.hasMoreElements()) {
                PropertyImpl property = (PropertyImpl)properties.nextElement();
                String propertyPath = appendToPath(aNodePath, property.getName());
                property.expand(propertyPath, aIsParentUpdateLayer);
             }
        }
	    if (mChildNodeTable != null) {
            Enumeration nodes = mChildNodeTable.elements();
            while (nodes.hasMoreElements()) {
                PolicyNodeImpl childNode = (PolicyNodeImpl)nodes.nextElement();
		        String childNodePath = appendToPath(aNodePath, childNode.getName());
                childNode.expand(childNodePath, aIsParentUpdateLayer);
            }
        }
    }


    /**
      * Returns the list of names for all the children. 
      *
      * @return   the list of child names 
      */
    public Vector getAllChildrenNames() { return mAllChildrenNames; }

   /**
    * Returns a string representing the attributes of
    * the node.
    *
    * @param aFormat    schema type
    * @return		attribute string
    */
    public String getAttributes(int aFormat) {
	    StringBuffer retCode = new StringBuffer(PolicyTree.BUFFER_SIZE);
	    retCode.append(NodeParsing.NAME_ATTR).append("=\"").append(mName);
        retCode.append("\"");
	    if (getPackage() != null) {
            retCode.append(" ").append(NodeParsing.PACKAGE_ATTR).append(
		    "=\"").append(getPackage()).append("\"") ;
	    }
	    String operationProp = OperationType.getString(getOperationType());
	    switch (aFormat) {
	        case UPDATE_SCHEMA:
	        case POLICY_SCHEMA:
	            if (isProtected()) {
	                if (getParent() == null ||
			            !((PolicyNodeImpl)getParent()).isProtected()) {
	                    retCode.append(" ").append(
		                     NodeParsing.FINALIZED_ATTR).append(
			                    "=\"true\"") ;
		            }
		        }
		        if (aFormat == UPDATE_SCHEMA && isMandatory())  {
		            if ((getOriginOfMandatory()).equals(
		                    	getPolicyTree().getPolicy())) {
                        retCode.append(" ");
			            retCode.append(NodeParsing.MANDATORY_ATTR);
		                retCode.append("=\"true\"") ;
                    }
                }
		        if (aFormat == POLICY_SCHEMA && isMandatory())  {
                    retCode.append(" ");
		            retCode.append(NodeParsing.MANDATORY_ATTR);
		            retCode.append("=\"true\"") ;
	            }
		        if (operationProp != null) {
		            switch(OperationType.getInt(operationProp)) {
		  	            case OperationType.OP_REPLACE:
			            case OperationType.OP_REMOVE:
          	                retCode.append(" ").append(
				            NodeParsing.OPERATION_ATTR).append(
				            "=\"").append(operationProp).append("\"");
                            break;
                    };
                }
	            break;
	        case MERGED_SCHEMA:
		        if (isReadOnly()) {
                     retCode.append(" ").append(
			        NodeParsing.READONLY_ATTR).append("=\"true\"") ;
		        } else if (isProtected()) {
                     retCode.append(" ").append(
			        NodeParsing.FINALIZED_ATTR).append("=\"true\"") ;
                }
	            if (isMandatory())	 {
		            retCode.append(" ").append(
			            NodeParsing.MANDATORY_ATTR).append(
				        "=\"true\"") ;
                }
		    if (operationProp != null ) {
               	retCode.append(" ").append(
		        NodeParsing.OPERATION_ATTR).append("=\"").append(
		        operationProp).append("\"");
		    }
		    break;
	    };
	    return retCode.toString();
    }


    /**
     * Returns a node child designated by its name.
     *
     * @param aName    name of the child
     * @return         child if it exists, null otherwise
     */
    public PolicyNodeImpl getChild(String aName) {
	    if (aName == null || mAllChildrenNames == null ||
			 mChildNodeTable == null) { return null ; }
	    return (PolicyNodeImpl) mChildNodeTable.get(aName) ;
    }

    /**
     * Returns an <code>Iterator</code> listing
     * the policy node children.
     *
     * @return      iterator on the children list
     */
    public Iterator getChildNodes() {
        if (mChildNodeTable == null ||
            mChildNodeTable.isEmpty()) {
            return new Vector().iterator() ;
        }
        Vector nodeList = new Vector();
        for (int i = 0; i < mAllChildrenNames.size(); ++i) {
            String name = (String)mAllChildrenNames.get(i);
            PolicyNodeImpl policyNode = 
                (PolicyNodeImpl)mChildNodeTable.get(name);
            if (policyNode != null) {
                nodeList.add(policyNode);
            }
        }
        return nodeList.iterator() ;
    }

    /**
     * Returns the requested child node, or null if
     * it doesn't exist.
     *
     * @param      aChildName
     * @return     child node 
     */
    public PolicyNodeImpl getChildNode(String aChildName) {
        if (mChildNodeTable == null ||
            mChildNodeTable.isEmpty()) {
            return null;
        }
	    return (PolicyNodeImpl)mChildNodeTable.get(aChildName);
    }

    /**
      * Returns the table of child nodes.
      *
      * @return    the table of child nodes
      */
    public Hashtable getChildNodeTable() { return mChildNodeTable; }


    /**
      * Returns list of child node names. 
      *
      * @return    array of child node names
      */
    public String[] getChildrenNames() {
        if (mChildNodeTable == null ||
                mChildNodeTable.isEmpty()) {
            return new String[0];
        }
        String[] retCode = new String[mChildNodeTable.size()];
        Enumeration names = mChildNodeTable.keys();
        int index = 0;
        while (names.hasMoreElements()) {
            retCode[index++] = (String)names.nextElement();
        }
        return retCode;
    }

    /**
     * Gets the name of the element (including path) where the finalized
     * attribute was set.
     *
     * @return   name of the element, including path
     */
    public String getNameOfElementWhereProtectionSet() {
	 return mNameOfElementWhereProtectionSet;
    }

    /**
     * Returns the policy where the element
     * originated.  
     *
     * @return	  the policy where the element originated
     */
    public Policy getOrigin() { return mOriginLayer; }

    /**
     * Gets the policy where the finalized attribute
     * was set. 
     *
     * @return   the policy where the attribute was set	
     */
    public Policy getOriginOfProtection() {
	    return mOriginOfProtection;
    }

    /**
     * Returns an <code>Iterator</code> listing
     * the property children.
     *
     * @return      iterator of child properties 
     */
    public Iterator getProperties() {
        if (mPropertyTable == null ||
            mPropertyTable.isEmpty()) {
            return new Vector().iterator() ;
        }
        Vector propertyList = new Vector();
        for (int i = 0; i < mAllChildrenNames.size(); ++i) {
            String name = (String)mAllChildrenNames.get(i);
            PropertyImpl property = 
                (PropertyImpl)mPropertyTable.get(name);
            if (property != null) {
                propertyList.add(property);
            }
        }
        return propertyList.iterator() ;
    }

    /**
     * Returns the requested property, or null if
     * it doesn't exist.
     *
     * @param      aPropertyName
     * @return     property 
     */
    public Property getProperty(String aPropertyName) {
        if (mPropertyTable == null ||
            mPropertyTable.isEmpty()) {
            return null;
        }
	    return (PropertyImpl)mPropertyTable.get(aPropertyName);
    }


    /**
      * Returns list of property names. 
      *
      * @return    array of property names
      */
    public String[] getPropertyNames() {
        if (mPropertyTable == null ||
                mPropertyTable.isEmpty()) {
            return new String[0];
        }
        String[] retCode = new String[mPropertyTable.size()];
        Enumeration names = mPropertyTable.keys();
        int index = 0;
        while (names.hasMoreElements()) {
            retCode[index++] = (String)names.nextElement();
        }
        return retCode;
    }

    /**
      * Returns the table of properties.
      *
      * @return    the table of properties
      */
    public Hashtable getPropertyTable() { return mPropertyTable; }


    /**
     * Prints the children of the node.
     * 
     * @param aIndent  indent prefix for the children
     * @param aOutput  output stream
     * @param aFormat  schema type
     */
    public void printChildren(String aIndent, 
		PrintStream aOutput, int aFormat) {
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        childNode.printToStream(
                       aIndent, aOutput, aFormat);
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        property.printToStream(
                            aIndent, aOutput, aFormat);
                    }
                }

	        }
        }
    }

    public static final String XML_HEADER = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" ; 
    private static final String COMPONENT_NAMESPACE = 
	" xmlns:oor=\"http://openoffice.org/2001/registry\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";
    /**
     * Prints the tag of the node (opening or closing).
     *
     * @param aIndent  indent prefix for the tag

    public static final String XML_HEADER = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" ; 
    private static final String COMPONENT_NAMESPACE = 
	" xmlns:oor=\"http://openoffice.org/2001/registry\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";
    /**
     * Prints the tag of the node (opening or closing).
     *
     * @param aIndent  indent prefix for the tag
     * @param aOutput  output stream
     * @param aOpening true if opening tag, false otherwise
     * @param aFormat  schema type
     */
    public void printTag(String aIndent, PrintStream aOutput,
                            boolean aOpening, int aFormat) {
        if (aOpening && getParent() == null) {  
	        aOutput.print(XML_HEADER) ; 
	    }
	    aOutput.print(aIndent + (aOpening ? "<" : "</")) ;
        switch(aFormat) {
            case UPDATE_SCHEMA:
	        case POLICY_SCHEMA:
		        /* if this is the component node, then
                   the tag is "<oor:component-data" */
		        if (getParent() == null) {
                    aOutput.print(NodeParsing.OOR_NAMESPACE);
                    aOutput.print(NodeParsing.COMPONENT_DATA_TAG) ;
	                if (aOpening) { 
                        aOutput.print(COMPONENT_NAMESPACE);     
                    }
		        } else {
                    aOutput.print(NodeParsing.NODE_TAG) ;
		        }
                break;
            case MERGED_SCHEMA:
		        aOutput.print(NodeParsing.NODE_TAG) ;
		        break;
	    }
	    if (aOpening) {
		    aOutput.print(" " + getAttributes(aFormat)) ;
	    }
	    aOutput.print(">\n") ;
    }

    /**
     * Outputs the node's contents to a PrintStream.
     *
     * @param aIndent  indent prefix for the printing
     * @param aOutput  stream to print to
     * @param aFormat  schema type
     */
    public void printToStream(String aIndent, 
		PrintStream aOutput, int aFormat) {
        printTag(aIndent, aOutput, true, aFormat) ;
        printChildren(aIndent + PolicyTree.TAB, aOutput, aFormat) ;
        printTag(aIndent, aOutput, false, aFormat) ;
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
    public abstract void readModifyNode(PolicyNodeImpl aResultNode,
	    NodeKey aUpdateNodeKey, String aUpdateNodePath,
	    boolean aIsParentUpdateLayer) throws SPIException ;

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
    public abstract void readRemoveNode(PolicyNodeImpl aResultNode,
		String aUpdateNodePath, boolean aIsParentUpdateLayer)
                                        throws SPIException ;

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
    public abstract void readReplaceNode(PolicyNodeImpl aResultNode, 
	    NodeKey aUpdateNodeKey, String aUpdateNodePath, 
	    boolean aIsParentUpdateLayer) throws SPIException ;

    /**
      * Removes a node.
      *
      * @param aName    name of the node to be removed
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void removeNode(String aName) throws SPIException;


    /**
      * Removes a <code>Property</code>.
      *
      * @param aName    name of the property to be removed
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void removeProperty(String aName) throws SPIException;


    /**
      * Adds a new node with attribute "op=replace".
      *
      * @param aName      name of the node to be replaced
      * @return           the newly replaced <code>PolicyNodeImpl</code>
      * @throws           <code>SPIException</code> if error occurs
      */
    public abstract PolicyNode addReplaceNode(String aName) 
        throws SPIException;


    /**
     * Set the finalized attribute for the property. 
     *
     * @param aSetting    <code>true</code> or <code>false</code>
     * @param aNameOfElementWhereProtectionSet
     * @param aOriginOfProtection
     */
    public void setFinalized (boolean aSetting, 
            String aNameOfElementWhereProtectionSet, 
            Policy aOriginOfProtection)  {
        super.setFinalized(aSetting);
        mNameOfElementWhereProtectionSet = aNameOfElementWhereProtectionSet;
        mOriginOfProtection = aOriginOfProtection;
        if (mChildNodeTable != null) {
            Enumeration nodes = mChildNodeTable.elements();
            while (nodes.hasMoreElements()) {
                PolicyNodeImpl node = (PolicyNodeImpl)nodes.nextElement();
                node.setFinalized(aSetting,
                        aNameOfElementWhereProtectionSet,
                        aOriginOfProtection);
            }
        }
        if (mPropertyTable != null) {
            Enumeration properties = mPropertyTable.elements();
            while (properties.hasMoreElements()) {
                PropertyImpl property = (PropertyImpl)properties.nextElement();
                property.setFinalized(aSetting,
                        aNameOfElementWhereProtectionSet,
                        aOriginOfProtection);
            }
        }
    }

    /**
     * Sets the layer where the node originated. 
     *
     * @param aOriginLayer   id of the layer where the node originated
     */
    public void setOrigin (Policy aOriginLayer) {
	    mOriginLayer = aOriginLayer;
    }

    /** 
     * Sets the readonly flag of the node and its children to true, 
     */
    public void setReadOnly() {
	    if (mIsProtected) {
	        mIsReadOnly = true;
	        mIsProtected = false;
	    }
        if (mChildNodeTable != null) {
            Enumeration nodes = mChildNodeTable.elements();
            while (nodes.hasMoreElements()) {
                PolicyNodeImpl node = (PolicyNodeImpl)nodes.nextElement();
                node.setReadOnly();
            }
        }
        if (mPropertyTable != null) {
            Enumeration properties = mPropertyTable.elements();
            while (properties.hasMoreElements()) {
                PropertyImpl property = (PropertyImpl)properties.nextElement();
                property.setReadOnly();
            }
        }
    }
     
    /**       
     * Sets the path, originating layer and dynamic
     * settings for this node and its children. 
     * 
     * @param aPath          path name for this node 
     * @param aOriginLayer   layer where node originated
     * @param aIsParentLayer <code>true</code> true if
     *			     data sourced from parent layer,
     *			     otherwise <code>false</code> 
     */
    public void setSettingsForAddedNode(String aPath,
		Policy aOriginLayer, 
		boolean aIsParentLayer ) throws SPIException {      
	    setPath(aPath);
	    if (aOriginLayer != null) { setOrigin(aOriginLayer); }
	    if (!aIsParentLayer) {
	        setAddedAtTopLayer();
	    }
        String pathName = null;
	    if (mAllChildrenNames != null) {
	        for (int i = 0; i < mAllChildrenNames.size(); i++) {
                String name = (String)mAllChildrenNames.get(i);
                PolicyNodeImpl childNode =  null;
                if (mChildNodeTable != null) {
                    childNode = 
                        (PolicyNodeImpl)mChildNodeTable.get(name);
                    if (childNode != null) {
                        pathName = appendToPath(aPath, childNode.getName());
                        childNode.setSettingsForAddedNode(pathName, 
		                    aOriginLayer, aIsParentLayer);
                    }
                }
                if (childNode == null && mPropertyTable != null) {
                    PropertyImpl property =
                        (PropertyImpl)mPropertyTable.get(name);
                    if (property != null) {
                        pathName = appendToPath(aPath, property.getName());
                        property.setSettingsForAddedProperty(pathName, 
		                    aOriginLayer, aIsParentLayer);
                    }
                }

	        }
        }
    }

    /**
     * Returns a shallow copy of the node (excludes child nodes).
     *
     * @return      copy of the node
     * @throws      <code>SPIException</code> if cannot 
     *		    create copy
     */
    public ConfigElementImpl shallowCopy() throws SPIException {
	    ConfigElementImpl returnNode = super.shallowCopy();
	    if (mRemovedChildren != null) {
            ((PolicyNodeImpl)returnNode).mRemovedChildren =
             copyNodeVector(mRemovedChildren);
        }
        ((PolicyNodeImpl)returnNode).mNameOfElementWhereProtectionSet =
            mNameOfElementWhereProtectionSet;
        ((PolicyNodeImpl)returnNode).mOriginOfProtection =
            mOriginOfProtection;
        ((PolicyNodeImpl)returnNode).mOriginLayer =
            mOriginLayer;
        return returnNode;
    }

    /**
     * Determines from this update node which operation is to
     * be carried out during the read merge, and then invokes 
     * the corresponding operation method.
     *
     * @param aResultNode	  node that will be the result of
     *				  the read merge process
     * @param aUpdateNodeKey      information on update node 
     * @param aUpdateNodePath     the path to this update node (used
     *				  for exception messages)
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *                            layer (needed for handling
     *				  finalized attribute): <code>true</code>
     *				  if final layer, otherwise <code>false</code> 
     * @throws			  <code>SPIException</code> if error occurs
     * @throws InvalidPolicyNodeException if node is invalid
     */
     public void processReadOperation(PolicyNodeImpl aResultNode, 
	        NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		boolean aIsParentUpdateLayer) throws SPIException {
        /* Check the validity of the configuration node */
	     if (mName == null ) {
	         throw new InvalidPolicyNodeException();
	    }
         /* determine the operation to be carried out */
    	switch (mOperationType) {
	        case OperationType.OP_REPLACE:
 		        readReplaceNode(aResultNode, aUpdateNodeKey, 
		            aUpdateNodePath, aIsParentUpdateLayer);
                break;
	        case OperationType.OP_REMOVE:
		        readRemoveNode(aResultNode, aUpdateNodePath, 
			    aIsParentUpdateLayer);
                break;
	        case OperationType.OP_UNKNOWN: //defaults to OP_MODIFY
	        case OperationType.OP_MODIFY:
		        readModifyNode(aResultNode, aUpdateNodeKey, 
			    aUpdateNodePath, aIsParentUpdateLayer);
                break;
	    };
    }
     
    /**
     * Helper method for creating config node paths. 
     * 
     * At the moment we use the slash '/' to separate the elements of config node 
     * paths (e.g. 'com.sun.apoc/node1/node2'). However, node names might also 
     * include a slash. To correctly identify nodes within a path, we require
     * that node names with inluded slashes must be encoded/escaped (via 
     * URLEncoder). For example, a node with name 'text/html' might appear as
     * 'com.sun.apoc/node1/text%2Fhtml' within a path.
     *
     * The helper method takes care of the neccessary encoding (if required)
     * before appending the node name to the path.
     *
     * @param path        An existing node path (or empty string)
     * @param newElement  Typically the name of a node that should be appended
     *                    to the existing path.
     */ 
    public static String appendToPath(String path, String newElement)
            throws SPIException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(path);
        buffer.append(PolicyTree.PATH_SEPARATOR);
        try {
            // endcoding a string is a somewhat expensive operation. For 
            // performance reasons we therefore check, if encoding is required
            // at all.
            if (newElement.indexOf('/') != -1) {
                buffer.append(URLEncoder.encode(newElement, "UTF-8"));
            } else {
                buffer.append(newElement);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new SPIException(ex);
        }
        return buffer.toString();
    }     
}
