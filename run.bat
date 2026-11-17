@echo off
title Grand Hotel Management System (GHMS)
set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2024.2.1\jbr\bin\java.exe"
if not exist "%JAVA_CMD%" (
    set "JAVA_CMD=java"
)

echo Memulai Grand Hotel Management System (GHMS)...
"%JAVA_CMD%" --module-path lib --add-modules javafx.controls,javafx.fxml -cp "lib/*;bin" MainApp
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Menjalankan dengan fallback classpath...
    "%JAVA_CMD%" -cp "lib/*;bin" MainApp
)
pause

