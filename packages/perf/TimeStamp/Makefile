all: timestamp_compile 

timestamp_compile:
	gcc -o libttimestampjni.so -shared -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux TimeStamp_NativeTimeStamp.cpp NativeTimeStamp.cpp -L/usr/lib -lstdc++

