package fi.muikku.plugins.workspace.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.muikku.dao.DAO;
import fi.muikku.plugin.PluginDAO;
import fi.muikku.plugins.material.model.QueryField;
import fi.muikku.plugins.workspace.model.WorkspaceMaterial;
import fi.muikku.plugins.workspace.model.WorkspaceMaterialField;
import fi.muikku.plugins.workspace.model.WorkspaceMaterialField_;

@DAO
public class WorkspaceMaterialFieldDAO extends PluginDAO<WorkspaceMaterialField> {

  private static final long serialVersionUID = 6804548350145556078L;

  public WorkspaceMaterialField create(String name, QueryField field, WorkspaceMaterial material) {

    WorkspaceMaterialField workspaceMaterialField = new WorkspaceMaterialField();

    workspaceMaterialField.setName(name);
    workspaceMaterialField.setMaterial(material);
    workspaceMaterialField.setQueryField(field);

    return persist(workspaceMaterialField);
  }
  
  public List<WorkspaceMaterialField> findMaterialFieldByMaterial(WorkspaceMaterial material){
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<WorkspaceMaterialField> criteria = criteriaBuilder.createQuery(WorkspaceMaterialField.class);
    Root<WorkspaceMaterialField> root = criteria.from(WorkspaceMaterialField.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(WorkspaceMaterialField_.material), material));

    return entityManager.createQuery(criteria).getResultList();
    
  }

  public WorkspaceMaterialField findMaterialFieldByWorkspaceMaterialAndName(WorkspaceMaterial workspaceMaterial, String name) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<WorkspaceMaterialField> criteria = criteriaBuilder.createQuery(WorkspaceMaterialField.class);
    Root<WorkspaceMaterialField> root = criteria.from(WorkspaceMaterialField.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(WorkspaceMaterialField_.material), workspaceMaterial),
        criteriaBuilder.equal(root.get(WorkspaceMaterialField_.name), name)
      )
    );

    return getSingleResult(entityManager.createQuery(criteria));    
  }

  public List<WorkspaceMaterialField> listByWorkspaceMaterial(WorkspaceMaterial workspaceMaterial) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<WorkspaceMaterialField> criteria = criteriaBuilder.createQuery(WorkspaceMaterialField.class);
    Root<WorkspaceMaterialField> root = criteria.from(WorkspaceMaterialField.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(WorkspaceMaterialField_.material), workspaceMaterial)
    );

    return entityManager.createQuery(criteria).getResultList();
  }

}
