import req from '../index.js'

export default {
    list: (query) => req({
        url: '/system/employee',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/employee',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/system/employee',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/employee',
        method: 'DELETE',
        data: ids,
    })
}