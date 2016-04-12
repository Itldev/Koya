package fr.itldev.koya.alfservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.invitation.site.KoyaInviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;

/**
 *
 *
 */
public class KoyaMailService implements InitializingBean {

	private final Logger logger = Logger.getLogger(this.getClass());

	private final static String SHARE_NOTIFICATION_SUBJECT = "koya.share-notification.subject";
	private final static String DLFILE_AVAILABLE_SUBJECT = "koya.dlfile-notification.subject";
	private final static String RESET_PASSWORD_SUBJECT = "koya.reset-password.subject";
	private final static String INACTIVEDOSSIER_NOTIFICATION_SUBJECT = "koya.inactivedossier-notification.subject";
	private final static String ACTIVITIESEMAIL_SUBJECT = "koya.activities-email.subject";
	private final static String CLIENT_UPLOADALERT_EMAIL_SUBJECT = "koya.clientdoc-upload.subject";
	private final static String TASKEXPIREALERT_EMAIL_SUBJECT = "koya.task-expire-alert.subject";

	
	/**
	 * TODO Get Theses values as config from properties files
	 * TODO Split Mail Service as Many Mailer instances : 1 per mail Type
	 */
	
	
	private final static String TPL_MAIL_KOYAROOT = "//app:company_home/app:dictionary/app:email_templates/cm:koya_templates";

	private final static String TPL_MAIL_I18NSUBJECTS = TPL_MAIL_KOYAROOT
			+ "/cm:koyamail.properties";
	private final static String TPL_MAIL_SHARENOTIF = TPL_MAIL_KOYAROOT
			+ "/cm:share-notification.html.ftl";
	private final static String TPL_MAIL_DLFILEAVAILBLE = TPL_MAIL_KOYAROOT
			+ "/cm:dlfile-available.html.ftl";
	private final static String TPL_MAIL_CLIENTUPLOADALERT = TPL_MAIL_KOYAROOT
			+ "/cm:clientdoc-upload.html.ftl";
	private final static String TPL_MAIL_RESET_PWD = TPL_MAIL_KOYAROOT
			+ "/cm:reset-password.html.ftl";
	public final static String TPL_MAIL_INVITATION = TPL_MAIL_KOYAROOT + "/cm:invite.html.ftl";
	private final static String TPL_MAIL_INACTIVEDOSSIERNOTIF_ = TPL_MAIL_KOYAROOT
			+ "/cm:inactive-dossiers.html.ftl";
	public final static String TPL_MAIL_TASKEXPIREALERT = TPL_MAIL_KOYAROOT + "/cm:task-expire-alert.html.ftl";

	protected NamespaceService namespaceService;
	protected FileFolderService fileFolderService;
	protected SearchService searchService;
	protected NodeService nodeService;
	protected ActionService actionService;
	protected KoyaNodeService koyaNodeService;
	protected UserService userService;
	protected ServiceRegistry serviceRegistry;
	protected CompanyAclService companyAclService;
	protected CompanyService companyService;
	protected SpaceAclService spaceAclService;
	protected CompanyPropertiesService companyPropertiesService;

	protected WorkflowService workflowService;
	protected AuthenticationService authenticationService;
	protected Repository repositoryHelper;
	protected MessageService messageService;

	/**
	 * Optional parameters, if not set, use clasic share url
	 */
	protected String koyaDirectLinkUrlTemplate;
	protected String koyaClientServerPath;

	/**
	 *
	 *
	 */
	protected HashMap<String, Object> koyaClientParams;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setKoyaDirectLinkUrlTemplate(String koyaDirectLinkUrlTemplate) {
		this.koyaDirectLinkUrlTemplate = koyaDirectLinkUrlTemplate;
	}

	public void setKoyaClientServerPath(String koyaClientServerPath) {
		this.koyaClientServerPath = koyaClientServerPath;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setCompanyPropertiesService(CompanyPropertiesService companyPropertiesService) {
		this.companyPropertiesService = companyPropertiesService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public HashMap getKoyaClientParams() {
		return koyaClientParams;
	}

	// </editor-fold>
	@Override
	public void afterPropertiesSet() throws Exception {
		koyaClientParams = new HashMap<>();		
		koyaClientParams.put("serverPath", koyaClientServerPath);		
	}

	/**
	 * 
	 */
	public void sendShareAlertMail(String destUserName, String inviterUserName,
			final NodeRef sharedNodeRef) throws KoyaServiceException {

		final Space s = koyaNodeService.getKoyaNode(sharedNodeRef, Space.class);
		final Company c = koyaNodeService.getFirstParentOfType(sharedNodeRef, Company.class);
		User dest = userService.getUserByUsername(destUserName);
                final KoyaPermission koyaPermission=spaceAclService.getKoyaPermission(s, dest);
                
                
		if (logger.isDebugEnabled()) {
			logger.debug("Alert Email - Share : space " + s.getTitle() + ";user " + dest.getEmail());
		}

		Map<String, Serializable> paramsMail = new HashMap<>();
		paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, 
				getFileTemplateRef(new RepositoryLocation(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						TPL_MAIL_SHARENOTIF, SearchService.LANGUAGE_LUCENE)));

		// TODO i18n templates
		Map<String, Object> templateModel = new HashMap<>();

		/**
		 * Model Objects
		 */
                templateModel.put("dest", new ScriptNode(dest.getNodeRef(), serviceRegistry));
		templateModel.put("sharedItem", new HashMap() {
			{
				put("url", (koyaPermission.equals(KoyaPermissionConsumer.PARTNER)?getDirectLinkUrl(c.getNodeRef()):getDirectLinkUrl(sharedNodeRef)));
				put("nodeRef", s.getNodeRef());
				put("title", s.getTitle());
			}
		});

		templateModel.put("koyaClient", koyaClientParams);

		if (inviterUserName != null) {
			User inviter = userService.getUserByUsername(inviterUserName);
			templateModel.put("inviter", new ScriptNode(inviter.getNodeRef(), serviceRegistry));
		}

		templateModel.put(TemplateService.KEY_COMPANY_HOME, repositoryHelper.getCompanyHome());
		templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(SHARE_NOTIFICATION_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);

	}

	public void sendDlFileAvailableAlertMail(String destUserName, final NodeRef dlFileNodeRef,
			final String fileName) throws KoyaServiceException {

		User dest = userService.getUserByUsername(destUserName);		
		final Document dlFile = koyaNodeService.getKoyaNode(dlFileNodeRef, Document.class);
		final Company c = koyaNodeService.getFirstParentOfType(dlFile.getNodeRef(), Company.class);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Alert Email - DlFileAvailble " + dlFile.getTitle() + ";user " + dest.getEmail());
		}

		Map<String, Serializable> paramsMail = new HashMap<>();
		paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, 
				getFileTemplateRef(new RepositoryLocation(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						TPL_MAIL_DLFILEAVAILBLE, SearchService.LANGUAGE_LUCENE)));

		// TODO i18n templates
		Map<String, Object> templateModel = new HashMap<>();

		final HashMap<String, String> dlLinkAttributes = new HashMap<>();
		dlLinkAttributes.put("mode", "dl");
		dlLinkAttributes.put("fileName", fileName);
		/**
		 * Model Objects
		 */
		
		templateModel.put("dlfile", new HashMap() {
			{
				put("url", getDirectLinkUrl(dlFile.getNodeRef(),dlLinkAttributes));
				put("nodeRef", dlFile.getNodeRef());
				put("title", fileName);
			}
		});

                templateModel.put("dest", new ScriptNode(dest.getNodeRef(), serviceRegistry));
		templateModel.put("koyaClient", koyaClientParams);

		templateModel.put(TemplateService.KEY_COMPANY_HOME, repositoryHelper.getCompanyHome());
		templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(DLFILE_AVAILABLE_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);

	}
	
	
	public void sendTaskExpireAlertMail(List<User> dest, final String taskName, final String processName,
			final Space refSpace, final Integer expDelay, final Date expDate) throws KoyaServiceException {
		
		final Company c = koyaNodeService.getFirstParentOfType(refSpace.getNodeRef(), Company.class);
		        
		if (logger.isDebugEnabled()) {
			logger.debug("Alert Email - task "+taskName+"expire in "+expDelay + " days");
		}

		Map<String, Serializable> paramsMail = new HashMap<>();
		
		
		ArrayList<String> mailDest = new ArrayList<>();
		for (User u : dest) {
			mailDest.add(u.getEmail());
		}
		paramsMail.put(MailActionExecuter.PARAM_TO_MANY, mailDest);

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, 
				getFileTemplateRef(new RepositoryLocation(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						TPL_MAIL_TASKEXPIREALERT, SearchService.LANGUAGE_LUCENE)));

		// TODO i18n templates
		Map<String, Object> templateModel = new HashMap<>();

		/**
		 * Model Objects
		 */

		templateModel.put("expireinfos", new HashMap() {
			{
				put("taskName",taskName);
				put("processName",processName);
				put("delay", expDelay);
				put("date",expDate);
				put("spaceNodeRef", refSpace.getNodeRef());
				put("spaceTitle", refSpace.getTitle());
				put("spaceUrl",getDirectLinkUrl(refSpace.getNodeRef())); //TODO direct acces to workflow page
				
			}
		});

		templateModel.put("koyaClient", koyaClientParams);
		

		templateModel.put(TemplateService.KEY_COMPANY_HOME, repositoryHelper.getCompanyHome());
		templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(TASKEXPIREALERT_EMAIL_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);

	}
	
	public void sendClientUploadAlertMail(Set<String> destUserNames,String uploaderUserName,
			final NodeRef document,final NodeRef dossier)
					throws KoyaServiceException {
		
		User uploader = userService.getUserByUsername(uploaderUserName);
		final Document doc = koyaNodeService.getKoyaNode(document,Document.class);		
		final Dossier d= koyaNodeService.getKoyaNode(dossier,Dossier.class);
		Company c = koyaNodeService.getFirstParentOfType(d.getNodeRef(), Company.class);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Alert Email - Client file Upload : dossier " + d.getTitle()
					+ ";client " + uploader.getEmail());
		}

		// set mail dest
		Map<String, Serializable> paramsMail = new HashMap<>();
		ArrayList<String> mailDest = new ArrayList<>();
		for (String username : destUserNames) {
			mailDest.add(userService.getUserByUsername(username).getEmail());
		}
		paramsMail.put(MailActionExecuter.PARAM_TO_MANY, mailDest);
		
		
		
		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, 
				getFileTemplateRef(new RepositoryLocation(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						TPL_MAIL_CLIENTUPLOADALERT, SearchService.LANGUAGE_LUCENE)));

		Map<String, Object> templateModel = new HashMap<>();
		/**
		 * Model Objects
		 */

		templateModel.put("notifyCompanyManagers", Boolean.FALSE);//TODO check templates usage

		templateModel.put("clientUploader",
				new ScriptNode(uploader.getNodeRef(), serviceRegistry));
		templateModel.put("uploadedDoc", new HashMap() {
			{
                                put("url", getDirectLinkUrl(doc.getNodeRef()));
				put("title", doc.getTitle());
				put("name", doc.getName());
			}
		});
		templateModel.put("referenceDossier", new HashMap() {
			{
				put("nodeRef", d.getNodeRef());
				put("title", d.getTitle());
			}
		});
		templateModel.put("koyaClient", koyaClientParams);

		templateModel.put(TemplateService.KEY_COMPANY_HOME, repositoryHelper.getCompanyHome());
		templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(CLIENT_UPLOADALERT_EMAIL_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);

	}

	public void sendUserNotifMail(NodeRef personNodeRef, Map<String, Object> model,
			String templateNodeRef, String companyName) {

		ParameterCheck.mandatory("personNodeRef", personNodeRef);

		Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);
		String emailAddress = (String) personProps.get(ContentModel.PROP_EMAIL);

		Map<String, Serializable> paramsMail = new HashMap<>();
		logger.debug("send user notification to '" + emailAddress + "'");
		paramsMail.put(MailActionExecuter.PARAM_TO, emailAddress);
		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, templateNodeRef);

		// Add extra variables to model
		model.put("koyaClient", koyaClientParams);
		model.put("company", companyPropertiesService.getProperties(companyName).toHashMap());

		logger.trace("User Notification mail model =" + model.toString());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) model);
		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(ACTIVITIESEMAIL_SUBJECT, model));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);
	}

	public void sendResetRequestMail(String destMail, String resetRequestUrl)
			throws KoyaServiceException {
		Map<String, Serializable> paramsMail = new HashMap<>();

		paramsMail.put(MailActionExecuter.PARAM_TO, destMail);

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
				getFileTemplateRef(new RepositoryLocation(
				StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				TPL_MAIL_RESET_PWD, SearchService.LANGUAGE_LUCENE)));

		// TODO i18n templates
		Map<String, Object> templateModel = new HashMap<>();

		/**
		 * TODO use global-properties param to set reset request url
		 */
		templateModel.put("resetRequestUrl", resetRequestUrl);
		templateModel.put("koyaClient", koyaClientParams);

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(RESET_PASSWORD_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null, false, true);
	}

	public void sendInactiveDossierNotification(User dest, NodeRef space,
			List<NodeRef> inactiveDossiers, Company c) throws KoyaServiceException {
		Map<String, Serializable> paramsMail = new HashMap<>();

		paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());

		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, 
				getFileTemplateRef(new RepositoryLocation(
				StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				TPL_MAIL_INACTIVEDOSSIERNOTIF_, SearchService.LANGUAGE_LUCENE)));

		Map<String, Object> templateModel = new HashMap<>();
		templateModel.put(TemplateService.KEY_COMPANY_HOME, repositoryHelper.getCompanyHome());

                templateModel.put("dest", new ScriptNode(dest.getNodeRef(), serviceRegistry));
		templateModel.put("koyaClient", koyaClientParams);
		List<Map<String, Serializable>> params = CollectionUtils.transform(inactiveDossiers,
				new Function<NodeRef, Map<String, Serializable>>() {

					@Override
					public Map<String, Serializable> apply(final NodeRef value) {
						return new HashMap<String, Serializable>() {
							{
								put("nodeRef", value);
								put("url", getDirectLinkUrl(value));

							}
						};
					}
				});
		templateModel.put("inactiveDossiers", (Serializable) params);

		templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

		/**
		 * TODO Add company and dossiers (or all path ) references to template
		 */
		paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

		/**
		 * Get subject from properties file in repository
		 */
		paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
				getI18nSubject(INACTIVEDOSSIER_NOTIFICATION_SUBJECT, templateModel));

		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				space, false, true);
	}

	/**
	 * Send again invitation mail return destination email adress.
	 * 
	 * @param inviteId
	 * @return
	 * @throws KoyaServiceException
	 */
	public String sendInviteMail(final String inviteId) throws KoyaServiceException {

		WorkflowTask task = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<WorkflowTask>() {
					@Override
					public WorkflowTask doWork() throws Exception {
						return workflowService.getStartTask(inviteId);
					}
				});

		KoyaInviteSender koyaInviteSender = new KoyaInviteSender(serviceRegistry, repositoryHelper,
				messageService, this, koyaNodeService, spaceAclService, companyService,
				companyPropertiesService, userService, koyaClientParams);

		Map<String, String> properties = new HashMap<>();

		properties.put(WorkflowModelNominatedInvitation.wfVarInviteeUserName, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME));
		properties.put(WorkflowModelNominatedInvitation.wfVarAcceptUrl, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL));
		properties.put(WorkflowModelNominatedInvitation.wfVarServerPath, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH));
		properties.put(WorkflowModelNominatedInvitation.wfVarRole, (String) task.getProperties()
				.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE));
		properties.put(WorkflowModelNominatedInvitation.wfVarInviterUserName, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME));
		properties.put(WorkflowModelNominatedInvitation.wfVarInviteeGenPassword,
				(String) task.getProperties()
						.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD));
		properties.put(WorkflowModelNominatedInvitation.wfVarResourceName, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME));
		properties.put(WorkflowModelNominatedInvitation.wfVarRejectUrl, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL));
		properties.put(WorkflowModelNominatedInvitation.wfVarInviteTicket, (String) task
				.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET));

		properties.put(WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId,
				(String) task.getProperties().get(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID));

		properties.put(InviteSender.WF_PACKAGE,
				((NodeRef) task.getProperties().get(WorkflowModel.ASSOC_PACKAGE)).toString());
		properties.put(InviteSender.WF_INSTANCE_ID, inviteId);

		koyaInviteSender.sendMail(properties);
		return (String) task.getProperties()
				.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL);
	}

	/**
	 * 
	 * @param wrapper
	 * @throws KoyaServiceException
	 */
	public void sendMail(MailWrapper wrapper) throws KoyaServiceException {

		/**
		 * Params Setting
		 */
		Map<String, Serializable> paramsMail = new HashMap<>();
		paramsMail.put(MailActionExecuter.PARAM_TO_MANY, new ArrayList(wrapper.getTo()));

		Map<String, Object> templateModel = new HashMap<>();
		/**
		 * Get subject and body Templates
		 */
		if (wrapper.getTemplatePath() != null) {
			RepositoryLocation templateLoc = new RepositoryLocation();// defaultQuery
			// language
			// =
			// xpath
			templateLoc.setPath(wrapper.getTemplatePath());
			templateLoc.setQueryLanguage(SearchService.LANGUAGE_LUCENE);
			// TODO wrapper should only indicate mail template name. Root path
			// is determniated
			// by company context (spcific template or koya generic template or
			// null)
			paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(templateLoc));

			templateModel.put("args", (Serializable) wrapper.getTemplateParams());
			templateModel.put("koyaClient", koyaClientParams);

			Company c = koyaNodeService.getKoyaNode(wrapper.getCompanyNodeRef(), Company.class);
			templateModel.put("company", companyPropertiesService.getProperties(c).toHashMap());

			paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
		} else {
			paramsMail.put(MailActionExecuter.PARAM_TEXT, wrapper.getContent());
		}

		if (wrapper.getTemplateKoyaSubjectKey() != null) {

			String subject = (String) getI18nSubject(wrapper.getTemplateKoyaSubjectKey(),
					templateModel);

			/**
			 * TODO replace MailActionExecuter.PARAM_SUBJECT_PARAMS parameter
			 */
			for (int i = 0; i < wrapper.getTemplateKoyaSubjectParams().size(); i++) {
				subject = subject.replace("{" + i + "}",
						wrapper.getTemplateKoyaSubjectParams().get(i));
			}
			paramsMail.put(MailActionExecuter.PARAM_SUBJECT, subject);

		} else {
			paramsMail.put(MailActionExecuter.PARAM_SUBJECT, wrapper.getSubject());
		}

		/**
		 * Action execution
		 */
		actionService.executeAction(actionService.createAction(MailActionExecuter.NAME, paramsMail),
				null);

	}

	public String getI18nSubject(String propKey, Map<String, Object> templateValues)
			throws KoyaServiceException {
		Properties i18n = koyaNodeService.readPropertiesFileContent(
				getFileTemplateRef(new RepositoryLocation(
				StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				TPL_MAIL_I18NSUBJECTS, SearchService.LANGUAGE_LUCENE)));
		if (i18n == null) {
			throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_INVALID_SUBJECT_PROPERTIES_PATH,
					"Invalid koya Mail subject properties path : ");
		}

		String mailSubject = i18n.getProperty(propKey);

		if (mailSubject != null && !mailSubject.isEmpty()) {
			// replace templates values in subject if exists

			Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
			Matcher m = p.matcher(mailSubject);
			while (m.find()) {
				String varName = m.group(1);
				String varReplaced = null;

				try {
					Object replaceValue = templateValues;
					for (String varChunk : varName.split("\\.")) {
						replaceValue = ((Map) replaceValue).get(varChunk);
					}
					varReplaced = replaceValue.toString();
				} catch (Exception e) {
				}

				if (varReplaced != null) {
					// Do replacement in value String
					mailSubject = mailSubject.replace("${" + varName + "}", varReplaced);
				} else {
					logger.warn("mail Subject remplacement token doesn't match a variable : "
							+ m.group());
				}
			}

			logger.trace("mail subject = " + mailSubject);

			return mailSubject;
		} else {
			throw new KoyaServiceException(
					KoyaErrorCodes.KOYAMAIL_SUBJECT_KEY_NOT_EXISTS_IN_PROPERTIES,
					" missing key = " + propKey);
		}
	}

	/**
	 * Returns nodeRef of template location. retruns I18n version if found
	 * 
	 * @param templateRepoLocation
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public NodeRef getFileTemplateRef(RepositoryLocation templateRepoLocation)
			throws KoyaServiceException {
		String locationType = templateRepoLocation.getQueryLanguage();

		if (locationType.equals(SearchService.LANGUAGE_XPATH)) {
			StoreRef store = templateRepoLocation.getStoreRef();
			String xpath = templateRepoLocation.getPath();

			try {
				List<NodeRef> nodeRefs = searchService.selectNodes(nodeService.getRootNode(store),
						xpath, null, namespaceService, false);
				if (nodeRefs.size() != 1) {
					throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE,
							nodeRefs.size() + " nodes match search");
				}
				return fileFolderService.getLocalizedSibling(nodeRefs.get(0));
			} catch (SearcherException e) {
				throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE, e);
			}
		} else
			if(locationType.equals(SearchService.LANGUAGE_LUCENE)) {
		       ResultSet rs = searchService.query(templateRepoLocation.getStoreRef(), locationType, "PATH:\""+templateRepoLocation.getPath()+"\"");

		       if(rs.length() != 1) {
				throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE,
						rs.length() + " nodes match search");
			}
		    return fileFolderService.getLocalizedSibling(rs.getNodeRef(0));
		}else {
			throw new KoyaServiceException(
					KoyaErrorCodes.KOYAMAIL_UNSUPPORTED_TEMPLATE_LOCATION_TYPE,
					"given type : " + locationType + " expected xpath");
		}
	}

	public String getDirectLinkUrl(NodeRef n) {

		if (koyaDirectLinkUrlTemplate == null || koyaClientServerPath == null
				|| koyaDirectLinkUrlTemplate.isEmpty() || koyaClientServerPath.isEmpty()) {
			return "#";// TODO build share url
		} else {

			return koyaClientServerPath + koyaDirectLinkUrlTemplate.replace("{nodeId}", n.getId());
		}
	}
	public String getDirectLinkUrl(NodeRef n,Map<String,String> attributes) {
		String url=getDirectLinkUrl(n);		
		String sep = "?";
		for(Entry<String, String> e:attributes.entrySet()){
			url += sep + e.getKey()+"="+e.getValue();
			sep="&";
		}
		return url;
	}

}
