#ifndef MESSAGE_LIB_H
#define MESSAGE_LIB_H

#ifdef WIN32
# define GSTATION
#endif 

#ifdef PAYLOAD
//# define SEND_SIZE 500
# define SEND_SIZE 89
# define RECV_SIZE 89
#elif defined (GSTATION)
# define SEND_SIZE 89
# define RECV_SIZE 445
//# define RECV_SIZE 500
#endif

#ifndef PAYLOAD
# ifndef GSTATION
#  error TARGET unspecified. build as make TARGET=PAYLOAD|GSTATION
# endif 
#endif

// returns -1 if no byte or not enough bytes available
int getMessage(unsigned char *);

int setMessage(unsigned char *);

#endif /* MESSAGE_LIB_H */
