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

package com.sun.apoc.spi.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.policies.Policy;
import com.sun.apoc.spi.profiles.Applicability;
import com.sun.apoc.spi.profiles.Profile;
import com.sun.apoc.spi.profiles.ProfileRepository;
import com.sun.apoc.spi.profiles.ProfileStreamException;
import com.sun.apoc.spi.profiles.ProfileZipException;
import com.sun.apoc.spi.profiles.ZipProfileReadWrite;
import java.util.ArrayList;
/**
 * Implementation of the importer/exporter of profiles in ZIP file format.
 */
public class ZipImporterExporter implements ImporterExporter {
    
    /**
     * Exports the contents of a profile to a stream.
     *
     * @param aProfile      profile identifier
     * @param aOutput       stream for exported data
     * @throws SPIException if an error occurs.
     */
    public void exportProfile(Profile aProfile, OutputStream aOutput)
    throws SPIException {
        ZipOutputStream output = new ZipOutputStream(aOutput);
        try {
            ZipProfileReadWrite.writeMetaData(aProfile, output);
            ZipProfileReadWrite.writePolicies(aProfile, output);
            output.close();
            
        } catch (ZipException ze) {
            try {
                output.close();
            } catch (Exception ignored) {}
            throw new ProfileZipException(ze);
            
        } catch (IOException ioe) {
            try {
                output.close();
            } catch (Exception ignored) {}
            throw new ProfileStreamException(
                    ProfileStreamException.ERROR_STREAM_KEY, ioe);
        }
    }
    
    
    
    /**
     * Imports the contents of a stream to a profile.
     * If a profile with this name already exists then
     * it is overwritten.
     *
     * @param aRepository      profile repository
     * @param aDisplayName     profile display name
     * @param aApplicability   scope
     * @param aInput           stream containing data to import
     *
     * @throws SPIException if an error occurs.
     */
    public void importProfile(ProfileRepository aRepository,
            String aDisplayName, Applicability aApplicability,
            InputStream aInput) throws SPIException {
        ZipInputStream input = new ZipInputStream(aInput);
        try {
            // first try to read the meta configuration data
            Properties metaData = ZipProfileReadWrite.readMetaData(input);
            Iterator it = null;
            if (metaData != null) {
                Applicability zippedApplicability = Applicability.getApplicability(metaData.getProperty(ZipProfileReadWrite.APPLICABILITY));
                if (!(aApplicability.equals(zippedApplicability))) {
                    Object[] obj = {zippedApplicability.getStringValue(), aApplicability.getStringValue()};
                    throw new SPIException("error.spi.profile.import", obj);
                }
                // then try to load the policy data
                it = ZipProfileReadWrite.readPolicies(input);
            } else {
                aApplicability  = Applicability.USER;
                // if meta data entry does not exist, we assume the old
                // profile/policy format
                it = ZipProfileReadWrite.readOldPoliciesFormat(input);
                // if 'it' contains no data that means it is either an old
                // format source but with no policies in it or that the source
                // doesn't have the required format (new or old)
                // in both cases, raise an exception.
                if (!it.hasNext()) {
                    input.close();
                    throw new ProfileStreamException(ProfileStreamException.EMPTY_STREAM_KEY);
                }
            }
            input.close();
            
            // check if a profile with the same name and applicability already exists - in that
            // case we want to overwrite it
            Iterator profiles = aRepository.getProfiles(aApplicability);
            ArrayList assignedEntities = new ArrayList();
            while (profiles.hasNext()) {
                Profile profile = (Profile)profiles.next();
                if (profile != null) {
                    if (profile.getDisplayName().equals(aDisplayName)) {
                        Applicability use = profile.getApplicability();
                        if (use.equals(aApplicability)) {
                            Iterator entityIt = profile.getAssignedEntities();
                            while(entityIt.hasNext()) {
                                Entity entity = (Entity)entityIt.next();
                                assignedEntities.add(entity);
                                entity.unassignProfile(profile);
                            }
                            aRepository.destroyProfile(profile);
                        }
                    }
                }
            }
            
            // create a new profile with specified name and applicability
            Profile profile = aRepository.createProfile(aDisplayName, aApplicability);
            
            // store the policies
            while(it.hasNext()) {
                profile.storePolicy((Policy) it.next());
            }
            
            // update the meta data
            if (metaData != null) {
                profile.setComment(metaData.getProperty(ZipProfileReadWrite.COMMENT));                
            }
            
            if (assignedEntities.size() != 0) {
                Iterator entityIt = assignedEntities.iterator();
                if (entityIt != null) {
                    while (entityIt.hasNext()) {
                        Entity entityToAssign = (Entity)entityIt.next();
                        entityToAssign.assignProfile(profile);
                    }
                }
            }
            
        } catch (ZipException ze) {
            try {
                input.close();
            } catch (Exception ignored) {}
            throw new ProfileZipException(ze);
            
        } catch (IOException ioe) {
            try {
                input.close();
            } catch (Exception ignored) {}
            throw new ProfileStreamException(
                    ProfileStreamException.ERROR_STREAM_KEY, ioe);
        }
    }
    
}

