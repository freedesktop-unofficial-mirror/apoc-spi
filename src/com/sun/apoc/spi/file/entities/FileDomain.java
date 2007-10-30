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
import java.util.Vector;
import com.sun.apoc.spi.SPIException;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Host;


public class FileDomain extends FileNode implements Domain {
    
    /**
     * @param aDisplayName
     * @param aId
     * @param aParent
     * @param aAssignment
     */
    public FileDomain(String aDisplayName, String aId, Entity aParent, PolicySource aPolicySource) {
        super(aDisplayName, aId, aParent, aPolicySource);
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Domain#getHosts()
     */
    public Iterator getHosts() throws SPIException {
        return getLeaves();
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Domain#getSubDomains()
     */
    public Iterator getSubDomains() throws SPIException {
        return getNodes();
    }
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Node#findEntities(java.lang.String, boolean)
     */
    public Iterator findEntities(String aFilter, boolean aIsRecursive)
    throws SPIException    
    {
        Iterator nodeIt;
        Iterator leafIt;
        Vector entities = new Vector();
        
        
        
        nodeIt = ((Domain)this).findSubDomains(aFilter, aIsRecursive);
        leafIt = ((Domain)this).findHosts(aFilter, aIsRecursive);
        
        while (nodeIt.hasNext()) {
            entities.add(nodeIt.next());
        }
        while (leafIt.hasNext()) {
            entities.add(leafIt.next());
        }
        
        return entities.iterator();
    }
    
    
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Domain#findSubDomains(java.lang.String, boolean)
     */
    public Iterator findSubDomains(String aFilter, boolean aIsRecursive)
    throws SPIException {
        Iterator domainIt;
        Domain subDomain;
        Vector foundDomains;
        Vector domains;
        Iterator it;
        String domainId = getIdFromFilter(aFilter);
        boolean filterIds = domainId.length() != 0 ;
        
        foundDomains = new Vector();
        domains = getDomains(this,aIsRecursive);
        domainIt = domains.iterator();
        while (domainIt.hasNext()) {
            Domain domain = (Domain)domainIt.next();
            if (!filterIds || domain.getId().indexOf(domainId) != -1) {
                foundDomains.add(domain);
            }
        }
        return foundDomains.iterator();
    }
    
    private Vector getDomains(Domain aDomain, boolean aIsRecursive)
    throws SPIException {
        Vector domains;
        Iterator domainsIt;
        
        domains = new Vector();
        domainsIt = aDomain.getSubDomains();
        while (domainsIt.hasNext()) {
            Domain domain = (Domain)domainsIt.next();
            domains.add(domain);
            if (aIsRecursive) {
                domains.addAll(getDomains(domain, aIsRecursive));
            }
        }
        
        return domains;
    }
    
     
    /* (non-Javadoc)
     * @see com.sun.apoc.spi.entities.Domain#findHosts(java.lang.String, boolean)
     */
    public Iterator findHosts(String aFilter, boolean aIsRecursive)
    throws SPIException {
        Iterator hostIt;
        Domain subDomain;
        Vector foundHosts;
        String hostId;
        Iterator it;
        
        foundHosts = new Vector();
        hostId = getIdFromFilter(aFilter);
        
        hostIt = findInLeafs( hostId, this);
        if ( hostIt.hasNext() || !aIsRecursive ) {
            return hostIt;
        } else {
            it = getSubDomains();
            while ( it.hasNext() ) {
                subDomain = (Domain)it.next();
                hostIt = subDomain.findHosts( hostId, aIsRecursive );
                while (hostIt.hasNext()) {
                    foundHosts.add(hostIt.next());
                }
            }
        }
        
        return foundHosts.iterator();
    }
    
    private Iterator findInLeafs( String aHostId, Domain aDomain ) throws SPIException {
        Iterator it;
        Host host;
        Vector v;
        boolean filterIds = aHostId.length() != 0 ;
        
        v = new Vector();
        it = aDomain.getHosts();
        while ( it.hasNext() ) {
            host = (Host)it.next();
            if (!filterIds || host.getId().indexOf(aHostId) != -1) {
                v.add(host);
                
            }
        }
        return v.iterator();
    }
}
