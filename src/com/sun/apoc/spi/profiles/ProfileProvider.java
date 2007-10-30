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

import java.util.Iterator;

import com.sun.apoc.spi.Provider;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;

/**
 * Provides access to the Profiles
 *
 */
public interface ProfileProvider extends Provider {

    /**
     * Returns the default <code>ProfileRepository</code>
     *
     * @return		the default <code>ProfileRepository</code>
     * @throws		<code>SPIException</code> if error occurs 
     */
    public ProfileRepository getDefaultProfileRepository()
    	throws SPIException;
    
    /**
     * Returns the requested <code>ProfileRepository</code>
     *
     * @param	id	the id for the required <code>ProfileRepository</code>
     * @return		the <code>ProfileRepository</code> object
     * @throws		<code>SPIException</code> if error occurs 
     */
    public ProfileRepository getProfileRepository(String id)
		throws SPIException;
    
    /**
     * Returns the requested <code>Profile</code>.
     *
     * @param id   the id for the required <code>Profile</code>
     * @return      the <code>Profile</code> object 
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Profile getProfile(String id) throws SPIException;
    
    /**
     * Returns all the <code>Profile</code>s.
     *
     * @return      an Iterator over all the <code>Profile</code> objects 
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Iterator getAllProfiles() throws SPIException;

    /**
     * Returns all the Profiles stored in the startingEntity
     * and all of its sub-entities.
     *
     * @return      an Iterator over all the Profile objects 
     * @throws      SPIException if error occurs 
     */
    public Iterator getAllProfiles(Entity startingEntity) throws SPIException;
}
