#ifndef server_HH_
#define server_HH_

#include "jitterS.h"

class HelloWorld_impl : public virtual POA_perf::jitter::HelloWorld
{ 
public:
  virtual void putOctetSeq (const perf::jitter::OctetSeq & inSeq ) ACE_THROW_SPEC ( (CORBA::SystemException));
  virtual void putShortSeq (const perf::jitter::ShortSeq & inSeq ) ACE_THROW_SPEC (( CORBA::SystemException ));
};

#endif /* server_HH_ */

