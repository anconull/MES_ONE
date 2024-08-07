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
      <el-form-item label="工序名" prop="name">
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入工序名"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
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
          v-hasPermi="['production:process:add']"
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
          v-hasPermi="['production:process:edit']"
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
          v-hasPermi="['production:process:remove']"
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
          v-hasPermi="['production:process:export']"
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

    <el-table v-loading="loading" :data="processList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="编号" prop="number"/>
      <el-table-column align="center" label="工序名" prop="name"/>
      <el-table-column align="center" label="注意事项" prop="attention"/>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['production:process:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['production:process:remove']"
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

    <!-- 添加或修改工序对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="550px">
      <el-form ref="form" :model="form" :rules="rules" class="jz-compact-form" label-width="100px">
        <el-form-item label="编号" prop="number">
          <el-input v-model="form.number" placeholder="请输入编号"/>
        </el-form-item>
        <el-form-item label="工序名" prop="name">
          <el-input v-model="form.name" placeholder="请输入工序名"/>
        </el-form-item>
        <el-form-item label="作业指导书" prop="guidance">
          <el-upload
            :action="upload.url"
            :file-list="upload.fileList"
            :headers="upload.headers"
            :on-preview="onPreview"
            :on-remove="uploadRemove"
            :on-success="uploadSucess"
            class="upload-demo"
            drag
            multiple>
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将附件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__text">请上传小于10M的附件</div>
            <div slot="tip" class="el-upload__tip">点击文件名即可下载</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="注意事项" prop="attention">
          <el-input v-model="form.attention" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <!--        <el-form-item label="领料审核">-->
        <!--          <el-radio v-model="form.pickChek" label="y">是</el-radio>-->
        <!--          <el-radio v-model="form.pickChek" label="N">否</el-radio>-->
        <!--        </el-form-item>-->
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addProcess, delProcess, getProcess, listProcess, updateProcess} from "@/api/mes/production/process";
import {getToken} from "@/utils/auth";

export default {
  name: "Process",
  dicts: ['sys_normal_disable'],
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
      // 工序表格数据
      processList: [],
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
        guidance: null,
        attention: null,
        status: '0',
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {required: true, message: "工序名不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "blur"}
        ],
        createBy: [
          {required: true, message: "创建人不能为空", trigger: "blur"}
        ],
        createTime: [
          {required: true, message: "创建时间不能为空", trigger: "blur"}
        ],
      },
      // 上传参数
      upload: {
        headers: {Authorization: "Bearer " + getToken()},
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/common/upload",
        fileList: [],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询工序列表 */
    getList() {
      this.loading = true;
      this.upload.fileList = [];
      listProcess(this.queryParams).then(response => {
        this.processList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.upload.fileList = [];
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        number: null,
        name: null,
        guidance: null,
        attention: null,
        status: "0",
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        pickChek: 'N'
      };
      this.upload.fileList = [];
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
      this.title = "添加工序";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProcess(id).then(response => {
        let fileTmp = [];
        let fileInfoList = response.data.sysFileInfoList;
        if (fileInfoList != undefined) {
          for (let i = 0; i < fileInfoList.length; i++) {
            fileTmp.push({'url': fileInfoList[i].filePath, 'name': fileInfoList[i].fileName})
          }
        }
        this.upload.fileList = fileTmp;
        this.form = response.data;
        this.open = true;
        this.title = "修改工序";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateProcess(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProcess(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除工序编号为"' + ids + '"的数据项？').then(function () {
        return delProcess(ids);
      }).then(response => {
        this.getList();
        this.$modal.msgSuccess("删除成功" + response.data + "条");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('production/process/export', {
        ...this.queryParams
      }, `process_${new Date().getTime()}.xlsx`)
    },
    uploadSucess(file, fileList) {

      if (this.form.sysFileInfoList == undefined) {
        this.form.sysFileInfoList = [];
      }
      this.form.sysFileInfoList.push(file);
    },
    uploadRemove(file, fileList) {
      let list = this.form.sysFileInfoList;
      for (let i = 0; i < list.length; i++) {
        let fileTmp = list[i];
        if (fileTmp.fileName === file.name) {
          this.$delete(list, i);
        }
      }
      console.log(this.form.sysFileInfoList)
    },
    onPreview(file) {
      this.$modal.confirm('是否确认下载：' + file.name).then(function () {
        let url = process.env.VUE_APP_BASE_API + file.url;
        const a = document.createElement('a')
        a.setAttribute('download', file.name)
        a.setAttribute('target', '_blank')
        a.setAttribute('href', url)
        a.click()
      }).then(() => {
        //下载成功
      }).catch(() => {
        //下载失败
      });
    }

  }
};
</script>
