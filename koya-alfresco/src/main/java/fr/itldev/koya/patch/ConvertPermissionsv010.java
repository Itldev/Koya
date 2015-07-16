package fr.itldev.koya.patch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;

public class ConvertPermissionsv010 extends AbstractWebScript {

	private Logger logger = Logger.getLogger(this.getClass());
	private CompanyService companyService;
	private SpaceService spaceService;
	private DossierService dossierService;
	private KoyaNodeService koyaNodeService;
	private PermissionService permissionService;
	private AuthorityService authorityService;
	private OwnableService ownableService;
	private TransactionService transactionService;

	public CompanyService getCompanyService() {
		return companyService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public SpaceService getSpaceService() {
		return spaceService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public DossierService getDossierService() {
		return dossierService;
	}

	public void setDossierService(DossierService dossierService) {
		this.dossierService = dossierService;
	}

	public KoyaNodeService getKoyaNodeService() {
		return koyaNodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public AuthorityService getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public OwnableService getOwnableService() {
		return ownableService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Override
	// protected String applyInternal() throws Exception {
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		for (Company c : companyService.list()) {
			for (Space s : spaceService.list(c)) {
				// deployed versions only contains one space so ignore with log
				// spaces not 'defaultSpace'
				if (s.getName().equals("defaultSpace")) {

					try {
						convertDefaultSpaceAcl(s);
					} catch (Exception e) {
						logger.error("error converting permissions Space"
								+ s.getName());
					}

					for (Dossier d : dossierService.list(s.getNodeRef())) {
						try {
							convertDossierAcl(d);
						} catch (Exception e) {
							logger.error("error converting permissions Dossier"
									+ d.getName() + " - " + e.toString());
							e.printStackTrace();
						}
					}
				} else {
					logger.error("space " + s.getName() + " found in company "
							+ c.getName() + " : IGNORED");
				}
			}
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write("");
	}

	private static Map<String, String> GROUP_KOYA_PERMISSIONS_SPACE = new HashMap<String, String>() {
		{
			put("KoyaResponsible", "KoyaResponsible");
			put("KoyaMember", "KoyaMember");
			put("KoyaClient", "KoyaClient");
			put("KoyaPartner", "KoyaPartner");
			put("KoyaSpaceReader", "KoyaClient");
		}
	};

	private static Map<String, String> GROUP_KOYA_PERMISSIONS_DOSSIER = new HashMap<String, String>() {
		{
			put("KoyaResponsible", "KoyaResponsible");
			put("KoyaMember", "KoyaMember");
			put("KoyaClient", "KoyaClient");
			put("KoyaPartner", "KoyaPartner");
		}
	};

	private void log(Space s, String message) {
		logger.error("Convert " + s.getKtype() + " " + s.getName() + " : "
				+ message);
	}

	private void convertDossierAcl(final Dossier dossier) {

		log(dossier, " ======= Start");

		// getKoyaNodes Hierachy
		List<KoyaNode> parents = koyaNodeService.getParentsList(
				dossier.getNodeRef(), KoyaNodeService.NB_ANCESTOR_INFINTE);
		Company c = null;
		Space firstParentSpace = null;

		try {
			firstParentSpace = (Space) parents.get(0);
			c = (Company) parents.get(parents.size() - 1);
		} catch (Exception ex) {
			logger.error("Error in node hierachy " + ex.toString());
		}

		// Clear the node inherited permissions
		permissionService.setInheritParentPermissions(dossier.getNodeRef(),
				false);

		String nodeHierachyPath = buildHierachyPath(parents);

		/*
		 * Create master authority group for this node
		 */
		final String masterGroupAuthorityName = dossier.getAuthorityName(null);
		final String masterGroupDispAuthorityName = buildGroupDispName(dossier,
				"", nodeHierachyPath);

		UserTransaction transaction = transactionService
				.getNonPropagatingUserTransaction();

		try {
			transaction.begin();
			authorityService.createAuthority(AuthorityType.GROUP,
					masterGroupAuthorityName, masterGroupDispAuthorityName,
					null);
			transaction.commit();
		} catch (Exception e) {
			try {
				transaction.rollback();
			} catch (IllegalStateException | SecurityException
					| SystemException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		final String firstParentSpaceAuthorityName = firstParentSpace
				.getAuthorityName("KoyaSpaceReader");

		// Add this authority to parent KoyaSpaceReader
		// authority to allow
		// listing permission
		authorityService.addAuthority("GROUP_" + firstParentSpaceAuthorityName,
				"GROUP_" + masterGroupAuthorityName);

		for (String permissionGroupName : GROUP_KOYA_PERMISSIONS_DOSSIER
				.keySet()) {

			final String authorityName = dossier
					.getAuthorityName(permissionGroupName);
			final String dispAuthorityName = buildGroupDispName(dossier,
					permissionGroupName, nodeHierachyPath);

			if (authorityService.getAuthorityNodeRef("GROUP_" + authorityName) != null) {
				// authority already exists .. pass
				continue;
			}

			authorityService.createAuthority(AuthorityType.GROUP,
					authorityName, dispAuthorityName, null);

			// Add to master authority group for this node
			authorityService.addAuthority("GROUP_" + masterGroupAuthorityName,
					"GROUP_" + authorityName);

			// set permission on node
			permissionService.setPermission(dossier.getNodeRef(), "GROUP_"
					+ authorityName,
					GROUP_KOYA_PERMISSIONS_DOSSIER.get(permissionGroupName),
					true);

			log(dossier, "set permission " + "GROUP_" + authorityName + " > "
					+ GROUP_KOYA_PERMISSIONS_DOSSIER.get(permissionGroupName));

		}

		// convert old permissions to new ones
		// read all user setted permissions, add user to matching group

		for (AccessPermission ap : permissionService
				.getAllSetPermissions(dossier.getNodeRef())) {

			if (!ap.getAuthority().startsWith("GROUP_")) {
				String groupName = "GROUP_"
						+ dossier.getAuthorityName(ap.getPermission());

				log(dossier, "add user " + ap.getAuthority() + " to "
						+ groupName);
				authorityService.addAuthority(groupName, ap.getAuthority());
			}

		}

		//
		removeOwner(dossier);
		removeAllUserPermissions(dossier);

		log(dossier, " ======= End");

	}

	private void convertDefaultSpaceAcl(final Space space) {
		log(space, " ======= Start");

		List<KoyaNode> parents = koyaNodeService.getParentsList(
				space.getNodeRef(), KoyaNodeService.NB_ANCESTOR_INFINTE);
		// Clear the node inherited permissions
		permissionService
				.setInheritParentPermissions(space.getNodeRef(), false);
		String nodeHierachyPath = buildHierachyPath(parents);

		/*
		 * Create master authority group for this node
		 */
		final String masterGroupAuthorityName = space.getAuthorityName(null);
		final String masterGroupDispAuthorityName = buildGroupDispName(space,
				"", nodeHierachyPath);

		UserTransaction transaction = transactionService
				.getNonPropagatingUserTransaction();

		try {
			transaction.begin();
			authorityService.createAuthority(AuthorityType.GROUP,
					masterGroupAuthorityName, masterGroupDispAuthorityName,
					null);
			transaction.commit();
		} catch (Exception e) {
			try {
				transaction.rollback();
			} catch (IllegalStateException | SecurityException
					| SystemException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		for (String permissionGroupName : GROUP_KOYA_PERMISSIONS_SPACE.keySet()) {

			final String authorityName = space
					.getAuthorityName(permissionGroupName);
			final String dispAuthorityName = buildGroupDispName(space,
					permissionGroupName, nodeHierachyPath);

			if (authorityService.getAuthorityNodeRef("GROUP_" + authorityName) != null) {
				// authority already exists .. pass
				continue;
			}

			authorityService.createAuthority(AuthorityType.GROUP,
					authorityName, dispAuthorityName, null);

			// Add to master authority group for this node
			authorityService.addAuthority("GROUP_" + masterGroupAuthorityName,
					"GROUP_" + authorityName);

			// set permission on node
			permissionService
					.setPermission(space.getNodeRef(),
							"GROUP_" + authorityName,
							GROUP_KOYA_PERMISSIONS_SPACE
									.get(permissionGroupName), true);
			log(space, "set permission " + "GROUP_" + authorityName + " > "
					+ GROUP_KOYA_PERMISSIONS_SPACE.get(permissionGroupName));
		}

		removeOwner(space);
		removeAllUserPermissions(space);

		log(space, " ======= End");

	}

	private void removeOwner(final Space space) {
		AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
					@Override
					public Void doWork() throws Exception {
						ownableService.setOwner(space.getNodeRef(),
								ownableService.NO_OWNER);
						return null;
					}
				});
	}

	private void removeAllUserPermissions(Space s) {
		for (AccessPermission ap : permissionService.getAllSetPermissions(s
				.getNodeRef())) {

			if (!ap.getAuthority().startsWith("GROUP_")) {
				permissionService.deletePermission(s.getNodeRef(),
						ap.getAuthority(), ap.getPermission());
			}

		}
	}

	private String buildHierachyPath(List<KoyaNode> parents) {
		String hierachy = "";
		String sep = "";
		for (KoyaNode n : parents) {
			hierachy += sep + n.getName();
			sep = "/";
		}
		return hierachy;
	}

	private String buildGroupDispName(Space s, String roleName,
			String hierachyPath) {
		String dispName = s.getKtype() + " " + s.getName() + " " + roleName;

		String sep = "";
		if (!hierachyPath.isEmpty()) {
			dispName += "(" + hierachyPath + ")";
		}

		return dispName;
	}

}
