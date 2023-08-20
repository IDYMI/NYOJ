<template>
  <div class="setting-main">
    <el-row :gutter="20">
      <!-- <el-col :sm="24" :md="10" :lg="10">
        <div class="left">
          <p class="section-title">{{ $t("m.Display_Preference") }}</p>
          <el-form ref="formProfile" :model="formProfile">
            <el-form-item :label="$t('m.UI_Language')" prop="oldPassword">
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
                  >{{ item.label }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <div class="separator hidden-md-and-down"></div>
        <p></p>
      </el-col> -->
      <!-- <el-col :sm="24" :md="10" :lg="10"> -->
      <el-col>
        <div class="right">
          <p class="section-title">{{ $t("m.Usage_Preference") }}</p>
          <el-form ref="formProfile" :model="formProfile">
            <el-form-item :label="$t('m.Code_Language')">
              <el-select
                :value="formProfile.codeLanguage"
                @change="changeCodeLanguage"
                class="left-adjust"
                size="small"
                style="width: 100%"
              >
                <el-option v-for="item in languages" :key="item" :value="item"
                  >{{ item }}
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('m.IDE_Theme')" prop="code">
              <el-select
                :value="formProfile.ideTheme"
                @change="changeIdeTheme"
                size="small"
                style="width: 100%"
              >
                <el-option
                  v-for="item in themes"
                  :key="item.label"
                  :label="$t('m.' + item.label)"
                  :value="item.value"
                  >{{ $t("m." + item.label) }}
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('m.Code_Size')" prop="password">
              <el-select
                :value="formProfile.codeSize"
                @change="changeCodeSize"
                size="small"
                style="width: 100%"
              >
                <el-option v-for="item in fontSizes" :key="item" :value="item"
                  >{{ item }}
                </el-option>
              </el-select>
            </el-form-item>
            <!-- <el-form-item :label="$t('m.Default_Code_Template')" prop="code">
              <code-mirror></code-mirror>
            </el-form-item> -->
          </el-form>
        </div>
      </el-col>
    </el-row>
    <div style="text-align: center; margin-top: 10px">
      <el-button
        type="primary"
        @click="updateUserPreferences"
        :loading="loadingSaveBtn"
        >{{ $t("m.Save") }}</el-button
      >
    </div>
  </div>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import "element-ui/lib/theme-chalk/display.css";
import CodeMirror from "@/components/admin/CodeMirror.vue";
import { mapGetters } from "vuex";
import storage from "@/common/storage";
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
        uiLanguage: "",
        codeLanguage: "",
        codeSize: "",
        ideTheme: "",
      },
      options: {
        // codemirror options
        tabSize: this.tabSize,
        mode: "text/x-csrc",
        theme: "solarized",
        // 显示行号
        lineNumbers: true,
        line: true,
        // 代码折叠
        foldGutter: true,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
        lineWrapping: true,
        // 选中文本自动高亮，及高亮方式
        styleSelectedText: true,
        showCursorWhenSelecting: true,
        highlightSelectionMatches: { showToken: /\w/, annotateScrollbar: true },
        matchBrackets: true, //括号匹配
        indentUnit: this.tabSize, //一个块（编辑语言中的含义）应缩进多少个空格
        styleActiveLine: true,
        autoCloseBrackets: true,
        autoCloseTags: true,
        hintOptions: {
          // 当匹配只有一项的时候是否自动补全
          completeSingle: false,
        },
        extraKeys: {
          "Ctrl-/": function (cm) {
            let startLine = cm.getCursor("start").line;
            let endLine = cm.getCursor("end").line;
            for (let i = startLine; i <= endLine; i++) {
              let origin = cm.getLine(i);
              if (!origin.startsWith("// ")) {
                cm.replaceRange(
                  "// " + origin,
                  { ch: 0, line: i },
                  { ch: origin.length, line: i },
                  null
                );
              } else {
                cm.replaceRange(
                  origin.substr(3),
                  { ch: 0, line: i },
                  { ch: origin.length, line: i },
                  null
                );
              }
            }
          },
          "Ctrl-;": function (cm) {
            // console.log(
            //   "current range:",
            //   cm.getCursor("start"),
            //   "-",
            //   cm.getCursor("end")
            // );

            // 获取选中区域的范围
            let from = cm.getCursor("start");
            let to = cm.getCursor("end");

            // 获取选中区域的内容并添加或去掉注释
            let content = cm.getRange(from, to);
            let note = "/*" + content.replace(/(\n|\r\n)/g, "$&") + "*/";
            if (content.startsWith("/*") && content.endsWith("*/")) {
              note = content.substr(2, content.length - 4);
            }

            // 将注释后的内容替换选中区域
            cm.replaceRange(note, from, to, null);
          },
        },
      },
      mode: {
        C: "text/x-csrc",
      },
      themes: [
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
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
      }
    });
    utils.getLanguages().then((languages) => {
      let mode = {};
      languages.forEach((lang) => {
        mode[lang.name] = lang.contentType;
      });
    });
    this.autoChangeLanguge();
    // if (this.formProfile.uiLanguage != this.webLanguage) {
    //   this.changeWebLanguage(this.formProfile.uiLanguage);
    // }
  },
  methods: {
    findLabelByValue(value) {
      // 根据选中的值返回对应的标签
      const language = this.webLanguages.find((lang) => lang.value === value);
      return language ? language.label : "";
    },
    changeWebLanguage(language) {
      // language = language === '简体中文' ? "zh-CN" : "en-US";
      this.$store.commit("changeWebLanguage", { language: language });
      this.formProfile.uiLanguage = language;
    },
    changeCodeLanguage(codelanguage) {
      this.formProfile.codeLanguage = codelanguage;
    },
    changeIdeTheme(idetheme) {
      this.formProfile.ideTheme = idetheme;
    },
    changeCodeSize(codesize) {
      this.formProfile.codeSize = codesize;
    },
    autoChangeLanguge() {
      /**
       * 语言自动转换优先级：路径参数 > 本地存储 > 浏览器自动识别
       */

      let lang = this.$route.query.l;
      if (lang) {
        lang = lang.toLowerCase();
        if (lang == "zh-cn") {
          this.$store.commit("changeWebLanguage", { language: "zh-CN" });
        } else {
          this.$store.commit("changeWebLanguage", { language: "en-US" });
        }
        return;
      }

      lang = storage.get("Web_Language");
      if (lang) {
        return;
      }

      lang = navigator.userLanguage || window.navigator.language;
      lang = lang.toLowerCase();
      if (lang == "zh-cn") {
        this.$store.commit("changeWebLanguage", { language: "zh-CN" });
      } else {
        this.$store.commit("changeWebLanguage", { language: "en-US" });
      }
    },
    updateUserPreferences() {
      // console.log(this.formProfile);
      this.loadingSaveBtn = true;
      let updateData = utils.filterEmptyValue(
        Object.assign({}, this.formProfile)
      );
      console.log(updateData);

      api.changeUserPreferences(updateData).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.$store.dispatch("setUserPreferences", res.data.data);
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
</style>
