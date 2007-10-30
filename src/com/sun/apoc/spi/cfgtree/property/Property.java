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

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.DataType;
import com.sun.apoc.spi.cfgtree.ProtectedElement;

/**
  * Interface for a property.
  *
  */
public interface Property extends ProtectedElement{

    /**
      * Returns the value as a string.
      * 
      * @return   the value as a string
      * @throws   <code>SPIException</code> if error occurs
      */
    public String getValue() throws SPIException;

    /**
      * Returns the data type.
      * 
      * @return   the data type
      */
    public DataType getDataType() ;

    /**
      * Returns a boolean indicating if the property is nil.
      * 
      * @return   <code>true</code> if nil, otherwise
      *           <code>false</code>
      */
    public boolean isNil() ;

    /**
      * Returns the value as an int.
      * 
      * @return   the value as an int
      * @throws   <code>SPIException</code> if error occurs
      */
    public int getInt() throws SPIException;

    /**
      * Returns the value as a double.
      * 
      * @return   the value as a double
      * @throws   <code>SPIException</code> if error occurs
      */
    public double getDouble() throws SPIException;

    /**
      * Returns the value as a short.
      * 
      * @return   the value as a short
      * @throws   <code>SPIException</code> if error occurs
      */
    public short getShort() throws SPIException;

    /**
      * Returns the value as a long.
      * 
      * @return   the value as a long
      * @throws   <code>SPIException</code> if error occurs
      */
    public long getLong() throws SPIException;

    /**
      * Returns the value as a boolean.
      * 
      * @return   the value as a boolean
      * @throws   <code>SPIException</code> if error occurs
      */
    public boolean getBoolean() throws SPIException;

    /**
      * Returns the value as an array of ints.
      * 
      * @return   the value as an array of ints
      * @throws   <code>SPIException</code> if error occurs
      */
    public int[] getIntList() throws SPIException;

    /**
      * Returns the value as an array of doubles.
      * 
      * @return   the value as an array of doubles
      * @throws   <code>SPIException</code> if error occurs
      */
    public double[] getDoubleList() throws SPIException;

    /**
      * Returns the value as an array of shorts.
      * 
      * @return   the value as an array of shorts
      * @throws   <code>SPIException</code> if error occurs
      */
    public short[] getShortList() throws SPIException;

    /**
      * Returns the value as an array of longs.
      * 
      * @return   the value as an array of longs
      * @throws   <code>SPIException</code> if error occurs
      */
    public long[] getLongList() throws SPIException;

    /**
      * Returns the value as an array of booleans.
      * 
      * @return   the value as an array of booleans
      * @throws   <code>SPIException</code> if error occurs
      */
    public boolean[] getBooleanList() throws SPIException;

    /**
      * Returns the separator.
      * 
      * @return         the separator
      */
    public String getSeparator() ;

    /**
      * Sets the value.
      * 
      * @param aValue      the value
      * @param aDataType   the type of the data
      * @throws   <code>SPIException</code> if error occurs
      */
    public void put(String aValue, DataType aDataType) throws SPIException;

    /**
      * Sets the string value.
      * 
      * @param aValue   the string value
      * @throws   <code>SPIException</code> if error occurs
      */
    public void putString(String aValue) throws SPIException;

    /**
      * Sets the int value.
      * 
      * @param aValue   the int value
      * @throws   <code>SPIException</code> if error occurs
      */
    public void putInt(int aValue) throws SPIException;

    /**
      * Sets the double value.
      * 
      * @param aValue   the double value
      * @throws   <code>SPIException</code> if error occurs
      */
    public void putDouble(double aValue) throws SPIException;

    /**
      * Sets the short value.
      * 
      * @param aValue   the short value
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putShort(short aValue) throws SPIException;

    /**
      * Sets the long value.
      * 
      * @param aValue   the long value
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putLong(long aValue) throws SPIException;

    /**
      * Sets the boolean value.
      * 
      * @param aValue   the boolean value
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putBoolean(boolean aValue) throws SPIException;

    /**
      * Sets the string list values.
      * 
      * @param aValues   the list of string values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putStringList(String[] aValues) throws SPIException;

    /**
      * Sets the hexbinary list values.    
      * 
      * @param aValues   the list of hexbinary values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putHexBinaryList(String[] aValues) throws SPIException;

    /**
      * Sets the list of int value.
      * 
      * @param aValues   the list of int values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putIntList(int[] aValues) throws SPIException;

    /**
      * Sets the list of double values.
      * 
      * @param aValues   the list of double values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putDoubleList(double[] aValues) throws SPIException;

    /**
      * Sets the list of short values.
      * 
      * @param aValues   the list of short values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putShortList(short[] aValues) throws SPIException;

    /**
      * Sets the list of long values.
      * 
      * @param aValues   the list of long values
      * @throws         <code>SPIException</code> if error occurs
      */
    public void putLongList(long []aValues) throws SPIException;

    /**
      * Sets the list of boolean values.
      * 
      * @param aValues   the list of boolean values
      * @throws         <code>SPIException</code> if error occurs
      */
    public abstract void putBooleanList(boolean[] aValues) throws SPIException;


    /**
      * Sets the separator.
      * 
      * @param aSeparator         the separator
      */
    public void setSeparator(String aSeparator) ;

}
