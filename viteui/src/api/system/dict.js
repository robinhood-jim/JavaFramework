import req from '../index.js'

export default {
    list: (query) => req({
        url: '/system/dict',
        method: 'GET',
        params: query,
    }),
    add: (data) => req({
        url: '/system/dict',
        method: 'POST',
        data,
    }),
    edit: (data) => req({
        url: '/system/dict',
        method: 'PUT',
        data,
    }),
    del: (ids) => req({
        url: '/system/dict',
        method: 'DELETE',
        data: ids,
    }),
    getDetail: (dict) => req({
        url: `/system/dict/code/${dict}`,
        method: 'GET'
    }),
    addDetail: (data) => req({
        url: '/system/dict/detail',
        method: 'POST',
        data,
    }),
    editDetail: (data) => req({
        url: '/system/dict/detail',
        method: 'PUT',
        data,
    }),
    delDetail: (ids) => req({
        url: '/system/dict/detail',
        method: 'DELETE',
        data: ids,
    }),
    loadDictList :async (dictCode)=>{
        const {data} = await req({
            url: `/system/code/${dictCode}`,
            method: 'GET'
        })
        return data;
    },

    loadDict: async (dictList = []) => {
        const dictMap = {}
        for (const dict of dictList) {
            const {data} = await req({
                url: `/system/code/${dict}`,
                method: 'GET'
            })
            if (data && data.length > 0) {
                dictMap[dict] = data.map(dd => {
                    // TODO 多语言 先默认中文
                    dd.desc = dd.label
                    return {
                        desc: dd.desc,
                        value: dd.value,
                    }
                })
                dictMap[dict + 'Kv'] = {}
                data.map(dd => dictMap[dict + 'Kv'][dd.value] = dd.desc)
            }
        }
        return dictMap
    }
}