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

package com.sun.apoc.spi.cfgtree.policynode;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.ProtectedElement;
import com.sun.apoc.spi.cfgtree.property.Property;

/**
  * Interface for a policy node.
  *
  */
public interface PolicyNode extends ProtectedElement{

    /**
      * Returns list of child node names. 
      *
      * @return    array of child node names
      */
    public String[] getChildrenNames();

    /**
      * Clears the properties. 
      *
      * @throws        <code>SPIException</code> if error occurs
      */
    public void clearProperties() throws SPIException;

    /**
     * Returns the requested property, or null if
     * it doesn't exist.
     *
     * @param      aPropertyName
     * @return     property 
     */
    public Property getProperty(String aPropertyName) ;

    /**
      * Returns list of property names. 
      *
      * @return    array of property names
      */
    public String[] getPropertyNames() ;

    /**
      * Returns the node name. 
      *
      * @return   the node name 
      */
    public String getName() ;

    /**
      * Sets the node name. 
      */
    public void setName(String aName) ;
    
    /**
      * Returns the absolute path for this node. 
      *
      * @return   the absolute path 
      */
    public String getAbsolutePath() ;

    /**
      * Returns  the parent <code>PolicyNode</code>.
      *
      * @return    the parent policy node 
      */
    public PolicyNode getParent() ;

    /**
      * Adds a new node.
      *
      * @param aName    name of the node to be added
      * @return         the newly added <code>PolicyNode</code>
      * @throws         <code>SPIException</code> if error occurs
      */
    public PolicyNode addNode(String aName) throws SPIException;

    /**
      * Removes a node.
      *
      * @param aName    name of the node to be removed
      * @throws         <code>SPIException</code> if error occurs
      */
    public void removeNode(String aName) throws SPIException;
    
    /**
      * Adds a new node with attribute "op=replace".
      *
      * @param aName      name of the node to be replaced
      * @return           the newly replaced <code>PolicyNode</code>
      * @throws           <code>SPIException</code> if error occurs
      */
    public PolicyNode addReplaceNode(String aName) throws SPIException;

    /**
      * Adds a new <code>Property</code>.
      *
      * @param aName    name of the property to be added
      * @return         the newly added <code>Property</code>
      * @throws         <code>SPIException</code> if error occurs
      */
    public Property addProperty(String aName) throws SPIException;

    /**
      * Removes a <code>Property</code>.
      *
      * @param aName    name of the property to be removed
      * @throws         <code>SPIException</code> if error occurs
      */
    public void removeProperty(String aName) throws SPIException;

}
