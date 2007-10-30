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

/**
 * Class <code>StringEnum</code> is an abstract base class for
 * the String version of a C/C++ enum.
 * <p>
 * StringEnum is immutable, which means that once it has been constructed,
 * it cannot be modified. The only instance member variable is <code>value</code>
 * which is private; subclasses can read it's value only through getIntValue().
 * See also StringRangeEnum, a subclass that enforces a contiguous range of
 * integer values.
 *
 */

abstract public class StringEnum {

	/**
	 * we store the value of a StringEnum as an int because it is
	 * compact, easy to check and easy to convert to a String
	 * using enumIntToString(value)
	 */
	private int value;

	protected StringEnum(int n) {
		value = n;
	}

	/**
	 * Obtain value for this StringEnum in the form of an int.
	 */
	public int getIntValue() {
		return value;
	}

	public String toString() {
		return getStringValue();
	}

	/* ### { abstract methods to be implemented in concrete classes */

	abstract public String getStringValue();

	/* ### } */
}
