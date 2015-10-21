package fr.itldev.koya.permissions;

import java.util.Random;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.services.CompanyService;
import fr.itldev.koya.services.DossierService;
import fr.itldev.koya.services.InvitationService;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.SecuService;
import fr.itldev.koya.services.SpaceService;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.impl.ShareServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class CompanyPermissionsScenarioTest extends TestCase {

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
	private SecuService secuService;
	@Autowired
	private KoyaContentService koyaContentService;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private ShareServiceImpl shareService;
	

	private Company companyTests;
	private Space spaceTests;
	User admin;

	private Integer testRamdomId;

	@Before
	public void init() throws RestClientException, AlfrescoServiceException {
		testRamdomId = new Random().nextInt(1000000);
		admin = userService.login("admin", "admin");
		companyTests = companyService.create(admin, "company" + testRamdomId,
				companyService.listSalesOffer(admin).get(0).getName(),
				"default");
		spaceTests = spaceService.create(admin, companyTests, "testSpace");

	}

	/**
	 * 
	 * Test permissions permeability between dossiers in the same site for
	 * KoyaClient Users
	 * 
	 * ======
	 * 
	 * Creates 2 Dossiers upload content in theses dossiers
	 * 
	 * Invite 1 client on first dossier
	 * 
	 * log in as client
	 * 
	 * - Try to download content of first dossier : should be ok
	 * 
	 * - Try to download content of second : should with beacause of permissions
	 * 
	 */
	@Test
	public void clientDownloadLink() {

		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		Dossier d2 = dossierService.create(admin, spaceTests, "d2");

		// upload content un both of them

		Document doc1 = koyaContentService
				.upload(admin, d1.getNodeRef(), applicationContext
						.getResource("classpath:docs/testupload.txt"));

		Document doc2 = koyaContentService
				.upload(admin, d2.getNodeRef(), applicationContext
						.getResource("classpath:docs/zippedtree.zip"));

		// share dossier d1 with new client : automatic accept invitation
		String userMail = "newclient" + testRamdomId + "@test.com";

		KoyaShare ks = shareService.shareItem(admin, d1, userMail);

		User u = new User();
		u.setName("TEST");
		u.setFirstName("test");
		u.setPassword("test");

		invitationService.validateInvitation(u, ks.getKoyaInvite()
				.getInviteId(), ks.getKoyaInvite().getTicket());
		// login as new client to get Afresco ticket
		u = userService.login(userMail, "test");

		// try to download doc1 by url
		String getDoc1Url = "http://localhost:8080/alfresco/s/api/node/workspace/SpacesStore/"
				+ doc1.getNodeRef().getId()
				+ "/content/"
				+ doc1.getName()
				+ "?alf_ticket=" + u.getTicketAlfresco();

		if (!canDownload(u, getDoc1Url)) {
			fail("doc1 should be downloadable");
		}
		// try to download doc2 by url : should fail (401)

		String getDoc2Url = "http://localhost:8080/alfresco/s/api/node/workspace/SpacesStore/"
				+ doc2.getNodeRef().getId()
				+ "/content/"
				+ doc2.getName()
				+ "?alf_ticket=" + u.getTicketAlfresco();

		if (canDownload(u, getDoc2Url)) {
			fail("doc2 should not  be downloadable");
		}
	}

	@Test
	public void deletedCollaboratorDownloadLink() {
		// share dossier d1 with new client : automatic accept invitation
		String collabMail = "deletedcollab" + testRamdomId + "@test.com";
		// create collaborator and validate invitation
		KoyaInvite ki = invitationService.inviteUser(admin, companyTests,
				collabMail, "SiteCollaborator");
		User u = new User();
		u.setName("TEST");
		u.setFirstName("test");
		u.setPassword("test");

		invitationService.validateInvitation(u, ki.getInviteId(),
				ki.getTicket());

		// login as collaborator to get Afresco ticket
		u = userService.login(collabMail, "test");
		// create dossier and upload content
		Dossier d1 = dossierService.create(u, spaceTests, "d1");
		Document doc1 = koyaContentService
				.upload(admin, d1.getNodeRef(), applicationContext
						.getResource("classpath:docs/testupload.txt"));

		// try to download doc1 by url
		String getDoc1Url = "http://localhost:8080/alfresco/s/api/node/workspace/SpacesStore/"
				+ doc1.getNodeRef().getId()
				+ "/content/"
				+ doc1.getName()
				+ "?alf_ticket=" + u.getTicketAlfresco();

		if (!canDownload(u, getDoc1Url)) {
			fail("doc1 should be downloadable as user is collaborator");
		}

		secuService.revokeAccess(admin, companyTests, u);

		if (canDownload(u, getDoc1Url)) {
			fail("doc1 should not  be downloadable as user is not member of company any more");
		}
	}

	private Boolean canDownload(User u, String getLink) {

		try {
			shareService.getTemplate().getForObject(getLink, String.class);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
