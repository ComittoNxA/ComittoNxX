FetchContent_Declare(
  libpng
  GIT_REPOSITORY https://github.com/glennrp/libpng
  GIT_TAG        v1.6.43
)

option(PNG_SHARED "" OFF)
option(PNG_STATIC "" ON)
option(PNG_TOOLS "" OFF)
option(PNG_TESTS "" OFF)

FetchContent_MakeAvailable(libpng)
target_include_directories(comitton PRIVATE ${libpng_BINARY_DIR} ${libpng_SOURCE_DIR})
target_link_libraries(comitton png_static z)
