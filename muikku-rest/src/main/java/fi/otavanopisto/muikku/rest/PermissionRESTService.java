package fi.otavanopisto.muikku.rest;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.otavanopisto.muikku.controller.PermissionController;
import fi.otavanopisto.muikku.dao.security.PermissionDAO;
import fi.otavanopisto.muikku.model.security.EnvironmentRolePermission;
import fi.otavanopisto.muikku.model.security.Permission;
import fi.otavanopisto.muikku.model.security.WorkspaceGroupPermission;
import fi.otavanopisto.muikku.model.security.WorkspaceRolePermission;
import fi.otavanopisto.muikku.model.users.RoleEntity;
import fi.otavanopisto.muikku.model.users.UserGroupEntity;
import fi.otavanopisto.muikku.model.workspace.WorkspaceEntity;
import fi.otavanopisto.muikku.rest.model.EnvironmentUserRolePermission;
import fi.otavanopisto.muikku.rest.model.WorkspaceUserGroupPermission;
import fi.otavanopisto.muikku.rest.model.WorkspaceUserRolePermission;
import fi.otavanopisto.muikku.schooldata.RoleController;
import fi.otavanopisto.muikku.schooldata.WorkspaceController;
import fi.otavanopisto.muikku.security.MuikkuPermissions;
import fi.otavanopisto.muikku.session.SessionController;
import fi.otavanopisto.muikku.users.UserGroupEntityController;
import fi.otavanopisto.security.rest.RESTPermit;
import fi.otavanopisto.security.rest.RESTPermit.Handling;

@Stateful
@RequestScoped
@Path("/permission")
@Produces ("application/json")
public class PermissionRESTService extends AbstractRESTService {

  @Inject
  private PermissionDAO permissionDAO;
  
  @Inject
  private WorkspaceController workspaceController;
  
  @Inject
  private UserGroupEntityController userGroupEntityController;
  
  @Inject
  private PermissionController permissionController;
  
  @Inject
  private RoleController roleController;
  
  @Inject
  private SessionController sessionController;
  
  @PUT
  @Path ("/environmentUserRolePermissions")
  @RESTPermit(handling = Handling.INLINE, requireLoggedIn = true)
  public Response setEnvironmentUserRolePermission(EnvironmentUserRolePermission payload) {
    RoleEntity roleEntity = roleController.findRoleEntityById(payload.getUserRoleId());
    Permission permission = permissionDAO.findById(payload.getPermissionId());

    if ((roleEntity == null) || (permission == null)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    if (!sessionController.hasPermission(MuikkuPermissions.MANAGE_PERMISSIONS, null)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    try {
      EnvironmentRolePermission rolePermission = permissionController.findEnvironmentRolePermission(roleEntity, permission);
      
      if (payload.getPermitted() && (rolePermission == null))
        permissionController.addEnvironmentRolePermission(roleEntity, permission);
      else {
        if (rolePermission != null)
          permissionController.removeEnvironmentRolePermission(rolePermission);
      }

      return Response.noContent().build();
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }
  
  @PUT
  @Path ("/workspaceUserRolePermissions")
  @RESTPermit(handling = Handling.INLINE, requireLoggedIn = true)
  public Response setWorkspaceUserRolePermission(WorkspaceUserRolePermission payload) {
    RoleEntity roleEntity = roleController.findRoleEntityById(payload.getUserRoleId());
    Permission permission = permissionDAO.findById(payload.getPermissionId());
    WorkspaceEntity workspaceEntity = workspaceController.findWorkspaceEntityById(payload.getWorkspaceId());

    if ((roleEntity == null) || (permission == null) || (workspaceEntity == null)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    if (!sessionController.hasPermission(MuikkuPermissions.WORKSPACE_MANAGEWORKSPACESETTINGS, workspaceEntity)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    try {
      WorkspaceRolePermission rolePermission = permissionController.findWorkspaceRolePermission(workspaceEntity, roleEntity, permission);
      
      if (payload.getPermitted() && (rolePermission == null))
        permissionController.addWorkspaceRolePermission(workspaceEntity, roleEntity, permission);
      else {
        if (rolePermission != null)
          permissionController.removeWorkspaceRolePermission(rolePermission);
      }

      return Response.noContent().build();
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }
  
  @PUT
  @Path ("/workspaceUserGroupPermissions")
  @RESTPermit(handling = Handling.INLINE, requireLoggedIn = true)
  public Response setWorkspaceUserGroupPermission(WorkspaceUserGroupPermission payload) {
    UserGroupEntity userGroupEntity = userGroupEntityController.findUserGroupEntityById(payload.getUserGroupId());
    Permission permission = permissionDAO.findById(payload.getPermissionId());
    WorkspaceEntity workspaceEntity = workspaceController.findWorkspaceEntityById(payload.getWorkspaceId());

    if (!sessionController.hasPermission(MuikkuPermissions.WORKSPACE_MANAGEWORKSPACESETTINGS, workspaceEntity)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    if ((userGroupEntity == null) || (permission == null)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    try {
      if (payload.getPermitted())
        permissionController.addWorkspaceGroupPermission(workspaceEntity, userGroupEntity, permission);
      else {
        WorkspaceGroupPermission workspaceGroupPermission = permissionController.findWorkspaceGroupPermission(workspaceEntity, userGroupEntity, permission);
        
        if (workspaceGroupPermission != null)
          permissionController.removeWorkspaceGroupPermission(workspaceGroupPermission);
        else
          return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.noContent().build();
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }
  
}
