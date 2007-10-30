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
package com.sun.apoc.spi.policies;

/**
 * Object representing some information about a Policy
 * in memory (no interaction with the backend).
 *
 */
public class PolicyInfo {
    protected String mId;
    protected long mLastModified=-1;
    protected String mProfileId;

	/**
	 * Constructor for class.
	 * 
	 * @param aId           identifier
	 * @param aProfileId    identifier of the Profile containing 
	 * 						this Policy
	 * @param aLastModified time in milliseconds of the last modification
	 * 						of this Policy
	 */
    public PolicyInfo(String aId, String aProfileId, long aLastModified) {
        mId = aId;
        mProfileId = aProfileId;
        mLastModified = aLastModified;
    }

	/**
	 * Constructor for class.
	 * 
	 * @param aId           identifier
	 * @param aProfileId    identifier of the Profile containing 
	 * 						this Policy
	 */
    public PolicyInfo(String aId, String aProfileId) {
        this(aId, aProfileId, System.currentTimeMillis());
    }

	/**
	 * Constructor for class.
	 * 
	 * @param aId           identifier
	 */
    public PolicyInfo(String aId) {
        mId = aId;
        mLastModified = System.currentTimeMillis();
    }

	/**
	 * Returns the id for this policy.
	 *
	 * @return    id for the policy
	 */
    public String getId() {
        return mId;
    }

	/**
	 * Returns the id of the profile containing this policy.
	 *
	 * @return    profile id
	 */
    public String getProfileId() {
        return mProfileId;
    }

    /**
     * Returns the time of the last modification of the policy.
     *
     * @return  time of the last modification in milliseconds
     * 			returns -1 if the time hasn't been initialized.
     */
    public long getLastModified() {
        return mLastModified;
    }
}
