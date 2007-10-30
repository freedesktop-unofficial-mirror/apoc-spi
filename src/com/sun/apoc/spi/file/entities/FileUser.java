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

package com.sun.apoc.spi.file.entities;

import java.util.Collections;
import java.util.Iterator;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.User;
import com.sun.apoc.spi.profiles.Profile;

public class FileUser extends FileEntity implements User
{

    /**
     * @param aDisplayName
     * @param aId
     * @param aParent
     * @param aAssignment
     */
    public FileUser(String aDisplayName, String aId, Entity aParent, PolicySource aPolicySource )
    {
        super(aDisplayName, aId, aParent, aPolicySource);
    }

    public String getUserId()
    {       
        return getDisplayName(null);
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Leaf#getMemberships()
     */
    public Iterator getMemberships() throws SPIException
    {
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * no virtual user profile for a file user,
     * return null
     *
     * @return	null 
     * @throws	<code>SPIException</code> if error occurs
     */
    public Profile getUserProfile() throws SPIException {
        return null;
    }

}
