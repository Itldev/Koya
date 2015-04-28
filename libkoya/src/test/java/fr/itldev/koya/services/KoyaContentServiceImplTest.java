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

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.KoyaContent;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class KoyaContentServiceImplTest extends TestCase {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	UserService userService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private SpaceService spaceService;

	@Autowired
	private DossierService dossierService;

	@Autowired
	private KoyaContentService koyaContentService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private KoyaNodeService koyaNodeService;

	private Company companyTests;
	private Space spaceTests;
	private Dossier dossierTests;
	private User admin;

	@Before
	public void createSpace() throws RestClientException,
			AlfrescoServiceException {
		admin = userService.login("admin", "admin");
		companyTests = companyService.create(admin,
				"company" + new Random().nextInt(1000000), companyService
						.listSalesOffer(admin).get(0).getName(), "default");
		spaceTests = spaceService.create(admin, new Space("testSpace"),
				companyTests);
		dossierTests = dossierService.create(admin, spaceTests, "doss1");
	}

	@After
	public void deleteCompany() throws RestClientException {
		try {
			companyService.delete(admin, companyTests);
		} catch (Exception aex) {
			System.err.println("error deleting company '"
					+ companyTests.getTitle() + "' : " + " - "
					+ aex.getMessage());
		}
	}

	@Test
	public void testCreateDir() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		assertNotNull("error creating directory", dir);
		koyaNodeService.delete(admin, (KoyaNode) dir);
	}

	@Test
	public void testCreateDirAlreadyExists() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		assertNotNull("error creating directory", dir);

		try {
			koyaContentService.createDir(admin, dossierTests.getNodeRef(),
					"dir");
			fail("should throw exception with error code : "
					+ KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
		} catch (AlfrescoServiceException aex) {
			assertEquals(aex.getKoyaErrorCode(),
					KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
		} finally {
			koyaNodeService.delete(admin, (KoyaNode) dir);
		}
	}

	@Test
	public void testListDir() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		koyaContentService.createDir(admin, dir.getNodeRef(), "subdir");
		koyaContentService.createDir(admin, dossierTests.getNodeRef(), "dir2");
		List<KoyaContent> lst = koyaContentService.list(admin,
				dossierTests.getNodeRef(), true, 50);
		assertEquals(2, lst.size());
	}

	@Test
	public void testListDirectChildren() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		koyaContentService.createDir(admin, dir.getNodeRef(), "subdir");
		koyaContentService.createDir(admin, dossierTests.getNodeRef(), "dir2");

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		koyaContentService.upload(admin, dossierTests.getNodeRef(), toUpload);

		List<KoyaContent> lst = koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 2);

		assertEquals(3, lst.size());

		assertEquals(1,
				koyaContentService.list(admin, dir.getNodeRef(), false, 2)
						.size());

	}

	@Test
	public void testMoveDir() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		Directory sDir = koyaContentService.createDir(admin, dir.getNodeRef(),
				"subdir");
		Directory dir2 = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir2");

		assertEquals(1,
				koyaContentService.list(admin, dir.getNodeRef(), true, 1)
						.size());
		assertEquals(0,
				koyaContentService.list(admin, dir2.getNodeRef(), true, 1)
						.size());
		koyaContentService.move(admin, sDir.getNodeRef(), dir2.getNodeRef());
		assertEquals(0,
				koyaContentService.list(admin, dir.getNodeRef(), true, 1)
						.size());
		assertEquals(1,
				koyaContentService.list(admin, dir2.getNodeRef(), true, 1)
						.size());
	}

	@Test
	public void testMoveDirAlreadyExists() throws AlfrescoServiceException {
		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir");
		Directory sDir = koyaContentService.createDir(admin, dir.getNodeRef(),
				"subdir");
		Directory dir2 = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir2");
		koyaContentService.createDir(admin, dir2.getNodeRef(), "subdir");

		try {
			koyaContentService
					.move(admin, sDir.getNodeRef(), dir2.getNodeRef());
		} catch (AlfrescoServiceException aex) {
			assertEquals(aex.getKoyaErrorCode(),
					KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
		}
	}

	@Test
	public void testUpload() throws AlfrescoServiceException {

		Integer sizeBefore = koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50).size();

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		koyaContentService.upload(admin, dossierTests.getNodeRef(), toUpload);

		List<KoyaContent> lstC = koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50);
		// should contain one more element
		assertEquals(sizeBefore + 1, lstC.size());
	}

	@Test
	public void testUploadAlreadyExists() throws AlfrescoServiceException {

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		koyaContentService.upload(admin, dossierTests.getNodeRef(), toUpload);

		try {
			koyaContentService.upload(admin, dossierTests.getNodeRef(),
					toUpload);
		} catch (AlfrescoServiceException aex) {

			System.out.println(aex.getKoyaErrorCode());
			assertEquals(aex.getKoyaErrorCode(),
					KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
		}

	}

	@Test
	public void testMoveFile() throws AlfrescoServiceException {

		Directory dir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir1");

		Integer sizeBefore = koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50).size();

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		Document doc = koyaContentService.upload(admin, dir.getNodeRef(),
				toUpload);

		assertEquals(
				sizeBefore.intValue(),
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());

		// deplacement du doc dans la racine
		koyaContentService.move(admin, doc.getNodeRef(),
				dossierTests.getNodeRef());

		// il doit y avoir un element en plus
		// TODO pas probant car la liste retourne l'ensemble des elements
		// recursivement
		// et donc ca ne change rien .... TODO idem avec structure hiérarchique
		assertEquals(
				sizeBefore + 1,
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());

	}

	@Test
	public void testGetParent() throws AlfrescoServiceException {
		Directory dir3 = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir3");
		Directory sdir = koyaContentService.createDir(admin, dir3.getNodeRef(),
				"sousrep");

		assertEquals(dir3, koyaNodeService.getParent(admin, (KoyaNode) sdir));
	}

	@Test
	public void testGetParentDoc() throws AlfrescoServiceException {

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		Document doc = koyaContentService.upload(admin,
				dossierTests.getNodeRef(), toUpload);

		assertEquals(dossierTests, koyaNodeService.getParent(admin, doc));
	}

	@Test
	public void testDocByteSize() throws AlfrescoServiceException {

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		Document doc = koyaContentService.upload(admin,
				dossierTests.getNodeRef(), toUpload);

		assertEquals(new Long(854), doc.getByteSize());
	}

	@Test
	public void testDirDiskSize() throws AlfrescoServiceException {

		Directory dir4 = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "dir4");

		Directory dir5 = koyaContentService.createDir(admin, dir4.getNodeRef(),
				"dir5");

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		koyaContentService.upload(admin, dir4.getNodeRef(), toUpload);

		koyaContentService.upload(admin, dir5.getNodeRef(), toUpload);

		assertEquals(new Long(854 * 2),
				koyaContentService.getDiskSize(admin, dir4));
	}

	@Test
	public void testDeleteDir() throws AlfrescoServiceException {

		int nbDir = koyaContentService.list(admin, dossierTests.getNodeRef(),
				false, 50).size();
		Directory deldir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "deldir");

		assertEquals(
				nbDir + 1,
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());

		koyaNodeService.delete(admin, deldir);

		assertEquals(
				nbDir,
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());
	}

	@Test
	public void testDeleteContent() throws AlfrescoServiceException {

		int nbDir = koyaContentService.list(admin, dossierTests.getNodeRef(),
				false, 50).size();

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		Document upDoc = koyaContentService.upload(admin,
				dossierTests.getNodeRef(), toUpload);

		assertEquals(
				nbDir + 1,
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());

		koyaNodeService.delete(admin, upDoc);

		assertEquals(
				nbDir,
				koyaContentService.list(admin, dossierTests.getNodeRef(),
						false, 50).size());
	}

	@Test
	public void testRenameDir() throws AlfrescoServiceException {

		Directory renameDir = koyaContentService.createDir(admin,
				dossierTests.getNodeRef(), "oldName");
		for (KoyaContent c : koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50)) {
			if (c.getName().equals("newName")) {
				fail();
			}
		}
		koyaNodeService.rename(admin, renameDir, "newName");

		for (KoyaContent c : koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50)) {
			if (c.getName().equals("oldName")) {
				fail();
			}
		}

	}

	@Test
	public void testRenameContent() throws AlfrescoServiceException {

		Resource toUpload = applicationContext
				.getResource("classpath:docs/testupload.txt");
		Document upDoc = koyaContentService.upload(admin,
				dossierTests.getNodeRef(), toUpload);

		for (KoyaContent c : koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50)) {
			if (c.getName().equals("newName")) {
				fail();
			}
		}
		koyaNodeService.rename(admin, upDoc, "newName");

		for (KoyaContent c : koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50)) {
			if (c.getName().equals("testupload.txt")) {
				fail();
			}
		}

	}

	@Test
	public void testImportTreeAsZip() throws AlfrescoServiceException {

		Resource toUpload = applicationContext
				.getResource("classpath:docs/zippedtree.zip");
		koyaContentService.upload(admin, dossierTests.getNodeRef(), toUpload);

		for (KoyaContent c : koyaContentService.list(admin,
				dossierTests.getNodeRef(), false, 50)) {
			if (c.getName().equals("rootzip")) {
				fail();
			}
		}

		// TODO fix this test - generate faled to import zip file : should work
		// ...
		// koyaContentService.importZipedContent(admin, upDoc);
		// boolean rootExists = false;
		// for (Content c : koyaContentService.list(admin, dossierTests, false))
		// {
		// if (c.getName().equals("rootzip")) {
		// rootExists = true;
		// }
		// }
		//
		// if (!rootExists) {
		// fail();
		// }
	}
}
