<template>
  <div class="app-container">
    <!--    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">-->
    <!--      <el-form-item label="物料名称" prop="materialName">-->
    <!--        <el-input-->
    <!--          v-model="queryParams.materialName"-->
    <!--          placeholder="请输入物料名称"-->
    <!--          clearable-->
    <!--          @keyup.enter.native="handleQuery"-->
    <!--        />-->
    <!--      </el-form-item>-->
    <!--      <el-form-item>-->
    <!--        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>-->
    <!--        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>-->
    <!--      </el-form-item>-->
    <!--    </el-form>-->

    <!--    <el-row :gutter="10" class="mb8">-->
    <!--      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>-->
    <!--    </el-row>-->
    <div v-for="(item,index) in processlist">
      <el-card class="box-card" style="margin: 10px">
        <div slot="header" class="clearfix">
          <span>{{ item.name }}</span>
        </div>
        <el-table :data="item.entryList" class="tableBox">
          <el-table-column align="center" label="状态" prop="status" width="50"/>
          <el-table-column :show-overflow-tooltip="true" align="center" label="客户" prop="customerName" width="250"/>
          <el-table-column align="center" label="物料" prop="material" width="120"/>
          <!--    天润定制部分功能begin          -->
          <el-table-column v-if="item.name!=='吹膜'" align="center" label="印刷版" prop="extendField.banxing"
                           width="120"/>
          <el-table-column v-if="item.name.trim()==='涂胶'||item.name.trim()==='分切'" align="center" label="粘度"
                           prop="extendField.viscosity"
                           width="120"/>
          <!--    天润定制部分功能end          -->
          <el-table-column align="center" label="规格型号" prop="specification" width="200"/>
          <el-table-column align="center" label="排产数" prop="planQuantity" width="120"/>
          <el-table-column align="center" label="报工数" prop="reportQuantity" width="120"/>
          <el-table-column align="center" label="进度" prop="schedule"/>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import {queryProductionExecutionByProcess} from "@/api/mes/statement/productionReport";
import {listProcess} from "@/api/mes/production/process";

export default {
  name: "ProcessExecution",
  data() {
    return {
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 工序
      processlist: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
      },

    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询即时库存列表 */
    getList() {
      listProcess({pageSize: 99999, status: '0'}).then(response => {
        let processlist = []
        processlist = response.rows;
        console.log(response)
        console.log(processlist)
        this.processlist = [];
        for (let i = 0; i < processlist.length; i++) {
          let process = processlist[i];
          queryProductionExecutionByProcess({processName: process.name}).then(res => {
            process.entryList = res;
            this.processlist.push(process);
            this.processlist.sort((a, b) => a.id - b.id)
          })
        }
        this.$forceUpdate();
        console.log("this.processlist====>", this.processlist)
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
  }
};
</script>
<style lang="scss">
.tableBox {
  th {
    padding: 0 !important;
    height: 20px;
    line-height: 20px;
  }

  td {
    padding: 0 !important;
    height: 20px;
    line-height: 20px;
  }
}

</style>
