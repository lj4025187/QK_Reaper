
@echo off

set CODE_PATH=%1

::%SystemRoot%\system32\ping.exe -n 1 10.18.49.11



cd QK_Reaper
echo Gradle info:
call gradle.bat -v 
call gradle.bat clean
echo ==============clean task finish===============================
echo ==============start loader task===============================
cd loader
call gradle.bat assembleDebug
cd ..
echo ==============finish loader task==============================
echo ==============start reaper task===============================
cd reaper
call gradle.bat assembleDebug
cd ..
echo ==============finish reaper task==============================
echo ==============start sample task===============================
copy /y bin\reaper.aar sample\libs\reaper.aar
copy /y bin\reaper.rr sample\src\main\assets\reaper.rr
cd sample
call gradle.bat assembleDebug
cd ..
echo ==============finish sample task==============================
echo ==============build assembleDebug task finish==============
cd ..
echo ==============%PROJECT% build finish!!!==============


if exist %CODE_PATH%bin (
	echo clear cache
	rd /s /q %CODE_PATH%bin
)

if not exist %CODE_PATH%bin (
	md %CODE_PATH%bin
)
echo md bin
set OUTPATH = %CODE_PATH%\bin
echo OUTPATH:%OUTPATH%
echo CODE_PATH:%CODE_PATH%

copy /s /y /e /i  %CODE_PATH%\QK_Reaper\QK_Reaper\bin\reaper.aar %CODE_PATH%\bin\reaper.aar
copy /s /y /e /i  %CODE_PATH%\QK_Reaper\QK_Reaper\bin\reaper.rr  %CODE_PATH%\bin\reaper.rr
copy /s /y /e /i  %CODE_PATH%\QK_Reaper\QK_Reaper\bin\sample.apk %CODE_PATH%\bin\sample.apk

echo ==============%PROJECT% copy finish!!!==============


