/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.webscript.salesoffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.SalesOfferService;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List available Sales Offer
 * 
 */
public class ListOffer extends AbstractWebScript {

	private SalesOfferService salesOfferService;

	public void setSalesOfferService(SalesOfferService salesOfferService) {
		this.salesOfferService = salesOfferService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		SalesOffer offer1 = new SalesOffer();
		SalesOffer offer2 = new SalesOffer();

		offer1.setTemplate("tpl1");
		offer1.setName("offer1");
		offer1.setActive(true);

		offer2.setTemplate("tpl2");
		offer2.setName("offer2");
		offer2.setActive(true);

		List<SalesOffer> soList = new ArrayList<>();
		soList.add(offer1);
		soList.add(offer2);

		String response = KoyaWebscript.getObjectAsJson(soList);

		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
