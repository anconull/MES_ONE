<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="编号" prop="number">
        <el-input
          v-model="queryParams.number"
          clearable
          placeholder="请输入编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          clearable
          placeholder="请输入客户名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库名称" prop="warehouseName">
        <el-input
          v-model="queryParams.warehouseName"
          clearable
          placeholder="请输入仓库名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="入库日期">
        <el-date-picker
          v-model="daterangeIntoDate"
          end-placeholder="结束日期"
          range-separator="-"
          start-placeholder="开始日期"
          style="width: 240px"
          type="daterange"
          value-format="yyyy-MM-dd"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.bill_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:saleReturn:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:saleReturn:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:saleReturn:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="saleReturnList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <div v-for="(item,index) in entryFieldExtendList">
        <el-table-column :label="item.fieldName" :width="item.width" align="center" show-overflow-tooltip='true'>
          <template slot-scope="scope">
            <!--  动态属性        -->
            <span v-if="item.type==='date'">
                  {{ parseTime(fieldFormat(scope.row, item), '{y}-{m}-{d}') }}
            </span>
            <span v-else-if="item.type.includes('dict:')">
                  <dict-tag :options="dict.type[dictFormat(item)]" :value="fieldFormat(scope.row,item)"/>
            </span>
            <span v-else-if="item.type==='annex'">
                   <el-link type="primary"
                            @click="fileDownload(scope.row)">{{
                       fieldFormat(scope.row, item) != null ? '下载' : ''
                     }}</el-link>
            </span>
            <span v-else>{{ fieldFormat(scope.row, item) }}</span>
          </template>
        </el-table-column>
      </div>
      <!--      <el-table-column label="物料名称" align="center" prop="materialName" />-->
      <!--      <el-table-column label="物料编号" align="center" prop="materialNumber" />-->
      <!--      <el-table-column label="规格型号" align="center" prop="specification" />-->
      <!--      <el-table-column label="单位" align="center" prop="materialUnitName" />-->
      <!--      <el-table-column label="数量" align="center" prop="quantity" />-->
      <!--      <el-table-column label="入库日期" align="center" prop="saleReturn.intoDate" width="180">-->
      <!--        <template slot-scope="scope">-->
      <!--          <span>{{ parseTime(scope.row.saleReturn.intoDate, '{y}-{m}-{d}') }}</span>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <!--      <el-table-column label="仓库" align="center" prop="saleReturn.warehouseName" />-->
      <!--      <el-table-column label="状态" align="center" prop="saleReturn.status" />-->
      <!--      <el-table-column label="备注" align="center" prop="saleReturn.remark" />-->

      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['warehouse:saleReturn:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row.mainId)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['warehouse:saleReturn:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

  </div>
</template>

<script>
import {delSaleReturn, listSaleReturn} from "@/api/mes/warehouse/saleReturn";
import {listFieldExtend} from "@/api/mes/system/fieldExtend";
import {dictFormat, fieldFormat} from "@/utils/jinzhong";

export default {
  name: "SaleReturn",
  dicts: ['bill_status'],
  data() {
    return {
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 销售退货单表格数据
      saleReturnList: [],
      // $comment时间范围
      daterangeIntoDate: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        number: null,
        customerName: null,
        warehouseName: null,
        intoDate: null,
        status: null,
      },
      entryFieldExtendList: {},
    };
  },
  created() {
    this.getFieldExtendList();
    this.getList();
  },
  methods: {
    /** 查询销售退货单列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeIntoDate && '' != this.daterangeIntoDate) {
        this.queryParams.params["beginIntoDate"] = this.daterangeIntoDate[0];
        this.queryParams.params["endIntoDate"] = this.daterangeIntoDate[1];
      }
      listSaleReturn(this.queryParams).then(response => {
        this.saleReturnList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.daterangeIntoDate = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.mainId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({path: "/mes/saleReturn-add/index"});
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({path: "/mes/saleReturn-edit/index/" + row});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除？').then(function () {
        return delSaleReturn(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('warehouse/saleReturn/export', {
        ...this.queryParams
      }, `saleReturn_${new Date().getTime()}.xlsx`)
    },
    getFieldExtendList() {
      //获取销售订单子表扩展字段结构
      listFieldExtend({pageSize: 99999, sourceBill: 'saleReturnEntry'}).then(response => {
        this.entryFieldExtendList = response.rows;
        this.loading = false;
      });
    },
    fieldFormat(row, item) {
      return fieldFormat(row, item);
    },
    dictFormat(item) {
      return dictFormat(item);
    }
  }
};
</script>
