
@echo off

set PROJECT=ReaperSDK
echo ===============================================
echo ===================ReaperSDK===================
echo       ..######..########..##....##
echo       .##....##.##.....##.##...##.
echo       .##.......##.....##.##..##..
echo       ..######..##.....##.#####...
echo       .......##.##.....##.##..##..
echo       .##....##.##.....##.##...##.
echo       ..######..########..##....##
echo ==================ReaperSDK====================
echo ===============================================


echo %PROJECT% will be build
set LOCAL_FILE_PATH=%~dp0
set welcome=%PROJECT% File-Path=%LOCAL_FILE_PATH%
echo %welcome%

::%SystemRoot%\system32\ping.exe -n 1 10.18.49.11


echo now environment path is %Path% 

cd QK_Reaper
echo Gradle info:
call gradle.bat -v 
call gradle.bat clean
echo ==============clean task finish===============================
call gradle.bat assembleDebug
echo ==============build assembleDebug task finish==============
cd ..
echo ==============%PROJECT% build finish!!!==============
echo %LOCAL_FILE_PATH%bin

if exist %LOCAL_FILE_PATH%bin (
	echo clear cache
	rd /s /q %LOCAL_FILE_PATH%bin
)

if not exist %LOCAL_FILE_PATH%bin (
	md %LOCAL_FILE_PATH%bin
)

rem set OUTPATH = %LOCAL_FILE_PATH%bin
rem echo OUTPATH:%OUTPATH%

copy /y QK_Reaper\bin\reaper.aar %LOCAL_FILE_PATH%bin\reaper.aar
copy /y QK_Reaper\bin\reaper.rr %LOCAL_FILE_PATH%bin\reaper.rr
copy /y QK_Reaper\bin\sample.apk %LOCAL_FILE_PATH%bin\sample.apk

echo ==============%PROJECT% copy finish!!!==============


