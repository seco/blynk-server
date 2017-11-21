const Roles = {
  'SUPER_ADMIN': {
    value: 'SUPER_ADMIN',
    title: 'Super Admin'
  },
  'ADMIN': {
    value: 'ADMIN',
    title: 'Admin'
  },
  'STAFF': {
    value: 'STAFF',
    title: 'Staff'
  },
  'USER': {
    value: 'USER',
    title: 'User'
  }
};

const AvailableRoles = {
  'ADMIN': {
    key: 'ADMIN',
    value: 'Admin'
  },
  'STAFF': {
    key: 'STAFF',
    value: 'Staff'
  },
  'USER': {
    key: 'USER',
    value: 'User'
  }
};

const InviteAvailableRoles = [
  AvailableRoles.ADMIN,
  AvailableRoles.STAFF,
  AvailableRoles.USER
];

const UsersAvailableRoles = [
  Roles.ADMIN,
  Roles.STAFF,
  Roles.USER
];

const MetadataRoles = [
  {
    key: Roles.ADMIN.value,
    value: Roles.ADMIN.title
  },
  {
    key: Roles.STAFF.value,
    value: Roles.STAFF.title
  },
  {
    key: Roles.USER.value,
    value: Roles.USER.title
  }
];

const MetadataRolesDefault = MetadataRoles[0].key;

export {
  MetadataRoles,
  MetadataRolesDefault,
  Roles,
  InviteAvailableRoles,
  UsersAvailableRoles
};

export const isUserAbleToEdit = (userRole, fieldRole) => {
  // roles should be in ascending order
  const permissions = ['USER','STAFF','ADMIN','SUPER_ADMIN'];

  return permissions.slice(0, permissions.indexOf(userRole)).indexOf(fieldRole) !== -1;
};
