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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.SearchService;
import java.io.Serializable;

/**
 * Search service.
 * 
 */
public class SearchServiceImpl extends AlfrescoRestService implements SearchService, Serializable {

	private static final String REST_GET_SEARCH = "/s/slingshot/search?"
			+ "term={term}&maxResults={maxResults}"
			+ "&sort=&repo=true&rootNode={rootNode}&query={query}&alf_ticket={alf_ticket}";

	private static final String SEARCHRESULTKEY_ITEMS = "items";

	private static final String AFRESCO_SITES_ROOT_NODE = "alfresco://sites/home";
	// item attributes
	private static final String SEARCHRESULTKEY_NODEREF = "nodeRef";

	/*
     * 
     */
	private static final Integer DEFAULT_MAXITEMS = 50;

	/**
	 * Execute basic search for all KoyaNodes from KoyaNode defined as base.
	 * 
	 * @param user
	 * @param base
	 * @param searchexpr
	 * @return
	 */
	@Override
	public List<KoyaNode> search(User user, KoyaNode base, String searchexpr) {

		String rootNode = "";

		if (base != null) {
			rootNode = base.getNodeRef().toString();
		} else {
			rootNode = AFRESCO_SITES_ROOT_NODE;
		}

		List<KoyaNode> itemsFound = new ArrayList<>();

		Map result = fromJSON(
				new TypeReference<Map>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_SEARCH, String.class,
						processSearchExpr(searchexpr), DEFAULT_MAXITEMS,
						rootNode, "",user.getTicketAlfresco()));

		for (Map itemMap : (List<Map>) result.get(SEARCHRESULTKEY_ITEMS)) {

			try {
				NodeRef nodeRef = new NodeRef(itemMap.get(
						SEARCHRESULTKEY_NODEREF).toString());
				KoyaNode item = getKoyaNode(user, nodeRef);
				if (item != null) {
					itemsFound.add(item);
				}
			} catch (RestClientException ex) {
			}
		}

		return itemsFound;
	}

	/**
	 * TODO complex methods that :
	 * 
	 * - allow paginator and max results.
	 * 
	 * - limit results type
	 */
	/**
	 * Simple input search processing
	 * 
	 * TODO options : starts with, ends with, contain
	 * 
	 * TODO do treat expressions between quotes
	 * 
	 * @param inSearch
	 * @return
	 */
	private String processSearchExpr(String inSearch) {
		/*
		 * ==== format search expression ====
		 * 
		 * Find all elements that starts with single words defined in searhexpr.
		 * 
		 * if many, results should contains each single words (AND separate in
		 * search)
		 */

		StringBuilder searchExpr = new StringBuilder();
		if (inSearch == null || inSearch.isEmpty()) {
			searchExpr.append("*");
		} else {

			// remove AND terms
			inSearch = inSearch.replaceAll(" AND ", " ");
			// deletes starting, finishing and multispaces
			inSearch = inSearch.replaceAll("^[\\s]+", "");
			inSearch = inSearch.replaceAll("\\s+$", "");
			inSearch = inSearch.replaceAll("\\s+", " ");

			String sep = "";
			for (String term : inSearch.split(" ")) {
				searchExpr.append(sep).append(" ").append(term).append("*");
				sep = " AND ";
			}
		}
		return searchExpr.toString();
	}
}
