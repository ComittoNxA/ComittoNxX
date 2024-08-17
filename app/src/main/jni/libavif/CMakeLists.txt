FetchContent_Populate(
        libavif
        GIT_REPOSITORY https://github.com/AOMediaCodec/libavif
        GIT_TAG         v1.1.1
        SUBBUILD_DIR ${FETCHCONTENT_BASE_DIR}/libavif-subbuild
        SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/libavif-src
        BINARY_DIR ${FETCHCONTENT_BASE_DIR}/libavif-build
)

file(COPY "${FETCHCONTENT_BASE_DIR}/dav1d-src/include" DESTINATION "${libavif_SOURCE_DIR}/ext/dav1d")
file(COPY "${FETCHCONTENT_BASE_DIR}/dav1d-build/build/${ANDROID_ABI}" DESTINATION "${libavif_SOURCE_DIR}/ext/dav1d/build")

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(AVIF_CODEC_DAV1D "LOCAL" CACHE STRING "" FORCE)
set(AVIF_CODEC_AOM OFF CACHE BOOL "" FORCE)
set(AVIF_CODEC_LIBGAV1 OFF CACHE BOOL "" FORCE)
set(AVIF_CODEC_RAV1E OFF CACHE BOOL "" FORCE)
set(AVIF_LIBYUV OFF CACHE BOOL "" FORCE)
set(AVIF_CODEC_SVT OFF CACHE BOOL "" FORCE)
set(AVIF_ZLIBPNG "SYSTEM" CACHE STRING "" FORCE)
set(AVIF_JPEG "SYSTEM" CACHE STRING "" FORCE)

add_subdirectory(${libavif_SOURCE_DIR} ${libavif_BINARY_DIR})

target_include_directories(comitton PRIVATE ${libavif_BINARY_DIR} ${libavif_SOURCE_DIR})
target_link_libraries(comitton jnigraphics avif log)