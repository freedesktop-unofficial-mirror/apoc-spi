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

import com.sun.apoc.spi.util.StringRangeEnum;

/**
  * Handles the possible types of the node values and
  * their storage as integers.
  *
  */
public class DataType extends StringRangeEnum
{
    protected static final int FIRST_LIST_ELEMENT = 8;
    /** String type string */
    public static final String STR_STRING = "xs:string" ;
    /** Boolean type string */
    public static final String STR_BOOLEAN = "xs:boolean" ;
    /** Short type string */
    public static final String STR_SHORT = "xs:short" ;
    /** Int type string */
    public static final String STR_INT = "xs:int" ;
    /** Long type string */
    public static final String STR_LONG = "xs:long" ;
    /** Double type string */
    public static final String STR_DOUBLE = "xs:double" ;
    /** HexBinary type string */
    public static final String STR_HEXBIN = "xs:hexBinary" ;
    /** Any type string - where oor:any is used for any 
	basic type or derived list */
    public static final String STR_ANY = "oor:any" ;
    /** String list type */
    public static final String STR_STRING_LIST = "oor:string-list" ;
    /** Boolean list type */
    public static final String STR_BOOLEAN_LIST = "oor:boolean-list" ;
    /** Short list type */
    public static final String STR_SHORT_LIST = "oor:short-list" ;
    /** Int list type */
    public static final String STR_INT_LIST = "oor:int-list" ;
    /** Long list type */
    public static final String STR_LONG_LIST = "oor:long-list" ;
    /** Double list type */
    public static final String STR_DOUBLE_LIST = "oor:double-list" ;
    /** HexBinary list type */
    public static final String STR_HEXBIN_LIST = "oor:hexBinary-list" ;

    /** Symbolic int value for unknown type */
    public static final int INT_UNKNOWN = -1 ;
    /** Symbolic int value for string type */
    public static final int INT_STRING = 0 ;
    /** Symbolic int value for boolean type */
    public static final int INT_BOOLEAN = 1 ;
    /** Symbolic int value for short type */
    public static final int INT_SHORT = 2 ;
    /** Symbolic int value for int type */
    public static final int INT_INT = 3 ;
    /** Symbolic int value for long type */
    public static final int INT_LONG = 4 ;
    /** Symbolic int value for double type */
    public static final int INT_DOUBLE = 5 ;
    /** Symbolic int value for hexBinary type */
    public static final int INT_HEXBIN = 6 ;
    /** Symbolic int value for any type */
    public static final int INT_ANY = 7 ;
    /** Symbolic int value for string list type */
    public static final int INT_STRING_LIST = 8 ;
    /** Symbolic int value for boolean list type */
    public static final int INT_BOOLEAN_LIST = 9 ;
    /** Symbolic int value for short list type */
    public static final int INT_SHORT_LIST = 10 ;
    /** Symbolic int value for int list type */
    public static final int INT_INT_LIST = 11 ;
    /** Symbolic int value for long list type */
    public static final int INT_LONG_LIST = 12 ;
    /** Symbolic int value for double list type */
    public static final int INT_DOUBLE_LIST = 13 ;
    /** Symbolic int value for hexBinary list type */
    public static final int INT_HEXBIN_LIST = 14 ;

    /** The equivalent DataTypes */
    public static final DataType UNKNOWN = new DataType(INT_UNKNOWN);
    public static final DataType STRING = new DataType(INT_STRING);
    public static final DataType BOOLEAN = new DataType(INT_BOOLEAN);
    public static final DataType SHORT = new DataType(INT_SHORT);
    public static final DataType INT = new DataType(INT_INT);
    public static final DataType LONG = new DataType(INT_LONG);
    public static final DataType DOUBLE = new DataType(INT_DOUBLE);
    public static final DataType HEXBIN = new DataType(INT_HEXBIN);
    public static final DataType ANY = new DataType(INT_ANY);
    public static final DataType STRING_LIST = new DataType(INT_STRING_LIST);
    public static final DataType BOOLEAN_LIST = new DataType(INT_BOOLEAN_LIST);
    public static final DataType SHORT_LIST = new DataType(INT_SHORT_LIST);
    public static final DataType INT_LIST = new DataType(INT_INT_LIST);
    public static final DataType LONG_LIST = new DataType(INT_LONG_LIST);
    public static final DataType DOUBLE_LIST = new DataType(INT_DOUBLE_LIST);
    public static final DataType HEXBIN_LIST = new DataType(INT_HEXBIN_LIST);

    /**
     * Array aligned on int values of possible types
     * to provide a string equivalent.
     */
    private static final String enumStrings [] = {
        STR_STRING,
        STR_BOOLEAN,
        STR_SHORT,
        STR_INT,
        STR_LONG,
        STR_DOUBLE,
        STR_HEXBIN,
	STR_ANY,
        STR_STRING_LIST,
        STR_BOOLEAN_LIST,
        STR_SHORT_LIST,
        STR_INT_LIST,
        STR_LONG_LIST,
        STR_DOUBLE_LIST,
        STR_HEXBIN_LIST
    } ;

    private static final DataType enums[] = {
	STRING,
	BOOLEAN,
	SHORT,
	INT,
	LONG,
	DOUBLE,
	HEXBIN,
	ANY,
	STRING_LIST,
	BOOLEAN_LIST,
	SHORT_LIST,
	INT_LIST,
	LONG_LIST,
	DOUBLE_LIST,
	HEXBIN_LIST
    };

    private DataType(int n) {
	super(n);
    }

    protected String[] getEnumStrings()  {
	return enumStrings;
    }
    protected DataType[] getEnums() {
	return enums;
    }
   /**
    * Checks if the type is a list type.
    *
    * @return   <code>true</code> if list type, otherwise
    *           <code>false</code>
    */
    public boolean isList() {
	boolean success = false;
	if (this != UNKNOWN) {
	    for (int i = 0; i < enums.length; i++) {
		if (this == enums[i]) {
		    if (i >= FIRST_LIST_ELEMENT) { 
			success = true; 
		    }
		    break;
		}
	    }
	}
	return success;
    }
	
    /**
     * Factory method for DataType instances where a
     * string is the parameter.
     *
     * @param aString   string name
     * @return          the corresponding <code>DataType</code>
     */
    public static DataType getDataType(String aString) {
	for (int i=0; i < enumStrings.length; i++) {
	    if (aString.equals(enumStrings[i])){
		return enums[i];
	    }
	}
        return DataType.UNKNOWN;
    }

    /**
     * Factory method for DataType instances, where
     * an int is the parameter.
     *
     * @param aIntValue  int value
     * @return           the corresponding <code>DataType</code>
     */
    public static DataType getDataType(int aIntValue) {
	if (aIntValue >=0 && aIntValue < enums.length) {
	    return enums[aIntValue];
	}
	return DataType.UNKNOWN;
    }

}
