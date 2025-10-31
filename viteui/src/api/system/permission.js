import req from '../index.js'

export default {
    list: (query) => req({
        url: '/system/menu',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/menu',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/system/menu',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/menu',
        method: 'DELETE',
        data: ids,
    }),userPermission: (userId)=> req({
        url: 'system/menu/getByUser/'+userId,
        method: 'GET'
    })
}