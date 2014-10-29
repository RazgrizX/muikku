package fi.muikku.schooldata.events;

public class SchoolDataUserEnvironmentRoleDiscoveredEvent {

  public SchoolDataUserEnvironmentRoleDiscoveredEvent(String roleDataSource, String roleIdentifier, String userDataSource, String userIdentifier) {
    super();
    this.roleDataSource = roleDataSource;
    this.roleIdentifier = roleIdentifier;
    this.userDataSource = userDataSource;
    this.userIdentifier = userIdentifier;
  }

  public String getRoleDataSource() {
    return roleDataSource;
  }

  public String getRoleIdentifier() {
    return roleIdentifier;
  }

  public String getUserDataSource() {
    return userDataSource;
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  private String roleDataSource;
  private String roleIdentifier;
  private String userDataSource;
  private String userIdentifier;
}
