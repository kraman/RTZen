#include "serial_driver.h"
#include "message_lib.h"


int getMessage(unsigned char *message) {
  return serial_driver::instance().read(message, RECV_SIZE);
}

int setMessage(unsigned char *message) { 
  return serial_driver::instance().write(message, SEND_SIZE); 
  
}
