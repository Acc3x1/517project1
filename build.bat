@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo  ISO 11820 构建脚本
echo ==========================================
echo.

:: 检查 Java
echo [1/3] 检查 Java 环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请先安装 JDK 17+
    echo 推荐下载: https://adoptium.net/
    pause
    exit /b 1
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%~v"
)
echo   Java 版本: %JAVA_VER%

:: 检查 Gradle
echo [2/3] 检查 Gradle...
set "GRADLE_CMD="

:: 方法1: 使用 gradlew
if exist "%~dp0gradlew.bat" (
    if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
        set "GRADLE_CMD=%~dp0gradlew.bat"
        echo   使用 Gradle Wrapper
    )
)

:: 方法2: 使用系统 PATH 中的 gradle
if "!GRADLE_CMD!"=="" (
    where gradle >nul 2>&1
    if !errorlevel! equ 0 (
        set "GRADLE_CMD=gradle"
        echo   使用系统 Gradle
    )
)

:: 方法3: 尝试常见的 Gradle 安装路径
if "!GRADLE_CMD!"=="" (
    for %%d in ("C:\gradle\bin\gradle.bat" "C:\Program Files\gradle\bin\gradle.bat") do (
        if exist "%%d" (
            set "GRADLE_CMD=%%d"
            echo   使用 %%d
        )
    )
)

if "!GRADLE_CMD!"=="" (
    echo [错误] 未找到 Gradle！
    echo.
    echo 请按以下步骤安装:
    echo   1. 下载 Gradle: https://gradle.org/install/
    echo   2. 解压到 C:\gradle
    echo   3. 将 C:\gradle\bin 添加到系统 PATH
    echo   4. 重新运行此脚本
    echo.
    echo 或者在 IntelliJ IDEA / Eclipse 中直接打开本项目
    pause
    exit /b 1
)

:: 先生成 wrapper（如果还没有）
if not exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
    echo   生成 Gradle Wrapper...
    "!GRADLE_CMD!" wrapper --gradle-version 8.8
    if !errorlevel! neq 0 (
        echo [错误] 生成 Wrapper 失败
        pause
        exit /b 1
    )
)

:: 构建
echo [3/3] 开始构建...
echo.
call "!GRADLE_CMD!" build --no-daemon

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo  构建成功！
    echo  运行: run.bat 或 gradle run
    echo ==========================================
) else (
    echo.
    echo ==========================================
    echo  构建失败！请检查上方错误信息
    echo ==========================================
)

pause
