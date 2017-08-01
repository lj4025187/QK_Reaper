#!/bin/bash

module=$1
testcase=$2

if [ "$module" == "" ]
then
    gradle connectedDebugAndroidTest
else
    gradle  $module:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=$testcase
fi


echo ""
echo "使用如下格式执行特定测试　：　"
echo ""
echo "gradle reaper:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.fighter.reaper.ReaperConfigTest#testDatabase"
echo ""
