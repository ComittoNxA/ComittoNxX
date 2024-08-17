@echo off

: ndk path
set ANDROID_NDK_BIN=%1

: ANDROID_ABI & ANDROID_ARCH
set ABI=%2
set ARCH=%3

set ANDROID_NDK_BIN=%ANDROID_NDK_BIN:"=%
set ABI=%ABI:"=%
set ARCH=%ARCH:"=%

echo ABI=%ABI%
echo ARCH=%ARCH%
echo ANDROID_NDK_BIN=%ANDROID_NDK_BIN%

: 本来なら %ANDROID_NDK_BIN% と書くが、動かないので ${ANDROID_NDK_BIN} と書く
: set PATH=%PATH%;${ANDROID_NDK_HOME}\toolchains\llvm\prebuilt\windows-x86_64\bin
set PATH=${ANDROID_NDK_BIN};%PATH%
: set PATH=%ANDROID_NDK_BIN%;%PATH%
echo PATH=%PATH%

meson setup ../dav1d-build/build/%ABI% --default-library=static ^
    --buildtype release ^
    --cross-file="WIN32/%ARCH%-android.meson" ^
    -Denable_tools=false -Denable_tests=false

ninja -C ../dav1d-build/build/%ABI%