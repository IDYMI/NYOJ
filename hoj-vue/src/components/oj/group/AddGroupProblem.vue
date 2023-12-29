<template>
  <div>
    <el-form :model="problemId" @submit.native.prevent>
      <el-form-item :label="$t('m.Problem_ID')" required>
        <el-input v-model="problemId" size="small" @keyup.enter.native="addGroupProblem"></el-input>
      </el-form-item>
      <el-form-item style="text-align:center">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="addGroupProblem"
          :loading="loading"
          size="small"
        >{{ $t('m.Add') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import api from "@/common/api";
import mMessage from "@/common/message";
export default {
  name: "AddProblemFromGroup",
  props: {
    trainingId: {
      type: Number,
      default: null,
    },
    contestId: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      loading: false,
      problemId: "",
    };
  },
  methods: {
    addGroupProblem() {
      const validateField = (field, fieldName) => {
        if (!field) {
          myMessage.error(
            this.$i18n.t(`m.${fieldName}`) + " " + this.$i18n.t("m.is_required")
          );
          return true;
        } else {
          let value = field;
          if (value && value.indexOf("$") !== -1) {
            myMessage.error(
              this.$i18n.t(`m.${fieldName}`) +
                " " +
                this.$i18n.t("m.The_title_role")
            );
            return true;
          }
        }
        return false;
      };
      if (this.contestId) {
        this.$prompt(
          this.$i18n.t("m.Enter_The_Problem_Display_ID_in_the_Contest"),
          "Tips"
        ).then(
          ({ value }) => {
            if (validateField(value, "Contest_Display_ID")) {
              return;
            }
            api
              .addGroupContestProblemFromGroup(
                this.problemId,
                this.contestId,
                value
              )
              .then(
                (res) => {
                  mMessage.success(this.$i18n.t("m.Add_Successfully"));
                  this.loading = false;
                  this.$emit("currentChangeProblem");
                  this.$emit("handleGroupPage");
                },
                () => {}
              );
          },
          () => {}
        );
      } else {
        api
          .addGroupTrainingProblemFromGroup(this.problemId, this.trainingId)
          .then(
            (res) => {
              mMessage.success(this.$i18n.t("m.Add_Successfully"));
              this.loading = false;
              this.$emit("currentChangeProblem");
              this.$emit("handleGroupPage");
            },
            () => {}
          );
      }
    },
  },
};
</script>
