package com.jean.project;

/*inboundSMS class that defines the persistent inboundSMS entity
 * enables saving of inbound SMSs into the datastore for later processing*/
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

public class inboundSMS {
	// Required primary key populated automatically by JDO
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private int number;
	@Persistent
	private String age;
	@Persistent
	private String disease;
	@Persistent
	private String location;
	@Persistent
	private String riskGroup;

	
	public inboundSMS(int number, String age, String disease, String location, String riskGroup)
	{
		this.number=number;
		this.age=age;
		this.location=location;
		this.disease=disease;
		this.riskGroup=riskGroup;		
	}
	
	public Long getId() {
		return id;
		}

	public int getNumber() {
		return number;
		}
	
		public String getLocation() {
			return location;
		}

		public String getDisease() {
			return disease;
		}
		public String getAge() {
			return age;
		}

		public String getRisk() {
			return riskGroup;
		}

		public void setId(Long id) {
			this.id = id;
			}

		public void setLocation(String location) {
			this.location = location;
		}

		public void setDisease(String disease) {
			this.disease = disease;
		}
		public void setAge(String age) {
			this.age = age;
		}

		public void setRisk(String riskGroup) {
			this.riskGroup = riskGroup;
		}
}
