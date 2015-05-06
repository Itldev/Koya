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

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
/**
 * TODO 
 * 
 * - chained permissions at share & unshare  		
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class ShareServiceImplTest extends TestCase {

	private static String USER_MANAGER = "manager";
	private static String USER_COLLABORATOR = "collaborator";
	private static String USER_SHAREDCLIENT = "sharedClient";
	private static String USER_MAILDOMAIN = "@itldev.net";

	private static String USERS_PWD = "itldevpwd";

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
	private InvitationService invitationService;
	@Autowired
	private ShareService shareService;

	private Company companyTests;
	private Space spaceTests;
	private User admin;
	private User manager;
	private User collaborator;
	private User sharedClient;
	private Integer randomTestId;

	@Before
	public void initShareContext() throws RestClientException,
			AlfrescoServiceException {

		randomTestId = new Random().nextInt(1000000);

		admin = userService.login("admin", "admin");
		companyTests = companyService.create(admin, "company" + randomTestId,
				companyService.listSalesOffer(admin).get(0).getName(),
				"default");
		spaceTests = spaceService.create(admin, new Space("testSpace"),
				companyTests);

		/**
		 * Create manager user
		 */
		KoyaInvite managerInviteWrapper = invitationService.inviteUser(admin,
				companyTests, USER_MANAGER + randomTestId + USER_MAILDOMAIN,
				SitePermission.MANAGER.toString());
		// validate invitation with default password
		manager = validateInvitationAndLogin(USER_MANAGER, managerInviteWrapper);

		/**
		 * Create Collaborator user
		 */
		KoyaInvite collaboratorInviteWrapper = invitationService.inviteUser(
				manager, companyTests, USER_COLLABORATOR + randomTestId
						+ USER_MAILDOMAIN,
				SitePermission.COLLABORATOR.toString());

		collaborator = validateInvitationAndLogin(USER_COLLABORATOR,
				collaboratorInviteWrapper);

	}

	@After
	public void deleteCompany() throws RestClientException,
			AlfrescoServiceException {
		// companyService.delete(admin, companyTests);
	}

	@Test
	public void testCollabNotMemberTryShareDossier()
			throws AlfrescoServiceException {
		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		try {
			shareService.shareItem(collaborator, d1, USER_SHAREDCLIENT
					+ randomTestId + USER_MAILDOMAIN);
			fail("collaborator shouldn't have share permission as not dossier member");
		} catch (AlfrescoServiceException e) {
			// acces denied
		}
	}

	@Test
	public void testSharedMemberTyrShareDossier()
			throws AlfrescoServiceException {

		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		dossierService.addMember(admin, d1, collaborator);

		KoyaShare share = shareService.shareItem(collaborator, d1,
				USER_SHAREDCLIENT + randomTestId + USER_MAILDOMAIN);
		assertNotNull(share.getKoyaInvite().getTicket());
		// sharing should result in invitation : user exists
		sharedClient = validateInvitationAndLogin(USER_SHAREDCLIENT,
				share.getKoyaInvite());

		try {
			shareService.shareItem(sharedClient, d1, USER_SHAREDCLIENT
					+ randomTestId + USER_MAILDOMAIN);
			fail("shared client shouldn't have any share permission");
		} catch (AlfrescoServiceException ase) {

		}
	}

	@Test
	public void testCollabMemberShareDossier() throws AlfrescoServiceException {

		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		dossierService.addMember(admin, d1, collaborator);

		KoyaShare share = shareService.shareItem(collaborator, d1,
				USER_SHAREDCLIENT + randomTestId + USER_MAILDOMAIN);
		assertNotNull(share.getKoyaInvite().getTicket()); // sharing should
															// result in
															// inviation.
		// User exists
		//
		sharedClient = validateInvitationAndLogin(USER_SHAREDCLIENT,
				share.getKoyaInvite());

		// shared client can now list/read dossier content
		// TODO fill dossier with demo content

		shareService.unShareItem(collaborator, d1, USER_SHAREDCLIENT
				+ randomTestId + USER_MAILDOMAIN);

		// now shared client should'nt have access on dossier

	}

	@Test
	public void testCollabMemberTryUnshareDossier()
			throws AlfrescoServiceException {

		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		dossierService.addMember(admin, d1, collaborator);

		KoyaShare share = shareService.shareItem(collaborator, d1,
				USER_SHAREDCLIENT + randomTestId + USER_MAILDOMAIN);
		assertNotNull(share.getKoyaInvite().getTicket()); // sharing should
															// result in
															// inviation.
		// User exists
		//
		sharedClient = validateInvitationAndLogin(USER_SHAREDCLIENT,
				share.getKoyaInvite());

		// remove collab membership
		dossierService.removeMembership(admin, d1, collaborator);
		// TODO l'acces est il reelement supprimé ?

		try {
			shareService.unShareItem(collaborator, d1, USER_SHAREDCLIENT
					+ randomTestId + USER_MAILDOMAIN);
			fail("collaborator shouldn't unshare if he is not member or responsible");
		} catch (Exception e) {

		}

	}

	private User validateInvitationAndLogin(String loginPrefix, KoyaInvite iw) {

		String login = loginPrefix + randomTestId + USER_MAILDOMAIN;
		User u = new User();
		u.setPassword(USERS_PWD);
		u.setName(loginPrefix);
		invitationService.validateInvitation(u, iw.getInviteId(),
				iw.getTicket());
		u = userService.login(login, USERS_PWD);
		return u;
	}

}
