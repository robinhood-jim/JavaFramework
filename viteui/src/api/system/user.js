import req from '../index.js'

export default {
    list: (query) => req({
        url: '/system/user',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/user',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/system/user',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/user',
        method: 'DELETE',
        data: ids,
    }),
    updateInfo: (data) => req({
        url: '/sytem/user/updateProfile',
        method: 'POST',
        data,
    }),
    changePassword: (data) => req({
        url: '/system/user/changePassword',
        method: 'POST',
        data,
    }),
    getUserType: (data) =>req({
        url: '/system/code/'+data,
        method: 'GET'
    }),
    configurePermissions: (id, permissionIds) => req({
        url: `/system/user/${id}/permission/`,
        method: 'POST',
        data: permissionIds
    }),
    selectTenant: (tenantId) =>  req({
        url: `/selectTenant/`+tenantId,
        method: 'GET',
    })
}