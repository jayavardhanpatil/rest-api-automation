package request;

public class CreateMessage {
	
	private String firstname;
	private String lastname;
	private String profilename;
	
	public CreateMessage() {
		
		this.firstname = "givenName";
		this.lastname = "lastname";
		this.profilename = "Bhoj";
	}
	
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getProfilename() {
		return profilename;
	}
	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}
	  

	
}
