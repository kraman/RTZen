#include <perf_cPrint_nativePrinter.h>
#include <stdio.h>
/* JNI implementation for class perf_cPrint_nativePrinter */

/*
 * Class:     perf_cPrint_nativePrinter
 * Method:    print
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_perf_cPrint_nativePrinter_print
   (JNIEnv *pEnv, jclass pClass, jint a, jint b, jint c) {

  printf("%d, %d, %d\n", a, b, c);

  return;
}


