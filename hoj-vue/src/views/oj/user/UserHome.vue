<template>
  <div class="container1" v-loading="loading">
    <div class="avatar-container">
      <avatar
        :username="profile.username"
        :inline="true"
        :size="130"
        color="#FFF"
        :src="profile.avatar"
      ></avatar>
    </div>
    <el-card class="box-card">
      <div class="recent-login">
        <el-tooltip :content="profile.recentLoginTime | localtime" placement="top">
          <el-tag type="success" effect="plain" size="medium">
            <i class="fa fa-circle">
              {{ $t("m.Recent_login_time")
              }}{{ profile.recentLoginTime | fromNow }}
            </i>
          </el-tag>
        </el-tooltip>
      </div>
      <div class="user-info">
        <p>
          <span class="emphasis">
            <i class="fa fa-user-circle-o" aria-hidden="true"></i>
            {{ profile.username }}
          </span>
          <span class="gender-male male" v-if="profile.gender == 'male'">
            <i class="fa fa-mars"></i>
          </span>
          <span class="gender-male female" v-else-if="profile.gender == 'female'">
            <i class="fa fa-venus"></i>
          </span>
        </p>
        <p v-if="profile.titleName">
          <span>
            <el-tag effect="dark" size="small" :color="profile.titleColor">{{ profile.titleName }}</el-tag>
          </span>
        </p>
        <p v-if="profile.nickname">
          <span>
            <el-tag
              effect="plain"
              size="small"
              :type="nicknameColor(profile.nickname)"
            >{{ profile.nickname }}</el-tag>
          </span>
        </p>
        <span class="default-info" v-if="profile.school">
          <i class="fa fa-graduation-cap" aria-hidden="true"></i>
          {{ profile.school }}
        </span>
        <span id="icons">
          <a :href="profile.github" v-if="profile.github" class="icon" target="_blank">
            <i class="fa fa-github">{{ $t("m.Github") }}</i>
          </a>
          <a :href="profile.blog" v-if="profile.blog" class="icon" target="_blank">
            <i class="fa fa-share-alt-square">{{ $t("m.Blog") }}</i>
          </a>
        </span>
        <hr id="split" />
        <el-row :gutter="12">
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="submission">
              <p>
                <i class="fa fa-th" aria-hidden="true"></i>
                {{ $t("m.UserHome_Submissions") }}
              </p>
              <p class="data-number">{{ profile.total }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="solved">
              <p>
                <i class="fa fa-check-circle" aria-hidden="true"></i>
                {{ $t("m.UserHome_Solved") }}
              </p>
              <p class="data-number">{{ profile.solvedList.length }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="score">
              <p>
                <i class="fa fa-star" aria-hidden="true"></i>
                {{ $t("m.UserHome_Contests") }}
              </p>
              <p class="data-number">{{ profile.contestPidList.length }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="rating">
              <p>
                <i class="fa fa-user-secret" aria-hidden="true"></i>
                {{ $t("m.UserHome_Score") }}
              </p>
              <p class="data-number">{{ getSumScore(profile.scoreList) }}</p>
            </el-card>
          </el-col>
        </el-row>
        <el-card style="margin-top: 1rem" v-if="loadingCalendarHeatmap">
          <div class="card-title">
            <i class="el-icon-data-analysis" style="color: #409eff"></i>
            {{ $t("m.Thermal_energy_table_submitted_in_the_last_year") }}
          </div>
          <calendar-heatmap
            :values="calendarHeatmapValue"
            :end-date="calendarHeatmapEndDate"
            :tooltipUnit="$t('m.Calendar_Tooltip_Uint')"
            :locale="calendarHeatLocale"
            :range-color="[
              'rgb(218, 226, 239)',
              '#9be9a8',
              '#40c463',
              '#30a14e',
              '#216e39',
            ]"
          ></calendar-heatmap>
        </el-card>
        <el-card style="margin-top: 1rem" v-if="this.options.series.length">
          <div class="card-title">
            <i class="el-icon-data-line" style="color: #409eff"></i>
            {{ $t("m.Ended_contests_ranking_changes") }}
          </div>
          <div class="echarts">
            <ECharts :options="options" ref="chart" :autoresize="true" @click="gotoEcharts"></ECharts>
          </div>
        </el-card>
        <el-tabs type="card" style="margin-top: 1rem">
          <el-tab-pane :label="$t('m.Personal_Profile')">
            <div class="signature-body">
              <Markdown v-if="profile.signature" :isAvoidXss="true" :content="profile.signature"></Markdown>
              <div class="markdown-body" v-else>
                <p>{{ $t("m.Not_set_yet") }}</p>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('m.UserHome_Solved_Problems')">
            <div id="problems">
              <el-card class="level-card" v-if="profile.solvedGroupByDifficulty != null">
                <div class="card-title" style="font-size: 1rem">
                  <i class="el-icon-set-up" style="color: #409eff"></i>
                  {{ $t("m.Difficulty_Statistics") }}
                </div>
                <el-collapse accordion>
                  <el-collapse-item v-for="(level, key) in PROBLEM_LEVEL" :key="key">
                    <template slot="title">
                      <div style="width: 100%; text-align: left">
                        <el-tag
                          effect="dark"
                          :style="getLevelColor(key)"
                          size="medium"
                        >{{ getLevelName(key) }}</el-tag>
                        <span class="card-p-count">
                          {{
                          getProblemListCount(
                          profile.solvedGroupByDifficulty[key]
                          )
                          }}
                          {{ $t("m.Problems") }}
                        </span>
                      </div>
                    </template>
                    <div class="btns">
                      <div
                        class="problem-btn"
                        v-for="(value, index) in profile
                          .solvedGroupByDifficulty[key]"
                        :key="index"
                      >
                        <el-button
                          round
                          :style="getLevelColor(key)"
                          @click="goProblem(value.problemId)"
                          size="small"
                        >{{ value.problemId }}</el-button>
                      </div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </el-card>

              <template v-if="profile.solvedList.length">
                <el-divider>
                  <i class="el-icon-circle-check"></i>
                </el-divider>
                <div>
                  {{ $t("m.List_Solved_Problems") }}
                  <el-button
                    type="primary"
                    icon="el-icon-refresh"
                    circle
                    size="mini"
                    @click="freshProblemDisplayID"
                  ></el-button>
                </div>
                <div class="btns">
                  <div class="problem-btn" v-for="problemID of profile.solvedList" :key="problemID">
                    <el-button round @click="goProblem(problemID)" size="small">{{ problemID }}</el-button>
                  </div>
                </div>
              </template>
              <template v-else>
                <p>{{ $t("m.UserHome_Not_Data") }}</p>
              </template>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('m.UserHome_Participated_Contests')">
            <div id="problems">
              <template v-if="profile.contestPidList.length">
                <!-- <el-divider><i class="el-icon-circle-check"></i></el-divider> -->
                <div>
                  {{ $t("m.List_Participated_Contests") }}
                  <el-button
                    type="primary"
                    icon="el-icon-refresh"
                    circle
                    size="mini"
                    @click="freshContestDisplayID"
                  ></el-button>
                </div>
                <div class="btns">
                  <div class="problem-btn" v-for="pid of profile.contestPidList" :key="pid">
                    <el-button round @click="goContest(pid)" size="small">
                      {{
                      pid
                      }}
                    </el-button>
                  </div>
                </div>
              </template>
              <template v-else>
                <p>{{ $t("m.UserHome_Not_Contest") }}</p>
              </template>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>
<script>
import { mapActions } from "vuex";
import api from "@/common/api";
import myMessage from "@/common/message";
import { addCodeBtn } from "@/common/codeblock";
import Avatar from "vue-avatar";
import "vue-calendar-heatmap/dist/vue-calendar-heatmap.css";
import { CalendarHeatmap } from "vue-calendar-heatmap";
import { PROBLEM_LEVEL } from "@/common/constants";
import utils from "@/common/utils";
import Markdown from "@/components/oj/common/Markdown";
import moment from "moment";
export default {
  components: {
    Avatar,
    CalendarHeatmap,
    Markdown,
  },
  data() {
    return {
      profile: {
        username: "",
        nickname: "",
        gender: "",
        avatar: "",
        school: "",
        signature: "",
        total: 0,
        rating: 0,
        score: 0,
        solvedList: [],
        contestPidList: [],
        dataList: [], // 日期对应的比赛名次数据列表
        solvedGroupByDifficulty: null,
        calendarHeatLocale: null,
        calendarHeatmapValue: [],
        calendarHeatmapEndDate: "",
        loadingCalendarHeatmap: false,
        loading: false,
      },
      PROBLEM_LEVEL: {},
      options: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "none",
            axis: "x",
          },
          formatter: function (params) {
            var long_title = params[0].data.title;
            var title = "";
            for (var i = 0, s; (s = long_title[i++]); ) {
              //遍历字符串数组
              title += s;
              if (!(i % 16)) title += "<br>"; //按需要求余
            }

            // console.log(params);
            return (
              "Contest：" +
              long_title +
              "<br>Rank：" +
              params[0].data.value +
              "<br>Time：" +
              params[0].name
            );
          },
        },

        grid: {
          x: 80,
          x2: 100,
          left: "5%", //设置canvas图距左的距离
          top: "5%",
          right: "5%",
          bottom: "15%",
        },
        xAxis: {
          type: "category",
          data: [], // 把时间组成的数组接过来，放在x轴上
          boundaryGap: true,
          axisTick: {
            show: false, // 不显示坐标轴刻度线
          },
          axisLine: {
            show: false, // 不显示坐标轴线
          },
        },
        yAxis: {
          type: "value",
          inverse: true,
          minInterval: 1,
          axisTick: {
            show: false, // 不显示坐标轴刻度线
          },
          axisLine: {
            show: false, // 不显示坐标轴线
          },
        },
        series: [],
      },
    };
  },
  created() {
    const uid = this.$route.query.uid;
    const username = this.$route.query.username;
    api.getUserCalendarHeatmap(uid, username).then((res) => {
      this.calendarHeatmapValue = res.data.data.dataList;
      this.calendarHeatmapEndDate = res.data.data.endDate;
      this.loadingCalendarHeatmap = true;
    });
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
  },
  mounted() {
    this.calendarHeatLocale = {
      months: [
        this.$i18n.t("m.Jan"),
        this.$i18n.t("m.Feb"),
        this.$i18n.t("m.Mar"),
        this.$i18n.t("m.Apr"),
        this.$i18n.t("m.May"),
        this.$i18n.t("m.Jun"),
        this.$i18n.t("m.Jul"),
        this.$i18n.t("m.Aug"),
        this.$i18n.t("m.Sep"),
        this.$i18n.t("m.Oct"),
        this.$i18n.t("m.Nov"),
        this.$i18n.t("m.Dec"),
      ],
      days: [
        this.$i18n.t("m.Sun"),
        this.$i18n.t("m.Mon"),
        this.$i18n.t("m.Tue"),
        this.$i18n.t("m.Wed"),
        this.$i18n.t("m.Thu"),
        this.$i18n.t("m.Fri"),
        this.$i18n.t("m.Sat"),
      ],
      on: this.$i18n.t("m.on"),
      less: this.$i18n.t("m.Less"),
      more: this.$i18n.t("m.More"),
    };
    this.init();
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    init() {
      const uid = this.$route.query.uid;
      const username = this.$route.query.username;
      this.loading = true;
      api.getUserInfo(uid, username).then(
        (res) => {
          this.changeDomTitle({ title: res.data.username });
          this.profile = res.data.data;
          let dataList = this.profile.dataList;
          let len = dataList.length;

          let [times, ranks, seriesData] = [[], [], []];

          for (let i = 0; i < len; i++) {
            let contest = dataList[i];
            let time = moment(contest["date"]).format("hh:mm MM-DD");
            let title = contest["title"];
            let rank = contest["rank"];
            let cid = contest["cid"];
            ranks.push({ value: rank, title: title, cid: cid });
            times.push(time);
          }
          if (ranks.length) {
            seriesData.push({
              name: username,
              type: "line",
              data: ranks,
              emphasis: {
                focus: "series",
              },
            });
            this.options.series = seriesData;
            this.options.xAxis.data = times;
            // myMessage.success(seriesData);
          }
          this.$nextTick((_) => {
            addCodeBtn();
          });
          this.loading = false;
        },
        (_) => {
          this.loading = false;
        }
      );
    },
    gotoEcharts(params) {
      let cid = params.data.cid;
      this.getContestRank(cid);
    },
    getContestRank(cid) {
      this.$router.push({
        name: "ContestRank",
        params: { contestID: cid },
      });
    },
    goContest(cid) {
      this.$router.push({
        name: "ContestDetails",
        params: { contestID: cid },
      });
    },
    goProblem(problemID) {
      this.$router.push({
        name: "ProblemDetails",
        params: { problemID: problemID },
      });
    },
    freshProblemDisplayID() {
      this.init();
      myMessage.success(this.$i18n.t("m.Update_Successfully"));
    },
    freshContestDisplayID() {
      this.init();
      myMessage.success(this.$i18n.t("m.Update_Successfully"));
    },
    getSumScore(scoreList) {
      if (scoreList) {
        var sum = 0;
        for (let i = 0; i < scoreList.length; i++) {
          sum += scoreList[i];
        }
        return sum;
      }
    },
    nicknameColor(nickname) {
      let typeArr = ["", "success", "info", "danger", "warning"];
      let index = nickname.length % 5;
      return typeArr[index];
    },
    getLevelColor(difficulty) {
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    getProblemListCount(list) {
      if (!list) {
        return 0;
      } else {
        return list.length;
      }
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    "$store.state.language"(newVal, oldVal) {
      // console.log(newVal, oldVal);
      this.calendarHeatLocale = {
        months: [
          this.$i18n.t("m.Jan"),
          this.$i18n.t("m.Feb"),
          this.$i18n.t("m.Mar"),
          this.$i18n.t("m.Apr"),
          this.$i18n.t("m.May"),
          this.$i18n.t("m.Jun"),
          this.$i18n.t("m.Jul"),
          this.$i18n.t("m.Aug"),
          this.$i18n.t("m.Sep"),
          this.$i18n.t("m.Oct"),
          this.$i18n.t("m.Nov"),
          this.$i18n.t("m.Dec"),
        ],
        days: [
          this.$i18n.t("m.Sun"),
          this.$i18n.t("m.Mon"),
          this.$i18n.t("m.Tue"),
          this.$i18n.t("m.Wed"),
          this.$i18n.t("m.Thu"),
          this.$i18n.t("m.Fri"),
          this.$i18n.t("m.Sat"),
        ],
        on: this.$i18n.t("m.on"),
        less: this.$i18n.t("m.Less"),
        more: this.$i18n.t("m.More"),
      };
    },
  },
};
</script>

<style scoped>
.echarts {
  margin: 20px auto;
  height: 240px;
  width: 100%;
}
.submission {
  background: skyblue;
  color: #fff;
  font-size: 14px;
}
.solved {
  background: #67c23a;
  color: #fff;
  font-size: 14px;
}
.score {
  background: #e6a23c;
  color: #fff;
  font-size: 14px;
}
.rating {
  background: #dd6161;
  color: #fff;
  font-size: 14px;
}
.default-info {
  font-size: 13px;
  padding-right: 5px;
}
.data-number {
  font-size: 20px;
  font-weight: 600;
}
.container1 {
  width: 100%;
  text-align: center;
  box-sizing: border-box;
  border: 1px solid #ebeef5;
}
.container1 p {
  margin-top: 8px;
  margin-bottom: 8px;
}

@media screen and (max-width: 1080px) {
  .container1 {
    position: relative;
    width: 100%;
    margin-top: 80px;
    text-align: center;
  }
  .container1 .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -85px;
  }

  .container1 .recent-login {
    text-align: center;
    margin-top: 30px;
  }
}

@media screen and (min-width: 1080px) {
  .container1 {
    position: relative;
    width: 100%;
    margin-top: 100px;
    text-align: center;
  }
  .container1 .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -7%;
  }
  .container1 .recent-login {
    position: absolute;
    right: 1rem;
    top: 0.5rem;
  }
  .container1 .user-info {
    margin-top: 50px;
  }
}
.container1 .avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  box-shadow: 0 1px 1px 0;
}

.container1 .emphasis {
  font-size: 20px;
  font-weight: 600;
}
#problems {
  padding-left: 30px;
  padding-right: 30px;
  font-size: 18px;
}
.level-card {
  width: calc(45% - 0.5em);
  margin: 1rem auto;
}
@media (max-width: 768px) {
  .level-card {
    margin: 1em 0;
    width: 100%;
  }
  #problems {
    padding-left: 0px;
    padding-right: 0px;
  }
}
.card-p-count {
  float: right;
  font-size: 1.1em;
  font-weight: bolder;
}
.btns {
  margin-top: 15px;
}
.problem-btn {
  display: inline-block;
  margin: 5px;
}

#icons .icon {
  font-size: 13px !important;
  padding: 0 10px;
  color: #2196f3;
}
.signature-body {
  background: #fff;
  overflow: hidden;
  width: 100%;
  padding: 10px 10px;
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}
.gender-male {
  font-size: 16px;
  margin-left: 5px;
  color: white;
  border-radius: 4px;
  padding: 2px;
}
.male {
  background-color: #409eff;
}
.female {
  background-color: pink;
}
.card-title {
  font-size: 1.2rem;
  font-weight: 500;
  align-items: center;
  text-align: left;
  margin-bottom: 10px;
}
/deep/.vch__day__square {
  cursor: pointer !important;
  transition: all 0.2s ease-in-out !important;
}
/deep/.vch__day__square:hover {
  height: 11px !important;
  width: 11px !important;
}

/deep/svg.vch__wrapper rect.vch__day__square:hover {
  stroke: rgb(115, 179, 243) !important;
}

/deep/svg.vch__wrapper .vch__months__labels__wrapper text.vch__month__label,
/deep/svg.vch__wrapper .vch__days__labels__wrapper text.vch__day__label,
/deep/svg.vch__wrapper .vch__legend__wrapper text {
  font-size: 0.5rem !important;
  font-weight: 600 !important;
}

/deep/rect {
  rx: 2;
  ry: 2;
}
</style>
