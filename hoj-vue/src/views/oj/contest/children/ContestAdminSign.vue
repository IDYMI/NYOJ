<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Admin_Sign') }}</span>
      <div class="filter-row">
        <span>
          <el-button
            type="success"
            icon="el-icon-s-check"
            @click="updateStatus(null, 1, null)"
            size="small"
          >{{ $t("m.Sign_Accept_More") }}</el-button>
        </span>
        <span style="max-width: 100%;">
          <vxe-input
            v-model="keyword"
            :placeholder="$t('m.Enter_Signkeyword')"
            type="search"
            size="large"
            @search-click="filterByKeyword"
            @keyup.enter.native="filterByKeyword"
            style="width: 320px;"
          ></vxe-input>
        </span>
        <span>
          <el-switch
            v-model="onlyStar"
            :active-text="$t('m.Star')"
            :width="40"
            @change="filterByStar"
            :inactive-text="$t('m.All')"
          ></el-switch>
        </span>
        <span>
          <el-switch
            v-model="onlyGirls"
            :active-text="$t('m.Girls')"
            :width="40"
            @change="filterByGirls"
            :inactive-text="$t('m.All')"
          ></el-switch>
        </span>
        <span></span>
        <span>
          <el-dropdown
            @command="onStatusChange"
            placement="bottom"
            trigger="hover"
            class="drop-menu"
          >
            <span class="el-dropdown-link">
              {{ $t("m.Status") }}
              <i class="el-icon-caret-bottom"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command>{{ $t("m.All") }}</el-dropdown-item>
              <el-dropdown-item command="0">{{ $t("m.Not_Signed") }}</el-dropdown-item>
              <el-dropdown-item command="1">{{ $t("m.Sign_Accept") }}</el-dropdown-item>
              <el-dropdown-item command="2">{{ $t("m.Sign_Refuse") }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
        <div class="filter-right">
          <span>
            {{ $t('m.Auto_Refresh') }}(10s)
            <el-switch @change="handleAutoRefresh" v-model="autoRefresh"></el-switch>
          </span>
          <span>
            <el-button
              type="primary"
              @click="getContestSign(1)"
              size="small"
              icon="el-icon-refresh"
              :loading="btnLoading"
            >{{ $t('m.Refresh') }}</el-button>
          </span>
        </div>
        <p></p>
      </div>

      <vxe-table
        border="inner"
        stripe
        auto-resize
        align="center"
        :data="signList"
        ref="xTable"
        :loading="loadingTable"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Team_Name')">
          <template slot-scope="{ row }">
            <span>
              <a
                @click="getSignInfo(row.id)"
                style="color:rgb(87, 163, 243);"
              >{{ row.cname }} | {{ row.ename }}</a>
            </span>
          </template>
        </vxe-table-column>

        <vxe-table-column field="teamNames" :title="$t('m.Team_Names')" min-width="150">
          <template slot-scope="{ row }">
            <!-- 使用 replace 将 '$' 替换为空格 -->
            <span>{{ row.teamNames.replace(/\$/g, ' ') }}</span>
          </template>
        </vxe-table-column>

        <vxe-table-column field="type" :title="$t('m.Type')" min-width="150">
          <template v-slot="{ row }">
            <el-tag effect="dark" color="#19be6b" v-if="row.type == 0">
              {{
              $t('m.Formal')
              }}
            </el-tag>
            <el-tag effect="dark" color="#409eff" v-if="row.type == 1">
              {{
              $t('m.Star')
              }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column field="gmtCreate" min-width="150" :title="$t('m.Submit_Time')">
          <template v-slot="{ row }">
            <span>{{ row.gmtCreate | localtime }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="status" :title="$t('m.Status')" min-width="150">
          <template v-slot="{ row }">
            <el-tag effect="dark" color="#f90" v-if="row.status == 0">
              {{
              $t('m.Not_Signed')
              }}
            </el-tag>
            <el-tag effect="dark" color="#19be6b" v-if="row.status == 1">
              {{
              $t('m.Sign_Accept')
              }}
            </el-tag>
            <el-tag effect="dark" color="#f56c6c" v-if="row.status == 2">
              {{
              $t('m.Sign_Refuse')
              }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column field="option" :title="$t('m.Option')" min-width="150">
          <template v-slot="{ row }">
            <el-button
              type="success"
              size="small"
              icon="el-icon-circle-check"
              @click="updateStatus([row.id], 1, null)"
              round
            >{{ $t('m.Sign_Accept') }}</el-button>
            <el-button
              type="danger"
              size="small"
              icon="el-icon-circle-close"
              @click="refuseSignDialog(row.id)"
              round
            >{{ $t('m.Sign_Refuse') }}</el-button>
          </template>
        </vxe-table-column>
      </vxe-table>
      <el-dialog
        v-if="isContestAdmin"
        :title="$t('m.Refuse_Sign')"
        width="500px"
        :visible.sync="refuseDialogVisible"
        :close-on-click-modal="false"
      >
        <el-form :model="refuseSign" @submit.native.prevent>
          <el-form-item :label="$t('m.Refuse_msg')">
            <el-input v-model="refuseSign.msg" size="small" :maxlength="300"></el-input>
          </el-form-item>
          <el-form-item style="text-align:center">
            <el-button
              type="primary"
              @click="refuseSignBtn(refuseSign.signId, refuseSign.msg)"
              :loading="refuseLoading"
            >{{ $t('m.Send') }}</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>
      <el-dialog
        v-if="isContestAdmin"
        :title="$t('m.Sign_View')"
        :visible.sync="ViewDialogVisible"
        width="85%"
      >
        <el-form :model="contestSign" ref="contestSign" label-width="100px">
          <p id="teamInfo">{{$t('m.Team_Info')}}</p>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="6">
              <el-form-item prop="cname" :label="$t('m.Cname')">
                <el-input
                  v-model="contestSign.cname"
                  prefix-icon="el-icon-s-flag"
                  :placeholder="$t('m.Cname')"
                  width="80%"
                  :disabled="true"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="6">
              <el-form-item prop="ename" :label="$t('m.Ename')">
                <el-input
                  v-model="contestSign.ename"
                  :placeholder="$t('m.Ename')"
                  width="80%"
                  :disabled="true"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="6">
              <el-form-item prop="school" :label="$t('m.Team_School')">
                <el-input
                  v-model="contestSign.school"
                  :placeholder="$t('m.Team_School')"
                  width="80%"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="5">
              <el-form-item :label="$t('m.Star')">
                <el-radio-group v-model="contestSign.type">
                  <el-radio :label="false">{{$t('m.Formal')}}</el-radio>
                  <el-radio :label="true">{{$t('m.Star')}}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <p id="teamInfo">{{$t('m.Number_Info')}}</p>
          <el-row>
            <el-col v-for="(item, index) in contestSign.teamConfig" :key="index">
              <p>{{ index === 0 ? $t('m.Captain') : $t('m.Participant_') + '：' + index }}</p>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item prop="realname" :label="$t('m.Participant_Realname')">
                  <el-input
                    v-model="item.realname"
                    :placeholder="$t('m.Participant_Realname')"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item prop="course" :label="$t('m.Participant_Course')">
                  <el-input
                    v-model="item.course"
                    :placeholder="$t('m.Participant_Course')"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item prop="number" :label="$t('m.Participant_Number')">
                  <el-input
                    v-model="item.number"
                    :placeholder="$t('m.Participant_Number')"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="5">
                <el-form-item :label="$t('m.Participant_Username')">
                  <el-input
                    v-model="item.username"
                    :placeholder="$t('m.Participant_Username')"
                    width="100%"
                    :disabled="true"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item prop="phoneNumber" :label="$t('m.Participant_PhoneNumber')">
                  <el-input
                    v-model="item.phoneNumber"
                    :placeholder="$t('m.Participant_PhoneNumber')"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item prop="clothesSize" :label="$t('m.Participant_ClothesSize')">
                  <el-input
                    v-model="item.clothesSize"
                    :placeholder="$t('m.Participant_ClothesSize')+'：S/M/L/XL'"
                    width="100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="6">
                <el-form-item :label="$t('m.Gender')">
                  <el-radio-group v-model="item.gender">
                    <el-radio :label="false">{{$t('m.Female')}}</el-radio>
                    <el-radio :label="true">{{$t('m.Male')}}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-col>
          </el-row>
        </el-form>
        <p></p>
        <div style="text-align:center">
          <el-button type="primary" @click="updateContestSign()">{{ $t('m.Edit') }}</el-button>
        </div>
      </el-dialog>
    </div>

    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getContestSign"
    ></Pagination>
  </el-card>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
const Pagination = () => import("@/components/oj/common/Pagination");
import { mapGetters } from "vuex";

export default {
  name: "Contest-Sign-Admin",
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 15,
      total: 0,
      btnLoading: false,
      autoRefresh: false,
      contestID: null,
      signList: [],
      contestSign: [],
      loadingTable: false,
      selectedSigns: [],
      refuseDialogVisible: false,
      ViewDialogVisible: false,
      refuseLoading: false,
      refuseSign: {
        msg: "信息未填写完整",
        signId: null,
      },
      onlyStar: false,
      onlyGirls: false,
      keyword: null,
      searchStatus: null,
      itemRules: {},
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestSign(1);
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.page = page;
      this.getContestSign(page);
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    filterByStar() {
      this.currentChange(1);
    },
    filterByGirls() {
      this.currentChange(1);
    },
    onStatusChange(status) {
      this.searchStatus = status;
      this.page = 1;
      this.currentChange(1);
    },
    refuseSignBtn(id, msg) {
      this.refuseLoading = false;
      this.refuseDialogVisible = false;
      this.updateStatus([id], 2, msg);
    },
    refuseSignDialog(id) {
      this.refuseDialogVisible = true;
      this.refuseSign.signId = id;
    },
    getSignInfo(id) {
      let params = {
        cid: this.contestID,
        id: id,
      };
      api
        .getContestUserSign(params)
        .then((res) => {
          this.contestSign = res.data.data;
          this.ViewDialogVisible = true;
        })
        .catch(() => {
          this.contestSign = [];
          this.ViewDialogVisible = true;
        });
    },
    updateStatus(ids, status, msg) {
      if (!ids) {
        ids = this.selectedSigns;
      }
      let data = {
        ids: ids,
        cid: this.contestID.toString(),
        status: status,
        msg: msg,
      };
      if (ids.length > 0) {
        api.updateContestSignStatus(data).then((res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.selectedSigns = [];
          this.getContestSign(this.page);
        });
      } else {
        myMessage.warning(
          this.$i18n.t("m.The_number_of_signs_selected_cannot_be_empty")
        );
      }
    },
    updateContestSign() {
      let data = this.contestSign;
      api.updateContestSign(data).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
        this.getContestSign(this.page);
      });
    },

    getContestSign(page = 1) {
      this.loadingTable = true;
      this.btnLoading = true;
      let params = {
        cid: this.contestID,
        page: page,
        limit: this.limit,
      };

      if (this.onlyStar) {
        params.type = this.onlyStar;
      }
      if (this.onlyGirls) {
        params.gender = this.onlyGirls;
      }

      if (this.keyword != null) {
        params.keyword = this.keyword;
      }
      if (this.searchStatus != null) {
        params.status = this.searchStatus;
      }
      api
        .getContestSignList(params)
        .then((res) => {
          this.loadingTable = false;
          this.btnLoading = false;
          this.signList = res.data.data.records;
          this.total = res.data.data.total;
        })
        .catch(() => {
          this.btnLoading = false;
          this.loadingTable = false;
          this.signList = [];
          this.total = 0;
        });
    },
    // 改变选中的内容
    handleSelectionChange({ records }) {
      this.selectedSigns = [];
      for (let num = 0; num < records.length; num++) {
        this.selectedSigns.push(records[num].id);
      }
    },
    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      let userList = this.$refs.xTable.getCheckboxRecords();
      this.selectedSigns = [];
      for (let num = 0; num < userList.length; num++) {
        this.selectedSigns.push(userList[num].id);
      }
    },
    handleAutoRefresh() {
      if (this.autoRefresh) {
        this.refreshFunc = setInterval(() => {
          this.page = 1;
          this.getContestSign(1);
        }, 10000);
      } else {
        clearInterval(this.refreshFunc);
      }
    },
  },
  beforeDestroy() {
    clearInterval(this.refreshFunc);
  },
  computed: {
    ...mapGetters(["isContestAdmin"]),
    selectedUserIDs() {
      let ids = [];
      for (let user of this.selectedSigns) {
        ids.push(user.id);
      }
      return ids;
    },
  },
};
</script>

<style scoped>
.filter-row {
  margin-top: 10px;
}
.filter-right {
  float: right;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
  .filter-right span {
    margin-right: 2px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
  .filter-right span {
    margin-right: 20px;
  }
}

/deep/ .el-tag--dark {
  border-color: #fff;
}
</style>
