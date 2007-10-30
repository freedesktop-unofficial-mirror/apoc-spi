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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.apoc.spi.cfgtree.policynode.PolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.policynode.ReadWritePolicyNodeImpl;
import com.sun.apoc.spi.cfgtree.property.PropertyImpl;
import com.sun.apoc.spi.cfgtree.property.ReadWritePropertyImpl;
import com.sun.apoc.spi.cfgtree.readwrite.ReadWritePolicyTreeImpl;
import com.sun.apoc.spi.policies.Policy;

/**
  * Used to provide constants and utilities related to the 
  * parsing of the schema and the output of nodes.
  *
  */
public class NodeParsing extends DefaultHandler
{
    /** Namespace for the OOR attributes */
    public static final String OOR_NAMESPACE = "oor:" ;
    /** Namespace for the XML attributes */
    public static final String XML_NAMESPACE = "xml:" ;
    /** Namespace for the XSI attributes */
    public static final String XSI_NAMESPACE = "xsi:" ;
    /** XML Namespace */
    public static final String XMLNS_NAMESPACE = "xmlns:" ;
    /** xmlns:oor attribute used in component schema, 
      * as in xmlns:oor="http://openoffice.org.policy" */
    public static final String XMLNSOOR_ATTR = XMLNS_NAMESPACE + "oor" ;
    /** xmlns:xs attribute used in component schema, 
      * as in xmlns:xs="http://www.w3.org/2001/XMLSchema" */
    public static final String XMLNSXS_ATTR = XMLNS_NAMESPACE + "xs" ;
    /** xmlns:xsi attribute used in component schema, 
      * as in xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" */
    public static final String XMLNSXSI_ATTR = XMLNS_NAMESPACE + "xsi" ;
    /** Name attribute, 
      * as in &lt;group <b>oor:name</b>="groupName"/&gt; */
    public static final String NAME_ATTR = OOR_NAMESPACE + "name" ;
    /** Value type attribute,
      * as in &lt;prop <b>oor:type</b>="xs:string"/&gt; */
    public static final String TYPE_ATTR = OOR_NAMESPACE + "type" ;
    /** Locale attribute,
      * as in &lt;value <b>xml:lang</b>="en-US"/&gt; */
    public static final String LOCALE_ATTR = XML_NAMESPACE + "lang" ;
    /** Separator attribute,
      * as in &lt;value <b>oor:separator</b>=" "/&gt; */
    public static final String SEPARATOR_ATTR = OOR_NAMESPACE + "separator" ;
    /** Localized attribute,
	* as in &lt;prop <b>oor:localized</b>="true"/&gt; */
    public static final String LOCALIZED_ATTR = OOR_NAMESPACE + "localized" ;
    /** Component attribute,
      * as in &lt;import <b>oor:component</b>="someComponent"/&gt; */
    public static final String COMPONENT_ATTR = OOR_NAMESPACE + "component" ;
    /** Package attribute,
      * as in &lt;component-schema <b>oor:package</b>="somePackage"/&gt; */
    public static final String PACKAGE_ATTR = OOR_NAMESPACE + "package" ;
    /** Node-type (set template) attribute,
      * as in &lt;set <b>oor:node-type</b>="templateName"/&gt; */
    public static final String NODETYPE_ATTR = OOR_NAMESPACE + "node-type" ;
    /** Context attribute,
      * as in &lt;group <b>oor:context</b>="somePackage/someSubNode"/&gt; */
    public static final String CONTEXT_ATTR = OOR_NAMESPACE + "context" ;
    /** Operation attribute,
      * as in &lt;group <b>oor:op</b>="remove"/&gt; */
    public static final String OPERATION_ATTR = OOR_NAMESPACE + "op" ;
    /** Nillable attribute,
      * as in &lt;prop <b>oor:nillable</b>="true"/&gt; */
    public static final String NILLABLE_ATTR = OOR_NAMESPACE + "nillable" ;
    /** Finalized attribute,
      * as in &lt;prop <b>oor:finalized</b>="true"/&gt; */
    public static final String FINALIZED_ATTR = OOR_NAMESPACE + "finalized" ;
    /** Read-only attribute,
      * as in &lt;prop <b>oor:readonly</b>="true"/&gt; */
    public static final String READONLY_ATTR = OOR_NAMESPACE + "readonly" ;
    /** Mandatory attribute,
      * as in &lt;group <b>oor:mandatory</b>="true"/&gt; */
    public static final String MANDATORY_ATTR = OOR_NAMESPACE + "mandatory" ;
    /** Value attribute,
      * as in &lt;group <b>oor:value</b>="someValue"/&gt; */
    public static final String VALUE_ATTR = OOR_NAMESPACE + "value" ;
    /** Nil attribute (for value node),
      * as in &lt;prop <b>xsi:nil</b>="true"/&gt; */
    public static final String NIL_ATTR = XSI_NAMESPACE + "nil" ;
    /** whitespace */
    public static final String WHITESPACE = " ";
    public static final String NODE_TAG = "node";
    public static final String PROP_TAG = "prop";
    public static final String VALUE_TAG = "value";
    public static final String COMPONENT_DATA_TAG = "component-data" ;

    /** used when creating a NodeValueImpl to indicate which layer was
        the source of this value */
    private  Policy mSourceLayer;
    /** id of policy being read */
    private String mPolicyId;
    /** indicates what locale is required (as specified by java proxy).  
        Needed for PropertyImpl to return correct NodeValueImpl(s) for 
	specified locale(s) */
    private String mLocale ;
    /** Locator object used for traces */
    private Locator mLocator ;
    /** Stack containing the parsing context, i.e the elements
      * encountered during parsing */
    private Stack mContextStack = new Stack() ;
    /** Root object of the tree parsed */
    private Object mRoot ;
    /** reference to the PolicyTree instance */
    private PolicyTreeImpl mPolicyTree;
    /** AttributesImpl object for reuse */
    private static Attributes mNodeParsingAttributes = new AttributesImpl(); 
    /** StringBuffer used for creating concatanated strings */
    private StringBuffer tmpBuf = 
		new StringBuffer(PolicyTree.BUFFER_SIZE);
	   
	    private static class ParseInfo
	    {
		Object mObject = null ;
		Attributes mAttributes = null ;

		ParseInfo(Object aObject, 
			  Attributes aAttributes) {
		    mObject = aObject ; 
		    if (aAttributes != null) {
			((AttributesImpl)mNodeParsingAttributes).clear();
			mAttributes = mNodeParsingAttributes ;
			((AttributesImpl)mAttributes).setAttributes(aAttributes) ;
		    }
		}
	    } ;
    /**
      * Returns the root object.
      * 
      * @return     the root object
      */
    public Object getRoot() { return mRoot; }
    
    /**
     * Sets the value of the layer that is the source of
     * this data. 
     *
     * @param aLayer     name of the layer that is the source of 
     *                   this data        
     */ 
    public void setSourceLayer(Policy aLayer) {
        mSourceLayer = aLayer; 
    }

    /**
     * Sets the id of the policy being read.  
     *
     * @param aPolicyId  id of the policy
     */
    public void setPolicyId(String aPolicyId) {
        mPolicyId = aPolicyId;
    }
    
    /**
     * Sets the reference to the <code>PolicyTreeImpl</code> instance.  
     *
     * @param aPolicyTree  reference to PolicyTree instance
     */
    public void setPolicyTree(PolicyTreeImpl aPolicyTree) {
        mPolicyTree = aPolicyTree;
    }

    /**
     * Sets the value for the locale. 
     *
     * @param aLocale    specified locale 
     */
    public void setLocale(String aLocale) { mLocale = aLocale; }

    /**
     * Returns the value for the locale. 
     *
     * @return    specified locale 
     */
    protected String getLocale() { return mLocale; }

    /** 
     * Sets the Locator to be used during the parsing.
     *
     * @param aLocator locator object
     */
    public void setDocumentLocator(Locator aLocator) { 
        mLocator = aLocator ; 
    }
 
    /**
     * If the mandatory attribute is set then sets that boolean
     * member of the node, and sets the profile where 
     * this attribute originated.
     *
     * @param aNode	 container of the mandatory property
     * @param aValue	 string value of the mandatory property
     */
    private void setParsedMandatory(PolicyNodeImpl aNode, String aValue) {
        boolean mandatory = (Boolean.valueOf(aValue)).booleanValue();
	    if (mandatory) {
	        aNode.setMandatoryFlag();
	        aNode.setOriginOfMandatory(mSourceLayer);
	    }
    }

    /**
     * If the mandatory attribute is set then sets that boolean
     * member of the property, and sets the profile 
     * where this attribute originated.
     *
     * @param aNode	 container of the mandatory property
     * @param aValue	 string value of the mandatory property
     */
    private void setParsedMandatory(PropertyImpl aProperty, String aValue) {
        boolean mandatory = (Boolean.valueOf(aValue)).booleanValue();
	    if (mandatory) {
	        aProperty.setMandatoryFlag();
	        aProperty.setOriginOfMandatory(mSourceLayer);
	    }
    }

    /**
      * Fills the properties of an element with the contents
      * of the XML attributes.
      *
      * @param aElement     element to fill
      * @param aAttributes  attributes of the XML element
      */
    private void handleProperties(ConfigElementImpl aElement, 
					 			  Attributes aAttributes) {
	    String attrValue = null;
	    attrValue =  aAttributes.getValue(OPERATION_ATTR);
	    if (attrValue != null) {
	        aElement.setOperationType(OperationType.getInt(attrValue));
	    }
	    attrValue =  aAttributes.getValue(MANDATORY_ATTR);
	    if (attrValue != null) {
	        setParsedMandatory((PolicyNodeImpl)aElement, attrValue);
	    }
	    attrValue =  aAttributes.getValue(PACKAGE_ATTR);
	    if (attrValue != null) {
	        aElement.setPackage(attrValue);
	    }
	    attrValue =  aAttributes.getValue(TYPE_ATTR);
	    if (attrValue != null) {
	        ((PropertyImpl)aElement).setDataType(DataType.getDataType(attrValue));
	    }
	    attrValue =  aAttributes.getValue(NILLABLE_ATTR);
	    if (attrValue != null) {
	        ((PropertyImpl)aElement).setNillable((Boolean.valueOf(attrValue)).booleanValue());
	    }
    }

    /**
      * Builds an element corresponding to the description of an
      * element.
      *
      * @param aNamespace   namespace URI
      * @param aLocalName   local name
      * @param aFullName    full name with prefix
      * @param aAttributes  element attributes
      * @return ParseInfo describing the node
      */
    private ParseInfo createElement(String aNamespace, String aLocalName,
			 						String aFullName, 
			 						Attributes aAttributes) {
        if (mPolicyTree instanceof ReadWritePolicyTreeImpl) {
            return createReadWriteElement(aNamespace, aLocalName,
                    					  aFullName, aAttributes);
        } else {
            return createReadWriteElement(aNamespace, aLocalName,
                    					  aFullName, aAttributes);
        }
    }

    /**
      * Builds a readwrite element corresponding to the description of 
      * the element.
      *
      * @param aNamespace   namespace URI
      * @param aLocalName   local name
      * @param aFullName    full name with prefix
      * @param aAttributes  element attributes
      * @return ParseInfo describing the node
      */
    private ParseInfo createReadWriteElement(String aNamespace, 
            String aLocalName, String aFullName, Attributes aAttributes) {
	    ConfigElementImpl node = null ;
        if (aLocalName.equals(NODE_TAG) ||
            aLocalName.equals(COMPONENT_DATA_TAG)) {
		    node = new ReadWritePolicyNodeImpl();
	        ((PolicyNodeImpl)node).setOrigin(mSourceLayer);
	        String attrValue =  aAttributes.getValue(FINALIZED_ATTR);
	        if (attrValue != null) {
	            ((PolicyNodeImpl)node).setFinalized(
                     (Boolean.valueOf(attrValue)).booleanValue(),
                     null, mSourceLayer);
	        }
	    } else if (aLocalName.equals(PROP_TAG)) {
		    node = new ReadWritePropertyImpl();
            ((PropertyImpl)node).setRequiredLocale(mLocale);
	        ((PropertyImpl)node).setOrigin(mSourceLayer);
	        String attrValue =  aAttributes.getValue(FINALIZED_ATTR);
	        if (attrValue != null) {
	            ((PropertyImpl)node).setFinalized(
                     (Boolean.valueOf(attrValue)).booleanValue(),
                     null, mSourceLayer);
	        }
        }
        if (node == null) { return null; }
	    node.setPolicyTree(mPolicyTree);
	    if (node.getName() == null) {
	        node.setName(aAttributes.getValue(NAME_ATTR)) ;
	    }
	    if (!mContextStack.empty()) {
	        ParseInfo parent = (ParseInfo) mContextStack.peek() ;
	        if (parent != null) {
                if (node instanceof PolicyNodeImpl) {
		            ((ReadWritePolicyNodeImpl) parent.mObject).addChildNode((PolicyNodeImpl)node) ;
                } else {
		            ((ReadWritePolicyNodeImpl) parent.mObject).addProperty((PropertyImpl)node) ;
                }

	        }
        }
	    handleProperties(node, aAttributes) ;
	    return new ParseInfo(node, aAttributes) ;
    }
        
    /**
      * Creates a NodeValueImpl object from the attributes of an element.
      *
      * @param aAttributes  element's attributes
      * @return            <code>ParseInfo</code> describing the value object
      */
    private ParseInfo createValue(Attributes aAttributes) {
        if (mContextStack.empty()) { return null ; }
        NodeValueImpl value = new NodeValueImpl() ;
        ParseInfo parent = (ParseInfo) mContextStack.peek() ;
        Attributes parentAttr = parent.mAttributes ;
	    int length = parentAttr.getLength();
        /* check if xsi:nil attribute is set */
        String nilValue = aAttributes.getValue(NIL_ATTR);
        if (nilValue != null && nilValue.equals("true")) {
            value.setNilAttribute(true);
	    }
        String localeName = (String)aAttributes.getValue(LOCALE_ATTR) ;
        if (localeName != null) {
            value.setLocaleName(localeName) ;
        } 
        String typeAttr = (String)parentAttr.getValue(TYPE_ATTR);
	    DataType dataType = DataType.UNKNOWN;
        if (typeAttr != null) {
	    dataType = DataType.getDataType(typeAttr);
            value.setDataType(dataType);
        }
        String separator = aAttributes.getValue(SEPARATOR_ATTR);
        if (separator != null) {
            ((PropertyImpl) parent.mObject).setSeparator(separator);
        } else {
            /* if this is a list, and separator is not 
               specified, then the default separator
	           is a whitespace */
            if (dataType.getIntValue() >=
				DataType.FIRST_LIST_ELEMENT) {
                ((PropertyImpl) parent.mObject).setSeparator(WHITESPACE);
            }
        } 
	    try {
            ((PropertyImpl) parent.mObject).setParsedValue(
		    value, localeName) ;
	    } catch (Exception ignoreException) { }
	    value.setOrigin(mSourceLayer);
        return new ParseInfo(value, aAttributes) ;
    }


    /**
     * Handles the beginning of an element.
     * Creates a new node and pushes it on the context stack.
     *
     * @param aNamespace   namespace URI
     * @param aLocalName   local name
     * @param aFullName    full name with prefix
     * @param aAttributes  attributes of the element
     */
    public void startElement(String aNamespace, String aLocalName, 
                             String aFullName, Attributes aAttributes) {
        ParseInfo stackedObject = null ;
	    if (aLocalName.equals(VALUE_TAG)) {
            stackedObject = createValue(aAttributes) ; 
	    } else {
            // Otherwise create a node.
            stackedObject = createElement(aNamespace, aLocalName,
                                       aFullName, aAttributes) ;
        }
        /* Anyway, always put something on the stack so that the
           endElement doesn't have to check for the special cases. */
        mContextStack.push(stackedObject) ;
    }

    /**
     * Handles the end of an element.
     * Gets one step up in the context stack.
     *
     * @param aNamespace   namespace URI
     * @param aLocalName   element name
     * @param aFullName    full name including prefix
     */
    public void endElement(String aNamespace, String aLocalName,
			   			   String aFullName) {
	    Object stackObj = mContextStack.pop() ;
	    if (stackObj != null) {
	        mRoot = ((ParseInfo) stackObj).mObject ;
	    }
    }

    /**
     * Handles a character string (text data).
     * Fills the appropriate NodeValueImpl object.
     *
     * @param aCharacters  character array
     * @param aStart       beginning of the data
     * @param aLength      length of the data
     */
    public void characters(char aCharacters [], int aStart, int aLength) {
	    String value = new String(aCharacters, aStart, aLength) ;
	    //System.out.println("Characters:" + value) ;
	    if (mContextStack.empty()) { return; }	
	    ParseInfo parent = (ParseInfo) mContextStack.peek() ;
	    if (parent == null) { return; }
	    if(parent.mObject != null && 
	        parent.mObject instanceof com.sun.apoc.spi.cfgtree.NodeValueImpl) {
		    ((NodeValueImpl) parent.mObject).appendContents(value) ;
	    } 
    }
}
