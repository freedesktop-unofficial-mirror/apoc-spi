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

package com.sun.apoc.spi.cfgtree;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNode;
import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.property.Property;
import com.sun.apoc.spi.policies.Policy;

/**
  * Abstract class for a configuration element.
  *
  */
public abstract class ConfigElementImpl {

    /** boolean indicating whether or not the element was added
        by the toplevel profile policy */
    protected boolean mIsAddedAtTopLayer = false;
    /** boolean indicating whether or not an element is dynamic, that is,
        an added element */
    protected boolean mIsDynamic = false;
    /** boolean indicating whether or not an element may be
	removed or replaced */
    protected boolean mIsMandatory = false;
    /** Name of the element */
    protected String mName;
    /** int representing operation attribute value */
    protected int mOperationType = OperationType.OP_UNKNOWN;
    /** layer where mandatory attribute set to true */
    protected Policy mOriginOfMandatory;
    /** The package name */
    protected String mPackage ;
    /** Parent node */
    protected PolicyNodeImpl mParent ;
    /** The path to the element */
    protected String mPath;
    /** Policy tree object */
    protected PolicyTreeImpl mPolicyTree;
    /** Module name for exceptions */
    private static final String MODULE = "ConfigElementImpl";


    /**
     * Used prior to removing a element to check if the element is  
     * mandatory, and if it is then an exception is thrown.
     *
     * @throws		<code>SPIException</code> if element 
     *			mandatory
     */
    public void checkIfMandatory() throws SPIException {
	    if (mIsMandatory) {
	        String elementType = "ProtectedElement";
	        if (this instanceof PolicyNode) {
	            elementType = "PolicyNode";
	        }
	        else if (this instanceof Property) {
	            elementType = "Property";
	        }
	        throw new MandatoryElementException(elementType, getName());
	    }
    }

    /**
     * Copies this elements's settings to the element passed
     * into the function. 
     *
     * @param aNewElement  the element to be changed
     * @throws      <code>SPIException</code> if error 
     *		    occurs
     */
    public void copySettings(ConfigElementImpl aNewElement) 
            throws SPIException {
	    aNewElement.mIsAddedAtTopLayer = mIsAddedAtTopLayer;
	    aNewElement.mIsDynamic = mIsDynamic;
	    aNewElement.mIsMandatory = mIsMandatory;
        aNewElement.mName = mName;
	    aNewElement.mOperationType = mOperationType;
	    aNewElement.mPackage = mPackage;
	    aNewElement.mPolicyTree = mPolicyTree;
	    aNewElement.mParent = mParent;
        aNewElement.mPath = mPath;
	    aNewElement.mOriginOfMandatory = mOriginOfMandatory;
    }

    /**
      * Returns the absolute path for this node. 
      *
      * @return   the absolute path 
      */
    public String getAbsolutePath() { return mPath; }

    /** 
     * Returns the name of the element.
     *
     * @return 	name of the element
     */
    public String getName() { return mName ; }


    /** 
     * Returns the operation type.
     *
     * @return  int representing operation type
     */
    public int getOperationType() {
	    return mOperationType ;
    }

    /**
     * Gets the Policy where the element became
     * mandatory. 
     *
     * @return   the Policy where the element became mandatory 	
     */
    public Policy getOriginOfMandatory() {
	    return mOriginOfMandatory;
    }

    /**
     * Returns the package name.
     *
     * @return      package name
     */
    public String getPackage() { return mPackage ; }

    /**
     * Returns the parent node.
     *
     * @return parent node, or null if there is none
     */
    public PolicyNode getParent() { return mParent ; }

    /**
     * Returns the <code>PolicyTreeImpl</code> object.
     *
     * @return  policy tree object 
     */
    public PolicyTreeImpl getPolicyTree() { 
        return mPolicyTree; 
    }

    /**
      * Returns a boolean indicating if the element has
      * been modified. 
      *
      * @return    <code>true</code> if the element has been
      *            modified, otherwise <code>false</code>
      */
    public abstract boolean hasBeenModified() ;

    /**
     * Returns the setting of the flag indicating
     * whether or not the element has been added at the 
     * toplevel layer.
     *
     * @return		<code>true</code> if added
     *              at toplevel layer,
     *			    otherwise <code>false</code>
     */
    public boolean isAddedAtTopLayer() { return mIsAddedAtTopLayer; }


    /**
     * Returns the setting of the mandatory flag, indicating
     * whether or not the element may be removed or replaced. 
     *
     * @return		  <code>true</code> if node is
     *			      mandatory, otherwise <code>false</code>
     */
    public boolean isMandatory() {
	    return mIsMandatory;
    }

    /**
     * Sets the flag to true, indicating that this property 
     * was added by the toplevel layer.
     */
    public void setAddedAtTopLayer() {
	    mIsAddedAtTopLayer = true;
    }

    /**
     * Sets the dynamic flag to true.
     */
    public void setDynamic() {
	   mIsDynamic = true;
    }


    /**
     * Sets the mandatory flag, without carrying out any checks on
     * the node.
     */
    public void setMandatoryFlag() { mIsMandatory = true; }

    /**
     * Sets the name of the element from a string. 
     *
     * @param aName    name of the element
     */
    public void setName(String aName) { mName = aName ;}

    /** 
     * Sets the operation type.
     *
     * @param aOperationType  int representing operation type
     */
    public void setOperationType(int aOperationType) {
	    mOperationType = aOperationType;
    }

    /** 
     * Sets the layer where the mandatory
     * flag was set. 
     *
     * @param aSourceLayer    layer where flag is set
     */
    public void setOriginOfMandatory(Policy aSourceLayer) {
	    mOriginOfMandatory = aSourceLayer;
    }

    /**
     * Sets the package name. 
     *
     * @param aPackage     package name
     */
    public void setPackage(String aPackage) { mPackage = aPackage ; }


    /**
     * Sets the parent node. 
     *
     * @param aParent	parent node
     */
    public void setParent(PolicyNodeImpl aParent) { mParent = aParent ; }

    /**
     * Sets the element's path. 
     *
     * @param aPath    the element's path
     */
    public void setPath(String aPath) { mPath = aPath; }

    /**
     * Sets the PolicyTree object.
     *
     * @param aPolicyTree  policy tree object 
     */
    public void setPolicyTree(PolicyTreeImpl aPolicyTree) { 
        mPolicyTree = aPolicyTree ; 
    }

    /**
     * Returns a copy of the element.
     *
     * @return      copy of the element
     * @throws ElementCopyException if cannot create copy
     */
    public ConfigElementImpl shallowCopy() throws SPIException {
	    ConfigElementImpl returnCode = null;
	    try {
            returnCode = (ConfigElementImpl)(getClass().newInstance());
	    } catch (Exception e) {
	        String elementType = "ProtectedElement";
	        if (this instanceof PolicyNode) {
	            elementType = "PolicyNode";
	        }
	        else if (this instanceof Property) {
	            elementType = "Property";
	        }
	        throw new ElementCopyException(elementType, getName(), e);
	    }   
	    copySettings(returnCode);
        return returnCode;
    }
}
