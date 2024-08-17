
# ndk path
export ANDROID_NDK_BIN="$1"

# ANDROID_ABI & ANDROID_ARCH
ABI="$2"
ARCH="$3"

echo ANDROID_NDK_BIN=${ANDROID_NDK_BIN}
echo ABI=${ABI}
echo ARCH=${ARCH}

# Add toolchains bin directory to PATH
export PATH=${ANDROID_NDK_BIN}:$PATH

meson setup ../dav1d-build/build/${ABI} --default-library=static \
    --buildtype release \
    --cross-file="UNIX/${ARCH}-android.meson" \
    -Denable_tools=false -Denable_tests=false

ninja -C ../dav1d-build/build/%ABI%