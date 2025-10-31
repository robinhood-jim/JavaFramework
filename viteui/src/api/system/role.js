import req from '../index.js'

export default {
    list: (query) => req({
        url: '/system/role',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/role',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/roles',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/role',
        method: 'DELETE',
        data: ids,
    }),
    getAll: () => req({
        url: '/system/role/all',
        method: 'GET'
    }),
    configurePermissions: (id, permissionIds) => req({
        url: `/system/role/${id}/permission`,
        method: 'POST',
        data: permissionIds
    })
}