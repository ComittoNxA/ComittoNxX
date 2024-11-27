# Windowsのとき
# BusyBox for Windowsがインストールされているかを確認する.
if("${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Windows")
  find_program(PATCH patch.exe)
  find_program(GREP grep.exe)
  find_program(XARGS xargs.exe)
  find_program(SED sed.exe)
  if((NOT PATCH) OR (NOT GREP) OR (NOT XARGS) OR (NOT SED))
    message(FATAL_ERROR "C:\\Program Files\\Git\\usr\\bin\\にパスを通しててください.")
  endif()
endif()

# オプションの値が前回のcmake実行から引き継がれないように削除しておく
unset(DOWNLOAD_UNRAR_SOURCES CACHE)
option(
  DOWNLOAD_UNRAR_SOURCES
  "Download UnRAR library, and patch. (Need Linux environment, not Windows)"
  ON
)

if(DOWNLOAD_UNRAR_SOURCES)
  set(UNRAR_VERSION "7.1.1")
  set(UNRAR_HASH_SHA256 "2e9cbc9d1c250b40f4a7a6a363b6ccfa3703e190534979d18c8c4ac5ae35dafc")

  set(FILE_NAME "unrarsrc-${UNRAR_VERSION}.tar.gz")
  FetchContent_Declare(
    unrar
    URL      https://www.rarlab.com/rar/${FILE_NAME}
    URL_HASH SHA256=${UNRAR_HASH_SHA256}
    DOWNLOAD_NO_EXTRACT true
  )
  FetchContent_MakeAvailable(unrar)

  execute_process(
    COMMAND ${CMAKE_COMMAND} -E tar xfz ${FILE_NAME}
    WORKING_DIRECTORY "${unrar_SOURCE_DIR}"
  )

  set(PATCH_PATH "${CMAKE_CURRENT_SOURCE_DIR}/unrar.patch")

  if("${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Windows")
    execute_process(
      #COMMAND "${BUSYBOX}" bash -c "${CMAKE_CURRENT_SOURCE_DIR}/patch-unrar.sh ${PATCH_PATH}"
      COMMAND cmd /C "${CMAKE_CURRENT_SOURCE_DIR}/patch-unrar.bat" "${PATCH_PATH}"
      WORKING_DIRECTORY "${unrar_SOURCE_DIR}/unrar"
      RESULT_VARIABLE BUILD_COMMAND_RESULT
      OUTPUT_VARIABLE BUILD_COMMAND_STDOUT
      ERROR_VARIABLE BUILD_COMMAND_STDERR
    )
  else()
    execute_process(
      COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/patch-unrar.sh "${PATCH_PATH}"
      WORKING_DIRECTORY "${unrar_SOURCE_DIR}/unrar"
      RESULT_VARIABLE BUILD_COMMAND_RESULT
      OUTPUT_VARIABLE BUILD_COMMAND_STDOUT
      ERROR_VARIABLE BUILD_COMMAND_STDERR
    )
  endif ()
  # ビルドの実行結果をログ出力する
  message(WARNING "Cmake: unrar: BUILD_COMMAND_RESULT = ${BUILD_COMMAND_RESULT}")
  message(WARNING "Cmake: unrar: BUILD_COMMAND_STDOUT = \n${BUILD_COMMAND_STDOUT}")
  message(WARNING "Cmake: unrar: BUILD_COMMAND_STDERR = \n${BUILD_COMMAND_STDERR}")
else()
  # Use local patched unrar sources
  set(unrar_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR})
endif ()

add_definitions(
  -D_FILE_OFFSET_BITS=64
  -D_LARGEFILE_SOURCE
  #  -DRAR_SMP
  #  -DUNRAR
  #  -DSILENT
)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-dangling-else -Wno-logical-op-parentheses -Wno-switch")

set(
  UNRAR_SOURCES
  ${unrar_SOURCE_DIR}/unrar/blake2s.cpp
  ${unrar_SOURCE_DIR}/unrar/crc.cpp
  ${unrar_SOURCE_DIR}/unrar/crypt.cpp
  ${unrar_SOURCE_DIR}/unrar/getbits.cpp
  ${unrar_SOURCE_DIR}/unrar/hash.cpp
  ${unrar_SOURCE_DIR}/unrar/rarvm.cpp
  ${unrar_SOURCE_DIR}/unrar/rdwrfn.cpp
  ${unrar_SOURCE_DIR}/unrar/rijndael.cpp
  ${unrar_SOURCE_DIR}/unrar/secpassword.cpp
  ${unrar_SOURCE_DIR}/unrar/sha1.cpp
  ${unrar_SOURCE_DIR}/unrar/sha256.cpp
  ${unrar_SOURCE_DIR}/unrar/strfn.cpp
  ${unrar_SOURCE_DIR}/unrar/timefn.cpp
  ${unrar_SOURCE_DIR}/unrar/unicode.cpp
  ${unrar_SOURCE_DIR}/unrar/unpack.cpp
  ${unrar_SOURCE_DIR}/unrar/largepage.cpp
)

add_library(unrar STATIC ${UNRAR_SOURCES})
include_directories(${unrar_SOURCE_DIR})
target_include_directories(comitton PRIVATE ${unrar_SOURCE_DIR})
target_link_libraries(comitton unrar)
