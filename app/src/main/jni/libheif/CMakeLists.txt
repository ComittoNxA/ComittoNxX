FetchContent_Declare(
        libheif
        GIT_REPOSITORY https://github.com/strukturag/libheif
        GIT_TAG        v1.18.2
)

option(CMAKE_BUILD_TYPE "" Release)
option(BUILD_SHARED_LIBS "" OFF) # CmakeListx.txt で上書きされてる？
option(BUILD_TESTING "" OFF)

option(ENABLE_PLUGIN_LOADING "" OFF)
option(WITH_AOM_DECODER "" OFF)
option(WITH_AOM_ENCODER "" OFF)
option(WITH_DAV1D "" ON)
option(WITH_VVDEC "" ON)
option(WITH_LIBDE265 "" ON)
option(WITH_RAV1E "" OFF)
option(WITH_SvtEnc "" OFF)
option(WITH_X265 "" OFF)
option(WITH_JPEG_DECODER "" ON)
option(WITH_JPEG_ENCODER "" OFF)
option(WITH_UNCOMPRESSED_CODEC "" ON)
option(WITH_KVAZAAR "" OFF)
option(WITH_OpenJPEG_DECODER "" ON)
option(WITH_OpenJPEG_ENCODER "" OFF)
option(WITH_FFMPEG_DECODER "" OFF)
option(WITH_REDUCED_VISIBILITY "" ON)
option(WITH_HEADER_COMPRESSION "" ON)
option(WITH_LIBSHARPYUV "" OFF) # CmakeListx.txt で上書きされてる？
option(WITH_EXAMPLES "" ON)
option(WITH_FUZZERS "" OFF)

option(ENABLE_MULTITHREADING_SUPPORT "" ON)
option(ENABLE_PARALLEL_TILE_DECODING "" ON)

# dav1d の build.bat から --default-library=static なしでビルドすべきか
# 出力ディレクトリも ../dav1d-build/build/%ABI% から ../dav1d-build/shared/%ABI% とかに変えるか
option(DAV1D_INCLUDE_DIR "" "${FETCHCONTENT_BASE_DIR}/dav1d-src/include")
option(DAV1D_LIBRARY "" "${FETCHCONTENT_BASE_DIR}/dav1d-build/build/${ANDROID_ABI}/src/libdav1d.so")

FetchContent_MakeAvailable(libheif)
target_include_directories(comitton PRIVATE ${libheif_BINARY_DIR} ${libheif_SOURCE_DIR})
target_link_libraries(comitton libheif)