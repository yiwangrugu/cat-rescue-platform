@echo off
chcp 65001 >nul
echo.
echo 🚀 启动流浪猫救助平台...
echo.

REM 启动后端服务
echo 📡 启动后端服务...
start "后端服务" cmd /k "cd /d cat-rescue-backend && mvn spring-boot:run"

REM 等待后端启动
echo ⏳ 等待后端服务启动...
timeout /t 15 >nul

REM 启动前端服务
echo 🌐 启动前端服务...
start "前端服务" cmd /k "cd /d cat-rescue-frontend && npm run dev:all"

echo.
echo ✅ 服务启动完成！
echo 📱 用户端: http://localhost:3000
echo 🛠️  救助端: http://localhost:3001
echo ⚙️  管理端: http://localhost:3002
echo 📡 后端API: http://localhost:8080
echo.
echo 按任意键退出...
pause >nul