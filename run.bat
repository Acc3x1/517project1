@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo  ISO 11820 运行脚本
echo ==========================================
echo.

:: 检查 Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请先安装 JDK 17+
    pause
    exit /b 1
)

:: 确定 Gradle 命令
set "GRADLE_CMD="

if exist "%~dp0gradlew.bat" (
    if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
        set "GRADLE_CMD=%~dp0gradlew.bat"
    )
)

if "!GRADLE_CMD!"=="" (
    where gradle >nul 2>&1
    if !errorlevel! equ 0 (
        set "GRADLE_CMD=gradle"
    )
)

if "!GRADLE_CMD!"=="" (
    echo [错误] 未找到 Gradle，请先运行 build.bat
    pause
    exit /b 1
)

echo 启动 ISO 11820 建筑材料不燃性试验仿真系统...
echo.

call "!GRADLE_CMD!" run --no-daemon

pause
