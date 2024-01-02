<template>
  <div>
    <el-card style="margin-top: 15px">
      <div slot="header">
        <span class="panel-title home-title">{{ isAdmin ? $t("m.File_Admin") : $t("m.Box_File") }}</span>
      </div>

      <ul class="el-upload-list el-upload-list--picture-card">
        <li
          tabindex="0"
          class="el-upload-list__item is-ready"
          v-for="(img, index) in boxFileList"
          :key="index"
          :style="{ height: isAdmin ? '146px' : '100px', width: isAdmin ? '146px' : '100px' }"
        >
          <div class="el-upload-list__item-inner">
            <img :src="floderImg" class="el-upload-list__item-thumbnail" />
            <span class="el-upload-list__item-actions">
              <div
                class="el-upload-list__item-text"
                :style="{ fontSize: isAdmin ? '16px' : '12px' }"
              >{{ img.hint }}</div>
              <span class="el-upload-list__item-buttons">
                <span
                  v-if="!disabled &&  isMainAdminRole"
                  class="el-upload-list__item-edit"
                  @click="handleEditInfo(img)"
                >
                  <i class="el-icon-edit"></i>
                </span>
                <span
                  v-if="!disabled"
                  class="el-upload-list__item-download"
                  @click="handleDownload(img)"
                >
                  <i class="el-icon-download"></i>
                </span>
                <span
                  v-if="!disabled && isAdmin && isMainAdminRole"
                  class="el-upload-list__item-delete"
                  @click="handleRemove(img, index)"
                >
                  <i class="el-icon-delete"></i>
                </span>
              </span>
            </span>
          </div>
        </li>
      </ul>

      <el-upload
        v-if="isAdmin && isMainAdminRole"
        action="/api/file/upload-file"
        list-type="picture-card"
        :on-edit="handleEdit"
        :on-remove="handleRemove"
        style="display: inline"
      >
        <i class="el-icon-plus"></i>
      </el-upload>

      <el-dialog
        :title="$t('m.Edit_Box_File')"
        width="350px"
        :visible.sync="HandleEditVisible"
        :close-on-click-modal="false"
      >
        <el-form>
          <el-form-item :label="$t('m.Hint2')" required>
            <el-input v-model="hint" size="small"></el-input>
          </el-form-item>

          <el-form-item style="text-align: center">
            <el-button
              type="primary"
              @click="handleEdit(hint)"
              :loading="handleEditLoading"
            >{{ $t("m.To_Update") }}</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import utils from "@/common/utils";
import { mapGetters } from "vuex";

export default {
  name: "BoxFile",
  data() {
    return {
      disabled: false,
      EditFileId: "",
      hint: "",
      handleEditLoading: false,
      HandleEditVisible: false,
      floderImg: require("@/assets/文件夹.png"),
    };
  },
  props: {
    boxFileList: {
      default: [],
      type: Array,
    },
    isAdmin: {
      default: true,
      type: Boolean,
    },
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getBoxFileList();
    },
    getBoxFileList() {
      api.getBoxFileList().then((res) => {
        if (this.boxFileList.length === 0) {
          this.boxFileList = res.data.data;
        }
      });
    },
    handleRemove(file, index = undefined) {
      let id = file.id;
      if (file.response != null) {
        id = file.response.data.id;
      }

      this.$http({
        url: "/api/file/delete-file",
        method: "get",
        params: {
          fileId: id,
        },
      }).then((response) => {
        // 在这里处理成功的情况
        if (response.status === 200) {
          myMessage.success(this.$i18n.t("m.Delete_successfully"));
          if (index != undefined) {
            this.boxFileList.splice(index, 1);
          }
        }
      });
    },
    handleEditInfo(file) {
      let id = file.id;
      if (id) {
        this.EditFileId = id;
        this.hint = file.hint;
      }
      this.HandleEditVisible = true;
    },
    handleEdit(hint = undefined) {
      this.handleEditLoading = true;

      let id = this.EditFileId;

      api.admin_editFileHint(id, hint).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.HandleEditVisible = false;
          this.handleEditLoading = false;
        },
        (err) => {
          this.handleEditLoading = false;
        }
      );

      this.EditFileId = "";
      this.hint = "";
    },

    handleDownload(file) {
      utils.downloadBoxFile(file.url, file.hint);
    },
  },
  computed: {
    ...mapGetters(["isMainAdminRole"]),
  },
};
</script>
<style>
.el-upload-list__item {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
}

.el-upload-list__item-thumbnail {
  height: 146px;
  width: 146px;
  margin-bottom: 10px; /* 调整文字和缩略图之间的间距 */
}

.el-upload-list__item-text {
  margin: 0;
  padding: 0;
}

.el-upload-list__item-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}
.el-upload-list__item-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.el-upload-list__item-buttons {
  display: flex;
  align-items: center;
}

.el-upload-list__item-buttons > span {
  margin-left: 10px; /* 调整按钮之间的间距 */
}
</style>