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

package com.sun.apoc.spi.file.profiles;

import java.util.Comparator;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.profiles.Profile;

/**
 * Compares two FileProfiles according to their priority
 *
 */
public class FileProfileComparator implements Comparator {

    public int compare(Object aProfile1, Object aProfile2) {
        int profDepth1;
        int profDepth2;
        Profile profile1 = (Profile)aProfile1;
        Profile profile2 = (Profile)aProfile2;
        
        if (profile1 == null) { return profile2 == null ? 0 : -1 ; }
        if (profile2 == null) { return 1 ; } 
        
        profDepth1 = getProfileDepth(profile1);
        profDepth2 = getProfileDepth(profile2);
        if (profDepth1 != profDepth2 ) {
            return profDepth1 - profDepth2;
        }
        
        if (profile1.getPriority() != profile2.getPriority()) {
            return profile1.getPriority() - profile2.getPriority() ;
        }
        return profile1.getId().compareTo(profile2.getId());
    }
         
    private int getProfileDepth(Profile aProfile) {
        int depth = 0;
        try {
            Entity entity = aProfile.getProfileRepository().getEntity();
            
            while (entity != null ) {
                depth++;
                entity = entity.getParent();
            }            
        } catch (SPIException e) {
            // should not happend
        }
        return depth;
    }

}
