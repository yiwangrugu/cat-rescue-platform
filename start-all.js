const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 启动流浪猫救助平台...\n');

// 启动后端服务
console.log('📡 启动后端服务...');
const backendProcess = spawn('mvn', ['spring-boot:run'], {
  cwd: path.join(__dirname, 'cat-rescue-backend'),
  stdio: 'pipe',
  shell: true
});

backendProcess.stdout.on('data', (data) => {
  console.log(`[后端] ${data.toString().trim()}`);
});

backendProcess.stderr.on('data', (data) => {
  console.error(`[后端错误] ${data.toString().trim()}`);
});

// 等待后端启动后启动前端
setTimeout(() => {
  console.log('\n🌐 启动前端服务...');
  const frontendProcess = spawn('npm', ['run', 'dev:all'], {
    cwd: path.join(__dirname, 'cat-rescue-frontend'),
    stdio: 'pipe',
    shell: true
  });

  frontendProcess.stdout.on('data', (data) => {
    console.log(`[前端] ${data.toString().trim()}`);
  });

  frontendProcess.stderr.on('data', (data) => {
    console.error(`[前端错误] ${data.toString().trim()}`);
  });

  frontendProcess.on('close', (code) => {
    console.log(`前端服务已关闭，退出码: ${code}`);
    backendProcess.kill();
  });
}, 10000); // 等待10秒让后端启动

// 处理退出信号
process.on('SIGINT', () => {
  console.log('\n🛑 正在关闭所有服务...');
  backendProcess.kill();
  process.exit();
});

console.log('✅ 服务启动中，请稍候...');
console.log('📱 用户端: http://localhost:3000');
console.log('🛠️  救助端: http://localhost:3001');
console.log('⚙️  管理端: http://localhost:3002');
console.log('📡 后端API: http://localhost:8080');
console.log('\n按 Ctrl+C 停止所有服务');