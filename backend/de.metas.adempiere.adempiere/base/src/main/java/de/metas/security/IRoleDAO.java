package de.metas.security;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.metas.adempiere.model.I_AD_Role;
import de.metas.user.UserId;
import de.metas.util.ISingletonService;

import javax.annotation.Nullable;

/**
 * {@link I_AD_Role} related DAO.
 * 
 * @author tsa
 *
 */
public interface IRoleDAO extends ISingletonService
{
	Role getById(RoleId roleId);

	Set<RoleId> getUserRoleIds(UserId userId);

	List<Role> getUserRoles(UserId adUserId);

	Set<RoleId> getSubstituteRoleIds(UserId adUserId, LocalDate date);

	List<RoleInclude> retrieveRoleIncludes(RoleId adRoleId);

	IRolesTreeNode retrieveRolesTree(RoleId adRoleId, UserId substituteForUserId, LocalDate substituteDate);

	/**
	 * @return all roles (from all clients) which were configured to be automatically maintained
	 */
	Collection<Role> retrieveAllRolesWithAutoMaintenance();

	/**
	 * Convenient method to retrieve the role's name.
	 */
	String getRoleName(RoleId adRoleId);

	Set<UserId> retrieveUserIdsForRoleId(RoleId adRoleId);

	@Nullable
	RoleId retrieveFirstRoleIdForUserId(UserId adUserId);

	void createUserRoleAssignmentIfMissing(UserId adUserId, RoleId adRoleId);
}
