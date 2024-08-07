<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="类型名称" prop="typeName">
        <el-input
          v-model="queryParams.typeName"
          clearable
          placeholder="请输入类型名称"
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
          v-hasPermi="['barcode:barcodeRecord:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >条码生成
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['barcode:barcodeRecord:remove']"
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
          v-hasPermi="['barcode:barcodeRecord:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['barcode:barcodeRecord:export']"
          :disabled="single"
          plain
          size="mini"
          type="success"
          @click="printBarcode"
        >打印
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="barcodeRecordList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="条码类型" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.basic_domain" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="类型编号" prop="typeNumber"/>
      <el-table-column align="center" label="类型名称" prop="typeName"/>
      <el-table-column align="center" label="生成数量" prop="quantity"/>
      <el-table-column align="center" label="条码区间" prop="interval"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['barcode:barcodeRecord:remove']"
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

    <!-- 添加或修改条码生成记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="条码格式" prop="format">
          <el-select v-model="form.format" placeholder="请选择条码格式">
            <el-option
              v-for="dict in dict.type.barcode_format"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="条码类型" prop="type">
          <el-select v-model="form.type" :disabled="true" placeholder="请选择条码类型">
            <el-option
              v-for="dict in dict.type.basic_domain"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="materialName">
          <el-input v-model="form.typeName" placeholder="请选择类型">
            <el-button slot="append" icon="el-icon-search" @click="handleSelectProduct"></el-button>
          </el-input>
          <MaterialSelect ref="materialSelect" @onMaterialSelect="onMaterialSelect"></MaterialSelect>
        </el-form-item>
        <el-form-item label="类型编号" prop="typeNumber">
          <el-input v-model="form.typeNumber" :disabled="true" placeholder="请输入类型编号"/>
        </el-form-item>
        <el-form-item label="生成数量" prop="quantity">
          <el-input-number v-model="form.quantity" :max="99999" :min="1" label="请输入生成数量"></el-input-number>
        </el-form-item>
        <el-form-item label="参数" prop="parameter">
          <el-input v-model="form.parameter" placeholder="请输入内容如 { 'batchNumber': '批次号' }" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">生 成</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addBarcodeRecord,
  delBarcodeRecord,
  getBarcodeRecord,
  listBarcodeRecord,
  updateBarcodeRecord
} from "@/api/mes/barcode/barcodeRecord";
import MaterialSelect from '@/components/mes/materialSelect'

export default {
  name: "BarcodeRecord",
  dicts: ['basic_domain', 'barcode_format'],
  components: {MaterialSelect},
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
      // 条码生成记录表格数据
      barcodeRecordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        typeName: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        type: [
          {required: true, message: "条码类型不能为空", trigger: "change"}
        ],
        typeId: [
          {required: true, message: "类型ID不能为空", trigger: "blur"}
        ],
        typeName: [
          {required: true, message: "类型名称不能为空", trigger: "blur"}
        ],
        codeRuleId: [
          {required: true, message: "编码规则ID不能为空", trigger: "change"}
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询条码生成记录列表 */
    getList() {
      this.loading = true;
      listBarcodeRecord(this.queryParams).then(response => {
        this.barcodeRecordList = response.rows;
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
        format: 'A',
        type: 'materialSN',
        typeId: null,
        typeNumber: null,
        typeName: null,
        codeRuleId: null,
        quantity: null,
        interval: null,
        parameter: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
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
      this.title = "添加条码生成记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getBarcodeRecord(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改条码生成记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateBarcodeRecord(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addBarcodeRecord(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除条码生成记录编号为"' + ids + '"的数据项？').then(function () {
        return delBarcodeRecord(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('barcode/barcodeRecord/export', {
        ...this.queryParams
      }, `barcodeRecord_${new Date().getTime()}.xlsx`)
    },
    //物料选择弹出框
    handleSelectProduct() {
      this.$refs.materialSelect.showFlag = true;
    },//接收物料弹框返回参数
    onMaterialSelect(obj) {
      if (obj != undefined && obj != null) {
        this.form.typeId = obj.id;
        this.form.typeName = obj.name;
        this.form.typeNumber = obj.number;
        this.$refs["form"].validate();
      }
    },
    printBarcode() {
      const id = this.ids
      window.open("http://127.0.0.1:8080/jmreport/view/808241198613966848?pageSize=10&mainId=" + id, '_blank')
    }
  }
};
</script>
