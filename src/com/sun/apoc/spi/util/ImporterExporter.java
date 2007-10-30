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

import java.io.InputStream;
import java.io.OutputStream;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;

/**
  * Common interface handling the import/export of a profile.
  * Each implementation must define the actual format of the 
  * exported data.
  */
public interface ImporterExporter {

    /**
     * Exports the contents of a profile to a stream.
     *
     * @param aProfile      profile identifier
     * @param aOutput       stream for exported data
     * @throws SPIException if an error occurs.
     */
    public void exportProfile(Profile aProfile, OutputStream aOutput)
        throws SPIException;
    
    /**
     * Imports the contents of a stream to a profile.
     * If a profile with this name already exists then
     * it is overwritten. 
     * 
     * @param aRepository      profile repository
     * @param aDisplayName     profile display name
     * @param aApplicability   scope
     * @param aInput           stream containing data to import
     *
     * @throws SPIException if an error occurs.
     */
    public void importProfile(ProfileRepository aRepository, 
        String aDisplayName, Applicability aApplicability, InputStream aInput) 
        throws SPIException;
}
