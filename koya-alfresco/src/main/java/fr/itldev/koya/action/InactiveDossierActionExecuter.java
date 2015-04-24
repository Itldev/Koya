package fr.itldev.koya.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;


public class InactiveDossierActionExecuter extends ActionExecuterAbstractBase {

    private Logger logger = Logger
            .getLogger(InactiveDossierActionExecuter.class);
    public static final String NAME = "inactive-dossier";

    protected NodeService nodeService;
    protected KoyaNodeService koyaNodeService;
    protected KoyaMailService koyaMailService;
    protected SpaceService spaceService;
    protected DossierService dossierService;
    private SubSpaceCollaboratorsAclService subSpaceCollaboratorsAclService;

    private String inactiveFrom = "-P15D";

    public static List<KoyaPermission> permissions = Collections
            .unmodifiableList(new ArrayList() {
                {
                    add(KoyaPermissionCollaborator.RESPONSIBLE);
                }
            });

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setSpaceService(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public void setSubSpaceCollaboratorsAclService(
            SubSpaceCollaboratorsAclService subSpaceCollaboratorsAclService) {
        this.subSpaceCollaboratorsAclService = subSpaceCollaboratorsAclService;
    }

    public void setInactiveFrom(String inactiveFrom) {
        this.inactiveFrom = inactiveFrom;
    }

    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (this.nodeService.exists(actionedUponNodeRef) == true) {
            try {
                logger.debug("Company "
                        + nodeService.getProperty(actionedUponNodeRef,
                                ContentModel.PROP_TITLE)
                        + " / "
                        + nodeService.getProperty(actionedUponNodeRef,
                                ContentModel.PROP_NAME));
                Company c = koyaNodeService.getSecuredItem(actionedUponNodeRef,
                        Company.class);
                executeSpace(action, spaceService.list(c.getName(), Integer.MAX_VALUE),c);
            } catch (KoyaServiceException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private void executeSpace(Action action, List<Space> spaces, Company c)
            throws KoyaServiceException {

        for (Space space : spaces) {
            logger.debug("Space " + space.getName());
            if (space.getChildSpaces() != null
                    && !space.getChildSpaces().isEmpty()) {
                executeSpace(action, space.getChildSpaces(),c);
            }
            // Calendar c = Calendar.getInstance();
            // c.roll(Calendar.DAY_OF_YEAR, -15);
            @SuppressWarnings("unchecked")
            Map<User, List<NodeRef>> m = LazyMap.<User, List<NodeRef>> decorate(
                    new HashMap<User, List<NodeRef>>(), new Factory() {

                        @Override
                        public List<NodeRef> create() {
                            return new ArrayList<NodeRef>();
                        }
                    });

            Duration duration = new Duration(inactiveFrom);

            List<Dossier> inactiveDossiers = dossierService.getInactiveDossier(
                    space, duration.add(new Date(), duration), true);
            for (Dossier d : inactiveDossiers) {
                logger.debug("Dossier " + d.getTitle() + " inactive since "
                        + d.getLastModifiedDate());
                List<User> responsibles = subSpaceCollaboratorsAclService
                        .listUsers(d, permissions);
                for (User u : responsibles) {
                    m.get(u).add(d.getNodeRef());
                }

                 nodeService.setProperty(d.getNodeRef(),
                 KoyaModel.PROP_NOTIFIED, Boolean.TRUE);

            }

            for (Map.Entry<User, List<NodeRef>> e : m.entrySet()) {
                koyaMailService.sendInactiveDossierNotification(e.getKey(),
                        space.getNodeRef(), e.getValue(),c);
            }

        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        logger.debug("addParameterDefinitions");
        paramList.add(new ParameterDefinitionImpl("dummy-param",
                DataTypeDefinition.QNAME, true,
                getParamDisplayLabel("dummy-param")));
    }
}
