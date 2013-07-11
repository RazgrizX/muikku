package fi.muikku.model.workspace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import fi.muikku.model.base.SchoolDataSource;
import fi.muikku.model.util.ArchivableEntity;
import fi.muikku.model.util.SchoolDataEntity;
import fi.muikku.security.ContextReference;

@Entity
public class WorkspaceEntity implements SchoolDataEntity, ArchivableEntity, ContextReference {
  
  public Long getId() {
    return id;
  }
  
  public String getIdentifier() {
		return identifier;
	}
  
  public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

  @Override
  public SchoolDataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(SchoolDataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  public String getUrlName() {
		return urlName;
	}
  
  public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

  public Boolean getArchived() {
    return archived;
  }

  public void setArchived(Boolean archived) {
    this.archived = archived;
  }

  @Id
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private Long id;

	@NotEmpty
	@NotNull
	@Column(nullable = false)
	private String identifier;

	@ManyToOne
  private SchoolDataSource dataSource;

	@NotEmpty
	@NotNull
	@Column(nullable = false)
	private String urlName;
  
  @NotNull
  @Column(nullable = false)
  private Boolean archived = Boolean.FALSE;
}