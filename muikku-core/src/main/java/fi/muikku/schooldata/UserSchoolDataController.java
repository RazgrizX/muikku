package fi.muikku.schooldata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.muikku.dao.base.SchoolDataSourceDAO;
import fi.muikku.dao.users.RoleSchoolDataIdentifierDAO;
import fi.muikku.dao.users.UserEntityDAO;
import fi.muikku.dao.users.UserSchoolDataIdentifierDAO;
import fi.muikku.model.base.SchoolDataSource;
import fi.muikku.model.users.UserEntity;
import fi.muikku.model.users.UserSchoolDataIdentifier;
import fi.muikku.schooldata.entity.Role;
import fi.muikku.schooldata.entity.User;
import fi.muikku.schooldata.entity.UserEmail;

@Dependent
@Stateful
class UserSchoolDataController { 
	
	// TODO: Caching 
	// TODO: Events
	
	@Inject
	private Logger logger;
	
	@Inject
	@Any
	private Instance<UserSchoolDataBridge> userBridges;
	
	@Inject
	@Any
	private Instance<SchoolDataEntityInitiator<User>> userInitiators;

	@Inject
	@Any
	private Instance<SchoolDataEntityInitiator<UserEmail>> userEmailInitiators;

	@Inject
	@Any
	private Instance<SchoolDataEntityInitiator<Role>> roleInitiators;
	
	@Inject
	private UserSchoolDataIdentifierDAO userSchoolDataIdentifierDAO;

	@Inject
	private SchoolDataSourceDAO schoolDataSourceDAO;
	
	@Inject
	private UserEntityDAO userEntityDAO;
	
	@Inject
  private RoleSchoolDataIdentifierDAO roleSchoolDataIdentifierDAO;

	/* User */

	public User findUser(SchoolDataSource schoolDataSource, UserEntity userEntity) {
		UserSchoolDataBridge userBridge = getUserBridge(schoolDataSource);
		if (userBridge != null) {
  		UserSchoolDataIdentifier schoolDataIdentifier = userSchoolDataIdentifierDAO.findByDataSourceAndUserEntity(schoolDataSource, userEntity);
	  	if (schoolDataIdentifier != null) {
	  		return initUser(findUserByIdentifier(userBridge, schoolDataIdentifier.getIdentifier()));
		  }
		}
		
		return null;
	}
	
	public User findUser(SchoolDataSource schoolDataSource, String userIdentifier) {
		UserSchoolDataBridge schoolDataBridge = getUserBridge(schoolDataSource);
		if (schoolDataBridge != null) {
			try {
				return initUser(schoolDataBridge.findUser(userIdentifier));
			} catch (SchoolDataBridgeRequestException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while find a user", e);
			} catch (UnexpectedSchoolDataBridgeException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while find a user", e);
			}
		}
		
		return null;
	}
	
	public User findUser(String schoolDataSource, String userIdentifier) {
		SchoolDataSource dataSource = schoolDataSourceDAO.findByIdentifier(schoolDataSource);
		if (dataSource != null) {
			return findUser(dataSource, userIdentifier);
		}
		
		return null;
	}
	
	public List<User> listUsers() {
	  // TODO: This method WILL cause performance problems, replace with something more sensible 
		
		List<User> result = new ArrayList<User>();
		
		for (UserSchoolDataBridge userBridge : getUserBridges()) {
			try {
				result.addAll(userBridge.listUsers());
			} catch (UnexpectedSchoolDataBridgeException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while listing users", e);
			}
		}
		
		return initUsers(result);
	}
	
	public List<User> listUsersByEntity(UserEntity userEntity) {
		List<User> result = new ArrayList<>();
		
		List<UserSchoolDataIdentifier> identifiers = userSchoolDataIdentifierDAO.listByUserEntity(userEntity);
		for (UserSchoolDataIdentifier identifier : identifiers) {
			User user = findUser(identifier.getDataSource(), userEntity);
			if (user != null) {
				result.add(user);
			}
		}

		return result;
	}
	
	public List<User> listUsersByEmail(String email) {
		List<User> result = new ArrayList<User>();
		
		for (UserSchoolDataBridge userBridge : getUserBridges()) {
			try {
				User user = userBridge.findUserByEmail(email);
				if (user != null) {
				  result.add(user);
				}
			} catch (SchoolDataBridgeRequestException e) {
				logger.log(Level.SEVERE, "SchoolDataBridge reported an error while listing users by email", e);
			} catch (UnexpectedSchoolDataBridgeException e) {
				logger.log(Level.SEVERE, "SchoolDataBridge reported an error while listing users by email", e);
			}
		}
		
		return initUsers(result);
	}
	
	/* User Entity */
	
	public UserEntity findUserEntity(User user) {
		SchoolDataSource schoolDataSource = schoolDataSourceDAO.findByIdentifier(user.getSchoolDataSource());
		UserSchoolDataIdentifier userSchoolDataIdentifier = userSchoolDataIdentifierDAO.findByDataSourceAndIdentifier(schoolDataSource, user.getIdentifier());
		if (userSchoolDataIdentifier != null) {
			return userSchoolDataIdentifier.getUserEntity();
		}
		
		return null;
	}
	
	/* User Emails */

	public List<UserEmail> listUserEmails(User user) {
		SchoolDataSource schoolDataSource = schoolDataSourceDAO.findByIdentifier(user.getSchoolDataSource());
		if (schoolDataSource != null) {
			UserSchoolDataBridge schoolDataBridge = getUserBridge(schoolDataSource);
			if (schoolDataBridge != null) {
				try {
					return initUserEmails(schoolDataBridge.listUserEmailsByUserIdentifier(user.getIdentifier()));
				} catch (SchoolDataBridgeRequestException | UnexpectedSchoolDataBridgeException e) {
					logger.log(Level.SEVERE, "SchoolDataBridge reported an error while listing user emails", e);
				}
			}
		}
		
		return null;
	}
	
	/* Roles*/

	public Role findRole(SchoolDataSource schoolDataSource, String identifier) {
		UserSchoolDataBridge userBridge = getUserBridge(schoolDataSource);
		if (userBridge != null) {
			try {
				return userBridge.findRole(identifier);
			} catch (SchoolDataBridgeRequestException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while finding a role", e);
			} catch (UnexpectedSchoolDataBridgeException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while finding a role", e);
			}
		} else {
		  logger.severe("Could not find userBridge for school data source " + schoolDataSource.getIdentifier());
		}
		
		return null;
	}
	
	public List<Role> listRoles() {
		List<Role> result = new ArrayList<>();
		
		for (UserSchoolDataBridge userBridge : getUserBridges()) {
			try {
				result.addAll(userBridge.listRoles());
			} catch (UnexpectedSchoolDataBridgeException e) {
				logger.log(Level.SEVERE, "School Data Bridge reported a problem while listing users", e);
			}
		}
		
		return initRoles(result);
	}

	public Role findUserEnvironmentRole(User user) {
		SchoolDataSource schoolDataSource = schoolDataSourceDAO.findByIdentifier(user.getSchoolDataSource());
		if (schoolDataSource != null) {
			UserSchoolDataBridge schoolDataBridge = getUserBridge(schoolDataSource);
			if (schoolDataBridge != null) {
				try {
					return initRole(schoolDataBridge.findUserEnvironmentRole(user.getIdentifier()));
				} catch (SchoolDataBridgeRequestException | UnexpectedSchoolDataBridgeException e) {
					logger.log(Level.SEVERE, "SchoolDataBridge reported an error while listing user emails", e);
				}
			}
		}
		
		return null;
	}
	
	private UserSchoolDataBridge getUserBridge(SchoolDataSource schoolDataSource) {
		Iterator<UserSchoolDataBridge> iterator = userBridges.iterator();
		while (iterator.hasNext()) {
			UserSchoolDataBridge userSchoolDataBridge = iterator.next();
			if (userSchoolDataBridge.getSchoolDataSource().equals(schoolDataSource.getIdentifier())) {
				return userSchoolDataBridge;
			}
		}
		
		return null;
	}
	
	private User findUserByIdentifier(UserSchoolDataBridge userBridge, String identifier) {
		try {
			return userBridge.findUser(identifier);
		} catch (SchoolDataBridgeRequestException e) {
			logger.log(Level.SEVERE, "SchoolDataBridge reported an error while finding user", e);
		} catch (UnexpectedSchoolDataBridgeException e) {
			logger.log(Level.SEVERE, "SchoolDataBridge reported an error while finding user", e);
		}
		
		return null;
	}

	private List<UserSchoolDataBridge> getUserBridges() {
		List<UserSchoolDataBridge> result = new ArrayList<UserSchoolDataBridge>();
		
		Iterator<UserSchoolDataBridge> iterator = userBridges.iterator();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		
		return Collections.unmodifiableList(result);
	}
	
	/* Initiators */

	private User initUser(User user) {
		if (user == null) {
			return null;
		}
		
		Iterator<SchoolDataEntityInitiator<User>> initiatorIterator = userInitiators.iterator();
		while (initiatorIterator.hasNext()) {
			user = initiatorIterator.next().single(user);
		}
		
		return user;
	};
	
	private List<User> initUsers(List<User> users) {
		if (users == null) {
			return null;
		}
		
		if (users.size() == 0) {
			return users;
		}
		
		Iterator<SchoolDataEntityInitiator<User>> initiatorIterator = userInitiators.iterator();
		while (initiatorIterator.hasNext()) {
			users = initiatorIterator.next().list(users);
		}
		
		return users;
	};
	
	private List<UserEmail> initUserEmails(List<UserEmail> userEmails) {
		if (userEmails == null) {
			return null;
		}
		
		if (userEmails.size() == 0) {
			return userEmails;
		}
		
		Iterator<SchoolDataEntityInitiator<UserEmail>> initiatorIterator = userEmailInitiators.iterator();
		while (initiatorIterator.hasNext()) {
			userEmails = initiatorIterator.next().list(userEmails);
		}
		
		return userEmails;
	}

	private Role initRole(Role role) {
		if (role == null) {
			return null;
		}
		
		Iterator<SchoolDataEntityInitiator<Role>> initiatorIterator = roleInitiators.iterator();
		while (initiatorIterator.hasNext()) {
			role = initiatorIterator.next().single(role);
		}
		
		return role;
	}

	private List<Role> initRoles(List<Role> roles) {
		if (roles == null) {
			return null;
		}
		
		if (roles.size() == 0) {
			return roles;
		}
		
		Iterator<SchoolDataEntityInitiator<Role>> initiatorIterator = roleInitiators.iterator();
		while (initiatorIterator.hasNext()) {
			roles = initiatorIterator.next().list(roles);
		}
		
		return roles;
	}
}
