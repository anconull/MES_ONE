<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="质检单号" prop="number">
        <el-input
          v-model="queryParams.number"
          clearable
          placeholder="请输入质检编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="报工单号" prop="reportNumber">
        <el-input
          v-model="queryParams.reportNumber"
          clearable
          placeholder="请输入报工单编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="工单号" prop="workOrderNumber">
        <el-input
          v-model="queryParams.workOrderNumber"
          clearable
          placeholder="请输入工单编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="工序" prop="processName">
        <el-input
          v-model="queryParams.processName"
          clearable
          placeholder="请输入工序"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品编码" prop="materialNumber">
        <el-input
          v-model="queryParams.materialNumber"
          clearable
          placeholder="请输入产品编码"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品名称" prop="materialName">
        <el-input
          v-model="queryParams.materialName"
          clearable
          placeholder="请输入产品名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="批次号" prop="batchNumber">
        <el-input
          v-model="queryParams.batchNumber"
          clearable
          placeholder="请输入批次号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质检类型" prop="qcType">
        <el-select v-model="queryParams.qcType" clearable placeholder="请选择质检类型">
          <el-option
            v-for="dict in dict.type.qc_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报工时间" prop="reportTime">
        <el-date-picker v-model="queryParams.reportTime"
                        clearable
                        placeholder="请选择报工时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="检测时间" prop="checkTime">
        <el-date-picker v-model="queryParams.checkTime"
                        clearable
                        placeholder="请选择检测时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="质检员" prop="checkUserName">
        <el-input
          v-model="queryParams.checkUserName"
          clearable
          placeholder="请输入质检员名称"
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
          plain
          size="mini"
          @click="getReport()"
        >上查工单
        </el-button>
      </el-col>
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--          type="success"-->
      <!--          plain-->
      <!--          icon="el-icon-edit"-->
      <!--          size="mini"-->
      <!--          :disabled="single"-->
      <!--          @click="handleUpdate"-->
      <!--          v-hasPermi="['quality:reportQuality:edit']"-->
      <!--        >修改</el-button>-->
      <!--      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['quality:reportQuality:remove']"
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
          v-hasPermi="['quality:reportQuality:export']"
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

    <el-table v-loading="loading" :data="reportQualityList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <!--      <el-table-column label="质检单号" align="center" prop="number" />-->
      <el-table-column align="center" label="报工单号" prop="reportNumber"/>
      <el-table-column align="center" label="工单号" prop="workOrderNumber" width="100"/>
      <el-table-column align="center" label="工序" prop="processName"/>
      <!--      <el-table-column label="产品编码" align="center" prop="materialNumber" />-->
      <el-table-column align="center" label="产品名称" prop="materialName"/>
      <el-table-column align="center" label="规格型号" prop="specification" width="150"/>
      <el-table-column align="center" label="单位" prop="materialUnitName"/>
      <!--      <el-table-column label="批次号" align="center" prop="batchNumber" />-->
      <el-table-column align="center" label="质检类型" prop="qcType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.qc_type" :value="scope.row.qcType"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="检测数量" prop="quantity"/>
      <el-table-column align="center" label="合格数" prop="qualifiedQuantity"/>
      <el-table-column align="center" label="不合格数" prop="unqualifiedQuantity"/>
      <el-table-column align="center" label="报工时间" prop="reportTime" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.reportTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="检测时间" prop="checkTime" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.checkTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="质检员" prop="checkUserName"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['production:report:edit']"
            size="mini"
            type="text"
            @click="qualityAddClick(scope.row)"
          >详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <reportQualityForm ref="reportQualityForm"></reportQualityForm>
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
import {
  addReportQuality,
  delReportQuality,
  getReportQuality,
  listReportQuality,
  updateReportQuality
} from "@/api/mes/quality/reportQuality";
import reportQualityForm from '@/views/mes/quality/reportQuality/form'

export default {
  name: "ReportQuality",
  dicts: ['qc_type', 'sys_normal_disable'],
  components: {reportQualityForm},
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
      // 报工质检单表格数据
      reportQualityList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        number: null,
        reportId: null,
        reportNumber: null,
        workOrderNumber: null,
        processName: null,
        materialNumber: null,
        materialName: null,
        specification: null,
        materialUnitName: null,
        batchNumber: null,
        qcType: null,
        quantity: null,
        qualifiedQuantity: null,
        unqualifiedQuantity: null,
        reportTime: null,
        checkTime: null,
        checkUserId: null,
        checkUserName: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        reportId: [
          {required: true, message: "报工单ID不能为空", trigger: "blur"}
        ],
        workOrderNumber: [
          {required: true, message: "工单编号不能为空", trigger: "blur"}
        ],
        processName: [
          {required: true, message: "工序不能为空", trigger: "blur"}
        ],
        materialNumber: [
          {required: true, message: "产品编码不能为空", trigger: "blur"}
        ],
        materialName: [
          {required: true, message: "产品名称不能为空", trigger: "blur"}
        ],
        materialUnitName: [
          {required: true, message: "单位不能为空", trigger: "blur"}
        ],
        qcType: [
          {required: true, message: "质检类型不能为空", trigger: "change"}
        ],
        quantity: [
          {required: true, message: "检测数量不能为空", trigger: "blur"}
        ],
        qualifiedQuantity: [
          {required: true, message: "合格数量不能为空", trigger: "blur"}
        ],
        unqualifiedQuantity: [
          {required: true, message: "不合格数量不能为空", trigger: "blur"}
        ],
        reportTime: [
          {required: true, message: "报工时间不能为空", trigger: "blur"}
        ],
        checkTime: [
          {required: true, message: "检测时间不能为空", trigger: "blur"}
        ],
        checkUserId: [
          {required: true, message: "质检员ID不能为空", trigger: "blur"}
        ],
        checkUserName: [
          {required: true, message: "质检员名称不能为空", trigger: "blur"}
        ],
        createBy: [
          {required: true, message: "创建人不能为空", trigger: "blur"}
        ],
        createTime: [
          {required: true, message: "创建时间不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "change"}
        ]
      }
    };
  },
  activated() {
    let workOrderNumber = this.$route.params.workOrderNumber;
    if (typeof (workOrderNumber) != "undefined") {
      this.queryParams.workOrderNumber = workOrderNumber
    }
    this.getList();
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询报工质检单列表 */
    getList() {
      this.loading = true;
      listReportQuality(this.queryParams).then(response => {
        this.reportQualityList = response.rows;
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
        reportId: null,
        reportNumber: null,
        workOrderNumber: null,
        processName: null,
        materialNumber: null,
        materialName: null,
        specification: null,
        materialUnitName: null,
        batchNumber: null,
        qcType: null,
        quantity: null,
        qualifiedQuantity: null,
        unqualifiedQuantity: null,
        reportTime: null,
        checkTime: null,
        checkUserId: null,
        checkUserName: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        status: null
      };
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
      this.title = "添加报工质检单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getReportQuality(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改报工质检单";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateReportQuality(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addReportQuality(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除报工质检单编号为"' + ids + '"的数据项？').then(function () {
        return delReportQuality(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('quality/reportQuality/export', {
        ...this.queryParams
      }, `reportQuality_${new Date().getTime()}.xlsx`)
    },
    //点击质检单新增按钮
    qualityAddClick(row) {
      this.$refs.reportQualityForm.qualityInfo(row.id);
    },
    //上查工单
    getReport() {
      //进行页面跳转
      let reportQuality = this.reportQualityList.filter(item => item.id === this.ids[0]);
      console.log("reportQuality===>", reportQuality)
      this.$router.push({name: "Report", params: {reportNumber: reportQuality[0].reportNumber}});
    }
  }
};
</script>
