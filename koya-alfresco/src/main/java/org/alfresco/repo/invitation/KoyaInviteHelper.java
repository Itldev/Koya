package org.alfresco.repo.invitation;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.site.SiteService;

/**
 *
 *
 */
public class KoyaInviteHelper extends InviteHelper {

    protected SiteService siteService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    /**
     * redefined method in order to execute membership setting as system user so
     * collaborator can send invitations that are acceptable.
     *
     * @param invitee
     * @param siteName
     * @param role
     * @param runAsUser
     * @param overrideExisting
     */
    @Override
    public void addSiteMembership(final String invitee, final String siteName,
            final String role, final String runAsUser, final boolean overrideExisting) {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                if (overrideExisting || !siteService.isMember(siteName, invitee)) {
                    siteService.setMembership(siteName, invitee, role);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
