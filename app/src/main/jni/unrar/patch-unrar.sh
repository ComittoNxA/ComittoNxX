#!/usr/bin/env bash

PATCH_PATH=$1
patch -p1 < $PATCH_PATH
grep -l -r 'ErrHandler.MemoryError()' | xargs sed -i 's/ErrHandler.MemoryError(/abort(/g'
grep -l -r 'ErrHandler.GeneralErrMsg(' | xargs sed -i -E 's/ErrHandler\.GeneralErrMsg\(.+?\;/abort\(\)\;/g'
