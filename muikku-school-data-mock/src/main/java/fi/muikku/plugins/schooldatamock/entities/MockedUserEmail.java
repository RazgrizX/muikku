package fi.muikku.plugins.schooldatamock.entities;

import fi.muikku.plugins.schooldatamock.MockedUserSchoolDataBridge;
import fi.muikku.schooldata.entity.UserEmail;

public class MockedUserEmail implements UserEmail {

	public MockedUserEmail(String identifier, String userIdentifier, String address) {
		this.identifier = identifier;
		this.userIdentifier = userIdentifier;
		this.address = address;
	}
	
	@Override
	public String getSchoolDataSource() {
		return MockedUserSchoolDataBridge.SCHOOL_DATA_SOURCE;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getUserIdentifier() {
		return userIdentifier;
	}

	@Override
	public String getAddress() {
		return address;
	}
	
	@Override
	public void setAddress(String address) {
		this.address = address;
	}

	private String identifier;

	private String userIdentifier;

	private String address;

}
