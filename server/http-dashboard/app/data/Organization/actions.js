import { API_COMMANDS } from "store/blynk-websocket-middleware/commands";

export function OrganizationPreloadFetch(data = {}) {
  if (!data.id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_PRELOAD_ORGANIZATION_FETCH',
    ws: {
      request: {
        command: API_COMMANDS.GET_ORG,
        query: [data.id]
      }
    }
  };
}

export function OrganizationSwitch(data = {}) {
  if (!data.orgId)
    throw Error('Organization id is not specified');
  return {
    type: 'TRACK_ORG',
    ws: {
      request: {
        command: API_COMMANDS.WEB_TRACK_ORG,
        query: [data.orgId]
      }
    }
  };
}

export function OrganizationFetch(data = {}) {
  if (!data.id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION',
    ws: {
      request: {
        command: API_COMMANDS.GET_ORG,
        query: [data.id]
      }
    }
  };
}

export function OrganizationSave(data = {}) {
  if (!data.id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION_SAVE',
    ws: {
      request: {
        command: API_COMMANDS.WEB_EDIT_OWN_ORG,
        query: [
          JSON.stringify(data)
        ],
      }
    }
  };
}

export function OrganizationUsersFetch(data) {
  if (!data.id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION_USERS',
    ws: {
      request: {
        command: API_COMMANDS.GET_ORG_USERS,
        query: [
          data.id,
        ],
      }
    }
  };
}

export function OrganizationSendInvite(data = {}) {
  if (!data.id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION_SEND_INVITE',
    ws: {
      request: {
        command: API_COMMANDS.INVITE_USER,
        query: [
          data.id,
          JSON.stringify(data)
        ],
      }
    }
  };
}

export function OrganizationLogoUpdate(logo) {
  return {
    type: 'ORGANIZATION_UPDATE_LOGO',
    logoUrl: logo
  };
}

export function OrganizationUpdateName(name) {
  return {
    type: 'ORGANIZATION_UPDATE_NAME',
    name: name
  };
}

export function OrganizationBrandingUpdate(colors) {
  return {
    type: 'ORGANIZATION_BRANDING_UPDATE',
    colors
  };
}

export function OrganizationUpdateTimezone(tzName) {
  return {
    type: 'ORGANIZATION_UPDATE_TIMEZONE',
    tzName: tzName
  };
}

export function OrganizationUpdateUser(id, data) {
  if (!id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION_UPDATE_USER',
    ws: {
      request: {
        command: API_COMMANDS.UPDATE_USER_INFO,
        query: [
          id,
          JSON.stringify(data),
        ],
      }
    }
  };
}

export function OrganizationUsersDelete(id, data) {
  if (!id)
    throw Error('Organization id is not specified');
  return {
    type: 'API_ORGANIZATION_USERS_DELETE',
    ws: {
      request: {
        command: API_COMMANDS.DELETE_USER,
        query: [
          id,
          JSON.stringify(data),
        ],
      }
    }
  };
}
