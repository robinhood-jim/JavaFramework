<template>
    <PageWrapper>
        <!-- 搜索栏 -->
        <MoneyRR :money-crud="moneyCrud">
            <el-input v-model="moneyCrud.query.name" placeholder="用户名/昵称" class="md:!w-48"
                      @keyup.enter.native="moneyCrud.doQuery" />
            <el-input v-model.number="moneyCrud.query.phone" placeholder="手机号" class="md:!w-48"
                      @keyup.enter.native="moneyCrud.doQuery" />
            <el-select v-model="moneyCrud.query.status" clearable placeholder="状态" class="md:!w-48">
                <el-option v-for="item in [true, false]" :key="item" :label="item ? '启用':'禁用'" :value="item" />
            </el-select>
          <el-select v-model="moneyCrud.query.accountType" clearable placeholder="用户类型" class="md:!w-48">
            <el-option v-for="item in accountType" :key="item" :label="item.label" :value="item.value" />
          </el-select>
        </MoneyRR>
        <!-- 操作行 -->
        <MoneyCUD :money-crud="moneyCrud" />
        <!-- 数据表格 -->
        <MoneyCrudTable :money-crud="moneyCrud">
          <template #accountType="{scope}">
            <el-tag type="warning">{{ accountType.filter(e=>e.value==scope.row.accountType)[0].label}}</el-tag>
          </template>
            <template #status="{scope}">
                <el-switch v-model="scope.row.status" :disabled="moneyCrud.rowOptDisabled.checkbox(scope.row)" @change="changeEnabled(scope.row)" />
            </template>
            <template #opt="{scope}">
                <el-button plain type="primary" size="small" @click="toConfigPermission(scope.row)"
                           :disabled="scope.row.level <= userStore.level">
                  <el-icon>
                    <Setting />
                  </el-icon>
                </el-button>
                <MoneyUD :money-crud="moneyCrud" :scope="scope" />
            </template>
        </MoneyCrudTable>
        <!-- 表单 -->
        <MoneyForm :money-crud="moneyCrud" :rules="rules">
            <el-form-item label="用户名" prop="userAccount">
                <el-input v-model.trim="moneyCrud.form.userAccount" :disabled="moneyCrud.state === moneyCrud.STATE.EDIT" />
            </el-form-item>
          <el-form-item label="中文名" prop="userName">
            <el-input v-model.trim="moneyCrud.form.userName" />
          </el-form-item>
            <el-form-item label="昵称" prop="nickName">
                <el-input v-model.trim="moneyCrud.form.nickName" />
            </el-form-item>
            <el-form-item label="手机号" prop="phoneNum">
                <el-input v-model.number="moneyCrud.form.phoneNum" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model.trim="moneyCrud.form.email" />
            </el-form-item>
            <el-form-item label="状态">
                <el-radio-group v-model="moneyCrud.form.status">
                    <el-radio v-for="(item, index) in [true, false]" :key="index" :label="item">
                        {{ item ? '启用' : '禁用' }}
                    </el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="用户类型" prop="accountType">
                <el-select v-model="moneyCrud.form.accountType" :disabled="!userStore.isPlatformAdmin()" class="w-full" placeholder="请选择">
                    <el-option v-for="item in accountType" :key="item" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model.trim="moneyCrud.form.remark" type="textarea" maxlength="250" show-word-limit />
            </el-form-item>
            <el-form-item label="角色" prop="roles">
                <el-select v-model="moneyCrud.form.roles" class="w-full" multiple placeholder="请选择">
                    <el-option v-for="item in roles" :key="item" :label="item.roleName" :value="item.id" />
                </el-select>
            </el-form-item>
        </MoneyForm>
    </PageWrapper>
    <el-dialog v-model="configDialogVisible" title="配置权限" draggable class="!w-11/12 md:!w-1/2 lg:!w-1/3"
               @open="openDialog" destroy-on-close>
      <el-tree ref="permissionTree" :props="{ label: 'resName' }" :data="permissions"
               node-key="id" show-checkbox :render-after-expand="false" />
      <template #footer>
            <span class="dialog-footer">
              <el-button @click="configDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="loading" @click="confirmConfig">确认</el-button>
            </span>
      </template>
    </el-dialog>
</template>

<script setup>
import PageWrapper from "@/components/PageWrapper.vue";
import MoneyCrud from '@/components/crud/MoneyCrud.js';
import MoneyCrudTable from "@/components/crud/MoneyCrudTable.vue";
import MoneyRR from "@/components/crud/MoneyRR.vue";
import MoneyCUD from "@/components/crud/MoneyCUD.vue";
import MoneyUD from "@/components/crud/MoneyUD.vue";
import MoneyForm from "@/components/crud/MoneyForm.vue";

import {ref} from "vue";
import {useUserStore} from "@/store/index.js";
import {useGlobalProp} from "@/composables/globalProp.js";
import userApi from "@/api/system/user.js";
import roleApi from "@/api/system/role.js";
import permissionApi from "@/api/system/permission.js";

const globalProp = useGlobalProp()
const userStore = useUserStore()
const columns = [
    {prop: 'userAccount', label: '用户名'},
    {prop: 'userName', label: '用户中文名'},
    {prop: 'nickName', label: '昵称'},
    {prop: 'phoneNum', label: '手机号'},
    {prop: 'email', label: '邮箱', width: 150},
    {prop: 'status', label: '状态', align: 'center'},
    {prop: 'roles', label: '最高角色', align: 'center',show:false},
    {prop: 'accountType', label: '用户类型', width: 150},
    {prop: 'createTime', label: '创建时间', width: 180, show: false},
    {prop: 'updateTime', label: '修改时间', width: 180, show: false},
    {prop: 'remark', label: '备注', show: false},
    {
        prop: 'opt',
        label: '操作',
        width: 180,
        align: 'center',
        fixed: 'right',
        showOverflowTooltip: false,
        isMoneyUD: true
    }
]
const rules = {
    userAccount: [
        {required: true, message: '请输入用户名'},
        {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
    ],
    nickName: [
        {required: true, message: '请输入用户昵称'},
        {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
    ],
    email: [{type: 'email', message: '请输入正确的邮箱地址'}],
    phone: [
        {required: true, message: '请输入手机号'},
        {pattern: /^1([38][0-9]|4[014-9]|[59][0-35-9]|6[2567]|7[0-8])\d{8}$/, message: '格式错误'}
    ]
}
const moneyCrud = ref(new MoneyCrud({
    columns,
    crudMethod: userApi,
    defaultForm: {
        status: true
    },
    optShow: {
        checkbox: userStore.isAdmin(),
        add: userStore.isAdmin(),
        edit: userStore.isAdmin(),
        del: userStore.isAdmin()
    },
    rowOptDisabled: {
        checkbox: (row) => row.status,
        edit: (row) => !userStore.isAdmin(),
        del: (row) => !userStore.isAdmin()
    }
}))
const accountType=ref([])
const roles = ref([])
const permissionTree = ref()
const permissions = ref([])
const configDialogVisible = ref(false)
const selectedRole = ref({})
const loading = ref(false)
const selectedPermission=ref([])

moneyCrud.value.init(moneyCrud, async () => {
    roles.value = await roleApi.getAll().then(res => res.data)
    accountType.value=await userApi.getUserType('ACCOUNTTYPE').then(res=>res.data)
    permissions.value = await permissionApi.list().then(res => res.data)
})
moneyCrud.value.Hook.beforeToEdit = (form) => {
    form.roles = form.roles.map(e =>{
      return{ value: e.id, label:e.roleName}
    })
    //form.accountType=form.accountType.map(e=>e.value)
}
function toConfigPermission(row) {
  selectedRole.value = row
  configDialogVisible.value = true
}
async function openDialog() {
  selectedPermission.value=await permissionApi.userPermission(selectedRole.value.id).then(res=>{
    if(res.success){
      return res.data
    }else{
      globalProp.$message.error("用户无权限");
      return null;
    }
  })
  if(selectedPermission.value) {
    selectedPermission.value.ids.forEach(e => permissionTree.value.setChecked(e, true, false))
  }else{
    configDialogVisible.value = false
  }
}
function confirmConfig() {
  const checkedKeys = permissionTree.value.getCheckedKeys()
  loading.value = true
  userApi.configurePermissions(selectedRole.value.id, checkedKeys)
      .then(() => {
        moneyCrud.value.messageOk()
        loading.value = false
        configDialogVisible.value = false
      })
      .catch(() => loading.value = false)
}

/**
 * 修改状态
 * @param row
 */
function changeEnabled(row) {
    const {id, nickname, status} = row
    globalProp.$confirm(
        `确认${status ? '启用' : '禁用'}用户【${nickname}】?`,
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        userApi.edit({id, status})
            .then(() => moneyCrud.value.messageOk())
            .catch(() => row.status = !status)
    }).catch(() => row.status = !status)
}
</script>
