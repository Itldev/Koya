package fr.itldev.koya.services.cache;

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
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.impl.util.CacheConfig;
import java.io.Serializable;

public class CacheManager implements InitializingBean, Serializable {

	private static final Logger logger = Logger.getLogger(CacheManager.class);

	private Cache<String, Permissions> permissionsCache;
	private CacheConfig permissionsCacheConfig;
	private Cache<Company, Preferences> companyPreferencesCache;
	private CacheConfig companyPreferencesCacheConfig;
	private Cache<User, List<KoyaNode>> userFavouritesCache;
	private CacheConfig userFavouritesCacheConfig;
	private Cache<KoyaNode, Boolean> nodeSharedWithConsumerCache;
	private CacheConfig nodeSharedWithConsumerCacheConfig;
	private Cache<String, Map<String, String>> invitationsCache;
	private CacheConfig invitationsCacheConfig;

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

	public void setNodeSharedWithConsumerCacheConfig(
			CacheConfig nodeSharedWithConsumerCacheConfig) {
		this.nodeSharedWithConsumerCacheConfig = nodeSharedWithConsumerCacheConfig;
	}

	public void setInvitationsCacheConfig(CacheConfig invitationsCacheConfig) {
		this.invitationsCacheConfig = invitationsCacheConfig;
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

		if (nodeSharedWithConsumerCacheConfig == null) {
			nodeSharedWithConsumerCacheConfig = CacheConfig.noCache();
		}
		nodeSharedWithConsumerCacheConfig
				.debugLogConfig("nodeSharedWithConsumerCache");

		if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
			nodeSharedWithConsumerCache = CacheBuilder
					.newBuilder()
					.maximumSize(nodeSharedWithConsumerCacheConfig.getMaxSize())
					.expireAfterWrite(
							nodeSharedWithConsumerCacheConfig
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
	 * ======= Node Shared With Consumer ==========
	 * 
	 */
	public Boolean getNodeSharedWithConsumer(KoyaNode i) {
		Boolean s;

		if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
			s = nodeSharedWithConsumerCache.getIfPresent(i);
			if (s != null) {
				return s;
			}
		}
		return null;
	}

	public void setNodeSharedWithConsumer(KoyaNode i, Boolean s) {
		if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
			nodeSharedWithConsumerCache.put(i, s);
		}
	}

	public void revokeNodeSharedWithConsumer(KoyaNode i) {
		if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
			nodeSharedWithConsumerCache.invalidate(i);
		}
	}

	/**
	 * ======= Invitations ==========
	 * 
	 */
	public Map<String, String> getInvitations(String userEmail) {
		Map<String, String> invitations;

		if (invitationsCacheConfig.getEnabled()) {
			invitations = invitationsCache.getIfPresent(userEmail);
			if (invitations != null) {
				return invitations;
			}
		}
		return null;
	}

	public void setInvitations(String userEmail, Map<String, String> invitations) {
		if (invitationsCacheConfig.getEnabled()) {
			invitationsCache.put(userEmail, invitations);
		}
	}

	public void revokeInvitations(String userEmail) {
		if (invitationsCacheConfig.getEnabled()) {
			invitationsCache.invalidate(userEmail);
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
		logger.info("[nodeSharedWithConsumerCache Stats] "
				+ nodeSharedWithConsumerCache.stats().toString());
		logger.info("[invitationsCache Stats] "
				+ invitationsCache.stats().toString());

	}

}
