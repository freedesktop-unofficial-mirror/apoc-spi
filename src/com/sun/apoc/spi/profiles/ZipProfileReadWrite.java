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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.policies.Policy;


/**
 * Implementation of the Profile read/write in ZIP file format.
 *
 * Policies are stored as xml files where each file represents one single
 * component. The content of each file is the XML-bob of the policy. In
 * addition, the first entry of each zip file corresponds to the meta
 * configuration data such as display name, applicability, etc.
 *
 * In APOC 1.x versions the format of the zip file was slightly different. Here
 * policies are stored in a tree structure where each item of the
 * component name is a subdirectory and the policy itself is stored
 * in a file named after the last item. The whole tree is then zipped
 * into a single file. The tree structure is identical to the one found
 * in the registry/data repositories in an office installation.
 *
 * As an example, if the profile contains org.openoffice.Inet and
 * org.openoffice.Office.Common, the ZIP file will contain two entries
 * (old format):
 * - org/openoffice/Inet.xcu (containing the policy data) and
 * - org/openoffice/Office/Common.xcu (containing the policy data)
 *
 * Or using the new format it will contain:
 * - META-INF/general.properties (containing the meta configuration data)
 * - org.openoffice.Inet.xml (containing the policy data) and
 * - org.openoffice.Office.Common.xml (containing the policy data)
 */
public class ZipProfileReadWrite {
    /**
     * The current product version
     */
    public static final String PRODUCT_VERSION = "2.0";
    
    /**
     * Constants for accessing/storing the meta configuration data
     */
    public static final String APOC_VERSION   = "ApocVersion";
    public static final String DISPLAY_NAME   = "DisplayName";
    public static final String COMMENT        = "Comment";
    public static final String APPLICABILITY  = "Applicability";
    public static final String PRIORITY       = "Priority";
    public static final String AUTHOR         = "Author";
    public static final String LAST_MODIFIED  = "LastModified";
    
    /**
     * The zip file entry for storing the meta configuration data
     */
    public static final String META_INF_ENTRY = "META-INF/general.properties";
    
    /**
     * Suffix appended to entries holding policy data.
     */
    public static final String ENTRY_SUFFIX = ".xml";
    
    /**
     * Encoding of the policy data in the entries.
     */
    public static final String ENTRY_ENCODING = "UTF-8";
    
    /**
     * Component items separator as a character.
     */
    public static final char COMPONENT_SEPARATOR = '.';
    
    /**
     * Entry components separator.
     */
    public static final char ENTRY_SEPARATOR = '/';
    
    /**
     * Old suffix appended to entries holding policy data.
     */
    public static final String OLD_ENTRY_SUFFIX = ".xcu";
    
    
    
    /**
     * Writes the meta configuration data of a given profile to a stream.
     *
     * @param aProfile      profile identifier
     * @param aOutput       stream for exported data
     * @throws SPIException if an error occurs.
     */
    public static void writeMetaData(Profile aProfile,
            ZipOutputStream aOutput) throws IOException, SPIException {
        // collect all the meta data information
        Properties metaData = new Properties();
        metaData.put(APOC_VERSION, PRODUCT_VERSION);
        metaData.put(DISPLAY_NAME, aProfile.getDisplayName());
        metaData.put(COMMENT, aProfile.getComment());
        metaData.put(AUTHOR, aProfile.getAuthor());
        metaData.put(PRIORITY, new Integer(aProfile.getPriority()).toString());
        metaData.put(APPLICABILITY, aProfile.getApplicability().getStringValue());
        metaData.put(LAST_MODIFIED, Long.toString(aProfile.getLastModified()));
        
        // create new zip entry
        ZipEntry entry = new ZipEntry(META_INF_ENTRY);
        aOutput.putNextEntry(entry);
        
        // store the meta data information
        metaData.store(aOutput, null);
        aOutput.closeEntry();
    }
    
    /**
     * Actual export of policies from the specified profile.
     * The policy names are transformed into ZIP entry names and the
     * policy data becomes the content of the entry.
     *
     * @param aProfile      profile identifier
     * @param aOutput      stream to export the data to
     * @throws SPIException if an IO error occurs.
     * @throws ProfileZipException if an error occurs
     * @throws ProfileStreamException if an error occurs
     */
    public static void writePolicies(Profile aProfile, ZipOutputStream aOutput)
    throws IOException, SPIException {
        Iterator it = aProfile.getPolicies();
        while (it.hasNext()) {
            Policy policy = (Policy) it.next();
            StringBuffer buffer = new StringBuffer(policy.getId());
            buffer.append(ENTRY_SUFFIX);
            
            // create new zip entry for each policy
            ZipEntry entry = new ZipEntry(buffer.toString());
            entry.setTime(policy.getLastModified());
            aOutput.putNextEntry(entry);
            
            // and store the content of the policy
            byte[] data = policy.getData().getBytes(ENTRY_ENCODING);
            aOutput.write(data, 0, data.length);
            aOutput.closeEntry();
        }
    }
    
    /**
     * Reads the meta configuration data from a stream.
     *
     * @param aInput stream for inported data
     */
    public static Properties readMetaData(ZipInputStream aInput)
    throws IOException {
        Properties metaData = null;
        ZipEntry entry = aInput.getNextEntry();
        if (entry != null) {
            if (entry.getName().equals(META_INF_ENTRY)) {
                metaData = new Properties();
                metaData.load(aInput);
                aInput.closeEntry();
            }
        }
        return metaData;
    }
    
    /**
     * Reads the policies from a stream.
     *
     * @param aInput stream for inported data
     */
    public static Iterator readPolicies(ZipInputStream aInput)
    throws IOException {
        ArrayList policies = new ArrayList();
        ZipEntry entry = aInput.getNextEntry();
        while (entry != null) {
            // determine policy name
            String name = entry.getName();
            int pos = name.indexOf(ENTRY_SUFFIX);
            if (pos > 0) {
                name = name.substring(0, pos);
                
                // retrieve policy data
                byte[] buffer = new byte[1024] ;
                StringBuffer data = new StringBuffer();
                while (aInput.available() != 0) {
                    int bytesRead = aInput.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        data.append(new String(buffer, 0, bytesRead, ENTRY_ENCODING));
                    }
                }
                Policy policy = new Policy(name, data.toString(), entry.getTime());
                policies.add(policy);
            }
            
            aInput.closeEntry() ;
            entry = aInput.getNextEntry() ;
        }
        return policies.iterator();
    }
    
    
    /**
     * Actual import of policy data (old format) from a ZIP file.
     *
     * Note: in that case, the first ZipEntry has already been read
     * before this method is called by importProfile
     * (in readMetaConfigurationData) but it will always be a directory
     * because every policy is stored in at least one-level deep directory
     * and directories are skipped by this method so no useful data is lost.
     *
     * @param aInput stream containing the zipped data
     * @throws SPIException if an IO error occurs.
     * @throws ProfileZipException if an error occurs
     * @throws ProfileStreamException if an error occurs
     */
    public static Iterator readOldPoliciesFormat(ZipInputStream aInput)
    throws IOException {
        ArrayList policies = new ArrayList();
        ZipEntry entry = aInput.getNextEntry() ;
        while (entry != null) {
            if (!entry.isDirectory()) {
                String name = entry.getName() ;
                int usefulSize = name.length() - OLD_ENTRY_SUFFIX.length();
                if (!(usefulSize <= 0 ||
                        !name.substring(usefulSize).equals(OLD_ENTRY_SUFFIX))) {
                    name = name.substring(0, usefulSize).replace(ENTRY_SEPARATOR,
                            COMPONENT_SEPARATOR);
                    byte [] buffer = new byte[1024] ;
                    StringBuffer data = new StringBuffer();
                    while (aInput.available() != 0) {
                        int bytesRead = aInput.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            data.append(new String(buffer, 0, bytesRead, ENTRY_ENCODING));
                        }
                    }
                    Policy policy = new Policy(name, data.toString());
                    policies.add(policy);
                }
            }
            aInput.closeEntry() ;
            entry = aInput.getNextEntry() ;
        }
        return policies.iterator();
    }
}
