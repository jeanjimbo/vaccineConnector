package com.jean.project;

/*Schedule class that defines the persistent schedule entity*/
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


//Declares the class as capable of being stored and retrieved with JDO
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Schedule {
	// Required primary key populated automatically by the JDO
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private String region;
	@Persistent
	private String country;
	@Persistent
	private String location;
	@Persistent
	private String vaccCode;
	@Persistent
	private String vaccDescr;
	@Persistent
	private String disease;
	@Persistent
	private String age;
	@Persistent
	private String startDate;
	@Persistent
	private String endDate;
	@Persistent
	private String riskGroup;
	
	public Schedule(String region, String country, String location, String vaccCode, String vaccDescr, String disease, String age, String startDate, String endDate, String riskGroup)
	{
		this.region=region;
		this.country=country;
		this.location=location;
		this.vaccCode=vaccCode;
		this.vaccDescr=vaccDescr;
		this.disease=disease;
		this.age=age;
		this.startDate=startDate;
		this.endDate=endDate;
		this.riskGroup=riskGroup;		
	}
	
	public Long getId() {
		return id;
		}
		public String getRegion() {
			return region;
		}	
		public String getCountry() {
			return country;
		}
		public String getLocation() {
			return location;
		}
		public String getCode() {
			return vaccCode;
		}
		public String getDescr() {
			return vaccDescr;
		}
		public String getDisease() {
			return disease;
		}
		public String getAge() {
			return age;
		}
		public String getStart() {
			return startDate;
		}
		public String getEnd() {
			return endDate;
		}
		public String getRisk() {
			return riskGroup;
		}

		public void setId(Long id) {
			this.id = id;
			}
		public void setRegion(String region) {
			this.region = region;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public void setCode(String vaccCode) {
			this.vaccCode = vaccCode;
		}
		public void setDescr(String vaccDescr) {
			this.vaccDescr = vaccDescr;
		}
		public void setDisease(String disease) {
			this.disease = disease;
		}
		public void setAge(String age) {
			this.age = age;
		}
		public void setStart(String start) {
			this.startDate = start;
		}
		public void setEnd(String end) {
			this.endDate = end;
		}
		public void setRisk(String riskGroup) {
			this.riskGroup = riskGroup;
		}
}