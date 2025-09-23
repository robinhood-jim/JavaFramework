<template>
  <PageWrapper>
    <!-- 搜索栏 -->
    <MoneyRR :money-crud="moneyCrud">
      <el-input v-model="moneyCrud.query.name" placeholder="用户名/地址" class="md:!w-48"
                @keyup.enter.native="moneyCrud.doQuery"/>
      <el-input v-model.number="moneyCrud.query.phone" placeholder="手机号" class="md:!w-48"
                @keyup.enter.native="moneyCrud.doQuery"/>
      <el-select v-model="moneyCrud.query.status" clearable placeholder="状态" class="md:!w-48">
        <el-option v-for="item in [true, false]" :key="item" :label="item ? '启用':'禁用'" :value="item"/>
      </el-select>

    </MoneyRR>
    <!-- 操作行 -->
    <MoneyCUD :money-crud="moneyCrud"/>
    <!-- 数据表格 -->
    <MoneyCrudTable :money-crud="moneyCrud">
      <template #gender="{scope}">
        {{ dict.GENDERKv[scope.row.gender] }}
      </template>
      <template #accountType="{scope}">
        <el-tag type="warning">{{ accountType.filter(e => e.value == scope.row.accountType)[0].label }}</el-tag>
      </template>
      <template #status="{scope}">
        <el-switch v-model="scope.row.status" :disabled="moneyCrud.rowOptDisabled.checkbox(scope.row)"
                   @change="changeEnabled(scope.row)"/>
      </template>
      <template #district="{scope}">
        {{scope.row.province}}/{{scope.row.city}}/{{scope.row.district}}
      </template>
      <template #opt="{scope}">
        <MoneyUD :money-crud="moneyCrud" :scope="scope"/>
      </template>
    </MoneyCrudTable>
    <!-- 表单 -->
    <MoneyForm :money-crud="moneyCrud" :rules="rules">
      <el-form-item label="用户名" prop="name">
        <el-input v-model.trim="moneyCrud.form.name" />
      </el-form-item>
      <el-form-item label="证件号" prop="creditNo">
        <el-input v-model.trim="moneyCrud.form.creditNo"/>
      </el-form-item>
      <el-form-item label="生日" prop="creditNo">
        <el-date-picker  v-model.trim="moneyCrud.form.brithDay" value-format="YYYY-MM-DD" :clearable="false"/>
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="moneyCrud.form.gender" class="w-full" placeholder="请选择">
          <el-option v-for="item in genderType" :key="item" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="手机号" prop="contactPhone">
        <el-input v-model.number="moneyCrud.form.contactPhone"/>
      </el-form-item>
      <el-form-item label="地址">
        <PcdLinkage :province="moneyCrud.form.province" :city="moneyCrud.form.city"
                    :district="moneyCrud.form.district" @change="pcdChange" class="w-full"/>
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model.trim="moneyCrud.form.address" type="textarea" maxlength="250" show-word-limit/>
      </el-form-item>

    </MoneyForm>
  </PageWrapper>

</template>

<script setup>
import PageWrapper from "@/components/PageWrapper.vue";
import MoneyCrud from '@/components/crud/MoneyCrud.js';
import MoneyCrudTable from "@/components/crud/MoneyCrudTable.vue";
import MoneyRR from "@/components/crud/MoneyRR.vue";
import MoneyCUD from "@/components/crud/MoneyCUD.vue";
import MoneyUD from "@/components/crud/MoneyUD.vue";
import MoneyForm from "@/components/crud/MoneyForm.vue";
import PcdLinkage from "@/components/PcdLinkage.vue";

import {ref} from "vue";
import {useUserStore} from "@/store/index.js";
import {useGlobalProp} from "@/composables/globalProp.js";
import roleApi from "@/api/system/role.js";
import dictApi from "@/api/system/dict.js";
import employeeApi from "@/api/system/employee.js";

const globalProp = useGlobalProp()
const userStore = useUserStore()
const columns = [
  {prop: 'name', label: '用户名'},
  {prop: 'gender', label: '性别'},
  {prop: 'address', label: '详细地址'},
  {prop: 'brithDay', label: '生日'},
  {prop: 'district', label: '地区'},
  {prop: 'contactPhone', label: '手机号'},
  {prop: 'createTime', label: '创建时间', width: 180, show: false},
  {prop: 'updateTime', label: '修改时间', width: 180, show: false},
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
  name: [
    {required: true, message: '请输入用户名'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
  ],
  address: [
    {required: true, message: '请输入用户昵称'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
  ],
  phone: [
    {required: true, message: '请输入手机号'},
    {pattern: /^1([38][0-9]|4[014-9]|[59][0-35-9]|6[2567]|7[0-8])\d{8}$/, message: '格式错误'}
  ]
}
const moneyCrud = ref(new MoneyCrud({
  columns,
  crudMethod: employeeApi,
  defaultForm: {
    status: true,
    province: '43',
    city: '4301',
    district: '430101'
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

const roles = ref([])

const dict = ref([])


const genderType=ref([])
moneyCrud.value.init(moneyCrud, async () => {
  roles.value = await roleApi.getAll().then(res => res.data)
  dict.value = await dictApi.loadDict(["GENDER"])
  genderType.value=await dictApi.loadDictList("GENDER")
})
moneyCrud.value.Hook.beforeToEdit = (form) => {

}
function pcdChange(province, city, district) {
  moneyCrud.value.form.province = province
  moneyCrud.value.form.city = city
  moneyCrud.value.form.district = district
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
    employeeApi.edit({id, status})
        .then(() => moneyCrud.value.messageOk())
        .catch(() => row.status = !status)
  }).catch(() => row.status = !status)
}
</script>
