<template>
    <PageWrapper>
        <!-- 搜索栏 -->
        <MoneyRR :money-crud="moneyCrud">
            <el-input v-model.number="moneyCrud.query.condition" placeholder="名称/编码" class="md:!w-48"
                      @keyup.enter.native="moneyCrud.doQuery" />

            <el-tree-select v-model="moneyCrud.query.pid" :data="orgTree" class="!w-96"
                            value-key="id" :props="{ label: 'orgName' }" check-strictly
                            :render-after-expand="false" :expand-on-click-node="false" />

        </MoneyRR>
        <!-- 操作行 -->
        <MoneyCUD :money-crud="moneyCrud" />
        <!-- 数据表格 -->
        <MoneyCrudTable :money-crud="moneyCrud" row-key="id">
            <template #resType="{scope}">
                {{ dict.MENURESTYPEKv[scope.row.resType] }}
            </template>
            <template #icon="{scope}">
                <svg-icon v-if="scope.row.icon" :name="scope.row.icon" dir="open" />
            </template>
            <template #status="{scope}">
                <el-tag v-if="scope.row.status" type="info">是</el-tag>
                <el-tag v-else type="success">否</el-tag>
            </template>
            <template #opt="{scope}">
              <el-button plain type="primary" size="small" @click="toConfigPermission(scope.row,1)">
                <el-icon>
                  <DocumentDelete />
                </el-icon>
              </el-button>
              <el-button plain type="primary" size="small" @click="toConfigPermission(scope.row,2)">
                <el-icon>
                  <DocumentAdd />
                </el-icon>
              </el-button>
              <MoneyUD :money-crud="moneyCrud" :scope="scope" del-confirm-msg="确定删除该节点及其子节点?" />
            </template>
        </MoneyCrudTable>
        <!-- 表单 -->
        <MoneyForm :money-crud="moneyCrud" :inline="true" :rules="rulesMap" :dialog-class="'!w-11/12 md:!w-5/12'">

            <el-form-item label="机构名" prop="orgName" class="w-full">
                <el-input v-model="moneyCrud.form.orgName" />
            </el-form-item>
            <el-form-item label="机构标识" prop="orgCode" class="w-full">
              <el-input v-model="moneyCrud.form.orgCode" />
            </el-form-item>

            <el-form-item v-if="type === '3'" label="权限标识" prop="permission" class="w-full">
                <el-input v-model="moneyCrud.form.permission" placeholder="如：user:list" />
            </el-form-item>
            <el-form-item v-if="type !== '3'" label="路由地址" prop="routerPath" class="w-full">
                <el-input v-model="moneyCrud.form.routerPath" placeholder="开头不带 /">
                    <template #suffix>
                        <el-tooltip placement="top" content="等于上级路由地址 + 填写的地址">
                            <el-icon class="el-input__icon">
                                <Help />
                            </el-icon>
                        </el-tooltip>
                    </template>
                </el-input>
            </el-form-item>

            <el-form-item label="上级组织" prop="parentId" class="w-full">
                <el-tree-select v-model="moneyCrud.form.pid" :data="orgTree" class="!w-full"
                                value-key="id" :props="{ label: 'orgName' }" check-strictly
                                :render-after-expand="false" :expand-on-click-node="false" />
            </el-form-item>
            <el-form-item label="排序" prop="seqNo">
                <el-input-number v-model.number="moneyCrud.form.seqNo" :min="0" :max="999" controls-position="right" />
            </el-form-item>
            <el-form-item  label="隐藏" prop="status">
                <el-radio-group v-model="moneyCrud.form.status">
                    <el-radio-button v-for="(item, index) in [true, false]" :key="index" :label="item">
                        {{ item ? '是' : '否' }}
                    </el-radio-button>
                </el-radio-group>
            </el-form-item>
        </MoneyForm>
    </PageWrapper>
  <el-dialog v-model="configDialogVisible" :title="getUserTitle()" draggable :width="'850px'" class="!w-11/12 md:!w-3/5 lg:!w-3/5"
             @open="openDialog" destroy-on-close>

      <MoneyRR :money-crud="moneyCrud2">
        <el-form-item label="用户名" prop="name" class="w-48">

          <el-input v-model="moneyCrud2.query.name" />
        </el-form-item>
        <el-form-item label="电话号码" prop="contactPhone" class="w-48">
          <el-input v-model="moneyCrud2.query.contactPhone" />
        </el-form-item>

      </MoneyRR>
      <MoneyCUD :money-crud="moneyCrud2" >
        <el-button plain type="danger" :disabled="moneyCrud2.selections.length < 1"
                    @click="saveUser">{{ moneyCrud2.query.selType=='2'? '加入组织':'移出组织'}}
        </el-button>
      </MoneyCUD>
      <MoneyCrudTable :money-crud="moneyCrud2" row-key="id">
        <template #gender="{scope}">
          {{ dict.GENDERKv[scope.row.gender] }}
        </template>
        <template #district="{scope}">
          {{scope.row.province}}/{{scope.row.city}}/{{scope.row.district}}
        </template>
        <template #icon="{scope}">
          <svg-icon v-if="scope.row.icon" :name="scope.row.icon" dir="open" />
        </template>
        <template #status="{scope}">
          <el-tag v-if="scope.row.status" type="info">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </template>
        <template #opt="{scope}">
          <MoneyUD :money-crud="moneyCrud2" :scope="scope" del-confirm-msg="确定删除该节点及其子节点?" />
        </template>
      </MoneyCrudTable>
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
import orgApi from "@/api/system/org.js";
import orgUserApi from "@/api/system/orgUser.js";
import dictApi from "@/api/system/dict.js";
import {useUserStore} from "@/store/index.js";
import {Delete, DocumentDelete} from "@element-plus/icons-vue";

const userStore = useUserStore()
const columns = [
    {prop: 'orgName', label: '组织名称', width: 150},
    {prop: 'orgCode', label: '组织编码', width: 80},
    {prop: 'pid', label: '上级组织',show: false},
    {prop: 'orgAbbr', label: '机构简称'},
    {prop: 'status', label: '状态', align: "center",show:false},
    {prop: 'createTm', label: '创建时间', show: false},
    {
        prop: 'opt',
        label: '操作',
        width: 230,
        align: 'center',
        fixed: 'right',
        showOverflowTooltip: false,
        isMoneyUD: true
    }
]
const orgUserColumns=[
    {prop: 'name', label: '用户名称', width: 150},
    {prop: 'gender', label: '性别', width: 60},
    {prop: 'creditNo', label: '证件号码', width: 180},
    {prop: 'brithDay', label: '生日', width: 80},
    {prop: 'contactPhone', label: '电话号', width: 150},
    {prop: 'district', label: '所在地区', width: 180},
    {prop: 'address', label: '详细地址', width: 200},
    {
      prop: 'opt',
      label: '操作',
      width: 120,
      align: 'center',
      fixed: 'right',
      showOverflowTooltip: false,
      isMoneyUD: true
    }
]
const rulesMap = {

        orgName: [
            {required: true, message: '请输入标题'},
            {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
        ],
        orgCode: [
          {required: true, message: '请输入标题'},
          {min: 2, max: 20, message: '长度在 2 到 20 个字符'}
        ],
        pid: [{required: true, message: '请选择上级目录'}]

}
const moneyCrud = ref(new MoneyCrud({
    columns,
    isPage: false,
    crudMethod: orgApi,
    defaultForm: {
        icon: 'app',
        resType: '1',
        parentId: null,
        sort: 999,
        hidden: false,
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
const moneyCrud2=ref(new MoneyCrud({
    columns: orgUserColumns,
    isPage: true,
    crudMethod: orgUserApi,
    defaultForm: {
      icon: 'app',
      resType: '1',
      parentId: null,
      sort: 999,
      hidden: false,
    },
    optShow: {
      checkbox: true,
      add: false,
      edit: false,
      del: false
    }
}))
const dict = ref([])
const orgTree = ref([])
const type = ref('1')
const permissionsTree = ref([])
const userSearchType=ref('1')
const selectOrg=ref({})
const configDialogVisible = ref(false)
moneyCrud.value.init(moneyCrud, async () => {
    dict.value = await dictApi.loadDict(["MENURESTYPE","GENDER"])
    orgTree.value= await orgApi.list()
})

moneyCrud.value.Hook.beforeToAdd = () => {
    changeresType('1')
}
moneyCrud.value.Hook.beforeToEdit = (form) => {
    changeresType(form.resType)
}
function getUserTitle(){
  if(moneyCrud2.value.query.selType=='1'){
    return '组织内员工'
  }else{
    return '邀请进入组织'
  }
}
function toConfigPermission(row,type) {
  selectOrg.value = row
  userSearchType.value=type
  moneyCrud2.value.query.selType=type
  moneyCrud2.value.query.pid=row.id
  moneyCrud2.value.init(moneyCrud2,async ()=>{
    moneyCrud2.value.query.selType=type
  })
  configDialogVisible.value = true
}

function saveUser(){
  moneyCrud.$confirm(
      `确认 ${moneyCrud2.selections.length} 数据?`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data =  Object.assign({"type":userSearchType.value}, {"ids":moneyCrud2.selections})
    orgApi.changeUser(data)
  })
}
/**
 * 切换资源类型
 * @param value
 */
function changeresType(value) {
    type.value = value
    // 目录和菜单上级只能是目录，按钮上级只能是菜单
    const data = moneyCrud.value.data
    const disableType = ['1', '2'].includes(value) ? ['2', '3'] : ['1', '3']
    permissionsTree.value = flagDisabled([{
        id: 0,
        resName: '顶级类目',
        resType: '1',
        children: data
    }], disableType)
}
function searchUser(){

}

function flagDisabled(data, disableType) {
    if (!data || data.length < 1) return
    data.forEach(e => {
        e.disabled = disableType.includes(e.resType)
        flagDisabled(e.children, disableType)
    })
    return data
}
</script>