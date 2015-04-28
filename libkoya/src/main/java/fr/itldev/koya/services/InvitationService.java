package fr.itldev.koya.services;

import java.util.Map;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

/**
 *
 *
 */
public interface InvitationService extends AlfrescoService {

	/**
	 * Invite user identified by email on company with rolename granted.
	 * 
	 * @param userLogged
	 * @param c
	 * @param userEmail
	 * @param roleName
	 * @throws AlfrescoServiceException
	 */
	void inviteUser(User userLogged, Company c, String userEmail,
			String roleName) throws AlfrescoServiceException;

	/**
	 * Validate invitation giving user modifications;
	 * 
	 * @param user
	 * @param inviteId
	 * @param inviteTicket
	 * @throws AlfrescoServiceException
	 */
	void validateInvitation(User user, String inviteId, String inviteTicket)
			throws AlfrescoServiceException;

	/**
	 * Get user's invitation on company if exists.
	 * 
	 * @param user
	 * @param c
	 * @param userToGetInvitaion
	 * @return
	 * @throws AlfrescoServiceException
	 */
	public Map<String, String> getInvitation(User user, Company c,
			User userToGetInvitaion) throws AlfrescoServiceException;

	/**
	 * Checks anynomously if given inviteId exists (is is a pending invite).
	 * 
	 * @param inviteId
	 * @return
	 * @throws AlfrescoServiceException
	 */
	public Boolean isInvitationPending(String inviteId)
			throws AlfrescoServiceException;

	/**
	 * Send invitation mail again to invitee based on invitationId
	 * 
	 * @param user
	 * @param inviteId
	 * @throws AlfrescoServiceException
	 */
	public void reSendInviteMail(User user, String inviteId)
			throws AlfrescoServiceException;

}
