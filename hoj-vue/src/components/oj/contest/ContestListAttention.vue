<template>
  <el-card class="contest-attention">
    <div slot="header">
      <span class="panel-title">Pay attention</span>
    </div>
    <el-row :gutter="20">
      <el-col :md="6" :xs="24" :key="index" v-for="(index,contest) in runningContestList">
        <el-card class="contest-attention-item running">
          <span class="state-phase">Contest is running</span>
          <br />
          <el-link type="primary">正规比赛</el-link>
          <br />
          <span class="countdown-text">08:05:37</span>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow>
          <div v-loading="loading">
            <p id="no-contest" v-show="contests.length == 0">
              <el-empty :description="$t('m.No_contest')"></el-empty>
            </p>
            <ol id="contest-list">
              <li v-for="contest in contests" :key="contest.title" :style="getborderColor(contest)">
                <el-row type="flex" justify="space-between" align="middle">
                  <el-col :xs="10" :sm="4" :md="3" :lg="2">
                    <template v-if="contest.type == 0">
                      <el-image
                        :src="acmSrc"
                        class="trophy"
                        style="width: 100px"
                        :preview-src-list="[acmSrc]"
                      ></el-image>
                    </template>
                    <template v-else>
                      <el-image
                        :src="oiSrc"
                        class="trophy"
                        style="width: 100px"
                        :preview-src-list="[oiSrc]"
                      ></el-image>
                    </template>
                  </el-col>
                  <el-col :xs="10" :sm="16" :md="19" :lg="20" class="contest-main">
                    <p class="title">
                      <a class="entry" @click.stop="toContest(contest)">{{ contest.title }}</a>
                      <template v-if="contest.auth == 1">
                        <i class="el-icon-lock" size="20" style="color: #d9534f"></i>
                      </template>
                      <template v-if="contest.auth == 2">
                        <i class="el-icon-lock" size="20" style="color: #f0ad4e"></i>
                      </template>
                    </p>
                    <ul class="detail">
                      <li>
                        <i class="fa fa-calendar" aria-hidden="true" style="color: #3091f2"></i>
                        {{ contest.startTime | localtime }}
                      </li>
                      <li>
                        <i class="fa fa-clock-o" aria-hidden="true" style="color: #3091f2"></i>
                        {{ getDuration(contest.startTime, contest.endTime) }}
                      </li>
                      <li>
                        <template v-if="contest.type == 0">
                          <el-button
                            size="mini"
                            round
                            :type="'primary'"
                            @click="onRuleChange(contest.type)"
                          >
                            <i class="fa fa-trophy"></i>
                            {{ contest.type | parseContestType }}
                          </el-button>
                        </template>
                        <template v-else>
                          <el-tooltip
                            :content="
                              $t('m.Contest_Rank') +
                              '：' +
                              (contest.oiRankScoreType == 'Recent'
                                ? $t(
                                    'm.Based_on_The_Recent_Score_Submitted_Of_Each_Problem'
                                  )
                                : $t(
                                    'm.Based_on_The_Highest_Score_Submitted_For_Each_Problem'
                                  ))
                            "
                            placement="top"
                          >
                            <el-button
                              size="mini"
                              round
                              :type="'warning'"
                              @click="onRuleChange(contest.type)"
                            >
                              <i class="fa fa-trophy"></i>
                              {{ contest.type | parseContestType }}
                            </el-button>
                          </el-tooltip>
                        </template>
                      </li>
                      <li>
                        <el-tooltip
                          :content="
                            $t('m.' + CONTEST_TYPE_REVERSE[contest.auth].tips)
                          "
                          placement="top"
                          effect="light"
                        >
                          <el-tag
                            :type="CONTEST_TYPE_REVERSE[contest.auth]['color']"
                            effect="plain"
                          >
                            {{
                            $t(
                            "m." +
                            CONTEST_TYPE_REVERSE[contest.auth]["name"]
                            )
                            }}
                          </el-tag>
                        </el-tooltip>
                      </li>
                      <li v-if="contest.count != null">
                        <i class="el-icon-user-solid" style="color: rgb(48, 145, 242)"></i>
                        x{{ contest.count }}
                      </li>
                      <li v-if="contest.openRank">
                        <el-tooltip
                          :content="$t('m.Contest_Outside_ScoreBoard')"
                          placement="top"
                          effect="dark"
                        >
                          <el-button
                            circle
                            size="small"
                            type="primary"
                            :disabled="
                              contest.status == CONTEST_STATUS.SCHEDULED
                            "
                            icon="el-icon-data-analysis"
                            @click="
                              toContestOutsideScoreBoard(
                                contest.id,
                                contest.type
                              )
                            "
                          ></el-button>
                        </el-tooltip>
                      </li>
                    </ul>
                  </el-col>
                  <el-col :xs="4" :sm="4" :md="2" :lg="2" style="text-align: center">
                    <el-tag
                      effect="dark"
                      :color="CONTEST_STATUS_REVERSE[contest.status]['color']"
                      size="medium"
                    >
                      <i class="fa fa-circle" aria-hidden="true"></i>
                      {{
                      $t(
                      "m." + CONTEST_STATUS_REVERSE[contest.status]["name"]
                      )
                      }}
                    </el-tag>
                  </el-col>
                </el-row>
              </li>
            </ol>
          </div>
        </el-card>
        <Pagination
          :total="total"
          :pageSize="limit"
          @on-change="onCurrentPageChange"
          :current.sync="currentPage"
        ></Pagination>
      </el-col>
    </el-row>
  </el-card>
</template>
<script>
export default {
  name: "ContestListAttention",
  props: {
    runningContestList: {
      default: [1, 2],
      type: Array,
    },
    scheduledContestList: {
      default: [1, 2, 3],
      type: Array,
    },
  },
};
</script>
<style scoped>
.contest-attention {
  margin-bottom: 20px;
}
.contest-attention-item {
  text-align: center;
  margin-bottom: 10px;
}
.contest-attention-item.running {
  border-color: rgb(25, 190, 107);
}
.contest-attention-item.scheduled {
  border-color: #f90;
}

.contest-attention-item .state-phase {
  font-size: 1.5rem;
  font-weight: 700;
}

.contest-attention-item.running .state-phase {
  color: #5eb95e;
}

.contest-attention-item.scheduled .state-phase {
  color: #f37b1d;
}

.contest-attention-item .countdown-text {
  color: #777;
}
/deep/.el-card__header {
  padding: 10px 20px;
}
/deep/.el-card__body {
  padding: 10px 20px;
}
</style>