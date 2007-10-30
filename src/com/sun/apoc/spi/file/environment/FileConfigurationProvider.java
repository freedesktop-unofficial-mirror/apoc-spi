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
package com.sun.apoc.spi.file.environment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.environment.ConfigurationProvider;
import com.sun.apoc.spi.environment.RemoteEnvironmentException;

public class FileConfigurationProvider implements ConfigurationProvider {
    
    protected String[] mFileURLs;
    
    /**
     * Constructor
     * 
     * @param urls	urls of the files to get 
     * 				the configuration data from
     */
    public FileConfigurationProvider(String[] urls) {
        mFileURLs = urls;
    }

    /**
     * Loads the configuration data from the different files 
     * and put them all in the same Hashtable returned
     * 
     * @return	the Hashtable containing all the parameters
     * 			read from the different files
     * @throws SPIException	if error occurs
     * @see com.sun.apoc.spi.environment.ConfigurationProvider#loadData()
     */
    public Hashtable loadData() throws SPIException {
        Hashtable data = new Hashtable();
        if (mFileURLs != null) {
            for (int i=0; i<mFileURLs.length; i++) {
                data.putAll(loadFileData(mFileURLs[i]));
            }
        }
        return data;
    }
    
    /**
     * Loads the configuration data from the specified file
     * and return a Hashtable containing them
     * 
     * @param fileURL		url of the file to get the data from
     * @return				the Hashtable containing the parameters
     * 						read in the file
     * @throws SPIException	if error occurs
     */
    private Hashtable loadFileData(String fileURL) throws SPIException {
	    Properties propertyList  = new Properties();
	    try {
	        URL url = new URL(fileURL);
	        InputStream input = url.openStream();
	        propertyList.load(input);
	        input.close();
        } catch (MalformedURLException mue) {
	        throw new RemoteEnvironmentException(
	                RemoteEnvironmentException.FILE_CONF_KEY,
	                new Object[]{fileURL}, mue);
        } catch (IOException ioe) {
	        throw new RemoteEnvironmentException(
	                RemoteEnvironmentException.FILE_CONF_KEY,
	                new Object[]{fileURL}, ioe);
        }
	    return propertyList;
    }
}
