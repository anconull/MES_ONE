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
      <el-form-item label="仓库名称" prop="warehouseName">
        <el-input
          v-model="queryParams.warehouseName"
          clearable
          placeholder="请输入仓库名称"
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
          v-hasPermi="['warehouse:otherOut:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
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
      <!--          v-hasPermi="['warehouse:otherOut:edit']"-->
      <!--        >修改</el-button>-->
      <!--      </el-col>-->
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['warehouse:otherOut:remove']"
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
          v-hasPermi="['warehouse:otherOut:export']"
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

    <el-table v-loading="loading" :data="otherOutList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="编号" prop="otherOut.number"/>
      <el-table-column align="center" label="物料名称" prop="materialName"/>
      <el-table-column align="center" label="物料编号" prop="materialNumber"/>
      <el-table-column align="center" label="规格型号" prop="specification"/>
      <el-table-column align="center" label="单位" prop="materialUnitName"/>
      <el-table-column align="center" label="数量" prop="quantity"/>
      <el-table-column align="center" label="出库日期" prop="otherOut.outTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.otherOut.outTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="仓库" prop="otherOut.warehouseName"/>
      <el-table-column align="center" label="状态" prop="otherOut.status"/>
      <el-table-column align="center" label="备注" prop="otherOut.remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['warehouse:otherOut:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row.mainId)"
          >修改
          </el-button>
          <!--          <el-button-->
          <!--            size="mini"-->
          <!--            type="text"-->
          <!--            icon="el-icon-delete"-->
          <!--            @click="handleDelete(scope.row)"-->
          <!--            v-hasPermi="['warehouse:otherOut:remove']"-->
          <!--          >删除</el-button>-->
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
import {delOtherOut, listOtherOut} from "@/api/mes/warehouse/otherOut";

export default {
  name: "OtherOut",
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
      // 其他出库单主表表格数据
      otherOutList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        number: null,
        workshopId: null,
        workshopName: null,
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
        outTime: null,
        checker: null,
        checkDate: null,
        status: null,
        snArray: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        number: [
          {required: true, message: "编号不能为空", trigger: "blur"}
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
    /** 查询其他出库单主表列表 */
    getList() {
      this.loading = true;
      listOtherOut(this.queryParams).then(response => {
        this.otherOutList = response.rows;
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
        outTime: null,
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
      this.otherOutEntryList = [];
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
      this.$router.push({path: "/mes/otherOut-add/index"});
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({path: "/mes/otherOut-edit/index/" + row});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除其他出库单主表编号为"' + ids + '"的数据项？').then(function () {
        return delOtherOut(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('warehouse/otherOut/export', {
        ...this.queryParams
      }, `otherOut_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
