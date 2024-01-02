<template>
  <div>
    <home-carousel></home-carousel>
    <box-file></box-file>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import HomeCarousel from "@/components/admin/HomeCarousel";
import BoxFile from "@/components/oj/common/BoxFile";

export default {
  name: "File",
  components: {
    HomeCarousel,
    BoxFile,
  },
  computed: {
    ...mapGetters(["isSuperAdmin"]),
  },
};
</script>
