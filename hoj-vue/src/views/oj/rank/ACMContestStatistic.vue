<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card shadow :padding="10">
        <div slot="header">
          <ul class="nav-list">
            <li>
              <span class="panel-title-acm">{{ $t("m.ACM_RankStatistic") }}</span>
            </li>
          </ul>
        </div>
        <el-row>
          <el-col :xs="24" :md="8">
            <div class="contest-rank-search contest-rank-filter">
              <el-input
                :placeholder="$t('m.Contest_Rank_Search_Placeholder')"
                v-model="keyword"
                @keyup.enter.native="getContestStatisticData(page)"
              >
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  class="search-btn"
                  @click="getContestStatisticData(page)"
                ></el-button>
              </el-input>
            </div>
          </el-col>
          <el-col :xs="24" :md="16">
            <div class="contest-rank-config" v-if="isMainAdminRole">
              <el-popover trigger="hover" placement="left-start">
                <el-button round size="small" slot="reference">{{ $t("m.Contest_Rank_Setting") }}</el-button>
                <div id="switches">
                  <el-row>
                    <el-col :span="24">
                      <el-button
                        type="primary"
                        size="small"
                        @click="downloadRankCSV"
                      >{{ $t("m.Download_as_CSV") }}</el-button>
                    </el-col>
                  </el-row>
                </div>
              </el-popover>
            </div>
          </el-col>
        </el-row>

        <div>
          <vxe-table
            round
            border
            auto-resize
            size="medium"
            align="center"
            ref="ACMContestStatic"
            :data="dataRank"
            :cell-class-name="cellClassName"
          >
            <vxe-table-column
              field="rank"
              width="50"
              fixed="left"
              :title="$t('m.Contest_Rank_Seq')"
            >
              <template v-slot="{ row }">
                <template>
                  <RankBox :num="row.rank"></RankBox>
                </template>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              fixed="left"
              v-if="!isMobileView"
              min-width="300"
              :title="$t('m.User')"
              header-align="center"
              align="left"
            >
              <template v-slot="{ row }">
                <div class="contest-rank-user-box">
                  <span>
                    <avatar
                      :username="row.rankShowName"
                      :inline="true"
                      :size="37"
                      color="#FFF"
                      :src="row.avatar"
                      :title="row.rankShowName"
                    ></avatar>
                  </span>
                  <span class="contest-rank-user-info">
                    <a @click=" getUserHomeByUsername(row.uid, row.username, row.synchronous)  ">
                      <span class="contest-username" :title="row.rankShowName">
                        <span class="contest-rank-flag" v-if="row.uid == userInfo.uid">Own</span>
                        <span class="contest-rank-flag" v-if="row.rank == -1">Star</span>
                        <span class="contest-rank-flag" v-if="row.gender == 'female'">Girl</span>
                        {{ row.rankShowName }}
                      </span>
                      <span
                        class="contest-school"
                        v-if="row.school"
                        :title="row.school"
                      >{{ row.school }}</span>
                    </a>
                  </span>
                </div>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              v-else
              min-width="300"
              :title="$t('m.User')"
              header-align="center"
              align="left"
            >
              <template v-slot="{ row }">
                <div class="contest-rank-user-box">
                  <span>
                    <avatar
                      :username="row.rankShowName"
                      :inline="true"
                      :size="37"
                      color="#FFF"
                      :src="row.avatar"
                      :title="row.rankShowName"
                    ></avatar>
                  </span>
                  <span class="contest-rank-user-info">
                    <a @click=" getUserHomeByUsername(row.uid, row.username, row.synchronous) ">
                      <span class="contest-username" :title="row.rankShowName">
                        <span class="contest-rank-flag" v-if="row.uid == userInfo.uid">Own</span>
                        <span class="contest-rank-flag" v-if="row.rank == -1">Star</span>
                        <span class="contest-rank-flag" v-if="row.gender == 'female'">Girl</span>
                        {{ row.rankShowName }}
                      </span>
                      <span
                        class="contest-school"
                        v-if="row.school"
                        :title="row.school"
                      >{{ row.school }}</span>
                    </a>
                  </span>
                </div>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="realname"
              min-width="96"
              :title="$t('m.RealName')"
              show-overflow
              v-if="isContestAdmin"
            ></vxe-table-column>
            <vxe-table-column field="rating" :title="$t('m.AC')" min-width="60">
              <template v-slot="{ row }">
                <span>{{ row.ac }}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="totalTime" :title="$t('m.TotalTime')" min-width="60">
              <template v-slot="{ row }">
                <el-tooltip effect="dark" placement="top">
                  <div slot="content">{{ parseTimeToSpecific(row.totalTime) }}</div>
                  <span>{{ parseInt(row.totalTime / 60) }}</span>
                </el-tooltip>
              </template>
            </vxe-table-column>
            <vxe-table-column
              min-width="74"
              v-for="(cid, index) in contestCids"
              :key="index"
              :field="index"
            >
              <template v-slot:header>
                <span>
                  <el-tooltip effect="dark" placement="top">
                    <div slot="content">{{getContestTitle(cid)}}</div>
                    <span
                      class="emphasis"
                      :style="{ color: '#495060', cursor: 'pointer' }"
                      @click="getContestDetailsById(cid)"
                    >{{ cid }}</span>
                  </el-tooltip>
                </span>
              </template>
              <template v-slot="{ row }">
                <span v-if="row.contestInfo[cid]" class="submission-hover">
                  <span
                    v-if="row.contestInfo[cid].AC"
                    class="submission-time"
                    @click="getUserACSubmit(row.username,cid)"
                    style="color: rgb(87, 163, 243); font-weight: 600; font-size: 14px;"
                  >
                    {{row.contestInfo[cid].AC}}
                    <br />
                  </span>
                </span>
              </template>
            </vxe-table-column>
          </vxe-table>
        </div>
        <Pagination
          :total="total"
          :page-size.sync="limit"
          :page-sizes="[10, 30, 50, 100, 300, 500]"
          :current.sync="page"
          @on-change="getContestStatisticData"
          @on-page-size-change="getContestStatisticData(1)"
          :layout="'prev, pager, next, sizes'"
        ></Pagination>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import Avatar from "vue-avatar";
import { mapActions } from "vuex";
const Pagination = () => import("@/components/oj/common/Pagination");
const RankBox = () => import("@/components/oj/common/RankBox");
import time from "@/common/time";
import utils from "@/common/utils";
import api from "@/common/api";

export default {
  name: "ACMContestStatic",
  components: {
    Pagination,
    RankBox,
    Avatar,
  },
  data() {
    return {
      total: 0,
      page: 1,
      limit: 30,
      cids: this.$route.params.cids,
      dataRank: [],
      keyword: null,
      contestCids: [],
    };
  },
  mounted() {
    this.getContestStatisticData(1);
    this.getContestCids();
  },
  methods: {
    ...mapActions(["isMainAdminRole", "isContestAdmin", "userInfo"]),

    getContestCids() {
      this.contestCids = this.$route.params.cids.split("+");
    },
    getContestStatisticData(page = 1) {
      let data = {
        currentPage: page,
        limit: this.limit,
        cids: this.cids,
        keyword: this.keyword == null ? null : this.keyword.trim(),
      };
      api.getContestStatistic(data).then((res) => {
        this.total = res.data.data.total;
        this.applyToTable(res.data.data.records);
      });
    },
    getRankShowName(rankShowName, username) {
      let finalShowName = rankShowName;
      if (
        rankShowName == null ||
        rankShowName == "" ||
        rankShowName.trim().length == 0
      ) {
        finalShowName = username;
      }
      return finalShowName;
    },
    getUserACSubmit(username, contestID) {
      this.$router.push({
        path: "/contest/" + contestID + "/submissions",
        query: { username: username, status: 0 },
      });
    },
    getUserHomeByUsername(uid, username, synchronous) {
      if (!synchronous) {
        this.$router.push({
          name: "UserHome",
          query: { username: username, uid: uid },
        });
      }
    },
    getContestDetailsById(contestID) {
      this.$router.push({
        name: "ContestDetails",
        params: {
          contestID: contestID,
        },
      });
    },
    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (row.username == this.userInfo.username) {
        if (
          column.property == "rank" ||
          column.property == "rating" ||
          column.property == "totalTime" ||
          column.property == "username" ||
          column.property == "realname"
        ) {
          return "own-submit-row";
        }
      }

      if (column.property == "username" && row.userCellClassName) {
        return row.userCellClassName;
      }
    },
    getContestTitle(cid) {
      let foundTitle = null;
      this.dataRank.forEach((rank, i) => {
        let info = rank.contestInfo;
        Object.keys(info).forEach((contestID) => {
          if (contestID.toString() == cid.toString()) {
            foundTitle = info[contestID].title;
            return;
          }
        });
        if (foundTitle) {
          return;
        }
      });
      return foundTitle;
    },
    applyToTable(dataRank) {
      dataRank.forEach((rank, i) => {
        let info = rank.contestInfo;
        let cellClass = {};
        Object.keys(info).forEach((contestID) => {
          rank[contestID] = info[contestID];
        });
        rank.cellClassName = cellClass;
        if (rank.gender == "female") {
          rank.userCellClassName = "bg-female";
        }
        rank.rankShowName = this.getRankShowName(rank[false], rank.username);
      });
      this.dataRank = dataRank;
    },
    parseTimeToSpecific(totalTime) {
      return time.secondFormat(totalTime);
    },
    downloadRankCSV() {
      utils.downloadFile(
        `/api/file/download-contest-rank?cid=${
          this.$route.params.contestID
        }&forceRefresh=${this.forceUpdate ? true : false}&containEnd=${
          this.isContainsAfterContestJudge
        }`
      );
    },
  },
  computed: {
    showTable: {
      get() {
        return this.$store.state.contest.itemVisible.table;
      },
      set(value) {
        this.$store.commit("changeContestItemVisible", {
          table: value,
        });
      },
    },
    isMobileView() {
      return window.screen.width < 768;
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

label {
  display: inline-block;
  margin-right: 5px;
  text-align: center; /* 将文字居中 */
}

/* 可选样式，用于将复选框和文字垂直居中 */
input[type="checkbox"] {
  vertical-align: middle;
}
.echarts {
  margin: 20px auto;
  height: 400px;
  width: 100%;
}
/deep/.el-card__body {
  padding: 20px !important;
  padding-top: 0px !important;
}

.screen-full {
  margin-right: 8px;
}

#switches p {
  margin-top: 5px;
}
#switches p:first-child {
  margin-top: 0;
}
#switches p span {
  margin-left: 8px;
  margin-right: 4px;
}
.vxe-cell p,
.vxe-cell span {
  margin: 0;
  padding: 0;
}

/deep/.vxe-table .vxe-header--column:not(.col--ellipsis) {
  padding: 4px 0 !important;
}

/deep/.vxe-table .vxe-body--column {
  line-height: 20px !important;
  padding: 0 !important;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0 !important;
}
/deep/.vxe-body--column {
  min-width: 0;
  height: 48px;
  box-sizing: border-box;
  text-align: left;
  text-overflow: ellipsis;
  vertical-align: middle;
}
/deep/.vxe-table .vxe-cell {
  padding-left: 5px !important;
  padding-right: 5px !important;
}
.submission-time {
  font-size: 15.6px;
  font-family: Roboto, sans-serif;
}
.submission-error {
  font-weight: 400;
}
</style>
