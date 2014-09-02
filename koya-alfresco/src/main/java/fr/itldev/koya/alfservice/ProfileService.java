/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.service.cmr.site.SiteService;

/**
 *
 */
public class ProfileService {

    private SiteService siteService;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    // </editor-fold>
    /**
     * Lists all available user Roles for a company.
     *
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<UserRole> getAvailableUserRoles(String companyName) throws KoyaServiceException {
        try {

            List<UserRole> userRoles = new ArrayList<>();
            for (String r : siteService.getSiteRoles(companyName)) {
                userRoles.add(new UserRole(r));
            }
            return userRoles;
        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }
    }

    /**
     * Returns currently setted userRole for specified user in the company
     * context.
     *
     * @param userName
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public UserRole getCurrentRole(String userName, String companyName) throws KoyaServiceException {

        //TODO exception if user can't acces the company --> no role
        try {
            /**
             * TODO use siteService.getMembersRoleInfo(userName, userName) in
             * order to get extended informations.
             *
             */
            return new UserRole(siteService.getMembersRole(companyName, userName));

        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }

    }

    /**
     * Set user identified by userName specified userRole in companyName
     * context.
     *
     * @param userName
     * @param companyName
     * @param userRole
     * @throws KoyaServiceException
     */
    public void setUserRole(String userName, String companyName, String userRole) throws KoyaServiceException {

        try {
            siteService.setMembership(companyName, userName, userRole);
        } catch (SiteDoesNotExistException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
        }

    }
}
