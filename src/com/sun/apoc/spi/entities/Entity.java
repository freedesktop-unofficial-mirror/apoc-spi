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

package com.sun.apoc.spi.entities;

import java.util.Iterator;
import java.util.Locale ;

import com.sun.apoc.spi.AssignmentProvider;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;

/**
  * Interface for an entity.
  */
public interface Entity
{
    /**
      * Returns the display name for this entity.
      *
      * @param aLocale  locale for the display
      * @return           the display name 
      */
    public String getDisplayName(Locale aLocale) ;

    /**
     * Returns an interator over the display names of the ancestors
     * of this entity, starting from the root entity down to the direct
     * parent of this entity, reflecting the entity structure.
     *
     * @param aLocale   locale for the display names
     * @return           display names of the ancestors
     */
    public Iterator getAncestorNames(Locale aLocale);
    
    /**
      * Returns the id for this entity.
      *
      * @return           the id 
      */
    public String getId() ;

    /**
      * Returns parent entity. 
      * 
      * @return            parent entity 
      */
    public Entity getParent() ;

    /**
     * Returns the profile repository for this entity. 
     *
     * @return            the profile repository for this entity 
     * @throws            <code>SPIException</code> if error occurs
     */
    public ProfileRepository getProfileRepository() throws SPIException;

    /**
     * Returns the profiles assigned to this entity.
	 * Returns only the profiles that have an Applicability
	 * compatible with the entity type.
     *
     * @return            <code>Iterator</code> of profiles assigned 
     *                    to this entity
     * @throws            <code>SPIException</code> if error occurs
	 * @see AssignmentProvider#assignProfile(Entity, Profile)
     */
    public Iterator getAssignedProfiles() throws SPIException;

    /**
     * Assigns a profile to this entity.
     * Verify if the Applicability of the Profile is
     * compatible with the type of Entity:
     * a profile with Applicabity.USER can be assigned
     * to a Organization, User or Role defined in an Organization
     * a profile with Applicabity.HOST can be assigned
     * to a Domain, Host or Role defined in Domain
     * a profile with Applicabity.ALL can be assigned
     * to any entity
     *
     * @param aProfile   profile to assign
     * @throws           <code>SPIException</code> if error occurs
     * 					or if Applicability of the profile is not
     * 					compatible with the type of entity
	 * @see AssignmentProvider#assignProfile(Entity, Profile)
     */
    public void assignProfile(Profile aProfile) throws SPIException;

    /**
      * Unassigns specified profile from this entity.
      *
      * @param aProfile   profile to uassign
      * @throws           <code>SPIException</code> if error occurs
	 * @see AssignmentProvider#unassignProfile(Entity, Profile)
      */
    public void unassignProfile(Profile aProfile) throws SPIException;

    /**
     * Returns an iterator of profiles 
     * that contribute to this entity's configuration data. 
     * The first element of the iterator has the lowest priority, 
     * the last element has the highest priority.
     *
     * @return                  iterator of hierarchical profiles
     * @throws                  <code>SPIException</code> if error
     *                          occurs
     */
    public Iterator getLayeredProfiles() 
        throws SPIException ;
    
    /**
     * Returns the name of <code>PolicySource</code> for this entity
     * @return                  the name of the <code>PolicySource</code>
     */    
    public String getPolicySourceName() ;
}
