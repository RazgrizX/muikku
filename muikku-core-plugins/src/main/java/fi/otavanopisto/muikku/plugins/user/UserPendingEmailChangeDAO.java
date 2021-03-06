package fi.otavanopisto.muikku.plugins.user;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.muikku.plugins.user.UserPendingEmailChange_;
import fi.otavanopisto.muikku.model.users.UserEmailEntity;
import fi.otavanopisto.muikku.plugins.CorePluginsDAO;
import fi.otavanopisto.muikku.plugins.user.UserPendingEmailChange;

@Deprecated
public class UserPendingEmailChangeDAO extends CorePluginsDAO<UserPendingEmailChange> {

  private static final long serialVersionUID = 4107269645530561563L;

  public UserPendingEmailChange create(UserEmailEntity userEmailEntity, String newEmail, String confirmationHash) {
    UserPendingEmailChange userPendingEmailChange = new UserPendingEmailChange();
    
    userPendingEmailChange.setUserEmailEntity(userEmailEntity.getId());
    userPendingEmailChange.setConfirmationHash(confirmationHash);
    userPendingEmailChange.setNewEmail(newEmail);
    
    getEntityManager().persist(userPendingEmailChange);
    
    return userPendingEmailChange;
  }
  
  public void delete(UserPendingEmailChange userPendingEmailChange) {
    super.delete(userPendingEmailChange);
  }

  public UserPendingEmailChange findByConfirmationHash(String confirmationHash) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<UserPendingEmailChange> criteria = criteriaBuilder.createQuery(UserPendingEmailChange.class);
    Root<UserPendingEmailChange> root = criteria.from(UserPendingEmailChange.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(UserPendingEmailChange_.confirmationHash), confirmationHash));
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  public UserPendingEmailChange findByUserEmailEntity(UserEmailEntity userEmailEntity) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<UserPendingEmailChange> criteria = criteriaBuilder.createQuery(UserPendingEmailChange.class);
    Root<UserPendingEmailChange> root = criteria.from(UserPendingEmailChange.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(UserPendingEmailChange_.userEmailEntity), userEmailEntity.getId()));
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
}
