<template>
  <div
    class="timeline"
    ref="timeline"
    @mousemove="handleMouseMove"
    @mousedown="handleMouseDown"
    @mouseup="handleMouseUp"
  >
    <div class="background-bar" :style="{ width: `${barWidth}px` }"></div>
    <div class="button" :style="{ left: `${buttonPosition}px` }"></div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      buttonPosition: 0, // 按钮位置
      barWidth: 0, // 背景条宽度
      isDragging: false, // 是否正在拖拽
      intervalId: null, // setInterval 的 ID
    };
  },
  mounted() {
    // 通过 setInterval 每100毫秒自增背景条的宽度
    this.intervalId = setInterval(() => {
      this.barWidth += 1;

      // 如果没有拖拽或点击，将按钮显示在背景条的最右侧
      if (!this.isDragging) {
        this.buttonPosition = this.barWidth;
      }
    }, 100);
  },
  beforeDestroy() {
    // 清除 setInterval
    clearInterval(this.intervalId);
  },
  methods: {
    handleMouseMove(event) {
      // 只有在拖拽状态下，才能改变按钮位置
      if (this.isDragging) {
        const timelineRect = this.$refs.timeline.getBoundingClientRect();
        const timelineLeft = timelineRect.left; // 时间轴的左侧坐标
        const mouseX = event.clientX - timelineLeft; // 鼠标相对于时间轴的水平位置
        const maxButtonPosition = this.barWidth; // 按钮可以移动的最远位置
        this.buttonPosition = Math.min(Math.max(mouseX, 0), maxButtonPosition); // 限制按钮位置在区域内
      }
    },
    handleMouseDown(event) {
      this.isDragging = true;
      const timelineRect = this.$refs.timeline.getBoundingClientRect();
      const timelineLeft = timelineRect.left; // 时间轴的左侧坐标
      const mouseX = event.clientX - timelineLeft; // 鼠标相对于时间轴的水平位置
      const maxButtonPosition = this.barWidth; // 按钮可以移动的最远位置
      this.buttonPosition = Math.min(Math.max(mouseX, 0), maxButtonPosition); // 限制按钮位置在区域内
    },
    handleMouseUp() {
      this.isDragging = false;
    },
  },
};
</script>

<style>
.timeline {
  width: 500px;
  height: 50px;
  position: relative;
  background-color: #ccc;
  cursor: pointer;
}

.background-bar {
  height: 100%;
  background-color: #ff0000;
  position: absolute;
  top: 0;
  left: 0;
}

.button {
  width: 20px;
  height: 20px;
  background-color: #00ff00;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
}
</style>
