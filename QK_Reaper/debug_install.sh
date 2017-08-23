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


cd loader;
gradle clean;gradle assembleRelease;
echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> loader release generated >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
cd ..;
cd reaper;
gradle clean;gradle assembleDebug;
echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> reaper debug generated >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
cd ..;
cp bin/reaper.aar sample/libs;
cp bin/reaper.rr sample/src/main/assets/;
cd sample;
gradle clean;gradle assembleDebug;
echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sample debug generated >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
adb root;
sleep 2
adb remount;
sleep 2
adb uninstall com.fighter.reaper;
adb uninstall com.fighter.reaper.test;
adb uninstall com.fighter.reaper.sample;
java -jar ../tools/signapk.jar ../tools/security/platform.x509.pem ../tools/security/platform.pk8 build/outputs/apk/sample-debug.apk build/outputs/apk/sample-debug-signed.apk
echo ">>>> prepare install debug signed sample apk >>>>"
adb install -r build/outputs/apk/sample-debug-signed.apk
cd ..
sleep 2
adb shell am start com.fighter.reaper.sample/.activities.TabMainActivity
