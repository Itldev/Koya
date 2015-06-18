package fr.itldev.koya.services;

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class NotificationServiceTest extends TestCase {

	private static String USER_MANAGER = "manager";
	private static String USER_COLLABORATOR = "collaborator";
	private static String USER_SHAREDCLIENT = "sharedClient";
	private static String USER_MAILDOMAIN = "@itldev.net";

	private static String USERS_PWD = "junittest";

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	UserService userService;
	@Autowired
	NotificationService notificationService;
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
	private User client1;
	private User client2;

	private Integer randomTestId;

	private Dossier d1;
	private Dossier d2;

	@Before
	public void init() throws RestClientException, AlfrescoServiceException {

		randomTestId = new Random().nextInt(1000000);

		admin = userService.login("admin", "admin");

		companyTests = companyService.create(admin, "company" + randomTestId,
				companyService.listSalesOffer(admin).get(0).getName(),
				"default");
		spaceTests = spaceService.create(admin, companyTests,"testSpace");

		/**
		 * Create manager user
		 */
		KoyaInvite managerInviteWrapper = invitationService.inviteUser(admin,
				companyTests, USER_MANAGER + randomTestId + USER_MAILDOMAIN,
				SitePermission.MANAGER.toString());
		// validate invitation with default password
		manager = validateInvitationAndLogin(USER_MANAGER, managerInviteWrapper);

		Dossier d1 = dossierService.create(admin, spaceTests, "d1");
		KoyaShare s1 = shareService.shareItem(manager, d1, USER_SHAREDCLIENT
				+ "-1-" + randomTestId + USER_MAILDOMAIN);
		Dossier d2 = dossierService.create(admin, spaceTests, "d2");
		KoyaShare s2 = shareService.shareItem(manager, d2, USER_SHAREDCLIENT
				+ "-2-" + randomTestId + USER_MAILDOMAIN);

		client1 = validateInvitationAndLogin(USER_SHAREDCLIENT + "-1-",
				s1.getKoyaInvite());
		client2 = validateInvitationAndLogin(USER_SHAREDCLIENT + "-2-",
				s2.getKoyaInvite());

		// activates koya feed

	}

	@Test
	public void testList() {
		List<Notification> result = notificationService.list(admin, null, null,
				null, null, null);
	}

	@Test
	public void testFeedFilter() throws InterruptedException {
		dumpFeed(manager);
		dumpFeed(client1);
		dumpFeed(client2);
	}

	private void dumpFeed(User u) throws InterruptedException {

		System.out.println("User : " + u.getUserName());
		List<Notification> result = readFeedTimeout(u);

		System.err.println(result.size() + " notifications");

		for (Notification n : result) {
			System.out.println(" " + n.getId() + " - " + n.getPostUserId()
					+ " - " + n.getNotificationType()+"--"+n.getMessage());
		}

	}

	private List<Notification> readFeedTimeout(User user)
			throws InterruptedException {
		List<Notification> result = notificationService.list(user, null, null,
				null, null, null);

		long t = System.currentTimeMillis();
		long end = t + 60000;

		while (result.size() == 0 && System.currentTimeMillis() < end) {
			result = notificationService.list(user, null, null, null, null,
					null);
			Thread.sleep(5000);
			System.out.print("+");

		}
		return result;
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
