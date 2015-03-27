/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.itldev.koya.repo.model.filefolder;

import java.util.List;
import java.util.Set;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQueryFactory;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 *
 * @author nico
 */
public class KoyaGetChildrenCannedQuerryFactory extends GetChildrenCannedQueryFactory {
    private HiddenAspect hiddenAspect;
    private Set<QName> ignoreAspectQNames;
    
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }
    
    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters) {

        NodePropertyHelper nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO);
        
        return (CannedQuery<NodeRef>) new KoyaGetChildrenCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, nodeService, methodSecurity, parameters, hiddenAspect, dictionaryService, ignoreAspectQNames);
    }

    @Override
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, String pattern,
            Set<QName> assocTypeQNames, Set<QName> childTypeQNames,
            Set<QName> ignoreAspectQNames, List<FilterProp> filterProps,
            List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest) {
        
        return super.getCannedQuery(parentRef, pattern, assocTypeQNames, childTypeQNames, ignoreAspectQNames, filterProps, sortProps, pagingRequest);
    }
    
    
}
