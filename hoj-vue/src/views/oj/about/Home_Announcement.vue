<template>
  <div>
    <Announcements class="card-top"></Announcements>
  </div>
</template>

<script>
const Announcements = () => import("@/components/oj/common/Announcements.vue");

export default {
  name: "Announcement",
  components: {
    Announcements,
  },
  methods: {},
  computed: {},
};
</script>

