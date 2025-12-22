let timer; // 存储定时器
let seconds = 0; // 总秒数
// alert(1)
/*let isConfirmed = confirm("你确定要删除这个项目吗？");
if (isConfirmed) {
    alert("确定")
    // 这里写删除的 JavaScript 代码
} else {
    alert("取消")
}*/
// alert(2)

function start() {
    alert("离开教室打发士大夫");
    // 启动前先清除旧的，防止多次点击导致计时加速
    if (timer) return;

    timer = setInterval(() => {
        seconds++;
        updateDisplay();
    }, 1000);
}

function pause() {
    clearInterval(timer);
    timer = null;
}

function reset() {
    pause();
    seconds = 0;
    updateDisplay();
}

function updateDisplay() {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    // 格式化输出：如果是个位数，前面补 0
    const formatted =
        `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

    document.getElementById('display').innerText = formatted;
}