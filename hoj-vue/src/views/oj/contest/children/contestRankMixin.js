import api from '@/common/api'
import {
  mapGetters,
  mapState
} from 'vuex'
import {
  CONTEST_STATUS,
  buildContestRankConcernedKey,
  CONTEST_TYPE
} from '@/common/constants'
import storage from '@/common/storage';
export default {
  data() {
    return {
      isGetStatusOk: false,
    };
  },
  methods: {
    initConcernedList() {
      let key = buildContestRankConcernedKey(this.$route.params.contestID);
      this.concernedList = storage.get(key) || [];
    },
    getContestRankData(page = 1, refresh = false) {
      if(this.$refs.chart)
      {
        if (this.showChart && !refresh) {
          this.$refs.chart.showLoading({
            maskColor: 'rgba(250, 250, 250, 0.8)'
          })
        }
        let data = {
          currentPage: page,
          limit: this.limit,
          cid: this.$route.params.contestID,
          forceRefresh: this.forceUpdate ? true : false,
          removeStar: !this.showStarUser,
          concernedList: this.concernedList,
          keyword: this.keyword == null ? null : this.keyword.trim(),
          containsEnd: this.isContainsAfterContestJudge,
          time: this.selectedTime
        }
        let func = this.contestAuth ?
          "getSynchronousRank" :
          "getContestRank";
        this.loadingTable = true;

        api[func](data).then(res => {
          if (this.showChart && !refresh) {
            this.$refs.chart.hideLoading()
          }
          this.total = res.data.data.total
          if (page === 1) {
            this.applyToChart(res.data.data.records)
          }
          this.applyToTable(res.data.data.records)
        })
      }
    },
    getContestProblems() {
      this.$store.dispatch("getContestProblems").then((res) => {
        if (this.isAuthenticated) {
          let isContestProblemList = true;
          // 如果已登录，则需要查询对当前页面题目列表中各个题目的提交情况
          let pidList = [];
          if (this.contestProblems && this.contestProblems.length > 0) {
            this.$store.commit("changeContestProblems", {
              contestProblems: this.contestProblems,
            });
            for (let index = 0; index < this.contestProblems.length; index++) {
              pidList.push(this.contestProblems[index].pid);
            }
            this.isGetStatusOk = false;
            api
              .getUserProblemStatus(
                pidList,
                isContestProblemList,
                this.$route.params.contestID,
                null,
                this.isContainsAfterContestJudge
              )
              .then((res) => {
                let result = res.data.data;
                for (let index = 0; index < this.contestProblems.length; index++) {
                  this.contestProblems[index]["myStatus"] =
                    result[this.contestProblems[index].pid]["status"];
                  this.contestProblems[index]["score"] =
                    result[this.contestProblems[index].pid]["score"];
                }
                this.isGetStatusOk = true;
              });
          }
        }
      });
    },
    handleAutoRefresh(status) {
      if (status == true) {
        this.refreshFunc = setInterval(() => {
          this.$store.dispatch('getContestProblems');
          this.getContestRankData(this.page, true);
        }, 10000)
      } else {
        clearInterval(this.refreshFunc)
      }
    },
    updateConcernedList(uid, isConcerned) {
      if (isConcerned) {
        this.concernedList.push(uid);
      } else {
        var index = this.concernedList.indexOf(uid);
        if (index > -1) {
          this.concernedList.splice(index, 1);
        }
      }
      let key = buildContestRankConcernedKey(this.contestID);
      storage.set(key, this.concernedList);
      this.getContestRankData(this.page, true);
    },
    getRankShowName(rankShowName, username) {
      let finalShowName = rankShowName;
      if (rankShowName == null || rankShowName == '' || rankShowName.trim().length == 0) {
        finalShowName = username;
      }
      return finalShowName;
    }
  },
  computed: {
    ...mapGetters(['isContestAdmin', 'userInfo', "isContainsAfterContestJudge", "selectedTime"]),
    ...mapState({
      'contest': state => state.contest.contest,
      'contestProblems': state => state.contest.contestProblems
    }),
    showChart: {
      get() {
        return this.$store.state.contest.itemVisible.chart
      },
      set(value) {
        this.$store.commit('changeContestItemVisible', {
          chart: value
        })
      }
    },
    showStarUser: {
      get() {
        return !this.$store.state.contest.removeStar
      },
      set(value) {
        this.$store.commit('changeRankRemoveStar', {
          value: !value
        })
      }
    },
    showTable: {
      get() {
        return this.$store.state.contest.itemVisible.table
      },
      set(value) {
        this.$store.commit('changeContestItemVisible', {
          table: value
        })
      }
    },
    forceUpdate: {
      get() {
        return this.$store.state.contest.forceUpdate
      },
      set(value) {
        this.$store.commit('changeRankForceUpdate', {
          value: value
        })
      }
    },
    concernedList: {
      get() {
        return this.$store.state.contest.concernedList
      },
      set(value) {
        this.$store.commit('changeConcernedList', {
          value: value
        })
      }
    },
    refreshDisabled() {
      return this.contest.status == CONTEST_STATUS.ENDED
    },
    contestAuth() {
      return this.contest.auth === CONTEST_TYPE.PUBLIC_SYNCHRONOUS || this.contest.auth === CONTEST_TYPE.PRIVATE_SYNCHRONOUS;
    }
  },
  beforeDestroy() {
    clearInterval(this.refreshFunc)
  }
}