package fr.itldev.koya.permissions;

/**
 *
 *
 */
public interface UserCheckPermissionsTest {

    /**
     * Companies
     */
    void canListCompaniesTest();

    void canCreateCompanyTest();

    void canRenameCompanyTest();

    void canDeleteCompanyTest();

    //Administration
    void canInviteUsersTest();

    void canChangeUserRoleTest();

    void canDeleteUserTest();

    /**
     *
     */
    //refine test with specific context cases (in owner space? ) 
    //responsible or not etc ...
    void canListDossierTest();

    void canCreateDossierTest();

    void canRenameDossierTest();

    void canDeleteDossierTest();

    //Administrative
    void canListMembersDossierTest();

    void canListResponsiblesDossierTest();

}
