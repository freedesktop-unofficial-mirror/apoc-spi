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


/**
  * Handles the possible types of the operation values and
  * their storage as integers.
  *
  */
public class OperationType
{
    /** modify operation string */
    public static final String MODIFY_STR = "modify" ;
    /** replace operation string */
    public static final String REPLACE_STR = "replace" ;
    /** remove operation string */
    public static final String REMOVE_STR = "remove" ;
    /** reset operation string */
    public static final String RESET_STR = "reset" ;

    /** symbolic int for invalid operation */
    public static final int OP_UNKNOWN = -1;
    /** symbolic int for modify operation */
    public static final int OP_MODIFY = 0;
    /** symbolic int for replace operation */
    public static final int OP_REPLACE = 1;
    /** symbolic int for remove operation */
    public static final int OP_REMOVE = 2;
    /** symbolic int for reset operation */
    public static final int OP_RESET = 3;
    
    /**
     * Array aligned on int values of possible operations
     * to provide a string equivalent.
     */
    public static final String OPERATION_STRINGS [] = {
        MODIFY_STR,
        REPLACE_STR,
        REMOVE_STR,
        RESET_STR
    } ;

    /**
     * Returns the symbolic int value corresponding to
     * an operation string.
     *
     * @param aOperation    string representing the operation
     * @return              int representing the operation
     */
    public static int getInt(String aOperation) {
        for (int i = 0 ; i < OPERATION_STRINGS.length ; ++ i) {
            if (OPERATION_STRINGS [i].equals(aOperation)) {
                return i ;
            }
        }
        return OP_UNKNOWN ;
    }
    /**
      * Returns the string value corresponding to an int operation.
      * 
      * @param aOperation    int representing the operation
      * @return              string representing the operation
      */
    public static String getString(int aOperation) {
        if (aOperation >= 0 && aOperation < OPERATION_STRINGS.length) {
             return OPERATION_STRINGS [aOperation] ; 
        }
        return null ;
    }
}
