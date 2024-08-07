<!--根据不同工序定制计划排产页面-->
<template>
  <keep-alive>
    <div class="app-container">
      <el-row>
        <div style="text-align: center;margin-bottom: 20px">排产工序：{{ process.name }}</div>
        <!--  工单主表    -->
        <el-form ref="form" :model="form" :rules="rules" style="margin-top: 20px">
          <el-table :data="form.workOrderList"
                    :row-style="tableRowClassName"
                    height="300"
          >
            <el-table-column :width="150" align="center" label="产成品">
              <template slot-scope="scope">
                <el-input v-model="scope.row.materialName" :disabled="true" style="width: 105px"/>
              </template>
            </el-table-column>
            <el-table-column :width="150" align="center" label="版型">
              <template slot-scope="scope">
                <el-input v-model="scope.row.extendFieldJson.banxing" :disabled="true" style="width: 105px"/>
              </template>
            </el-table-column>
            <el-table-column :width="180" align="center" label="型号">
              <template slot-scope="scope">
                <el-form-item :prop="'workOrderList.'+scope.$index+'.specification'" :rules="rules.specification"
                              label-width="0"
                              style="margin-bottom: 7px;">
                  <el-input v-model="scope.row.specification"/>
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column :width="150" align="center" label="数量">
              <template slot-scope="scope">
                <el-form-item :prop="'workOrderList.'+scope.$index+'.quantity'" :rules="rules.quantity" label-width="0"
                              style="margin-bottom: 7px;">
                  <el-input v-model="scope.row.quantity"/>
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column :width="150" align="center" label="计划完成日期" width="160">
              <template slot-scope="scope">
                <el-form-item :prop="'workOrderList.'+scope.row.index+'.planFinishDate'" :rules="rules.planFinishDate"
                              label-width="0"
                              style="margin-bottom: 7px;">
                  <el-date-picker v-model="scope.row.planFinishDate" :clearable="false" style="width: 140px"
                                  type="date" value-format="yyyy-MM-dd"/>
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column :width="120" align="center" label="附件">
              <template slot-scope="scope">
                <el-link type="primary" @click="fileDownload(scope.row)">下载</el-link>
              </template>
            </el-table-column>
            <el-table-column align="center" label="备注">
              <template slot-scope="scope">
                <el-form-item label-width="0" style="margin-bottom: 7px;">
                  <el-input v-model="scope.row.remark"/>
                </el-form-item>
              </template>
            </el-table-column>
          </el-table>
        </el-form>
      </el-row>
      <el-row>

        <el-tabs v-model="activeName">
          <el-tab-pane label="领料" name="first">
            <el-button type="primary" @click="entryAddClick('pick')">新增</el-button>
            <el-form ref="pickForm" :model="form" :rules="rules" label-width="80px" style="margin-top: 20px">
              <el-table :data="form.pickEntryList"
                        height="300"
              >
                <el-table-column label="领取物料" width="240">
                  <template slot-scope="scope">
                    <el-form-item :prop="'pickEntryList.'+scope.$index+'.pickMaterialName'" :rules="rules.materialName"
                                  label-width="0" style="margin-bottom: 12px;">
                      <el-input v-model="scope.row.pickMaterialName" placeholder="产品为必填项">
                        <el-button slot="append" icon="el-icon-search"
                                   @click="handleSelectProduct(scope.row)"></el-button>
                      </el-input>
                    </el-form-item>
                  </template>
                </el-table-column>
                <!--                <el-table-column label="规格型号" align="center">-->
                <!--                  <template slot-scope="scope">-->
                <!--                    <el-form-item label-width="0" style="margin-bottom: 7px;"-->
                <!--                                  :rules="rules.specification"-->
                <!--                                  :prop="'pickEntryList.'+scope.$index+'.pickSpecification'">-->
                <!--                      <el-input v-model="scope.row.pickSpecification"/>-->
                <!--                    </el-form-item>-->
                <!--                  </template>-->
                <!--                </el-table-column>-->
                <el-table-column align="center" label="规格型号">
                  <template slot-scope="scope">
                    <el-form-item :prop="'pickEntryList.'+scope.$index+'.pickSpecification'"
                                  :rules="rules.specification"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-autocomplete
                        v-model="scope.row.pickSpecification"
                        :fetch-suggestions="((queryString,cb)=>{querySearch(queryString,cb,scope.row)})"
                        :trigger-on-focus="false"
                        class="inline-input"
                        placeholder="请输入内容"
                        @select="handleSelect"
                      ></el-autocomplete>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="领取数量">
                  <template slot-scope="scope">
                    <el-form-item :prop="'pickEntryList.'+scope.$index+'.pickQuantity'" :rules="rules.quantity"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-input v-model="scope.row.pickQuantity"/>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="备注">
                  <template slot-scope="scope">
                    <el-form-item label-width="0" style="margin-bottom: 7px;">
                      <el-input v-model="scope.row.remark"/>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
                  <template slot-scope="scope">
                    <el-button
                      v-hasPermi="['production:workOrder:remove']"
                      icon="el-icon-delete"
                      size="mini"
                      type="text"
                      @click="handleDelete( scope.$index,'pick',scope.row)"
                    >移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="报工" name="second">
            <el-button type="primary" @click="entryAddClick('report')">新增</el-button>
            <el-form ref="reportForm" :model="form" :rules="rules" label-width="80px" style="margin-top: 20px">
              <el-table :data="form.reportEntryList"
              >
                <el-table-column label="报工物料" width="180">
                  <template slot-scope="scope">
                    <el-form-item :prop="'reportEntryList.'+scope.$index+'.reportMaterialName'"
                                  :rules="rules.materialName" label-width="0" style="margin-bottom: 12px;">
                      <el-input v-model="scope.row.reportMaterialName" placeholder="产品为必填项">
                        <el-button slot="append" icon="el-icon-search"
                                   @click="handleSelectProduct(scope.row)"></el-button>
                      </el-input>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="规格型号" width="180">
                  <template slot-scope="scope">
                    <el-form-item :prop="'reportEntryList.'+scope.$index+'.reportSpecification'"
                                  :rules="rules.specification"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-autocomplete
                        v-model="scope.row.reportSpecification"
                        :fetch-suggestions="((queryString,cb)=>{querySearch(queryString,cb,scope.row)})"
                        :trigger-on-focus="false"
                        class="inline-input"
                        placeholder="请输入内容"
                        @select="handleSelect"
                      ></el-autocomplete>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="报工数量" width="80">
                  <template slot-scope="scope">
                    <el-form-item :prop="'reportEntryList.'+scope.$index+'.reportQuantity'" :rules="rules.quantity"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-input v-model="scope.row.reportQuantity"/>
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="工位" width="120">
                  <template slot-scope="scope">
                    <el-form-item :prop="'reportEntryList.'+scope.$index+'.stationId'" :rules="rules.station"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-select v-model="scope.row.stationId" placeholder="请选择工位"
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
                <el-table-column label="设备" prop="stationId" width="120">
                  <template slot-scope="scope">
                    <el-form-item :prop="'reportEntryList.'+scope.$index+'.machineId'" :rules="rules.machine"
                                  label-width="0"
                                  style="margin-bottom: 7px;">
                      <el-select v-model="scope.row.machineId" placeholder="请选择设备"
                                 style="width: 100%" @change="machineSelectChange($event,scope.row)">
                        <el-option
                          v-for="item in equipmentOptions"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </template>
                </el-table-column>
                <!--   子表自定义扩展字段     -->
                <div v-if="entryFieldExtendList.length==0">
                  <el-table-column align="center" label="备注">
                    <template slot-scope="scope">
                      <el-form-item :prop="'reportEntryList.'+scope.$index+'.remark'" label-width="0"
                                    style="margin-bottom: 7px;">
                        <el-input v-model="scope.row.remark"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </div>
                <div v-for="(item,index) in entryFieldExtendList">
                  <el-table-column :label="item.fieldName" :width="item.width">
                    <template slot-scope="scope">
                      <el-form-item :prop="'reportEntryList.'+scope.$index+'.extendFieldJson[item.field]'"
                                    label-width="0"
                                    style="margin-bottom: 12px;">
                        <el-input v-model="scope.row.extendFieldJson[item.field]"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <div v-if="index===entryFieldExtendList.length-1">
                    <el-table-column align="center" label="备注">
                      <template slot-scope="scope">
                        <el-form-item :prop="'reportEntryList.'+scope.$index+'.remark'" label-width="0"
                                      style="margin-bottom: 7px;">
                          <el-input v-model="scope.row.remark"/>
                        </el-form-item>
                      </template>
                    </el-table-column>
                  </div>
                </div>
                <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
                  <template slot-scope="scope">
                    <el-button
                      v-hasPermi="['production:workOrder:remove']"
                      icon="el-icon-delete"
                      size="mini"
                      type="text"
                      @click="handleDelete( scope.$index,'report',scope.row)"
                    >移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form>
          </el-tab-pane>

        </el-tabs>
        <MaterialSelect ref="materialSelect" @onMaterialSelect="onMaterialSelect"></MaterialSelect>
        <el-dialog
          :visible.sync="dialogVisible"
          title="下载">
          <span v-for="(item,index) in fileList">
            <el-row>
              <a :href="item" target="_blank">{{ item }}</a>
            </el-row>
          </span>
        </el-dialog>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button style="margin-top: 20px;" type="primary" @click="closeButton">关闭</el-button>
        <el-button :disabled="buttonDisabled" style="margin-top: 20px;" type="primary" @click="handleAdd">保存工单
        </el-button>
      </div>

    </div>

  </keep-alive>
</template>

<script>

import {batchAddWorkOrderByJson} from '@/api/mes/production/workOrder'
import Treeselect from "@riophae/vue-treeselect";
import MaterialSelect from '@/components/mes/materialSelect'
import {listEntryByIds} from '@/api/mes/sale/saleOrder'
import {listWorkshop} from '@/api/mes/masterdata/workshop'
import {listStation} from '@/api/mes/masterdata/station'
import {getProcess} from "@/api/mes/production/process";
import {listEquipment} from "@/api/mes/equipment/equipment";
import {listFieldExtend} from "@/api/mes/system/fieldExtend";
import {listWarehouse} from '@/api/mes/warehouse/warehouse'
import {getSpecificationList} from "@/api/mes/warehouse/inventory";


export default {
  name: "workOrderPreviewA",
  dicts: ['production_status'],
  components: {
    Treeselect, MaterialSelect
  },
  data() {
    return {

      // 工单主表集合数据
      form: {
        workOrderList: [],
        reportEntryList: [],
        pickEntryList: []
      },

      // 表单校验
      rules: {
        quantity: [
          {
            required: true,
            pattern: /^[1-9]*[1-9][0-9]*$/g,
            message: "请输入生产数量",
            trigger: "blur"
          }
        ],
        specification: [
          {
            required: true,
            message: "请填写规格型号",
            trigger: "blur"
          }
        ],

        materialName: [
          {
            required: true,
            message: "请选中物料",
            trigger: "blur"
          }
        ],
        station: [
          {
            required: true,
            message: "请选择",
            trigger: "change"
          }
        ],
        machine: [
          {
            required: true,
            message: "请选择",
            trigger: "change"
          }
        ],
      },
      //车间选项
      workshopOptions: [],
      //工位选项
      stationOptions: [],
      buttonDisabled: false,
      activeName: 'first',
      process: {},
      currentRow: "",
      equipmentOptions: [],
      //选中的订单index
      selectWorkOrder: {index: -1},
      dialogVisible: false,
      fileList: [],
      entryFieldExtendList: {},
      warehouseList: [],
      restaurants: [
        {"value": "三全鲜食（北新泾店）", "address": "长宁区新渔路144号"},
        {"value": "Hot honey 首尔炸鸡（仙霞路）", "address": "上海市长宁区淞虹路661号"},
        {"value": "新旺角茶餐厅", "address": "上海市普陀区真北路988号创邑金沙谷6号楼113"}],
    };
  },
  created() {
    //this.getList();
    this.getEquipmentList();
    this.getStation();
  },
  activated() {
    this.cleanData();
    this.getList();
  },
  methods: {
    /** 查询工单列表 */
    getList() {
      let ids = this.$route.params.ids;
      let processId = this.$route.params.processId;
      this.processSort = this.$route.params.processSort;
      let routeId = this.$route.params.routeId;

      if (ids === undefined || ids == null) {
        this.$confirm('此页面无任何可生成的排产计划,请重新操作', '提示', {
          confirmButtonText: '确定',
          type: 'warning'
        }).then(() => {
          const obj = {path: "/index"};
          this.$tab.closeOpenPage(obj);
        })
        return;
      }
      getProcess(processId).then(response => {
        this.process = response.data;
        //扩展字段结构
        listFieldExtend({pageSize: 99999, sourceBill: 'process-' + this.process.id}).then(response => {
          this.entryFieldExtendList = response.rows;
          console.log("this.entryFieldExtendList===========>", this.entryFieldExtendList)
        });
      });
      //根据销售订单子表id查询所有销售订单信息
      listEntryByIds({ids: ids}).then(response => {
        let saleOrderEntryList = response.data;
        let reportEntryList = [];
        for (let i = 0; i < saleOrderEntryList.length; i++) {
          let soe = saleOrderEntryList[i];
          let workOrder = {};
          workOrder.materialId = soe.materialId;
          workOrder.materialNumber = soe.materialNumber;
          workOrder.materialUnitId = soe.unitId;
          workOrder.materialUnitName = soe.unitName;
          workOrder.quantity = soe.quantity;
          workOrder.priority = 1;
          workOrder.remark = soe.remark;
          workOrder.sourceInfo = {saleOrderEntryId: soe.id}
          workOrder.materialName = soe.materialName;
          workOrder.specification = soe.materialSpecification;
          workOrder.pickEntryList = [];
          workOrder.reportEntryList = [];
          workOrder.proStatus = 'A';
          workOrder.annex = soe.salOrder.annex;
          workOrder.extendFieldJson = soe.extendFieldJson;
          workOrder.routeId = routeId;
          //将订单信息默认携带到报工上
          reportEntryList.push({
            reportQuantity: soe.quantity,
            processId: this.process.id,
            processName: this.process.name,
            type: 'report',
            pIndex: 0,
            sort: i + 1,
            qcType: 'D',
            extendFieldJson: soe.extendFieldJson,
            reportMaterialId: soe.materialId,
            reportMaterialName: soe.materialName,
            reportSpecification: soe.materialSpecification,
            processSort: this.processSort
          })
          this.form.workOrderList.push(workOrder);
        }
        //放到第一行数据中
        this.form.workOrderList[0].reportEntryList = reportEntryList;
        //将上一道工序的报工携带到本道的领料上
        // getFrontProcessReport({salOrderEntryIds: ids,sort: this.processSort}).then(res =>{
        //   let list = res.data;
        //   for (let i = 0; i < list.length; i++) {
        //     list[i].processId=this.process.id,
        //     list[i].processName=this.process.name,
        //     list[i].processSort=this.processSort
        //   }
        //   this.form.workOrderList[0].pickEntryList=list;
        //   this.onRowClick();
        // })
        this.onRowClick();
        console.log("this.form.workOrderList===>", this.form.workOrderList)
      })
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$refs["pickForm"].validate(validB => {
            if (validB) {
              this.$refs["reportForm"].validate(validC => {
                if (validC) {
                  //this.buttonDisabled = true;
                  console.log("form=====>", this.form)
                  batchAddWorkOrderByJson(this.form).then(response => {
                    //二次确认弹框
                    this.$modal.msgSuccess("保存成功");
                    const obj = {path: "/pro/workOrder"};
                    this.$tab.closeOpenPage(obj);
                  }).catch(() => {

                    this.$message.error(response.mas)
                  });
                }
              })
            }
          })
        }
      });
    },

    /** 删除按钮操作 */
    handleDelete(index, type, row) {
      console.log("row========>", row)
      let pIndex = row.pIndex;

      if (type === "pick") {
        this.form.workOrderList[pIndex].pickEntryList.splice(index, 1);
      }
      if (type === "report") {
        this.form.workOrderList[pIndex].reportEntryList.splice(index, 1);
      }
    },

    tableRowClassName({row, rowIndex}) {
      row.index = rowIndex;
      row.sort = rowIndex + 1;

    },
    closeButton() {
      let billType = this.$route.params.billType;
      let path = "/index";
      if (billType === "saleOrder") {
        path = "/pro/saleOrder"
      }
      console.log("path========>", path)
      const obj = {path: path};
      this.$tab.closeOpenPage(obj);
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
      currentRow.stationId = '';
      //返回对应的车间的工位
      let queryParams = {workshopId: res.id, pageSize: 999999, status: 0}
      listStation(queryParams).then(response => {
        this.stationOptions = response.rows;
      });
      this.$refs["form"].validate();
    },
    machineSelectChange(value, row) {
      //获取下拉框中label的值
      let currentRow = row;
      let res = this.equipmentOptions.find((item) => {
        return item.id === value;
      });
      currentRow.machineName = res.name;

    },
    entryAddClick(type) {
      this.onRowClick()
      let index = this.selectWorkOrder.index;
      let entry = {
        pickQuantity: 0,
        reportQuantity: 0,
        processId: this.process.id,
        processName: this.process.name,
        remark: "",
        type: type,
        pIndex: index,
        sort: index + 1,
        qcType: 'D',
        processSort: this.processSort
      }
      entry.extendFieldJson = {};
      for (let i = 0; i < this.entryFieldExtendList.length; i++) {
        var field = this.entryFieldExtendList[i].field;
        entry.extendFieldJson[field] = '';
      }
      if (type === "pick") {
        entry.pickMaterialId = ""
        entry.pickMaterialName = ""
        entry.pickSpecification = ""
        this.form.workOrderList[index].pickEntryList.push(entry);
        this.$refs["pickForm"].validate();
      }
      if (type === "report") {
        entry.reportMaterialId = ""
        entry.reportMaterialName = ""
        entry.reportSpecification = ""
        entry.extendFieldJson = {}
        for (let i = 0; i < this.entryFieldExtendList.length; i++) {
          var field = this.entryFieldExtendList[i].field;
          var defaultValue = this.entryFieldExtendList[i].defaultValue;
          entry.extendFieldJson[field] = defaultValue == null ? '' : defaultValue;
        }
        this.form.workOrderList[index].reportEntryList.push(entry);
        console.log("this.form.workOrderList===>", this.form.workOrderList)
        this.$refs["reportForm"].validate();
      }
      this.$forceUpdate();
      listWarehouse({pageSize: 99999}).then(response => {
        this.warehouseList = response.rows;
      });
    },
    //物料选择弹出框
    handleSelectProduct(row) {
      this.currentRow = row;
      this.$refs.materialSelect.showFlag = true;
    },
    //接收物料弹框返回参数
    onMaterialSelect(obj) {
      let row = this.currentRow;
      if (row.type === "pick") {
        row.pickMaterialId = obj.id;
        row.pickMaterialName = obj.name;
      } else {
        row.reportMaterialId = obj.id;
        row.reportMaterialName = obj.name;
      }
      this.$refs["form"].validate();
    },
    getEquipmentList() {
      listEquipment({pageSize: 99999, status: '0'}).then(res => {
        this.equipmentOptions = res.rows;
      })
    },
    onRowClick(row, event, column) {
      //默认只有一行 begin
      row = this.form.workOrderList[0]
      ///默认只有一行 end
      this.$refs["pickForm"].validate(valid => {
        if (valid) {
          this.$refs["reportForm"].validate(validB => {
            if (validB) {
              this.selectWorkOrder = row;
              //过滤选中行的子表数据
              this.form.pickEntryList = this.form.workOrderList[0].pickEntryList
              this.form.reportEntryList = this.form.workOrderList[0].reportEntryList
            }
          })
        }
      })
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
    warehouseSelect(value, row) {
      //获取下拉框中label的值
      let currentRow = row;
      let res = this.warehouseList.find((item) => {
        return item.id === value;
      });
      currentRow.inWarehouseName = res.name;
    },
    fileDownload(row) {
      let annex = row.annex;
      this.fileList = annex.split(",");
      this.dialogVisible = true
    },
    querySearch(queryString, cb, row) {
      //输入大于等于6位之后的字符后，调用即时库存接口，
      let _this = this;
      if (queryString.length >= 6) {
        getSpecificationList({
          pageSize: 99999,
          materialId: row.pickMaterialId || row.reportMaterialId,
          materialName: row.pickMaterialName || row.reportMaterialName,
          specification: row.pickSpecification || row.reportSpecification
        }).then(response => {
          let list = response.rows
          let res = [];
          for (let i = 0; i < list.length; i++) {
            let obj = {
              "value": list[i]
            }
            res.push(obj)
          }
          _this.restaurants = res;
          console.log("_this.restaurants===>", _this.restaurants)
        });

      }
      //传递物料id+物料名称+和规格型号进行查询,返回完整规格型号+库存数量+仓库
      var restaurants = this.restaurants;
      var results = queryString ? restaurants.filter(this.createFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    handleSelect(item) {
      console.log("item===>", item);
    },
    createFilter(queryString) {
      return (restaurant) => {
        return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
      };
    },
    cleanData() {
      this.form = {
        workOrderList: [],
        reportEntryList: [],
        pickEntryList: []
      }
    }
  }
};
</script>
<style>


</style>

