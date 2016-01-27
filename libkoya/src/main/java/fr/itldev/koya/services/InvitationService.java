package fr.itldev.koya.services;

import java.util.List;
import java.util.Map;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
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
	KoyaInvite inviteUser(User userLogged, Company c, String userEmail,
			String roleName) throws AlfrescoServiceException;

	/**
	 * Validate invitation giving user modifications;
	 * 
	 * @param user
	 * @param inviteId
	 * @param inviteTicket
	 * @throws AlfrescoServiceException
	 */
	User validateInvitation(User user, String inviteId, String inviteTicket)
			throws AlfrescoServiceException;

	/**
	 * Get user's pending invitation on company if exists.
	 * 
	 * @param user
	 * @param c
	 * @param userToGetInvitaion
	 * @return
	 * @throws AlfrescoServiceException
	 */
	public Map<String, String> getInvitation(Company c,
			String userName) throws AlfrescoServiceException;

	
	/**
	 * List all pending invitations for user
	 * @param user
	 * @param userToGetInvitaion
	 * @return
	 * @throws AlfrescoServiceException
	 */
	public List<Map<String, String>> listInvitations(String userName) throws AlfrescoServiceException;
	
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
