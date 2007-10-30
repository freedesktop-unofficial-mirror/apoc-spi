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

package com.sun.apoc.spi.cfgtree.property;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.ConfigElementImpl;
import com.sun.apoc.spi.cfgtree.DataType;
import com.sun.apoc.spi.cfgtree.InvalidDataTypeException;
import com.sun.apoc.spi.cfgtree.MandatoryElementException;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.NodeValueImpl;
import com.sun.apoc.spi.cfgtree.OperationType;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.policynode.ReadWritePolicyNodeImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Class for a property.
  *
  */
public class ReadWritePropertyImpl extends PropertyImpl {
    /** Default property */
    private ReadWritePropertyImpl mDefaultProperty ;
    private boolean mHasBeenModified = false;

    /**
     * Creates and adds a default <code>NodeValueImpl</code>.
     * 
     * @param aOrigin   originating layer
     */
    public void addDefaultNodeValueImpl(Policy aOrigin) {
	    if (mValues == null) { mValues = new Hashtable(); }
	    NodeValueImpl defaultValue = createDefaultNodeValueImpl();
	    defaultValue.setOrigin(aOrigin);
        mValues.put(mRequiredLocale, defaultValue);
    }			

    /**
     * Clears the settings added at this layer.
     *
     * @throws   <code>SPIException</code> if 
     *           error occurs
     */
    public void clear() throws SPIException {
	    /* cannot apply reset operation if property
	       is readonly */ 
       	checkIfReadOnly();
	    ReadWritePropertyImpl defaultProperty = mDefaultProperty;
	    PolicyNodeImpl parent = (PolicyNodeImpl)getParent();
	    if (defaultProperty != null) {
	        setDefaultProperty(defaultProperty);
	        defaultProperty.setOperationType(OperationType.OP_RESET);
            /* replace the property in the policy tree with
	           the default property. */
	        if (parent.isProtected()) {
	            defaultProperty.setFinalized(true, 
	                    			getNameOfElementWhereProtectionSet(),
	                    			getOriginOfProtection());
                parent.addProperty(defaultProperty);
	       } 
	    } else {
	       /* we are dealing with a property added at this layer */
	        checkIfMandatory(); 
	       setOperationType(OperationType.OP_REMOVE);
	    }
        mHasBeenModified = true;
    }

    /**
     * Utility function for copying updated <code>NodeValueImpls</code>
     * into an existing table of <code>NodeValueImpls</code>.
     *
     * @param aUpdateTable	    the table to be copied
     * @param aPropertyImpl	    the property containing the table
     *   			            to be updated
     * @param aLayer            layer where values originated
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *				            layer (needed for handling
     *				            finalized attribute): <code>true</code>
     *				            if final layer, otherwise <code>false</code>
     * @throws			        <code>SPIException</code> if
     *				            error occurs
     */
     public void copyUpdateNodeValueImpls(Hashtable aUpdateTable,
		PropertyImpl aPropertyImpl, Policy aLayer,
		boolean aIsParentUpdateLayer) throws SPIException {
        Enumeration keys = aUpdateTable.keys();
	    if (keys != null) {
	        while (keys.hasMoreElements()) {
		        String key = (String)keys.nextElement();
		        NodeValueImpl updatedNodeValueImpl = 
			    (NodeValueImpl)aUpdateTable.get(key);
		        if (updatedNodeValueImpl != null) {
		            /* if the property node is not nillable, then 
		               ensure that don't copy a nil NodeValueImpl */
		            if (!aPropertyImpl.isNillable() && 
			            updatedNodeValueImpl.hasNilAttribute()) {
			            /* skip this value */
	                } else {
		                NodeValueImpl newNodeValueImpl = 
				        updatedNodeValueImpl.copyNodeValueImpl();
		                newNodeValueImpl.setOrigin(aLayer);
	 	                aPropertyImpl.setNodeValue(newNodeValueImpl, key);
		                newNodeValueImpl.setPropertyImpl(aPropertyImpl);
		                if (!aIsParentUpdateLayer) {
		                    newNodeValueImpl.setModifiedAtTopLayer();
		                }
		            }
		        }
            }
	    }
    }

    /**
     * Deletes this property if it is dynamic.
     *
     * @throws                <code>SPIException</code>
     *                        if error occurs
     */
     public void delete () throws SPIException {
        /* if this property is marked readonly then it cannot
           be removed */
        checkIfReadOnly();
        /* only properties that have been added in the current
           layer may be removed */
        if (! ((PropertyImpl)this).isAddedAtTopLayer()) {
            throw new MandatoryElementException(
                    "Property", getName(), 
                    MandatoryElementException.REMOVE_MANDATORY_KEY);
         }
         /* set the operation attribute to "remove" */
        setOperationType(OperationType.OP_REMOVE);
        mHasBeenModified = true;
    }

    /**
     * Returns the default property, that is the property as it 
     * was in the default layer. 
     *
     * @return    the default property
     * @throws    <code>SPIException</code> if error occurs
     */
    public ReadWritePropertyImpl getDefaultProperty() 
        throws SPIException {
	    return ( (mDefaultProperty != null) ? 
                (ReadWritePropertyImpl)mDefaultProperty.shallowCopy() : null);
    }

    /**
      * Returns a boolean indicating if the property has
      * been modified. 
      *
      * @return    <code>true</code> if the property has been
      *            modified, otherwise <code>false</code>
      */
    public boolean hasBeenModified() {
        return mHasBeenModified;
    }


    /**
     * Determines from this update operation which operation is to
     * be carried out during the read merge, and then invokes
     * the corresponding operation method.
     *
     * @param aOutputProperty   the equivalent property for the new
     *				            update layer
     * @param aLayer			layer to update
     * @param aUpdateNodePath	        the path to this update node (used
     *				        for exception messages
     * @return			        <code>true</code> if this node is 
     *                                  required for the new update layer,
     *				        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> 
     *                                  if error occurs     
     */
    public boolean processUpdateOperation(PropertyImpl aOutputProperty,
            							  Policy aLayer, 
            							  String aUpdateNodePath) 
		throws SPIException {   
	    boolean update = false;
	    switch (mOperationType) {
            case OperationType.OP_REPLACE:
		        update = updateReplaceProperty(aOutputProperty, aLayer,
				aUpdateNodePath);
		        break;
            case OperationType.OP_REMOVE:
		        update = updateRemoveProperty(aOutputProperty, aLayer, 
					aUpdateNodePath);
		        break;
            case OperationType.OP_RESET:
		        update = updateResetProperty(aOutputProperty, aLayer,
					aUpdateNodePath);
		        break;
            case OperationType.OP_UNKNOWN: //defaults to OP_MODIFY
            case OperationType.OP_MODIFY:
                update = updateModifyProperty(aOutputProperty, aLayer,
					aUpdateNodePath);
       		    break;
    	    };
	    return update;
    }

    private String[] getArrayFromListValue(String aValue) {
        StringTokenizer st = new StringTokenizer(aValue, mSeparator);
        String [] retCode = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            retCode [i++] = st.nextToken();
        }
        return retCode;
    }
    
    private String getStringFromArray(String[] aValues) {
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        return sBuf.toString();
    }
    
    /**
      * Sets the value.
      * 
      * @param aValue      the value
      * @param aDataType   the type of the data
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void put(String aValue, DataType aDataType) throws SPIException {
        if (aValue == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        if (mDataType == DataType.UNKNOWN) {
            mDataType = aDataType;
        } else if (mDataType != aDataType) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        getNodeValue().setNewContents(aValue);
        mHasBeenModified = true;
    }

    /**
      * Sets the string value.
      * 
      * @param aValue   the string value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putString(String aValue) throws SPIException{
        put(aValue, DataType.STRING);
    }

    /**
      * Sets the hexbinary value as a string.
      * 
      * @param aValue   the hexbinary value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putHexBinary(String aValue) throws SPIException{
        put(aValue, DataType.HEXBIN);
    }

    /**
      * Sets the int value.
      * 
      * @param aValue   the int value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putInt(int aValue) throws SPIException{
        put(Integer.toString(aValue), DataType.INT);
    }

    /**
      * Sets the double value.
      * 
      * @param aValue   the double value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putDouble(double aValue) throws SPIException{
        put(Double.toString(aValue), DataType.DOUBLE);
    }

    /**
      * Sets the short value.
      * 
      * @param aValue   the short value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putShort(short aValue) throws SPIException{
        put(Short.toString(aValue), DataType.SHORT);
    }


    /**
      * Sets the long value.
      * 
      * @param aValue   the long value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putLong(long aValue) throws SPIException{
        put(Long.toString(aValue), DataType.LONG);
    }

    /**
      * Sets the boolean value.
      * 
      * @param aValue   the boolean value
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putBoolean(boolean aValue) throws SPIException{
        put(Boolean.toString(aValue), DataType.BOOLEAN);
    }

    /**
      * Sets the string list values.
      * 
      * @param aValues   the list of string values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putStringList(String[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        put(getStringFromArray(aValues), DataType.STRING_LIST);
    }

    /**
      * Sets the hexbinary list values.    
      * 
      * @param aValues   the list of hexbinary values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public void putHexBinaryList(String[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        put(getStringFromArray(aValues), DataType.HEXBIN_LIST);
    }

    /**
      * Sets the list of int value.
      * 
      * @param aValues   the list of int values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putIntList(int[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        put(sBuf.toString(), DataType.INT_LIST);
    }

    /**
      * Sets the list of double values.
      * 
      * @param aValue   the list of double values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putDoubleList(double[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        put(sBuf.toString(), DataType.DOUBLE_LIST);
    }

    /**
      * Sets the list of short values.
      * 
      * @param aValue   the list of short values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putShortList(short[] aValues) throws SPIException {
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        put(sBuf.toString(), DataType.SHORT_LIST);
    }

    /**
      * Sets the list of long values.
      * 
      * @param aValue   the list of long values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putLongList(long []aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        put(sBuf.toString(), DataType.LONG_LIST);
    }

    /**
      * Sets the list of boolean values.
      * 
      * @param aValue   the list of boolean values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putBooleanList(String[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        put(getStringFromArray(aValues), DataType.BOOLEAN_LIST);
    }

    /**
      * Sets the list of boolean values.
      * 
      * @param aValue   the list of boolean values
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public void putBooleanList(boolean[] aValues) throws SPIException{
        if (aValues == null || aValues.length == 0) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        StringBuffer sBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        for (int i = 0; i < aValues.length - 1; ++i) {
            sBuf.append(aValues[i]);
            sBuf.append(mSeparator);
        }
        sBuf.append(aValues[aValues.length - 1]);
        put(sBuf.toString(), DataType.BOOLEAN_LIST);
    }

    /**
     * Carries out the read modification operation specified in
     * this update node.
     *
     * @param aResultNode         node that will be the result of
     *                            the merge process
     * @param aUpdateNodeKey      information on update node
     * @param aUpdateNodePath     the path to this update node (used
     *                            for exception messages)
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *                            layer (needed for handling
     *                            finalized attribute): <code>true</code>
     *                            if final layer, otherwise <code>false</code>
     * @throws                    <code>SPIException</code> if error occurs
     */
    public void readModifyProperty(PolicyNodeImpl aResultNode, 
	        NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		    boolean aIsParentUpdateLayer) throws SPIException {
        /* If the property exists in the source layer and is marked
           readonly then this operation is ignored  */
        PropertyImpl resultNode = 
            (PropertyImpl)aResultNode.getProperty(getName());
        if (resultNode != null && resultNode.isReadOnly()) { return; }

        PropertyImpl newProperty;
	    if (resultNode == null) {
	        newProperty = this;
	        if (!aIsParentUpdateLayer) {
	            newProperty.setAddedAtTopLayer();
	        }
	    } else {
	        newProperty = (PropertyImpl)resultNode.shallowCopy();
            /* If the update node has empty tags then the new PropertyImpl
               will keep the values of the source layer. Otherwise, the 
               new Node copies the updated values of the update layer */
	        if (mValues != null && !mValues.isEmpty()) {
	            copyUpdateNodeValueImpls(mValues, newProperty,
		        aUpdateNodeKey.mLayer, aIsParentUpdateLayer);
	        }
	    }
	    newProperty.setPath(PolicyNodeImpl.appendToPath(aResultNode.getAbsolutePath(), getName()));
        /* Check if this update property is finalized. If it is, and this
	       is a parent update layer being processed, then set the 
	       readonly attribute to true for this node. If it is
	        finalized and this is the source layer or this is not a
	        parent layer, then set the finalized attribute to true */
	    if (isProtected()) {
	        newProperty.setFinalized(true, newProperty.getAbsolutePath(),
	                				 getOriginOfProtection());
	        if (aIsParentUpdateLayer) { 
	            newProperty.setReadOnly();
	        }
	    }
	    /* set the operation property to reflect that this node
	        was modified */
	    newProperty.setOperationType(OperationType.OP_MODIFY);
        /* add the node to aResultNode */
	    aResultNode.addProperty(newProperty);
    }

    /**
     * Carries out the replacement operation specified in
     * this update node.
     *
     * @param aResultNode	        node that will be the result of
     *				                the merge process
     * @param aUpdateNodePath	    the path to this update property (used
     *				                for exception messages)
     * @param aUpdateNodeKey        information on update property
     * @param aIsParentUpdateLayer  indicates if this is a parent update
     *				                layer (needed for handling
     *				                finalized attribute): <code>true</code>
     *				                if final layer, otherwise <code>false</code>
     * @throws			            <code>SPIException</code> if 
     *				                error occurs	
     */
    public void readReplaceProperty(PolicyNodeImpl aResultNode, 
		NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		boolean aIsParentUpdateLayer)
				throws SPIException {
	    /* if property element exists in source layer then
	       it cannot be replaced, so the operation is ignored */
	    if (aResultNode.getChild(getName()) != null) {
	        return;
	    } else {
            /* Check if the update node is finalized. If it is, and it is 
               a parent update layer being processed, then set the readonly 
	           attribute to true. If it is a default layer, or not a parent 
	           layer, then set the finalized attribute to true */
	        if (isProtected()) {
	            String name = PolicyNodeImpl.appendToPath(
                            aResultNode.getAbsolutePath(),
                            getName());
	            setFinalized(true, name, getOriginOfProtection());
	            if (aIsParentUpdateLayer) {
	                setReadOnly();
	            }
	        }
	        /* if the update node is empty, then add a default NodeValueImpl */
	        if (mValues == null || mValues.isEmpty()) {
	            addDefaultNodeValueImpl(getOrigin());
	        }
	        if (aIsParentUpdateLayer) {
		        /* a node inserted at a parent layer is
                   mandatory to subsequent layers */
		        setMandatoryFlag();
		        setOriginOfMandatory(getOrigin());
	        }
	        /* if this is the entity layer being read, then
               set the dynamic flag to indicate that the property
	           was added on the current layer */
	        if (!aIsParentUpdateLayer) {
	            setAddedAtTopLayer();
	        }
	        setPath(PolicyNodeImpl.appendToPath(aResultNode.getAbsolutePath(), getName()));
	        aResultNode.addProperty(this);
	    }
    }

    /**
     * Sets the default property, that is the property as it was in the
     * default layer. 
     *
     * @param aDefaultProperty   the default property
     * @throws 		            <code>SPIException</code> if error
     *			               occurs
     */
    public void setDefaultProperty(
            ReadWritePropertyImpl aDefaultProperty) throws SPIException {
	    mDefaultProperty = aDefaultProperty;
    }

    /**
     * Sets the property value to Nil.
     *
     * @throws             <code>SPIException</code>
     */
    public void setNil() throws SPIException {
        getNodeValue().setValueToNil();
        mHasBeenModified = true;
    }

    /**
     * Sets the value of the finalized property of the property.
     *
     * @param aIsProtected  <code>true</code> if the property 
     *					    is finalized, <code>false</code> 
     *					    otherwise
     *				            if aIsProtected is <code>false</code>
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
     * @param aNameOfNodeWhereProtectionSet  name of the node where 
     *                                      the flag was set 
     *                                      (null if aIsProtected is 
     *				                        <code>false</code>
     * @param aOriginOfProtection       layer where the flag was set 
     *                                 (null if aIsProtected is 
     *                                  <code>false</code>
     */
    public void setProtected(boolean aIsProtected, 
			String aNameOfNodeWhereProtectionSet, 
			Policy aOriginOfProtection) 
                throws SPIException {
        checkIfReadOnly();
        mIsProtected = aIsProtected ;
	    mNameOfElementWhereProtectionSet = aNameOfNodeWhereProtectionSet;
	    mOriginOfProtection = aOriginOfProtection;
        mHasBeenModified = true;
    }

    

    /**
     * Sets the property to its default.
     *
     * @param aDefaultProperty  the default for the property on which
     *			                the setDefault() method was called
     * @throws   	            <code>SPIException</code> if 
     *                          error occurs
     */
    private void setPropertyToDefault(
		ReadWritePropertyImpl aDefaultProperty) 
        throws SPIException {
        aDefaultProperty.setDefaultProperty(
                (ReadWritePropertyImpl)aDefaultProperty.shallowCopy());
    }


    /**
     * Sets the separator used by the property's values.
     *
     * @param aSeparator   separator
     */
    public void setSeparator(String aSeparator) {
        if (mSeparator != null) {
            mSeparator = aSeparator;
	    }
        mHasBeenModified = true;
    }


    /**
     * Returns a shallow copy of the property.
     *
     * @return	    copy of the property
     * @throws      <code>SPIException</code> if cannot 
     *              create copy
     */
    public ConfigElementImpl shallowCopy() throws SPIException {
	    ConfigElementImpl retCode = super.shallowCopy();
        ((ReadWritePropertyImpl)retCode).mDefaultProperty = mDefaultProperty;
        return retCode;
    }

    /**
     * Carries out the update merge modification operation specified in
     * this result property.
     *
     * @param aOutputProperty    the equivalent node for the new
     *				             update layer
     * @param aLayer			 layer to be updated
     * @param aUpdateNodePath	 the path to this result node (used
     *				             for exception messages
     * @return			        <code>true</code> if this node is 
     *					        required for the new update layer, 
     *					        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> 
     *					        if error occurs
     */
    public boolean updateModifyProperty(PropertyImpl aOutputProperty, 
            							Policy aLayer, 
            							String aUpdateNodePath)
    	throws SPIException{
	    boolean update = false;
	    if (isProtected()) {
	        if (getParent() == null || !getParent().isProtected()) { 
                /* should be included if the protect or setMandatory
	           	   function was applied directly to this node. */
		        update = true;
	        }
	    } else if (isMandatory() &&
		    (getOriginOfMandatory()).equals(aLayer)) {
            update = true;
        } else if (mValues != null) {
            /* if the property was added at this layer
               then all the values should be written */
            if (isAddedAtTopLayer()) {
                update = true;
            } else {
	            /* go through each NodeValueImpl and see if it was
	                modified at this layer */
	            Enumeration keys = mValues.keys();
	            if (keys != null) {
		            Hashtable updateValues = null;
		            while (keys.hasMoreElements()) {
		                String key = (String)keys.nextElement();
		                NodeValueImpl value = (NodeValueImpl)getNodeValue(key);
		                if (value != null) {
		                    if (value.isModifiedAtTopLayer()) {
			                    update = true;
			                    if (updateValues == null) {
				                    updateValues = new Hashtable();
				                    aOutputProperty.mValues = 
					                    updateValues;
			                    }
			                    updateValues.put(key, value);
			                }
	                    }
		            }    
	            }
	        }
        }
	    if (!update) {
	        ((ReadWritePolicyNodeImpl)aOutputProperty.getParent()).removeProperty(
			        getName());
	    }
	    return update;
    }



    /**
     * Carries out the update merge remove operation specified in
     * this result node.
     *
     * @param aOutputProperty                the equivalent node for the new
     *                                   update layer
     * @param aLayer                     layer to be updated
     * @param aUpdateNodePath            the path to this result node (used
     *                                   for exception messages
     * @return                           <code>true</code> if this node is
     *                                   required for the new update layer,
     *                                   otherwise <code>false</code>
     * @throws                           <code>SPIException</code> if
     *                                   error occurs
     */
    public boolean updateRemoveProperty(PropertyImpl aOutputProperty,
            							Policy aLayer,
            							String aUpdateNodePath) 
    	throws SPIException {
        ReadWritePolicyNodeImpl parentNode = 
            (ReadWritePolicyNodeImpl)getParent();
        // delete the node 
        parentNode.removeProperty(getName());
        return false;
    }

    /**
     * Carries out the update merge replacement operation specified in
     * this result property.
     *
     * @param aOutputProperty	    the equivalent property for the new
     *				            update layer
     * @param aLayer			layer to be updated
     * @param aUpdateNodePat    the path to this result node (used
     *				            for exception messages
     * @return			        <code>true</code> if this node is 
     * 					        required for the new update layer, 
     *					        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> if 
     *					        error occurs
     */
    public boolean updateReplaceProperty(PropertyImpl aOutputProperty, 
            							 Policy aLayer,
            							 String aUpdateNodePath) 
        throws SPIException {
	    boolean update = false;
	    if (isProtected()) {
	        if (getParent() == null || !getParent().isProtected()) { 
                /* should be included if the protect or setMandatory
		           function was applied directly to this node. */
		        update = true;
	        }
	    } else if (isMandatory() &&
		        (getOriginOfMandatory()).equals(aLayer)) {
            update = true;
	    } else if (mValues != null) {
            /* if the property was added at this layer
               then all the values should be written */
            if (isAddedAtTopLayer()) {
                update = true;
            } else {
	            /* go through each NodeValueImpl and see if it was
		            modified at this layer */
                Enumeration keys = mValues.keys();
                if (keys != null) {
		            Hashtable updateValues = null;
		            while (keys.hasMoreElements()) {
                        String key = (String)keys.nextElement();
                        NodeValueImpl value = (NodeValueImpl)getNodeValue(key);
		                if (value != null) {
		                    if (value.isModifiedAtTopLayer()) {
			                    update = true;
			                    if (updateValues == null) {
				                    updateValues = new Hashtable();
				                    ((PropertyImpl)aOutputProperty).mValues = 
					                updateValues;
			                    }
			                    updateValues.put(key, value);
			                }
                        }
                    }    
                }
            }
	    }
	    if (update) {
           /* set the operation attribute to "modify" */
	        setOperationType(OperationType.OP_MODIFY);
	        aOutputProperty.setOperationType(OperationType.OP_MODIFY);
	    } else {
	        ((ReadWritePolicyNodeImpl)aOutputProperty.getParent()).removeProperty(
			            getName());
	    }
        return update;
    }
  
    /**
     * Carries out the update merge reset operation specified in
     * this result node.
     *
     * @param aOutputNode       the equivalent property for the new
     *				            update layer
     * @param aLayer			layer to be updated
     * @param aUpdateNodePath   the path to this result node (used
     *				            for exception messages
     * @return			        <code>true</code> if this node is 
     *					        required for the new update layer, 
     *					        otherwise <code>false</code>
     * @throws			        <code>SPIException</code> if 
     *                          error occurs
     */
    public boolean updateResetProperty(PropertyImpl aOutputNode, 
            						   Policy aLayer, 
            						   String aUpdateNodePath) 
    	throws SPIException {
        boolean update = false;
	    if (isProtected()) {  
	        if (getParent() == null || !getParent().isProtected()) {
                /* it should be included if the protect function was
		           applied directly to this node. */
                update = true;
	        }
        } else if (isMandatory() && getOriginOfMandatory() != null &&
                aLayer.equals(getOriginOfMandatory())) {
            update = true;
	    } else if (mValues != null) {
            /* go through each NodeValueImpl and see if it was
		       modified at this layer */
            Enumeration keys = mValues.keys();
            if (keys != null) {
		        Hashtable updateValues = null;
	    	    while (keys.hasMoreElements()) {
                    String key = (String)keys.nextElement();
                    NodeValueImpl value = (NodeValueImpl)getNodeValue(key);
		            if (value != null) {
		                if (value.isModifiedAtTopLayer()) {
			                 update = true;
			                if (updateValues == null) {
				                updateValues = new Hashtable();
				                aOutputNode.mValues = updateValues;
			                }
			                updateValues.put(key, value);
			            }
                    }
                } 
            }
	    }
        if (update) {
           /* set the operation attribute to "modify" */
	        aOutputNode.setOperationType(OperationType.OP_MODIFY);
	    }
        return update;
    }

}
