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

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.property.InvalidPropertyException;
import com.sun.apoc.spi.cfgtree.property.PropertyImpl;
import com.sun.apoc.spi.policies.Policy;

/**
 * Handles storage of the value of a node.
 *
 */
public class NodeValueImpl 
{
    /** buffer for storing value */
    private StringBuffer mValueBuf;
    /** Value array */
    public String mValueArray [] ;
    /** Value type */
    private DataType mDataType = DataType.UNKNOWN ;
    /** Name of the locale for the value */
    private String mLocaleName ;
    /** layer where this value originated */
    private Policy mOriginLayer ;
    /** nil attribute */
    private boolean mNilAttribute = false;
    /** flag indicating contents have been set originally */
    private boolean mIsContentsSet = false;
    /** flag indicating if this NodeValueImpl was modified at
        the entity contained update layer */
    private boolean mIsModifiedAtTopLayer = false;
    /** property node for this value */
    private PropertyImpl mPropertyImpl;
    /** module name used by exceptions */
    private static final String MODULE = "NodeValueImpl";

    /**
     * Returns a string representing the contents of the value.
     *
     * @return contents of the value as a string
     */
    public String getContents() { 
        if (!mIsContentsSet) {
            if (mValueBuf != null) {
                setOriginalContents(mValueBuf.toString());
            } 
            mIsContentsSet = true;
        }
        if ((mValueArray == null) || (mValueArray.length < 1)) {
            return null; 
        }
        String retCode = mValueArray[0];
        if (getSeparator() == null) { return retCode ; }
        for (int i = 1; i < mValueArray.length; ++ i) {
            retCode += getSeparator() + mValueArray[i];
        }
        return retCode;
    }

    /** 
      * Returns the array representing the contents.
      *
      * @return   an array representing contents
      */
    public String[] getValueArray() {
        if (!mIsContentsSet) {
            if (mValueBuf != null) {
                setOriginalContents(mValueBuf.toString());
            } 
            mIsContentsSet = true;
        }
        if (mValueArray == null) {
            return new String[0];
        }
        return mValueArray;
    }

    /**
     * Sets a new value for the contents. 
     *
     * @param aContents     the data
     * @throws		    <code>SPIException</code> if
     *		             error occurs 
     */
    public void setNewContents(String aContents) throws SPIException {
        /* throw an exception if the node is readonly */
        mPropertyImpl.checkIfReadOnly();
        String origContents = getContents();
        String[] origValues = null;
        /* if new contents are different to the original
           contents then check and set the new contents */
        if ((origContents != null) && !origContents.equals(aContents)) {
            if (mValueArray != null) {
                origValues = new String[mValueArray.length];
                for (int i = 0 ; i < mValueArray.length; i++) {
                    origValues[i] = new String(mValueArray[i]);
                }
            }
        }
        setOriginalContents(aContents);
        /* check that the new contents comply with
           the value type specified by the property node */
        try {
            checkContentsAgainstDataType(mDataType);
        } catch (SPIException e) {
            mValueArray = origValues;
            throw e;
        }
        /* set the hasNilAttribute to false */
        mNilAttribute = false;
        /* indicate that this value was changed at 
           uppermost layer */
        mIsModifiedAtTopLayer = true;
        /* set the origin for this nodevalue */
        mOriginLayer = mPropertyImpl.getPolicyTree().getPolicy();
        /* ensure the nodevalue is stored in the propertynode 
           (this is necessary because of the provision of
           fallback values in PropertyImpl.getValue()) */
        mPropertyImpl.setNodeValue(this, mPropertyImpl.getRequiredLocale());
        /* if the property was not introduced on this layer, then
           ensure the opertation attribute is "op=modify" */
        if (!mPropertyImpl.isAddedAtTopLayer()) {
            mPropertyImpl.setOperationType(OperationType.OP_MODIFY);
        }
    }

    /**
     * Sets the value to nil. 
     *
     * @throws SPIException if error occurs 
     * @throws InvalidPropertyException if property
     * 		cannot be set to nil
     */
    public void setValueToNil() throws SPIException {
        /* throw an exception if the node is readonly */
        mPropertyImpl.checkIfReadOnly();
        /* throw an exception if the node is not nillable */
        if (!mPropertyImpl.isNillable()) {
            throw new InvalidPropertyException(
                    InvalidPropertyException.NOTNIL_PROPERTY_KEY,
                    mPropertyImpl.getName());
        }
        /* ensure contents were originally set */
        if (!mIsContentsSet) {
            getContents();
        }
        /* set contents to null */
        mValueArray = null;
        mNilAttribute = true;
        mIsModifiedAtTopLayer = true;
        /* set the origin for this nodevalue */
        mOriginLayer = mPropertyImpl.getPolicyTree().getPolicy();
        /* ensure the nodevalue is stored in the propertynode 
           (this is necessary because of the provision of
           fallback values in PropertyImpl.getValue()) */
        mPropertyImpl.setNodeValue(this, mPropertyImpl.getRequiredLocale());
        /* if the property was not introduced on this layer, then
           ensure the opertation attribute is "op=modify" */
        if (!mPropertyImpl.isAddedAtTopLayer()) {
            mPropertyImpl.setOperationType(OperationType.OP_MODIFY);
        }
    }

    /**
     * Returns the layer where this value was set.
     *
     * @return     layer that is the source of this value
     */
    public Policy getOrigin() {
        return mOriginLayer;
    }

    /**
     * Checks if the xsi:nil attribute has been set. 
     *
     * @return     <code>true</code> if this attribute has been
     *             set, otherwise <code>false</code>a
     */
    public boolean hasNilAttribute () { return mNilAttribute; }

    /** 
     * Returns the type of the value.
     *
     * @return value type
     */
    public DataType getDataType() { return mDataType ; }

    /**
     * Outputs the contents of the value to an XML PrintStream.
     *
     * @param aIndent  indent prefix for the print out
     * @param aOutput  PrintStream to write to
     */
    public void printToStream(String aIndent, PrintStream aOutput) {
        aOutput.print(aIndent + "<" + NodeParsing.VALUE_TAG) ;
        if (mLocaleName != null) {
            aOutput.print(" " + NodeParsing.LOCALE_ATTR + "=\"") ;
            aOutput.print(mLocaleName + "\"") ;
        }
        if (getSeparator() != null &&  !getSeparator().equals(NodeParsing.WHITESPACE)) {
            aOutput.print(" " + NodeParsing.SEPARATOR_ATTR + "=\"") ;
            aOutput.print(getSeparator() + "\"") ;
        }
        printXMLValues(aOutput) ;
    }

    /**
     * Prints a string as a UTF8 byte array into a print stream
     * and performs encoding into entities of the special XML
     * characters.
     *
     * @param aStream    stream to write to
     * @param aString    string to normalise
     */
    private void printXMLString(PrintStream aStream, String aString) {
        byte [] encodedString ;
        
        try {
            encodedString = aString.getBytes("UTF8") ;
        }
        catch (UnsupportedEncodingException exception) {
            // Not good, but UTF8 is supposed to be part
            // of the list of supported encodings by every 
            // Java implementation...
            aStream.print(aString) ;
            return ;
        }
        for (int i = 0 ; i < encodedString.length ; ++ i) {
            byte character = encodedString [i] ;

            switch (character) {
                case '&': aStream.print("&amp;") ; break ;
                case '<': aStream.print("&lt;") ; break ;
                case '>': aStream.print("&gt;") ; break ;
                default: aStream.write(character) ; break ;
            }
        }
    }

    /**
     * Prints in an print stream the values as an UTF8 encoded
     * string, suitable for XML dumping.
     *
     * @param aStream print stream
     */
    private void printXMLValues(PrintStream aStream) {
        if (!mIsContentsSet) {
            if (mValueBuf != null) { 
                setOriginalContents(mValueBuf.toString()); 
            }
            else { mIsContentsSet = true; }
        }
        if (mValueArray != null ) { 
            aStream.print(">");
            int length = mValueArray.length;
            if (length > 0) {
                printXMLString(aStream, mValueArray [0]);
            }
            if (getSeparator() != null && length > 1) { 
                for (int i = 1 ; i < mValueArray.length ; ++ i) {
                    printXMLString(aStream, getSeparator());
                    printXMLString(aStream, mValueArray [i]);
                }
            }
            aStream.print("</"+NodeParsing.VALUE_TAG+">\n");
        } 
        else if (mNilAttribute) {
            aStream.print(" "+NodeParsing.NIL_ATTR+"=\"true\""+"/>\n");
        } 
        else {
            aStream.print("/>\n");
        }
    }

    /**
     * Create a copy of a <code>NodeValueImpl</code>.
     * 
     * @return       a copy of the <code>NodeValueImpl</code>
     */
    public NodeValueImpl copyNodeValueImpl()  {
        NodeValueImpl retValue = new NodeValueImpl();
        /* ensure that mValueBuf in original has been read */
        if (!mIsContentsSet) {
            getContents();
        }
        retValue.mIsContentsSet = true;
        if (mValueArray != null) {
            String[] copiedValues = new String[mValueArray.length];
            for (int i = 0; i < mValueArray.length; i++) {
                copiedValues[i] = new String(mValueArray[i]);
            }
            retValue.mValueArray = copiedValues;
        }
        retValue.mDataType = mDataType;
        retValue.mLocaleName = mLocaleName;
        retValue.mNilAttribute = mNilAttribute;
        retValue.mPropertyImpl = mPropertyImpl;
        retValue.mOriginLayer =  mOriginLayer;
        retValue.mIsModifiedAtTopLayer = mIsModifiedAtTopLayer;
        return retValue;
    }

    /** 
     * Checks if a value complies with the type specified by
     * the property node. Check is only done for values of type
     * short, int, long, double, boolean, or the corresponding
     * lists.
     * 
     * @param aDataType  numerical representation of the value
     * 		             type specified by the property node
     * @throws InvalidDataTypeException if aDataType does not comply
     */
    public void checkContentsAgainstDataType(DataType aDataType) 
            throws SPIException {
        if (mValueArray == null || mValueArray.length < 1) { return; }
        int valueType = aDataType.getIntValue();
        /* if the value type is unknown then return */
        if (valueType == DataType.INT_UNKNOWN) { return; }
        if (valueType == DataType.INT_SHORT || 
            valueType == DataType.INT_INT ||
            valueType == DataType.INT_LONG) {
            long value = 0;
            switch(valueType) {
                case DataType.INT_SHORT:
                    for (int i = 0; i < mValueArray.length; i++) {
                        try {
                            value = Short.parseShort(mValueArray[i]);
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDataTypeException(
                                    aDataType.getStringValue(), nfe);
                        }
                    }
                    break;
                case DataType.INT_INT:
                    for (int i = 0; i < mValueArray.length; i++) {
                        try {
                            value = Integer.parseInt(mValueArray[i]);
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDataTypeException(
                                    aDataType.getStringValue(), nfe);
                        }
                    }
                    break; 
                case DataType.INT_LONG:
                    for (int i = 0; i < mValueArray.length; i++) {
                        try {
                            value = Long.parseLong(mValueArray[i]);
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDataTypeException(
                                    aDataType.getStringValue(), nfe);
                        }
                    }
                    break;
            };
        } else if (valueType == DataType.INT_DOUBLE) {
            for (int i = 0; i < mValueArray.length; i++) {
                try {
                    double doubleValue = Double.parseDouble(mValueArray[i]);
                } catch (NumberFormatException nfe) {
                    throw new InvalidDataTypeException(
                            aDataType.getStringValue(), nfe);
                }
            }
        } else if (valueType == DataType.INT_BOOLEAN) {
            for (int i = 0; i < mValueArray.length; i++) {
                if (mValueArray[i].length() == 1) {
                    /* the value should be 1 or zero */
                    if(!(mValueArray[i].equals("1") ||
                         mValueArray[i].equals("0"))) {
                        throw new InvalidDataTypeException(
                                aDataType.getStringValue());
                    }
                } else {
                    String booleanStr = mValueArray[i].toLowerCase();
                    if (!(booleanStr.equals("true") ||
                          booleanStr.equals("false"))) {
                        throw new InvalidDataTypeException(
                                aDataType.getStringValue());
                    }
                }
            }
        }
    } 

    /**
     * Sets the value of the nil attribute. 
     *
     * @param      <code>true</code> or </code>false</code>
     */
    public void setNilAttribute(boolean aNilAttribute) {
        mNilAttribute = aNilAttribute;
    }

    /**
     * Sets a pointer to the property node to which this
     * value belongs.
     *
     * @param aNode     the property node
     */
    public void setPropertyImpl(PropertyImpl aNode) { 
        mPropertyImpl = aNode; 
    }

    /**
     * Sets a flag indicating the <code>NodeValueImpl</code> was modified
     * at the final layer.
     */
    public void setModifiedAtTopLayer() {
        mIsModifiedAtTopLayer = true;
    }

    /**
     * Returns a boolean indicating whether or not the
     * <code>NodeValueImpl</code> was modified at the final layer. 
     *
     * @return	 <code>true</code> if was modified at 
     *		 final layer, otherwise <code>false</code>
     */
    public boolean isModifiedAtTopLayer() { return mIsModifiedAtTopLayer; }

    /**
     * Sets the type of the value from the int representation.
     *
     * @param aDataType    symbolic int value for the type
     */
    public void setDataType(DataType aDataType) { 
        mDataType = aDataType ; 
    }

    /**
     * Sets the locale name of the value.
     *
     * @param aLocaleName  name of the locale
     */
    public void setLocaleName(String aLocaleName) { 
        mLocaleName = aLocaleName ; 
    }

    /**
     * Returns the name of the locale for the value.
     *
     * @return name of the locale
     */
    public String getLocaleName() { return mLocaleName ; }

    /**
     * Returns the separator used when producing the values as
     * one string.
     *
     * @return separator character
     */
    public String getSeparator() { return mPropertyImpl.getSeparator() ; }

    /*
     * Used by parser to set the original contents 
     * during parsing of the configuration data. 
     * No checking is done, and no exception is thrown.  
     *
     * @param aContents    the data
     */
    public void setOriginalContents(String aContents) {
        String valueStr = aContents;
        if (getSeparator() == null) {
            mValueArray = new String[1];
            mValueArray[0] = valueStr;
        } 
        else if (getSeparator() == NodeParsing.WHITESPACE) {
            mValueArray = valueStr.split(getSeparator());
        } 
        else {
            int start = 0;
            int end = 0;
            Vector results = new Vector();
            while (start <= valueStr.length()) {
                end = valueStr.indexOf(getSeparator());
                if (end < 0 ) {
                    /* just one item in list */
                    results.add(valueStr);
                    break;
                }
                String resultStr = valueStr.substring(start, end);
                resultStr.trim();
                results.add(resultStr);
                valueStr = valueStr.substring(end + 1, valueStr.length());
            }
            if (results != null && !results.isEmpty()) {
                int size = results.size();
                mValueArray = new String[size];
                for (int i = 0; i < size; i++) {
                    mValueArray[i] = new String((String)results.get(i));
                }
            } 
        }
        mIsContentsSet = true;
        /* set the hasNilAttribute to false */
        mNilAttribute = false;
    }

    /**
     * Appends a string read during parsing a value to a buffer. 
     *
     * @param aData   the data read
     */
    public void appendContents(String aContent) {
        if (mValueBuf == null) {
            mValueBuf = new StringBuffer(PolicyTree.BUFFER_SIZE);
        }
        mValueBuf.append(aContent);
    }

    /**
     * Sets the layer that is the source of this value.
     *
     * @param aLayer   layer that is the source of this value
     */
    public void setOrigin(Policy aLayer) {
        mOriginLayer = aLayer;
    }
}
