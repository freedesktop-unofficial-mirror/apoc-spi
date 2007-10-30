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

package com.sun.apoc.spi.memory.entities;

import com.sun.apoc.spi.PolicySource;
import com.sun.apoc.spi.entities.EntityTreeProvider;
import com.sun.apoc.spi.entities.OrganizationTreeProvider;
import com.sun.apoc.spi.entities.DomainTreeProvider;
import com.sun.apoc.spi.entities.Organization;
import com.sun.apoc.spi.entities.Domain;
import com.sun.apoc.spi.entities.Entity;
import com.sun.apoc.spi.entities.Node;
import com.sun.apoc.spi.SPIException;


public class EntityProviderImpl implements EntityTreeProvider, 
    OrganizationTreeProvider, DomainTreeProvider
{
    protected OrganizationImpl m_orgRootEntity = null;
    protected DomainImpl m_domainRootEntity = null;
    protected PolicySource m_mgr = null;
    
    public EntityProviderImpl(PolicySource mgr) {
        m_mgr = mgr;
    }
    
    public Node getRootEntity() throws SPIException {
        return m_orgRootEntity;
    }
    
    public Organization getRootOrganization() {
        return m_orgRootEntity;
    }
    
    public Domain getRootDomain() {
        return m_domainRootEntity;
    }
    
    public Entity getEntity(String id) throws SPIException {
        Entity entity = m_orgRootEntity.getEntity(id);
        if (entity == null) {
            m_domainRootEntity.getEntity(id);
        }
        return entity;
    }
    
    
    public void open() {
        initializeSampleData();
    }
    
    
    public void close() {
        m_mgr = null;
        m_orgRootEntity = null;
    }
    
    protected void initializeSampleData() {
        m_orgRootEntity = new OrganizationImpl(m_mgr, null, "o=ITCompany", "Organization Tree Root");
        EntityNodeImpl subOrg1 = new OrganizationImpl(m_mgr, m_orgRootEntity, "o=subOrg1, o=ITCompany", "Customer Care");
        EntityNodeImpl subOrg2 = new OrganizationImpl(m_mgr, m_orgRootEntity, "o=subOrg2, o=ITCompany", "Development");
        EntityNodeImpl subOrg3 = new OrganizationImpl(m_mgr, m_orgRootEntity, "o=subOrg3, o=ITCompany", "Sales");
        EntityNodeImpl subOrg4 = new OrganizationImpl(m_mgr, m_orgRootEntity, "o=subOrg4, o=ITCompany", "Security");
        m_orgRootEntity.addChild(subOrg1);
        m_orgRootEntity.addChild(subOrg2);
        m_orgRootEntity.addChild(subOrg3);
        m_orgRootEntity.addChild(subOrg4);
        
        EntityNodeImpl subOrg11 = new OrganizationImpl(m_mgr, subOrg1, "o=suborgA, o=subOrg1, o=ITCompany", "Call Center");
        EntityNodeImpl subOrg12 = new OrganizationImpl(m_mgr, subOrg1, "o=suborgB, o=subOrg1, o=ITCompany", "Representatives");
        subOrg1.addChild(subOrg11);
        subOrg1.addChild(subOrg12);
        
        EntityNodeImpl subOrg21 = new OrganizationImpl(m_mgr, subOrg2, "o=suborgA, o=subOrg2, o=ITCompany", "Hardware");
        EntityNodeImpl subOrg22 = new OrganizationImpl(m_mgr, subOrg2, "o=suborgB, o=subOrg2, o=ITCompany", "Software");
        subOrg2.addChild(subOrg21);
        subOrg2.addChild(subOrg22);
        
        EntityNodeImpl subOrg31 = new OrganizationImpl(m_mgr, subOrg3, "o=suborgA, o=subOrg3, o=ITCompany", "Call Center");
        EntityNodeImpl subOrg32 = new OrganizationImpl(m_mgr, subOrg3, "o=suborgB, o=subOrg3, o=ITCompany", "Representatives");
        subOrg3.addChild(subOrg31);
        subOrg3.addChild(subOrg32);
        
        Entity sample_user = new UserImpl(m_mgr, subOrg4, "uid=jclarke, o=subOrg4, o=ITCompany", "jmonroe");
        subOrg4.addChild(sample_user);
        
        for(int i=0; i < 10; i++) {
            StringBuffer id = new StringBuffer("uid=user");
            id.append(i);
            id.append(", o=subOrg1, o=ITCompany");
            StringBuffer name = new StringBuffer("user");
            name.append(i);
            Entity sample_user2 = new UserImpl(m_mgr, subOrg1, id.toString(), name.toString());
            subOrg1.addChild(sample_user2);
        }
        
        m_domainRootEntity = new DomainImpl(m_mgr, null, "dc=Sun", "Domain Tree Root");
        EntityNodeImpl subDomain = new DomainImpl(m_mgr, m_domainRootEntity, "dc=destiny, dc=Sun", "Destiny");
        m_domainRootEntity.addChild(subDomain);
        
        for(int i=0; i < 10; i++) {
            StringBuffer id = new StringBuffer("ou=host");
            id.append(i);
            id.append(", dc=Sun, dc=destiny");
            StringBuffer name = new StringBuffer("host");
            name.append(i);
            Entity host = new HostImpl(m_mgr, subDomain, id.toString(), name.toString());
            subDomain.addChild(host);
        }
    }
}
