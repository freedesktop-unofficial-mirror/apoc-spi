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

package com.sun.apoc.spi.util;

import java.util.Hashtable;
import java.util.StringTokenizer;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.environment.RemoteEnvironmentException;

/**
 * Meta-configuration source.
 *
 */
 
public class MetaConfiguration {
    /** value of separator for list items in the metaconfiguration */
    public static final String SEPARATOR = ",";
    /** Table for storing metaconfiguration data*/
    private Hashtable mTable;
                                                                                     
    /**
     * Constructor initialises the table.
     *
     * @param aTable     the table containing the properties.
     * @throws           <code>SPIException</code> if table
     *                   does not exist or is empty
     */
    public MetaConfiguration(Hashtable aTable) 
    		throws SPIException { 
        if (aTable == null || aTable.isEmpty()) {
            throw new RemoteEnvironmentException (
                    RemoteEnvironmentException.INVALID_META_CONF_KEY,
                    null);
        }
        mTable = aTable;
    }
    
    /**
     * Get the Hashtable containing the metaconfiguration properties
     * 
     * @return the hashtable containing the properties
     */
    public Hashtable getTable() {
        return mTable;
    }
  
    /**
     * Searches the table for the <code>String</code> value 
     * for the property with the specified aKey. Returns the default
     * value provided if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @param aDefaultValue      a default value to be used if there
     *                          is no value for the required property
     * @return                  <code>String</code> value of property
     */ 
    public String getString(String aKey, String aDefaultValue) {
        String retCode = getString(aKey);
        if (retCode == null && aDefaultValue != null) {
            retCode = aDefaultValue.trim();
        }
        return retCode;
    }

    /**
     * Searches the table for the <code>String</code> value	
     * for the property with the specified aKey. Returns 
     * <code>null</code> if the property cannot be found.
     *
     * @param aKey	aKey for the required property
     * @return		<code>String</code> value of property,
     *                  or <code>null</code> if property not
     *                  found
     */
    public String getString(String aKey) {
        String retCode = (String)mTable.get(aKey) ;
        if (retCode != null) { 
            retCode = retCode.trim(); 
        }
        return retCode;
    }

    /**
     * Searches for the <code>String</code> value for the 
     * property with the specified aKey.  Converts this String
     * to an array of Strings (using mSeparator as the delimeter), and
     * returns this array. 
     * Returns  <code>null</code> if the property cannot be found.
     *
     * @param aKey      aKey for the required property
     * @return          <code>String</code> value of property,
     *                  or <code>null</code> if property not
     *                  found
     */
    public String[] getStrings(String aKey) {
        return getStrings(aKey, SEPARATOR) ;
    }

    /**
     * Searches for the <code>String</code> value for the
     * property with the specified aKey.  Converts this String
     * to an array of Strings (using aSeparator as the delimeter), and
     * returns this array. 
     * Returns  <code>null</code> if the property cannot be found.
     *
     * @param aKey          aKey for the required property
     * @param aSeparator    separator to use to split the strings
     * @return              <code>String</code> value of property,
     *                      or <code>null</code> if property not
     *                      found
     */
    public String[] getStrings(String aKey, String aSeparator) {
        String value = null;
        StringTokenizer st = null;
        String []stringArray = null;
        value = getString(aKey);
        if (value == null) { return stringArray ;}
        st = new StringTokenizer(value, aSeparator);
        int total = st.countTokens();
        if (total > 0) {
            stringArray = new String[total];
            for (int i = 0; i < total; i++) {
                stringArray[i] = st.nextToken().trim();
            }
        }
        return stringArray;
    } 
      
    /**
     * Searches for the <code>boolean</code> value for the
     * property with the specified aKey. Returns the default
     * value provided if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @param aDefaultValue      a default value to be used if there
     *                          is no value for the required property
     * @return                  <code>boolean</code> value of property
     */ 
    public boolean getBoolean(String aKey, boolean aDefaultValue) {
        String value = getString(aKey) ;
        if (value == null) { return aDefaultValue ; }
        return Boolean.valueOf(value).booleanValue() ;
    }
    
    /**
     * Searches for the <code>boolean</code> value  for the
     * property with the specified aKey. Returns the default
     * value provided if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @param aDefaultValue      a default value to be used if there
     *                          is no value for the required property
     * @return                  <code>boolean</code> value of property
     */ 
     public boolean getBoolean(String aKey, String aDefaultValue) {
        String value = getString(aKey, aDefaultValue);
        return Boolean.valueOf(value).booleanValue() ;
    }

    /**
     * Searches for the <code>boolean</code> value for
     * the property with the specified aKey. Returns 
     * <code>false</code> if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @return                  <code>boolean</code> value of property,
     *                          or false if unsuccessful
     */ 
    public boolean getBoolean(String aKey) {
        return getBoolean(aKey, false) ;
    }

    /**
     * Searches for the <code>int</code> value for the
     * property with the specified aKey. Returns the default
     * value provided if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @param aDefault          a default value to be used if there
     *                          is no value for the required property
     * @return                  <code>int</code> value of property
     * @throws                  <code>NumberFormatException</code> if cannot
     *                          convert <code>String</code> to <code>int</code>
     */ 
    public int getInt(String aKey,
                             int aDefault) throws NumberFormatException {
        String value = getString(aKey) ;
        if (value == null) { return aDefault ; }
        return Integer.parseInt(value) ;
    }

    /**
     * Searches for the <code>int</code> value for the
     * property with the specified aKey. Returns the default
     * value provided if the property cannot be found.
     *
     * @param aKey               aKey for the required property
     * @param aDefaultValue      a default value to be used if there
     *                          is no value for the required property
     * @return                  <code>int</code> value of property
     * @throws                  <code>NumberFormatException</code> if cannot
     *                          convert <code>String</code> to <code>int</code>
     */ 
    public int getInt(String aKey,
                         String aDefaultValue) throws NumberFormatException{
        String value = getString(aKey, aDefaultValue);
        return Integer.parseInt(value);
    }

    /**
     * Searches for the <code>int</code> value for the
     * property with the specified aKey. Returns -1 if property
     * not found.  
     *
     * @param aKey               aKey for the required property
     * @return                  <code>int</code> value of property
     * @throws                  <code>NumberFormatException</code> if cannot
     *                          convert <code>String</code> to <code>int</code>
     */ 
    public int getInt(String aKey) throws NumberFormatException {
        return getInt(aKey, -1) ;
    }
}
	
