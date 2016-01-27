package fr.itldev.koya.services.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.impl.util.CacheConfig;

public class CacheManager implements InitializingBean, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CacheManager.class);

	private Cache<String, Permissions> permissionsCache;
	private CacheConfig permissionsCacheConfig;
	private Cache<Company, Preferences> companyPreferencesCache;
	private CacheConfig companyPreferencesCacheConfig;
	private Cache<User, List<KoyaNode>> userFavouritesCache;
	private CacheConfig userFavouritesCacheConfig;
	private Cache<KoyaNode, Boolean> nodeSharedWithKoyaClientCache;
	private CacheConfig nodeSharedWithKoyaClientCacheConfig;
	private Cache<KoyaNode, Boolean> nodeSharedWithKoyaPartnerCache;
	private CacheConfig nodeSharedWithKoyaPartnerCacheConfig;
	private Cache<String, List<Map<String, String>>> invitationsCache;
	private CacheConfig invitationsCacheConfig;
	private Cache<String, Boolean> isManagerCache;
	private CacheConfig isManagerCacheConfig;
	private Cache<User, List<String>> userGroupsCache;
	private CacheConfig userGroupsCacheConfig;
	private Cache<Dossier, Boolean> dossierConfidentialCache;
	private CacheConfig dossierConfidentialCacheConfig;

	public void setPermissionsCacheConfig(CacheConfig permissionsCacheConfig) {
		this.permissionsCacheConfig = permissionsCacheConfig;
	}

	public void setCompanyPreferencesCacheConfig(
			CacheConfig companyPreferencesCacheConfig) {
		this.companyPreferencesCacheConfig = companyPreferencesCacheConfig;
	}

	public void setUserFavouritesCacheConfig(
			CacheConfig userFavouritesCacheConfig) {
		this.userFavouritesCacheConfig = userFavouritesCacheConfig;
	}

	public void setNodeSharedWithKoyaClientCacheConfig(
			CacheConfig nodeSharedWithKoyaClientCacheConfig) {
		this.nodeSharedWithKoyaClientCacheConfig = nodeSharedWithKoyaClientCacheConfig;
	}
	
	public void setNodeSharedWithKoyaPartnerCacheConfig(
			CacheConfig nodeSharedWithKoyaPartnerCacheConfig) {
		this.nodeSharedWithKoyaPartnerCacheConfig = nodeSharedWithKoyaPartnerCacheConfig;
	}

	public void setInvitationsCacheConfig(CacheConfig invitationsCacheConfig) {
		this.invitationsCacheConfig = invitationsCacheConfig;
	}

	public void setIsManagerCacheConfig(CacheConfig isManagerCacheConfig) {
		this.isManagerCacheConfig = isManagerCacheConfig;
	}

	public void setUserGroupsCacheConfig(CacheConfig userGroupsCacheConfig) {
		this.userGroupsCacheConfig = userGroupsCacheConfig;
	}

	public void setDossierConfidentialCacheConfig(
			CacheConfig dossierConfidentialCacheConfig) {
		this.dossierConfidentialCacheConfig = dossierConfidentialCacheConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		//
		if (permissionsCacheConfig == null) {
			permissionsCacheConfig = CacheConfig.noCache();
		}
		permissionsCacheConfig.debugLogConfig("permissionsCache");

		if (permissionsCacheConfig.getEnabled()) {
			permissionsCache = CacheBuilder
					.newBuilder()
					.maximumSize(permissionsCacheConfig.getMaxSize())
					.expireAfterWrite(
							permissionsCacheConfig.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//
		if (companyPreferencesCacheConfig == null) {
			companyPreferencesCacheConfig = CacheConfig.noCache();
		}

		companyPreferencesCacheConfig.debugLogConfig("companyPreferencesCache");

		if (companyPreferencesCacheConfig.getEnabled()) {
			companyPreferencesCache = CacheBuilder
					.newBuilder()
					.maximumSize(companyPreferencesCacheConfig.getMaxSize())
					.expireAfterWrite(
							companyPreferencesCacheConfig
									.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//
		if (userFavouritesCacheConfig == null) {
			userFavouritesCacheConfig = CacheConfig.noCache();
		}
		userFavouritesCacheConfig.debugLogConfig("userFavouritesCache");

		if (userFavouritesCacheConfig.getEnabled()) {
			userFavouritesCache = CacheBuilder
					.newBuilder()
					.maximumSize(userFavouritesCacheConfig.getMaxSize())
					.expireAfterWrite(
							userFavouritesCacheConfig
									.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//

		if (nodeSharedWithKoyaClientCacheConfig == null) {
			nodeSharedWithKoyaClientCacheConfig = CacheConfig.noCache();
		}
		nodeSharedWithKoyaClientCacheConfig
				.debugLogConfig("nodeSharedWithKoyaClientCache");

		if (nodeSharedWithKoyaClientCacheConfig.getEnabled()) {
			nodeSharedWithKoyaClientCache = CacheBuilder
					.newBuilder()
					.maximumSize(nodeSharedWithKoyaClientCacheConfig.getMaxSize())
					.expireAfterWrite(
							nodeSharedWithKoyaClientCacheConfig
									.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}
		//
		if (nodeSharedWithKoyaPartnerCacheConfig == null) {
			nodeSharedWithKoyaPartnerCacheConfig = CacheConfig.noCache();
		}
		nodeSharedWithKoyaPartnerCacheConfig
				.debugLogConfig("nodeSharedWithKoyaPartnerCache");

		if (nodeSharedWithKoyaPartnerCacheConfig.getEnabled()) {
			nodeSharedWithKoyaPartnerCache = CacheBuilder
					.newBuilder()
					.maximumSize(nodeSharedWithKoyaPartnerCacheConfig.getMaxSize())
					.expireAfterWrite(
							nodeSharedWithKoyaPartnerCacheConfig
									.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}
		
		//
		if (invitationsCacheConfig == null) {
			invitationsCacheConfig = CacheConfig.noCache();
		}
		invitationsCacheConfig.debugLogConfig("invitationsCache");

		if (invitationsCacheConfig.getEnabled()) {
			invitationsCache = CacheBuilder
					.newBuilder()
					.maximumSize(invitationsCacheConfig.getMaxSize())
					.expireAfterWrite(
							invitationsCacheConfig.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//
		if (isManagerCacheConfig == null) {
			isManagerCacheConfig = CacheConfig.noCache();
		}
		isManagerCacheConfig.debugLogConfig("isManagerCache");

		if (isManagerCacheConfig.getEnabled()) {
			isManagerCache = CacheBuilder
					.newBuilder()
					.maximumSize(isManagerCacheConfig.getMaxSize())
					.expireAfterWrite(
							isManagerCacheConfig.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//
		if (userGroupsCacheConfig == null) {
			userGroupsCacheConfig = CacheConfig.noCache();
		}
		userGroupsCacheConfig.debugLogConfig("userGroupsCache");

		if (userGroupsCacheConfig.getEnabled()) {
			userGroupsCache = CacheBuilder
					.newBuilder()
					.maximumSize(userGroupsCacheConfig.getMaxSize())
					.expireAfterWrite(
							userGroupsCacheConfig.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

		//
		if (dossierConfidentialCacheConfig == null) {
			dossierConfidentialCacheConfig = CacheConfig.noCache();
		}
		dossierConfidentialCacheConfig
				.debugLogConfig("dossierConfidentialCache");

		if (dossierConfidentialCacheConfig.getEnabled()) {
			dossierConfidentialCache = CacheBuilder
					.newBuilder()
					.maximumSize(dossierConfidentialCacheConfig.getMaxSize())
					.expireAfterWrite(
							dossierConfidentialCacheConfig
									.getExpireAfterWriteSeconds(),
							TimeUnit.SECONDS).build();
		}

	}

	/**
	 * 
	 * ======= USER PERMISSIONS ========
	 * 
	 */

	public Permissions getPermission(User u, NodeRef n) {
		Permissions p;

		if (permissionsCacheConfig.getEnabled()) {
			p = permissionsCache.getIfPresent(permissionKey(u, n));
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	public void setPermission(User u, NodeRef n, Permissions p) {
		if (permissionsCacheConfig.getEnabled()) {
			permissionsCache.put(permissionKey(u, n), p);
		}
	}

	public void revokePermission(User u, NodeRef n) {
		if (permissionsCacheConfig.getEnabled()) {
			permissionsCache.invalidate(permissionKey(u, n));
		}
	}

	private String permissionKey(User u, NodeRef n) {
		return u.getUserName() + "-" + n.getId();
	}

	/**
	 * ======= Company Preferences ==========
	 * 
	 */
	public Preferences getCompanyPreferences(Company c) {
		Preferences p;

		if (companyPreferencesCacheConfig.getEnabled()) {
			p = companyPreferencesCache.getIfPresent(c);
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	public void setCompanyPreferences(Company c, Preferences p) {
		if (companyPreferencesCacheConfig.getEnabled()) {
			companyPreferencesCache.put(c, p);
		}
	}

	public void revokeCompanyPreferences(Company c) {
		if (companyPreferencesCacheConfig.getEnabled()) {
			companyPreferencesCache.invalidate(c);
		}
	}

	/**
	 * ======= User Favourites ==========
	 * 
	 */
	public List<KoyaNode> getUserFavourites(User u) {
		List<KoyaNode> f;

		if (userFavouritesCacheConfig.getEnabled()) {
			f = userFavouritesCache.getIfPresent(u);
			if (f != null) {
				return f;
			}
		}
		return null;
	}

	public void setUserFavourites(User u, List<KoyaNode> favourites) {
		if (userFavouritesCacheConfig.getEnabled()) {
			userFavouritesCache.put(u, favourites);
		}
	}

	public void revokeUserFavourites(User u) {
		if (userFavouritesCacheConfig.getEnabled()) {
			userFavouritesCache.invalidate(u);
		}
	}

	/**
	 * ======= Node Shared With Koya Client ==========
	 * 
	 */
	public Boolean getNodeSharedWithKoyaClient(KoyaNode i) {
		Boolean s;

		if (nodeSharedWithKoyaClientCacheConfig.getEnabled()) {
			s = nodeSharedWithKoyaClientCache.getIfPresent(i);
			if (s != null) {
				return s;
			}
		}
		return null;
	}

	public void setNodeSharedWithKoyaClient(KoyaNode i, Boolean s) {
		if (nodeSharedWithKoyaClientCacheConfig.getEnabled()) {
			nodeSharedWithKoyaClientCache.put(i, s);
		}
	}

	public void revokeNodeSharedWithKoyaClient(KoyaNode i) {
		if (nodeSharedWithKoyaClientCacheConfig.getEnabled()) {
			nodeSharedWithKoyaClientCache.invalidate(i);
		}
	}

	

	/**
	 * ======= Node Shared With Koya Partner ==========
	 * 
	 */
	public Boolean getNodeSharedWithKoyaPartner(KoyaNode i) {
		Boolean s;

		if (nodeSharedWithKoyaPartnerCacheConfig.getEnabled()) {
			s = nodeSharedWithKoyaPartnerCache.getIfPresent(i);
			if (s != null) {
				return s;
			}
		}
		return null;
	}

	public void setNodeSharedWithKoyaPartner(KoyaNode i, Boolean s) {
		if (nodeSharedWithKoyaPartnerCacheConfig.getEnabled()) {
			nodeSharedWithKoyaPartnerCache.put(i, s);
		}
	}

	public void revokeNodeSharedWithKoyaPartner(KoyaNode i) {
		if (nodeSharedWithKoyaPartnerCacheConfig.getEnabled()) {
			nodeSharedWithKoyaPartnerCache.invalidate(i);
		}
	}

	
	/**
	 * ======= Invitations ==========
	 * 
	 */
	public List<Map<String, String>> getInvitations(String userName) {
		List<Map<String, String>> invitations;

		if (invitationsCacheConfig.getEnabled()) {
			invitations = invitationsCache.getIfPresent(userName);
			if (invitations != null) {
				return invitations;
			}
		}
		return null;
	}

	public void setInvitations(String userName, List<Map<String, String>> invitations) {
		if (invitationsCacheConfig.getEnabled()) {
			invitationsCache.put(userName, invitations);
		}
	}

	public void revokeInvitations(String userName) {
		if (invitationsCacheConfig.getEnabled()) {
			invitationsCache.invalidate(userName);
		}
	}

	/**
	 * 
	 * ======= USER IS MANAGER ========
	 * 
	 */

	public Boolean getIsManager(User u, Company c) {
		Boolean isManager;

		if (isManagerCacheConfig.getEnabled()) {
			isManager = isManagerCache.getIfPresent(isManagerKey(u, c));
			if (isManager != null) {
				return isManager;
			}
		}
		return null;
	}

	public void setIsManager(User u, Company c, Boolean isManager) {
		if (isManagerCacheConfig.getEnabled()) {
			isManagerCache.put(isManagerKey(u, c), isManager);
		}
	}

	public void revokeIsManager(User u, Company c) {
		if (isManagerCacheConfig.getEnabled()) {
			isManagerCache.invalidate(isManagerKey(u, c));
		}
	}

	private String isManagerKey(User u, Company c) {
		return u.getUserName() + "-" + c.getNodeRef().getId();
	}

	/**
	 * 
	 * ======= User GROUPS ========
	 * 
	 */

	public List<String> getUserGroups(User u) {
		List<String> userGroups;
		if (userGroupsCacheConfig.getEnabled()) {
			userGroups = userGroupsCache.getIfPresent(u);
			if (userGroups != null) {
				return userGroups;
			}
		}
		return null;
	}

	public void setUserGroups(User u, List<String> userGroups) {
		if (userGroupsCacheConfig.getEnabled()) {
			userGroupsCache.put(u, userGroups);
		}
	}

	public void revokeUserGroups(User u) {
		if (userGroupsCacheConfig.getEnabled()) {
			userGroupsCache.invalidate(u);
		}
	}

	/**
	 * 
	 * ======= Dossier Confidential ========
	 * 
	 */

	public Boolean getDossierConfidential(Dossier d) {
		Boolean confidential;
		if (dossierConfidentialCacheConfig.getEnabled()) {
			confidential = dossierConfidentialCache.getIfPresent(d);
			if (confidential != null) {
				return confidential;
			}
		}
		return null;
	}

	public void setDossierConfidential(Dossier d, Boolean confidential) {
		if (dossierConfidentialCacheConfig.getEnabled()) {
			dossierConfidentialCache.put(d, confidential);
		}
	}

	public void revokeDossierConfidential(Dossier d) {
		if (dossierConfidentialCacheConfig.getEnabled()) {
			dossierConfidentialCache.invalidate(d);
		}
	}

	/**
	 * 
	 * =================== Cache Dump =======================
	 * 
	 */

	public void dumpCacheStatistics() {
		logger.info("[permissionsCache Stats] "
				+ permissionsCache.stats().toString());
		logger.info("[companyPreferencesCache Stats] "
				+ companyPreferencesCache.stats().toString());
		logger.info("[userFavouritesCache Stats] "
				+ userFavouritesCache.stats().toString());
		logger.info("[nodeSharedWithKoyaClientCache Stats] "
				+ nodeSharedWithKoyaClientCache.stats().toString());
		logger.info("[invitationsCache Stats] "
				+ invitationsCache.stats().toString());
		logger.info("[isManagerCache Stats] "
				+ isManagerCache.stats().toString());
		logger.info("[userGroupsCache Stats] "
				+ userGroupsCache.stats().toString());
		logger.info("[dossierConfidentialCache Stats] "
				+ dossierConfidentialCache.stats().toString());

	}

}
