cd QK_Reaper;
rm -rf bin/;
gradle clean;gradle assembleDebug;
cd ..;
mkdir bin;
cp QK_Reaper/bin/reaper.aar bin/;
cp QK_Reaper/bin/reaper.rr bin/;
cp QK_Reaper/bin/sample.apk bin/;
