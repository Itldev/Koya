/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.Resource;

import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.PaginatedContentList;
import fr.itldev.koya.services.DossierService;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class DossierServiceImpl extends AlfrescoRestService implements
        DossierService {

    private static final String REST_GET_CREATEDOSSIER = "/s/fr/itldev/koya/dossier/create/{parentNodeRef}?title={title}";

   
    private static final String REST_GET_LISTRESP = "/s/fr/itldev/koya/dossier/resp/list/{nodeRef}";

    private static final String REST_GET_LISTMEMBERS = "/s/fr/itldev/koya/dossier/member/list/{nodeRef}";

    private static final String REST_GET_ADDRESP = "/s/fr/itldev/koya/dossier/resp/add/{userName}/{nodeRef}";
    private static final String REST_GET_ADDMEMBER = "/s/fr/itldev/koya/dossier/member/add/{userName}/{nodeRef}";

    private static final String REST_GET_DELRESP = "/s/fr/itldev/koya/dossier/resp/del/{userName}/{nodeRef}";

    private static final String REST_CONFIDENTIAL = "/s/fr/itldev/koya/dossier/confidential/{nodeRef}";
    
    private static final String REST_SUMMARY = "/s/fr/itldev/koya/dossier/summary/{nodeRef}?documentName={documentName}";

    private KoyaContentService KoyaContentService;
	private CacheManager cacheManager;


    public void setKoyaContentService(KoyaContentService KoyaContentService) {
        this.KoyaContentService = KoyaContentService;
    }

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

    @Override
    public Dossier create(User user, Space parentSpace, String title)
            throws AlfrescoServiceException {
        return fromJSON(
                new TypeReference<Dossier>() {
                },
                user.getRestTemplate().getForObject(
                        getAlfrescoServerUrl() + REST_GET_CREATEDOSSIER,
                        String.class, parentSpace.getNodeRef(), title));

    }

    /**
     * Creates a new Dossier with content in a zip file
     * 
     * TODO make this process atomic
     * 
     * @param user
     * @param parentSpace
     * @param zipFile
     * 
     * 
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Dossier create(User user, Space parentSpace, String title,
            Resource zipFile) throws AlfrescoServiceException {
        Dossier d = create(user, parentSpace, title);
        Document zipDoc = KoyaContentService.upload(user,
                d.getNodeRef(), zipFile);
        KoyaContentService.importZipedContent(user, zipDoc);
        return d;
    }

    /**
     * 
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Dossier edit(User user, Dossier dossier)
            throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // To
                                                                       // change
                                                                       // body
                                                                       // of
                                                                       // generated
                                                                       // methods,
                                                                       // choose
                                                                       // Tools
                                                                       // |
                                                                       // Templates.
    }

    /**
     * List all Space Dossiers
     * 
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    @Override
    public PaginatedContentList list(User user, Space space, int skipCount,
            int maxItems) throws AlfrescoServiceException {     
    	return list(user, space, skipCount, maxItems,"","");
    }
    
    /**
     * List all Space Dossiers
     * TODO sort parameter not process in this version
     * 
     * 
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    @Override
    public PaginatedContentList list(User user, Space space, int skipCount,
            int maxItems, String filter,String sort) throws AlfrescoServiceException {     
    	
		PaginatedContentList pcl = 
				user.getRestTemplate()
						.getForObject(
								getAlfrescoServerUrl()
										+ AlfrescoRestService.REST_GET_LISTCHILD_PAGINATED,
										PaginatedContentList.class, space.getNodeRef(), skipCount,
								maxItems, true, filter, sort, "");    	
    	return pcl; 
    }

    /**
     * Count all Space Dossiers
     * 
     * @param user
     * @param space
     * @return
     * @throws AlfrescoServiceException
     */
    @SuppressWarnings("serial")
	@Override
    public Integer countChildren(User user, Space space)
            throws AlfrescoServiceException {
        return countChildren(user, space, new HashSet<QName>() {
            {
                add(KoyaModel.TYPE_DOSSIER);
            }
        });
    }

    /**
     * List all users in charge of specified Dossier.
     * 
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<User> listResponsibles(User user, Dossier dossier)
            throws AlfrescoServiceException {
        return fromJSON(
                new TypeReference<List<User>>() {
                },
                user.getRestTemplate().getForObject(
                        getAlfrescoServerUrl() + REST_GET_LISTRESP,
                        String.class, dossier.getNodeRef()));
    }

    /**
     * List all users in charge of specified Dossier.
     * 
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<User> listMembers(User user, Dossier dossier)
            throws AlfrescoServiceException {
        return fromJSON(
                new TypeReference<List<User>>() {
                },
                user.getRestTemplate().getForObject(
                        getAlfrescoServerUrl() + REST_GET_LISTMEMBERS,
                        String.class, dossier.getNodeRef()));
    }

    /**
     * Adds a user in charge of specified Dossier.
     * 
     * @param user
     * @param dossier
     * @param responsible
     * @throws AlfrescoServiceException
     */
    @Override
    public void addResponsible(User user, Dossier dossier, User responsible)
            throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_ADDRESP, String.class,
                responsible.getUserName(), dossier.getNodeRef());
        
        //invalidate user cache
        cacheManager.revokePermission(responsible, dossier.getNodeRef());
        
    }

    @Override
    public void addMember(User user, Dossier dossier, User member)
            throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_ADDMEMBER, String.class,
                member.getUserName(), dossier.getNodeRef());
        
        //invalidate user cache
        cacheManager.revokePermission(member, dossier.getNodeRef());      
    }

    /**
     * Add a list of users in charge of specified Dossier.
     * 
     * @param user
     * @param dossier
     * @param responsibles
     * @throws AlfrescoServiceException
     */
    @Override
    public void addResponsible(User user, Dossier dossier,
            List<User> responsibles) throws AlfrescoServiceException {
        for (User u : responsibles) {
            addResponsible(user, dossier, u);
        }
    }

    /**
     * Removes any collaborator role set on dossier.
     * 
     * @param user
     * @param dossier
     * @param collaborator
     * @throws AlfrescoServiceException
     */
    @Override
    public void removeKoyaCollaboratorRole(User user, Dossier dossier,
            User collaborator) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_DELRESP, String.class,
                dossier.getNodeRef(), collaborator.getUserName());
        cacheManager.revokePermission(collaborator, dossier.getNodeRef());
    }

    /**
     * Remove user member or responsible of specified Dossier.
     * 
     * @param user
     * @param dossier
     * @param memberOrResp
     * @throws AlfrescoServiceException
     */
    @Override
    public void removeMembership(User user, Dossier dossier,
            User memberOrResp) throws AlfrescoServiceException {
        user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_DELRESP, String.class,
                memberOrResp.getUserName(), dossier.getNodeRef());               
        cacheManager.revokePermission(memberOrResp, dossier.getNodeRef());

    }

    /**
     * checks if dossier is confidential
     * 
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Boolean isConfidential(User user, Dossier dossier)
            throws AlfrescoServiceException {
        return Boolean.valueOf(user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_CONFIDENTIAL, String.class,
                dossier.getNodeRef()));
    }

    /**
     * change dossier confidentiality status
     * 
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Boolean setConfidentiality(User user, Dossier dossier,
            Boolean confidential) throws AlfrescoServiceException {
        Map<String, String> params = new HashMap<>();
        params.put("confidential", confidential.toString());
        return Boolean.valueOf(user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_CONFIDENTIAL, params,
                String.class,dossier.getNodeRef()));
    }
    
	public Map<String, NodeRef> createSummary(User user, Dossier dossier,
			String summaryFileName) throws AlfrescoServiceException {
		// extract map
		Map<String, String> returnValues = user.getRestTemplate().getForObject(
				getAlfrescoServerUrl() + REST_SUMMARY, Map.class,
				dossier.getNodeRef(), summaryFileName);

		Map<String, NodeRef> nodes = new HashMap<String, NodeRef>();
		for (String k : returnValues.keySet()) {
			nodes.put(k, new NodeRef(returnValues.get(k)));
		}
		return nodes;
	}
}
