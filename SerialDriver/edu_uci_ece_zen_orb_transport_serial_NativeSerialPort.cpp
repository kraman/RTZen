#include "jni.h"
#include "edu_uci_ece_zen_orb_transport_serial_NativeSerialPort.h"
#include "message_lib.h"
#include "string.h"

JNIEXPORT jint JNICALL Java_edu_uci_ece_zen_orb_transport_serial_NativeSerialPort_getMessage(JNIEnv *env, jobject, jbyteArray msg)
{
  unsigned char cmsg[RECV_SIZE];
  memset(cmsg, 0, RECV_SIZE);

  int retval = getMessage(cmsg);

  if (retval != -1) {
    jbyte *jmsg = env->GetByteArrayElements(msg, 0);
    int msgLength = ((cmsg[0] << 8) & 0xFF) | (cmsg[1] & 0xFF);
    memcpy(jmsg, cmsg+2, msgLength);
    env->ReleaseByteArrayElements(msg, jmsg, 0);
    retval = msgLength;
  }

  return retval;
}

JNIEXPORT void JNICALL Java_edu_uci_ece_zen_orb_transport_serial_NativeSerialPort_setMessage(JNIEnv *env, jobject, jbyteArray msg, int msgLength)
{
  jbyte *jmsg = env->GetByteArrayElements(msg, 0);
  unsigned char cmsg[SEND_SIZE];

  memcpy(cmsg+2, jmsg, msgLength);
  env->ReleaseByteArrayElements(msg, jmsg, 0);

  cmsg[0] = (msgLength >> 8) & 0xFF;
  cmsg[1] = (msgLength     ) & 0xFF;

  setMessage((unsigned char*) cmsg);
}
