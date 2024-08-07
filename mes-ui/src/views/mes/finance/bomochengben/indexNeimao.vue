<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="96px" size="small">
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="核算方式" prop="priceType">
        <el-select v-model="queryParams.priceType" :clearable="true">
          <el-option
            v-for="item in priceTypeList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
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
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--          type="warning"-->
      <!--          plain-->
      <!--          icon="el-icon-download"-->
      <!--          size="mini"-->
      <!--          @click="handleExport"-->
      <!--          v-hasPermi="['finance:bomochengben:export']"-->
      <!--        >导出</el-button>-->
      <!--      </el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bomochengbenList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="名称" prop="name" width="125"/>
      <el-table-column align="center" label="核算方式" prop="priceType"/>
      <!--      <el-table-column label="颜色" align="center" prop="yanse" />-->
      <el-table-column align="center" label="类型" prop="type"/>
      <el-table-column align="center" label="吹膜厚度" prop="chuimohd"/>
      <el-table-column align="center" label="宽度" prop="kd"/>
      <el-table-column align="center" label="米数" prop="mishu"/>
      <!--      <el-table-column label="吹膜速度" align="center" prop="chuimosd" />-->
      <!--      <el-table-column label="原料价格" align="center" prop="yuanliaojg" />-->
      <!--      <el-table-column label="胶水价格" align="center" prop="jiaoshuijg" />-->
      <!--      <el-table-column label="吹膜材料成本" align="center" prop="chuimocailiaocb" />-->
      <!--      <el-table-column label="吹膜人工成本" align="center" prop="chuimorengcb" />-->
      <!--      <el-table-column label="吹膜电费成本" align="center" prop="chuimodianfeicb" />-->
      <el-table-column align="center" label="上胶量" prop="shangjiaoliang"/>
      <!--      <el-table-column label="胶水材料成本" align="center" prop="jiaoshuicailiangcb" />-->
      <!--      <el-table-column label="涂布机器速度" align="center" prop="tubujiqisd" />-->
      <!--      <el-table-column label="涂布人工成本" align="center" prop="tuburengcb" />-->
      <!--      <el-table-column label="涂布电费成本" align="center" prop="tubudianfeicb" />-->
      <!--      <el-table-column label="纸管" align="center" prop="zhiguan" width="120" />-->
      <!--      <el-table-column label="纸片" align="center" prop="zhipian" width="120"/>-->
      <!--      <el-table-column label="人工" align="center" prop="rengong" width="120"/>-->
      <!--      <el-table-column label="印刷" align="center" prop="yinshua" width="120" />-->
      <el-table-column align="center" label="运费" prop="yunfei"/>
      <el-table-column align="center" label="基准价" prop="jizhunjia" width="120"/>
      <el-table-column align="center" label="不含税基准价" prop="jizhunjiaB" width="120"/>
      <!--      <el-table-column label="成本" align="center" prop="chengben" width="120"/>-->
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >详情
          </el-button>
          <el-button
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

    <!-- 添加或修改薄膜核算对话框 -->
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="open" append-to-body width="980px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder=""/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="核算方式" prop="priceType">
              <el-select v-model="form.priceType">
                <el-option
                  v-for="item in priceTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type">
                <el-option
                  v-for="item in typeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="宽度(m)" prop="kd">
              <el-input v-model="form.kd" placeholder="" type="number"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="吹膜厚度(丝)" prop="chuimohd">
              <el-input v-model="form.chuimohd" placeholder="" type="number" @input="chuimohdChange($event)"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="米数(m)" prop="mishu">
              <el-input v-model="form.mishu" placeholder="" type="number"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="上胶量(g)" prop="shangjiaoliang">
              <el-input v-model="form.shangjiaoliang" placeholder="" type="number" @input="shangjiaoChange($event)"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="印刷" prop="yanse">
              <el-select v-model="form.yanse">
                <el-option
                  v-for="item in yinshuaList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="抗uv" prop="kanguvP">
              <el-select v-model="form.kanguvP">
                <el-option
                  v-for="item in kanguvPList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="form.priceType=='公斤价'?'运费(元/吨)':'运费(元/平方)'" prop="yunfei">
              <el-input v-model="form.yunfei" placeholder="" type="number"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="含税基准价" label-width="150px" prop="jizhunjia">
              <el-input v-model="form.jizhunjia" :disabled="true" placeholder=""/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="不含税基准价" label-width="150px" prop="jizhunjiaB">
              <el-input v-model="form.jizhunjiaB" :disabled="true" placeholder=""/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-collapse>
          <el-collapse-item v-show="true" title="成本明细">
            <el-card>
              <el-row>
                <el-col :span="8">
                  <el-form-item label="吹膜材料成本" prop="chuimocailiaocb">
                    <el-input v-model="form.chuimocailiaocb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="吹膜人工成本" prop="chuimorengcb">
                    <el-input v-model="form.chuimorengcb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="吹膜电费成本" prop="chuimodianfeicb">
                    <el-input v-model="form.chuimodianfeicb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="8">
                  <el-form-item label="胶水材料成本" prop="jiaoshuicailiangcb" width="120">
                    <el-input v-model="form.jiaoshuicailiangcb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="涂布人工成本" prop="tuburengcb">
                    <el-input v-model="form.tuburengcb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="涂布电费成本" prop="tubudianfeicb">
                    <el-input v-model="form.tubudianfeicb" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="8">
                  <el-form-item label="涂布机器速度" prop="tubujiqisd">
                    <el-input v-model="form.tubujiqisd" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="纸管" prop="zhiguan">
                    <el-input v-model="form.zhiguan" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="印刷" prop="yinshua">
                    <el-input v-model="form.yinshua" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="8">
                  <el-form-item label="包装" prop="baozhuang">
                    <el-input v-model="form.baozhuang" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="分切" prop="fenqie">
                    <el-input v-model="form.fenqie" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="抗uv" prop="kanguv">
                    <el-input v-model="form.kanguv" :disabled="true" placeholder=""/>
                  </el-form-item>
                </el-col>
              </el-row>

            </el-card>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">计算成本</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addBomochengbenNeimao,
  delBomochengbenNeimao,
  getBomochengbenNeimao,
  listBomochengbenNeimao,
  updateBomochengbenNeimao
} from "@/api/mes/finance/bomochengben";

export default {
  name: "BomochengbenNeimao",
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
      // 薄膜核算表格数据
      bomochengbenList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        name: null,
        priceType: null,
        yanse: null,
        type: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        chuimohd: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
        kd: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
        mishu: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
        shangjiaoliang: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
        chuimosd: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
        yunfei: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
      },
      priceTypeList: [{
        value: '平方价',
        label: '平方价'
      }, {
        value: '公斤价',
        label: '公斤价'
      }],
      typeList: [{
        value: '透明',
        label: '透明'
      }, {
        value: '黑白',
        label: '黑白'
      }, {
        value: '黑白加填充',
        label: '黑白加填充'
      }],
      yinshuaList: [{
        value: '0',
        label: '无'
      }, {
        value: '0.0945',
        label: '单色'
      }, {
        value: '0.10125',
        label: '双色'
      }, {
        value: '0.108',
        label: '三色'
      }],
      jiaoshuiTypeList: [{
        value: '普通胶水',
        label: '普通胶水'
      }, {
        value: '特殊胶水',
        label: '特殊胶水'
      }],
      baozhuangPList: [{
        value: 2,
        label: '纸片'
      }, {
        value: 4,
        label: '纸箱'
      }],
      zhiguanPList: [{
        value: 8,
        label: '4mmPVC管'
      }, {
        value: 11,
        label: '6mmPVC管'
      }, {
        value: 5.7,
        label: '5mm纸管'
      }, {
        value: 8.0,
        label: '7mm纸管'
      }, {
        value: 9.4,
        label: '8mm纸管'
      }],
      fenqiePList: [{
        value: 2.25,
        label: '10cm以上'
      }, {
        value: 3,
        label: '5cm~10cm'
      }, {
        value: 4,
        label: '5cm以下'
      }],
      kanguvPList: [{
        value: 0,
        label: '无'
      }, {
        value: 0.012,
        label: '一年'
      }, {
        value: 0.006,
        label: '半年'
      }]

    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询薄膜核算列表 */
    getList() {
      this.loading = true;
      listBomochengbenNeimao(this.queryParams).then(response => {
        this.bomochengbenList = response.rows;
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
        name: null,
        priceType: '平方价',
        jiaoshuiType: '普通胶水',
        yanse: '0',
        type: '透明',
        zhiguanP: 8,
        kanguvP: 0,
        gongneng: 0,
        baozhuangP: 2,
        fenqieP: 2.25,
        chuimohd: null,
        kd: null,
        mishu: null,
        chuimosd: 12,
        yuanliaojg: null,
        jiaoshuijg: null,
        chuimocailiaocb: null,
        chuimorengcb: null,
        chuimodianfeicb: null,
        shangjiaoliang: null,
        jiaoshuicailiangcb: null,
        tubujiqisd: null,
        tuburengcb: null,
        tubudianfeicb: null,
        zhiguan: null,
        zhipian: null,
        rengong: null,
        yinshua: null,
        yunfei: 0,
        chengben: null,
        jizhunjia: null,
        id: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
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
      this.title = "添加薄膜核算";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getBomochengbenNeimao(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改薄膜核算";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.chuimohdChange(this.form.chuimohd);
          if (this.form.id != null) {
            updateBomochengbenNeimao(this.form).then(response => {
              this.$modal.msgSuccess("成本核算成功");
              this.open = false;
              this.getList();
            });
          } else {
            addBomochengbenNeimao(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除薄膜核算编号为"' + ids + '"的数据项？').then(function () {
        return delBomochengbenNeimao(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('finance/bomochengben/export', {
        ...this.queryParams
      }, `bomochengben_${new Date().getTime()}.xlsx`)
    },
    /** 吹膜厚度对应吹膜机器速度*/
    chuimohdChange(value) {
      //黑白膜
      if (this.form.type === '黑白') {
        if (value >= 1.7 && value <= 2.0) {
          this.form.chuimosd = 50;
        }
        if (value >= 2.1 && value <= 2.3) {
          this.form.chuimosd = 40;
        }
        if (value >= 2.4 && value <= 3.0) {
          this.form.chuimosd = 38;
        }
        if (value >= 3.1 && value <= 3.5) {
          this.form.chuimosd = 28;
        }
        if (value >= 3.6 && value <= 4) {
          this.form.chuimosd = 25;
        }
        if (value >= 4.1 && value <= 4.5) {
          this.form.chuimosd = 22;
        }
        if (value >= 4.6 && value <= 5.0) {
          this.form.chuimosd = 21;
        }
        if (value >= 5.1 && value <= 5.5) {
          this.form.chuimosd = 19;
        }
        if (value >= 5.6 && value <= 6.0) {
          this.form.chuimosd = 18;
        }
        if (value >= 6.1 && value <= 7.0) {
          this.form.chuimosd = 15;
        }
        if (value >= 7.1 && value <= 8.0) {
          this.form.chuimosd = 14;
        }
        if (value >= 8.1 && value <= 9.0) {
          this.form.chuimosd = 13;
        }
        if (value >= 9.1 && value <= 9.5) {
          this.form.chuimosd = 12;
        }
        if (value >= 9.5 && value <= 10.0) {
          this.form.chuimosd = 11;
        }
        if (value >= 10.1 && value <= 11) {
          this.form.chuimosd = 10;
        }
        if (value >= 11.1 && value <= 12) {
          this.form.chuimosd = 9;
        }
        if (value >= 12.1 && value <= 15) {
          this.form.chuimosd = 8;
        }
      }
      //透明膜
      if (this.form.type === '透明') {
        if (value >= 1.7 && value <= 2.0) {
          this.form.chuimosd = 78;
        }
        if (value >= 2.1 && value <= 2.3) {
          this.form.chuimosd = 60;
        }
        if (value >= 2.4 && value <= 3.0) {
          this.form.chuimosd = 55;
        }
        if (value >= 3.1 && value <= 3.5) {
          this.form.chuimosd = 43;
        }
        if (value >= 3.6 && value <= 4) {
          this.form.chuimosd = 42;
        }
        if (value >= 4.1 && value <= 4.5) {
          this.form.chuimosd = 36;
        }
        if (value >= 4.5 && value <= 5.0) {
          this.form.chuimosd = 34;
        }
        if (value >= 5.1 && value <= 5.5) {
          this.form.chuimosd = 33;
        }
        if (value >= 5.6 && value <= 6.0) {
          this.form.chuimosd = 31;
        }
        if (value >= 6.1 && value <= 7.0) {
          this.form.chuimosd = 24;
        }
        if (value >= 7.1 && value <= 8.0) {
          this.form.chuimosd = 23;
        }
        if (value >= 8.1 && value <= 9.0) {
          this.form.chuimosd = 22;
        }
        if (value >= 9.1 && value <= 9.5) {
          this.form.chuimosd = 20;
        }
        if (value >= 9.5 && value <= 10.0) {
          this.form.chuimosd = 19;
        }
        if (value >= 10.1 && value <= 11) {
          this.form.chuimosd = 18;
        }
        if (value >= 11.1 && value <= 12) {
          this.form.chuimosd = 17;
        }
        if (value >= 12.1 && value <= 15) {
          this.form.chuimosd = 16;
        }
      }
    },
    shangjiaoChange(value) {
      if (value >= 2.5 && value <= 4.9) {
        this.form.tubujiqisd = 80;
      }
      if (value >= 5 && value <= 8.5) {
        this.form.tubujiqisd = 100;
      }
      if (value >= 8.6 && value <= 11) {
        this.form.tubujiqisd = 80;
      }
      if (value >= 11.1 && value <= 21.9) {
        this.form.tubujiqisd = 50;
      }
      if (value >= 22 && value <= 27.9) {
        this.form.tubujiqisd = 35;
      }
      if (value >= 28 && value <= 35.9) {
        this.form.tubujiqisd = 30;
      }
      if (value >= 36 && value <= 45.9) {
        this.form.tubujiqisd = 27;
      }
      if (value >= 45 && value <= 59.9) {
        this.form.tubujiqisd = 23;
      }
      if (value >= 60 && value <= 100) {
        this.form.tubujiqisd = 19;
      }
    },


  }
};
</script>
