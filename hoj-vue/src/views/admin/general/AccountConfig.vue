<template>
  <div>
    <el-card class="card-top">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Account_Config') }}</span>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.hduUsernameList"
            :passwordList.sync="switchConfig.hduPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="HDU"
          ></RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.cfUsernameList"
            :passwordList.sync="switchConfig.cfPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="Codeforces"
          ></RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.pojUsernameList"
            :passwordList.sync="switchConfig.pojPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="POJ"
          ></RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.atcoderUsernameList"
            :passwordList.sync="switchConfig.atcoderPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="Atcoder"
          ></RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.spojUsernameList"
            :passwordList.sync="switchConfig.spojPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="SPOJ"
          ></RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount
            :usernameList.sync="switchConfig.scpcUsernameList"
            :passwordList.sync="switchConfig.scpcPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="SCPC"
          ></RemoteJudgeAccount>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>
<script>
import api from "@/common/api";
import myMessage from "@/common/message";
const RemoteJudgeAccount = () =>
  import("@/components/admin/RemoteJudgeAccount.vue");
export default {
  name: "SystemConfig",
  components: {
    RemoteJudgeAccount,
  },
  data() {
    return {
      loading: false,
      switchConfig: {},
      labelPosition: "left",
    };
  },
  created() {
    let screenWidth = window.screen.width;
    if (screenWidth < 500) {
      this.labelPosition = "top";
    }
  },
  mounted() {
    api.admin_getSwitchConfig().then((res) => {
      this.switchConfig = res.data.data;
    });
  },
  methods: {
    saveSwitchConfig() {
      this.loading = true;
      api.admin_saveSwitchConfig(this.switchConfig).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.loading = false;
        },
        () => {
          this.loading = false;
        }
      );
    },
  },
};
</script>
<style scoped>
.switch-item-title {
  font-size: 18px;
  font-weight: bolder;
}
@media screen and (max-width: 992px) {
  .card-top {
    margin-top: 15px;
  }
}
</style>
