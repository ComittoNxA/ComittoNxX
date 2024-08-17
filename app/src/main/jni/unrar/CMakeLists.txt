option(
  DOWNLOAD_UNRAR_SOURCES
  "Download UnRAR library, and patch. (Need Linux environment, not Windows)"
  OFF
)

if(DOWNLOAD_UNRAR_SOURCES)
  set(UNRAR_VERSION "6.2.1")
  set(UNRAR_HASH_SHA256 "5cc8f7ded262d27c29d01e7a119d2fd23edda427711820454f2eb667044a8900")

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

  execute_process(
    COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/patch-unrar.sh "${PATCH_PATH}"
    WORKING_DIRECTORY "${unrar_SOURCE_DIR}/unrar"
  )
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
)

add_library(unrar STATIC ${UNRAR_SOURCES})
include_directories(${unrar_SOURCE_DIR})
target_include_directories(comitton PRIVATE ${unrar_SOURCE_DIR})
target_link_libraries(comitton unrar)
