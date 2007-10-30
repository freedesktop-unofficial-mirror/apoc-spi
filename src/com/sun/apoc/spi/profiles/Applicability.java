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

package com.sun.apoc.spi.profiles;

import com.sun.apoc.spi.environment.EnvironmentConstants ;
import com.sun.apoc.spi.util.StringRangeEnum;

/**
  * Handles the possible applications for a profile and
  * their storage as integers.
  *
  */
public class Applicability extends StringRangeEnum 
{
    /** Symbolic string for unknown */
    public static final String STR_UNKNOWN = "UNKNOWN";
    /** Symbolic string for org use */
    public static final String STR_USER = EnvironmentConstants.USER_SOURCE ; 
    /** Symbolic string for host use */
    public static final String STR_HOST = EnvironmentConstants.HOST_SOURCE ;
    /** Symbolic string for all use */
    public static final String STR_ALL = "ALL";
    /** Symbolic int value for unknown use */
    private static final int INT_UNKNOWN = 0 ;
    /** Symoblic int value for org use  */
    private static final int INT_USER = 1 ;
    /** Symbolic int value for domain use */
    private static final int INT_HOST = 2 ;
    /** Symbolic int value for user and host use */
    private static final int INT_ALL = 3 ;
    
    public static final Applicability UNKNOWN = 
			new Applicability(INT_UNKNOWN) ;
    public static final Applicability USER = new Applicability(INT_USER);
    public static final Applicability HOST = new Applicability(INT_HOST);
    public static final Applicability ALL = new Applicability(INT_ALL);

    /**
      * Array aligned on int values of possible types
      * to provide a string equivalent.
      */
    private static final String enumStrings [] = {
		STR_UNKNOWN,
		STR_USER,
		STR_HOST,
		STR_ALL
                
    } ;

    private static final Applicability enums[] = {
		UNKNOWN,
		USER,
		HOST,
        ALL
    };

    private Applicability(int n) {
        super(n);
    }

    protected String[] getEnumStrings() {
        return enumStrings;
    }

    protected Applicability[] getEnums() {
        return enums;
    }

    /**
     * Factory method for Applicability instances where a
     * string is the parameter.
     *
     * @param aString	string name
     * @return		the corresponding <code>Applicability</code>
     */
    public static Applicability getApplicability(String aString) {
		for (int i=0; i < enumStrings.length; i++) {
            if (aString.equals(enumStrings[i])){
				return enums[i];
            }
		}
		return Applicability.UNKNOWN;
    }
    
    public boolean equals (Object aApplicability) {
        if (aApplicability instanceof Applicability) {
            int thisValue = this.getIntValue();
            int appValue = ((Applicability)aApplicability).getIntValue();
            return thisValue == appValue;
        }
        else if (aApplicability instanceof String) {
            return aApplicability.equals(getStringValue()) ;
        }
        return false;
    }
}
