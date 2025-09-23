import req from '../index.js'

export default {
  loadProvinces: () => req({
    url: '/region/provinces',
    method: 'GET'
  }),
  loadCities: (province) => req({
    url: `/region/cities?province=${province}`,
    method: 'GET'
  }),
  loadDistricts: (city) => req({
    url: `/region/districts?city=${city}`,
    method: 'GET'
  }),
}

