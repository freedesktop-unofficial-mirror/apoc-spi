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

package com.sun.apoc.spi.ldap.datastore;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPAttributeSet;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPDN;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPModification;
import netscape.ldap.LDAPModificationSet;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.LDAPv2;

import com.sun.apoc.spi.IllegalReadException;
import com.sun.apoc.spi.IllegalWriteException;
import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.NoSuchEntityException;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.ldap.LdapClientContext;
import com.sun.apoc.spi.ldap.entities.LdapDomain;
import com.sun.apoc.spi.ldap.entities.LdapEntity;
import com.sun.apoc.spi.ldap.entities.LdapEntityType;
import com.sun.apoc.spi.ldap.entities.LdapNode;
import com.sun.apoc.spi.ldap.entities.LdapOrganization;
import com.sun.apoc.spi.ldap.entities.LdapRole;
import com.sun.apoc.spi.ldap.entities.mapping.LdapEntityMapping;
import com.sun.apoc.spi.ldap.policies.LdapPolicy;
import com.sun.apoc.spi.ldap.profiles.LdapProfile;
import com.sun.apoc.spi.ldap.profiles.LdapProfileComparator;
import com.sun.apoc.spi.ldap.profiles.LdapProfileRepository;
import com.sun.apoc.spi.ldap.util.Timestamp;
import com.sun.apoc.spi.policies.NoSuchPolicyException;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.policies.PolicyInfo;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.InvalidPriorityException;
import com.sun.apoc.spi.profiles.InvalidProfileException;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileImpl;
import com.sun.apoc.spi.profiles.ProfileRepository;
import com.sun.apoc.spi.profiles.ProfileRepositoryImpl;
import com.sun.apoc.spi.util.BooleanReturnValue;
import com.sun.apoc.spi.util.MetaConfiguration;

/**
  * Handles the access to data stored in an LDAP storage.
  */
public class LdapDataStore
{
    /** set length for buffers used for building strings */
    public static final int BUFFER_LENGTH = 200;
    /** Constant indicating  recursive search required */
    public static final boolean RECURSIVE_SEARCH = true;
    /** Constant indicating  non-recursive search required */
    public static final boolean NON_RECURSIVE_SEARCH = false;

    private float versionNb = 1.0f;
    private boolean isVersion1 = true;
    private LdapOrganization mRootOrganization;
    private LdapDomain mRootDomain;
    /** service entry buffer*/
    public StringBuffer mServiceEntryDNBuf = null ;
    /** buffer for root service entry */
    public StringBuffer mRootServiceEntryDNBuf = 
			new StringBuffer(BUFFER_LENGTH);

    /** key to Version in MetaConfiguration */
    static final String VERSION_KEY = "ApocVersion";
    
    /** strings describing elements of the service entry */
    public static final String SERVICES 		  = "services";
    public static final String SERVICE_APOC 	  = "ApocService";
    public static final String SERVICE_ORG_CONFIG = "OrganizationConfig";
    public static final String SERVICE_VERSION 	  = "1.0";
    public static final String SERVICE_DEFAULT 	  = "default";
    public static final String SERVICE_REGISTRY   = "ApocRegistry";
    /** number of service elements */
    public static final int NUMBER_OF_SERVICE_MAPPING_ELEMENTS = 6;
    /** Array describing the object classes of the service entry. */
    public static ServiceMapping [] SERVICE_MAPPING_ELEMENTS = 
            new ServiceMapping [NUMBER_OF_SERVICE_MAPPING_ELEMENTS] ;
    /** mapping for object class and attribute for profile container */
    private static EntryMapping CONTAINER_MAPPING;
    /** mapping for object class and attribute for profile  */
    private static EntryMapping PROFILE_MAPPING;
    /** mapping for object class and attribute for policy  */
    private static EntryMapping POLICY_MAPPING;
    /** Key used to store DN value in search results */
    public static final String DN_KEY = "DN";
    /** name for an LDAP object class */
    public static final String LDAP_OBJCLASS = "objectclass" ;
    /** name for the LDAP organizational unit object class */
    public static final String ORG_UNIT_OBJCLASS = "organizationalunit" ;
    /** name for the LDAP organizational unit naming attribute */
    public static final String ORG_UNIT_NAMING_ATTR = "ou" ;
    public static final String CONFIG_NAMING_ATTR= ORG_UNIT_NAMING_ATTR + "=";
    /** name of the sunservice object class */
    private static final String SUNSERVICE_OBJCLASS = "sunservice";
    /** name of the sunservice component object class */
    public static final String SUNSERVICE_COMPONENT_OBJCLASS = 
        	"sunservicecomponent";
    /** name of the attribute used to store id value */
    private static final String SUNSERVICEID_ATTR = "sunserviceid";
    /** value of the sunserviceid for profiles */
    private static final String SUNSERVICEID_PROFILE = "ApocPolicyGroup";
    /** value of the sunserviceid for policies */
    private static final String SUNSERVICEID_POLICY = "ApocPolicy";
    /** name of the attribute used to store key/value data */
    public static final String KEYVALUE_ATTR = "sunkeyvalue";
    /** name of the attribute used to store profile priority */
    private static final String PRIORITY_ATTR = "sunsmspriority"; 
    /** name of attribute storing time of last modification of entry */
    public static final String MODIFY_TIMESTAMP_ATTR = 
			"modifytimestamp";
    /** name of attribute storing DN of last modifier of entry */
    private static final String MODIFY_AUTHOR_ATTR = 
			"modifiersname";
    /** name of applicability key for the sunkeyvalue attribute */
    private static final String APPLICABILITY_KEY = "applicability=";
    /** name of key for xmlblobs for the sunkeyvalue attribute */
    private static final String APOC_BLOB_KEY = "APOCBlob=";
    /** name of display name key for the sunkeyvalue attribute */
    private static final String DISPLAY_NAME_KEY = "displayname=";
    /** name of organization mapping key for the sunkeyvalue attribute */
    public static final String ORG_MAP_KEY = "organizationalmapping=";
    /** name of user profile mapping key for the sunkeyvalue attribute */
    public static final String LDAP_ATTR_MAP_KEY = "ldapattributemapping=";
    /** name of key for assigning profiles to an entity using
        the sunkeyvalue attribute */
    private static final String ASSIGNED_KEY = "assigned=";
    /** name of key for adding comment to profile using 
        the sunkeyvalue attribute */
    private static final String COMMENT_KEY = "comment=";
    /** value of keyvalue attribute storing profile applicability 
        with value user */
    private static final String USER_PROFILE_USE_VALUE = 
		APPLICABILITY_KEY + Applicability.USER;
    /** value of keyvalue attribute storing profile applicability 
        with value host */
    private static final String HOST_PROFILE_USE_VALUE =
		APPLICABILITY_KEY + Applicability.HOST;
    /** PolicyIdParser object  */
    PolicyIdParser mPolicyIdParser = new PolicyIdParser();
    /** wildcard for LDAP searchs */
    public static final String LDAP_WILDCARD = "*";
    /** LDAP repository base DN. */
    private String mBaseDN = null ;
    /** Version of the base DN as retrieved from the LDAP server. **/
    private String mRootDN = null ;

	
    /** class for mapping entry to required objectclass
        and attribute */
    static class EntryMapping {
		String mObjectClass = null;
		LDAPAttribute mServiceIdAttr = null;
	
        EntryMapping(String aObjectClass) {
            mObjectClass = aObjectClass;
		}
        
        EntryMapping(String aObjectClass, String aServiceIdValue) {
		    mObjectClass = aObjectClass;
		    mServiceIdAttr = new LDAPAttribute(SUNSERVICEID_ATTR, 
		           							   aServiceIdValue);
		}
    }

    /** class for mapping service entry to required objectclass, 
        RDN and attribute */
    public static class ServiceMapping {
		String mEntryRDN = null;
		LDAPAttributeSet mAttrSet = new LDAPAttributeSet();
	
        ServiceMapping(String aObjectClass, String aEntryRDN) {
		    mAttrSet.add(new LDAPAttribute(LDAP_OBJCLASS, aObjectClass));
		    mEntryRDN = aEntryRDN;
		}
        
        ServiceMapping(String aObjectClass, String aServiceIdValue,
                	   String aEntryRDN) {
		   mAttrSet.add(new LDAPAttribute(LDAP_OBJCLASS, aObjectClass));
		   mAttrSet.add(new LDAPAttribute(SUNSERVICEID_ATTR, aServiceIdValue));
		   mEntryRDN = aEntryRDN;
		}
        
        public String getRDN() { return mEntryRDN; }
    }

    /** 
      * Static constructor, initialises the list of objectclasses and 
      * attriubtes for the service entry and the profile container,
      * profile and policy entries.
      */
    static {
        int i = 0;
        // ou=ApocRegistry 
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			SUNSERVICE_COMPONENT_OBJCLASS,
			SERVICE_REGISTRY,
			CONFIG_NAMING_ATTR + SERVICE_REGISTRY);
        // ou=default 
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			SUNSERVICE_COMPONENT_OBJCLASS,
			CONFIG_NAMING_ATTR + SERVICE_DEFAULT);
        // ou=OrganizationConfig
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			 ORG_UNIT_OBJCLASS,
			 CONFIG_NAMING_ATTR + SERVICE_ORG_CONFIG);
        // ou=1.0
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			SUNSERVICE_OBJCLASS,
			CONFIG_NAMING_ATTR + SERVICE_VERSION);
        // ou=ApocService 
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			SUNSERVICE_OBJCLASS,
			CONFIG_NAMING_ATTR + SERVICE_APOC);
        // ou=Services
        SERVICE_MAPPING_ELEMENTS [i++] = new ServiceMapping(
			ORG_UNIT_OBJCLASS,
			CONFIG_NAMING_ATTR + SERVICES);
	    CONTAINER_MAPPING = new EntryMapping(SUNSERVICE_COMPONENT_OBJCLASS,
						"ApocPolicyGroupContainer");	
		PROFILE_MAPPING = new EntryMapping(SUNSERVICE_COMPONENT_OBJCLASS,
						SUNSERVICEID_PROFILE);	
		POLICY_MAPPING = new EntryMapping(SUNSERVICE_COMPONENT_OBJCLASS,
						SUNSERVICEID_POLICY);	
    }

    /**
     * Constructor
     *
     * @param aBaseEntry 	base entry in the LDAP datasource
     * @param aContext      client context
     * @throws                <code>SPIException</code>
     */
    public LdapDataStore(String aBaseEntry,
            			 LdapClientContext aContext)
    		throws SPIException {
        readBootstrapData(aBaseEntry);
    }
    
    public void setVersion(MetaConfiguration metaConfData) {        
        // get version from MetaConfiguration, default is 1.0
        String version = metaConfData.getString(VERSION_KEY);
        if (version != null) {
            try {
                versionNb = Float.parseFloat(version);
                isVersion1 = versionNb < 2.0f;
            } catch (NumberFormatException ignored) {}
        }
    }
    
    /**
     * returns if the MetaConfiguration comes from APOC 1
     * @return	true if MetaConfiguration comes from APOC 1
     */
    public boolean isVersion1() { return isVersion1; }

    /** 
     * Accessor for Base Distinguished Name. 
     *
     * @return      base DN
     */
     public String getBaseDN() { return mBaseDN; }

    /**
     * Accessor for Root Service Entry DN. 
     *
     * @return      DN for Root Service Entry
     */
    public String getRootServiceEntryDN() { 
        return mRootServiceEntryDNBuf.toString(); 
    }

    /**
     * Accessor for Services Entry DN. 
     *
     * @return       DN for Services Entry
     */
    public String getServiceEntryDN() { 
        return mServiceEntryDNBuf.toString(); 
    }

    /**
     * Extracts LDAP settings from bootstrapping source.  
     *
     * @param aBaseDN   DN for base entry 
     */
    private void readBootstrapData(String aBaseDN) {
        mBaseDN = aBaseDN;
	    mServiceEntryDNBuf = new StringBuffer(BUFFER_LENGTH) ;
	    for (int i = 0 ; i < NUMBER_OF_SERVICE_MAPPING_ELEMENTS ; ++ i) {
	        mServiceEntryDNBuf.append(SERVICE_MAPPING_ELEMENTS[i].mEntryRDN);
	        if (i + 1 < NUMBER_OF_SERVICE_MAPPING_ELEMENTS) {
	            mServiceEntryDNBuf.append(LdapEntity.LDAP_SEPARATOR) ;
	        }
	    }
		mRootServiceEntryDNBuf.append(mServiceEntryDNBuf).append(
	        LdapEntity.LDAP_SEPARATOR).append(mBaseDN);
    }

    /**
      * Returns a <code>Vector</code> listing the members of the specified type for
      * a role. The members returned are those within the same organizational subtree
      * as the role, and include any specified required attribute values.
      * If no such members are found then an empty <code>Vector</code> is returned.
      *
      * @param aRoleEntity      role whose members are required
      * @param aObjectClass     object class for member
      * @param aMemberAttribute attribute used by member to store role membership
      * @param aAttributes      member attributes required
      * @param aTypeOfSearch    if <code>true</code> then just checks if
      *                          there are children of this type, otherwise
      *                          returns the children
      * @param aReturnValue      if just checking if there are children then
      *                          this will indicate if there are childen,
      *                          <code>true</code>, or if there are none,
      *                          <code>false</code>
      * @return                  <code>Vector</code> detailing the members
      * @throws         <code>SPIException</code> if an error occurs
      */
      public Vector getRoleMembers(LdapRole aRole, String aObjectClass,
              					   String aMemberAttribute, 
              					   String[] aAttributes, 
              					   boolean aTypeOfSearch,
              					   BooleanReturnValue aReturnValue) 
      	throws SPIException {
          StringBuffer searchBuf = new StringBuffer(BUFFER_LENGTH);
          searchBuf.append("(&(");
          searchBuf.append(LDAP_OBJCLASS);
          searchBuf.append("=");
          searchBuf.append(aObjectClass);
          searchBuf.append(")(");
          searchBuf.append(aMemberAttribute);
          searchBuf.append("=");
          searchBuf.append(aRole.getLocation());
          searchBuf.append("))");
          Vector returnList = null;
          returnList = performSearch(
                ((LdapNode)aRole.getParentOrgOrDomain())
                		.getLocation(),
 	            RECURSIVE_SEARCH,
                searchBuf.toString(),
                aAttributes,
                true,
                aTypeOfSearch,
                aReturnValue,
                false,
                aRole.getContext());
          return returnList == null ? new Vector(): returnList;
     }

     
     /**
       * Utility function that searchs the datastore for a list
       * of children of a particular type for the specified entity.
       *
       * @param aParentEntity     parent container 
       * @param aContainer        container for children
       * @param aClassFilter      filter for search
       * @param aIsRecursive      <code>true</code> if require all children,
       *                          <code>false</code> if only want children at
       *                          this depth
       * @param aAttributes       child attributes required
       * @param aTypeOfSearch     if <code>true</code> then just checks if
       *                          there are children of this type, otherwise
       *                          returns the children
       * @param aContext          client context
       * @param aReturnValue      if just checking if there are children then
       *                          this will indicate if there are childen,
       *                          <code>true</code>, or if there are none,
       *                          <code>false</code>
       * @return                  list of DNs for the children found
       * @throws                  <code>SPIException</code> if
       *                          error occurs
       */
       public Vector getListOfChildren(
               LdapNode aParentEntity,
               String aContainer, String aClassFilter, boolean aIsRecursive,
               String[] aAttributes, boolean aTypeOfSearch, 
               LdapClientContext aContext, BooleanReturnValue aReturnValue) 
 			throws SPIException {
         StringBuffer startBuf = new StringBuffer(BUFFER_LENGTH);
         if (aContainer != null) {
             startBuf.append(aContainer).append(LdapEntity.LDAP_SEPARATOR);
         }
         startBuf.append(aParentEntity.getLocation());
         Vector returnList = performSearch(
                 				startBuf.toString(), aIsRecursive, 
                 				aClassFilter, aAttributes, true,
								aTypeOfSearch, aReturnValue, false,
								aContext);
         return returnList;
      }
      

    /** 
     * Reads the contents of a list of attributes and returns the
     * resulting <code>LDAPEntry</code>. 
     *
     * @param aDN          Distinguished Name for this read
     * @param aAttributes  attributes to be read
     * @param aContext     client context
     * @return             a table of attribute/value pairs
     * @throws             <code>LDAPException</code> if LDAP
     *                     error occurs
     */
     private LDAPEntry readAttributes(String aDN, String aAttributes [],
				      				  LdapClientContext aContext) 
     	throws LDAPException {
		LDAPConnection connection = aContext.getConnection() ;
		LDAPEntry entry = connection.read(aDN, aAttributes);
		return entry;
    }

     /**
       * Reads all attributes in an entry plus its timestamp and puts one
       * value for each in a hashtable of attribute name to byte array value.
       */
     public Hashtable readAllAttributes(String aDn, 
                            LdapClientContext aContext) throws LDAPException {
         String [] allAttributes = { "*", MODIFY_TIMESTAMP_ATTR } ;
         LDAPEntry entry = aContext.getConnection().read(aDn, allAttributes) ;
         Enumeration attributes = entry.getAttributeSet().getAttributes() ;
         Hashtable retCode = new Hashtable() ;

         while (attributes.hasMoreElements()) {
             LDAPAttribute attribute = 
                                    (LDAPAttribute) attributes.nextElement() ;
             Enumeration values = attribute.getByteValues() ;

             if (values.hasMoreElements()) {
                 retCode.put(attribute.getName(), values.nextElement()) ;
             }
         }
         return retCode ;
     }

    /** 
     * Reads an entry and returns the DN. Used when want to find
     * the display name for an <code>Entity</code>.
     *
     * @param aDN          distinguished name derived from 
     *                     <code>Entity</code>
     * @param aContext     client context
     * @return             distinguished name as read from LDAP 
     * @throws IllegalReadException if LDAP error occurs
     */
     public String readEntryDN(String aDN, LdapClientContext aContext)
     	throws SPIException {
		String dn = null;
        // Need to lookup just one attribute explicitly, otherwise all the
        // entry's contents are transmitted over the network, at least for
        // OpenLDAP implementations.
        String [] noAttributes = { "objectclass" } ;

		try {
		    LDAPConnection connection = aContext.getConnection();
		    LDAPEntry entry = connection.read(aDN, noAttributes);
		    if (entry != null) {
		        dn = entry.getDN();
		    }
		} catch (LDAPException ldape) {
		    if (ldape.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
		        throw new NoSuchEntityException(aDN);
		    } else {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
		    }
		}
		return dn;
    }
	
	

    /**
     * Reads the contents of an array  of attributes and puts the
     * attribute/value(s) mappings in a Hashtable.
     *
     * @param aDN          Distinguished name for this layer
     * @param aDNRequired  <code>true</code> if DN required,  
     *                     otherwise <code>false</code> 
     * @param aAttributes  array of attribute names to be read
     * @param aContext     client context
     * @return             table containing the attribute/value(s) mappings
     * @throws             <code>LDAPException</code> if LDAP
     *                     error occurs 
     */
    public Hashtable getAttributeValueTable(String aDN, boolean aDNRequired,
							String aAttributes [], LdapClientContext aContext)
		throws LDAPException {
		LDAPEntry entry = null;
        entry = readAttributes(aDN, aAttributes, aContext);
        Hashtable retTable = new Hashtable();
        if (entry != null) {
		    if (aDNRequired) {
				Vector values = new Vector();
				values.add(entry.getDN());
				retTable.put(DN_KEY, values);
		    }
            for (int i = 0 ; i < aAttributes.length ; ++ i) {
                retTable.put(aAttributes[i], 
                        getAllValues(entry, aAttributes[i]));
            }
        } 
        return retTable ;
    }

    /**
     * Reads the contents of an array of attributes and returns the
     * value in a <code>Vector</code>.
     *
     * @param aDN	        Distinguished Name of layer to be accessed
     * @param aAttributes   array of attributes to be read
     * @param aTypeOfSearch if <code>true</code> then just checks if
     *			            values exist, otherwise processes values
     * @param aIncludeDN    if <code>true</code> then add read DN of 
     * 			            entry to end of returned list, otherwise don't
     * @param aContext      client context
     * @param aReturnValue  if just checking if there are values then
     *			            this will indicate if there are,
     *			            <code>true</code>, or if there are none,
     *			            <code>false</code>
     * @return              <code>Vector</code> containing the attribute
     *                      values
     * @throws              <code>LDAPException</code> if LDAP
     *                      error occurs
     */
    public Vector getAttributeValueList(String aDN,  String[] aAttributes,
										boolean aTypeOfSearch, 
										boolean aIncludeDN,
										LdapClientContext aContext,
										BooleanReturnValue aReturnValue)
    	throws LDAPException { 
		Vector entityList = new Vector();
		LDAPEntry entry = null;
		entry = readAttributes(aDN, aAttributes, aContext);
		if (entry != null) {
            for (int i = 0 ; i < aAttributes.length ; ++ i) {
                LDAPAttribute attribute=entry.getAttribute(aAttributes[i]);
                if (attribute != null) {
                    /* If just checking that values exist, then
                       return here */
		            if(aTypeOfSearch) {
		                aReturnValue.setReturnValue(true);
		                return entityList;
		            }
		            Enumeration enumVal = attribute.getStringValues();
		            while (enumVal.hasMoreElements()) {
		                entityList.add((String)enumVal.nextElement());
		            }
                }
		    }
		    if (aIncludeDN) {
		        entityList.add(entry.getDN());
		    }
		}
        return entityList ;
    }
    /**
     * Reads the contents of an array of attributes and returns the
     * value in a <code>Vector</code>.
     *
     * @param aDN		   Distinguished Name of layer to be accessed
     * @param aAttributes  array of attributes to be read
     * @param aContext     client context
     * @return			   <code>Vector</code> containing the attribute
     *				       values
     * @throws			   <code>LDAPException</code> if
     *				       error occurs
     */
    public Vector getAttributeValueList(String aDN, String[] aAttributes,
										LdapClientContext aContext) 
    	throws LDAPException {
        return getAttributeValueList(aDN, aAttributes, false, false, aContext,
                					 new BooleanReturnValue(false));
    }

    /**
     * Carries out an LDAP search and returns the results 
     * in a <code>Vector</code>.
     *
     * @param aRelativeDn		RelativeDN of place in Directory Tree
     *                          to start search 
     * @param aRecursiveSearch  <code>boolean</code> value indicating whether
     *                          to carry out a search recursively, 
     *                          <code>true</code>, or to one level only,
     *                          <code>false</code> 
     * @param aSearchFilter     filter (search criteria) for the LDAP search
     * @param aAttributes       attributes to search for 
     * @param aDNRequired       <code>true</code> if DNs required,  
     *                          otherwise <code>false</code> 
     * @param aCheckOnly        <code>boolean</code> indicating if only
     *			        		checking if entries found, <code>true</code>, 
     *                          or not, <code>false</code> 
     * @param aSuccess          only used if aCheckOnly is <code>true</code>.
     *                          indicates if entries found from search
     *                          <code>true</code>, or not, <code>false</code> 
     * @param aAttributesOnly   <code>boolean</code> indicating if only 
     *                          attributes should be retrieved, 
     *                          <code>true</code>, or whether attributes and
     *                          values should be retrieved, <code>false</code>.
     * @param aContext          client context
     * @return		        	<code>Vector</code> containing a Hashtable 
     *			        		of the attribute values for each entry
     * @throws                  IllegalReadException if 
     * 			        		LDAP error occurs
     */
    public Vector performSearch (String aRelativeDn,
							     boolean aRecursiveSearch,
							     String aSearchFilter,
							     String[] aAttributes,
							     boolean aDNRequired,
							     boolean aCheckOnly,
						         BooleanReturnValue aReturnValue,
							     boolean aAttributesOnly,
							     LdapClientContext aContext) 
    	throws IllegalReadException {
        return performSearch(aRelativeDn, aRecursiveSearch,
			                 aSearchFilter, aAttributes,
			                 aDNRequired, aCheckOnly,
			                 aReturnValue, aAttributesOnly,
			                 aContext.getConnection());
    }

    /**
     * Carries out an LDAP search and returns the results 
     * in a <code>Vector</code>.
     *
     * @param aRelativeDn		RelativeDN of place in Directory Tree
     *                          to start search 
     * @param aRecursiveSearch  <code>boolean</code> value indicating whether
     *                          to carry out a search recursively, 
     *                          <code>true</code>, or to one level only,
     *                          <code>false</code> 
     * @param aSearchFilter     filter (search criteria) for the LDAP search
     * @param aAttributes       attributes to search for 
     * @param aDNRequired       <code>true</code> if DNs required,  
     *                          otherwise <code>false</code> 
     * @param aCheckOnly        <code>boolean</code> indicating if only
     *			        		checking if entries found, <code>true</code>, 
     *                          or not, <code>false</code> 
     * @param aSuccess          only used if aCheckOnly is <code>true</code>.
     *                          indicates if entries found from search
     *                          <code>true</code>, or not, <code>false</code> 
     * @param aAttributesOnly   <code>boolean</code> indicating if only 
     *                          attributes should be retrieved, 
     *                          <code>true</code>, or whether attributes and
     *                          values should be retrieved, <code>false</code>.
     * @param aConnection       client connection
     * @return		        	<code>Vector</code> containing a Hashtable 
     *			        		of the attribute values for each entry
     * @throws                  IllegalReadException if 
     * 			        		LDAP error occurs
     */
    public Vector performSearch (String aRelativeDn,
							     boolean aRecursiveSearch,
							     String aSearchFilter,
							     String[] aAttributes,
							     boolean aDNRequired,
							     boolean aCheckOnly,
						         BooleanReturnValue aReturnValue,
							     boolean aAttributesOnly,
							     LDAPConnection aConnection) 
    	throws IllegalReadException {
		Vector entityList = new Vector();
		LDAPSearchResults results = null;
		try {
		    results = doSearch(aRelativeDn,
				               aRecursiveSearch,
				               aSearchFilter,
						       aAttributes,
						       aAttributesOnly,
						       aConnection);
		    if (aCheckOnly) {
		        if(results.hasMoreElements()) {
		            aReturnValue.setReturnValue(true);
		        } 
	            aConnection.abandon(results) ;
		        return entityList;
		    }
		    while (results.hasMoreElements()) {
		        Hashtable attrsValues = new Hashtable();
		        LDAPEntry entry = results.next();
				if (aDNRequired) {
				    Vector dnValues = new Vector();
				    dnValues.add(entry.getDN());
				    attrsValues.put(DN_KEY, dnValues);
				}
				if (aAttributes != null) {
				    for (int i = 0; i < aAttributes.length; ++i) {
						attrsValues.put(aAttributes[i], 
							getAllValues(entry, aAttributes[i]));
		            }
				}
				entityList.add(attrsValues);
		    }
        } catch (LDAPException ldape) {
            int result = ldape.getLDAPResultCode();
            if (result == LDAPException.ADMIN_LIMIT_EXCEEDED) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_SIZE_READ_KEY, ldape);
            }
            else if (result != LDAPException.SIZE_LIMIT_EXCEEDED){
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
        }
		return entityList;
    }

    /**
      * Performs an LDAP search and returns the DN and attribute
      * values for each entry found. The results are returned as 
      * a <code>Vector</code> of <code>Vector</code>s, each element 
      * containing all the values for the attributes.
      * In case the DN was asked in addition of the attributes,
      * its value is stored in the last slot of the returned element. 
      *
      * @param aBaseDn          base DN of the search
      * @param aRecursive       is the search recursive?
      * @param aFilter          search filter
      * @param aAttributes      attribute list
      * @param aRetrieveDn      do we also want the DNs?
      * @param aContext         context to the repository
      * @return                 <code>Vector</code> containing value arrays 
      *                         for each entry found
      * @throws IllegalReadException   if an error occurs.
      */
    public Vector getEntriesAttributes (String aBaseDn, 
			                            boolean aRecursive,
			                            String aFilter, 
			                            String [] aAttributes, 
			                            boolean aRetrieveDn,
			                            LdapClientContext aContext) 
    	throws SPIException {
        Vector retCode = new Vector() ;
        try {
            LDAPSearchResults results = 
                doSearch(aBaseDn, aRecursive, aFilter, aAttributes, 
                         false, aContext);
    
	        while (results.hasMoreElements()) {
                LDAPEntry entry = results.next() ;
				Vector allEntryAttrValues = new Vector();
				for (int i = 0; i < aAttributes.length; ++i) {
				    Vector values = getAllValues(entry, aAttributes[i]);
				    if (values != null && !values.isEmpty()) {
				        for (int j = 0; j < values.size(); ++j) {
				            allEntryAttrValues.add((String)values.get(j));
				        }
				    }
				}
                if (aRetrieveDn) { allEntryAttrValues.add(entry.getDN());}
                retCode.add(allEntryAttrValues) ;
            }
        }
        catch (LDAPException ldape) {
            int result = ldape.getLDAPResultCode() ;
            if ( (result == LDAPException.SIZE_LIMIT_EXCEEDED)
              || (result == LDAPException.ADMIN_LIMIT_EXCEEDED)) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_SIZE_READ_KEY, ldape);
            }
            else {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
        }
        return retCode ;
    }

    /**
     * Carries out an LDAP search and returns the
     * <code>LDAPSearchResults</code>
     *
     * @param aRelativeDn		RelativeDN of place in Directory Tree
     *							to start search
     * @param aRecursiveSearch	<code>boolean</code> value indicating whether
     *							to carry out a search recursively,
     *							<code>true</code>, or to one level only,
     *							<code>false</code>
     * @param aSearchFilter		filter (search criteria) for the LDAP search
     * @param aAttributes		attributes to search for
     * @param aAttributesOnly	<code>boolean</code> indicating if only
     *							attributes should be retrieved,
     *							<code>true</code>, or whether attributes and
     *							values should be retrieved, <code>false</code>
     * @param aContext			client context
     * @return			<code>LDAPSearchResults</code>
     * @throws			<code>LDAPException</code> if LDAP error occurs
     */
    private LDAPSearchResults doSearch (String aRelativeDn,
										boolean aRecursiveSearch,
										String aSearchFilter,
										String[] aAttributes,
										boolean aAttributesOnly,
										LdapClientContext aContext)
    	throws LDAPException {
		return doSearch(aRelativeDn, aRecursiveSearch, aSearchFilter,
						aAttributes, aAttributesOnly,
						aContext.getConnection());
    }

    /**
     * Carries out an LDAP search and returns the  
     * <code>LDAPSearchResults</code>
     *
     * @param aRelativeDn		RelativeDN of place in Directory Tree
     *                          to start search 
     * @param aRecursiveSearch  <code>boolean</code> value indicating whether
     *                          to carry out a search recursively, 
     *                          <code>true</code>, or to one level only,
     *                          <code>false</code> 
     * @param aSearchFilter     filter (search criteria) for the LDAP search
     * @param aAttributes       attributes to search for
     * @param aAttributesOnly   <code>boolean</code> indicating if only 
     *                          attributes should be retrieved, 
     *                          <code>true</code>, or whether attributes and
     *                          values should be retrieved, <code>false</code>
     * @param aConnection       client LDAP connection
     * @return		        	<code>LDAPSearchResults</code> 
     * @throws                  <code>LDAPException</code> if LDAP error occurs
     */
    private LDAPSearchResults doSearch (String aRelativeDn, 
										boolean aRecursiveSearch,
										String aSearchFilter,
										String[] aAttributes,
										boolean aAttributesOnly,
										LDAPConnection aConnection)
    	throws LDAPException {
        /* Perform a search for entries at one level below
	   the relativeDn  by default */
		int scope = LDAPv2.SCOPE_ONE;
	
		if (aRecursiveSearch) {
		    scope = LDAPv2.SCOPE_SUB;
        }
    	if (aRelativeDn == null) { aRelativeDn = mBaseDN ; }
        return aConnection.search( aRelativeDn,
								   scope,
								   aSearchFilter,
								   aAttributes,
								   aAttributesOnly);
     }

    /**
     * Deletes an entry from the LDAP datastore.
     * @param aDN       entry DN
     * @param aContext  client context
     *
     * @throws      	<code>LDAPException</code> if entry cannot be
     *              	deleted
     */
    private void deleteEntry(String aDN, LdapClientContext aContext) 
    	throws LDAPException {
        aContext.getConnection().delete(aDN);
    }

    /**
     * Renames an entry in the LDAP datastore.
     * @param aDN       entry DN
     * @param aNewRDN   new RDN
     * @param aContext  client context
     *
     * @throws      	<code>LDAPException</code> if entry cannot be
     *              	deleted
     */
    private void renameEntry(String aDN, String aRDN,
            				 LdapClientContext aContext) 
    	throws LDAPException {
        aContext.getConnection().rename(aDN, aRDN, true);
    }

    /**
     * Creates an LDAP entry. 
     *
     * @param aDN	   		DN for this new entry
     * @param aAttrs   		set of attributes to be added to the
     *			   			entry
     * @param aConnection  	client LDAP connection
     * @throws		   <code>LDAPException</code> if error occurs
     */
    private void addEntry(String aDN, LDAPAttributeSet aAttrs,
            			  LDAPConnection aConnection) 
    	throws LDAPException{
        /* create the entry to be added */
        String [] dn = LDAPDN.explodeDN(aDN, false) ;
        int equalSign = dn [0].indexOf('=') ;
        String attr = dn [0].substring(0, equalSign) ;

        if (aAttrs.getAttribute(attr) == null) {
            aAttrs.add(new LDAPAttribute(attr,
                                         dn [0].substring(equalSign + 1)));
        }
        LDAPEntry newEntry = new LDAPEntry(aDN, aAttrs);
        aConnection.add(newEntry);
    }


    /**
     * Writes a list of changes to the repository.
     *
     * @param aDN          DN of the entity to be modified
     * @param aContainer   name of the container
     * @param aChanges     array of <code>Change</code> containing
     *                     the new attribute/value pairs
     * @param aIsMultiValued <code>true</code> if attribute is multivalued,
     *                     otherwise <code>false</code>
     * @param aConnection  client connection
     * @throws IllegalWriteException if LDAP error occurs
     */
    private void writeAttributes(String aDN, String aContainer, 
            					 Change[] aChanges, boolean aIsMultiValued,
    					 		LDAPConnection aConnection) 
		throws SPIException {
        LDAPModificationSet set = new LDAPModificationSet() ;
        try {
            if (!containsContainer(aDN, aContainer, aConnection)) {
                set.add(LDAPModification.ADD, 
                        new LDAPAttribute(LDAP_OBJCLASS, aContainer));
            }
            LDAPAttribute attribute; 
            for (int i = 0 ; i < aChanges.length ; ++ i) {
                if (aIsMultiValued) {
                    attribute =  new LDAPAttribute(aChanges[i].mName, 
                                      aChanges[i].mValues) ;
                } else {
                    attribute =  new LDAPAttribute(aChanges[i].mName, 
                                      aChanges[i].mValues[0]) ;
                }
                set.add(LDAPModification.REPLACE, attribute);
            }
            aConnection.modify(aDN, set) ;
        }
        catch (LDAPException ldape) {
            throw new IllegalWriteException(
                    IllegalWriteException.LDAP_WRITE_KEY, ldape);
        }
    }

    /**
     * Deletes a list of attribute values from an entity.  
     *
     * @param aDN	   		DN of the entity to be modified
     * @param aContainer   	name of the container
     * @param aChanges	   	array of <code>Change</code> containing
     *			   			the attribute/value pairs
     * @param aIsMultiValued <code>true</code> if attribute is multivalued,
     *                     	otherwise <code>false</code>
     * @param aConnection  	client connection
     * @throws IllegalReadException if LDAP error occurs
     */
    private void deleteAttributeValues(String aDN, String aContainer,
            						   Change [] aChanges, 
            						   boolean aIsMultiValued,
            						   LDAPConnection aConnection)
		throws SPIException {
		LDAPModificationSet set = new LDAPModificationSet() ;
		try {
		    LDAPAttribute attribute;
            for (int i = 0 ; i < aChanges.length ; ++ i) {
                if (aIsMultiValued) {
                    attribute =  new LDAPAttribute(aChanges[i].mName, 
                                      aChanges[i].mValues) ;
                } else {
                    attribute =  new LDAPAttribute(aChanges[i].mName, 
                                      aChanges[i].mValues[0]) ;
                }
                set.add(LDAPModification.DELETE, attribute);
            }
            aConnection.modify(aDN, set) ;
		}
        catch (LDAPException ldape) {
            int error = ldape.getLDAPResultCode();
            if ( (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
		}
    }
    
    /**
     * Checks if an entry exists in the repository. 
     *
     * @param aDN       DN for entry
     * @param aContext  client context
     * @return          <code>true</code> if entry exists, otherwise
     *                  <code>false</code>
     * @throws			LDAPException
     */
    public boolean entryExists(String aDN, LdapClientContext aContext) 
    		throws LDAPException {
        return entryExists(aDN, aContext.getConnection());
    }

    private boolean entryExists(String aDn, LDAPConnection aConnection) 
    		throws LDAPException{
        try {
            aConnection.read(aDn) ;
            return true ;
        }
        catch (LDAPException ldape) {
            int error = ldape.getLDAPResultCode();
            if ( (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
                throw ldape;
            }
        }
        return false ;
    }

    /**
     * Queries whether a particular object class is present in an entry.
     *
     * @param aDN	   DN for this entity
     * @param aContainer   object class to force
     * @param aConnection   client connection
     * @return true if the object class is present in the entry
     */
    private boolean containsContainer(String aDN, String aObjectClass,
			    					  LDAPConnection aConnection)
		throws LDAPException {
        String [] attributes = new String [1] ;
        attributes [0] = LDAP_OBJCLASS ;
        LDAPEntry entry = aConnection.read(aDN, attributes) ;
        if (entry != null) {
	    LDAPAttribute attribute = entry.getAttribute(attributes [0]) ;
		    if (attribute != null) {
	            Enumeration values = attribute.getStringValues() ;
	            while (values.hasMoreElements()) {
	                if (aObjectClass.equalsIgnoreCase(
	                               (String) values.nextElement())) {
	                    return true ;
	                }
	            }
	        }
        }
        return false ;
    }

    /**
     * Deletes values from a multi-valued attribute for a specific entity id.
     *
     * @param aDN	   entity to be modified
     * @param aAttributes  attribute 
     * @param aValues	   value list 
     * @param aContainer   object class allowing this attribute
     * @param aContext	   context to the repository
     * @throws SPIException if an error occurs
     */
    public void removeMultiValuedAttributeValues(String aDN,
                            String aAttribute,
                            String [] aValues,
                            String aContainer,
                            LdapClientContext aContext) 
    	throws SPIException {
        if (aAttribute == null || aValues == null) { 
            throw new IllegalArgumentException();
        }
        Change [] changes = new Change [1] ;
        changes [0] = new Change(aAttribute, aValues) ;
        LDAPConnection connection;
        connection = aContext.getConnection() ;
        deleteAttributeValues(aDN, aContainer, changes, true, connection);
    }


    /**
     * Writes values for a single-valued attribute to a specific entity id.
     *
     * @param aDN          entity to be modified
     * @param aAttributes  attribute list
     * @param aValues      value list aligned on attribute one
     * @param aContainer   object class allowing these attributes
     * @param aContext     context to the repository
     * @throws SPIException if an error occurs
     */
    public void fillAttributes(String aDN, 
                               String [] aAttributes,
                               String [] aValues, 
                               String aContainer,
                               LdapClientContext aContext) 
    	throws SPIException {
		if (aAttributes == null || aValues == null ||
			aAttributes.length != aValues.length) {
            throw new IllegalArgumentException();
		}
		int numOfAttributes = aAttributes.length;
	    Change [] changes = new Change [numOfAttributes] ;
		for (int i = 0 ; i < numOfAttributes ; ++ i) {
	        changes [i] = new Change(aAttributes [i], aValues[i]) ;
		}
		LDAPConnection connection;
	    connection = aContext.getConnection() ;
	    writeAttributes(aDN, aContainer, changes, false, connection) ;
    }

    /**
     * Adds values for a multi-valued attribute to a specific entity.
     *
     * @param aDN              entry to be modified
     * @param aAttribute       multi-valued attribute name
     * @param aValues          multi-valued list 
     * @param aContainer       object class allowing this attribute
     * @param aContext	       context to the repository
     * @throws IllegalReadException if LDAP error occurs
     * @throws	SPIException if an error occurs
     */
    public void addValuesToMultiValuedAttribute(String aDN,
												String aAttribute,
												String [] aValues,
												String aContainer,
												LdapClientContext aContext)
    	throws SPIException {
		if (aAttribute == null || aValues == null) {
            throw new IllegalArgumentException();
		}
		Vector entityList = new Vector();
		LDAPConnection connection = null;
		HashSet uniqueValues = new HashSet();
		LDAPEntry entry = null;
		try {
		    connection = aContext.getConnection() ;
		    entry = connection.read(aDN);
		} catch (LDAPException ldape1) {
		    if (ldape1.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
		        try {
				    String shortDN = getDNExcludingServiceEntry(aDN);
				    ensureServiceEntryExistsForEntry(shortDN, connection);
		            entry = connection.read(aDN);
		        } catch (LDAPException ldape2) {
	                throw new IllegalReadException(
	                        IllegalReadException.LDAP_READ_KEY, ldape2);
		        }
	    	} else {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape1);
		    }
		}
		LDAPAttribute attribute = entry.getAttribute(aAttribute);
		if (attribute != null) {
		    Enumeration enumVals = attribute.getStringValues();
		    while (enumVals.hasMoreElements()) {
		        uniqueValues.add(((String)enumVals.nextElement()));
		    }
		}
		for (int i = 0; i < aValues.length; ++i) {
		    uniqueValues.add(aValues[i]);
		}
		Change [] changes = new Change [1] ;
		changes[0] = new Change();
		changes[0].mName = aAttribute;
		int size = uniqueValues.size();
		changes[0].mValues = new String[size];
		Iterator values = uniqueValues.iterator();
		int index = 0;
		while (values.hasNext()){
		    changes[0].mValues[index ++] = (String)values.next();
        }
	    writeAttributes(aDN, aContainer, changes, true, connection);
    }

    /**
     * Builds the XML-Blob(tm) representing the component data
     * given the LDAP attributes of the entity.
     *
     * @param aValues     the table of attribute/value keys
     * @param aAttributes the attribute names which act as keys to the 
     *                    table 
     * @return 	 	  string representing the contents of the component
     *		          as an XML-Blob(tm)
     */
    String buildComponent(Hashtable aValues, String[] aAttributes) {
	return (String) aValues.get(aAttributes[0]) ;
    }

    /**
     * Utility class representing an attribute/value 
     * pair to be used in a change operation.
     */
    public static class Change
    {
    	/** Attribute name */
    	String mName ;
    	/** Attribute values */
    	String []mValues ;

        public Change() {}

        public Change(String aName, String []aValues) { 
            mName = aName ; mValues = aValues ; 
        }
        public Change(String aName, String aValue) {
	    mName = aName;
	    mValues = new String[1]; 
            mValues[0] = aValue ; 
        }
    }


    /**
      * Utility function to build the filter corresponding to a list
      * of components for a search, i.e either an overall filter,
      * a single value one or an OR between the possible values.
      *
      * @param aPolicyIds  array of component names
      * @param aIdParser  used to build stored component names
      * @return filter
      */
    private static String buildComponentNameFilter(
                                            String [] aPolicyIds,
                                            PolicyIdParser aIdParser) {
        StringBuffer retCode = new StringBuffer("(") ;

        if (aPolicyIds.length > 1) {
            retCode.append("|") ;
            for (int i = 0 ; i < aPolicyIds.length ; ++ i) {
                retCode.append("(").append(CONFIG_NAMING_ATTR) ;
                retCode.append(aIdParser.getStoredFormat(aPolicyIds [i])) ;
                retCode.append(")") ;
            }
        }
        else {
            retCode.append(CONFIG_NAMING_ATTR) ;
            if (aPolicyIds.length == 0) { 
                retCode.append(LDAP_WILDCARD) ; 
            }
            else {
                retCode.append(aIdParser.getStoredFormat(aPolicyIds [0])) ; 
            }
        }
        retCode.append(")") ;
        return retCode.toString() ;
    }

    /**
      * Utility function to return the first value of a given attribute
      * in an LDAP entry. 
      *
      * @param aEntry       LDAP entry
      * @param aAttribute   attribute name
      * @return the first attribute value or null if any problem arises.
      */
    private static String getFirstValue(LDAPEntry aEntry, String aAttribute) {
        LDAPAttribute attribute = aEntry.getAttribute(aAttribute) ;

        if (attribute != null) {
            Enumeration values = attribute.getStringValues() ;

            if (values.hasMoreElements()) {
                return (String) values.nextElement() ;
            }
        }
        return null ;
    }

    /**
      * Utility function to return all value for a given attribute
      * in an LDAP entry. 
      *
      * @param aEntry       LDAP entry
      * @param aAttribute   attribute name
      * @return             a <code>Vector</code> of attribute values
      */
    private static Vector getAllValues(LDAPEntry aEntry, String aAttribute) {
	Vector retCode = new Vector();
        LDAPAttribute attribute = aEntry.getAttribute(aAttribute) ;
        if (attribute == null) { return retCode; }
        Enumeration values = attribute.getStringValues() ;
        while (values.hasMoreElements()) {
            retCode.add((String) values.nextElement()) ;
        }
        return retCode ;
    }

    private static final String [] TIMEONLY_POLICY_ATTRS = 
                                        { ORG_UNIT_NAMING_ATTR,
                                          MODIFY_TIMESTAMP_ATTR} ;
    private static final String [] ALL_POLICY_ATTRS = { ORG_UNIT_NAMING_ATTR,
                                                       MODIFY_TIMESTAMP_ATTR,
                                                       KEYVALUE_ATTR } ;
    private static final String [] ALL_PROFILE_ATTRS = 
                                        { ORG_UNIT_NAMING_ATTR,
                                          PRIORITY_ATTR,  
                                          MODIFY_TIMESTAMP_ATTR,
                                          MODIFY_AUTHOR_ATTR,
                                          KEYVALUE_ATTR } ;
    /**
     * Takes a key/value string and a key as parameters, and returns
     * the value.
     *
     * @param aKeyValue	   key/value string
     * @param aKey	   key
     * @return	           value for this key
     */
    public static String getValueForKey(String aKeyValue,
				String aKey) {
	if (aKeyValue == null) {
		return null;
	}
	String keyValue = null;
	if (aKeyValue.startsWith(aKey)) {
	    int begin = aKeyValue.indexOf("=");
	    if (begin > 0) {
		keyValue = aKeyValue.substring(++begin);
            }
	}
	return keyValue;
    }
 
    /**
     * Takes a list of key/value strings, finds those of the required
     * key and returns those values in an array. If no values are found
     * then the returned array is empty.
     *
     * @param aKeyValues    <code>Vector</code> of key/value strings
     * @param aKey	    required key
     * @return	            values for this key
     */
    public static String[] getValuesForKey(Vector aKeyValues,
				String aKey) {
	if (aKeyValues == null || (aKeyValues.size() == 0)) {
		return new String[0];
	}
	Vector values = new Vector();
	String keyValue = null;
	for (int i = 0; i < aKeyValues.size(); ++i) {
		keyValue = (String)aKeyValues.get(i);
		keyValue = getValueForKey(keyValue, aKey);
		if (keyValue != null) {
		values.add(keyValue);
            }
	}
	int size = values.size();
	String []retCode = new String[size];
	for (int i = 0; i < size; ++i) {
		retCode[i] = (String)values.get(i);
	}
        return retCode;
    }
    private String getDNExcludingServiceEntry(String aDN) {
        String [] dnComponents = LDAPDN.explodeDN(aDN, false);
        int numOfComponents = dnComponents.length ;
        String []tmpComponents =
	   new String[numOfComponents - NUMBER_OF_SERVICE_MAPPING_ELEMENTS];
        int index = 0;
	for (int i = NUMBER_OF_SERVICE_MAPPING_ELEMENTS; i < numOfComponents; i++) {
    	   tmpComponents[index ++] = dnComponents[i];
        }
	StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
	tmpBuf.append(tmpComponents[0]);
	for (int i = 1; i < tmpComponents.length; ++i) {
	    tmpBuf.append(LdapEntity.LDAP_SEPARATOR);
	    tmpBuf.append(tmpComponents[i]);
	}
	return tmpBuf.toString();
    }

   /** 
    * Utility function that adds missing service entry elements
    * to entries in the datastore.
    *
    * @param aStartDN      entry which requires service subentry
    * @param aConnection   LDAP connection
    * @return              DN for entry
    * @throws              <code>LDAPException</code> if error occurs
    */
    public String ensureServiceEntryExistsForEntry(
            String aStartDN, LDAPConnection aConnection) 
    		throws LDAPException {
        StringBuffer currentDn = new StringBuffer(BUFFER_LENGTH) ;
        boolean alreadyThere = false ;

        currentDn.append(mServiceEntryDNBuf) ;
        currentDn.append(LdapEntity.LDAP_SEPARATOR) ;
        currentDn.append(aStartDN) ;
        try {
            aConnection.read(currentDn.toString()) ;
            alreadyThere = true ;
        }
        catch (LDAPException exception) {
            if (exception.getLDAPResultCode() 
                    != LDAPException.NO_SUCH_OBJECT) {
                throw exception ;
            }
        }
        if (alreadyThere) { return currentDn.toString() ; }
        currentDn.delete(0, currentDn.length()) ;
        currentDn.append(aStartDN) ;
        for (int i=NUMBER_OF_SERVICE_MAPPING_ELEMENTS-1; i>=0; --i) {
            currentDn.insert(0, LdapEntity.LDAP_SEPARATOR) ;
            currentDn.insert(0, SERVICE_MAPPING_ELEMENTS[i].mEntryRDN);
            try {
                addEntry(currentDn.toString(), 
                         SERVICE_MAPPING_ELEMENTS[i].mAttrSet, 
                         aConnection);
            }
            catch (LDAPException exception) {
                if (exception.getLDAPResultCode() != 
                    		LDAPException.ENTRY_ALREADY_EXISTS) {
                    throw exception ;
                }
            }
        }
        return currentDn.toString() ;
    }

        

    /**
     * For a given host or user entity returns the list of roles of which this
     * entity is a member. If the entity is not a member of a role then an
     * empty <code>Vector</code>  is returned.
     *
     * @param aEntity             entity whose membership information is required
     * @param aListingAttribute   attribute used to store membership information
     * @param aClientContext      client context
     * @return                    <code>Vector</code> listing role entries
     * @throws IllegalReadException if LDAP error occurs
     */
    public Vector getListedRolesForEntity(LdapEntity aEntity,
            String aListingAttribute, LdapClientContext aContext) 
    		throws SPIException {
        Vector entityList = null;
        String []attributes = new String[1];
        attributes[0] = aListingAttribute;
        try {
            entityList = getAttributeValueList(aEntity.getLocation(),
                    				attributes, aContext);
        } catch (LDAPException ldape) {
            int error = ldape.getLDAPResultCode();
            if ( (error != LDAPException.NO_RESULTS_RETURNED)
              && (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
        }
        return entityList == null ? new Vector(): entityList;
    }

    /**
      * Creates and returns object representing root organization. 
      *
      * @param aEntityMapping         entity mapping object 
      * @param aAuthorizedContext     authorized client context
      * @param aContext               client context
      * @return                      object representing root organization 
      * @throws SPIException if error occurs 
      */
    public Organization createRootOrganization(
            LdapEntityMapping aEntityMapping,
            LdapClientContext aAuthorizedContext, 
            LdapClientContext aContext) 
    		throws SPIException {
        if (mRootDN == null) {
     		mRootDN = readEntryDN(getBaseDN(), aAuthorizedContext);
        }
        mRootOrganization = new LdapOrganization(mRootDN, -1, this,
                 					aEntityMapping, aContext) ;
        return mRootOrganization;
    }

    /**
      * Returns object representing root organization. 
      *
      * @return                      object representing root organization 
      */
     public Organization getRootOrganization() { return mRootOrganization; }
 
     /**
      * Creates and returns object representing root domain. 
      *
      * @param aEntityMapping         entity mapping object 
      * @param aAuthorizedContext     authorized client context
      * @param aContext               client context
      * @return                       object representing root domain 
      * @throws             <code>SPIException</code> if error occurs 
      */
    public Domain createRootDomain(LdapEntityMapping aEntityMapping,
             						LdapClientContext aAuthorizedContext,
         							LdapClientContext aContext)
     		throws SPIException {
        if (mRootDN == null) {
     	    mRootDN = readEntryDN(getBaseDN(), aAuthorizedContext);
        }
        mRootDomain = new LdapDomain(
                 			LdapEntity.DOMAIN_TREE_INDICATOR + mRootDN,
                 			-1, this, aEntityMapping, aContext) ;
        return (Domain)mRootDomain;
    }
     
    /**
      * Returns object representing root domain. 
      *
      * @return      object representing root domain 
      */
     public Domain getRootDomain() { return mRootDomain; }
   
    /**
     * Returns the error code of the LDAPException 
     * which was the source of the SPIException
     *
     * @param aException   the SPIException
     * @return the error code of the LDAPException inside the
     * 			SPIException or 0 if SPIException does not 
     * 			contain an LDAPException
     */
    public static int getLdapErrorCode(SPIException spie) {
        int code = 0;
        Throwable cause = spie.getCause();
        if (cause != null) {
            if (cause instanceof LDAPException)
            code = ((LDAPException)cause).getLDAPResultCode();
        }
        return code;
    }

    /**
      * Creates and returns a profile with the
      * specified display name, applicability, and priority.
      * If the profile already exists or if a profile
      * with that priority already exists then a
      * <code>SPIException</code> is thrown.
      *
      * @param aRepository   profile repository
      * @param aDisplayName  display name for the profile
      * @param aPriority     priority for profile
      * @param aApplicability          profile scope
      * @return              the <code>Profile</code> representing
      *                      the created profile
      * @throws              <code>SPIException</code>
      *                      if error occurs
      */
    public Profile createProfile(ProfileRepositoryImpl aRepository,
            					 String aDisplayName, 
            					 Applicability aApplicability,
            					 int aPriority)
        	throws SPIException {
        String location = null;
        try {
            LDAPConnection connection = 
                ((LdapEntity)aRepository.getEntity())
                		.getContext().getConnection();
            location = createProfileEntry(aRepository, aDisplayName, 
                    					  aApplicability, aPriority, 
                    					  connection);
        } catch (LDAPException ldape) {
            throw new IllegalWriteException(
                    IllegalWriteException.LDAP_WRITE_KEY,
                    ldape);
        }
        return new LdapProfile(location, aRepository, aDisplayName, 
                			   aApplicability, aPriority);
    }

   /**
    * Creates a profile entry.
    * 
    * @param aRepository   profile repository
    * @param aDisplayName  display name for the profile
    * @param aApplicability profile scope
    * @param aPriority     priority for profile
    * @param aConnection     LDAP connection
    * @throws                <code>LDAPException</code> if LDAP
    * 			             error occurs
    */
    private String  createProfileEntry(
            ProfileRepository aRepository, String aDisplayName,
            Applicability aApplicability, int aPriority,
            LDAPConnection aConnection) 
    			throws LDAPException {
        /* First off, try to find a suitable name for the profile 
           if we can afford it. */
        String name = null ;
        String location = null ;
        Random randomiser = new Random() ;
        int identifier = 0 ;

        do {
            identifier = randomiser.nextInt() ;
            if (identifier < 0) { identifier = -identifier ; }
            name = String.valueOf(randomiser.nextInt()) ;
            location = CONFIG_NAMING_ATTR + name + 
                        LdapEntity.LDAP_SEPARATOR + 
                        ((LdapProfileRepository)aRepository).getLocation() ;
        } while (entryExists(location, aConnection)) ;
        /* Then create the entry. */
	    LDAPAttributeSet profileAttrs = new LDAPAttributeSet();
        String [] keyValues = new String [2] ;

        keyValues [0] = DISPLAY_NAME_KEY + aDisplayName ;
        keyValues [1] = APPLICABILITY_KEY + 
                        aApplicability.getStringValue() ;
        try {
	        profileAttrs.add(new LDAPAttribute(LDAP_OBJCLASS,
					PROFILE_MAPPING.mObjectClass));
	        profileAttrs.add(new LDAPAttribute(PRIORITY_ATTR, 
				Integer.toString(aPriority)));
	        profileAttrs.add(new LDAPAttribute(KEYVALUE_ATTR, keyValues)) ;
	        profileAttrs.add(PROFILE_MAPPING.mServiceIdAttr) ;
	        addEntry(location, profileAttrs, aConnection);
        } catch (LDAPException e1) {
	        if (e1.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
	            try {
	                LdapEntity entity = 
	                    (LdapEntity)aRepository.getEntity();
			        String entityServiceEntry = 
	                        ensureServiceEntryExistsForEntry(
	                                entity.getLocation(), aConnection);
		            // ensure _GlobalPolicyGroups_ container exists
		            StringBuffer containerBuf = new StringBuffer();
		            containerBuf.append(CONFIG_NAMING_ATTR); 
		            containerBuf.append(LdapProfileRepository.GLOBAL_PROFILE_CONTAINER);
		            containerBuf.append(LdapEntity.LDAP_SEPARATOR);
		            containerBuf.append(entityServiceEntry);
		            String containerDN = containerBuf.toString();
	                try {
	                    aConnection.read(containerDN);
	                } catch (LDAPException e2) {
	                    if (e2.getLDAPResultCode() == 
				            	LDAPException.NO_SUCH_OBJECT) {
		                    LDAPAttributeSet tmpSet = 
                                   new LDAPAttributeSet();
		                    tmpSet.add(new LDAPAttribute(LDAP_OBJCLASS,
		                               CONTAINER_MAPPING.mObjectClass));
		                    tmpSet.add(CONTAINER_MAPPING.mServiceIdAttr);
		                    addEntry(containerDN, tmpSet, aConnection);
	                    } else {
                           throw e2;
	                    }
		            }
		            addEntry(location, profileAttrs, aConnection);
	            } catch (SPIException spie) {
	                throw e1;
	            }
            } else {
                throw e1;
            }
	    }
        return location;
    }

    /**
      * Finds a profile object given its repository and displayname
      *
      * @param aRepository      profile repository
      * @param aDisplayName     display name for profile
      * @return                 object representing the profile
      * 						or null if not found
      * @throws                 <code>SPIException/code> if error
      *                         occurs
      */
    public  Profile findProfile(LdapProfileRepository aRepository,
            					String aDisplayName)
             throws SPIException {
        Profile profile = null;
        StringBuffer filter = new StringBuffer(BUFFER_LENGTH);
        filter.append("(&(");
        filter.append(KEYVALUE_ATTR);
        filter.append("=");
        filter.append(DISPLAY_NAME_KEY);
        filter.append(aDisplayName);
        filter.append("))");
        Vector settings = null;
        try {
            settings = performSearch(
                    aRepository.getLocation(),
                    true, filter.toString(),
                    ALL_PROFILE_ATTRS,
                    true, false,
                    null, false,
                    ((LdapEntity)aRepository.getEntity()).getContext());
        } catch (SPIException spie) { 
            int error = getLdapErrorCode(spie);
            if ( (error == LDAPException.NO_RESULTS_RETURNED)
              || (error == LDAPException.NO_SUCH_ATTRIBUTE)
              || (error == LDAPException.NO_SUCH_OBJECT)) {
                settings =  null;
            }
            else {
                throw spie;
            }
        }
        if (settings == null || settings.isEmpty()) {
            if (isVersion1) {
                // get Local Profile if any
    	        Profile tmpProfile = aRepository.getProfile(
    	                    aRepository.getLocalProfileId());
    	        if ((tmpProfile != null) &&
    	            (tmpProfile.getDisplayName().equals(aDisplayName))) {
    	            profile = tmpProfile;
    	        }
            }
        }
        else {
            profile = getProfileObject(aRepository, (Hashtable)settings.get(0));
        }
        return profile;
    }

    /** 
      * Takes the attribute values returned for a profile
      * entry and returns a <code>Profile</code> object.
      *
      * @param aRepository  profile repository
      * @param aAttrValues    collection containing required attribute
      *                     values
      * @return             <code>LdapProfile</code> object
      * @throws             <code>SPIException</code> if error occurs
      */
    private Profile getProfileObject(
            ProfileRepository aRepository, Hashtable aAttrValues) 
            throws SPIException {
        String dn = null, displayName = null, priority = null;
        String lastModified = null, authorDN = null, comment = null;
        Applicability applicability = Applicability.UNKNOWN;
        int priorityInt = ProfileImpl.UNDEFINED_PRIORITY;
        Vector values = (Vector) aAttrValues.get(DN_KEY);
        if (values != null && !values.isEmpty()) {
            dn = (String)values.get(0);
        }
        values = (Vector) aAttrValues.get(PRIORITY_ATTR);
        if (values != null && !values.isEmpty()) {
            priority = (String)values.get(0);
        }
        try {
            priorityInt = Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            return null;
        }
        values = (Vector) aAttrValues.get(MODIFY_TIMESTAMP_ATTR);
        if (values != null && !values.isEmpty()) {
            lastModified = (String)values.get(0);
        }
        values = (Vector) aAttrValues.get(MODIFY_AUTHOR_ATTR);
        if (values != null && !values.isEmpty()) {
            authorDN = (String)values.get(0);
        }
        values = (Vector) aAttrValues.get(KEYVALUE_ATTR);
        if (values != null && !values.isEmpty()) {
            String valuesArray[] = 
                 getValuesForKey(values, DISPLAY_NAME_KEY);
            if (valuesArray.length != 0) {
                displayName = valuesArray[0];
            }
            values = (Vector) aAttrValues.get(KEYVALUE_ATTR);
            valuesArray = 
                 getValuesForKey(values, APPLICABILITY_KEY);
            if (valuesArray.length != 0) {
                applicability = 
                    Applicability.getApplicability(valuesArray[0]);
            }
            valuesArray = 
                 getValuesForKey(values, COMMENT_KEY);
            if (valuesArray.length != 0) {
                comment =  valuesArray[0];
            }
        }
        if (dn == null || displayName == null || lastModified == null
         || applicability.equals(Applicability.UNKNOWN)) { 
            return null; 
        }
        // a priority of 0 is accepted for LocalProfiles only 
        // (and only in Version1)
        if (priorityInt <= ProfileImpl.UNDEFINED_PRIORITY) {
            if (isVersion1) {
                if (!LdapProfile.isLocalProfileDN(dn)) {
                    return null;
                }
            } else {
                return null;
            }
        }
        ProfileRepositoryImpl repository = (ProfileRepositoryImpl)aRepository;
        if (applicability.equals(Applicability.HOST)) {
            ProfileRepositoryImpl rootDomainRepository = 
                (ProfileRepositoryImpl)mRootDomain.getProfileRepository();
            if (repository.getId().equals(rootDomainRepository.getId())) {
                repository = rootDomainRepository;
            }
        }
        LdapProfile retCode = new LdapProfile(dn, repository, displayName,
                                              applicability, priorityInt);
        retCode.setLastModified(lastModified);
        Entity author = null;
        if (mRootOrganization != null) {
            author = mRootOrganization.getEntity(authorDN);
        }
        if ((author == null) && (mRootDomain != null)) {
            author = mRootDomain.getEntity(authorDN);
        }
        retCode.setAuthor(author);
        retCode.setExistingComment(comment);
        return retCode;
    }

    /** 
      * Takes the attribute values returned for a policy 
      * entry and returns a <code>Policy</code> object.
      *
      * @param aId          id for the policy
      * @param aProfile     profile 
      * @param aAttrValues  collection containing required attribute
      *                     values
      * @return             <code>LdapPolicy</code> object
      * @throws             <code>SPIException</code> if error occurs
      */
    private Policy getPolicyObject(String aId, ProfileImpl aProfile,
            					   Hashtable aAttrValues) 
            throws SPIException {
        if (aProfile == null || aAttrValues == null) { 
            return null; 
        }
        String dn = null, blob = null, lastModified = null;
        Vector values = (Vector) aAttrValues.get(DN_KEY);
        if (values != null && !values.isEmpty()) {
            dn = (String)values.get(0);
        }
        if (aId == null) {
            values = (Vector) aAttrValues.get(ORG_UNIT_NAMING_ATTR);
            if (values != null && !values.isEmpty()) {
               aId = (String)values.get(0);
               if (aId == null) {
                   return null; 
               } else {
                    aId = mPolicyIdParser.getOutputFormat(aId) ;
               }
            }
        }
        values = (Vector) aAttrValues.get(MODIFY_TIMESTAMP_ATTR);
        if (values != null && !values.isEmpty()) {
            lastModified = (String)values.get(0);
        }
        values = (Vector) aAttrValues.get(KEYVALUE_ATTR);
        if (values != null && !values.isEmpty()) {
            String valuesArray[] = 
                 getValuesForKey(values, APOC_BLOB_KEY);
            if (valuesArray.length != 0) {
                blob = valuesArray[0];
            }
        }
        if (dn == null || blob == null || lastModified == null) { 
            return null; 
        }
        LdapPolicy retCode = new LdapPolicy(aId, blob, 
                	Timestamp.getMillis(lastModified), dn, aProfile);
        return retCode;
    }

    /** 
     * Takes the attribute values returned for a policy 
     * entry and returns a <code>PolicyInfo</code> object.
     *
     * @param aProfile     profile 
     * @param aAttrValues  collection containing required attribute
     *                     values
     * @return             <code>PolicyInfo</code> object
     * @throws             <code>SPIException</code> if error occurs
     */
    private PolicyInfo getPolicyInfoObject(ProfileImpl aProfile,
           					   		   	   Hashtable aAttrValues) 
    		throws SPIException {
        if (aProfile == null || aAttrValues == null) { 
            return null; 
        }
        String id = null, lastModified = null;
        Vector values = (Vector) aAttrValues.get(ORG_UNIT_NAMING_ATTR);
        if (values != null && !values.isEmpty()) {
            id = (String)values.get(0);
            if (id == null) {
                return null; 
            } else {
                id = mPolicyIdParser.getOutputFormat(id) ;
            }
        }
        values = (Vector) aAttrValues.get(MODIFY_TIMESTAMP_ATTR);
        if (values != null && !values.isEmpty()) {
            lastModified = (String)values.get(0);
        }
        if (lastModified == null) { 
            return null; 
        }
        PolicyInfo retCode = new PolicyInfo(id, aProfile.getId(),
                				Timestamp.getMillis(lastModified));
        return retCode;
    }

    /**
      * Checks if another profile of this scope and with
      * this priority already exists. If it does then a 
      * <code>SPIException</code> is thrown.
      * 
      * @param aRepository    the profile repository
      * @param aApplicability           profile scope
      * @param aPriority      the priority
      * @param aContext        client context 
      * @throws <code>SPIException</code> if priority already
      *         in use
      */
    public void checkForPriorityConflict(ProfileRepository aRepository,
            Applicability aApplicability, int aPriority)
            throws SPIException {
        StringBuffer location = new StringBuffer(BUFFER_LENGTH);
        StringBuffer filter = new StringBuffer(BUFFER_LENGTH);
        filter.append("(&");
        addApplicabilityFilter(filter, aApplicability);
        filter.append("(");
        filter.append(PRIORITY_ATTR);
        filter.append("=");
        filter.append(aPriority);
        filter.append("))");
        String []attributes = new String[1];
        attributes[0] = PRIORITY_ATTR;
        Vector profiles = null;
        try {
            profiles = performSearch(
                    ((LdapProfileRepository)aRepository).getLocation(),
                            RECURSIVE_SEARCH, filter.toString(),
                            attributes,
                            false, false,
                            null, false,
                            ((LdapEntity)aRepository.getEntity()).getContext());
        } catch (SPIException spie) { 
            int error = getLdapErrorCode(spie);
            if ( (error == LDAPException.NO_RESULTS_RETURNED)
              || (error == LDAPException.NO_SUCH_ATTRIBUTE)
              || (error == LDAPException.NO_SUCH_OBJECT) ){
                return;
            }
            else {
                throw spie;
            }
        }
        if (profiles != null && !profiles.isEmpty()) {
            throw new InvalidPriorityException(
                    InvalidPriorityException.USED_PRIORITY_KEY,
                    aPriority);
        }
    }

     /**
       * Returns a list of the profile priorities already
       * existing for this scope in the profile repository.
       *
       * @param aRepository      profile repository 
       * @param aApplicability  scope
       * @return                 <code>Vector</code> listing existing
       *                         profile priorities for this scope
       * @throws                 <code>SPIException</code> if
       *                         the priorities contain a duplicate
       */
    public Iterator getProfilePriorities(
            			ProfileRepositoryImpl aRepository,
            			Applicability aApplicability)
    	throws SPIException {
        Vector retCode = new Vector();
        if (aApplicability.equals(Applicability.UNKNOWN)) { 
            return retCode.iterator(); 
        }
        StringBuffer filter = new StringBuffer(BUFFER_LENGTH);
        filter.append("(&");
        addApplicabilityFilter(filter, aApplicability);
        filter.append("(");
        filter.append(PRIORITY_ATTR);
        filter.append("=*");
        filter.append(")");
        filter.append(")");
        String[] attributes = new String[1];
        attributes[0] = PRIORITY_ATTR;
        Vector settings = null;
        try {
            settings = performSearch(
                    ((LdapProfileRepository)aRepository).getLocation(),
                            false, filter.toString(),
                            attributes,
                            false, false,
                            null, false,
                            ((LdapEntity)aRepository.getEntity()).getContext());
        } catch (SPIException spie) { 
            int error = getLdapErrorCode(spie);
            if ( (error == LDAPException.NO_RESULTS_RETURNED)
              || (error == LDAPException.NO_SUCH_ATTRIBUTE)
              || (error == LDAPException.NO_SUCH_OBJECT) ){
                return retCode.iterator();
            }
            else {
                throw spie;
            }
        }
        if (settings == null || settings.isEmpty()) { 
            return retCode.iterator(); 
        }
        String priority = null;
        for (int i = 0; i < settings.size(); ++i) {
            Integer priorityInt;
            Hashtable groupAttributes = (Hashtable)settings.get(i);
            Vector values = (Vector) groupAttributes.get(PRIORITY_ATTR);
            if (values != null && !values.isEmpty()) {
                priority = (String)values.get(0);
                try {
                    priorityInt = new Integer(priority);
                    retCode.add(priorityInt);
                } catch (NumberFormatException ignore) { }
            }
        }
        return retCode.iterator();
    }

    /**
     * Deletes the profile from the LDAP server. 
     *
     * @param aRepository  profile repository
     * @param aProfile     profile to be deleted
     * @param aContext     client context
     */
    public void destroyProfile(ProfileRepositoryImpl aRepository,
            ProfileImpl aProfile) throws SPIException {
        LdapClientContext context = ((LdapEntity)aRepository.getEntity())
            							.getContext();
        String location = ((LdapProfile)aProfile).getLocation();
	    try {
	        deleteEntry(location, context);
	    } catch (LDAPException ldape) {
	        int error = ldape.getLDAPResultCode();
	        if (error == LDAPException.NO_SUCH_OBJECT) {
	            throw new InvalidProfileException(
	                    InvalidProfileException.NO_EXIST_PROFILE_KEY,
	                    aProfile.getId(), ldape);
	        } else if (error == LDAPException.NOT_ALLOWED_ON_NONLEAF) {
		        // need to delete components first
		        try {
		            LDAPSearchResults results = doSearch(location,
			        false,
			        LDAP_OBJCLASS + "=" + SUNSERVICE_COMPONENT_OBJCLASS,
			        null, false, context);
	                while (results.hasMoreElements()) {
	                    LDAPEntry entry = results.next();
		                deleteEntry(entry.getDN(), context);
	                }
		            //then delete profile
		            deleteEntry(location, context);
	            } catch (LDAPException ldape1) {
	                throw new IllegalWriteException(
	                        IllegalWriteException.LDAP_WRITE_KEY,
	                        ldape1);
	            }
	        } else {
                throw new IllegalWriteException(
                        IllegalWriteException.LDAP_WRITE_KEY,
                        ldape);
	        }
	    }
    }

    /**
     * Returns a boolean indicating if the entity has write
     * access for this profile repository.
     *
     * @param aRepository  profile repository
     * @param aEntity      entity
     * @return             <code>true</code> if the entity has write access
     *                     otherwise <code>false</code>
     * @throws             <code>SPIException</code> if error occurs 
     */
    public boolean hasWriteAccess(ProfileRepositoryImpl aRepository,
            Entity aEntity) throws SPIException{
        LdapClientContext context = ((LdapEntity)aEntity).getContext();

        try {
            String profileId = createProfileEntry(
                    aRepository, "__TestGroupDoNotUse__", 
                    Applicability.USER, ProfileImpl.UNDEFINED_PRIORITY,
                    context.getConnection()); 
            context.getConnection().delete(profileId);
        } catch (LDAPException ldape) {
            if (ldape.getLDAPResultCode() != 
                             LDAPException.INSUFFICIENT_ACCESS_RIGHTS) {
                throw new IllegalWriteException(
                        IllegalWriteException.LDAP_WRITE_KEY,
                        ldape);
            }
            return false ;
        }
        return true ;
    }
    /**
     * Returns the profile that matches the id. 
     *
     * @param aRepository  profile repository
     * @param aId          id for the required profile
     * @return             <code>Profile</code> matching this id
     * 					   or null if one does not exist
     * @throws IllegalReadException if LDAP error occurs
     * @throws SPIException if error occurs 
     */
    public Profile getProfile(ProfileRepositoryImpl aRepository,
            String aId) throws SPIException {
        if (aId == null) { return null; }
        Hashtable valuesTable = null;
        try {
            valuesTable = getAttributeValueTable(aId,
                    true, ALL_PROFILE_ATTRS,
                    ((LdapEntity)aRepository.getEntity()).getContext());
        } catch (LDAPException ldape) {
            if (ldape.getLDAPResultCode() == 
                	LDAPException.NO_SUCH_OBJECT) {
                return null;
            }
            else {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
        }
        return getProfileObject(aRepository, valuesTable);
    }
    
    private String getEntityIdFromProfileId(String aProfileId) {
        if (aProfileId == null) { return null; }
        String [] dnComponents = LDAPDN.explodeDN(aProfileId, false);
        if (dnComponents == null 
         || dnComponents.length < NUMBER_OF_SERVICE_MAPPING_ELEMENTS+2) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        int start = 1 + NUMBER_OF_SERVICE_MAPPING_ELEMENTS;
        /* exclude legacy global container element */
        if (dnComponents[start].equalsIgnoreCase(
                CONFIG_NAMING_ATTR + SERVICES)) {
            start++;
        }
        buf.append(dnComponents[start]);
	    for (int i = start + 1; i < dnComponents.length; ++i) {
	        buf.append(LdapEntity.LDAP_SEPARATOR);
	        buf.append(dnComponents[i]);
	    }
	    return buf.toString();
    }
    
    /**
     * Returns the profile that matches the id. 
     *
     * @param aId          id for the required profile
     * @return             <code>Profile</code> matching this id
     * @throws             <code>SPIException</code> if error occurs 
     */
    public Profile getProfile(String aId) throws SPIException {
        if (aId == null) { return null; }
	    String entityId = getEntityIdFromProfileId(aId);
        Entity entity = null;
        if (mRootOrganization != null) {
            entity = mRootOrganization.getEntity(entityId);
        }
        if ((entity == null) && (mRootDomain != null)) {
            entity = mRootDomain.getEntity(entityId);
        }
        if (entity == null) { return null; }
        return getProfile(
                (ProfileRepositoryImpl)entity.getProfileRepository(),
                aId);
    }

    /**
     * Returns the profiles that match the filter string. 
     *
     * @param aRepository  profile repository
     *                     then all profiles required
     * @param aApplicability scope of profiles required
     * @return             TreeSet of profiles matching
     *                     the filter
     * @throws             <code>SPIException</code> if error occurs 
     */
    public TreeSet getProfiles(LdapProfileRepository aRepository,
            Applicability aApplicability) 
                       throws SPIException {
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
        tmpBuf.append("(&(");
        tmpBuf.append(CONFIG_NAMING_ATTR);
        tmpBuf.append(LDAP_WILDCARD);
        tmpBuf.append(")");
        tmpBuf.append("(");
        tmpBuf.append(SUNSERVICEID_ATTR);
        tmpBuf.append("=");
        tmpBuf.append(SUNSERVICEID_PROFILE);
        tmpBuf.append("))");
        Vector profiles = null;

        try {
            profiles = performSearch(
                     aRepository.getLocation(),
                     true, tmpBuf.toString(),
                     ALL_PROFILE_ATTRS,
                     true, false,
                     null, false,
                     ((LdapEntity)aRepository.getEntity()).getContext());
        } catch (SPIException spie) {
            if (getLdapErrorCode(spie) != LDAPException.NO_SUCH_OBJECT) {
                throw spie;
            }
        }
        TreeSet profilesTree = new TreeSet(new LdapProfileComparator());
        if (profiles != null) {
	        ProfileImpl tmpProfile = null;
	        int nbProfiles = profiles.size();
	        for (int i = 0; i < nbProfiles; ++i) {
	            tmpProfile = (ProfileImpl)getProfileObject(aRepository,
	                    (Hashtable)profiles.get(i)); 
	            if (tmpProfile == null) {
	                continue; 
	            }
	            boolean includeProfile = false;
	            Applicability tmpApp = tmpProfile.getApplicability();
	            if (tmpApp.equals(Applicability.ALL)){
	                includeProfile = true;
	            }
	            else if (tmpApp.equals(Applicability.HOST)
	                  || tmpApp.equals(Applicability.USER)) {
	                if ( aApplicability.equals(Applicability.ALL) 
	                  || aApplicability.equals(tmpApp)) {
	                    includeProfile = true;
	                }
	            }
	            if (includeProfile) {
	                profilesTree.add(tmpProfile);
	            } 
	        }
        }
        if (isVersion1) {
            // include Local Profile if any
	        Profile tmpProfile = aRepository.getProfile(
	                    aRepository.getLocalProfileId());
	        if (tmpProfile != null) {
	            profilesTree.add(tmpProfile);
	        }
        }
        return profilesTree;
    }
    
    /**
     * Returns all the profiles
     *
     * @param aPolicyManager   the PolicyManager object
     * @return             <code>Iterator</code> over all the Profiles
     * @throws             <code>SPIException</code> if error occurs 
     */
    public Iterator getAllProfiles(PolicySource aPolicySource, LdapEntity startingEntity,
                                   Applicability applicability)
            throws SPIException {
        Vector retCode = new Vector();
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
        tmpBuf.append("(&(");
        tmpBuf.append(LDAP_OBJCLASS);
        tmpBuf.append("=");
        tmpBuf.append(SUNSERVICE_COMPONENT_OBJCLASS);
        tmpBuf.append(")");
        tmpBuf.append("(");
        tmpBuf.append(CONFIG_NAMING_ATTR);
        tmpBuf.append(LDAP_WILDCARD);
        tmpBuf.append(")");
        tmpBuf.append("(");
        tmpBuf.append(SUNSERVICEID_ATTR);
        tmpBuf.append("=");
        tmpBuf.append(SUNSERVICEID_PROFILE);
        tmpBuf.append("))");
        Vector profiles = null;

        try {
            profiles = performSearch(
                     startingEntity.getLocation(),
                     true, tmpBuf.toString(),
                     ALL_PROFILE_ATTRS,
                     true, false,
                     null, false,
                     startingEntity.getContext());
        } catch (SPIException spie) {
             if (getLdapErrorCode(spie) == LDAPException.NO_SUCH_OBJECT) {
                 return retCode.iterator();
             } else {
                 throw spie;
             }
        }
        if (profiles == null || profiles.isEmpty()) { 
            return retCode.iterator(); 
        }
        
        Hashtable repositories = new Hashtable();
        TreeSet profilesTree =
                        new TreeSet(new LdapProfileComparator());
        ProfileImpl profile = null;
        int nbProfiles = profiles.size();
        for (int i = 0; i < nbProfiles; ++i) {
            Hashtable attrValues = (Hashtable)profiles.get(i);
            // reuse or create a ProfileRepository for the Profile
            String dn = null;
            Vector values = (Vector) attrValues.get(DN_KEY);
            if (values != null && !values.isEmpty()) {
                dn = (String)values.get(0);
            }
            if (dn == null) {
                continue;
            }
            String entityId = getEntityIdFromProfileId(dn);
            if (aPolicySource.getEntity(entityId) == null) {
                continue;
            }
            LdapProfileRepository repository = 
                (LdapProfileRepository)repositories.get(entityId);
            if (repository == null) {
                repository = new LdapProfileRepository(entityId, 
                        							   aPolicySource);
                repositories.put(entityId, repository);
            }
            profile = (ProfileImpl)getProfileObject(repository,
                    								attrValues);
            if (profile == null) {
                continue; 
            }
            // filter profiles after applicability
            if (!applicability.equals(Applicability.ALL)
             && !applicability.equals(profile.getApplicability())) {
                    continue;
            }
            profilesTree.add(profile);
        }
        return profilesTree.iterator();
    }

     /**
      * Returns an <code>Iterator</code> of <code>Profile</code>s
      * assigned to an entity. 
      * The <code>Iterator</code> is ordered by profile priority,
      * with the most specific profile as the last element.
      *
      * @param aEntity         entity whose assigned profiles are
      *                        required
      * @return                <code>Iterator</code> of profiles 
      * @throws                <code>SPIException</code> if
      *                        error occurs
      */
    public Iterator getAssignedProfiles(Entity aEntity)
        	throws SPIException {
        LdapEntity entity = (LdapEntity)aEntity;
        Vector policyList = new Vector();
        Vector assignedProfiles = new Vector();
        String []attributes = new String[1];
	    attributes[0] = KEYVALUE_ATTR;
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
        tmpBuf.append(getServiceEntryDN()).append(
                LdapEntity.LDAP_SEPARATOR).append(
                entity.getLocation());
        try {
             policyList = getAttributeValueList(tmpBuf.toString(),
                          		attributes, entity.getContext());
        } catch (LDAPException ldape) {
            int error = ldape.getLDAPResultCode();
            if ( (error != LDAPException.NO_RESULTS_RETURNED) 
              && (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY,
                        ldape);
            }
        }
        if (policyList == null || policyList.isEmpty()) {
            return assignedProfiles.iterator();
        }
        String [] assignedValues = getValuesForKey(policyList, 
                								   ASSIGNED_KEY);
        for (int i = 0; i < assignedValues.length; ++i) {
            String profileDN = assignedValues[i];
            if (profileDN != null) {
                Profile profile = null;
                try {
                    profile = getProfile(profileDN);
                    if (profile != null) {
                        assignedProfiles.add(profile);
                    }
                } catch (Exception ignore) {
                    /* problem creating profile, so omit */
                }
            }
        }
        // for compatiblity with APOC 1, add Local Profile if any
        if (isVersion1) {
            LdapProfileRepository rep = 
                (LdapProfileRepository)entity.getProfileRepository();
            String localId = rep.getLocalProfileId();
            Profile localProfile = rep.getProfile(localId);
            if (localProfile != null) {
                boolean profileIncluded = false;
                Iterator iterProfiles = assignedProfiles.iterator();
                while (iterProfiles.hasNext()) {
                    Profile profile = (Profile)iterProfiles.next();
                    if (profile.equals(localProfile)) {
                        profileIncluded = true;
                        break;
                    }
                }
                if (!profileIncluded) {
                    assignedProfiles.add(localProfile);
                }
            }
        }
        return assignedProfiles.iterator();
    }

    /**
      * Assigns a profile to this entity.
      *
      * @param aProfile   profile to assign
      * @param aEntity    entity to which the profile is to
      *                   to be assigned
      * @throws            <code>SPIException</code> if error occurs
      */
    public void assignProfile(Profile aProfile, Entity aEntity)
    		throws SPIException {
        if (aProfile == null) { return; }
        String attribute = KEYVALUE_ATTR;
        try {
	        boolean entryExists = entryExists(((LdapProfile)aProfile).getLocation(),
	                ((LdapEntity)aEntity).getContext());
	        if (!entryExists) {
	            throw new InvalidProfileException(
	                    InvalidProfileException.NO_EXIST_PROFILE_KEY,
	                    aProfile.getId());
	        }
        } catch (LDAPException ldape) {
            throw new IllegalReadException(
                    IllegalReadException.LDAP_READ_KEY, ldape);
        }
        String values[] = 
           {ASSIGNED_KEY + ((LdapProfile)aProfile).getLocation().toLowerCase() };
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
            tmpBuf.append(getServiceEntryDN());
            tmpBuf.append(LdapEntity.LDAP_SEPARATOR);
            tmpBuf.append(((LdapEntity)aEntity).getLocation());
        addValuesToMultiValuedAttribute(tmpBuf.toString(),
                    attribute, values,
                    SUNSERVICE_COMPONENT_OBJCLASS, 
                    ((LdapEntity)aEntity).getContext()) ;
        
    }

    /**
      * Unassigns a profile to this entity.
      *
      * @param aProfile   profile to unassign
      * @param aEntity    entity that currently has the profile 
      *                   assigned
      * @throws           <code>SPIException</code> if error occurs
      */
    public void unassignProfile(Profile aProfile, 
            Entity aEntity) throws SPIException  {
        if (aProfile == null) { return; }
        String values [] = { ASSIGNED_KEY + 
            ((LdapProfile)aProfile).getLocation() };
        values[0] = values[0].toLowerCase();
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
            tmpBuf.append(getServiceEntryDN()).append(
            LdapEntity.LDAP_SEPARATOR).append(
                ((LdapEntity)aEntity).getLocation());
        removeMultiValuedAttributeValues(tmpBuf.toString(),
               KEYVALUE_ATTR, values,
                   SUNSERVICE_COMPONENT_OBJCLASS, 
                   ((LdapEntity)aEntity).getContext()) ;
    }

    /**
     * Sets the priority for this profile.
     *
     * @param aProfile  	profile
     * @param aPriority 	priority for this profile
     * @throws SPIException if error occurs 
     * @throws InvalidPriorityException if aPriority invalid or in use 
     */
    public void setProfilePriority(ProfileImpl aProfile,
            int aPriority) throws SPIException {
        if (aPriority <= ProfileImpl.UNDEFINED_PRIORITY) {
            throw new InvalidPriorityException(
                    InvalidPriorityException.INVALID_PRIORITY_KEY, 
                    aPriority);
        }
        checkForPriorityConflict(aProfile.getProfileRepository(),
            aProfile.getApplicability(), aPriority);
        String attributes[] =  { PRIORITY_ATTR } ;
        String values [] = { Integer.toString(aPriority) };
        fillAttributes(((LdapProfile)aProfile).getLocation(),
                  attributes, values,
                  SUNSERVICE_COMPONENT_OBJCLASS, ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
    }

    /**
     * Sets the display name for this profile.
     *
     * @param aProfile      profile
     * @param aDisplayName  new display name for this profile
     * @throws              <code>SPIException</code> if error occurs 
     */
    public void setProfileDisplayName(ProfileImpl aProfile,
            String aDisplayName) throws SPIException {
        String values[] = { DISPLAY_NAME_KEY +
                      aProfile.getDisplayName() };
        removeMultiValuedAttributeValues(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS, 
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
         values[0] = DISPLAY_NAME_KEY + aDisplayName;
         addValuesToMultiValuedAttribute(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS,
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
    }

    /**
     * Sets the comment for this profile. If the new
     * comment is null then the existing comment is removed.
     *
     * @param aProfile  profile object
     * @param aComment  comment as a String 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public  void setProfileComment(ProfileImpl aProfile,
            String aComment) throws SPIException {
        String values [] = new String[1];
        if (aProfile.getComment() != null) {
           values[0] = COMMENT_KEY + aProfile.getComment();
           removeMultiValuedAttributeValues(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS, 
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
        }
        if (aComment != null) {
            values[0] = COMMENT_KEY + aComment;
            addValuesToMultiValuedAttribute(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS,
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
        }
    }

    /**
     * Sets the scope for this profile.
     *
     * @param aProfile       profile
     * @param aApplicability new scope for this profile
     * @throws               <code>SPIException</code> if error occurs
     */
    public void setProfileApplicability(ProfileImpl aProfile,
            Applicability aApplicability) throws SPIException{
        String values [] = { APPLICABILITY_KEY + aProfile.getApplicability() } ;
        removeMultiValuedAttributeValues(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS, 
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
         values[0] = APPLICABILITY_KEY + aApplicability ;
         addValuesToMultiValuedAttribute(
                 ((LdapProfile)aProfile).getLocation(),
                 KEYVALUE_ATTR, values,
                 SUNSERVICE_COMPONENT_OBJCLASS,
                 ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext()) ;
    }

    /**
     * Returns the entities to which this profile has been assigned.
     *
     * @param aProfile  profile
     * @return          <code>Iterator</code> of entities 
     * @throws          <code>SPIException</code> if error occurs 
     */
    public Iterator getAssignedEntities(Profile aProfile) 
        throws SPIException {
        Vector entityList = getListOfEntitiesForProfile(aProfile);
        Vector retCode = new Vector();
        if (!entityList.isEmpty()) {
            int size = entityList.size();
            for (int i = 0 ; i < size ; i++) {
                try {
                    Hashtable attrsValues = (Hashtable)entityList.get(i);
                    Vector values =
                       (Vector)attrsValues.get(DN_KEY);
                    if (values != null && !values.isEmpty()) {
                        String dN = (String)values.get(0);
                        Entity tmpEntity = null;
                        LdapEntity root = null;
                        if (aProfile.getApplicability().equals(
                                Applicability.HOST)) {
                            root = mRootDomain ;
                        }
                        else {
                            root = mRootOrganization ;
                        }
                        if (root != null) {
                            tmpEntity = root.getEntityFromDN(dN, 
                                    LdapEntityType.UNKNOWN, true, null) ;
                            if (tmpEntity != null) { retCode.add(tmpEntity) ; }
                        }
                    }
                } catch (Exception ignore) {
                     /* cannot convert DN to EntityId, so omit from list */
                }
            }
        }
        // for compatiblity with APOC 1, 
        // add containing Entity if Local Profile
        if (isVersion1 && ((LdapProfile)aProfile).isLocal()) {
            Entity profileEntity = aProfile.getProfileRepository().getEntity();
            boolean entityIncluded = false;
            Iterator iterEntities = retCode.iterator();
            while (iterEntities.hasNext()) {
                Entity entity = (Entity)iterEntities.next();
                if (entity.equals(profileEntity)) {
                    entityIncluded = true;
                    break;
                }
            }
            if (!entityIncluded) {
                retCode.add(profileEntity);
            }
        }
        return retCode.iterator();
    }

    /**
     * For a given profile return a list of entries
     * that have this profile assigned to them.
     *
     * @param aProfile    profile
     * @return            <code>Vector</code> listing entries for
    *                     entities to which this profile
     *                    is assigned
     * @throws            <code>SPIException</code> if error occurs
     */
     public Vector getListOfEntitiesForProfile(Profile aProfile)
     	throws SPIException {
        Vector entityList = null;
        StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
        try {
            tmpBuf.append("(&(");
            tmpBuf.append(LDAP_OBJCLASS);
            tmpBuf.append("=");
            tmpBuf.append(SUNSERVICE_COMPONENT_OBJCLASS);
            tmpBuf.append(")(");
            tmpBuf.append(KEYVALUE_ATTR);
            tmpBuf.append("=");
            tmpBuf.append(ASSIGNED_KEY);
            tmpBuf.append(((LdapProfile)aProfile).getLocation());
            tmpBuf.append("))");
            entityList = performSearch(getBaseDN(),
               RECURSIVE_SEARCH,
               (tmpBuf.toString()).toLowerCase(),
               null, true,
               false, null,
               false,
               ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext());
        } catch (SPIException spie) {
            int error = getLdapErrorCode(spie);
            if ( (error != LDAPException.NO_RESULTS_RETURNED)
              && (error != LDAPException.NO_SUCH_ATTRIBUTE)
              && (error != LDAPException.NO_SUCH_OBJECT) ) {
                throw spie;
            }
        }
        return entityList == null ? new Vector(): entityList;
    }

    /**
     * Returns the requested policy object.
     *
     * @param aProfile  the profile
     * @param aId       the id for the required policy
     * @return          the policy object 
     * @throws IllegalReadException if LDAP error occurs
     * @throws SPIException if error occurs 
     */
    public Policy getPolicy(ProfileImpl aProfile, String aId)
    	throws SPIException {
        StringBuffer location = new StringBuffer(BUFFER_LENGTH);
        location.append(CONFIG_NAMING_ATTR);
        location.append(mPolicyIdParser.getStoredFormat(aId));
        location.append(LdapEntity.LDAP_SEPARATOR);
        location.append(((LdapProfile)aProfile).getLocation());
        Hashtable policyTable = null;
        try {
            policyTable = getAttributeValueTable(location.toString(),
                     true, ALL_POLICY_ATTRS, 
                     ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext());
        } catch (LDAPException ldape) {
	        if (ldape.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
                throw new IllegalReadException(
                        IllegalReadException.LDAP_READ_KEY, ldape);
            }
        }
        Policy retCode = null;
        if (policyTable != null && !policyTable.isEmpty()) {
            retCode = getPolicyObject(aId, aProfile, policyTable);
        } 
        return retCode;
    }


    /**
      * Utility function to build the filter corresponding to a list
      * of policy ids for a search, i.e either an overall filter,
      * a single value one or an OR between the possible values.
      *
      * @param aPolicyIds  array of policy ids
      * @param aIdParser  used to build stored policy ids 
      * @return filter
      */
    private static String buildPolicyIdFilter(Iterator aPolicyIds,
                PolicyIdParser aIdParser) {
        StringBuffer retCode = new StringBuffer();
        retCode.append("(");
        if (aPolicyIds.hasNext()) {
            String policyId = (String)aPolicyIds.next();
            if (aPolicyIds.hasNext()) {
                // more than 1 element => multiple criteria
                retCode.append("|") ;
                retCode.append("(").append(CONFIG_NAMING_ATTR) ;
                retCode.append(aIdParser.getStoredFormat(policyId)) ;
                retCode.append(")") ;
                while (aPolicyIds.hasNext()) {
                    policyId = (String)aPolicyIds.next();
                    retCode.append("(").append(CONFIG_NAMING_ATTR) ;
                    retCode.append(aIdParser.getStoredFormat(policyId)) ;
                    retCode.append(")") ;
                }
            } else {
                // 1 element => simple criteria
                retCode.append(CONFIG_NAMING_ATTR) ;
                retCode.append(aIdParser.getStoredFormat(policyId)) ; 
            }
        } else {
            // 0 element => wildcard
            retCode.append(CONFIG_NAMING_ATTR);
            retCode.append(LDAP_WILDCARD);
        }
        retCode.append(")") ;
        return retCode.toString() ;
    }

    /**
     * Destroys a <code>Policy</code>.
     *
     * @param aPolicy        the policy object
     * @throws IllegalReadException if LDAP error occurs
     * @throws SPIException  if error occurs 
     * @throws NoSuchPolicyException if policy not found
     */
    public void destroyPolicy(Policy aPolicy) 
        throws SPIException {
	    try {
	        LdapPolicy ldapPolicy = (LdapPolicy)aPolicy;
	        LdapClientContext context = 
	            ((LdapEntity)ldapPolicy.getProfile()
                    .getProfileRepository().getEntity()).getContext();
	        deleteEntry(ldapPolicy.getLocation(), context);
	    } catch (LDAPException ldape) {
	        int error = ldape.getLDAPResultCode();
	        if (error == LDAPException.NO_SUCH_OBJECT) {
	            throw new NoSuchPolicyException (aPolicy.getId());
		    } else {
                throw new IllegalWriteException(
                        IllegalWriteException.LDAP_WRITE_KEY, ldape);
		    }
        }
    }

    /**
     * Adds policy entry for a given profile, and 
     * returns the DN of this entry. 
     *
     * @param aProfile        profile
     * @param aStoredFormatId name of the component
     * @param aConnection     client LDAP connection
     * @return		          DN for this entry 
     * @throws		          <code>SPIException</code> if
     *                        error occurs
     */
    private String createPolicyEntry(ProfileImpl aProfile,
            						 String aStoredFormatId, 
            						 LDAPConnection aConnection) 
		throws SPIException {
	    StringBuffer tmpBuf = new StringBuffer(BUFFER_LENGTH);
	    try {
            /* create entry for policy */
	        tmpBuf.append(CONFIG_NAMING_ATTR).append(aStoredFormatId).append(
			     LdapEntity.LDAP_SEPARATOR).append( 
			     ((LdapProfile)aProfile).getLocation());
	        /* add an entry for this component */
	        LDAPAttributeSet policyAttrs = new LDAPAttributeSet();
	        policyAttrs.add(new LDAPAttribute(LDAP_OBJCLASS, 
			    POLICY_MAPPING.mObjectClass));
	        policyAttrs.add(POLICY_MAPPING.mServiceIdAttr);
	        addEntry(tmpBuf.toString(), policyAttrs, aConnection);
	    } catch (LDAPException ldape) {
	        throw new IllegalWriteException(
	                IllegalWriteException.LDAP_WRITE_KEY,
	                ldape);
	    }
        return tmpBuf.toString();
    }

    /**
     * Check if the given profile contains any Policy
     * 
     * @param aProfile  profile to look at
     * @return          true if profile contains policies, 
     * 					false otherwise 
     * @throws          <code>SPIException</code> if an error occurs
     */
    public boolean hasPolicies(ProfileImpl aProfile)
   		throws SPIException {
       String filter = buildPolicyIdFilter(new Vector().iterator(),
                       					   mPolicyIdParser);
       Vector policies = getListOfPoliciesForProfile(aProfile, filter, true);
       return ((policies!=null) && (!policies.isEmpty()));
    }

    /**
      * Returns an <code>Vector</code> of policy objects representing 
      * the policies present in a given profile.
      * 
      * @param aProfile  profile to look at
      * @return          list of policy objects 
      * @throws          <code>SPIException</code> if an error occurs
      */
    public Vector getPolicies(ProfileImpl aProfile)
    	throws SPIException {
        String filter = buildPolicyIdFilter(new Vector().iterator(),
                        					mPolicyIdParser);
        return getPolicies(aProfile, filter);
    }

    /**
     * Returns the policies for this profile that match the specified 
     * policy ids.
     *
     * @param aProfile profile to look at
     * @param aPolicyIdList  list of policy ids
     * @return               list of all the policies 
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Vector getPolicies(ProfileImpl aProfile, Iterator aPolicyIdList)
    		throws SPIException {
        String filter = buildPolicyIdFilter(aPolicyIdList,
                							mPolicyIdParser);
        return getPolicies(aProfile, filter);

    }

    /**
     * Returns the policyInfos for this profile that match the specified 
     * policy ids.
     *
     * @param aProfile 		 profile to look at
     * @param aPolicyIdList  list of policy ids
     * @return               list of all the policyInfos
     *                       for this profile
     * @throws               <code>SPIException</code> if error occurs 
     */
    public Vector getPolicyInfos(ProfileImpl aProfile, 
            					 Iterator aPolicyIdList)
    		throws SPIException {
        String filter = buildPolicyIdFilter(aPolicyIdList,
                							mPolicyIdParser);
        return getPolicyInfos(aProfile, filter);
    }

    /**
      * Returns an <code>Vector</code> of policy objects representing 
      * the policies matching the search criteria present in a given 
      * profile.
      * 
      * @param aProfile       profile to look at
      * @param aSearchFilter  search filter
      * @return               list of policy objects 
      * @throws               <code>SPIException</code> if an error occurs
      */
    private Vector getPolicies(ProfileImpl aProfile, String aSearchFilter)
    	throws SPIException {
        Vector policies = getListOfPoliciesForProfile(aProfile, 
                				aSearchFilter, false);
        if (policies == null || policies.isEmpty()) {
            return new Vector();
        }
        Vector retCode = new Vector();
        int size = policies.size();
        for (int i = 0 ; i < size; ++i) {
            Policy policy = getPolicyObject(null,
                    aProfile, (Hashtable)policies.get(i));
            if (policy != null) {
                retCode.add(policy) ;
            }
        } 
        return retCode ;
    }
    
    /**
     * Returns an <code>Vector</code> of PolicyInfo objects
     * matching the search criteria present in a given 
     * profile.
     * 
     * @param aProfile       profile to look at
     * @param aSearchFilter  search filter
     * @return               list of policy objects 
     * @throws               <code>SPIException</code> if an error occurs
     */
    private Vector getPolicyInfos(ProfileImpl aProfile, String aSearchFilter)
   			throws SPIException {
        Vector policies = getListOfPoliciesForProfile(aProfile, 
                				aSearchFilter, true);
        if (policies == null || policies.isEmpty()) {
            return new Vector();
        }
        Vector retCode = new Vector();
        int size = policies.size();
        for (int i = 0 ; i < size; ++i) {
            PolicyInfo policy = getPolicyInfoObject(aProfile,
                    				(Hashtable)policies.get(i));
            if (policy != null) {
                retCode.add(policy) ;
            }
        } 
        return retCode ;
    }
    
    /**
     * Returns an <code>Vector</code> of hashtables representing
     * the policies matching the search criteria present in a given 
     * profile.
     * 
     * @param aProfile       profile to look at
     * @param aSearchFilter  search filter
     * @param aTimestampOnly indicate if search for timestamp of policy only
     * 						 or all attributes of policy
     * @return               list of policy hashtables 
     * @throws               <code>SPIException</code> if an error occurs
     */
    public Vector getListOfPoliciesForProfile(ProfileImpl aProfile, 
            					  String aSearchFilter, boolean aTimestampOnly)
    	throws SPIException {
            if (aProfile == null) {
                throw new IllegalArgumentException();
            }
        Vector policies = null ;
        String[] attrs = aTimestampOnly ? TIMEONLY_POLICY_ATTRS 
                						: ALL_POLICY_ATTRS;
        try {
            policies = performSearch(
                   ((LdapProfile) aProfile).getLocation(),
                   false, aSearchFilter,
                   attrs,
                   true, false,
                   new BooleanReturnValue(false),
                   false,
                   ((LdapEntity)aProfile.getProfileRepository()
                           .getEntity()).getContext()) ;
        } catch (SPIException spie) {
            if (getLdapErrorCode(spie) == LDAPException.NO_SUCH_OBJECT) {
                return new Vector();
            } else {
                throw spie;
            }
        }
        return policies;
    }
    
    /**
      * Stores a policy.
      *
      * @param aProfile  the profile
      * @param aPolicy   policy 
      * @throws          <code>SPIException</code> if error occurs
      */
    public void storePolicy(ProfileImpl aProfile, Policy aPolicy)
    	throws SPIException  {
        LDAPConnection connection = 
            ((LdapEntity)aProfile.getProfileRepository().getEntity())
            		.getContext().getConnection();
        // we have to calculate the dn for the policy to be able
        // to delete it, even though it will be calculated again
        // in createPolicyEntry
        String policyId = aPolicy.getId();
        String storedId = mPolicyIdParser.getStoredFormat(policyId);
	    StringBuffer locationBuf = 
	        new StringBuffer(BUFFER_LENGTH);
	    locationBuf.append(CONFIG_NAMING_ATTR).append(storedId)
        		.append(LdapEntity.LDAP_SEPARATOR)
        		.append(((LdapProfile)aProfile).getLocation());
        String dn = locationBuf.toString();
        if (aPolicy.getData() == null || aPolicy.getData().length() == 0) {
            try {
                connection.delete(dn) ;
            } catch (LDAPException ldape) {
                if (ldape.getLDAPResultCode() != 
                    		LDAPException.NO_SUCH_OBJECT) {
                    throw new IllegalWriteException(
                            IllegalWriteException.LDAP_WRITE_KEY, ldape);
    		    }
            }
        }
        else {
            Change [] update = new Change [1] ;

            update [0] = new Change(KEYVALUE_ATTR, APOC_BLOB_KEY + aPolicy.getData()) ;
            try {
                writeAttributes(dn, SUNSERVICE_COMPONENT_OBJCLASS,
                                update, false, connection) ;
            }
            catch (SPIException spie) {
                if (getLdapErrorCode(spie) 
                        == LDAPException.NO_SUCH_OBJECT) {
                    createPolicyEntry(aProfile, storedId, connection) ;
                    writeAttributes(dn, SUNSERVICE_COMPONENT_OBJCLASS,
                                    update, false, connection) ;
                }
                else { throw spie; }
            }
        }
    }

    /**
     * Adds policy entry. 
     *
     * @param aPolicy	    policy object
     * @param aConnection   client LDAP connection
     * @return		        <code>true</code> if the entry is
     *			            present, otherwise <code>false</code>
     * @throws		        <code>SPIException</code> if
     *                      error occurs
     */
    private void createPolicyEntry(LdapPolicy aPolicy,
            					   LDAPConnection aConnection)
    		throws SPIException{
        Profile existingProfile =
            findProfile(
                    (LdapProfileRepository)aPolicy.getProfile()
                    		.getProfileRepository(), 
                    aPolicy.getProfile().getDisplayName());
        if (existingProfile == null) {
            try {
                /* add the profile entry first */
	            createProfileEntry(aPolicy.getProfile().getProfileRepository(), 
                    aPolicy.getProfile().getDisplayName(), 
                    aPolicy.getProfile().getApplicability(), 
                    aPolicy.getProfile().getPriority(),
                    aConnection);
            } catch (LDAPException ldape) {
                throw new IllegalWriteException(
                        IllegalWriteException.LDAP_WRITE_KEY, ldape);
            }
        }
        try {
            /* then create entry for policy */
	        LDAPAttributeSet policyAttrs = new LDAPAttributeSet();
	        policyAttrs.add(new LDAPAttribute(LDAP_OBJCLASS, 
			POLICY_MAPPING.mObjectClass));
	        policyAttrs.add(POLICY_MAPPING.mServiceIdAttr);
	        addEntry(aPolicy.getLocation(), policyAttrs, aConnection);
	    } catch (LDAPException ldape) {
            throw new IllegalWriteException(
                    IllegalWriteException.LDAP_WRITE_KEY, ldape);
	   }
    }
    /**
      * Adds applicability filter to string buffer.
      * 
      * @param aFilter   filter buffer
      * @param aApplicability  applicability setting
      */
    private void addApplicabilityFilter(StringBuffer aFilter,
            Applicability aApplicability) {
        aFilter.append("(|(");
        aFilter.append(KEYVALUE_ATTR);
        aFilter.append("=");
        aFilter.append(APPLICABILITY_KEY);
        aFilter.append(Applicability.STR_ALL);
        aFilter.append(")");
        if (aApplicability.equals(Applicability.ALL)) {
            aFilter.append("(");
            aFilter.append(KEYVALUE_ATTR);
            aFilter.append("=");
            aFilter.append(APPLICABILITY_KEY);
            aFilter.append(Applicability.STR_USER);
            aFilter.append(")(");
            aFilter.append(KEYVALUE_ATTR);
            aFilter.append("=");
            aFilter.append(APPLICABILITY_KEY);
            aFilter.append(Applicability.STR_HOST);
            aFilter.append(")");
        } else {
            aFilter.append("(");
            aFilter.append(KEYVALUE_ATTR);
            aFilter.append("=");
            aFilter.append(APPLICABILITY_KEY);
            aFilter.append(aApplicability.getStringValue());
            aFilter.append(")");
        }
        aFilter.append(")");
    }

    /**
      * Gets the value for the modifiedtimestamp attribute
      * for this profile.
      *
      * @param aProfile   the profile object
      * @return           value for modifiedtimestamp
      *                   attribute and author
      * @throws           <code>SPIException</code> if error
      *                   occurs
      */
    public ArrayList getModificationDetails(LdapProfile aProfile) 
        throws SPIException {
        return getModificationDetails(aProfile.getLocation(),
                    ((LdapEntity)aProfile.getProfileRepository().
                            getEntity()).getContext());
    }

    /**
      * Gets the value for the modifiedtimestamp attribute
      * for this policy.
      *
      * @param aPolicy   the policy object
      * @return          value for modifiedtimestamp
      *                  attribute and author
      * @throws          <code>SPIException</code> if error
      *                  occurs
      */
    public ArrayList getModificationDetails(LdapPolicy aPolicy) 
        throws SPIException {
        return getModificationDetails(aPolicy.getLocation(),
                    ((LdapEntity)aPolicy.getProfile().getProfileRepository().getEntity()).getContext());
    }

    /**
      * Gets the value for the modifiedtimestamp attribute
      * for this DN.
      *
      * @param aDN          the DN for the entry
      * @param aContext     the client context
      * @return             String value for modifiedtimestamp
      *                     attribute
      * @throws             <code>SPIException</code> if error
      *                     occurs
      */
    public ArrayList getModificationDetails(String aDN, 
            								LdapClientContext aContext)
        	throws SPIException {
        Vector entries = null;
        String attributes[] = {MODIFY_TIMESTAMP_ATTR,
                               MODIFY_AUTHOR_ATTR};
        try {
            entries = getAttributeValueList(aDN, attributes, aContext);
        } catch (LDAPException ldape) {
            throw new IllegalReadException(
                    IllegalReadException.LDAP_READ_KEY, ldape);
        }
        if (entries == null || entries.size() == 0) {
            throw new IllegalReadException(
                    IllegalReadException.LDAP_READ_KEY);
        }
        ArrayList retCode = new ArrayList();
        // lastmodified timestamp
        retCode.add(entries.get(0));
        // author if there
        if (entries.size() > 1) {
	        String authorDN = (String)entries.get(1);
	        if ((authorDN != null) && (mRootOrganization != null)) {
	            Entity author = mRootOrganization.getEntity(authorDN);
	            if (author != null) {
	                retCode.add(author);
	            }
	        }
        }
        return retCode;
    }

    /**
     * Gets the comment for this profile.
     *
     * @param aProfile   the profile
     * @return           String value for comment
     * @throws IllegalReadException if LDAP error occurs
     * @throws SPIException if error occurs
     */
    public String getProfileComment(Profile aProfile)
        throws SPIException {
        Vector entries = null;
        String attributes[] = {KEYVALUE_ATTR};
        try {
            entries = getAttributeValueList(
                    ((LdapProfile)aProfile).getLocation(), attributes,
                    ((LdapEntity)aProfile.getProfileRepository().getEntity()).getContext());
        } catch (LDAPException ldape) {
            throw new IllegalReadException(
                    IllegalReadException.LDAP_READ_KEY, ldape);
        }
        String comment = null;
        if (entries != null || !entries.isEmpty()) {
            String valuesArray[] = 
                 getValuesForKey(entries, COMMENT_KEY);
            if (valuesArray.length != 0) {
                comment = valuesArray[0];
            }
        }
        return comment;

    }

    /**
     * Class used for converting policy ids to/from storage
     * format.
     */
    public class PolicyIdParser
    {
	    /** table matching requested policy ids
	        to the string used in the backend store */
	    Hashtable policyIds = new Hashtable();
	    /** String buffer used during conversion */
	    StringBuffer compBuf = new StringBuffer(BUFFER_LENGTH);
	    /** escape character */
	    char ESCAPE_CHAR = '/';
	
        /**
         * Returns the policy id in the format used in the storage
         * backend. Capital letters and forward slashes are escaped with
         * a forward slash.
         *
         * @param aPolicyId   id of policy requested by client
         * @return            id as stored in the backend
         */
        public String getStoredFormat(String aPolicyId) {
            if (aPolicyId == null) { return null; }
            String returnStr =
		        (String)policyIds.get(aPolicyId);
            if (returnStr != null) { return returnStr; }
            compBuf.delete(0, compBuf.length());
            String allLowerCase = aPolicyId.toLowerCase();
            char [] policyIdArray = aPolicyId.toCharArray();
            char [] allLowerCaseArray = allLowerCase.toCharArray();
            for (int i = 0; i < policyIdArray.length; ++i) {
 	            if (policyIdArray[i] == ESCAPE_CHAR ||
		            policyIdArray[i] != allLowerCaseArray[i]) {
	                compBuf.append(ESCAPE_CHAR);
	            }
	            compBuf.append(policyIdArray[i]);
	        }
	        returnStr = compBuf.toString();
	        policyIds.put(aPolicyId, returnStr);
	        return returnStr;
        }

        /**
         * Returns the policy id in the output format.
         * Forward slashes are removed, and capital letters
         * substituted as appropriate. 
         *
         * @param aStoredPolicyId  policy id in storage format 
         * @return                 policy id in output format
         */
        public String getOutputFormat(String aStoredPolicyId) {
	        if (aStoredPolicyId == null) { return null; }
            compBuf.delete(0, compBuf.length());
            char [] storedPolicyIdArray =
		        aStoredPolicyId.toCharArray();
            String allUpperCase = aStoredPolicyId.toUpperCase();
            char [] allUpperCaseArray = allUpperCase.toCharArray();
            for (int i = 0; i < storedPolicyIdArray.length; ++i) {
		        if (storedPolicyIdArray[i] == ESCAPE_CHAR) {
		            compBuf.append(allUpperCaseArray[++i]);
		        } else {
	                compBuf.append(storedPolicyIdArray[i]);
		        }
            }
	        String returnStr = compBuf.toString();
	        policyIds.put(returnStr, aStoredPolicyId);
	        return returnStr;
        }
    }

}

