#include "serial_driver.h"


// create singleton on stack insurring destructor is called
serial_driver& serial_driver::instance() {
  static serial_driver instance_;
  return instance_;
}

serial_driver::serial_driver() : 
#ifdef linux
  device_("/dev/ttyS0") 
#else /* WIN32 */
  device_("COM1")
#endif
{
  open_serial();
}

serial_driver::~serial_driver() {
  close_serial();
}

int
serial_driver::read(unsigned char *message, int size) {

#ifdef linux
  int n = 0;
  int bytes = 0;
  
  // check for bytes 
  while (!bytes)
  {
      ioctl(fd_, FIONREAD, &bytes);
      sleep(1);
  }

  if (bytes >= size) {
    n = ::read(fd_, message, size);
    DEBUG_OUT(bytes << " bytes available. read " << n);
  }
  else {
    DEBUG_OUT("bytes only " << bytes);
    return -1;
  }
  
  return n;

#else /* WIN32 */
  DWORD num = 0;
  unsigned char* s = message;
  
  // this is going to block
  /*
  for (int ctr = 0; ctr < size; ctr += SEND_INCR) {
    ReadFile(fh_, s, SEND_INCR, &n, 0);  
    num += n;
    s += SEND_INCR;
  }
  */
  
  ReadFile(fh_, message, size, &num, 0);
  DEBUG_OUT("read " << num << " total bytes.");
  
  return (int)num;
#endif

}

int 
serial_driver::write(unsigned char *message, int size) {

#ifdef linux
  int num = 0;
  
  // ppc cant' send 500 bytes at a time so we split this.
  
  /*
  unsigned char *s = message;
  for (int ctr = 0; ctr < size; ctr += SEND_INCR) {
    num += ::write(fd_, s, SEND_INCR);
    //DEBUG_OUT(fsync(fd_));
    //perror("fsync");
    s += SEND_INCR;
  }
  */
  num = ::write(fd_, message, size);
  DEBUG_OUT("wrote " << num << " total bytes"); fflush(stdout);

#else /* WIN32 */
  DWORD num = 0;
  WriteFile(fh_, message, size, &num, 0);
  DEBUG_OUT("wrote " << num << " bytes");
#endif

  return (int)num;
} 

int 
serial_driver::open_serial() {
#ifdef linux
  //  fd_ = open(device_.c_str(), O_RDWR | O_NOCTTY | O_NDELAY);
  fd_ = open(device_.c_str(), O_RDWR | O_NOCTTY | O_FSYNC);

  if (fd_ == -1) {
    std::cerr << "device : " << device_ << " ";
    perror("open port failed");
    return -1;
  }
  else {
    
    // save settings
    tcgetattr(fd_, &old_opt_);

    // zero options
    memset (&options, 0, sizeof(options));

    options.c_cflag = B57600 | CS8 | CLOCAL | CREAD;
   
    options.c_cc[VMIN] = 1;

    tcflush(fd_, TCIFLUSH);
    tcflush(fd_, TCOFLUSH);

    tcsetattr(fd_, TCSANOW, &options);

    DEBUG_OUT("Opened comm port " << device_);
  }

#else /* WIN32 */
	COMMTIMEOUTS timeouts;
	fh_ = CreateFile( device_.c_str(), 
			  GENERIC_READ | GENERIC_WRITE,
			  0,
			  0,
			  OPEN_EXISTING,
			  0,
			  0);
	GetCommState(fh_,&fhDCB_);
  	fhDCB_.BaudRate=CBR_57600;
	fhDCB_.fBinary = true;
	fhDCB_.fParity = false;
	
	fhDCB_.fOutxCtsFlow = false;
	fhDCB_.fOutxDsrFlow = false;

	fhDCB_.fDtrControl = DTR_CONTROL_DISABLE;
	fhDCB_.fRtsControl = RTS_CONTROL_DISABLE;

	fhDCB_.fDsrSensitivity = false;

	fhDCB_.fOutX = false;
	fhDCB_.fInX = false;

	fhDCB_.ByteSize = 8;
	fhDCB_.Parity = NOPARITY;
	fhDCB_.StopBits = ONESTOPBIT;

	SetCommState(fh_,&fhDCB_);
	SetupComm(fh_,4096,4096);
		
	// disable read timeouts
	GetCommTimeouts(fh_, &timeouts);
	timeouts.ReadIntervalTimeout = 0;
	SetCommTimeouts(fh_, &timeouts);

    DEBUG_OUT("Opened comm port " << device_);
#endif

  return 0;
}

int 
serial_driver::close_serial() {
#ifdef linux
	// restore port settings
    tcsetattr(fd_, TCSANOW, &old_opt_);

    if (fd_ != -1) {
      close(fd_);
      DEBUG_OUT("closing comm port " << device_);
      return 0;
    }
#else /* WIN32 */
    DEBUG_OUT("closing comm port " << device_);
	return CloseHandle(fh_);
#endif 

  return -1;
}
