package fi.muikku.plugins.communicator;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fi.muikku.model.users.EnvironmentRoleArchetype;
import fi.muikku.model.workspace.WorkspaceRoleArchetype;
import fi.muikku.security.AbstractMuikkuPermissionCollection;
import fi.muikku.security.MuikkuPermissionCollection;
import fi.muikku.security.PermissionScope;
import fi.muikku.security.Scope;

@ApplicationScoped
public class CommunicatorPermissionCollection extends AbstractMuikkuPermissionCollection implements MuikkuPermissionCollection {

  @Scope (PermissionScope.PERSONAL)
  public static final String COMMUNICATOR_MANAGE_SETTINGS = "COMMUNICATOR_MANAGE_SETTINGS";
  
  @Override
  public List<String> listPermissions() {
    return listPermissions(CommunicatorPermissionCollection.class);
  }

  @Override
  public boolean containsPermission(String permission) {
    return listPermissions().contains(permission);
  }
  
  @Override
  public String getPermissionScope(String permission) throws NoSuchFieldException {
    return getPermissionScope(CommunicatorPermissionCollection.class, permission);
  }
  
  @Override
  public String[] getDefaultPseudoRoles(String permission) throws NoSuchFieldException {
    return getDefaultPseudoRoles(CommunicatorPermissionCollection.class, permission);
  }

  @Override
  public EnvironmentRoleArchetype[] getDefaultEnvironmentRoles(String permission) throws NoSuchFieldException {
    return getDefaultEnvironmentRoles(CommunicatorPermissionCollection.class, permission);
  }

  @Override
  public WorkspaceRoleArchetype[] getDefaultWorkspaceRoles(String permission) throws NoSuchFieldException {
    return getDefaultWorkspaceRoles(CommunicatorPermissionCollection.class, permission);
  }
}
