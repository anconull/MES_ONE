<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="领料单号" prop="number">
        <el-input
          v-model="queryParams.number"
          clearable
          placeholder="请输入入库单编号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领料时间">
        <el-date-picker
          v-model="daterangePickTime"
          end-placeholder="结束日期"
          range-separator="-"
          start-placeholder="开始日期"
          style="width: 240px"
          type="daterange"
          value-format="yyyy-MM-dd"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="领料仓库" prop="warehouseName">
        <el-input
          v-model="queryParams.warehouseName"
          clearable
          placeholder="请输入仓库名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领料人" prop="pickUserName">
        <el-input
          v-model="queryParams.pickUserName"
          clearable
          placeholder="请输入领料人名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物料名称">
        <el-input
          v-model="queryParams.materialName"
          clearable
          placeholder="请输入物料名称"
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['warehouse:proPick:add']"
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
          :disabled="multiple"
          icon="el-icon-s-promotion"
          plain
          size="mini"
          style="color: #448B73;  background-color: #CEE8DF"
          @click="handleCheck"
        >审核
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:proPick:remove']"
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
          v-hasPermi="['warehouse:proPick:export']"
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

    <el-table v-loading="loading" :data="proPickList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="领料单号" prop="proPick.number" width="110"/>
      <el-table-column align="center" label="领料时间" prop="proPick.pickTime" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.proPick.pickTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="领料仓库" prop="proPick.warehouseName" width="80"/>
      <el-table-column align="center" label="领料人" prop="proPick.pickUserName" width="60"/>
      <el-table-column align="center" label="物料编号" prop="materialNumber" width="100"/>
      <el-table-column align="center" label="物料名称" prop="materialName" width="130"/>
      <el-table-column align="center" label="规格型号" prop="specification" width="150"/>
      <el-table-column align="center" label="数量" prop="quantity" width="85">
        <template slot-scope="scope">
          {{ scope.row.quantity }} ({{ scope.row.materialUnitName }})
        </template>
      </el-table-column>
      <el-table-column align="center" label="仓管" prop="proPick.keeperName" width="60"/>
      <el-table-column align="center" label="备注" prop="proPick.remark"/>
      <el-table-column align="center" label="状态" prop="proPick.status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.bill_status" :value="scope.row.proPick.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['warehouse:proPick:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row.mainId)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['warehouse:proPick:remove']"
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
import {addProPick, batchCheck, delProPick, listProPick, updateProPick} from "@/api/mes/warehouse/proPick";

export default {
  name: "ProPick",
  dicts: ['bill_status'],
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
      // 生产领料单表格数据
      proPickList: [],
      // 是否显示弹出层
      open: false,
      // 备注时间范围
      daterangePickTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        number: null,
        workshopName: null,
        stationName: null,
        warehouseName: null,
        pickUserName: null,
        pickTime: null,
        materialName: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        pickUserId: [
          {required: true, message: "领料人ID不能为空", trigger: "blur"}
        ],
        pickTime: [
          {required: true, message: "领料时间不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "change"}
        ],
        createBy: [
          {required: true, message: "创建人不能为空", trigger: "blur"}
        ],
        createTime: [
          {required: true, message: "创建时间不能为空", trigger: "blur"}
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询生产领料单列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      this.queryParams.params.materialName = this.queryParams.materialName;
      if (null != this.daterangePickTime && '' != this.daterangePickTime) {
        this.queryParams.params["beginPickTime"] = this.daterangePickTime[0];
        this.queryParams.params["endPickTime"] = this.daterangePickTime[1];
      }
      listProPick(this.queryParams).then(response => {
        this.proPickList = response.rows;
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
        workshopId: null,
        workshopName: null,
        stationId: null,
        stationName: null,
        workOrderEntryIds: null,
        warehouseId: null,
        warehouseNumber: null,
        warehouseName: null,
        regionId: null,
        regionNumber: null,
        regionName: null,
        areaId: null,
        areaNumber: null,
        areaName: null,
        pickUserId: null,
        pickUserName: null,
        keeperId: null,
        keeperName: null,
        pickTime: null,
        checker: null,
        checkDate: null,
        status: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      };
      this.proPickEntryList = [];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.daterangePickTime = [];
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
      this.$router.push({path: "/mes/proPick-add/index"});
    },
    /** 修改按钮操作 */
    handleUpdate(id) {
      this.$router.push({path: "/mes/proPick-edit/index/" + id});
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.proPickEntryList = this.proPickEntryList;
          if (this.form.id != null) {
            updateProPick(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProPick(this.form).then(response => {
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
      const ids = row.mainId || this.ids;
      this.$modal.confirm('是否确认删除生产领料单编号为"' + ids + '"的数据项？').then(function () {
        return delProPick(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('warehouse/proPick/export', {
        ...this.queryParams
      }, `生产领料${new Date().getTime()}.xlsx`)
    },
    handleCheck() {
      //只有状态为待审核B的单据才能审核
      for (let i = 0; i < this.ids.length; i++) {

        let proPick = this.proPickList.filter(item => item.mainId === this.ids[i]);

        console.log("proPick====>", proPick);
        if (proPick[0].proPick.status != "B") {
          this.$modal.msgWarning("只有状态为待审核B的单据才能审核!")
          return;
        }
      }
      batchCheck({ids: this.ids.join(","), checkStatus: 'A'}).then(res => {
        this.$modal.msgSuccess("审核成功" + res.data + "条");
      })
    }
  }
};
</script>
