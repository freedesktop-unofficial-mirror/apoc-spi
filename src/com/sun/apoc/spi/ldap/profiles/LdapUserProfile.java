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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.User;
import com.sun.apoc.spi.ldap.datastore.LdapDataStore ;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.ldap.util.Timestamp ;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;

import netscape.ldap.util.LDIF ;

/**
 *
 *
 */
public class LdapUserProfile implements Profile {
    
    /** Constants to generate the Policy data */
    private static final String COMPONENT_PKG= "com.sun.jds";
    private static final String COMPONENT_NAME= "UserProfile";
    private static final String NODE_NAME= "Data";
    
    /** Constants to initialise the Policy */
    private static final String ID = "ou=UserProfile,";
    private static final String DISPLAY_NAME = " User Profile";
    private static final String POLICY_ID = COMPONENT_PKG+"."+COMPONENT_NAME;
    
    private String mId;
    private String mDisplayName = "";
    private Policy mPolicy = null;
    
    public LdapUserProfile(User user, Hashtable ldapAttributes) {
        mId = ID+user.getId();
        String data = getPolicyData(ldapAttributes);
        byte [] timestamp = 
            (byte []) ldapAttributes.get(LdapDataStore.MODIFY_TIMESTAMP_ATTR) ;

        if (timestamp != null) {
            long lastModification = Timestamp.getMillis(new String(timestamp)) ;

            mPolicy = new Policy(POLICY_ID, mId, data, lastModification) ;
        }
        else {
            mPolicy = new Policy(POLICY_ID, mId, data);
        }
        mDisplayName = user.getDisplayName(null)+DISPLAY_NAME;
}

    /**
     * Returns the policy object corresponding to the user profile.
     *
     * @return      the policy object
     * @throws      <code>SPIException</code> if error occurs 
     */
    public Policy getPolicy() throws SPIException {
        return mPolicy;
    }
    
    /** Header and trailer of the XML blob corresponding to the user profile. */
    private static final String XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
        "<oor:component-data " +
        "xmlns:oor=\"http://openoffice.org/2001/registry\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
        "oor:package=\"" + COMPONENT_PKG + "\" oor:name=\"" + COMPONENT_NAME +
        "\"><node oor:name=\"" + NODE_NAME + "\">" ;
    private static final String XML_TRAILER = "</node></oor:component-data>" ;
    /**
     * Generate the Policy blob from the Ldap attributes of the User
     * and the attributes mapping
     * 
     * @param ldapAttributes	Ldap attributes and their values
     * @return the Policy blob
     */
    private static String getPolicyData(Hashtable ldapAttributes) {
        StringBuffer data = new StringBuffer(XML_HEADER);
        Enumeration attributes = ldapAttributes.keys() ;

        while (attributes.hasMoreElements()) {
            String attribute = (String) attributes.nextElement() ;
            
            data.append(
                    getPropertyData(attribute, 
                                    (byte []) ldapAttributes.get(attribute))) ;
        }
        data.append(XML_TRAILER) ;
        return data.toString() ;
    }
    
    /** XML Strings used to build the prop node for a user profile attribute. */
    private static final String PROP_HEADER = "<prop oor:name=\"" ;
    private static final String PROP_MIDDLE = 
                                        "\" oor:type=\"xs:string\"><value>" ;  
    private static final String PROP_TRAILER = "</value></prop>" ;
    /**
     * returns the property data generated for a couple (key, value)
     * 
     * @param key		key of the property
     * @param value		value of the property
     * @return			property data generated
     */
    private static StringBuffer getPropertyData(String key, byte [] value) {
        StringBuffer data = new StringBuffer();

        if (value != null && value.length > 0 && LDIF.isPrintable(value)) {
            data.append(PROP_HEADER) ;
            data.append(key).append(PROP_MIDDLE) ;
            int lastValid = 0 ;
            
            for (int i = 0 ; i < value.length ; ++ i) {
                byte current = value [i] ;
                boolean special = current == '&' || 
                                  current == '<' || current == '>' ;

                if (special) {
                    if (i != lastValid) { 
                        data.append(new String(value, lastValid, 
                                               i - lastValid)) ;
                    }
                    if (current == '&') { data.append("&amp;") ; }
                    else if (current == '<') { data.append("&lt;") ; }
                    else if (current == '>') { data.append("&gt;") ; }
                    lastValid = i + 1 ;
                }
            }
            if (lastValid != value.length) {
                data.append(new String(value, lastValid, 
                                       value.length - lastValid)) ;
            }
            data.append(PROP_TRAILER) ;
        }
        return data;
    }
    
    /**
     * Returns the id for this profile.
     *
     * @return             id for this profile
     */
    public String getId() {
        return mId;
    }

    /**
     * Returns the display name for this profile.
     *
     * @return             display name for this profile
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Returns the priority for this profile.
     *
     * @return	0
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Returns the scope for this profile.
     *
     * @return	Applicability.USER 
     */
    public Applicability getApplicability() {
        return Applicability.USER;
    }

    /** 
     * Returns the author for the last modification of this 
     * profile.
     *
     * @return	empty String
     * @throws    <code>SPIException</code> if error occurs 
     */
    public String getAuthor() throws SPIException {
        return new String("");
    }
    /**
     * Returns the comment for this profile.
     *
     * @return	empty String
     * @throws  SPIException if error occurs 
     */
    public String getComment() throws SPIException{
        return new String("");
    }

    /**
      * Returns the time of the last modification of the profile.
      *
      * @return    time of the creation of the Policy in milliseconds 
      * @throws    SPIException if error occurs           
      */
    public long getLastModified() throws SPIException {
        return mPolicy.getLastModified();
    }

    /**
     * Returns a boolean indicating whether or not this profile
     * has policies. 
     *
     * @return   <code>true</code> if there are policies, 
     *           otherwise <code>false</code>
     * @throws   <code>SPIException</code> if error occurs
     */
    public boolean hasPolicies() throws SPIException {
        return (mPolicy != null);
    }

    /**
     * Returns the policies for this profile.
     *
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicies() throws SPIException {
        Vector policies = new Vector();
        policies.add(mPolicy);
        return policies.iterator();
    }

    /**
     * Returns the policies for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicies(Iterator aPolicyIdList) 
        throws SPIException {
        Vector policies = new Vector();
        String thePolicyId = mPolicy.getId();
        while (aPolicyIdList.hasNext()) {
            String policyId = (String)aPolicyIdList.next();
            if (policyId.equals(thePolicyId)) {
                policies.add(mPolicy);
                break;
            }
        }
        return policies.iterator();
    }

    /**
     * Returns the PolicyInfos for this profile that match the specified 
     * policy ids.
     *
     * @param aPolicyIdList  list of policy ids
     * @return               <code>Iterator</code> of all the PolicyInfos 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Iterator getPolicyInfos(Iterator aPolicyIdList) 
        throws SPIException {
        return getPolicies(aPolicyIdList);
    }

    /**
     * Returns the requested policy object.
     *
     * @param aId   the id for the required policy
     * @return      the policy object or null if no policy
     * 				with aId exists
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Policy getPolicy(String aId) throws SPIException {
        if (aId.equals(mPolicy.getId())) {
            return mPolicy;
        }
        return null;
    }

    public ProfileRepository getProfileRepository() {
        return null;
    }
    
    public void storePolicy(Policy aPolicy) throws SPIException {
        throw new UnsupportedOperationException();
    }

    public void destroyPolicy(Policy aPolicy) throws SPIException {
        throw new UnsupportedOperationException();
    }
 
    public boolean hasAssignedEntities() throws SPIException {
        throw new UnsupportedOperationException();
    }

    public Iterator getAssignedEntities() throws SPIException {
        throw new UnsupportedOperationException();
    }

    public void setApplicability(Applicability aApplicability) 
    	throws SPIException {
            throw new UnsupportedOperationException();
        }

    public void setComment(String aComment) throws SPIException {
        throw new UnsupportedOperationException();
    }

    public void setPriority(int aPriority) throws SPIException {
        throw new UnsupportedOperationException();
    }

    public void setDisplayName(String aDisplayName) throws SPIException {
        throw new UnsupportedOperationException();
    }
}
