<template>
    <div class="mx-auto max-w-screen-xl px-4 py-24 sm:px-6 lg:px-8" @keydown.enter="login">
        <el-card class="mx-auto max-w-lg">
            <el-form ref="loginFormRef" :model="loginForm" :rules="rules"
                     class="mb-0 mt-2 rounded-lg p-4 sm:p-6 lg:p-8">
                <h1 class="text-center text-2xl font-bold text-indigo-600 sm:text-3xl mb-10">
                    {{title}}
                </h1>

                <div>
                    <label for="username" class="sr-only">username</label>
                    <el-form-item prop="username">
                        <el-input v-model="loginForm.username" size="large" placeholder="输入用户名" />
                    </el-form-item>
                </div>

                <div>
                    <label for="password" class="sr-only">Password</label>
                    <el-form-item prop="password">
                        <el-input v-model="loginForm.password" size="large" type="password" show-password
                                  placeholder="输入密码" />
                    </el-form-item>
                </div>
              <div>
                <el-form-item prop="code">
                  <el-input
                      v-model="loginForm.code"
                      auto-complete="off"
                      placeholder="验证码"
                      style="width: 63%"
                  >
                    <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
                  </el-input>
                  <div class="login-code">
                    <img id="kaptus" @click="getKaptus"  class="login-code-img"/>
                  </div>
                </el-form-item>
              </div>


                <el-button :loading="loading" @click="login" size="large"
                           class="w-full !bg-indigo-600 !text-white !rounded-lg mt-3">
                    登录
                </el-button>
            </el-form>
            <div class="flex justify-center gap-6" v-if="noProd">
                <el-tag class="cursor-pointer !p-2" type="danger" @click="() => {loginForm.username = 'money';loginForm.password= '123'}">超级管理员</el-tag>
                <el-tag class="cursor-pointer !p-2" type="success" @click="() => {loginForm.username = 'admin';loginForm.password= '123456'}">管理员</el-tag>
                <el-tag class="cursor-pointer !p-2" @click="() => {loginForm.username = 'guest';loginForm.password= '123456'}">游客</el-tag>
            </div>
        </el-card>
        <el-dialog v-model="configDialogVisible" title="选择所在的租户" resizable draggable class="!w-11/12 md:!w-1/2 lg:!w-1/3"
                   @open="openDialog" destroy-on-close>
          <TenantCard :cardData="tenantInfos" @cardFunction="selectTenantCard" />
        </el-dialog>
    </div>
</template>
<script setup>
const noProd = import.meta.env.MODE !== 'production'
const title = document.title
import {ref,onMounted } from "vue"
import {useUserStore} from '@/store'
import {useRoute, useRouter} from 'vue-router'
import userApi from "@/api/system/user.js";
import {setToken} from "@/composables/token.js"
import TenantCard from "@/components/TenantCard.vue";

const configDialogVisible = ref(false)
const $router = useRouter();
const redirect = useRoute().query.redirect
const userStore = useUserStore()
const loginFormRef = ref()
const loginForm = ref({
    username: 'admin',
    password: '123456',
    code: "",
    uuid: ""
})
const selectTenantForm=ref({
  id:''
})
const tenantInfos=ref([])
const  codeContent=ref({
  codeUrl:''
})
const rules = {
    username: [{required: true, trigger: 'change'}],
    password: [{required: true, trigger: 'change'}],
    code: [{required: true, trigger: 'change'}],
    id: [{required: true, trigger: 'change'}]
}
const loading = ref(false)
onMounted(()=>{
  getCode();
})


function getCode(){
   userStore.getCode().then(res=>{
    var url = "data:image/gif;base64," + res.img;
    loginForm.uuid = res.uuid;
    document.getElementById('kaptus').src=url
  })

}
async function selectTenantCard(id){
  loading.value = true
  await userStore.selectTenant(id).then(res=>{
      loading.value = false
      configDialogVisible.value = false;
      $router.push({path: redirect || '/'})
  }).catch(() => loading.value = false)
}


async function login(evt) {
    evt.preventDefault()
    await loginFormRef.value.validate((valid) => {
        if (!valid) return
        loading.value = true
        userStore.login(loginForm.value)
            .then((data) =>{
              console.log(data)
              if(data.success) {
                if (data.selectTenant) {
                  tenantInfos.value = data.tenants;
                  loading.value = false
                  configDialogVisible.value = true;
                } else {
                  $router.push({path: redirect || '/'})
                }
              }else{
                loading.value = false
              }
            })
            .catch(() => loading.value = false)
    })
}


</script>
<style>
.login-code-img {
  height: 40px;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
</style>