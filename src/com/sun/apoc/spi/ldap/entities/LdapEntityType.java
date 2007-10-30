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

package com.sun.apoc.spi.ldap.entities;
import com.sun.apoc.spi.util.StringRangeEnum;

/**
 * Class <code>LdapEntityType</code> is a StringRangeEnum for
 * storing the possible values of entity-type.
 *
 */

public class LdapEntityType extends StringRangeEnum {

	/**
	 * The LdapEntityType String constants uniquely identify the type.
	 */
	public static final String STR_NULL    = "";
	public static final String STR_UNKNOWN = "UNKNOWN";
	public static final String STR_ALL  = "ALL";
	public static final String STR_ORG     = "ORG";
	public static final String STR_DOMAIN     = "DOMAIN";
	public static final String STR_ROLE    = "ROLE";
	public static final String STR_USERID  = "USERID";
	public static final String STR_HOST  = "HOST";

	public static final int INT_NULL    = 0;
	public static final int INT_UNKNOWN = 1;
	public static final int INT_ALL     = 2;
	public static final int INT_ORG     = 3;
	public static final int INT_DOMAIN     = 4;
	public static final int INT_ROLE    = 5;
	public static final int INT_USERID  = 6;
	public static final int INT_HOST  = 7;

	public static final LdapEntityType NULL    = new LdapEntityType(INT_NULL);
	public static final LdapEntityType UNKNOWN = new LdapEntityType(INT_UNKNOWN);
	public static final LdapEntityType ALL = new LdapEntityType(INT_ALL);
	public static final LdapEntityType ORG     = new LdapEntityType(INT_ORG);
	public static final LdapEntityType DOMAIN     = new LdapEntityType(INT_DOMAIN);
	public static final LdapEntityType ROLE    = new LdapEntityType(INT_ROLE);
	public static final LdapEntityType USERID  = new LdapEntityType(INT_USERID);
	public static final LdapEntityType HOST  = new LdapEntityType(INT_HOST);
        
	private LdapEntityType(int n) {
		super(n);
	}
	
	protected String[] getEnumStrings() {
		return enumStrings;
	}
	protected LdapEntityType[] getEnums() {
	    return enums;
        }

	private static final String enumStrings[] = {
		STR_NULL,
		STR_UNKNOWN,
		STR_ALL,
		STR_ORG,
		STR_DOMAIN,
		STR_ROLE,
		STR_USERID,
		STR_HOST
	};

	private static final LdapEntityType enums[] = {
		NULL,
		UNKNOWN,
	        ALL,
		ORG,
		DOMAIN,
		ROLE,
		USERID,
	        HOST
	};

	/**
	 * Factory method for StringEnum instances.
	 */
	public static LdapEntityType getEntityType(String eString) {
		for (int i=0; i < enumStrings.length; i++)
			if (eString.equals(enumStrings[i]))
				return enums[i];

		return null;
	}

	/**
	 * Factory method for StringEnum instances.
	 */
	public static LdapEntityType getEntityType(int n) {
		if (n >=0 && n < enums.length)
			return enums[n];

		return null;
	}
}
