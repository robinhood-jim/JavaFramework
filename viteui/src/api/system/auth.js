import req from '../index.js'

export default {
    getDyRoutes: () => req({
        url: '/getRouters',
        method: 'GET'
    }),
    login: (data) => req({
        url: '/login',
        method: 'POST',
        data
    }),
    getInfo: () => req({
        url: '/getInfo',
        method: 'GET'
    }),
    logout: () => req({
        url: '/logout',
        method: 'GET',
    }),
    getCodeImg: () =>req({
        url: '/captchaImage',
        headers: {
            isToken: false
        },
        method: 'get',
        timeout: 20000
    })
}