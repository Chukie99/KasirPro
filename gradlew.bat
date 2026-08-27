@echo off
setlocal
set APP_HOME=%~dp0
set app_path=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%app_path%" (
    echo Gradle wrapper not found. Open this project in Android Studio instead.
    exit /b 1
)
java -cp "%app_path%" org.gradle.wrapper.GradleWrapperMain %*
