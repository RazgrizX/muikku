package fi.muikku.plugins.workspace;

import java.util.List;

import javax.inject.Inject;

import fi.muikku.model.workspace.WorkspaceEntity;
import fi.muikku.plugins.workspace.dao.WorkspaceJournalEntryDAO;
import fi.muikku.plugins.workspace.model.WorkspaceJournalEntry;

public class WorkspaceJournalController {
  
  @Inject
  private WorkspaceJournalEntryDAO workspaceJournalEntryDAO;
  
  public void createJournalEntry(WorkspaceEntity workspaceEntity,
                                 String html,
                                 String title
  ) {
    workspaceJournalEntryDAO.create(workspaceEntity, html, title);
  }
  
  public List<WorkspaceJournalEntry> listEntries(WorkspaceEntity workspaceEntity) {
    return workspaceJournalEntryDAO.listByWorkspaceEntityId(workspaceEntity.getId());
  }

}
