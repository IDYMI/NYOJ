<template>
  <el-card class="box-card">
    <el-collapse v-model="activeName" accordion>
      <el-collapse-item name="Preferences">
        <template slot="title">
          <i class="fa fa-gear">{{ $t("m.Preferences_Setting") }}</i>
        </template>
        <component :is="Preferences"></component>
      </el-collapse-item>
      <el-collapse-item name="UserInfo">
        <template slot="title">
          <i class="fa fa-gear">{{ $t("m.UserInfo_Setting") }}</i>
        </template>
        <component :is="UserInfo"></component>
      </el-collapse-item>
      <el-collapse-item name="Account">
        <template slot="title">
          <i class="fa fa-gear">{{ $t("m.Account_Setting") }}</i>
        </template>
        <component :is="Account"></component>
      </el-collapse-item>
      <el-collapse-item name="Race">
        <template slot="title">
          <i class="fa fa-gear">{{ $t("m.Race_Setting") }}</i>
        </template>
        <component :is="Race"></component>
      </el-collapse-item>
    </el-collapse>
  </el-card>
</template>
<script>
const Account = () => import("@/components/oj/setting/Account");
const UserInfo = () => import("@/components/oj/setting/UserInfo");
const Preferences = () => import("@/components/oj/setting/Preferences");
const Race = () => import("@/components/oj/setting/Race");

export default {
  components: {
    Account,
    UserInfo,
    Preferences,
    Race,
  },
  data() {
    return {
      Account: "Account",
      UserInfo: "UserInfo",
      Preferences: "Preferences",
      Race: "Race",
      activeName: ["Preferences"],
    };
  },
};
</script>
<style scoped>
@media screen and (min-width: 1200px) {
  .box-card {
    width: 100%;
    margin: 0 auto;
  }
}
/deep/ .el-collapse-item__header {
  border-radius: 4px;
  font-size: 18px;
  color: #409eff;
}
</style>
