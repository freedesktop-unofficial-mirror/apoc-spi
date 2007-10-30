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

package com.sun.apoc.spi.file.entities;

import com.sun.apoc.spi.PolicySource;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale ;
import java.util.StringTokenizer;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.AbstractEntity;
import com.sun.apoc.spi.entities.Entity;

public class FileEntity extends AbstractEntity
{
    public static final String ENTITY_SEPARATOR = "_";
    public static final String DOMAIN_TAG = "Dom";
    public static final String ORGANIZATION_TAG = "Org";
    
    private Entity mParent;
    private String mDisplayName ;
        
    public FileEntity( String aDisplayName, String aId, Entity aParent, PolicySource aPolicySource )
    {
        mDisplayName = aDisplayName;
        mId = aId;
        mParent = aParent;
        mPolicySource = aPolicySource;
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Entity#getDisplayName()
     */
    public String getDisplayName(Locale aLocale)
    {       
        return mDisplayName;
    }

    public Iterator getAncestorNames(Locale aLocale) {
        LinkedList ancestorNames = new LinkedList();
        StringTokenizer elements = new StringTokenizer(mId, FileEntity.ENTITY_SEPARATOR);
        if (elements.hasMoreTokens()) {
            String rootElement = elements.nextToken();
            if (rootElement.endsWith(FileEntity.ORGANIZATION_TAG)) {
                int newRootElementLength = rootElement.length() - FileEntity.ORGANIZATION_TAG.length();
                rootElement = rootElement.substring(0, newRootElementLength);
            }
            else if (rootElement.endsWith(FileEntity.DOMAIN_TAG)) {
                int newRootElementLength = rootElement.length() - FileEntity.DOMAIN_TAG.length();
                rootElement = rootElement.substring(0, newRootElementLength);
            }
            ancestorNames.add(rootElement);
        }
        while (elements.hasMoreTokens()) {
            ancestorNames.add(elements.nextToken());
        }
        // the last element is the name of this entity, so it's not part of the ancestors
        if (ancestorNames.size() > 0) {
            ancestorNames.removeLast();
        }
        return ancestorNames.iterator();
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Entity#getParent()
     */
    public Entity getParent()
    {
        return mParent;
    }

    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Entity#getLayeredProfiles()
     */
    public Iterator getLayeredProfiles() throws SPIException
    {   
        Iterator parents = getAllParents();
        return getLayeredProfiles(parents, null);
    }
}
