package fr.itldev.koya.repo.security.authentication;

public class UserMailEntity {

	private String mail;
	private String userName;
	private String uuid;
	private String mailPropUri;
	private String mailPropName;
	private String userNamePropUri;
	private String userNamePropName;

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getMailPropUri() {
		return mailPropUri;
	}

	public void setMailPropUri(String mailPropUri) {
		this.mailPropUri = mailPropUri;
	}

	public String getMailPropName() {
		return mailPropName;
	}

	public void setMailPropName(String mailPropName) {
		this.mailPropName = mailPropName;
	}

	public String getUserNamePropUri() {
		return userNamePropUri;
	}

	public void setUserNamePropUri(String userNamePropUri) {
		this.userNamePropUri = userNamePropUri;
	}

	public String getUserNamePropName() {
		return userNamePropName;
	}

	public void setUserNamePropName(String userNamePropName) {
		this.userNamePropName = userNamePropName;
	}

}
