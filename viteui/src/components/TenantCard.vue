<template>
  <el-row>
    <el-col :span="10" v-for="card in cardData" :key="card.id" >
      <el-card :body-style="{ padding: '10px' }" @click.native="handleClick(card.id)">
        <el-image
            class="w-16 h-16"
            preview-teleported
            :src="$money.getOssUrl(card.logo)"
            fit="cover"
        />
        <!--<img :src="card.logo" class="image">-->
        <div style="padding: 14px;">
          <span>{{ card.tenantName }}</span>
          <div v-if="card.orgName" class="bottom clearfix">
              <span>所属组织: {{ card.orgName }} </span>
          </div>
          <div v-if="card.orgCode" class="bottom clearfix">
            <span>租户编码: {{ card.tenantCode }} </span>
          </div>
          <div v-if="card.typeName" class="bottom clearfix">
            <span>用户角色: {{ card.typeName }} </span>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import {defineEmits} from 'vue'

const emit=defineEmits(['cardFunction'])
const {cardData} = defineProps({
  cardData: {
    required: true,
    type: Object
  }
})
function handleClick(id){
  emit('cardFunction',id)
}

</script>

<style scoped>


.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  width: 100%;
  display: block;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}
</style>