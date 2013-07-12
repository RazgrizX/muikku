package fi.muikku.schooldata;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.muikku.dao.base.SchoolDataSourceDAO;
import fi.muikku.dao.users.EnvironmentRoleEntityDAO;
import fi.muikku.dao.users.RoleEntityDAO;
import fi.muikku.dao.users.RoleSchoolDataIdentifierDAO;
import fi.muikku.dao.workspace.WorkspaceRoleEntityDAO;
import fi.muikku.model.base.SchoolDataSource;
import fi.muikku.model.users.EnvironmentRoleEntity;
import fi.muikku.model.users.RoleEntity;
import fi.muikku.model.users.RoleSchoolDataIdentifier;
import fi.muikku.model.workspace.WorkspaceRoleEntity;
import fi.muikku.schooldata.entity.Role;

@Dependent
@Stateless
public class RoleController {

	@Inject
	private Logger logger;

	@Inject
	private SchoolDataSourceDAO schoolDataSourceDAO;
	
	@Inject
	private UserSchoolDataController userSchoolDataController;

  @Inject
  private RoleEntityDAO roleEntityDAO;
  
  @Inject
  private EnvironmentRoleEntityDAO environmentRoleEntityDAO;

  @Inject
  private WorkspaceRoleEntityDAO workspaceRoleEntityDAO;

  @Inject
  private RoleSchoolDataIdentifierDAO roleSchoolDataIdentifierDAO;
  
  /* Roles */

	public List<Role> listRoles() {
		return userSchoolDataController.listRoles();
	}
	
	public void setRoleEntity(String schoolDataSource, String identifier, RoleEntity roleEntity) {
		SchoolDataSource dataSource = schoolDataSourceDAO.findByIdentifier(schoolDataSource);
		if (dataSource != null) {
		  RoleSchoolDataIdentifier roleSchoolDataIdentifier = roleSchoolDataIdentifierDAO.findByDataSourceAndIdentifier(dataSource, identifier);
		  if (roleSchoolDataIdentifier != null) {
		  	if (roleEntity != null) {
		  	  roleSchoolDataIdentifierDAO.updateRoleEntity(roleSchoolDataIdentifier, roleEntity);
		  	} else {
		  		roleSchoolDataIdentifierDAO.delete(roleSchoolDataIdentifier);
		  	}
		  } else {
		  	if (roleEntity != null) {
		  		roleSchoolDataIdentifierDAO.create(dataSource, identifier, roleEntity);
		  	}		  	
		  }
		}
	}

	/* Role Entities */

	public RoleEntity findRoleEntityById(Long id) {
		return roleEntityDAO.findById(id);
	}

	public RoleEntity findRoleEntity(Role role) {
		SchoolDataSource schoolDataSource = schoolDataSourceDAO.findByIdentifier(role.getSchoolDataSource());
		if (schoolDataSource != null) {
		  RoleSchoolDataIdentifier schoolDataIdentifier = roleSchoolDataIdentifierDAO.findByDataSourceAndIdentifier(schoolDataSource, role.getIdentifier());
		  if (schoolDataIdentifier != null) {
		  	return schoolDataIdentifier.getRoleEntity();
		  }
		}
		
		return null;
	}
	
	public List<RoleEntity> listRoleEntities() {
		return roleEntityDAO.listAll();
	}

	/* Environment Role Entities */

	public List<EnvironmentRoleEntity> listEnvironmentRoleEntities() {
		return environmentRoleEntityDAO.listAll();
	}

	/* Workspace Role Entities */
	
	public List<WorkspaceRoleEntity> listWorkspaceRoleEntities() {
		return workspaceRoleEntityDAO.listAll();
	}
  
  
}
