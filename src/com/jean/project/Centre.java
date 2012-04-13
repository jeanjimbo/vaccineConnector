package com.jean.project;
/*Centre class that defines the persistent centre entity*/
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Centre {

	// Required primary key populated automatically by JDO
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private String centreName;
	@Persistent
	private String number;
	@Persistent
	private String email;
	@Persistent
	private String twitter;
	
	
	public Centre(String centreName, String number, String email, String twitterHandle)
	{
		this.centreName=centreName;
		this.number=number;
		this.email=email;
		this.twitter=twitterHandle;
	
	}
	
		public Long getId() {
		return id;
		}
		public String getCentreName() {
			return centreName;
		}	
		public String getNumber() {
			return number;
		}
		public String getLocation() {
			return email;
		}
		public String getTwitterHandle() {
			return twitter;
		}

		public void setId(Long id) {
			this.id = id;
			}
		public void setCentreName(String centreName) {
			this.centreName = centreName;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public void setTwitterHandle(String twitterHandle) {
			this.twitter = twitterHandle;
		}
}