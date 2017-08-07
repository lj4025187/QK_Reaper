cp ../bin/reaper.aar libs/;
cp ../bin/reaper.rr src/main/assets/;
gradle clean;
gradle assembleDebug

adb root;
sleep 2
adb remount;
sleep 2
java -jar ../tools/signapk.jar ../tools/security/platform.x509.pem ../tools/security/platform.pk8 build/outputs/apk/sample-debug.apk build/outputs/apk/sample-release-signed.apk
echo "after sign apk"
adb shell mkdir /system/app/ReaperSample
adb push build/outputs/apk/sample-release-signed.apk /system/app/ReaperSample/ReaperSample.apk

if [ $? -eq 0 ]; then
    echo "pushed to device, now reboot"
    adb reboot;
    #adb shell stop; adb shell start
fi

#adb reboot;
#adb shell stop
#adb shell start

