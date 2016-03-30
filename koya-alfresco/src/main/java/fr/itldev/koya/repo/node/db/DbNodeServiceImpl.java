package fr.itldev.koya.repo.node.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import fr.itldev.koya.repo.model.filefolder.NaturalOrderComparator;


public class DbNodeServiceImpl extends org.alfresco.repo.node.db.DbNodeServiceImpl {
	
	@Override
	public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef,
			QNamePattern typeQNamePattern, QNamePattern qnamePattern,
			int maxResults, boolean preload) {
		// TODO Auto-generated method stub
		List<ChildAssociationRef> results =  super.getChildAssocs(nodeRef, typeQNamePattern, qnamePattern,
				maxResults, preload);
		
		Collections.sort(results, new NodeRefChildTitleComparator());
		
		return results;
	}
	@Override
	public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef,
			Set<QName> childNodeTypeQNames) {
		// TODO Auto-generated method stub
		List<ChildAssociationRef> results = super.getChildAssocs(nodeRef, childNodeTypeQNames);
		
		Collections.sort(results, new NodeRefChildTitleComparator());
		
		return results;
	}
	
	
	private class NodeRefChildTitleComparator extends NaturalOrderComparator<ChildAssociationRef> {
		@Override
		protected String stringify(ChildAssociationRef childAssociationRef) {
			Serializable title = getProperty(childAssociationRef.getChildRef(), ContentModel.PROP_TITLE);
			return (title!=null)?title.toString():getProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME).toString();
		}
	}
	
}
