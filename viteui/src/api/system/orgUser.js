import req from '../index.js'

export default  {
    list: (query) => req({
        url: '/system/org/listUser',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/org',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/system/org',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/org',
        method: 'DELETE',
        data: ids,
    }),
    changeUser: (data) =>req({
        url:"/system/org/changeUser",
        method: 'POST',
        data
    })
}