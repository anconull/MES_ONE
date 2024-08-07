<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="100px" size="small">
      <el-form-item label="数据源名称" prop="dataSourceName">
        <el-input
          v-model="queryParams.dataSourceName"
          clearable
          placeholder="请输入数据源名称"
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
          v-hasPermi="['integration:integratePlan:add']"
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
          v-hasPermi="['integration:integratePlan:edit']"
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
          v-hasPermi="['integration:integratePlan:remove']"
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
          v-hasPermi="['integration:integratePlan:export']"
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

    <el-table v-loading="loading" :data="integratePlanList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="编号" prop="number"/>
      <el-table-column align="center" label="名称" prop="name"/>
      <el-table-column align="center" label="数据源名称" prop="dataSourceName"/>
      <el-table-column align="center" label="源单名称" prop="souceBill"/>
      <el-table-column align="center" label="目标单据" prop="targetBill"/>
      <el-table-column align="center" label="状态" prop="status"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['integration:integratePlan:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['integration:integratePlan:remove']"
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

    <!-- 添加或修改集成方案对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="800px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="编号" prop="number">
              <el-input v-model="form.number" placeholder="请输入编号"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据源" prop="dataSourceId">
              <el-select v-model="form.dataSourceId" clearable filterable placeholder="请选择"
                         @change="dataSourceChange($event)">
                <el-option
                  v-for="item in dataSourceList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源单名称" prop="souceBill">
              <el-input v-model="form.souceBill" :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="目标单据" prop="targetBill">
              <el-select v-model="form.targetBill" placeholder="请选择"
                         @change="targetBillChange($event)">
                <el-option
                  v-for="dict in dict.type.basic_domain"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" type="textarea"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="center">集成方案字段信息</el-divider>
        <el-row :gutter="10" class="mb8">
        </el-row>
        <el-table ref="intePlanField" :data="intePlanFieldList"
                  :row-class-name="rowIntePlanFieldIndex" @selection-change="handleIntePlanFieldSelectionChange">
          <el-table-column align="center" type="selection" width="50"/>
          <el-table-column align="center" label="序号" prop="index" width="50"/>
          <el-table-column label="mes字段名称" prop="mesName" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.mesName" :disabled="true" placeholder="请输入mes字段名称"/>
            </template>
          </el-table-column>
          <el-table-column label="mes字段" prop="mesField" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.mesField" :disabled="true" placeholder="请输入mes字段"/>
            </template>
          </el-table-column>
          <el-table-column label="源单字段" prop="souceField">
            <template slot-scope="scope">
              <el-select v-model="scope.row.souceField" placeholder="请选择">
                <el-option
                  v-for="item in sourceFieldList"
                  :key="item.field"
                  :label="item.field"
                  :value="item.field"
                ></el-option>
              </el-select>
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
  addIntegratePlan,
  delIntegratePlan,
  getIntegratePlan,
  listIntegratePlan,
  mesField,
  updateIntegratePlan
} from "@/api/mes/integration/integratePlan";
import {getIntegrateDataSource, listIntegrateDataSource} from "@/api/mes/integration/integrateDataSource";

export default {
  name: "IntegratePlan",
  dicts: ['basic_domain'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedIntePlanField: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 集成方案表格数据
      integratePlanList: [],
      // 集成方案字段表格数据
      intePlanFieldList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        number: null,
        name: null,
        dataSourceId: null,
        dataSourceName: null,
        souceBill: null,
        targetBill: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {required: true, message: "名称不能为空", trigger: "blur"}
        ],
        dataSourceId: [
          {required: true, message: "数据源ID不能为空", trigger: "blur"}
        ],
        dataSourceName: [
          {required: true, message: "数据源名称不能为空", trigger: "blur"}
        ],
        souceBill: [
          {required: true, message: "源单名称不能为空", trigger: "blur"}
        ],
        targetBill: [
          {required: true, message: "目标单据不能为空", trigger: "blur"}
        ],
      },
      dataSourceList: [],
      sourceFieldList: []
    };
  },
  created() {
    this.getList();
    this.getDataSourceList();
  },
  methods: {
    /** 查询集成方案列表 */
    getList() {
      this.loading = true;
      listIntegratePlan(this.queryParams).then(response => {
        this.integratePlanList = response.rows;
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
        name: null,
        dataSourceId: null,
        dataSourceName: null,
        souceBill: null,
        targetBill: null,
        status: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      };
      this.intePlanFieldList = [];
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
      this.title = "添加集成方案";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getIntegrateDataSource(row.dataSourceId).then(res => {
        this.sourceFieldList = [];
        this.sourceFieldList = res.data.inteDataSourceFieldList

        console.log("this.sourceFieldList===>", this.sourceFieldList)
        let _this = this;
        getIntegratePlan(id).then(response => {
          _this.form = response.data;
          _this.intePlanFieldList = response.data.intePlanFieldList
          _this.open = true;
          _this.title = "修改集成方案";
        });
      })

    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.intePlanFieldList = this.intePlanFieldList;
          if (this.form.id != null) {
            updateIntegratePlan(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addIntegratePlan(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除集成方案编号为"' + ids + '"的数据项？').then(function () {
        return delIntegratePlan(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 集成方案字段序号 */
    rowIntePlanFieldIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
      row.sort = row.index;
    },
    /** 集成方案字段添加按钮操作 */
    handleAddIntePlanField() {
      let obj = {};
      obj.sort = "";
      obj.mesName = "";
      obj.mesField = "";
      obj.souceName = "";
      obj.souceField = "";
      obj.isQuery = "";
      obj.remark = "";
      obj.status = "";
      this.intePlanFieldList.push(obj);
    },
    /** 集成方案字段删除按钮操作 */
    handleDeleteIntePlanField() {
      if (this.checkedIntePlanField.length == 0) {
        this.$modal.msgError("请先选择要删除的集成方案字段数据");
      } else {
        const intePlanFieldList = this.intePlanFieldList;
        const checkedIntePlanField = this.checkedIntePlanField;
        this.intePlanFieldList = intePlanFieldList.filter(function (item) {
          return checkedIntePlanField.indexOf(item.index) == -1
        });
      }
    },
    /** 复选框选中数据 */
    handleIntePlanFieldSelectionChange(selection) {
      this.checkedIntePlanField = selection.map(item => item.index)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('integration/integratePlan/export', {
        ...this.queryParams
      }, `integratePlan_${new Date().getTime()}.xlsx`)
    },
    getDataSourceList() {
      listIntegrateDataSource({pageSize: 99999}).then(res => {
        this.dataSourceList = res.rows
      })
    },
    dataSourceChange(value) {
      let res = this.dataSourceList.find((item) => {
        return item.id === value;
      });
      this.form.souceBill = res.sourceBillName;
      getIntegrateDataSource(value).then(res => {
        this.sourceFieldList = [];
        this.sourceFieldList = res.data.inteDataSourceFieldList
      })
    },
    targetBillChange(value) {
      mesField({domainName: value}).then(res => {
        this.intePlanFieldList = res.data;
      })
    },

  }
};
</script>
