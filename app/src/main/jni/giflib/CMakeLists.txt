FetchContent_Populate(
        giflib
        GIT_REPOSITORY https://git.code.sf.net/p/giflib/code
        GIT_TAG        5.2.2
        SOURCE_DIR "${FETCHCONTENT_BASE_DIR}/giflib-src"
        BINARY_DIR "${FETCHCONTENT_BASE_DIR}/giflib-subbuild"
        SUBBUILD_DIR "${FETCHCONTENT_BASE_DIR}/giflib-build"

)

add_library(giflib
        ${giflib_SOURCE_DIR}/dgif_lib.c
        ${giflib_SOURCE_DIR}/egif_lib.c
        ${giflib_SOURCE_DIR}/gif_err.c
        ${giflib_SOURCE_DIR}/gif_font.c
        ${giflib_SOURCE_DIR}/gif_hash.c
        ${giflib_SOURCE_DIR}/gif_hash.h
        ${giflib_SOURCE_DIR}/gif_lib.h
        ${giflib_SOURCE_DIR}/gif_lib_private.h
        ${giflib_SOURCE_DIR}/gifalloc.c
        ${giflib_SOURCE_DIR}/openbsd-reallocarray.c
)

target_include_directories(comitton PRIVATE ${giflib_BINARY_DIR} ${giflib_SOURCE_DIR})
target_link_libraries(comitton giflib)
