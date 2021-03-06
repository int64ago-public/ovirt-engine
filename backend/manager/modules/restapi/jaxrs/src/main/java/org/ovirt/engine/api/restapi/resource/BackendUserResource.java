package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendUsersResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.UserResource;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * This resource corresponds to an user that has been added to the engine and
 * stored in the database.
 */
public class BackendUserResource
        extends AbstractBackendSubResource<User, DbUser>
        implements UserResource {

    private BackendUsersResource parent;

    public BackendUserResource(String id, BackendUsersResource parent) {
        super(id, User.class, DbUser.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    public void setParent(BackendUsersResource parent) {
        this.parent = parent;
    }

    public BackendUsersResource getParent() {
        return parent;
    }

    @Override
    public User get() {
        return performGet(VdcQueryType.GetDbUserByUserId, new IdQueryParameters(guid), BaseResource.class);
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        return inject(new BackendAssignedRolesResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendUserTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsByAdElementId,
                                                             new IdQueryParameters(guid),
                                                             User.class));
    }

    @Override
    protected User doPopulate(User model, DbUser entity) {
        return model;
    }

    public User getUserByName(String name) {
        return performGet(VdcQueryType.GetDbUserByUserName, new NameQueryParameters(name), BaseResource.class);
    }
}
