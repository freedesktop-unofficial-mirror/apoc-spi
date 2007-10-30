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

package com.sun.apoc.spi.file;

import com.sun.apoc.spi.AssignmentProvider;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.Provider;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import com.sun.apoc.spi.environment.EnvironmentConstants;
import com.sun.apoc.spi.file.assignments.FileAssignmentProvider;
import com.sun.apoc.spi.file.entities.FileDomainProvider;
import com.sun.apoc.spi.file.entities.FileOrganizationProvider;
import com.sun.apoc.spi.file.profiles.FileProfileProvider;
import com.sun.apoc.spi.profiles.ProfileProvider;

/**
  * Class for providing File-type Providers.
  */
public class FileProviderFactory {
    
    public FileProviderFactory(String url, Class aProviderClass, PolicySource aSource) {
    }
    
     /**
      * Returns the requested type of <code>Provider</code> object .
      *
      * @param url              the url specifying the provider data source
      * @param aProviderClass   the type of <code>Provider</code> object
      * @param aPolicySource    the <code>PolicySource</code> that owns these providers
      * @return     <code>Provider</code>
      * @throws     <code>SPIException</code> if error occurs 
      */    
    static public Provider get(String url, Class aProviderClass, PolicySource aPolicySource) throws SPIException {
        Provider provider = null;
        String sEntityType = aPolicySource.getName();
        if (aProviderClass == EntityTreeProvider.class) {
            if (sEntityType.equals(EnvironmentConstants.HOST_SOURCE)) {
                provider = new FileDomainProvider(aPolicySource, url);
            } else if (sEntityType.equals(EnvironmentConstants.USER_SOURCE)) {
                provider = new FileOrganizationProvider(aPolicySource, url);
            } 
        } else if (aProviderClass == AssignmentProvider.class) {
                provider = new FileAssignmentProvider(aPolicySource, url);
        } else if (aProviderClass == ProfileProvider.class) {
                provider = new FileProfileProvider(aPolicySource, url);
        }
        return provider;
    }
}

