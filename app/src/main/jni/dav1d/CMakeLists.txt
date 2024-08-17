# ソースファイルのアーカイブを取得する
FetchContent_Populate(
        dav1d
    GIT_REPOSITORY https://code.videolan.org/videolan/dav1d.git
    GIT_TAG         1.4.3
    SOURCE_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-src"
    BINARY_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-subbuild"
    SUBBUILD_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-build"
)

if("${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Windows")
    # Windowsのとき

    # NDKの実行ファイルパスを "/" から "\" に変換
    string(REPLACE "/" "\\" NDK_BIN_PATH "${ANDROID_TOOLCHAIN_ROOT}\\bin")

    # ビルド用のファイルをソースディレクトリにコピーする
    file(COPY "${CMAKE_CURRENT_LIST_DIR}/WIN32" DESTINATION "${FETCHCONTENT_BASE_DIR}/dav1d-src")
    set(BUILD_COMMAND "WIN32\\build.bat")
else()
    # Windowsじゃないとき

    # NDKの実行ファイルパスをセット
    set(NDK_BIN_PATH "${ANDROID_TOOLCHAIN_ROOT}/bin")

    # ビルド用のファイルをソースディレクトリにコピーする
    file(COPY "${CMAKE_CURRENT_LIST_DIR}/UNIX" DESTINATION ${source_dir})
    set(BUILD_COMMAND "bash UNIX/build.sh")
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

# スタティックライブラリをビルドする
execute_process(COMMAND "${BUILD_COMMAND}" "${NDK_BIN_PATH}" "${ANDROID_ABI}" "${android_arch}"
        WORKING_DIRECTORY "${FETCHCONTENT_BASE_DIR}/dav1d-src"
        COMMAND_ECHO STDERR
        #COMMAND_ERROR_IS_FATAL ANY
        RESULT_VARIABLE BUILD_COMMAND_RESULT
        OUTPUT_VARIABLE BUILD_COMMAND_STDOUT
        ERROR_VARIABLE BUILD_COMMAND_STDERR
)

# ビルドの実行結果をログ出力する
message(WARNING "Cmake: dav1d: BUILD_COMMAND_RESULT = ${BUILD_COMMAND_RESULT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDOUT = ${BUILD_COMMAND_STDOUT}")
message(WARNING "Cmake: dav1d: BUILD_COMMAND_STDERR = ${BUILD_COMMAND_STDERR}")
