#ifndef SERIAL_DRIVER_H
#define SERIAL_DRIVER_H

#include <iostream>

#include <string>

#ifdef linux
# include <unistd.h>
# include <fcntl.h>
# include <errno.h>
# include <termios.h>
# include <sys/ioctl.h>
#else /* WIN32 */
# include <Windows.h>
#endif

#define BUF_SIZE 500
#define SEND_INCR 50

#ifdef WIN32
# define DEBUG_ON
#endif 

#ifdef DEBUG_ON
# define DEBUG_OUT(x) std::cout << x << std::endl
#else 
# define DEBUG_OUT(x)
#endif


class serial_driver {
 public:
  static serial_driver& instance();

  int read(unsigned char *,  int size);
  int write(unsigned char *, int size);

 protected:
  serial_driver();
  ~serial_driver();

 private:

  int open_serial();
  int close_serial();

#ifdef linux
  int fd_;
  struct termios old_opt_;  // original port settings
  struct termios options;
#else /* WIN32 */
  HANDLE fh_;
  DCB fhDCB_;
#endif

  std::string device_;

};

#endif
