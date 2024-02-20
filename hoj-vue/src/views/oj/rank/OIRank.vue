<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card :padding="10">
        <div slot="header">
          <ul class="nav-list">
            <li>
              <span class="panel-title-oi">{{ $t("m.OI_Ranklist") }}</span>
            </li>
          </ul>
        </div>
        <div class="echarts">
          <ECharts :options="options" ref="chart" auto-resize></ECharts>
        </div>
      </el-card>
      <el-card :padding="10" style="text-align: center;">
        <el-input
          :placeholder="$t('m.Rank_Search_Placeholder')"
          v-model="query.searchUser"
          @keyup.enter.native="getRankData(currentPage)"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            class="search-btn"
            @click="getRankData(currentPage)"
          ></el-button>
        </el-input>
        <div class="filter-right">
          <el-switch
            v-model="query.new"
            @change="handleOnlyNew"
            :active-text="$t('m.NewAcmer')"
            :inactive-text="$t('m.All')"
          ></el-switch>
        </div>
      </el-card>
      <vxe-table
        :data="dataRank"
        :loading="loadingTable"
        align="center"
        highlight-hover-row
        auto-resize
        :seq-config="{ seqMethod }"
        style="font-weight: 500;"
      >
        <vxe-table-column type="seq" min-width="50"></vxe-table-column>
        <vxe-table-column
          field="username"
          :title="$t('m.User')"
          min-width="200"
          show-overflow
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row.username"
              :inline="true"
              :size="25"
              color="#FFF"
              :src="row.avatar"
              class="user-avatar"
            ></avatar>
            <a
              @click="getInfoByUsername(row.uid, row.username)"
              style="color:#2d8cf0;"
            >{{ row.username }}</a>
            <span style="margin-left:2px" v-if="row.titleName">
              <el-tag effect="dark" size="small" :color="row.titleColor">{{ row.titleName }}</el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="nickname" :title="$t('m.Nickname')" width="160">
          <template v-slot="{ row }">
            <el-tag
              effect="plain"
              size="small"
              v-if="row.nickname"
              :type="nicknameColor(row.nickname)"
            >{{ row.nickname }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Score')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ row.score }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          :title="$t('m.Signature')"
          min-width="300"
          show-overflow="ellipsis"
          align="left"
        >
          <template v-slot="{ row }">
            <span v-katex class="rank-signature-body" v-if="row.signature">
              {{
              row.signature
              }}
            </span>
          </template>
        </vxe-table-column>
      </vxe-table>
      <Pagination
        :page-size.sync="limit"
        :current.sync="currentPage"
        @on-change="getRankData"
        show-sizer
        @on-page-size-change="getRankData(currentPage)"
        :layout="'jumper, prev, pager, next, sizes'"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from "@/common/api";
import utils from "@/common/utils";
import { RULE_TYPE } from "@/common/constants";
import { mapGetters } from "vuex";
import Avatar from "vue-avatar";
const Pagination = () => import("@/components/oj/common/Pagination");
export default {
  name: "acm-rank",
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      dataRank: [],
      loadingTable: false,
      screenWidth: 768,
      options: {
        tooltip: {
          trigger: "axis",
        },
        legend: {
          data: ["Score"],
        },
        grid: {
          x: "3%",
          x2: "3%",
          left: "8%",
          right: "8%",
        },
        toolbox: {
          show: true,
          feature: {
            dataView: { show: true, readOnly: true },
            magicType: { show: true, type: ["line", "bar"] },
            saveAsImage: { show: true },
          },
          right: "8%",
          top: "5%",
        },
        calculable: true,
        xAxis: [
          {
            type: "category",
            data: ["root"],
            boundaryGap: true,
            axisLabel: {
              interval: 0,
              showMinLabel: true,
              showMaxLabel: true,
              align: "center",
              formatter: (value, index) => {
                if (this.screenWidth < 768) {
                  if (this.isAuthenticated && this.userInfo.username == value) {
                    return utils.breakLongWords(value, 14);
                  } else {
                    return "";
                  }
                } else {
                  return utils.breakLongWords(value, 14);
                }
              },
              textStyle: {
                color: this.getAxisLabelColor(),
              },
            },
            axisTick: {
              alignWithLabel: true,
            },
          },
        ],
        yAxis: [
          {
            type: "value",
            axisLabel: {
              rotate: 50,
              textStyle: {
                fontSize: "12em",
                color: this.getAxisLabelColor(),
              },
            },
          },
        ],
        series: [
          {
            name: this.$i18n.t("m.Score"),
            type: "bar",
            data: [0],
            barMaxWidth: "80",
            markPoint: {
              data: [{ type: "max", name: "max" }],
            },
          },
        ],
      },
      query: {
        new: false,
        searchUser: null,
        currentPage: 1,
      },
    };
  },
  created() {
    let route = this.$route.query;
    let page = route.currentPage;
    this.currentPage = parseInt(page) || 1;
    if (page !== 1) {
      this.query.currentPage = parseInt(page);
    }
    this.query.new = route.new === "true" || false;
    this.query.searchUser = route.searchUser || null;
    this.screenWidth = window.screen.width;
    const that = this;
    window.onresize = () => {
      return (() => {
        that.screenWidth = document.documentElement.clientWidth;
      })();
    };
  },
  mounted() {
    this.getRankData(this.currentPage);
  },
  methods: {
    handleOnlyNew() {
      this.getRankData(this.currentPage);
    },
    getRankData(page) {
      this.filterByChange(page);
      let bar = this.$refs.chart;
      bar.showLoading({ maskColor: "rgba(250, 250, 250, 0.8)" });
      this.loadingTable = true;
      const type = this.query.new ? RULE_TYPE.NewOI : RULE_TYPE.OI;
      api.getUserRank(page, this.limit, type, this.searchUser).then(
        (res) => {
          if (page === 1) {
            this.changeCharts(res.data.data.records.slice(0, 10));
          }
          this.dataRank = res.data.data.records;
          this.loadingTable = false;
          bar.hideLoading();
        },
        (err) => {
          this.loadingTable = false;
          bar.hideLoading();
        }
      );
    },
    seqMethod({ rowIndex }) {
      return this.limit * (this.page - 1) + rowIndex + 1;
    },
    changeCharts(rankData) {
      let [usernames, scores] = [[], []];
      rankData.forEach((ele) => {
        usernames.push(ele.username);
        scores.push(ele.score);
      });
      this.options.xAxis[0].data = usernames;
      this.options.series[0].data = scores;
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },
    goUserACStatus(username) {
      this.$router.push({
        path: "/status",
        query: { username, status: 0 },
      });
    },
    filterByChange(page) {
      let query = Object.assign({}, this.query);
      if (page !== 1) {
        query.currentPage = this.currentPage;
      }
      this.$router.push({
        name: "OI Rank",
        query: utils.filterEmptyValue(query),
      });
    },
    getACRate(ac, total) {
      return utils.getACRate(ac, total);
    },
    nicknameColor(nickname) {
      let typeArr = ["", "success", "info", "danger", "warning"];
      let index = nickname.length % 5;
      return typeArr[index];
    },
    getAxisLabelColor() {
      return this.webTheme === "Light" ? "black" : "white";
    },
  },
  computed: {
    ...mapGetters(["isAuthenticated", "userInfo", "webTheme"]),
  },
  watch: {
    webTheme: {
      handler() {
        // 监听 webTheme 变化，并更新文字颜色
        if (this.options.xAxis && this.options.yAxis) {
          this.options.xAxis[0].axisLabel.textStyle.color =
            this.getAxisLabelColor();
          this.options.yAxis[0].axisLabel.textStyle.color =
            this.getAxisLabelColor();
        }
      },
      immediate: true,
    },
  },
};
</script>

<style scoped>
.nav-list {
  display: flex;
  /* justify-content: center;
  align-items: center; */
  list-style: none;
}

.nav-list li {
  display: inline-block;
  margin-right: 100px;
}

.selected {
  color: #409eff;
}

.panel-title-oi {
  font-size: 2em;
  font-weight: 500;
  line-height: 30px;
}

.echarts {
  margin: 0 auto;
  width: 100%;
  height: 400px;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
@media screen and (min-width: 768px) {
  .el-input-group {
    width: 50%;
  }
}
@media screen and (min-width: 1050px) {
  .el-input-group {
    width: 30%;
  }
}
.filter-right {
  float: right;
  margin-top: 15px;
}
</style>
