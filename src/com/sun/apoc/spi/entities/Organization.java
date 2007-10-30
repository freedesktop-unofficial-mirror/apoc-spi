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

import com.sun.apoc.spi.SPIException;

/**
  * Interface for an organization.
  *
  */
public interface Organization extends Node
{
    /**
      * Returns user entities contained by this organization.
      * 
      * @return            <code>Iterator</code> of child user entities
      * @throws            <code>SPIException</code> if error occurs
      */
    public Iterator getUsers() throws SPIException;

    /**
      * Returns suborganizations.
      * 
      * @return           <code>Iterator</code> of suborganizations
      * @throws           <code>SPIException</code> if error occurs
      */
    public Iterator getSubOrganizations() throws SPIException;

    /**
      * Returns organizations that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for organizations
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of organization objects
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findSubOrganizations(String aFilter, boolean aIsRecursive) throws SPIException;

    /**
      * Returns users that match the specified filter.
      *
      * @param aFilter       the filter to use in searching for users
      * @param aIsRecursive  <code>true</code> if recursive search required,
      *                      otherwise <code>false</code>
      * @return              <code>Iterator</code> of user objects
      * @throws              <code>SPIException</code> if error occurs
      */
    public Iterator findUsers(String aFilter, boolean aIsRecursive) throws SPIException;
}
