FetchContent_Declare(libjxl
        GIT_REPOSITORY https://github.com/libjxl/libjxl
        GIT_TAG v0.11.1
        BINARY_DIR build
        SUBBUILD_DIR subbuild
)

#option(JPEGXL_STATIC "" ON)
option(JPEGXL_FORCE_SYSTEM_BROTLI "" OFF)
option(JPEGXL_FORCE_SYSTEM_HWY "" OFF)
option(BUILD_TESTING "" OFF)
option(JPEGXL_BUNDLE_LIBPNG "" OFF)
option(JPEGXL_ENABLE_VIEWERS "" OFF)
option(JPEGXL_ENABLE_COVERAGE "" OFF)
option(JPEGXL_ENABLE_FUZZERS "" OFF)
option(JPEGXL_ENABLE_DOXYGEN "" OFF)
option(JPEGXL_ENABLE_MANPAGES "" OFF)
option(JPEGXL_ENABLE_EXAMPLES "" OFF)
option(JPEGXL_ENABLE_SJPEG "" OFF)
option(JPEGXL_ENABLE_OPENEXR "" OFF)
option(JPEGXL_ENABLE_TRANSCODE_JPEG "" OFF)
option(JPEGXL_ENABLE_TOOLS "" OFF)
option(JPEGXL_ENABLE_BENCHMARK "" OFF)
option(JPEGXL_ENABLE_JPEGLI "" OFF)
option(JPEGXL_ENABLE_JNI "" OFF)
option(JPEGXL_ENABLE_DEVTOOLS "" OFF)
option(JPEGXL_ENABLE_BENCHMARK "" OFF)

FetchContent_GetProperties(libjxl)

if(NOT libjxl_POPULATED)
    FetchContent_MakeAvailable(libjxl)
endif()

target_include_directories(comitton PRIVATE
        ${libjxl_BINARY_DIR}
        ${libjxl_SOURCE_DIR}
        $<TARGET_PROPERTY:hwy,INCLUDE_DIRECTORIES>
)

target_link_libraries(comitton
        jxl_dec
        jxl_threads
        brotlidec
        brotlienc
        brotlicommon
        hwy
        -Wl,--allow-multiple-definition
)