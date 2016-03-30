package fr.itldev.koya.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;

import com.google.common.base.Stopwatch;

import fr.itldev.koya.model.KoyaModel;

public class UpdateLastModificationDateActionExecuter extends
		ActionExecuterAbstractBase {
	protected Logger logger = Logger.getLogger(UpdateLastModificationDateActionExecuter.class);
	
	public static final String NAME = "updateLastModificationDate";
	
	protected TransactionService transactionService;
	protected NodeService nodeService;

	
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	@Override
	protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

		final Stopwatch timer = new Stopwatch().start();
		AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
					@Override
					public Object doWork() throws Exception {
						// UserTransaction transaction = transactionService
						// .getNonPropagatingUserTransaction();
						// try {
						// transaction.begin();
						transactionService
								.getRetryingTransactionHelper()
								.doInTransaction(
										new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
											@Override
											public Object execute() {
												// Add lastModified Aspect if
												// not already
												// present
												if (!nodeService
														.hasAspect(
																actionedUponNodeRef,
																KoyaModel.ASPECT_LASTMODIFIED)) {
													Map<QName, Serializable> props = new HashMap<>();
													nodeService
															.addAspect(
																	actionedUponNodeRef,
																	KoyaModel.ASPECT_LASTMODIFIED,
																	props);
												}
												logger.error("aspect check/add > "
														+ timer.elapsedMillis());

												nodeService
														.setProperty(
																actionedUponNodeRef,
																KoyaModel.PROP_LASTMODIFICATIONDATE,
																new Date());
												logger.error("setProperty lastModificationDate > "
														+ timer.elapsedMillis());

												nodeService
														.setProperty(
																actionedUponNodeRef,
																KoyaModel.PROP_NOTIFIED,
																Boolean.FALSE);
												logger.error("updateLastModificationDate > "
														+ timer.elapsedMillis());

												// transaction.commit();
												logger.error("commit > "
														+ timer.elapsedMillis());

												logger.debug("Updated lastModificationDate of dossier : "
														+ nodeService
																.getProperty(
																		actionedUponNodeRef,
																		ContentModel.PROP_TITLE));
												// } catch
												// (ConcurrencyFailureException
												// cex) {
												// // /**
												// // * silent concurency
												// exception If occurs, then
												// node
												// // * have update
												// // */
												// logger.error("ConcurrencyFailureException",
												// cex);
												// transaction.rollback();
												// } catch
												// (InvalidNodeRefException ie)
												// {
												// // Occurs on dossier node
												// creation because if
												// // separated transaction : no
												// need to update this
												// // date until dossier is
												// empty
												// logger.trace("Dossier "
												// + d.getTitle()
												// +
												// " Error writing last Update modification date : InvalidNodeRefException");
												// logger.error("InvalidNodeRefException",
												// ie);
												// transaction.rollback();
												// } catch (Throwable e) {
												// logger.warn("Dossier "
												// + d.getTitle()
												// +
												// "Error writing last Update modification date : "
												// + e.toString());
												// logger.error("Exception ",e);
												// // transaction.rollback();
												// }
												logger.error("end > "
														+ timer.elapsedMillis());
												//
												return null;
											}
										}, false, true);

						return null;
					}
				});

	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}

}
