const initialState = {
  roles: [],
  usersPerRole: {},
  currentRole: {}
};

export default function RolesAndPermissions(state = initialState, action) {
  switch (action.type) {
    case "WEB_GET_ROLES_SUCCESS":
      return {
        ...state,
        roles: (Object.values(action.payload.data).map(
          ({
             id,
             name,
             permissionGroup1,
             permissionGroup2
           }) => {

            return {
              id,
              name,
              permissionGroup1,
              permissionGroup2,
              permissionsGroup1Binary: getBinaryValueFromNumber(permissionGroup1),
              permissionsGroup2Binary: getBinaryValueFromNumber(permissionGroup2)
            };
          }))
        // Skip super admin!!! This role has id of 0
          .filter(role => role.id)
      };

    case "WEB_UPDATE_ROLE":
      const { id, permissionGroup1 } = JSON.parse(action.ws.request.query);

      return {
        ...state,
        roles: [...state.roles].map((role) => {
          return role.id === id ? {
            ...role,
            permissionGroup1: permissionGroup1,
            permissionsGroup1Binary: getBinaryValueFromNumber(permissionGroup1),
          } : role;
        })
      };
    case "WEB_GET_ROLE_SUCCESS":
      return {
        ...state,
        currentRole: action.payload.data
      };
    case "WEB_GET_USER_COUNTERS_BY_ROLE_SUCCESS":
      return {
        ...state,
        usersPerRole: action.payload.data
      };
    default:
      return state;
  }
}


function getBinaryValueFromNumber(number) {
  let value = (number >>> 0).toString(2);

  for (let i = 0; value.length < 32; i++) {
    value = `0${value}`;
  }

  return value;
}
