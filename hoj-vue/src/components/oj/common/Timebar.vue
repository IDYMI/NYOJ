<template>
  <div
    class="timeline"
    ref="timeline"
    @mousemove="handleMouseMove"
    @mousedown.stop="handleMouseDown"
    @mouseup="handleMouseUp"
    @click="handleClick"
  >
    <div class="background-bar" :style="{ width: `${barWidth}px` }"></div>
    <el-tooltip :content="Tooltip" placement="top">
      <button class="draggable-button" :style="{ left: buttonPosition + 'px' }"></button>
    </el-tooltip>
  </div>
</template>

<script>
export default {
  name: "Timebar",
  props: {
    // 按钮位置
    buttonPosition: {
      type: Number,
      default: 0,
    },
    // 显示
    Tooltip: {
      type: String,
      default: "00:00:00",
    },
    // 比例
    progressValue: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      barWidth: 0, // 背景条宽度
      isDragging: false, // 是否正在拖拽
      startX: 0,
    };
  },
  mounted() {
    // 通过 setInterval 每100毫秒自增背景条的宽度
    this.intervalId = setInterval(() => {
      this.barWidth =
        (this.progressValue / 100) * this.$refs.timeline.offsetWidth;
      this.buttonPosition = Math.min(
        this.$refs.timeline.offsetWidth,
        this.barWidth - 10
      );
    }, 100);
  },
  beforeDestroy() {
    // 清除 setInterval
    clearInterval(this.intervalId);
  },
  methods: {
    getLeftPoint() {
      const timelineRect = this.$refs.timeline.getBoundingClientRect();
      const timelineLeft = timelineRect.left; // 时间轴的左侧坐标
      return timelineLeft;
    },
    newInterval(event) {
      // 清除 setInterval
      clearInterval(this.intervalId);
      const timelineLeft = this.getLeftPoint();
      const offsetX = event.clientX - timelineLeft - 10;
      let percentage_real = offsetX / this.$refs.timeline.offsetWidth;
      if (percentage_real > 0.99) {
        // 解决右边终点
        percentage_real = 1;
      }
      const percentage = Math.max(0, Math.min(percentage_real, 1));
      this.$emit("transfer", percentage); //触发transfer方法，传递 百分比 为向父组件传递的数据
      this.barWidth =
        (this.progressValue / 100) * this.$refs.timeline.offsetWidth;
    },
    handleMouseMove(event) {
      if (this.isDragging) {
        const offsetX = event.clientX - this.startX - 10;
        this.buttonPosition = Math.max(
          0,
          Math.min(offsetX, this.barWidth - 10)
        );
      }
    },
    handleMouseDown(event) {
      this.isDragging = true;
      this.startX = event.clientX - this.buttonPosition - 10;

      this.newInterval(event);
    },
    handleMouseUp() {
      this.isDragging = false;
    },
    handleClick(event) {
      const timelineLeft = this.getLeftPoint();
      const offsetX = event.clientX - timelineLeft - 10;
      this.buttonPosition = Math.max(0, Math.min(offsetX, this.barWidth - 10));

      this.newInterval(event);
    },
  },
};
</script>

<style scoped>
.timeline {
  width: 100%;
  height: 8px;
  border-radius: 5px;
  position: relative;
  background-color: #e4e7ed;
}

.draggable-button {
  position: absolute;
  width: 20px;
  height: 20px;
  background-color: #409eff;
  border-radius: 50%;
  color: #fff;
  border: none;
  cursor: grab;
  top: 50%;
  transform: translateY(-50%);
}

.background-bar {
  height: 10px;
  background-color: #09be24;
  border-radius: 5px;
  position: absolute;
  cursor: pointer;
}
</style>
