package fr.itldev.koya.permissions;

import fr.itldev.koya.model.impl.Company;

/**
 *
 * Check each permissions access.
 */
public class GlobalPermissionsChecker {

    public Boolean checkCanListSiteRoles(Company c) {
        return true;
    }

    public Boolean checkCanCreateCompanyTest() {
        return true;
    }

    public Boolean checkCanRenameCompanyTest() {
        return true;
    }

    public Boolean checkCanDeleteCompanyTest() {
        return true;
    }

    //Administration
    public Boolean checkCanInviteUsersTest() {
        return true;
    }

    public Boolean checkCanChangeUserRoleTest() {
        return true;
    }

    public Boolean checkCanDeleteUserTest() {
        return true;
    }

    /**
     *
     */
    //refine test with specific context cases (in owner space? ) 
    //responsible or not etc ...
    public Boolean checkCanListDossierTest() {
        return true;
    }

    public Boolean checkCanCreateDossierTest() {
        return true;
    }

    public Boolean checkCanRenameDossierTest() {
        return true;
    }

    public Boolean checkCanDeleteDossierTest() {
        return true;
    }

    //Administrive
    Boolean checkCanMembersDossierTest() {
        return true;
    }

    Boolean checkCanResponsiblesDossierTest() {
        return true;
    }

}
