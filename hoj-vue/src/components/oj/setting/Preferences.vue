<template>
  <el-form ref="formProfile" :model="formProfile">
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <p class="section-title">{{ $t("m.Display_Preference") }}</p>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <!-- <div class="separator hidden-md-and-down"></div> -->
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <p class="section-title">{{ $t("m.Usage_Preference") }}</p>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <el-form-item :label="$t('m.UI_Language')">
            <el-select
              :value="findLabelByValue(formProfile.uiLanguage)"
              @change="changeWebLanguage"
              class="left-adjust"
              size="small"
              style="width: 100%"
            >
              <el-option
                v-for="item in webLanguages"
                :key="item"
                :value="item.value"
              >{{ item.label }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('m.UI_Theme')">
            <el-select
              :value="findLabelByValue2(formProfile.uiTheme)"
              @change="changeWebTheme"
              class="left-adjust"
              size="small"
              style="width: 100%"
            >
              <el-option v-for="item in themes" :key="item" :value="item.value">{{ item.label }}</el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <div class="separator hidden-md-and-down"></div>
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <el-form-item :label="$t('m.Code_Language')">
            <el-select
              :value="formProfile.codeLanguage"
              @change="changeCodeLanguage"
              class="left-adjust"
              size="small"
              style="width: 100%"
            >
              <el-option v-for="item in languages" :key="item" :value="item">{{ item }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('m.IDE_Theme')">
            <el-select
              :value="formProfile.ideTheme"
              @change="changeIdeTheme"
              size="small"
              style="width: 100%"
            >
              <el-option
                v-for="item in ideThemes"
                :key="item.label"
                :label="$t('m.' + item.label)"
                :value="item.value"
              >{{ $t("m." + item.label) }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('m.Code_Size')">
            <el-select
              :value="formProfile.codeSize"
              @change="changeCodeSize"
              size="small"
              style="width: 100%"
            >
              <el-option v-for="item in fontSizes" :key="item" :value="item">{{ item }}</el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-col>
    </el-row>
    <label class="el-form-item__label" style="float: none">
      {{
      $t("m.Default_Code_Template")
      }}
    </label>
    <div>
      <code-mirror v-model="formProfile.codeTemplate" class="template_code"></code-mirror>
    </div>
    <label style="float: none">{{ $t("m.Your_Code_Template") }}</label>
    <div style="text-align: center; margin-top: 10px">
      <el-button
        type="primary"
        @click="updateUserPreferences"
        :loading="loadingSaveBtn"
      >{{ $t("m.Save") }}</el-button>
    </div>
  </el-form>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import "element-ui/lib/theme-chalk/display.css";
import CodeMirror from "@/components/admin/CodeMirror.vue";
import { mapGetters } from "vuex";
import utils from "@/common/utils";

// 风格对应的样式
import "codemirror/theme/monokai.css";
import "codemirror/theme/solarized.css";
import "codemirror/theme/material.css";
import "codemirror/theme/idea.css";
import "codemirror/theme/eclipse.css";
import "codemirror/theme/base16-dark.css";
import "codemirror/theme/cobalt.css";
import "codemirror/theme/dracula.css";

// highlightSelectionMatches
import "codemirror/addon/scroll/annotatescrollbar.js";
import "codemirror/addon/search/matchesonscrollbar.js";
import "codemirror/addon/dialog/dialog.js";
import "codemirror/addon/dialog/dialog.css";
import "codemirror/addon/search/searchcursor.js";
import "codemirror/addon/search/search.js";
import "codemirror/addon/search/match-highlighter.js";

// mode
import "codemirror/mode/clike/clike.js";
import "codemirror/mode/python/python.js";
import "codemirror/mode/pascal/pascal.js"; //pascal
import "codemirror/mode/go/go.js"; //go
import "codemirror/mode/d/d.js"; //d
import "codemirror/mode/haskell/haskell.js"; //haskell
import "codemirror/mode/mllike/mllike.js"; //OCaml
import "codemirror/mode/perl/perl.js"; //perl
import "codemirror/mode/php/php.js"; //php
import "codemirror/mode/ruby/ruby.js"; //ruby
import "codemirror/mode/rust/rust.js"; //rust
import "codemirror/mode/javascript/javascript.js"; //javascript
import "codemirror/mode/fortran/fortran.js"; //fortran

// active-line.js
import "codemirror/addon/selection/active-line.js";

// foldGutter
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/fold/foldgutter.js";

import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/edit/matchtags.js";
import "codemirror/addon/edit/closetag.js";
import "codemirror/addon/edit/closebrackets.js";
import "codemirror/addon/fold/brace-fold.js";
import "codemirror/addon/fold/indent-fold.js";
import "codemirror/addon/hint/show-hint.css";
import "codemirror/addon/hint/show-hint.js";
import "codemirror/addon/hint/anyword-hint.js";
import "codemirror/addon/hint/javascript-hint";
import "codemirror/addon/selection/mark-selection.js";

export default {
  components: {
    CodeMirror,
  },
  props: {
    value: {
      type: String,
      default: "",
    },
    languages: {
      type: Array,
      default: () => {
        return ["C", "C++", "C++ 17", "C++ 20", "Java", "Python3", "Python2"];
      },
    },
    language: {
      type: String,
      default: "C",
    },
    height: {
      type: Number,
      default: 550,
    },
    theme: {
      type: String,
      default: "solarized",
    },

    tabSize: {
      type: Number,
      default: 4,
    },
    type: {
      type: String,
      default: "public",
    },
    isAuthenticated: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      loadingSaveBtn: false,
      formProfile: {
        username: "",
        uiLanguage: "",
        uiTheme: "",
        codeLanguage: "",
        codeSize: "",
        ideTheme: "",
        codeTemplate: "",
      },
      ideThemes: [
        { label: "monokai", value: "monokai" },
        { label: "solarized", value: "solarized" },
        { label: "material", value: "material" },
        { label: "idea", value: "idea" },
        { label: "eclipse", value: "eclipse" },
        { label: "base16_dark", value: "base16-dark" },
        { label: "cobalt", value: "cobalt" },
        { label: "dracula", value: "dracula" },
      ],
      fontSizes: ["12px", "14px", "16px", "18px", "20px"],
      webLanguages: [
        { value: "zh-CN", label: "简体中文" },
        { value: "en-US", label: "English" },
      ],
      themes: [
        { value: "Light", label: "亮色" },
        { value: "Dark", label: "暗色" },
      ],
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
        this.$emit("codeTemplate", this.formProfile.codeTemplate);
      }
    });
    utils.getLanguages().then((languages) => {
      let mode = {};
      languages.forEach((lang) => {
        mode[lang.name] = lang.contentType;
      });
    });
  },
  methods: {
    findLabelByValue(value) {
      // 根据选中的值返回对应的标签
      const language = this.webLanguages.find((lang) => lang.value === value);
      return language ? language.label : "";
    },
    findLabelByValue2(value) {
      // 根据选中的值返回对应的标签
      const theme = this.themes.find((lang) => lang.value === value);
      return theme ? theme.label : "";
    },
    changeWebLanguage(language) {
      // language = language === '简体中文' ? "zh-CN" : "en-US";
      this.$store.commit("changeWebLanguage", { language: language });
      this.formProfile.uiLanguage = language;
    },
    changeWebTheme(theme) {
      this.$store.commit("changeWebTheme", { theme: theme });
      this.formProfile.uiTheme = theme;
    },
    changeCodeLanguage(codelanguage) {
      this.formProfile.codeLanguage = codelanguage;
    },
    changeIdeTheme(theme) {
      this.formProfile.ideTheme = theme;
    },
    changeCodeSize(codesize) {
      this.formProfile.codeSize = codesize;
    },
    updateUserPreferences() {
      this.loadingSaveBtn = true;
      let updateData = utils.filterEmptyValue(
        Object.assign({}, this.formProfile)
      );
      api.changeUserPreferences(updateData).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.$store.dispatch("setUserInfo", res.data.data);
          this.loadingSaveBtn = false;
        },
        (_) => {
          this.loadingSaveBtn = false;
        }
      );
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
/* .language-select .el-form-item__label {
  display: block;
  text-align: center;
  margin-bottom: 10px;
} */
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
