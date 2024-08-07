<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="90px" size="small">
      <el-form-item label="入库单编号" prop="number">
        <el-input
          v-model="queryParams.number"
          clearable
          placeholder="请输入入库单编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="queryParams.warehouseId" clearable filterable placeholder="请选择仓库">
          <el-option
            v-for="item in warehouseList"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="入库日期" prop="intoDate">
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
      <el-form-item label="物料名称">
        <el-input
          v-model="queryParams.params.materialName"
          clearable
          placeholder="请输入物料名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商">
        <el-input
          v-model="queryParams.supplierName"
          clearable
          placeholder="请输入"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="规格型号">
        <el-input
          v-model="queryParams.params.specification"
          clearable
          placeholder="请输入"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:procureInto:add']"
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
          v-hasPermi="['warehouse:procureInto:remove']"
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
          v-hasPermi="['warehouse:procureInto:export']"
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

    <el-table v-loading="loading" :data="procureIntoList" class="tableBox" height="630px"
              @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="入库单编号" prop="procureInto.number" width="110"/>
      <el-table-column align="center" label="入库日期" prop="procureInto.intoDate" width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.procureInto.intoDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="仓库" prop="procureInto.warehouseName" width="75"/>
      <el-table-column align="center" label="物料名称" prop="materialName" width="150"/>
      <el-table-column align="center" label="物料编号" prop="materialNumber" width="80"/>
      <el-table-column align="center" label="规格型号" prop="specification"/>
      <el-table-column align="center" label="数量" prop="quantity" width="85">
        <template slot-scope="scope">
          {{ scope.row.quantity }} ({{ scope.row.materialUnitName }})
        </template>
      </el-table-column>
      <el-table-column align="center" label="单价" prop="price" width="75"/>
      <el-table-column align="center" label="金额" prop="totalPrice" width="75"/>
      <!--      <el-table-column label="状态" align="center" prop="procureInto.status" />-->
      <el-table-column align="center" label="供应商" prop="procureInto.supplierName" width="190"/>
      <el-table-column align="center" label="备注" prop="procureInto.remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['warehouse:procureInto:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row.mainId)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['warehouse:procureInto:remove']"
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
import {addProcureInto, delProcureInto, listProcureInto, updateProcureInto} from "@/api/mes/warehouse/procureInto";
import {listWarehouse} from "@/api/mes/warehouse/warehouse";

export default {
  name: "ProcureInto",
  data() {
    return {
      // 遮罩层
      loading: true,
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
      // 采购入库单表格数据
      procureIntoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 更新时间时间范围
      daterangeIntoDate: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        number: null,
        supplierId: null,
        supplierName: null,
        method: null,
        warehouseNumber: null,
        warehouseName: null,
        intoDate: null,
        status: null,
        keeper: null,
        checker: null,
        checkDate: null,
        params: {
          materialName: null,
          supplierName: null,
          specification: null
        }
      },
      // 表单参数
      form: {},
      warehouseList: [],
    };
  },
  created() {
    this.getWarehouseList();
    this.getList();
  },
  methods: {
    /** 获取仓库数据*/
    getWarehouseList() {
      let qp = {pageSize: 99999};
      listWarehouse(qp).then(response => {
        this.warehouseList = response.rows;
      });
    },
    /** 查询采购入库单列表 */
    getList() {
      this.loading = true;
      if (null != this.daterangeIntoDate && '' != this.daterangeIntoDate) {
        this.queryParams.params["beginIntoDate"] = this.daterangeIntoDate[0];
        this.queryParams.params["endIntoDate"] = this.daterangeIntoDate[1];
      } else {
        delete this.queryParams.params["beginIntoDate"];
        delete this.queryParams.params["endIntoDate"];
      }
      listProcureInto(this.queryParams).then(response => {
        this.procureIntoList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        number: null,
        supplierId: null,
        supplierName: null,
        amount: null,
        price: null,
        method: null,
        warehouseId: null,
        warehouseNumber: null,
        warehouseName: null,
        regionId: null,
        regionNumber: null,
        regionName: null,
        areaId: null,
        areaNumber: null,
        areaName: null,
        intoDate: null,
        status: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        keeper: null,
        checker: null,
        checkDate: null,
        snArray: null
      };
      this.procureIntoEntryList = [];
      this.resetForm("form");
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.mainId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({path: "/mes/procureInto-add/index"});
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      const id = row
      console.log("id====>", id)
      this.$router.push({path: "/mes/procureInto-edit/index/" + id});
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.procureIntoEntryList = this.procureIntoEntryList;
          if (this.form.id != null) {
            updateProcureInto(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProcureInto(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除采购入库单编号为"' + ids + '"的数据项？').then(function () {
        return delProcureInto(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 采购入库单子表序号 */
    rowProcureIntoEntryIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
    },
    /** 采购入库单子表添加按钮操作 */
    handleAddProcureIntoEntry() {
      let obj = {};
      obj.sort = "";
      obj.materialId = "";
      obj.materialNumber = "";
      obj.materialName = "";
      obj.specification = "";
      obj.materialUnitId = "";
      obj.materialUnitName = "";
      obj.quantity = "";
      obj.batchNumber = "";
      obj.warehouseId = "";
      obj.warehouseNumber = "";
      obj.warehouseName = "";
      obj.regionId = "";
      obj.regionNumber = "";
      obj.regionName = "";
      obj.areaId = "";
      obj.areaNumber = "";
      obj.areaName = "";
      obj.manufactureDate = "";
      obj.remark = "";
      this.procureIntoEntryList.push(obj);
    },
    /** 采购入库单子表删除按钮操作 */
    handleDeleteProcureIntoEntry() {
      if (this.checkedProcureIntoEntry.length == 0) {
        this.$modal.msgError("请先选择要删除的采购入库单子表数据");
      } else {
        const procureIntoEntryList = this.procureIntoEntryList;
        const checkedProcureIntoEntry = this.checkedProcureIntoEntry;
        this.procureIntoEntryList = procureIntoEntryList.filter(function (item) {
          return checkedProcureIntoEntry.indexOf(item.index) == -1
        });
      }
    },
    /** 复选框选中数据 */
    handleProcureIntoEntrySelectionChange(selection) {
      this.checkedProcureIntoEntry = selection.map(item => item.index)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('warehouse/procureInto/export', {
        ...this.queryParams
      }, `采购入库${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style lang="scss">
.tableBox {
  th {
    padding: 0 !important;
    height: 15px;
    line-height: 15px;
  }

  td {
    padding: 0 !important;
    height: 15px;
    line-height: 15px;
  }
}
</style>
