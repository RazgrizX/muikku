package fi.muikku.plugins.workspace.dao;


import fi.muikku.plugins.CorePluginsDAO;
import fi.muikku.plugins.workspace.model.WorkspaceFolder;
import fi.muikku.plugins.workspace.model.WorkspaceNode;


public class WorkspaceFolderDAO extends CorePluginsDAO<WorkspaceFolder> {
	
	private static final long serialVersionUID = 9095130166469638314L;

  public WorkspaceFolder create(WorkspaceNode parent, String title, String urlName, Integer orderNumber) {
    return create(parent, title, urlName, orderNumber, Boolean.FALSE);
  }

  public WorkspaceFolder create(WorkspaceNode parent, String title, String urlName, Integer orderNumber, Boolean hidden) {
		WorkspaceFolder workspaceFolder = new WorkspaceFolder();
		workspaceFolder.setParent(parent);
		workspaceFolder.setUrlName(urlName);
		workspaceFolder.setTitle(title);
		workspaceFolder.setOrderNumber(orderNumber);
		workspaceFolder.setHidden(hidden);
		return persist(workspaceFolder);
	}

	public void delete(WorkspaceFolder workspaceFolder) {
	  super.delete(workspaceFolder);
	}
	
}
