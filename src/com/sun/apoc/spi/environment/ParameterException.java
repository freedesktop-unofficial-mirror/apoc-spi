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
package com.sun.apoc.spi.environment;

public class ParameterException extends EnvironmentException {
    
    private static final String PARAMETER_KEY = 
        "error.spi.environment.parameter";
    private static final String PARAMETER_WITH_RANGE_KEY = 
        "error.spi.environment.parameter.range";

    protected String mParamName = null;
    protected String mValueRange = null;
    
    public ParameterException () {
        super();
    }

    public ParameterException (String paramName) {
        super();
        mParamName = paramName;
        mMessageKey = PARAMETER_KEY;
        mMessageParams = new Object[]{mParamName};
    }

    public ParameterException (String paramName, String valueRange) {
        super();
        mParamName = paramName;
        mValueRange = valueRange;
        mMessageKey = PARAMETER_WITH_RANGE_KEY;
        mMessageParams = new Object[]{mParamName, mValueRange};
    }
    
    public String getName() {return mParamName;}
    public String getAllowedValueRange() {return mValueRange;}
}
