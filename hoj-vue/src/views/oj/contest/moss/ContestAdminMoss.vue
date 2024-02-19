<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Admin_Moss') }}</span>
      <div class="filter-row">
        <span>
          <el-button
            type="success"
            icon="el-icon-guide"
            @click="addMoss()"
            size="small"
            :loading="getMossLoading"
          >{{ $t("m.Add_Moss") }}</el-button>
        </span>
        <span style="max-width: 100%;">
          <vxe-input
            v-model="keyword"
            :placeholder="$t('m.Enter_Mosskeyword')"
            type="search"
            size="large"
            @search-click="filterByKeyword"
            @keyup.enter.native="filterByKeyword"
            style="width: 250px;"
          ></vxe-input>
        </span>
        <el-dialog
          v-if="isContestAdmin"
          :title="$t('m.Add_Moss')"
          width="500px"
          :visible.sync="mossVisible"
          :close-on-click-modal="false"
        >
          <el-form :model="addMossContains" @submit.native.prevent>
            <el-form-item>
              <el-switch
                v-model="addMossContains.excludeAdmin"
                :active-text="$t('m.Exclude_admin_submissions')"
              ></el-switch>
            </el-form-item>
            <el-form-item :label="$t('m.Plagiarism_LanguageList')">
              <template v-if="languageList.length > 0">
                <el-row>
                  <el-tag
                    :key="index"
                    v-for="(tag, index) in addMossContains.modeList"
                    closable
                    :color="'#409eff'"
                    effect="dark"
                    :disable-transitions="false"
                    @close="removeLanguage(tag)"
                    size="medium"
                    class="filter-item"
                  >{{ tag }}</el-tag>
                </el-row>
              </template>
            </el-form-item>
            <el-form-item style="text-align:center">
              <el-button
                type="primary"
                @click="mossGetBtn()"
                :loading="mossLoading"
              >{{ $t('m.Send') }}</el-button>
            </el-form-item>
          </el-form>
        </el-dialog>
        <div class="filter-right">
          <span>
            <el-dropdown
              @command="onDateChange"
              placement="bottom"
              trigger="hover"
              class="drop-menu"
            >
              <span class="el-dropdown-link">
                {{ $t("m.Plagiarism_Time") }}
                <i class="el-icon-caret-bottom"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command>{{ $t("m.All") }}</el-dropdown-item>
                <el-dropdown-item
                  v-for="result in mossDataList"
                  :key="result.gmtCreate"
                  :command="result.gmtCreate"
                >{{ result.gmtCreate }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
          <span>
            <el-dropdown
              @command="onLanguageChange"
              placement="bottom"
              trigger="hover"
              class="drop-menu"
            >
              <span class="el-dropdown-link">
                {{ $t("m.Plagiarism_Language") }}
                <i class="el-icon-caret-bottom"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command>{{ $t("m.All") }}</el-dropdown-item>
                <el-dropdown-item
                  v-for="result in uniqueLanguages"
                  :key="result"
                  :command="result"
                >{{ result }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </div>
      </div>
    </div>

    <vxe-table border="inner" stripe auto-resize align="center" :data="mossList">
      <vxe-table-column field="username" :title="$t('m.Username')" min-width="150">
        <template v-slot="{ row }">
          <span>
            <a
              @click="getUserACSubmit(row.username1)"
              style="color:rgb(87, 163, 243);"
            >{{ row.username1 }}</a>
            {{" ---- "}}
            <a
              @click="getUserACSubmit( row.username2)"
              style="color:rgb(87, 163, 243);"
            >{{ row.username2 }}</a>
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Plagiarism_Percentage')" min-width="50">
        <template v-slot="{ row }">
          <span>{{ row.percent1 + "%" + " ---- " + row.percent2 + "%" }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column field="length" :title="$t('m.Plagiarism_Length')" min-width="50"></vxe-table-column>
      <vxe-table-column field="language" :title="$t('m.Plagiarism_Language')" min-width="50"></vxe-table-column>
      <vxe-table-column field="option" :title="$t('m.Option')" min-width="100">
        <template v-slot="{ row }">
          <el-button
            type="success"
            size="small"
            icon="el-icon-check"
            @click="getContestAdminMossList(row)"
            round
          >{{ $t('m.Moss_View') }}</el-button>
          <!-- <el-button
            type="primary"
            size="small"
            icon="el-icon-download"
            @click="downloadSubmissions(row)"
            round
          >{{ $t('m.Download') }}</el-button>-->
        </template>
      </vxe-table-column>
    </vxe-table>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getContestMoss"
    ></Pagination>
  </el-card>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import utils from "@/common/utils";
const Pagination = () => import("@/components/oj/common/Pagination");
import { mapGetters } from "vuex";

export default {
  name: "Contest-Print-Admin",
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 15,
      keyword: null,
      total: 0,
      contestID: null,
      mossList: [],
      language: null,
      time: null,
      mossDataList: [],
      mossVisible: false,
      mossLoading: false,
      getMossLoading: false,
      languageList: [],
      mossResultList: [],
      addMossContains: {
        cid: null,
        modeList: [],
        excludeAdmin: true,
      },
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestMoss(1);
    this.getMossList();
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.page = page;
      this.getContestMoss(page);
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    onLanguageChange(language) {
      this.language = language;
      this.page = 1;
      this.currentChange(1);
    },
    onDateChange(time) {
      this.time = time;
      this.page = 1;
      this.currentChange(1);
    },
    getMossList() {
      let params = {
        cid: this.contestID,
        language: this.language,
      };
      api.getMossList(params).then((res) => {
        this.mossDataList = res.data.data;
      });
    },
    getContestMoss(page = 1) {
      let params = {
        cid: this.contestID,
        currentPage: page,
        limit: this.limit,
        keyword: this.keyword,
        language: this.language,
      };
      if (this.time !== null) {
        params.time = this.time;
      }
      api.getContestMossList(params).then((res) => {
        this.mossList = res.data.data.records;
        this.total = res.data.data.total;
      });
    },
    getContestLanguage() {
      let params = {
        cid: this.contestID,
        excludeAdmin: this.addMossContains.isExcludeAdmin,
      };
      this.mossLoading = true;
      api.getContestLanguage(params).then(
        (res) => {
          this.languageList = res.data.data;
          this.addMossContains.modeList = this.languageList;
          this.mossLoading = false;
        },
        () => {
          this.mossLoading = false;
        }
      );
    },
    getContestAdminMossList(row) {
      let mossID = row.id;
      // 比赛查重详情
      this.$router.push({
        name: "ContestAdminMossDetails",
        params: {
          contestID: this.contestID,
          mossID: mossID,
        },
      });
    },
    getUserACSubmit(username) {
      this.$router.push({
        path: "/contest/" + this.contestID + "/submissions",
        query: { username: username, status: 0 },
      });
    },
    mossGetBtn() {
      this.mossVisible = false;

      this.getMossLoading = true;
      this.addMossContains.cid = this.contestID;
      api.submitContestMoss(this.addMossContains).then(
        (res) => {
          this.mossResultList = res.data.data;
          // console.log(this.mossResultList);
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.getMossLoading = false;
          this.getContestMoss(1);
        },
        () => {
          this.getMossLoading = false;
          this.getContestMoss(1);
        }
      );
    },
    addMoss() {
      this.mossVisible = true;
      this.getContestLanguage();
    },
    removeLanguage(language) {
      this.addMossContains.modeList.splice(
        this.addMossContains.modeList.indexOf(language),
        1
      );
    },
  },
  computed: {
    ...mapGetters(["isContestAdmin"]),
    uniqueLanguages() {
      // Use Set to store unique language values
      const languageSet = new Set();
      this.mossDataList.forEach((result) => {
        languageSet.add(result.language);
      });
      // Convert Set back to an array
      return Array.from(languageSet);
    },
  },
  beforeDestroy() {
    clearInterval(this.refreshFunc);
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
.filter-item {
  margin-right: 1em;
  margin-top: 0.5em;
  font-size: 13px;
}
.filter-item:hover {
  cursor: pointer;
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
