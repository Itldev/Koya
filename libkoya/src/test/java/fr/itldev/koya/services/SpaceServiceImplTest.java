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
package fr.itldev.koya.services;

import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class SpaceServiceImplTest extends TestCase {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	UserService userService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private SpaceService spaceService;
	@Autowired
	private KoyaNodeService koyaNodeService;

	private Company companyTests;
	private User admin;

	@Before
	public void createCompany() throws RestClientException,
			AlfrescoServiceException {
		admin = userService.login("admin", "admin");
		companyTests = companyService.create(admin,
				"company" + new Random().nextInt(1000), companyService
						.listSalesOffer(admin).get(0).getName(), "default");
	}

	@After
	public void deleteCompany() throws RestClientException,
			AlfrescoServiceException {
		try {
			companyService.delete(admin, companyTests);
		} catch (AlfrescoServiceException aex) {
			System.err.println("error deleting company '"
					+ companyTests.getTitle() + "' : " + aex.getKoyaErrorCode()
					+ " - " + aex.getMessage());
		}
	}

	@Test
	public void testCreateSpace() throws RestClientException,
			AlfrescoServiceException {


		Space eCreated = spaceService.create(admin,  companyTests,"space1");
		assertNotNull("error crating 'space1'", eCreated);

	}

	@Test
	public void testCreateChildSpace() throws RestClientException,
			AlfrescoServiceException {

		Space eCreated = spaceService.create(admin,
				companyTests,"parentSpace");
		assertNotNull("'parent Space creation error'", eCreated);

		Space eEnfant = spaceService.create(admin, 
				eCreated,"childSpace");
		assertNotNull("'child Space creation error'", eEnfant);

	}

	@Test
	public void testListSpaces() throws RestClientException,
			AlfrescoServiceException {

		Space eParent1 = spaceService.create(admin, 
				companyTests,"parentSpace1");

		spaceService.create(admin, eParent1,"childSpace11");

		spaceService.create(admin, eParent1,"childSpace12");

		Space eParent2 = spaceService.create(admin,
				companyTests,"parentSpace2");

		spaceService.create(admin, eParent2,"childSpace21");

		spaceService.create(admin, eParent2,"childSpace22");

		List<Space> lstArboEspaces = spaceService.list(admin, companyTests);

		// 2 created + defaultspace automaticly created on company creation
		assertEquals(3, lstArboEspaces.size());

		for (Space e : lstArboEspaces) {
			if (!e.getName().equals("defaultSpace")) {
				assertEquals(2, e.getChildSpaces().size());
			}
		}

	}

	@Test
	public void testMoveSpaces() throws RestClientException,
			AlfrescoServiceException {

		Space parentSpace = spaceService.create(admin,
				 companyTests,"parentSpace");

		Space childSpace = spaceService.create(admin, 
				parentSpace,"childSpace");
		// 1 created + defaultspace automaticly created on company creation
		assertEquals(2, spaceService.list(admin, companyTests).size());
		// move
		spaceService.move(admin, childSpace, companyTests);
		// TODO check this test
		// assertEquals(3, spaceService.list(admin, companyTests).size());
	}

	@Test
	public void testDelSpace() throws RestClientException,
			AlfrescoServiceException {

		spaceService.create(admin, companyTests,"space1");

		Space space2 = spaceService.create(admin,
				companyTests,"space2");

		// 2 created + defaultspace automaticly created on company creation
		assertEquals(3, spaceService.list(admin, companyTests).size());
		// del
		koyaNodeService.delete(admin, space2);

		assertEquals(2, spaceService.list(admin, companyTests).size());
	}

}
