cmake_minimum_required(VERSION 3.22.1)
project(comitton C CXX ASM)
include(FetchContent)
include(GNUInstallDirs)

set(CMAKE_BUILD_TYPE Release)

set(
  COMITTON_SOURCES
  comitton/ImageBlur.cpp
  comitton/ImageScaleHalf.cpp
  comitton/ImageCommon.cpp
  comitton/ImageThumbnail.cpp
  comitton/ImageScaleLinear.cpp
  comitton/ImageScaleCubic.cpp
  comitton/ImageScaleNear.cpp
  comitton/ImageScale.cpp
  comitton/ImageBright.cpp
  comitton/ImageRotate.cpp
  comitton/ImageMarginCut.cpp
  comitton/ImageSharpen.cpp
  comitton/ImageInvert.cpp
  comitton/ImageGray.cpp
  comitton/ImageHalf.cpp
  comitton/callImage.cpp
  comitton/TextCommon.cpp
  comitton/callText.cpp
)

add_library(
  comitton SHARED
  ${COMITTON_SOURCES}
)

# rar 解凍ライブラリのビルド
add_subdirectory(unrar)
target_sources(comitton PRIVATE comitton/callUnrar.cpp)

# 画像フォーマットデコーダーのビルド
# WITH_XXX は app/build.gradle 内で宣言している
if(WITH_JPEG)
  # 不要
  #add_definitions(-DHAVE_LIBJPEG)
  #add_subdirectory(libjpeg-turbo)
  #target_sources(comitton PRIVATE comitton/ImageJPEG.cpp)
endif()
if(WITH_PNG)
  # 不要
  #add_definitions(-DHAVE_LIBPNG)
  #add_subdirectory(libpng)
  #target_sources(comitton PRIVATE comitton/ImagePNG.cpp)
endif()
if(WITH_GIF)
  # 不要
  #add_definitions(-DHAVE_LIBGIF)
  #add_subdirectory(giflib)
  #target_sources(comitton PRIVATE comitton/ImageGIF.cpp)
endif()
if(WITH_WEBP)
  # 不要
  #add_definitions(-DHAVE_LIBWEBP)
  #add_subdirectory(libwebp)
  #target_sources(comitton PRIVATE comitton/ImageWebP.cpp)
endif()
if(WITH_AVIF)
  add_definitions(-DHAVE_LIBAVIF)
  add_subdirectory(dav1d)
  add_subdirectory(libavif)
  target_sources(comitton PRIVATE comitton/ImageAVIF.cpp)
endif()
if(WITH_HEIF)
  # 不要
  #add_definitions(-DHAVE_LIBHEIF)
  #add_subdirectory(dav1d)
  #add_subdirectory(libheif)
  #target_sources(comitton PRIVATE comitton/ImageHEIF.cpp)
endif()
if(WITH_JXL)
# TODO: add JXL support
  add_definitions(-DHAVE_LIBJXL)
  add_subdirectory(libjxl)
  target_sources(comitton PRIVATE comitton/ImageJXL.cpp)
endif()

include_directories(${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(comitton android jnigraphics log)

# オプションの値が前回のcmake実行から引き継がれないように削除しておく
unset(WITH_JPEG CACHE)
unset(WITH_PNG CACHE)
unset(WITH_GIF CACHE)
unset(WITH_WEBP CACHE)
unset(WITH_AVIF CACHE)
unset(WITH_HEIF CACHE)
unset(WITH_JXL CACHE)