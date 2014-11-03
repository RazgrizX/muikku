package fi.muikku.plugins.schooldatapyramus.entities;

import fi.muikku.plugins.schooldatapyramus.SchoolDataPyramusPluginDescriptor;
import fi.muikku.schooldata.entity.User;

public class PyramusUser implements User {

	public PyramusUser(String identifier, String firstName, String lastName, String displayName) {
		this.identifier = identifier;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
	}
	
	@Override
	public String getSchoolDataSource() {
		return SchoolDataPyramusPluginDescriptor.SCHOOL_DATA_SOURCE;
	}
	
	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getDisplayName() {
	  return displayName;
	}
	
	private String identifier;
	private String firstName;
	private String lastName;
	private String displayName;
}
