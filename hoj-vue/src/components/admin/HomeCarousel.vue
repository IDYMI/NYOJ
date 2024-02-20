<template>
  <div>
    <el-card style="margin-top: 15px">
      <div slot="header">
        <span class="panel-title home-title">{{ $t("m.Home_Rotation_Chart") }}</span>
      </div>

      <ul class="el-upload-list el-upload-list--picture-card">
        <li
          tabindex="0"
          class="el-upload-list__item is-ready"
          v-for="(img, index) in carouselImgList"
          :key="index"
        >
          <div>
            <img
              :src="img.url"
              alt="load faild"
              style="height: 146px; width: 146x"
              class="el-upload-list__item-thumbnail"
            />
            <span class="el-upload-list__item-actions">
              <span class="el-upload-list__item-edit" @click="handleEditInfo(img)">
                <i class="el-icon-edit"></i>
              </span>

              <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(img)">
                <i class="el-icon-zoom-in"></i>
              </span>
              <span
                v-if="!disabled"
                class="el-upload-list__item-download"
                @click="handleDownload(img)"
              >
                <i class="el-icon-download"></i>
              </span>
              <span
                v-if="!disabled"
                class="el-upload-list__item-delete"
                @click="handleRemove(img, index)"
              >
                <i class="el-icon-delete"></i>
              </span>
            </span>
          </div>
        </li>
      </ul>
      <el-upload
        action="/api/file/upload-carouse-img"
        list-type="picture-card"
        style="display: inline"
        :on-error="init"
        :on-success="init"
        :show-file-list="false"
      >
        <i class="el-icon-plus"></i>
      </el-upload>

      <el-dialog :visible.sync="dialogVisible">
        <img width="100%" :src="dialogImageUrl" alt />
      </el-dialog>

      <el-dialog
        :title="$t('m.Edit_Home_Rotation_Chart')"
        width="350px"
        :visible.sync="HandleEditVisible"
        :close-on-click-modal="false"
      >
        <el-form>
          <el-form-item :label="$t('m.Url')" required>
            <el-input v-model="link" size="small"></el-input>
          </el-form-item>

          <el-form-item :label="$t('m.Hint2')" required>
            <el-input v-model="hint" size="small"></el-input>
          </el-form-item>

          <el-form-item style="text-align: center">
            <el-button
              type="primary"
              @click="handleEdit(link, hint)"
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
  name: "File",
  data() {
    return {
      dialogImageUrl: "",
      dialogVisible: false,
      disabled: false,
      carouselImgList: [],
      EditImgId: "",
      link: "",
      hint: "",
      handleEditLoading: false,
      HandleEditVisible: false,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getHomeCarousel();
    },
    getHomeCarousel() {
      api.getHomeCarousel().then((res) => {
        this.carouselImgList = res.data.data;
      });
    },
    handleRemove(file, index = undefined) {
      let id = file.id;
      if (file.response != null) {
        id = file.response.data.id;
      }
      api.admin_deleteHomeCarousel(id).then((res) => {
        myMessage.success(this.$i18n.t("m.Delete_successfully"));
        if (index != undefined) {
          this.carouselImgList.splice(index, 1);
          this.init();
        }
      });
    },
    handleEditInfo(file) {
      let id = file.id;
      if (id) {
        this.EditImgId = id;
        this.link = file.link;
        this.hint = file.hint;
      }
      this.HandleEditVisible = true;
    },
    handleEdit(link = undefined, hint = undefined) {
      this.handleEditLoading = true;

      let id = this.EditImgId;

      api.admin_editHomeCarousel(id, link, hint).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.HandleEditVisible = false;
          this.handleEditLoading = false;
          this.init();
        },
        (err) => {
          this.handleEditLoading = false;
          this.init();
        }
      );
      this.EditImgId = "";
      this.link = "";
      this.hint = "";
    },

    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    handleDownload(file) {
      utils.downloadFile(file.url);
    },
  },
  computed: {
    ...mapGetters(["isSuperAdmin"]),
  },
};
</script>
