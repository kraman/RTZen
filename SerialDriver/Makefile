CXX = g++
CCFLAGS = -g -W -Wall
ARCH = x86

ifeq ($(CPU), X86)
  CXX = i586-linux-g++ 
endif

ifeq ($(CPU), PPC)
  CXX = powerpc-linux-g++
  ARCH = ppc
endif

ifeq ($(DEBUG), ON)
  CCFLAGS += -DDEBUG_ON
endif

ifeq ($(TARGET),)
  $(warning Default TARGET=PAYLOAD. make with TARGET=PAYLOAD | TARGET=GSTATION)
  CCFLAGS += -DPAYLOAD
else 
  ifeq ($(TARGET), PAYLOAD)
    CCFLAGS += -DPAYLOAD
  else 
    ifeq ($(TARGET), GSTATION)
    CCFLAGS += -DGSTATION
    endif
  endif
endif

INCLDIRS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

# rules ===============================

.PHONY : clean

libmessage_$(TARGET)_$(ARCH).so: message_lib.h message_lib.cpp serial_driver.h serial_driver.cpp edu_uci_ece_zen_orb_transport_serial_NativeSerialPort.cpp
	$(CXX) $(CCFLAGS) -fPIC $(INCLDIRS) -c message_lib.cpp serial_driver.cpp edu_uci_ece_zen_orb_transport_serial_NativeSerialPort.cpp
	$(CXX) -o libNativeSerialPort.so -shared $(INCLDIRS) edu_uci_ece_zen_orb_transport_serial_NativeSerialPort.o message_lib.o serial_driver.o -lpthread

all: libNativeSerialPort.so

clean:
	-rm *.o *.so Serial/*.class message_test_*	 

# DO NOT DELETE
