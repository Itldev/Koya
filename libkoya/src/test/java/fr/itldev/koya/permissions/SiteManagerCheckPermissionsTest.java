package fr.itldev.koya.permissions;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class SiteManagerCheckPermissionsTest extends TestCase implements
		UserCheckPermissionsTest {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void checkCanListSiteRoles() {
		assertTrue(true/* canlistSiteRoles */);
	}

	@Test
	public void checkCanCreateDossier() {

	}

	@Override
	public void canListCompaniesTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canCreateCompanyTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canRenameCompanyTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canDeleteCompanyTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canInviteUsersTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canChangeUserRoleTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canDeleteUserTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canListDossierTest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void canCreateDossierTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canRenameDossierTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canDeleteDossierTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canListMembersDossierTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void canListResponsiblesDossierTest() {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

}
