LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

jni_c_files := jpgf.c jni_utils.c
pgf_c_files := data.c expr.c graphviz.c linearizer.c literals.c parser.c parseval.c pgf.c printer.c reader.c \
reasoner.c evaluator.c jit.c typechecker.c lookup.c aligner.c writer.c scanner.c
gu_c_files := assert.c  choice.c  exn.c   fun.c   in.c      map.c  out.c     utf8.c \
bits.c    defs.c    enum.c  file.c  hash.c  mem.c  prime.c  seq.c   string.c  ucs.c   variant.c

LOCAL_MODULE    := jpgf
LOCAL_SRC_FILES := $(addprefix ../../../gf-core/src/runtime/java/, $(jni_c_files)) \
                   $(addprefix ../../../gf-core/src/runtime/c/pgf/, $(pgf_c_files)) \
                   $(addprefix ../../../gf-core/src/runtime/c/gu/, $(gu_c_files))
LOCAL_C_INCLUDES := /home/krasimir/GF/gf-core/src/runtime/c

include $(BUILD_SHARED_LIBRARY)

$(realpath ../obj/local/armeabi/objs/jpgf/__/__/__/runtime/c/pgf/jit.o): lightning
$(realpath ../obj/local/armeabi/objs-debug/jpgf/__/__/__/runtime/c/pgf/jit.o): lightning

lightning:
	ln -s -f arm/asm.h ../../../../runtime/c/pgf/lightning/asm.h
	ln -s -f arm/core.h ../../../../runtime/c/pgf/lightning/core.h
	ln -s -f arm/fp.h ../../../../runtime/c/pgf/lightning/fp.h
	ln -s -f arm/funcs.h ../../../../runtime/c/pgf/lightning/funcs.h


include $(CLEAR_VARS)

daison_c_files ::= c/sqlite3Btree.c java/jdaison.c

LOCAL_MODULE    := jdaison
LOCAL_SRC_FILES := $(addprefix ../../../../daison/, $(daison_c_files))
LOCAL_C_INCLUDES := /home/krasimir/daison/c
LOCAL_LDLIBS := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
