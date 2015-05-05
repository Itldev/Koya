package fr.itldev.koya.model.json;

import fr.itldev.koya.model.impl.Space;

public class KoyaShare {

	private KoyaInvite koyaInvite;
	private Space sharedSpace;
	private String email;
	private String koyaPermission;

	public KoyaInvite getKoyaInvite() {
		return koyaInvite;
	}

	public void setKoyaInvite(KoyaInvite koyaInvite) {
		this.koyaInvite = koyaInvite;
	}

	public Space getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(Space sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getKoyaPermission() {
		return koyaPermission;
	}

	public void setKoyaPermission(String koyaPermission) {
		this.koyaPermission = koyaPermission;
	}

	public KoyaShare() {

	}

	public KoyaShare(Space sharedSpace, String email, String koyaPermission) {
		super();
		this.sharedSpace = sharedSpace;
		this.email = email;
		this.koyaPermission = koyaPermission;
	}

}
