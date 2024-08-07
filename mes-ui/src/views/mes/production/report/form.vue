<template>
  <div class="app-container">
    <!-- 生产工单主表信息 -->
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-descriptions :column="5" :title="form.workOrderName" border direction="vertical">
        <el-descriptions-item label="工单编号">{{ form.number }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ form.materialName }}</el-descriptions-item>
        <el-descriptions-item label="产品规格型号">{{ form.specification }}</el-descriptions-item>
        <el-descriptions-item label="产品编号">{{ form.materialNumber }}</el-descriptions-item>
        <el-descriptions-item label="工单状态">
          <dict-tag :options="dict.type.production_status" :value="form.proStatus"/>
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ form.batchNumber }}</el-descriptions-item>
        <el-descriptions-item label="计划完成时间">{{ form.planFinishDate }}</el-descriptions-item>
        <el-descriptions-item label="排产数量 (单位)">{{ form.quantity }}&nbsp;({{ form.materialUnitName }})
        </el-descriptions-item>
        <el-descriptions-item label="工序">{{ form.workOrderEntryList[0].processName }}</el-descriptions-item>
        <el-descriptions-item label="工序在工单中顺序">第&nbsp;{{ form.workOrderEntryList[0].sort }}&nbsp;道
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="工单备注">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="center">报工单明细</el-divider>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-show="isShow" icon="el-icon-plus" size="mini" type="primary" @click="handleAddEntry">添加
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-show="isShow" icon="el-icon-delete" size="mini" type="danger" @click="handleDeleteEntry">删除
          </el-button>
        </el-col>
      </el-row>
      <el-table ref="entry" :data="form.reportList" :row-class-name="rowEntryIndex"
                @selection-change="handleSelectionChange">
        <el-table-column align="center" type="selection" width="50"/>
        <el-table-column align="center" label="序号" prop="index" width="50">
        </el-table-column>
        <el-table-column align="center" label="报工人"
                         width="150">
          <template slot-scope="scope">
            <el-form-item :key="Math.random()" :prop="'reportList.'+scope.$index+'.reportUserId'"
                          :rules="rules.reportUserId" class="reportFormItem" label-width="0">
              <el-select v-model="scope.row.reportUserId" :disabled="scope.row.disabledFlag" filterable
                         filterable
                         placeholder="请选择报工人" @change="userChange($event,scope.row)">
                <el-option
                  v-for="item in userList"
                  :key="item.userId"
                  :label="item.nickName"
                  :value="item.userId"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="报工时间" prop="reportTime" width="240">
          <template slot-scope="scope">
            <el-form-item :key="Math.random()" :prop="'reportList.'+scope.$index+'.reportTime'"
                          :rules="rules.reportTime" class="reportFormItem" label-width="0">
              <el-date-picker v-model="scope.row.reportTime" :disabled="scope.row.disabledFlag" clearable
                              placeholder="请选择报工时间"
                              type="date" value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="报工数量" prop="reportQuantity" width="150">
          <template slot-scope="scope">
            <el-form-item :key="scope.$index" :prop="'reportList.'+scope.$index+'.reportQuantity'"
                          :rules="rules.reportQuantity" class="reportFormItem" label-width="0">
              <el-input v-model="scope.row.reportQuantity" :disabled="scope.row.disabledFlag"
                        placeholder="请输入报工数量"/>
            </el-form-item>
          </template>
        </el-table-column>
        <!--   合格数和不合格数尽在具有质检权限时显示   -->
        <!--      <el-table-column label="合格数量" prop="qualifiedQuantity" width="150">-->
        <!--        <template slot-scope="scope">-->
        <!--          <el-input v-model="scope.row.qualifiedQuantity" placeholder="请输入合格数量" />-->
        <!--        </template>-->
        <!--      </el-table-column>-->
        <!--      <el-table-column label="不合格数量" prop="unqualifiedQuantity" width="150">-->
        <!--        <template slot-scope="scope">-->
        <!--          <el-input v-model="scope.row.unqualifiedQuantity" placeholder="请输入不合格数量" />-->
        <!--        </template>-->
        <!--      </el-table-column>-->
        <el-table-column label="车间" prop="workshopId">
          <template slot-scope="scope">
            <el-form-item :key="Math.random()" :prop="'reportList.'+scope.$index+'.workshopId'"
                          :rules="rules.workshopId" class="reportFormItem" label-width="0">
              <el-select v-model="scope.row.workshopId" :disabled="scope.row.disabledFlag" placeholder="请选择车间"
                         style="width: 100%" @change="workshopSelectChange($event,scope.row)">
                <el-option
                  v-for="item in workshopOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="工位" prop="stationId">
          <template slot-scope="scope">
            <el-form-item :key="Math.random()" :prop="'reportList.'+scope.$index+'.stationId'"
                          :rules="rules.stationId" class="reportFormItem" label-width="0">
              <el-select v-model="scope.row.stationId" :disabled="scope.row.disabledFlag" placeholder="请选择工位"
                         style="width: 100%" @change="stationSelectChange($event,scope.row)">
                <el-option
                  v-for="item in stationOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="是否已结算工资" prop="isSettle" width="150">
          <template slot-scope="scope">
            <el-form-item :prop="'reportList.'+scope.$index+'.isSettle'"
                          class="reportFormItem" label-width="0">
              <el-select v-model="scope.row.isSettle" :disabled="true">
                <el-option
                  v-for="dict in dict.type.sys_yes_no"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="结算工资" prop="wages" width="150">
          <template slot-scope="scope">
            <el-form-item :prop="'reportList.'+scope.$index+'.wages'"
                          class="reportFormItem" label-width="0">
              <el-input v-model="scope.row.wages" :disabled="true" placeholder="还未结算工资"/>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['production:report:edit']"
              size="mini"
              type="text"
              @click="qualityAddClick(scope.row)"
            >质检单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-form>
    <reportQualityForm ref="reportQualityForm"></reportQualityForm>
    <div slot="footer" class="dialog-footer">
      <el-button v-show="isShow" type="primary" @click="submitForm">保 存</el-button>
    </div>

  </div>
</template>

<script>
import addressJson from '@/components/mes/address.json'
import {listUser} from '@/api/system/user'
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import MaterialSelect from "@/components/mes/materialSelect.vue";
import {batchSave, listReport} from "@/api/mes/production/report";
import {getWorkOrderByEntryId} from '@/api/mes/production/workOrder'
import {listStation} from '@/api/mes/masterdata/station'
import {listWorkshop} from '@/api/mes/masterdata/workshop'
import reportQualityForm from "@/views/mes/quality/reportQuality/form.vue";

export default {
  name: 'form',
  components: {Treeselect, MaterialSelect, reportQualityForm},
  dicts: ['currency', 'production_status', 'report_channel', 'sys_yes_no', 'qc_type'],
  data() {
    return {
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedEntry: [],
      //质检单
      reportQuality: {},
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {
        currency: 'CNY',
        saleDate: new Date(),
        entryList: [],
        reportList: []
      },
      // 表单校验
      rules: {
        reportUserId: [
          {required: true, message: "报工人必选", trigger: "blur"}
        ],
        reportTime: [
          {required: true, message: "报工时间必填", trigger: "blur"}
        ],
        reportQuantity: [
          {
            required: true,
            pattern: /^[1-9]*[1-9][0-9]*$/g,
            message: '请输入大于0的数字',
            trigger: "blur"
          }
        ],
        workshopId: [
          {required: true, message: "车间必选", trigger: "blur"}
        ],
        stationId: [
          {required: true, message: "工位必选", trigger: "blur"}
        ],
        checkUserId: [
          {required: true, message: "质检员必选", trigger: "blur"}
        ],
        qualifiedQuantity: [
          {
            required: true,
            pattern: /^[1-9]*[1-9][0-9]*$/g,
            message: '请输入大于0的数字',
            trigger: "blur"
          }
        ],
        unqualifiedQuantity: [
          {
            required: true,
            pattern: /^[1-9]*[1-9][0-9]*$/g,
            message: '请输入大于0的数字',
            trigger: "blur"
          }
        ],
      },
      options: addressJson,
      // 部门树选项
      deptOptions: undefined,
      currentRow: '',
      //页面类型，0新增，1编辑,2只读
      formType: 0,
      maxIndex: 0,
      userList: [],
      customerList: [],
      //车间选项
      workshopOptions: [],
      //工位选项
      stationOptions: [],
      //质检合格等级列表
      qualityLevelList: [],
      //质检不合格等级列表
      unQualityLevelList: [],
      //质检原因合格列表
      reasonList: [],
      //质检原因不合格列表
      unReasonList: [],
      tmpQualityLevelList: [],
      tmpUnQualityLevelList: [],
      qualityDisabled: false,
      isShow: true
    };
  },
  created() {
    //新增
    this.formType = 0;
    this.getUserList();
    this.getWorkshops();
    this.getStation();
    const workOrderId = this.$route.params.workOrderId;
    if (typeof (workOrderId) != 'undefined') {
      this.getForm(workOrderId);
    }
  },
  methods: {
    getForm(id) {
      this.formType = 0;
      getWorkOrderByEntryId(id).then(response => {
        this.form = response.data;
        this.form.entryList = response.data.workOrderEntryList;
        this.form.reportChannel = "PC";
        this.form.workOrderEntryId = id;

        //不为生产中状态的生产工单禁用按钮
        if (this.form.proStatus != "B") {
          this.isShow = false
        } else {
          this.isShow = true
        }

        //再根据当前工单分录id查起所有的报工单
        listReport({pageSize: 99999, workOrderEntryId: id}).then(reportResponse => {
          let list = [];
          list = reportResponse.rows;
          for (let i = 0; i < list.length; i++) {
            list[i].disabledFlag = true;
          }
          this.form.reportList = list;
          console.log("this.form.reportList===>", this.form.reportList)
          this.$forceUpdate();
        });
      });


    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          let tmp = [];
          for (let i = 0; i < this.form.reportList.length; i++) {
            let report = this.form.reportList[i];
            if (typeof (report.id) == "undefined") {
              tmp.push(report);
            }
          }
          batchSave(tmp).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.$tab.refreshPage();
          })
        }
      });
    },

    /** 添加按钮操作 */
    handleAddEntry() {
      //获取当前最大的sort为maxIndex
      let tmp = 0;
      for (let i = 0; i < this.form.reportList.length; i++) {
        if (this.form.reportList[i].index > tmp) {
          tmp = this.form.reportList[i].index;
        }
      }
      this.maxIndex = tmp;
      this.maxIndex = this.maxIndex + 1
      let obj = {};
      obj.reportTime = new Date().format("yyyy-MM-dd HH:mm:ss");
      obj.reportQuantity = this.form.quantity;
      obj.qualifiedQuantity = "";
      obj.reportUserId = "",
        obj.reportUserName = "",
        obj.unqualifiedQuantity = "";
      obj.workshopId = "";
      obj.workshopName = "";
      obj.stationId = "";
      obj.stationName = "";
      obj.isSettle = "N";
      obj.wages = 0;
      obj.workOrderEntryId = this.form.workOrderEntryList[0].id
      this.form.reportList.push(obj);
      this.$refs.entry.sort("index", "ascending")
    },
    /** 删除按钮操作 */
    handleDeleteEntry() {

      if (this.checkedEntry.length == 0) {
        this.$modal.msgError("请先选择要删除的数据");
      } else {
        const checkedEntry = this.checkedEntry;
        const entryList = this.form.reportList;
        let tmp = entryList.filter(function (item) {
          return checkedEntry.indexOf(item.index) != -1
        });
        for (let i = 0; i < tmp.length; i++) {
          if (tmp[i].disabledFlag === true) {
            this.$modal.msgError("已生成的报工单无法删除,请重新选择");
            return;
          }
        }

        this.form.reportList = entryList.filter(function (item) {
          return checkedEntry.indexOf(item.index) === -1
        });
        console.log("this.form.reportList========>", this.form.reportList)
        for (let i = 0; i < this.form.reportList.length; i++) {
          this.form.reportList[i].index = i + 1
        }
        this.maxIndex = this.form.reportList.length;
        this.$refs.entry.sort("index", "ascending")
        this.$refs.entry.clearSelection();
        this.checkedEntry = []
        this.$forceUpdate();
      }
    },
    /** 复选框选中数据 */
    handleSelectionChange(selection) {
      this.checkedEntry = selection.map(item => item.index)
      console.log("this.checkedEntry====>", this.checkedEntry)
    },

    /** 获取用户数据*/
    getUserList() {
      let qp = {pageSize: 99999};
      listUser(this.addDateRange(qp, [])).then(response => {
          this.userList = response.rows;
        }
      );
    },

    userChange(value, row) {
      let opt = {};
      opt = this.userList.find((item) => {
        return item.userId === value;
      });
      let currentRow = row;
      currentRow.reportUserName = opt.nickName;
    },


    /** 编码规则子序号 */
    rowEntryIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
      if (row.disabledFlag == true) {
        console.log("row.disabledFlag===>", row.disabledFlag)
        return "warning-row";
      }
    },

    //查询车间信息
    getWorkshops() {
      listWorkshop({pageSize: 999999}).then(response => {
        this.workshopOptions = response.rows;
        console.log("this.workshopOptions=========>", this.workshopOptions)
      });
    },
    //查询工位信息
    getStation() {
      listStation({pageSize: 999999}).then(response => {
        this.stationOptions = response.rows;
      });
    },
    workshopSelectChange(value, row) {
      //获取下拉框中label的值
      let currentRow = row;
      let res = this.workshopOptions.find((item) => {
        return item.id === value;
      });
      currentRow.workshopName = res.name;
      this.$refs["form"].validate();
    },
    stationSelectChange(value, row) {
      //获取下拉框中label的值
      let currentRow = row;
      let res = this.stationOptions.find((item) => {
        return item.id === value;
      });
      currentRow.stationName = res.name;
      this.$refs["form"].validate();
    },

    //点击质检单新增按钮
    qualityAddClick(row) {
      if (typeof (row.id) == 'undefined') {
        this.$modal.msgError("请先保存报工单");
        return;
      }
      row.isShow = this.isShow;
      this.$refs.reportQualityForm.qualityAddClick(row);

    },

  }
}
</script>
<style>
.reportFormItem {
  margin-bottom: 0px;
}

.el-table .warning-row {
  background: oldlace;
}

</style>

