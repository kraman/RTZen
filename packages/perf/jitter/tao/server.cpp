#include "tao/corba.h"
#include "tao/PortableServer/PortableServer.h"
#include "server.h"


/* servant operation implementation */
void HelloWorld_impl::putOctetSeq (const perf::jitter::OctetSeq & inSeq , CORBA::Environment& ) 
  ACE_THROW_SPEC ( (CORBA::SystemException)) {
  /* don't do anything, just consume the message */
  return;
}


/* servant operation implementation */
void HelloWorld_impl::putShortSeq (const perf::jitter::ShortSeq & inSeq, CORBA::Environment& ) 
  ACE_THROW_SPEC (( CORBA::SystemException )) {
  /* don't do anything, just consume the message */
  return;
}


int main (int argc, char *argv[])
{
    try 
    {
      // Initialize orb
      printf("orb init\n");
      CORBA::ORB_var orb = CORBA::ORB_init (argc, (char**)argv);
        
      // Get reference to Root POA.
      printf("resolve_initial_references\n");

      CORBA::Object_var obj = orb->resolve_initial_references ("RootPOA");
      PortableServer::POA_var poa = PortableServer::POA::_narrow (obj.in ());

      // Activate POA manager
      PortableServer::POAManager_var mgr = poa->the_POAManager ();
      mgr->activate();

      // Create an object
      HelloWorld_impl HelloWorld_servant;

      // Write its stringified reference to stdout
      perf::jitter::HelloWorld_var myInterface = HelloWorld_servant._this ();
      CORBA::String_var str = orb->object_to_string (myInterface.in ());

      FILE* outFile = fopen("ior.txt", "w");
      if (outFile == NULL)
      {
          printf("Error: Cannot open IOR file for writing\n");
      }
      else
      {
          if (fprintf(outFile, "%s\n", str.in ()) <= 0)
          {
              printf("Error: Could not write IOR string to file\n");
          }

          fclose(outFile);
          
          printf("Listening for CORBA requests...\n");
          orb->run();
      }
    }
    catch (const CORBA::Exception &) 
    {
      printf("Uncaught CORBA exception");
    }
	
	return 0;
}

