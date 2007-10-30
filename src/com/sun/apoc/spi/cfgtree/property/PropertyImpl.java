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

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.ConfigElementImpl;
import com.sun.apoc.spi.cfgtree.DataType;
import com.sun.apoc.spi.cfgtree.InvalidDataTypeException;
import com.sun.apoc.spi.cfgtree.NodeKey;
import com.sun.apoc.spi.cfgtree.NodeParsing;
import com.sun.apoc.spi.cfgtree.NodeValueImpl;
import com.sun.apoc.spi.cfgtree.OperationType;
import com.sun.apoc.spi.cfgtree.PolicyTree;
import com.sun.apoc.spi.cfgtree.ProtectedElementImpl;
import com.sun.apoc.spi.cfgtree.XMLStreamable;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Abstract class for a property.
  *
  */
public abstract class PropertyImpl extends ProtectedElementImpl implements Property, MergedProperty, XMLStreamable {

    /** Value type */
    public DataType mDataType = DataType.UNKNOWN ;
    /** boolean indicating if property is localized */
    public boolean mLocalized = false;
    /** boolean indicating if property may assume value nil */
    public boolean mNillable = true;
    /** locale  */
    public String mRequiredLocale = PolicyTree.DEFAULT_LOCALE_NAME;
    /** the separator used */
    public String mSeparator = NodeParsing.WHITESPACE;
    /** the layer where the property originated */
    protected Policy mOriginLayer;
    /** the name of the element, including path, where the finalized
        attribute was set to true */
    protected String mNameOfElementWhereProtectionSet;
    /** the layer where the finalized attribute was set to true */
    protected Policy mOriginOfProtection;
    /** Node value table */
    public Hashtable mValues ;


    /**
     * Utility function for creating a copy of a table of 
     * <code>NodeValueImpls</code>.
     *
     * @param aOriginalTable	the table to be copied
     * @param aPropertyImpl     the property to which the new table belongs
     * @return			        new table
     * @throws			<code>SPIException</code> if
     *				error occurs
     */
    public Hashtable copyNodeValueImplTable(Hashtable aOriginalTable,
		       PropertyImpl aPropertyImpl) throws SPIException {
	    Hashtable newTable = new Hashtable();
	    Enumeration keys = aOriginalTable.keys();
	    if (keys != null) {
            while (keys.hasMoreElements()) {
		        String key = (String)keys.nextElement();
		        NodeValueImpl origNodeValueImpl = (NodeValueImpl)aOriginalTable.get(key);
		        if (origNodeValueImpl != null) {
		            NodeValueImpl newNodeValueImpl = origNodeValueImpl.copyNodeValueImpl();
		            newNodeValueImpl.setPropertyImpl(aPropertyImpl);
		            newTable.put(key, newNodeValueImpl);
		        }
            }
	    }
        return newTable;
    }

    /**
     * Creates a new default <code>NodeValueImpl</code>.
     * 
     * @return   the new <code>NodeValueImpl</code>
     */
    public NodeValueImpl createDefaultNodeValueImpl() {
	    NodeValueImpl defaultValue = new NodeValueImpl();
	    defaultValue.setPropertyImpl(this);
	    defaultValue.setNilAttribute(true);
        defaultValue.setDataType(mDataType);
        return defaultValue;
    }			       

    /**
     * Expands the base source layer node.
     *
     * @param aPath	    the name of this property, including path
     * @param aIsParentUpdateLayer indicates if this is a parent update
     *                            layer (needed for handling
     *				              finalized attribute): <code>true</code>
     *				              if final layer, otherwise <code>false</code> 
     * @throws		    <code>SPIException</code> if
     *                      error occurs
     */
    public void expand(String aPath, boolean aIsParentUpdateLayer)
 			throws SPIException {
	    mPath = aPath;    
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
    }

    /**
     * Returns a string representing the attributes of
     * the node.
     *
     * @param aFormat	format
     * @return		attribute string
     */
    public String getAttributes(int aFormat) {
	    StringBuffer retCode = new StringBuffer(PolicyTree.BUFFER_SIZE);
	    retCode.append(NodeParsing.NAME_ATTR).append(
		    "=\"").append(getName()).append("\"") ;
	    String operationProp =
		    OperationType.getString(getOperationType());
	    if (operationProp != null &&
		    operationProp.equals(OperationType.REPLACE_STR)) {
           	retCode.append(" ").append(
		    NodeParsing.OPERATION_ATTR).append("=\"").append(
	            operationProp).append("\"");
	    }
	    switch (aFormat) {
	        case UPDATE_SCHEMA:
	        case POLICY_SCHEMA:
		        if (isProtected()) {
		            if (getParent() == null ||
				        !getParent().isProtected()) {
		                retCode.append(" ").append(
		 	            NodeParsing.FINALIZED_ATTR).append(
				            "=\"true\"") ;
		            }
		        }
	            if (isLocalized()) {
                    retCode.append(" ").append(
			        NodeParsing.LOCALIZED_ATTR).append("=\"true\"") ;
                }
		        if (!mNillable) {
                    retCode.append(" ").append(
			        NodeParsing.NILLABLE_ATTR).append("=\"false\"") ;
                }
		        if (mDataType != DataType.UNKNOWN) {
              	    retCode.append(" ").append(
		            NodeParsing.TYPE_ATTR).append("=\"").append(
		            mDataType.getStringValue()).append("\"");
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
	        if (isLocalized()) {
                    retCode.append(" ").append(
			NodeParsing.LOCALIZED_ATTR).append("=\"true\"") ;
                }
		if (!isNillable()) {
               	   retCode.append(" ").append(
			NodeParsing.NILLABLE_ATTR).append("=\"false\"") ;
                }
		if (mDataType != DataType.UNKNOWN) {
           	    retCode.append(" ").append(
			NodeParsing.TYPE_ATTR).append("=\"").append(
			mDataType.getStringValue()).append("\"");
                }
               break;
	};
	return retCode.toString();
    }


    
    /**
      * Returns the data type.
      * 
      * @return   the data type
      */
    public DataType getDataType() {
        return mDataType;
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
     * Returns the node value for the locale as
     * as specified by the policy tree. If there is
     * not a node value for this locale, the function 
     * will return, in order of preference:
     * 1) the node value containing the default data;
     * or 2) the node value for "en-US";
     * or 3) another localized node value;
     * or 4) a newly created default node value.
     * 
     * @return              <code>NodeValue</code> 
     */
    public NodeValueImpl getNodeValue() {
	    NodeValueImpl nodeValue = null;
	    if (mValues != null) {
	        if (mLocalized) {
	           nodeValue = (NodeValueImpl)mValues.get(mRequiredLocale);
	        } else {
	            nodeValue = 
                   (NodeValueImpl)mValues.get(PolicyTree.DEFAULT_LOCALE_NAME);
	        }
	        /* if don't have the required NodeValueImpl, then provide
	           a default one */
	        if (nodeValue == null && mLocalized) {
	            nodeValue = (NodeValueImpl)mValues.get(PolicyTree.DEFAULT_LOCALE_NAME);
	            if (nodeValue != null) {
	  	            nodeValue = nodeValue.copyNodeValueImpl();
	            }
	        }
	        if (nodeValue == null && mLocalized) {
	            nodeValue = (NodeValueImpl)mValues.get("en-US");
	            if (nodeValue != null) {
		            nodeValue = nodeValue.copyNodeValueImpl();
	            }
	        }
	        if (nodeValue == null && mLocalized) {
	            Enumeration keys = mValues.keys();
	            if (keys != null) {
	     	        while(keys.hasMoreElements()) {
		                String key = (String)keys.nextElement();
		                if (key != null) { 
			                nodeValue = 
			                (NodeValueImpl)mValues.get(key);
			                if (nodeValue != null) {
			                    nodeValue = nodeValue.copyNodeValueImpl();
			                }
			                break;
		                }
		            }
	            }
	        }
	    }
	    if (nodeValue == null) {
	        /* create a default NodeValue */
	        nodeValue = createDefaultNodeValueImpl();
	    }
	    return nodeValue;
    }

    /**
     * Returns the node value for the specified locale, 
     * or null if it does not exist.
     *
     * @param  aLocale      the required locale
     * @return		    <code>NodeValueImpl</code> for
     * 			    the specified locale, or null
     *			    if it does not exist
     */
   public NodeValueImpl getNodeValue(String aLocale) {
	    if (mValues == null) { return null; }
	    return (NodeValueImpl)mValues.get(aLocale);
    }

    /**
     * Returns the profile where the element
     * originated.  
     *
     * @return	  the profile where the element originated
     */
    public Policy getOrigin() { return mOriginLayer; }

    /**
     * Gets the Policy where the finalized attribute
     * was set. 
     *
     * @return   the Policy where the attribute was set	
     */
    public Policy getOriginOfProtection() {
	    return mOriginOfProtection;
    }

    /**
      * Returns the <code>Policy</code> object where the 
      * value originated.
      *
      * @return    <code>Policy</code> where the value
      *            originated
      */
    public Policy getOriginOfValue() {
        return getNodeValue().getOrigin();
    }


    /**
     * Returns the required locale. 
     *
     * @return 	the locale specified by the policy tree. 
     *	        If the property is not localized, then null
     *          is returned.	
     */
    public String getRequiredLocale() {
        // If the property is not localized to begin with,
        // we don't care about the locale.
        return mLocalized ? mRequiredLocale : null ;
    }

    /**
      * Returns the separator.
      * 
      * @return         the separator
      */
    public String getSeparator()  {
        return mSeparator;
    }

    /**
      * Returns the value as a string.
      * 
      * @return   the value as a string
      * @throws   <code>SPIException</code> if error occurs
      */
    public String getValue() throws SPIException {
        String retCode = getNodeValue().getContents();
        return retCode;
    }

    /**
     * Returns the values hashtable.
     * 
     * @return              values hashtable
     */
    public Hashtable getValues() { 
	    return mValues;
    }
        

    /**
      * Returns the string value. 
      * 
      * @return   the string value 
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public String getStringValue() throws SPIException {
        if (mDataType != DataType.STRING) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        return  getNodeValue().getContents();
    }

    /**
      * Returns the hexBinary value as a string.
      * 
      * @return   the hexBinary value as a string
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public String getHexBinary() throws SPIException {
        if (mDataType != DataType.HEXBIN) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        return getNodeValue().getContents();
    }

    /**
      * Returns the value as an int.
      * 
      * @return   the value as an int
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
     public int getInt() throws SPIException {
        if (mDataType != DataType.INT) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String value = getNodeValue().getContents();
        if (value == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        int retCode = 0;
        try {
            retCode = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY,
                    nfe);
        }
        return retCode;
    }


    /**
      * Returns the value as a double.
      * 
      * @return   the value as a double
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public double getDouble() throws SPIException {
        if (mDataType != DataType.DOUBLE) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String value = getNodeValue().getContents();
        if (value == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        double retCode = 0;
        try {
            retCode = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY,
                    nfe);
        }
        return retCode;
    }

    /**
      * Returns the value as a short.
      * 
      * @return   the value as a short
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public short getShort() throws SPIException {
        if (mDataType != DataType.SHORT) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String value = getNodeValue().getContents();
        if (value == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        short retCode = 0;
        try {
            retCode = Short.parseShort(value);
        } catch (NumberFormatException nfe) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY,
                    nfe);
        }
        return retCode;
    }

    /**
      * Returns the value as a long.
      * 
      * @return   the value as a long
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public long getLong() throws SPIException{
        if (mDataType != DataType.LONG) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String value = getNodeValue().getContents();
        if (value == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        long retCode = 0;
        try {
            retCode = Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY,
                    nfe);
        }
        return retCode;
    }

    /**
      * Returns the value as a boolean.
      * 
      * @return   the value as a boolean
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public boolean getBoolean() throws SPIException {
        if (mDataType != DataType.BOOLEAN) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String value = getNodeValue().getContents();
        if (value == null) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        boolean retCode = false;
        if (value.equalsIgnoreCase("true")) {
            retCode = true;
        } else if (!value.equalsIgnoreCase("false")) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.INVALID_VALUE_KEY);
        }
        return retCode;
    }

    /**
      * Returns the values as a string array.
      * 
      * @return   the values as a string array
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      */
    public String[] getHexBinaryList() throws SPIException {
        if (mDataType != DataType.STRING_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        return getNodeValue().getValueArray();
    }

    /**
      * Returns the values as an array of ints.
      * 
      * @return   the value as an array of ints
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidElementException if invalid value
      */
    public int[] getIntList() throws SPIException {
        if (mDataType != DataType.INT_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String[] values = getNodeValue().getValueArray();
        if (values.length == 0) { return new int[0]; }
        int []retCode = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            try {
                retCode[i] = Integer.parseInt(values[i]);
            } catch (NumberFormatException nfe) {
                throw new InvalidPropertyException(
                        InvalidPropertyException.INVALID_VALUE_KEY,
                        nfe);
            }
        }
        return retCode;
    }

    /**
      * Returns the value as an array of doubles.
      * 
      * @return   the value as an array of doubles
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidElementException if invalid value
      */
    public double[] getDoubleList() throws SPIException {
        if (mDataType != DataType.DOUBLE_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String[] values = getNodeValue().getValueArray();
        if (values.length == 0) { return new double[0]; }
        double []retCode = new double[values.length];
        for (int i = 0; i < values.length; ++i) {
            try {
                retCode[i] = Double.parseDouble(values[i]);
            } catch (NumberFormatException nfe) {
                throw new InvalidPropertyException(
                        InvalidPropertyException.INVALID_VALUE_KEY,
                        nfe);
            }
        }
        return retCode;
    }

    /**
      * Returns the value as an array of shorts.
      * 
      * @return   the value as an array of shorts
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidElementException if invalid value
      */
    public short[] getShortList() throws SPIException {
        if (mDataType != DataType.SHORT_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String[] values = getNodeValue().getValueArray();
        if (values.length == 0) { return new short[0]; }
        short []retCode = new short[values.length];
        for (int i = 0; i < values.length; ++i) {
            try {
                retCode[i] = Short.parseShort(values[i]);
            } catch (NumberFormatException nfe) {
                throw new InvalidPropertyException(
                        InvalidPropertyException.INVALID_VALUE_KEY,
                        nfe);
            }
        }
        return retCode;
    }

    /**
      * Returns the value as an array of longs.
      * 
      * @return   the value as an array of longs
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidElementException if invalid value
      */
    public long[] getLongList() throws SPIException {
        if (mDataType != DataType.LONG_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String[] values = getNodeValue().getValueArray();
        if (values.length == 0) { return new long[0]; }
        long []retCode = new long[values.length];
        for (int i = 0; i < values.length; ++i) {
            try {
                retCode[i] = Long.parseLong(values[i]);
            } catch (NumberFormatException nfe) {
                throw new InvalidPropertyException(
                        InvalidPropertyException.INVALID_VALUE_KEY,
                        nfe);
            }
        }
        return retCode;
    }

    /**
      * Returns the value as an array of booleans.
      * 
      * @return   the value as an array of booleans
      * @throws SPIException if error occurs
      * @throws InvalidDataTypeException if invalid data type
      * @throws InvalidPropertyException if invalid value
      */
    public boolean[] getBooleanList() throws SPIException {
        if (mDataType != DataType.BOOLEAN_LIST) {
            throw new InvalidDataTypeException(
                    mDataType.getStringValue());
        }
        String[] values = getNodeValue().getValueArray();
        if (values.length == 0) { return new boolean[0]; }
        boolean []retCode = new boolean[values.length];
        for (int i = 0; i < values.length; ++i) {
            if (values[i].equalsIgnoreCase("true")) {
                retCode[i] = true;
            } else if (values[i].equalsIgnoreCase("false")) {
                retCode[i] = false;
            } else {
                throw new InvalidPropertyException(
                        InvalidPropertyException.INVALID_VALUE_KEY);
            }
        }
        return retCode;
    }

    /**
     * Returns the setting of the localized flag.
     *
     * @return		  <code>true</code> if property is
     *			      localized, otherwise <code>false</code>
     */
    public boolean isLocalized() {
	    return mLocalized;
    }


    /**
      * Returns a boolean indicating if the property is nil.
      * 
      * @return   <code>true</code> if nil, otherwise
      *           <code>false</code>
      */
    public boolean isNil() {
        return getNodeValue().hasNilAttribute();
    }

    /**
     * Returns the setting of the nillable flag, indicating
     * whether or not the property may assume the value nil.
     *
     * @return		  <code>true</code> if node is
     *			  nillable, otherwise <code>false</code>
     */
    public boolean isNillable() {
	    return mNillable;
    }

    /**
     * Prints the tag of the node (opening or closing).
     *
     * @param aIndent	indent prefix for the tag
     * @param aOutput	output stream
     * @param aOpening true if opening tag, false otherwise
     * @param aFormat  schema type
     */
    public void printTag(String aIndent, PrintStream aOutput,
                            boolean aOpening, int aFormat) {
	    aOutput.print(aIndent + (aOpening ? "<" : "</")) ;
        aOutput.print(NodeParsing.PROP_TAG) ;
        if (aOpening) {
	        aOutput.print(" " + getAttributes(aFormat)) ;
	    }
        aOutput.print(">\n") ;
    }

    /**
     * Outputs the property's contents to a PrintStream.
     *
     * @param aIndent	indent prefix for the printing
     * @param aOutput	stream to print to
     * @param aFormat  schema type
     */
    public void printToStream(String aIndent, 
		PrintStream aOutput, int aFormat) {
	    printTag(aIndent, aOutput, true, aFormat) ;
	    printValue(aIndent + PolicyTree.TAB, aOutput, aFormat) ;
	    printTag(aIndent, aOutput, false, aFormat) ;
    }


    /**
     * Prints the possible values.
     *
     * @param aIndent  indent prefix for the printing
     * @param aOutput  stream to print to
     * @param aFormat  schema type
     */
    public void printValue(String aIndent,
		PrintStream aOutput, int aFormat) {
	    if (mValues != null) {
            NodeValueImpl value = null;
	        Enumeration values = mValues.elements();
	        if (values != null) {
                while (values.hasMoreElements()) {
		            value = (NodeValueImpl)values.nextElement();
		            if (value != null) {
		                if (aFormat == XMLStreamable.UPDATE_SCHEMA) {
                            if (isAddedAtTopLayer() ||
				                value.isModifiedAtTopLayer()) {
			                    value.printToStream(aIndent, aOutput);
			                }
			            } else {
			                value.printToStream(aIndent, aOutput);
                        }
                    }
		        }
            }
	    }
    }

    /**
     * Determines from this update property which operation is to
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
     * @throws InvalidPropertyException if property is invalid
     */
     public void processReadOperation(PolicyNodeImpl aResultNode, 
	        NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		boolean aIsParentUpdateLayer) throws SPIException {
        /* Check the validity of the configuration node */
	     if (mName == null ) {
		        throw new InvalidPropertyException();
	    }
         /* determine the operation to be carried out */
    	switch (mOperationType) {
	        case OperationType.OP_REPLACE:
 		        readReplaceProperty(aResultNode, aUpdateNodeKey, 
		            aUpdateNodePath, aIsParentUpdateLayer);
                break;
	        case OperationType.OP_UNKNOWN: //defaults to OP_MODIFY
	        case OperationType.OP_MODIFY:
		        readModifyProperty(aResultNode, aUpdateNodeKey, 
			    aUpdateNodePath, aIsParentUpdateLayer);
                break;
	    };
    }

    /**
      * Sets the value.
      * 
      * @param aValue      the value
      * @param aDataType   the type of the data
      * @throws             <code>SPIException</code> if error occurs
      */
    public abstract void put(String aValue, DataType aDataType) throws SPIException ;

    /**
      * Sets the string value.
      * 
      * @param aValue   the string value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putString(String aValue) throws SPIException;

    /**
      * Sets the hexbinary value as a string.
      * 
      * @param aValue   the hexbinary value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putHexBinary(String aValue) throws SPIException;

    /**
      * Sets the int value.
      * 
      * @param aValue   the int value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putInt(int aValue) throws SPIException;

    /**
      * Sets the double value.
      * 
      * @param aValue   the double value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putDouble(double aValue) throws SPIException;

    /**
      * Sets the short value.
      * 
      * @param aValue   the short value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putShort(short aValue) throws SPIException;


    /**
      * Sets the long value.
      * 
      * @param aValue   the long value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putLong(long aValue) throws SPIException;

    /**
      * Sets the boolean value.
      * 
      * @param aValue   the boolean value
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putBoolean(boolean aValue) throws SPIException;


    /**
      * Sets the string list values.
      * 
      * @param aValues   the list of string values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putStringList(String[] aValues) throws SPIException;

    /**
      * Sets the hexbinary list values.    
      * 
      * @param aValues   the list of hexbinary values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putHexBinaryList(String[] aValues) throws SPIException;

    /**
      * Sets the list of int value.
      * 
      * @param aValues   the list of int values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putIntList(int[] aValues) throws SPIException;

    /**
      * Sets the list of double values.
      * 
      * @param aValue   the list of double values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putDoubleList(double[] aValues) throws SPIException;

    /**
      * Sets the list of short values.
      * 
      * @param aValue   the list of short values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putShortList(short[] aValues) throws SPIException;

    /**
      * Sets the list of long values.
      * 
      * @param aValue   the list of long values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putLongList(long []aValues) throws SPIException;

    /**
      * Sets the list of boolean values.
      * 
      * @param aValue   the list of boolean values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putBooleanList(boolean[] aValues) throws SPIException;

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
    public abstract void readModifyProperty(PolicyNodeImpl aResultNode, 
	        NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		    boolean aIsParentUpdateLayer) throws SPIException ;

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
    public abstract void readReplaceProperty(PolicyNodeImpl aResultNode, 
		NodeKey aUpdateNodeKey, String aUpdateNodePath, 
		boolean aIsParentUpdateLayer)
				throws SPIException ;

    /**
      * Sets the data type.
      * 
      * @return   the data type
      */
    public void setDataType(DataType aDataType) {
        mDataType = aDataType;
    }

    /**
     * Set the finalized attribute for the element. 
     *
     * @param aSetting    <code>true</code> or <code>false</code>
     * @param aNameOfElementWhereProtectionSet
     * @param aOriginOfProtection
     */
    public void setFinalized (boolean aSetting, 
            String aNameOfElementWhereProtectionSet, 
            Policy aOriginOfProtection) {
        super.setFinalized(aSetting);
        mNameOfElementWhereProtectionSet = aNameOfElementWhereProtectionSet;
        mOriginOfProtection = aOriginOfProtection;
    }


    /**
     * Sets the localized flag to true. Only properties 
     * defined in the schema may have this attribute.
     *
     */
    public void setLocalized() {
	    mLocalized = true;
    }               
    

    /**
     * Sets the property value to Nil.
     *
     * @throws             <code>SPIException</code>
     */
    public abstract void setNil() throws SPIException ;


    /**
     * Sets the nillable flag indicating whether or not
     * a property may assume the value nil. A property  is
     * nillable by default. 
     *
     * @param aNillable   <code>true</code> if node is
     *                    nillable, otherwise <code>false</code>
     */
    public void setNillable(boolean aNillable) {
	    mNillable = aNillable;
    }

    /**
     * Sets a new value if the property is not read only.
     * 
     * @param aValue   node value
     * @param aLocale  locale 
     * @throws         <code>SPIException</code> if
     *		       property is readonly 
     */
    public void setNodeValue(NodeValueImpl aValue, String aLocale) 
		throws SPIException {
	    /* if property node is readonly then its value may not be changed */ 
	    checkIfReadOnly();
	    aValue.setPropertyImpl(this);
	    if (mValues == null) { mValues = new Hashtable(); }
            if (!mLocalized || aLocale == null || 
                aLocale.equals(PolicyTree.DEFAULT_LOCALE_NAME)) {
	        mValues.put(PolicyTree.DEFAULT_LOCALE_NAME, aValue);
	    } else {
	        mValues.put(aLocale, aValue);
	    }
    }

    /**
     * Sets the layer where the property originated.
     *
     * @param aOriginLayer   id of the layer where the node originated
     */
    public void setOrigin (Policy aOriginLayer) {
	    mOriginLayer = aOriginLayer;
	    if (mValues != null) {
	        Enumeration values = mValues.elements();
	        if (values != null) {
	            while (values.hasMoreElements()) {
		            ((NodeValueImpl)values.nextElement()).setOrigin(aOriginLayer);
		        }
	        }
	    }
    }

    /**
     * Used during parsing to set a new value.
     *
     * @param aValue   node value
     * @param aLocale  locale
     */
    public void setParsedValue(NodeValueImpl aValue, String aLocale) {
	    if (mValues == null) { mValues = new Hashtable(); }
	    aValue.setPropertyImpl(this);
	    if (aLocale == null ||
            aLocale.equals(PolicyTree.DEFAULT_LOCALE_NAME)) {
            mValues.put(PolicyTree.DEFAULT_LOCALE_NAME, aValue);
	    } else {
            mValues.put(aLocale, aValue);
	    }
    }


    /**
     * Sets the required locale.
     *
     * @param aRequiredLocale   the locale specified 
     *                          by java proxy
     */
    public void setRequiredLocale(String aRequiredLocale) {
	    /* if the required locale is null or the default
	       locale then no action is required */
	    if (aRequiredLocale != null ||
		    !aRequiredLocale.equals(PolicyTree.DEFAULT_LOCALE_NAME)) {
                mRequiredLocale = aRequiredLocale;
	    }
    }

    /**
     * Sets the separator used by the property's values.
     *
     * @param aSeparator   separator
     */
    public abstract void setSeparator(String aSeparator) ;

    /**       
     * Sets the path, originating layer and dynamic
     * settings for this property.
     * 
     * @param aPath          path name for this property 
     * @param aOriginLayer   layer where property originated
     * @param aIsParentLayer <code>true</code> true if
     *			     data sourced from parent layer,
     *			     otherwise <code>false</code> 
     */
    public void setSettingsForAddedProperty(String aPath,
		Policy aOriginLayer, boolean aIsParentLayer ) {      
	    setPath(aPath);
	    if (aOriginLayer != null) { setOrigin(aOriginLayer); }
	    if (!aIsParentLayer) {
	        setAddedAtTopLayer();
	    }
    }

    /**
     * Returns a copy of the property.
     *
     * @return      copy of the property
     * @throws      <code>SPIException</code> if cannot 
     *		        create copy
     */
    public ConfigElementImpl shallowCopy() throws SPIException {
	    ConfigElementImpl returnProperty = super.shallowCopy();
        if (mValues != null && !mValues.isEmpty()) {
            ((PropertyImpl)returnProperty).mValues =
                     copyNodeValueImplTable(mValues, 
                             (PropertyImpl)returnProperty);
       }
        ((PropertyImpl)returnProperty).mSeparator = mSeparator;
        ((PropertyImpl)returnProperty).mNameOfElementWhereProtectionSet =
            mNameOfElementWhereProtectionSet;
        ((PropertyImpl)returnProperty).mOriginOfProtection =
            mOriginOfProtection;
        ((PropertyImpl)returnProperty).mOriginLayer =
            mOriginLayer;
        ((PropertyImpl)returnProperty).mRequiredLocale = mRequiredLocale;
        ((PropertyImpl)returnProperty).mDataType = mDataType;
        return returnProperty;
    }
}
