// $Id: client.cpp,v 1.5 2002/01/30 20:16:57 okellogg Exp $

#include "jitterC.h"
#include "ace/Get_Opt.h"
#include "NativeTimeStamp.h"
//#include "ace/High_Res_Timer.h"
//#include "ace/Sched_Params.h"
//#include "ace/Stats.h"
//#include "ace/Sample_History.h"

//#include "tao/Strategies/advanced_resource.h"


//TODO implement short sequence


const char *ior = "file://ior.txt";
int niterations = 10000;
int nWarmUpIterations = 5000;
int do_dump_history = 0;
int do_shutdown = 1;
int length = 128;

int
parse_args (int argc, char *argv[])
{
  ACE_Get_Opt get_opts (argc, argv, "k:l:");
  int c;

  while ((c = get_opts ()) != -1)
    switch (c)
      {
      case 'k':
        ior = get_opts.opt_arg ();
        break;

      case 'l':
        length = ACE_OS::atoi (get_opts.opt_arg ());
        break;

      case '?':
      default:
	ACE_ERROR_RETURN ((LM_ERROR,
				"usage:  %s "
				"-k <ior> "
				"-l <sequence length> "
				"\n",
				argv [0]),
			-1);
      }
  // Indicates sucessful parsing of the command line
  return 0;
}

	int
main (int argc, char *argv[])
{

	ACE_TRY_NEW_ENV
	{
		CORBA::ORB_var orb =
			CORBA::ORB_init (argc, argv, "" ACE_ENV_ARG_PARAMETER);
		ACE_TRY_CHECK;

		if (parse_args (argc, argv) != 0)
			return 1;

		CORBA::Object_var object =
			orb->string_to_object (ior ACE_ENV_ARG_PARAMETER);
		ACE_TRY_CHECK;

		perf::jitter::HelloWorld_var roundtrip =
			perf::jitter::HelloWorld::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
		ACE_TRY_CHECK;

		if (CORBA::is_nil (roundtrip.in ()))
		{
			ACE_ERROR_RETURN ((LM_ERROR,
						"Nil perf::jitter::HelloWorld reference <%s>\n",
						ior),
					1);
		}

		NativeTimeStamp timeStamp; 
		timeStamp.Init(1, 20.0);

		perf::jitter::OctetSeq *pOctetSeq;
		pOctetSeq = new perf::jitter::OctetSeq(length);

		for (int i = 0; i < length; i++) {
			(*pOctetSeq)[i] = (CORBA::Octet) (i & 0xFF);
		}


		ACE_DEBUG ((LM_DEBUG, "============= warm up =================\n"));
		for (int j = 0; j < nWarmUpIterations; ++j)
		{
                        timeStamp.RecordTime(21);
			(void) roundtrip->putOctetSeq (*pOctetSeq ACE_ENV_ARG_PARAMETER);
                        timeStamp.RecordTime(21);
			ACE_TRY_CHECK;
		}

		ACE_DEBUG ((LM_DEBUG, "============= performance test BYTE ====================\n"));

		//ACE_hrtime_t test_start = ACE_OS::gethrtime ();
		for (int i = 0; i < niterations; ++i)
		{
			timeStamp.RecordTime(22);
			(void) roundtrip->putOctetSeq (*pOctetSeq  ACE_ENV_ARG_PARAMETER);
			timeStamp.RecordTime(22);
			ACE_TRY_CHECK;
		}

		ACE_DEBUG ((LM_DEBUG, "test finished\n"));

		timeStamp.OutputLogRecords();

	}
	ACE_CATCHANY
	{
		ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION, "Exception caught:");
		return 1;
	}
	ACE_ENDTRY;

	return 0;
}
