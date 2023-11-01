<template>
  <codemirror v-model="currentValue" :options="options" ref="editor"></codemirror>
</template>
<script>
import { codemirror, CodeMirror } from "vue-codemirror-lite";
import "codemirror/mode/javascript/javascript";
import "codemirror/mode/clike/clike.js";
import "codemirror/mode/python/python.js";
import "codemirror/theme/solarized.css";
// active-line.js
import "codemirror/addon/selection/active-line.js";
// foldGutter
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/fold/foldgutter.js";
import "codemirror/addon/fold/brace-fold.js";
import "codemirror/addon/fold/indent-fold.js";
import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/edit/matchtags.js";
import "codemirror/addon/edit/closetag.js";
import "codemirror/addon/edit/closebrackets.js";
import "codemirror/addon/hint/show-hint.css";
import "codemirror/addon/hint/show-hint.js";
import "codemirror/addon/hint/anyword-hint.js";

export default {
  name: "CodeMirror",
  data() {
    return {
      currentValue: "",
      options: {
        // codemirror options
        tabSize: 4,
        mode: "text/x-csrc",
        theme: "solarized",
        // 显示行号
        lineNumbers: true,
        line: true,
        // 代码折叠
        foldGutter: true,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
        lineWrapping: true,
        // 自动对焦
        autofocus: true,
        // 选中文本自动高亮，及高亮方式
        styleSelectedText: true,
        showCursorWhenSelecting: true,
        highlightSelectionMatches: { showToken: /\w/, annotateScrollbar: true },
        matchBrackets: true, //括号匹配
        indentUnit: 4, //一个块（编辑语言中的含义）应缩进多少个空格
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
          // "Alt-Shift-f": function (cm) {
          //   CodeMirror.commands["selectAll"](cm);
          //   var range = {
          //     from: editor.getCursor(true),
          //     to: editor.getCursor(false)
          // }
          //   cm.autoFormatRange(range.from, range.to);
          //   cm.commentRange(false, range.from, range.to);
          // },
        },
      },
    };
  },
  components: {
    codemirror,
  },
  props: {
    value: {
      type: String,
      default: "",
    },
    mode: {
      type: String,
      default: "text/x-c++src",
    },
  },
  mounted() {
    this.currentValue = this.value;
    this.$refs.editor.editor.setOption("mode", this.mode);
    this.$refs.editor.editor.on("inputRead", (instance, changeObj) => {
      if (/\w|\./g.test(changeObj.text[0]) && changeObj.origin !== "complete") {
        instance.showHint({
          hint: CodeMirror.hint.anyword,
          completeSingle: false,
          range: 1000, // 附近多少行代码匹配
        });
      }
    });
  },
  watch: {
    value(val) {
      if (this.currentValue !== val) {
        this.currentValue = val;
      }
    },
    currentValue(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.$emit("change", newVal);
        this.$emit("input", newVal);
      }
    },
    mode(newVal) {
      this.$refs.editor.editor.setOption("mode", newVal);
    },
  },
};
</script>

<style scoped>
.CodeMirror {
  height: auto !important;
}

.CodeMirror-scroll {
  min-height: 300px;
  max-height: 600px;
}
</style>
