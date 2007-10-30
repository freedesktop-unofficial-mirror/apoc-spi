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
package com.sun.apoc.spi.ldap.profiles;

import java.util.Comparator;

import netscape.ldap.LDAPDN;

/**
 * Compares two LdapProfiles according to their priority
 *
 */
public class LdapProfileComparator implements Comparator {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object aProfile1, Object aProfile2) {
        LdapProfile profile1 = (LdapProfile) aProfile1 ;
        LdapProfile profile2 = (LdapProfile) aProfile2 ;
        if (profile1 == null) { return profile2 == null ? 0 : -1 ; }
        if (profile2 == null) { return 1 ; }
        String [] pg1DN =  LDAPDN.explodeDN(
                profile1.getProfileRepository().getId(), false) ;
        int pg1Length = pg1DN.length;
        String [] pg2DN =  LDAPDN.explodeDN(
                profile2.getProfileRepository().getId(), false) ;
        int pg2Length = pg2DN.length;
        if (pg1Length != pg2Length) {
            return pg1Length - pg2Length;
        }
        // the lengths are equals, 
        // the 2 profiles ares defined in the same repository
        // if one of the profiles is local, it wins
        if (profile1.isLocal()) { return 1 ; }
        if (profile2.isLocal()) { return -1 ; }
        // 2 non-local profiles defined in the same repository
        // are compared using their priority
        if (profile1.getPriority() != profile2.getPriority()) {
            return profile1.getPriority() - profile2.getPriority() ;
        }
        return profile1.getId().compareTo(
        profile2.getId());
    }

}
