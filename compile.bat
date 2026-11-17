@echo off
title Kompilasi GHMS
set "JAVAC_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2024.2.1\jbr\bin\javac.exe"
if not exist "%JAVAC_CMD%" (
    set "JAVAC_CMD=javac"
)

echo Mengompilasi source code GHMS...
if not exist "bin" mkdir bin
if not exist "bin\view" mkdir bin\view

"%JAVAC_CMD%" -cp "lib/*;src" -d bin src/model/*.java src/dao/*.java src/util/*.java src/controller/*.java src/test/*.java src/MainApp.java
xcopy /Y /S /I src\view\* bin\view\

echo Kompilasi selesai!
pause

