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
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" clearable placeholder="请选择类型">
          <el-option
            v-for="dict in dict.type.profit_loss_record_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--          type="primary"-->
      <!--          plain-->
      <!--          icon="el-icon-plus"-->
      <!--          size="mini"-->
      <!--          @click="handleAdd"-->
      <!--          v-hasPermi="['warehouse:profitLossRecord:add']"-->
      <!--        >新增</el-button>-->
      <!--      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:profitLossRecord:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:profitLossRecord:remove']"
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
          v-hasPermi="['warehouse:profitLossRecord:export']"
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

    <el-table v-loading="loading" :data="profitLossRecordList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="编号" prop="number"/>
      <el-table-column align="center" label="名称" prop="name"/>
      <el-table-column align="center" label="仓库名称" prop="warehouseName"/>
      <el-table-column align="center" label="仓管名称" prop="keeperName"/>
      <el-table-column align="center" label="记录时间" prop="recordTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.recordTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="类型" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.profit_loss_record_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.bill_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['warehouse:profitLossRecord:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['warehouse:profitLossRecord:remove']"
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

    <!-- 添加或修改盘点盈亏记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="1300px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="6">
            <el-form-item label="编号" prop="number">
              <el-input v-model="form.number" :disabled="true" placeholder="请输入编号"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" :disabled="true" placeholder="请输入名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记录时间" prop="recordTime">
              <el-date-picker v-model="form.recordTime"
                              :disabled="true"
                              clearable
                              placeholder="请选择记录时间"
                              type="date" value-format="yyyy-MM-dd">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="true" placeholder="请选择状态">
                <el-option
                  v-for="dict in dict.type.bill_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" :disabled="true" clearable filterable
                         placeholder="请选择仓库" @change="warehouseSelect">
                <el-option
                  v-for="item in warehouseList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="仓管员" prop="keeperId">
              <el-select v-model="form.keeperId" :disabled="true" clearable filterable placeholder="请选择仓管员"
                         @change="userChange">
                <el-option
                  v-for="item in userList"
                  :key="item.userId"
                  :label="item.nickName"
                  :value="item.userId"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" :disabled="true" placeholder="请选择类型">
                <el-option
                  v-for="dict in dict.type.profit_loss_record_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">盘点盈亏记录明细信息</el-divider>
        <el-table ref="profitLossRecordEntry" :data="profitLossRecordEntryList"
                  :row-class-name="rowProfitLossRecordEntryIndex"
                  @selection-change="handleProfitLossRecordEntrySelectionChange">
          <el-table-column label="物料编号" prop="materialNumber" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.materialNumber" :disabled="true"/>
            </template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.materialName" :disabled="true"/>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specification" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.specification" :disabled="true"/>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="materialUnitName" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.materialUnitName" :disabled="true"/>
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="inventoryQuantity" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.quantity" placeholder="请输入库存数量"/>
            </template>
          </el-table-column>

        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addProfitLossRecord,
  delProfitLossRecord,
  getProfitLossRecord,
  listProfitLossRecord,
  updateProfitLossRecord
} from "@/api/mes/warehouse/profitLossRecord";
import {listUser} from "@/api/system/user";
import {listWarehouse} from '@/api/mes/warehouse/warehouse'

export default {
  name: "ProfitLossRecord",
  dicts: ['profit_loss_record_type', 'take_inventory_status', 'bill_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedProfitLossRecordEntry: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 盘点盈亏记录表格数据
      profitLossRecordList: [],
      // 盘点盈亏记录明细表格数据
      profitLossRecordEntryList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        name: null,
        type: null,
        number: null,
        warehouseId: null,
        keeperId: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        type: [
          {required: true, message: "类型不能为空", trigger: "change"}
        ],
        takeInventoryId: [
          {required: true, message: "盘点表ID不能为空", trigger: "blur"}
        ],
        warehouseId: [
          {required: true, message: "仓库ID不能为空", trigger: "change"}
        ],
        keeperId: [
          {required: true, message: "仓管员ID不能为空", trigger: "change"}
        ],
        recordTime: [
          {required: true, message: "记录时间不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "change"}
        ],
      },
      warehouseList: [],
    };
  },
  created() {
    this.getUserList();
    this.getWarehouseList();
    this.getList();
  },
  methods: {
    /** 查询盘点盈亏记录列表 */
    getList() {
      this.loading = true;
      listProfitLossRecord(this.queryParams).then(response => {
        this.profitLossRecordList = response.rows;
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
        name: null,
        type: null,
        takeInventoryId: null,
        number: null,
        warehouseId: null,
        warehouseNumber: null,
        warehouseName: null,
        regionId: null,
        regionNumber: null,
        regionName: null,
        areaId: null,
        areaNumber: null,
        areaName: null,
        keeperId: null,
        keeperName: null,
        recordTime: null,
        checker: null,
        checkDate: null,
        status: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        snArray: null
      };
      this.profitLossRecordEntryList = [];
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
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加盘点盈亏记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProfitLossRecord(id).then(response => {
        this.form = response.data;
        this.profitLossRecordEntryList = response.data.profitLossRecordEntryList;
        this.open = true;
        this.title = "修改盘点盈亏记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.profitLossRecordEntryList = this.profitLossRecordEntryList;
          if (this.form.id != null) {
            updateProfitLossRecord(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProfitLossRecord(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除盘点盈亏记录编号为"' + ids + '"的数据项？').then(function () {
        return delProfitLossRecord(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 盘点盈亏记录明细序号 */
    rowProfitLossRecordEntryIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
    },
    /** 盘点盈亏记录明细添加按钮操作 */
    handleAddProfitLossRecordEntry() {
      let obj = {};
      obj.sort = "";
      obj.materialNumber = "";
      obj.materialName = "";
      obj.specification = "";
      obj.materialUnitName = "";
      obj.quantity = "";
      obj.warehouseNumber = "";
      obj.warehouseName = "";
      this.profitLossRecordEntryList.push(obj);
    },
    /** 盘点盈亏记录明细删除按钮操作 */
    handleDeleteProfitLossRecordEntry() {
      if (this.checkedProfitLossRecordEntry.length == 0) {
        this.$modal.msgError("请先选择要删除的盘点盈亏记录明细数据");
      } else {
        const profitLossRecordEntryList = this.profitLossRecordEntryList;
        const checkedProfitLossRecordEntry = this.checkedProfitLossRecordEntry;
        this.profitLossRecordEntryList = profitLossRecordEntryList.filter(function (item) {
          return checkedProfitLossRecordEntry.indexOf(item.index) == -1
        });
      }
    },
    /** 复选框选中数据 */
    handleProfitLossRecordEntrySelectionChange(selection) {
      this.checkedProfitLossRecordEntry = selection.map(item => item.index)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('warehouse/profitLossRecord/export', {
        ...this.queryParams
      }, `profitLossRecord_${new Date().getTime()}.xlsx`)
    },
    getWarehouseList() {
      let qp = {pageSize: 99999};
      listWarehouse(qp).then(response => {
        this.warehouseList = response.rows;
      });
    },
    warehouseSelect(id) {
      let warehouse = [];
      warehouse = this.warehouseList.filter(function (item) {
        return item.id === id
      });
      this.form.warehouseId = warehouse[0].id;
      this.form.warehouseNumber = warehouse[0].number;
      this.form.warehouseName = warehouse[0].name;
    },
    getUserList() {
      let qp = {pageSize: 99999};
      listUser(this.addDateRange(qp, [])).then(response => {
          this.userList = response.rows;
        }
      );
    },
    userChange(value) {
      let opt = {};
      opt = this.userList.find((item) => {
        return item.userId === value;
      });
      this.form.keeperName = opt.nickName;
    },
  }
};
</script>
