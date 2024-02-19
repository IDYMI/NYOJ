<template>
<div class="markdown-body submission-detail">
  <body bgcolor="white">
    <div class="code-container">
      <pre>
          <!-- 添加末尾换行，使得引索在页面头部展示 -->
          <div v-highlight v-html="code + '\n'.repeat(50)"></div>
        </pre>
    </div>
  </body>
</div>
</template>

<script>
export default {
  props: {
    code: {
      type: String,
      required: true,
    },
  },
};
</script>

<style scoped>
.hljs {
  padding: 0 !important;
  margin: 0 !important;
}

.submission-detail pre {
  padding-left: 30px !important;
  margin: 0 !important;
}

.code-container {
  max-height: 1000px;
  overflow-y: auto;
}

.code-container pre {
  width: 100%;
  box-sizing: border-box; /* Ensure padding doesn't make it exceed 100% */
}
</style>
