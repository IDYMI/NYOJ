<template>
  <el-form ref="formProfile" :model="formProfile" :rules="rules">
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <p class="section-title">{{ $t("m.Real_Info") }}</p>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <!-- <div class="separator hidden-md-and-down"></div> -->
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <p class="section-title">{{ $t("m.Race_Info") }}</p>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <el-form-item prop="realname" :label="$t('m.RealName')">
            <el-input v-model="formProfile.realname" />
          </el-form-item>
          <el-form-item prop="school" :label="$t('m.School')">
            <el-input v-model="formProfile.school" :maxlength="20" />
          </el-form-item>
          <el-form-item prop="course" :label="$t('m.Course')">
            <el-input v-model="formProfile.course" :maxlength="20" />
          </el-form-item>
          <el-form-item prop="number" :label="$t('m.Student_Number')">
            <el-input v-model="formProfile.number" :maxlength="20" />
          </el-form-item>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <div class="separator hidden-md-and-down"></div>
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <el-form-item prop="clothesSize" :label="$t('m.Clothes_Size')">
            <el-input
              v-model="formProfile.clothesSize"
              :placeholder="$t('m.Clothes_Size')+' ：S/M/L/XL'"
              :maxlength="10"
            />
          </el-form-item>
          <el-form-item prop="phoneNumber" :label="$t('m.Phone_Number')">
            <el-input v-model="formProfile.phoneNumber" :maxlength="20" />
          </el-form-item>
        </div>
      </el-col>
    </el-row>
    <div style="text-align: center; margin-top: 10px">
      <el-button type="primary" @click="updateUserSign" :loading="loadingSaveBtn">{{ $t("m.Save") }}</el-button>
    </div>
  </el-form>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import "element-ui/lib/theme-chalk/display.css";
import { mapGetters } from "vuex";
import utils from "@/common/utils";

export default {
  data() {
    return {
      loadingSaveBtn: false,
      formProfile: {
        username: "",
        realname: "",
        school: "南阳理工学院",
        course: "",
        number: "",
        clothesSize: "",
        phoneNumber: "",
      },
      rules: {
        realname: [
          {
            required: true,
            message: this.$i18n.t("m.RealName_Check_Required"),
            trigger: "blur",
          },
          {
            pattern: /^[\u4e00-\u9fa5]+$/,
            min: 2,
            max: 6,
            message: this.$i18n.t("m.RealName_Check_length"),
            trigger: "blur",
          },
        ],
        school: [
          {
            pattern: /^[\u4e00-\u9fa5\d]*[\u4e00-\u9fa5]+[\u4e00-\u9fa5\d]*$/,
            min: 2,
            max: 15,
            message: this.$i18n.t("m.School_Check_length"),
            trigger: "blur",
          },
        ],
        course: [
          {
            required: true,
            message: this.$i18n.t("m.Course_Check_Required"),
            trigger: "blur",
          },
          {
            pattern: /^\d+-[\u4e00-\u9fa5]+$/,
            min: 5,
            max: 20,
            message: this.$i18n.t("m.Course_Check_length"),
            trigger: "blur",
          },
        ],
        number: [
          {
            pattern: /^\d+$/,
            message: this.$i18n.t("m.StudentNumber_Check_OnlyDigits"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 15,
            message: this.$i18n.t("m.StudentNumber_Check_length"),
            trigger: "blur",
          },
        ],
        phoneNumber: [
          {
            min: 11,
            pattern:
              /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/,
            message: this.$i18n.t("m.PhoneNumber_Check_length"),
            trigger: "blur",
          },
        ],
        clothesSize: [
          {
            pattern: /^(S|M|L|XL)$/i,
            min: 1,
            max: 2,
            message: this.$i18n.t("m.ClothesSize_Check_length"),
            trigger: "blur",
          },
        ],
      },
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
      }
    });
  },
  methods: {
    updateUserSign() {
      this.$refs["formProfile"].validate((valid) => {
        if (valid) {
          this.loadingSaveBtn = true;
          let updateData = utils.filterEmptyValue(
            Object.assign({}, this.formProfile)
          );
          api.changeUserRace(updateData).then(
            (res) => {
              myMessage.success(this.$i18n.t("m.Update_Successfully"));
              this.$store.dispatch("setUserInfo", res.data.data);
              this.loadingSaveBtn = false;
            },
            (_) => {
              this.loadingSaveBtn = false;
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(["webLanguage", "token", "isAuthenticated"]),
  },
};
</script>

<style scoped>
.form-item-wrapper {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
}
.left {
  text-align: center;
}
.right {
  text-align: center;
}
/deep/ .el-input__inner {
  height: 32px;
}
/deep/ .el-form-item__label {
  font-size: 12px;
  line-height: 20px;
}
.separator {
  display: block;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  border: 1px dashed #eee;
}
.template_code {
  text-align: left;
  margin-left: 10px;
}
</style>
