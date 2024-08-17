FetchContent_Declare(
  libjpeg-turbo
  GIT_REPOSITORY https://github.com/libjpeg-turbo/libjpeg-turbo
  GIT_TAG        2.1.91
)

option(ENABLE_SHARED "" OFF)
option(ENABLE_STATIC "" ON)
option(REQUIRE_SIMD "" ON)
option(WITH_ARITH_ENC "" OFF)
option(WITH_JPEG8 "" ON)
option(WITH_TURBOJPEG "" OFF)

FetchContent_MakeAvailable(libjpeg-turbo)
target_include_directories(comitton PRIVATE ${libjpeg-turbo_BINARY_DIR} ${libjpeg-turbo_SOURCE_DIR})
target_link_libraries(comitton jpeg-static)
