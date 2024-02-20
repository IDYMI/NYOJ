<template>
  <div class="contest-body">
    <el-row>
      <el-col :xs="24" :md="24" :lg="24">
        <el-card shadow>
          <div class="contest-title">
            <div slot="header">
              <span class="panel-title">{{ contest.title }}</span>
            </div>
          </div>
          <el-row style="margin-top: 10px">
            <el-col :span="14" class="text-align:left">
              <el-tooltip
                v-if="contest.auth != null && contest.auth != undefined"
                :content="$t('m.' + CONTEST_TYPE_REVERSE[contest.auth]['tips'])"
                placement="top"
              >
                <el-tag
                  :type.sync="CONTEST_TYPE_REVERSE[contest.auth]['color']"
                  effect="plain"
                  style="font-size: 13px"
                >
                  <i class="el-icon-collection-tag"></i>
                  {{ $t("m." + CONTEST_TYPE_REVERSE[contest.auth]["name"]) }}
                </el-tag>
              </el-tooltip>
              <el-tooltip
                v-if="contest.gid != null"
                :content="$t('m.Go_To_Group_Contest_List')"
                style="margin-left: 10px"
                placement="top"
              >
                <el-button size="small" type="primary" @click="toGroupContestList(contest.gid)">
                  <i class="fa fa-users"></i>
                  {{ $t("m.Group_Contest_Tag") }}
                </el-button>
              </el-tooltip>
            </el-col>
            <el-col :span="10" style="text-align: right">
              <el-button size="small" plain v-if="contest.count != null">
                <i class="el-icon-user-solid" style="color: rgb(48, 145, 242)"></i>
                x{{ contest.count }}
              </el-button>
              <template v-if="contest.type == 0">
                <el-button size="small" :type="'primary'">
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
                  <el-button size="small" :type="'warning'">
                    <i class="fa fa-trophy"></i>
                    {{ contest.type | parseContestType }}
                  </el-button>
                </el-tooltip>
              </template>
            </el-col>
          </el-row>
          <div class="contest-time">
            <el-row>
              <el-col :xs="24" :md="12" class="left">
                <p>
                  <i class="fa fa-hourglass-start" aria-hidden="true"></i>
                  {{ $t("m.StartAt") }}：{{ contest.startTime | localtime }}
                </p>
              </el-col>
              <el-col :xs="24" :md="12" class="right">
                <p>
                  <i class="fa fa-hourglass-end" aria-hidden="true"></i>
                  {{ $t("m.EndAt") }}：{{ contest.endTime | localtime }}
                </p>
              </el-col>
            </el-row>
          </div>
          <div style="width: 100%">
            <!-- <el-slider
              v-model="progressValue"
              :format-tooltip="formatTooltip"
              :step="timeStep"
            ></el-slider>-->
            <Timebar
              :progressValue="progressValue"
              :Tooltip="formatTooltip()"
              @transfer="getStopFlag"
            />
            <p></p>
          </div>
          <el-row>
            <el-col :span="24" style="text-align: center">
              <el-tag effect="dark" size="medium" :style="countdownColor">
                <i class="fa fa-circle" aria-hidden="true"></i>
                {{ countdown }}
              </el-tag>
            </el-col>
          </el-row>
          <div class="contest-config" v-if="isShowContestSetting">
            <el-popover trigger="hover" placement="left-start">
              <el-button round size="small" slot="reference">{{ $t("m.Contest_Setting") }}</el-button>
              <div class="contest-config-switches">
                <p>
                  <span>{{ $t("m.Contains_Submission_After_Contest") }}</span>
                  <el-switch v-model="isContainsAfterContestJudge"></el-switch>
                </p>
              </div>
            </el-popover>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <Announcement></Announcement>
    <div class="sub-menu">
      <el-tabs @tab-click="tabClick" v-model="route_name">
        <el-tab-pane name="ContestDetails" lazy>
          <span slot="label">
            <i class="el-icon-s-home"></i>
            &nbsp;{{ $t("m.Overview") }}
          </span>
          <!-- 判断是否需要密码验证 -->
          <el-card
            v-if="passwordFormVisible"
            class="password-form-card"
            style="text-align: center; margin-bottom: 15px"
          >
            <div slot="header">
              <span class="panel-title" style="color: #e6a23c">
                <i class="el-icon-warning">{{ $t("m.Password_Required") }}</i>
              </span>
            </div>
            <p class="password-form-tips">{{ $t("m.To_Enter_Need_Password") }}</p>
            <el-form>
              <el-input
                v-model="contestPassword"
                type="password"
                :placeholder="$t('m.Enter_the_contest_password')"
                @keydown.enter.native="checkPassword"
                style="width: 70%"
              />
              <el-button
                type="primary"
                @click="checkPassword"
                style="float: right"
              >{{ $t("m.Enter") }}</el-button>
            </el-form>
          </el-card>

          <!-- 正式赛报名窗口 -->
          <el-card
            v-if="signFormVisible"
            class="box-card"
            style="text-align: center; margin-bottom: 15px"
          >
            <div slot="header">
              <span :class="getPanelClass()" :style="getPanelStyle()">
                <i :class="getIconClass()">{{ $t('m.' + SIGN_TYPE_REVERSE[signStatus].name) }}</i>
              </span>
              <div class="filter-row">
                <span>
                  <el-button
                    type="primary"
                    @click="checkSign()"
                    size="small"
                    icon="el-icon-refresh"
                    :loading="getBtnLoading"
                  >{{ $t('m.Refresh') }}</el-button>
                </span>
              </div>
            </div>
            <el-col :span="24">
              <el-row :gutter="20">
                <el-col
                  :xxl="4"
                  :xl="6"
                  :lg="8"
                  :md="8"
                  :sm="12"
                  :xs="24"
                  v-if="!signList.length"
                  style="margin-top: 10px; margin-bottom: 10px;"
                >
                  <el-card :body-style="{ padding: '0px' }" style="border-radius: 10px;">
                    <el-empty :description="$t('m.No_Sign_Time')"></el-empty>
                  </el-card>
                </el-col>
                <el-col
                  :xxl="4"
                  :xl="6"
                  :lg="8"
                  :md="8"
                  :sm="12"
                  :xs="24"
                  v-for="(sign, index) in signList"
                  :key="index"
                  style="margin-top: 10px; margin-bottom: 10px"
                >
                  <el-row :gutter="1">
                    <el-col :span="7" style="text-align: center;">
                      <el-card
                        :body-style="{ padding: '0px' }"
                        style="border-radius: 10px; height: 170px"
                      >
                        <template v-if="sign.avatar">
                          <el-image
                            :src="sign.avatar"
                            fit="cover"
                            style="height: 135px; width: 100%"
                          ></el-image>
                        </template>
                        <template v-else>
                          <el-image
                            :src="defaultAvatar"
                            fit="cover"
                            style="height: 135px; width: 100%"
                          ></el-image>
                        </template>
                        <el-link
                          style="font-size: 16px"
                          type="primary"
                          :underline="false"
                          @click="toUserHome(sign.username)"
                        >
                          <i class="el-icon-user-solid"></i>
                          {{ sign.username }}
                        </el-link>

                        <!-- 编辑组队状态 -->
                        <template v-if="index > 0 && editRule">
                          <el-tooltip
                            effect="dark"
                            :content="$t('m.Send_Invent_Again')"
                            placement="top"
                            style="margin-left: 5px;"
                          >
                            <span
                              class="reply-opt reply-text"
                              @click="openInventDialog('radd', sign.username)"
                            >
                              <i class="iconfont el-icon-s-promotion"></i>
                              <!-- <span>{{ $t('m.Invent') }}</span> -->
                            </span>
                          </el-tooltip>
                          <el-tooltip
                            effect="dark"
                            :content="$t('m.Delete_Invent')"
                            placement="top"
                            style="margin-left: 5px;"
                          >
                            <span
                              class="reply-opt reply-delete"
                              @click="deleteInvent(sign.username)"
                            >
                              <i class="iconfont el-icon-delete"></i>
                              <!-- <span>{{ $t('m.Delete') }}</span> -->
                            </span>
                          </el-tooltip>
                        </template>
                      </el-card>
                    </el-col>
                    <el-col :span="17" :class="SIGN_TYPE_REVERSE[sign.status].name">
                      <el-card
                        :body-style="{ padding: '0px' }"
                        style="border-radius: 10px; height: 170px; text-align: left;"
                      >
                        <div slot="header" style="height: 24px">
                          <el-col :span="8">
                            <a class="sign-name">
                              <span>{{ $t('m.Participant_Realname') + " : " }}</span>
                              <br />
                              <span>{{ $t('m.Participant_School') + " : " }}</span>
                              <br />
                              <span>{{ $t('m.Participant_Course') + ": " }}</span>
                              <br />
                              <span>{{ $t('m.Participant_Number') + " : " }}</span>
                              <br />
                              <span>{{ $t('m.Participant_PhoneNumber') + " : " }}</span>
                            </a>
                          </el-col>
                          <el-col :span="1">
                            <p></p>
                          </el-col>
                          <el-col :span="8">
                            <a class="sign-name">
                              <span>{{ sign.realname }}</span>
                              <br />
                              <span>{{ sign.school }}</span>
                              <br />
                              <span>{{ sign.course }}</span>
                              <br />
                              <span>{{ sign.number }}</span>
                              <br />
                              <span>{{ sign.phoneNumber }}</span>
                            </a>
                          </el-col>
                        </div>
                      </el-card>
                    </el-col>
                  </el-row>
                </el-col>
                <el-col
                  :xxl="4"
                  :xl="6"
                  :lg="8"
                  :md="8"
                  :sm="12"
                  :xs="24"
                  style="margin-top: 10px; margin-bottom: 10px"
                >
                  <el-row :gutter="1">
                    <el-col
                      :span="24"
                      v-if="signList.length && signList.length < contest.maxParticipants && editRule"
                    >
                      <el-row :gutter="20">
                        <el-col :span="7" style="text-align: center;">
                          <el-card
                            :body-style="{ padding: '0px' }"
                            style="border-radius: 10px; height: 170px; display: flex; justify-content: center; align-items: center;"
                          >
                            <template>
                              <el-image
                                :src="defaultAvatar"
                                fit="cover"
                                style="height: 135px; width: 100%"
                              ></el-image>
                            </template>
                            <el-button
                              icon="el-icon-plus"
                              type="primary"
                              style="width: 100%; height: 100%;"
                              @click="openInventDialog('add', null)"
                            ></el-button>
                          </el-card>
                        </el-col>
                        <el-col :span="17" :class="SIGN_TYPE_REVERSE[signStatus].name">
                          <el-card
                            :body-style="{ padding: '0px' }"
                            style="border-radius: 10px; height: 170px"
                          ></el-card>
                        </el-col>
                      </el-row>
                    </el-col>
                  </el-row>
                </el-col>
              </el-row>
            </el-col>

            <!-- <Pagination
              :total="total"
              :page-size="query.limit"
              @on-change="currentChange"
              :current.sync="query.currentPage"
              @on-page-size-change="onPageSizeChange"
              style="margin-top: 10px; margin-bottom: 30px;"
              :layout="'prev, pager, next, sizes'"
              :pageSizes="[9, 15, 30, 60, 120]"
            ></Pagination>-->
            <p></p>
            <el-button
              v-if="signList.length && editRule"
              type="primary"
              @click="openSignDialog"
            >{{ $t('m.' + SIGN_TYPE_REVERSE[signStatus].action) }}</el-button>
          </el-card>
          <el-dialog
            :title="$t('m.Add_Participant')"
            width="400px"
            :visible.sync="addInventDialogVisible"
            :close-on-click-modal="false"
          >
            <el-form>
              <el-form-item :label="$t('m.Participant_Username')" required>
                <el-input v-model="userSign.toUsername" size="small"></el-input>
              </el-form-item>
              <el-form-item :label="$t('m.Invent_msg')">
                <el-input v-model="userSign.content" size="small"></el-input>
              </el-form-item>
              <el-form-item style="text-align:center">
                <el-button
                  type="primary"
                  @click="sendInvent"
                  :loading="addInventLoading"
                >{{ $t('m.Send_Invent') }}</el-button>
              </el-form-item>
            </el-form>
          </el-dialog>
          <el-dialog
            :title="$t('m.' + signTitle)"
            width="500px"
            :visible.sync="addSignDialogVisible"
            :close-on-click-modal="false"
          >
            <el-form :model="contestSign" ref="contestSign" :rules="contestSignRules">
              <el-form-item prop="cname" :label="$t('m.Cname')" required>
                <el-input v-model="contestSign.cname" size="small" :maxlength="20"></el-input>
              </el-form-item>
              <el-form-item prop="ename" :label="$t('m.Ename')" required>
                <el-input v-model="contestSign.ename" size="small" :maxlength="20"></el-input>
              </el-form-item>
              <el-form-item :label="$t('m.Team_School')">
                <el-input v-model="contestSign.school" size="small" :maxlength="20"></el-input>
              </el-form-item>
              <el-form-item>
                <el-col :span="24" style="margin-top: 10px; margin-bottom: 10px">
                  <el-col :span="12">
                    <el-switch
                      v-model="contestSign.type"
                      :active-text="$t('m.Star')"
                      :inactive-text="$t('m.Formal')"
                    ></el-switch>
                  </el-col>
                  <el-col :span="12">
                    <el-switch
                      v-model="contestSign.gender"
                      :active-text="$t('m.Girls')"
                      :inactive-text="$t('m.Formal')"
                    ></el-switch>
                  </el-col>
                </el-col>
              </el-form-item>
              <p></p>
              <p></p>
              <el-form-item style="text-align:center">
                <el-button
                  type="primary"
                  @click="sendSign"
                  :loading="addSignLoading"
                >{{ $t('m.' + signBtn) }}</el-button>
              </el-form-item>
            </el-form>
          </el-dialog>

          <el-card class="box-card">
            <Markdown :isAvoidXss="contest.gid != null" :content="contest.description"></Markdown>
          </el-card>

          <div v-if="contest.openFile">
            <box-file :isAdmin="false" :boxFileList="fileList"></box-file>
          </div>
        </el-tab-pane>

        <el-tab-pane name="ContestProblemList" lazy :disabled="contestMenuDisabled">
          <span slot="label">
            <i class="fa fa-list" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.Problem")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestProblemList'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane name="ContestSubmissionList" lazy :disabled="contestMenuDisabled">
          <span slot="label">
            <i class="el-icon-menu"></i>
            &nbsp;{{ $t("m.Status") }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestSubmissionList'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane name="ContestRank" lazy :disabled="contestMenuDisabled">
          <span slot="label">
            <i class="fa fa-bar-chart" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.NavBar_Rank")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestRank'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane name="ContestAnnouncementList" lazy :disabled="contestMenuDisabled">
          <span slot="label">
            <i class="fa fa-bullhorn" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.Announcement")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestAnnouncementList'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane
          name="ContestComment"
          lazy
          :disabled="contestMenuDisabled"
          v-if="websiteConfig.openContestComment"
        >
          <span slot="label">
            <i class="fa fa-commenting" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.Comment")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestComment'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane
          name="ContestPrint"
          lazy
          :disabled="contestMenuDisabled"
          v-if="contest.openPrint"
        >
          <span slot="label">
            <i class="el-icon-printer"></i>
            &nbsp;{{ $t("m.Print") }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestPrint'"></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane
          name="ContestAdminPrint"
          lazy
          :disabled="contestMenuDisabled"
          v-if="isContestAdmin && contest.openPrint"
        >
          <span slot="label">
            <i class="el-icon-printer"></i>
            &nbsp;{{
            $t("m.Admin_Print")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestAdminPrint'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane
          name="ContestAdminSign"
          lazy
          :disabled="contestMenuDisabled"
          v-if="isContestAdmin && contest.auth === 3"
        >
          <span slot="label">
            <i class="el-icon-edit-outline"></i>
            &nbsp;{{
            $t("m.Admin_Sign")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestAdminSign'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane
          name="ContestACInfo"
          lazy
          :disabled="contestMenuDisabled"
          v-if="showAdminHelper"
        >
          <span slot="label">
            <i class="el-icon-s-help" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.Admin_Helper")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestACInfo'"></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane
          name="ContestRejudgeAdmin"
          lazy
          :disabled="contestMenuDisabled"
          v-if="isContestAdmin"
        >
          <span slot="label">
            <i class="el-icon-refresh" aria-hidden="true"></i>
            &nbsp;{{
            $t("m.Rejudge")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestRejudgeAdmin'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane
          name="ContestAdminMoss"
          lazy
          :disabled="contestMenuDisabled"
          v-if="isContestAdmin"
        >
          <span slot="label">
            <i class="el-icon-connection"></i>
            &nbsp;{{
            $t("m.Admin_Moss")
            }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ContestAdminMoss'"></router-view>
          </transition>
        </el-tab-pane>

        <el-tab-pane name="ScrollBoard" lazy :disabled="contestMenuDisabled" v-if="showScrollBoard">
          <span slot="label">
            <i class="el-icon-video-camera-solid" aria-hidden="true"></i>
            &nbsp;{{ $t("m.ScrollBoard") }}
          </span>
          <transition name="el-zoom-in-bottom">
            <router-view v-if="route_name === 'ScrollBoard'"></router-view>
          </transition>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import time from "@/common/time";
import moment from "moment";
import api from "@/common/api";
import { mapState, mapGetters, mapActions } from "vuex";
import { addCodeBtn } from "@/common/codeblock";
import Timebar from "@/components/oj/common/Timebar.vue";
import BoxFile from "@/components/oj/common/BoxFile";

import {
  CONTEST_STATUS_REVERSE,
  CONTEST_STATUS,
  CONTEST_TYPE_REVERSE,
  RULE_TYPE,
  buildContestAnnounceKey,
} from "@/common/constants";
import myMessage from "@/common/message";
import storage from "@/common/storage";
import Markdown from "@/components/oj/common/Markdown";
import { SIGN_TYPE_REVERSE } from "@/common/constants";
const Announcement = () => import("@/views/oj/about/Switch_Announcement.vue");

export default {
  name: "ContestDetails",
  components: {
    Markdown,
    Timebar,
    BoxFile,
    Announcement,
  },
  data() {
    return {
      percentage: -1, // 点击点占总长度的距离
      sliderValue: 0,
      route_name: "contestDetails",
      timer: null,
      CONTEST_STATUS: {},
      CONTEST_STATUS_REVERSE: {},
      CONTEST_TYPE_REVERSE: {},
      RULE_TYPE: {},
      btnLoading: false,
      getBtnLoading: false,
      contestPassword: "",
      defaultAvatar: require("@/assets/default.jpg"),

      addInventDialogVisible: false,
      addInventLoading: false,

      addSignDialogVisible: false,
      addSignLoading: false,
      signTitle: "Send_Sign",
      signBtn: "Send",

      editRule: false,
      usernames: "",
      signList: [],
      signStatus: -1,
      userSign: {
        cid: this.$route.params.contestID,
        username: this.$store.getters.userInfo.username,
        toUsername: "",
        content: "听说你很强，但是我比你更强。所以一块上大分！",
      },
      contestSign: {
        cid: this.$route.params.contestID,
        cname: "",
        ename: "",
        school: "南阳理工学院",
        teamNames: "",
        type: 0,
        gender: 0,
      },
      contestSignRules: {
        cname: [
          {
            required: true,
            message: this.$i18n.t("m.Cname_Check_Required"),
            trigger: "blur",
          },
          {
            min: 1,
            max: 20,
            message: this.$i18n.t("m.TeamName_Check_Length"),
            trigger: "blur",
          },
          {
            pattern: /^[\u4e00-\u9fa5]+$/,
            message: this.$i18n.t("m.Check_Chinese"),
            trigger: "blur",
          },
        ],
        ename: [
          {
            required: true,
            message: this.$i18n.t("m.Ename_Check_Required"),
            trigger: "blur",
          },
          {
            min: 1,
            max: 20,
            message: this.$i18n.t("m.TeamName_Check_Length"),
            trigger: "blur",
          },
          {
            pattern: /[a-zA-z]$/,
            message: this.$i18n.t("m.Check_English"),
            trigger: "blur",
          },
        ],
      },
      fileList: [],
    };
  },
  mounted() {
    this.getContestFile();
  },
  created() {
    this.SIGN_TYPE_REVERSE = Object.assign({}, SIGN_TYPE_REVERSE);
    this.contestID = this.$route.params.contestID;
    this.route_name = this.$route.name;
    if (this.route_name == "ContestProblemDetails") {
      this.route_name = "ContestProblemList";
    }
    if (this.route_name == "ContestSubmissionDetails") {
      this.route_name = "ContestSubmissionList";
    }
    if (this.route_name == "ContestAdminMossDetails") {
      this.route_name = "ContestAdminMoss";
    }
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.CONTEST_STATUS = Object.assign({}, CONTEST_STATUS);
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
    this.$store.dispatch("getContest").then((res) => {
      this.changeDomTitle({ title: res.data.data.title });
      let data = res.data.data;
      let endTime = moment(data.endTime);
      // 如果当前时间还是在比赛结束前的时间，需要计算倒计时，同时开启获取比赛公告的定时器
      if (endTime.isAfter(moment(data.now))) {
        // 实时更新时间
        this.timer = setInterval(() => {
          this.$store.commit("nowAdd1s");
        }, 1000);

        // 每分钟获取一次是否存在未阅读的公告
        this.announceTimer = setInterval(() => {
          let key = buildContestAnnounceKey(this.userInfo.uid, this.contestID);
          let readAnnouncementList = storage.get(key) || [];
          let data = {
            cid: this.contestID,
            readAnnouncementList: readAnnouncementList,
          };

          api.getContestUserNotReadAnnouncement(data).then((res) => {
            let newAnnounceList = res.data.data;
            for (let i = 0; i < newAnnounceList.length; i++) {
              readAnnouncementList.push(newAnnounceList[i].id);
              this.$notify({
                title: newAnnounceList[i].title,
                message:
                  '<p style="text-align:center;"><i class="el-icon-time"> ' +
                  time.utcToLocal(newAnnounceList[i].gmtCreate) +
                  "</i></p>" +
                  '<p style="text-align:center;color:#409eff">' +
                  this.$i18n.t(
                    "m.Please_check_the_contest_announcement_for_details"
                  ) +
                  "</p>",
                type: "warning",
                dangerouslyUseHTMLString: true,
                duration: 0,
              });
            }
            storage.set(key, readAnnouncementList);
          });
        }, 60 * 1000);
      }
      this.getSign();
      this.$nextTick((_) => {
        addCodeBtn();
      });
    });
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    getStopFlag(percentage) {
      this.percentage = percentage;
    },
    getContestFile() {
      let cid = this.$route.params.contestID;
      api.getContestFile(cid).then(
        (res) => {
          let data = res.data.data;
          this.fileList = data;
        },
        (_) => {
          this.fileList = [];
        }
      );
    },
    getSign() {
      if (this.contest.auth === 3) {
        // 如果是同步赛
        let cid = this.$route.params.contestID;
        let username = this.$store.getters.userInfo.username;
        api.getSign(cid, username).then(
          (res) => {
            let data = res.data.data;
            this.signList = data.teamConfig;
            this.usernames = data.teamNames;
            this.signStatus = data.status;
            if (data.cname) {
              // 将报名信息补全
              this.contestSign = Object.assign({}, data);
            }

            // 队长是否等于当前用户
            if (username == this.signList[0].username) {
              this.editRule = true;
            }
            if (this.sendSign === 0) {
              this.signTitle = "Edit_Sign";
              this.signBtn = "Edit";
            }

            // 更新进入比赛的权限
            this.$store.commit("contestIntoAccess", {
              intoAccess: this.signStatus === 1,
            });

            if (this.signStatus === 2) {
              myMessage.warning(data.msg);
            }
          },
          (_) => {
            this.signList = [];
            this.usernames = "";
            this.signStatus = -1;
            this.editRule = false;
            this.signTitle = "Send_Sign";
            this.signBtn = "Send";
          }
        );
      }
    },
    formatTooltip(val) {
      if (this.percentage !== -1) {
        const selectedTime = this.contest.duration * this.percentage;
        this.selectedTime = parseInt(selectedTime);
        return time.secondFormat(selectedTime); // 格式化时间
      } else {
        if (this.contest.status == -1) {
          // 还未开始
          return "00:00:00";
        } else if (this.contest.status == 0) {
          return time.secondFormat(this.BeginToNowDuration);
        } else {
          return time.secondFormat(this.contest.duration);
        }
      }
    },
    checkPassword() {
      if (this.contestPassword === "") {
        myMessage.warning(this.$i18n.t("m.Enter_the_contest_password"));
        return;
      }
      this.btnLoading = true;
      api.registerContest(this.contestID + "", this.contestPassword).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Register_contest_successfully"));
          this.$store.commit("contestIntoAccess", { intoAccess: true });
          this.btnLoading = false;
        },
        (res) => {
          this.btnLoading = false;
        }
      );
    },
    checkSign() {
      this.getBtnLoading = true;
      this.getSign();
      this.getBtnLoading = false;
    },
    tabClick(tab) {
      let name = tab.name;
      if (name !== this.$route.name) {
        this.$router.push({ name: name });
      }
    },
    toGroupContestList(gid) {
      this.$router.push({
        name: "GroupContestList",
        params: {
          groupID: gid,
        },
      });
    },
    toUserHome(username) {
      this.$router.push({
        name: "UserHome",
        query: { username: username },
      });
    },
    openInventDialog(action, toUsername) {
      if (action == "add") {
        this.userSign = {
          cid: this.$route.params.contestID,
          username: this.$store.getters.userInfo.username,
          toUsername: null,
          content: "听说你很强，但是我比你更强。所以一块上大分！",
        };
      } else {
        this.userSign = {
          cid: this.$route.params.contestID,
          username: this.$store.getters.userInfo.username,
          toUsername: toUsername,
          content: "所以爱会消失嘛？燕子，回来把！",
        };
      }
      this.addInventDialogVisible = true;
    },
    openSignDialog() {
      this.addSignDialogVisible = true;
    },
    sendInvent() {
      api.addInvent(this.userSign).then(
        (res) => {
          this.addInventLoading = false;
          myMessage.success(this.$i18n.t("m.Invent_Successfully"));
          this.getSign();
          this.addInventDialogVisible = false;
        },
        (_) => {
          this.addInventLoading = false;
        }
      );
    },
    sendSign() {
      this.$refs.contestSign.validate((valid) => {
        if (valid) {
          this.contestSign.teamNames = this.usernames;
          api.addSign(this.contestSign).then(
            (res) => {
              this.addSignLoading = false;
              myMessage.success(this.$i18n.t("m.Sign_Successfully"));
              this.getSign();
              this.addSignDialogVisible = false;
            },
            (_) => {
              this.addSignLoading = false;
            }
          );
        } else {
          myMessage.error(this.$i18n.t("m.Please_check_your_Cname_or_Ename"));
        }
      });
    },
    deleteInvent(toUsername) {
      let username = this.$store.getters.userInfo.username;
      let cid = this.$route.params.contestID;
      api
        .deleteInvent(cid, username, toUsername)
        .then((res) => {
          myMessage.success(this.$i18n.t("m.Delete_successfully"));
          this.getSign();
        })
        .catch(() => {});
    },
    getPanelClass() {
      let status = this.signStatus;
      return {
        "panel-title": true,
        "color-warning": status === -1 || status === 0,
        "color-success": status === 1,
        "color-error": status === 2,
      };
    },
    getPanelStyle() {
      let status = this.signStatus;
      return {
        color:
          status === -1 || status === 0
            ? "#e6a23c"
            : status === 1
            ? "#67c23a"
            : "#ed3f14",
      };
    },
    getIconClass() {
      let status = this.signStatus;
      return {
        "el-icon-warning": status === -1 || status === 0,
        "el-icon-success": status === 1,
        "el-icon-error": status === 2,
      };
    },
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
      now: (state) => state.contest.now,
    }),
    ...mapGetters([
      "contestMenuDisabled",
      "contestRuleType",
      "contestStatus",
      "countdown",
      "isShowContestSetting",
      "BeginToNowDuration",
      "isContestAdmin",
      "ContestRealTimePermission",
      "passwordFormVisible",
      "signFormVisible",
      "userInfo",
      "websiteConfig",
    ]),
    progressValue: {
      get: function () {
        return this.$store.getters.progressValue;
      },
      set: function () {},
    },
    timeStep() {
      // 时间段平分滑条长度
      return 100 / this.contest.duration;
    },
    countdownColor() {
      if (this.contestStatus) {
        return "color:" + CONTEST_STATUS_REVERSE[this.contestStatus].color;
      }
    },
    showAdminHelper() {
      return this.isContestAdmin && this.contestRuleType === RULE_TYPE.ACM;
    },
    showScrollBoard() {
      return this.isContestAdmin && this.contestRuleType === RULE_TYPE.ACM;
    },
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
    isContainsAfterContestJudge: {
      get() {
        return this.$store.state.contest.isContainsAfterContestJudge;
      },
      set(value) {
        this.$store.commit("changeContainsAfterContestJudge", { value: value });
      },
    },
    selectedTime: {
      get() {
        return this.$store.state.contest.selectedTime;
      },
      set(value) {
        this.$store.commit("changeSelectedTime", {
          value: value,
        });
      },
    },
  },
  watch: {
    $route(newVal) {
      this.route_name = newVal.name;
      if (newVal.name == "ContestProblemDetails") {
        this.route_name = "ContestProblemList";
      } else if (this.route_name == "ContestSubmissionDetails") {
        this.route_name = "ContestSubmissionList";
      } else if (this.route_name == "ContestAdminMossDetails") {
        this.route_name = "ContestAdminMoss";
      }
      this.contestID = newVal.params.contestID;
      this.changeDomTitle({ title: this.contest.title });
    },
  },
  beforeDestroy() {
    clearInterval(this.timer);
    clearInterval(this.announceTimer);
    this.$store.commit("clearContest");
  },
};
</script>
<style scoped>
.panel-title {
  font-size: 1.5rem !important;
  font-weight: 500;
}
@media screen and (min-width: 768px) {
  .contest-time .left {
    text-align: left;
  }
  .contest-time .right {
    text-align: right;
  }
  .password-form-card {
    width: 400px;
    margin: 0 auto;
  }
}
@media screen and (max-width: 768px) {
  .contest-time .left,
  .contest-time .right {
    text-align: center;
  }
}

/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
/deep/.el-tabs__nav-wrap {
  background: #fff;
  border-radius: 3px;
}
/deep/.el-tabs--top .el-tabs__item.is-top:nth-child(2) {
  padding-left: 20px;
}
.contest-title {
  text-align: center;
}
.contest-time {
  width: 100%;
  font-size: 16px;
}
.el-tag--dark {
  border-color: #fff;
}
.el-tag {
  color: rgb(25, 190, 107);
  background: #fff;
  border: 1px solid #e9eaec;
  font-size: 18px;
}
.sub-menu {
  margin-top: 15px;
}
.password-form-tips {
  text-align: center;
  font-size: 14px;
}
.sign-name {
  font-size: 0.95rem;
  font-weight: 600;
}

.Sign_Required .sign-name {
  color: rgb(230, 162, 60);
}
.Sign_Waiting .sign-name {
  color: rgb(230, 162, 60);
}
.Sign_Refused .sign-name {
  color: rgb(245, 108, 108);
}
.Sign_Successfully .sign-name {
  color: rgb(103, 194, 58);
}

.reply-opt {
  align-items: center;
  cursor: pointer;
}
.reply-text:hover {
  color: #66b1ff;
}
.reply-delete:hover {
  color: #ff503f;
}
.filter-row {
  float: right;
}
</style>
