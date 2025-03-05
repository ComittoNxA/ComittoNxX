# ソースファイルのアーカイブを取得する
FetchContent_Populate(
        dav1d
    GIT_REPOSITORY https://code.videolan.org/videolan/dav1d.git
    GIT_TAG         1.5.1
    SOURCE_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-src"
    BINARY_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-subbuild"
    SUBBUILD_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-build"
)

if("${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Windows")
    # Windowsのとき
    set(BUILD_OS "WIN32")

    # NDKの実行ファイルパスを環境変数のパスに追加する
    string(REPLACE "/" "\\" NDK_BIN_PATH "${ANDROID_TOOLCHAIN_ROOT}\\bin")
    set(ENV{PATH} "${NDK_BIN_PATH};$ENV{PATH}")
else()
    # Windowsじゃないとき
    set(BUILD_OS "UNIX")

    # NDKの実行ファイルパスを環境変数のパスに追加する
    set(NDK_BIN_PATH "${ANDROID_TOOLCHAIN_ROOT}/bin")
    set(ENV{PATH} "${NDK_BIN_PATH}:;$ENV{PATH}")
endif()

# ビルドするアーキテクチャーの情報を変数に入れる
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(android_arch "arm")
elseif(ANDROID_ABI STREQUAL "arm64-v8a")
    set(android_arch "aarch64")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(android_arch "x86_64")
else()
    set(android_arch "x86")
endif()

# ビルド用のファイルをソースディレクトリにコピーする
file(COPY "${CMAKE_CURRENT_LIST_DIR}/${BUILD_OS}" DESTINATION "${FETCHCONTENT_BASE_DIR}/dav1d-src")

EXECUTE_PROCESS(
    COMMAND meson setup ../dav1d-build/build/${ANDROID_ABI}
    --default-library=static --buildtype release
    --cross-file=${BUILD_OS}/${android_arch}-android.meson
    -Denable_tools=false -Denable_tests=false
    WORKING_DIRECTORY "${FETCHCONTENT_BASE_DIR}/dav1d-src"
    COMMAND_ECHO STDERR
    #COMMAND_ERROR_IS_FATAL ANY
    RESULT_VARIABLE BUILD_COMMAND_RESULT
    OUTPUT_VARIABLE BUILD_COMMAND_STDOUT
    ERROR_VARIABLE BUILD_COMMAND_STDERR
)

# ビルドの実行結果をログ出力する
message(WARNING "Cmake: dav1d: BUILD_COMMAND_RESULT = ${BUILD_COMMAND_RESULT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDOUT = \n${BUILD_COMMAND_STDOUT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDERR = \n${BUILD_COMMAND_STDERR}")

EXECUTE_PROCESS(
        COMMAND ninja -C ../dav1d-build/build/${ANDROID_ABI}
        WORKING_DIRECTORY "${FETCHCONTENT_BASE_DIR}/dav1d-src"
        COMMAND_ECHO STDERR
        #COMMAND_ERROR_IS_FATAL ANY
        RESULT_VARIABLE BUILD_COMMAND_RESULT
        OUTPUT_VARIABLE BUILD_COMMAND_STDOUT
        ERROR_VARIABLE BUILD_COMMAND_STDERR
)

# ビルドの実行結果をログ出力する
message(WARNING "Cmake: dav1d: BUILD_COMMAND_RESULT = ${BUILD_COMMAND_RESULT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDOUT = \n${BUILD_COMMAND_STDOUT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDERR = \n${BUILD_COMMAND_STDERR}")
