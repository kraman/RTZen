// client.cpp,v 1.10 2003/04/16 17:57:34 irfan Exp

#include "testC.h"
#include "ace/Task.h"
#include "tao/RTCORBA/RTCORBA.h"
#include "Client_ORBInitializer.h"
#include "tao/RTCORBA/RTCORBA.h"
#include "tao/RTCORBA/Priority_Mapping_Manager.h"
#include "ace/Get_Opt.h"
#include "../check_supported_priorities.cpp"


const char *ior = "file://test.ior";
const char *ior1 = "file://test1.ior";
const char *ior2 = "file://test2.ior";
int whichIOR = 1;
int client_priority = 0;
//mode {client_propagated , server_declared}
int mode = 0;

int
parse_args (int argc, char *argv[])
{
	//w: another parameter to indicate which ior should be used
	//mode {Client propagated, Server declared}
	//p priority for Client Propagated
	
  ACE_Get_Opt get_opts (argc, argv, "m:p:w:");
  int c;

  while ((c = get_opts ()) != -1)
    switch (c)
      {
      case 'm':
        char *whichMode = get_opts.opt_arg ();
        if(whichMode[0] == 'c')
	{
		//Set Cleint priority here
		mode = 0;
		printf(" Client_Propagated mode\n");
	}
	else
	{
		//Set Cleint priority here
		mode = 1;
		printf(" Server_Declared mode\n");
	} 
        break;
	
	case 'p':
      	char *in_priority = get_opts.opt_arg ();
        if(in_priority[0] == '1')
	{
		//Set Cleint priority here
		client_priority = 10;
		printf(" Using %s with priority %d\n",ior , client_priority);
	}
	else
	{
		//Set Cleint priority here
		client_priority = 100;
		printf(" Using %s with priority %d\n",ior , client_priority);
	} 
	break;
      
      case 'w':
        char *iorNum = get_opts.opt_arg ();
        if(iorNum[0] == '1')
	{
		whichIOR = 1;
		printf(" Using %s\n",ior1);
	}
	else
	{
		whichIOR = 2;
		printf(" Using %s\n",ior2);
	}
	break;
      case '?':
      default:
        ACE_ERROR_RETURN ((LM_ERROR,
                           "usage:  %s "
                           "-p <ior> "
                           "-o <ior> "
                           "\n",
                           argv [0]),
                          -1);
      }

  return 0;
}

int
check_for_nil (CORBA::Object_ptr obj, const char *msg)
{
  if (CORBA::is_nil (obj) )
    ACE_ERROR_RETURN ((LM_ERROR,
                       "ERROR: Object reference <%s> is nil\n",
                       msg),
                      -1);
  else
    return 0;
}

CORBA::Short
check_policy (Test_ptr server
              ACE_ENV_ARG_DECL)
{
  CORBA::Policy_var policy =
    server->_get_policy (RTCORBA::PRIORITY_MODEL_POLICY_TYPE
                         ACE_ENV_ARG_PARAMETER);
  ACE_CHECK_RETURN (-1);

  RTCORBA::PriorityModelPolicy_var priority_policy =
    RTCORBA::PriorityModelPolicy::_narrow (policy.in () ACE_ENV_ARG_PARAMETER);
  ACE_CHECK_RETURN (-1);

  if (check_for_nil (priority_policy.in (), "PriorityModelPolicy") == -1)
    return -1;

  RTCORBA::PriorityModel priority_model =
    priority_policy->priority_model (ACE_ENV_SINGLE_ARG_PARAMETER);
  ACE_CHECK_RETURN (-1);
  if (priority_model != RTCORBA::SERVER_DECLARED)
    ACE_ERROR_RETURN ((LM_ERROR,
                       "ERROR: priority_model != "
                       "RTCORBA::SERVER_DECLARED!\n"),
                      -1);

  return priority_policy->server_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
}


//*******************************************************
//For Client propagated
class Task : public ACE_Task_Base
{
public:

  Task (ACE_Thread_Manager &thread_manager,
        CORBA::ORB_ptr orb);

  int svc (void);

  CORBA::ORB_var orb_;

};

Task::Task (ACE_Thread_Manager &thread_manager,
            CORBA::ORB_ptr orb)
  : ACE_Task_Base (&thread_manager),
    orb_ (CORBA::ORB::_duplicate (orb))
{
}

int
Task::svc (void)
{
  ACE_TRY_NEW_ENV
    {
      CORBA::Object_var object =
        this->orb_->string_to_object (ior ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      Test_var server =
        Test::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      if (CORBA::is_nil (server.in ()))
        {
          ACE_ERROR_RETURN ((LM_ERROR,
                             "ERROR: Object reference <%s> is nil\n",
                             ior),
                            -1);
        }

      // Check that the object is configured with CLIENT_PROPAGATED
      // PriorityModelPolicy.
      CORBA::Policy_var policy =
        server->_get_policy (RTCORBA::PRIORITY_MODEL_POLICY_TYPE
                             ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      RTCORBA::PriorityModelPolicy_var priority_policy =
        RTCORBA::PriorityModelPolicy::_narrow (policy.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      if (CORBA::is_nil (priority_policy.in ()))
        ACE_ERROR_RETURN ((LM_ERROR,
                           "ERROR: Priority Model Policy not exposed!\n"),
                          -1);

      RTCORBA::PriorityModel priority_model =
        priority_policy->priority_model (ACE_ENV_SINGLE_ARG_PARAMETER);
      ACE_TRY_CHECK;

      if (priority_model != RTCORBA::CLIENT_PROPAGATED)
        ACE_ERROR_RETURN ((LM_ERROR,
                           "ERROR: priority_model != "
                           "RTCORBA::CLIENT_PROPAGATED!\n"),
                          -1);

      // Make several invocation, changing the priority of this thread
      // for each.
      object =
        this->orb_->resolve_initial_references ("RTCurrent" ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;
      RTCORBA::Current_var current =
        RTCORBA::Current::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      object = this->orb_->resolve_initial_references ("PriorityMappingManager"
                                                       ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;
      RTCORBA::PriorityMappingManager_var mapping_manager =
        RTCORBA::PriorityMappingManager::_narrow (object.in ()
                                                  ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      RTCORBA::PriorityMapping *pm =
        mapping_manager->mapping ();

      int sched_policy =
        this->orb_->orb_core ()->orb_params ()->ace_sched_policy ();

      int max_priority =
        ACE_Sched_Params::priority_max (sched_policy);
      int min_priority =
        ACE_Sched_Params::priority_min (sched_policy);

      CORBA::Short native_priority =
        (max_priority + min_priority) / 2;

      CORBA::Short desired_priority = 0;

      if (pm->to_CORBA (native_priority, desired_priority) == 0)
        ACE_ERROR_RETURN ((LM_ERROR,
                           "Cannot convert native priority %d to corba priority\n",
                           native_priority),
                          -1);

      for (int i = 0; i < 3; ++i)
        {
          //current->the_priority (desired_priority ACE_ENV_ARG_PARAMETER);
	  current->the_priority (client_priority ACE_ENV_ARG_PARAMETER);
          ACE_TRY_CHECK;

          CORBA::Short priority =
            current->the_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
          ACE_TRY_CHECK;

          //if (desired_priority != priority)
	  //ACE_ERROR_RETURN ((LM_ERROR,
          //                     "ERROR: Unable to set thread "
          //                     "priority to %d\n", desired_priority),
          //                    -1);

          if (client_priority != priority)
            ACE_ERROR_RETURN ((LM_ERROR,
                               "ERROR: Unable to set thread "
                               "priority to %d\n", client_priority),
                              -1);


	  //Testing the JPL method
	  int length = 0;
	  if(client_priority == 10)
		  length = 100;
	  else
		  length = 10;
	  /*
	  int array[100];
          for(int i =0; i < arraySize; i++)
            {
                array[i] = i;
            }
	  */
	  MyArray *array;
	  array = new MyArray(length);
	  for (int i = 0; i < length; i++) {
		   (*array)[i] = (CORBA::Long)(i);
	   }
		   
	  server->getMessage (length , *array ACE_ENV_ARG_PARAMETER);
          ACE_TRY_CHECK;
			      
			      
          server->test_method (priority ACE_ENV_ARG_PARAMETER);
          ACE_TRY_CHECK;

          //desired_priority++;
	  
        }

      // Shut down Server ORB.
      server->shutdown (ACE_ENV_SINGLE_ARG_PARAMETER);
      ACE_TRY_CHECK;
    }
  ACE_CATCH (CORBA::DATA_CONVERSION, ex)
    {
      ACE_PRINT_EXCEPTION(ex,
                          "Most likely, this is due to the in-ability "
                          "to set the thread priority.");
      return -1;
    }
  ACE_CATCHANY
    {
      ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
                           "Caught exception:");
      return -1;
    }
  ACE_ENDTRY;

  return 0;
}



//******************************************************

int
main (int argc, char *argv[])
{
  ACE_TRY_NEW_ENV
  {
	if(mode == 0)
	{
		
		printf("Client Propagated client\n");
//  ACE_TRY_NEW_ENV
//    {
      // Register the interceptors to check for the RTCORBA
      // service contexts in the reply messages.
      PortableInterceptor::ORBInitializer_ptr temp_initializer;

      ACE_NEW_RETURN (temp_initializer,
                      Client_ORBInitializer,
                      -1);  // No exceptions yet!
      PortableInterceptor::ORBInitializer_var initializer =
        temp_initializer;

      PortableInterceptor::register_orb_initializer (initializer.in ()
                                                     ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      // Initialize and obtain reference to the Test object.
      CORBA::ORB_var orb =
        CORBA::ORB_init (argc, argv, "" ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      if (parse_args (argc, argv) != 0)
        return -1;

      // Make sure we can support multiple priorities that are required
      // for this test.
      check_supported_priorities (orb.in());

      // Thread Manager for managing task.
      ACE_Thread_Manager thread_manager;

      // Create task.
      Task task (thread_manager,
                 orb.in ());

      // Task activation flags.
      long flags =
        THR_NEW_LWP |
        THR_JOINABLE |
        orb->orb_core ()->orb_params ()->thread_creation_flags ();

      // Activate task.
      int result =
        task.activate (flags);
      if (result == -1)
        {
          if (errno == EPERM)
            {
              ACE_ERROR_RETURN ((LM_ERROR,
                                 "Cannot create thread with scheduling policy %s\n"
                                 "because the user does not have the appropriate privileges, terminating program....\n"
                                 "Check svc.conf options and/or run as root\n",
                                 sched_policy_name (orb->orb_core ()->orb_params ()->ace_sched_policy ())),
                                2);
            }
          else
            // Unexpected error.
            ACE_ASSERT (0);
        }

      // Wait for task to exit.
      result =
        thread_manager.wait ();
      ACE_ASSERT (result != -1);
/*$$$
    }
  ACE_CATCHANY
    {
      ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
                           "Caught exception:");
      return -1;
    }
  ACE_ENDTRY;

  return 0;
		
$$$*/	
	}
	else
	{
		
	printf("Server Declared client\n");
//  ACE_TRY_NEW_ENV
//    {
      // Initialize the ORB, resolve references and parse arguments.

      // ORB.
      CORBA::ORB_var orb =
        CORBA::ORB_init (argc, argv, "" ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      // Parse arguments.
      if (parse_args (argc, argv) != 0)
        return -1;

      // Test object 1.
      CORBA::Object_var object =
        orb->string_to_object (ior1 ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      Test_var server1 = Test::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;
      if (check_for_nil (server1.in (), "server1") == -1)
        return -1;

      // Test object 2.
      object = orb->string_to_object (ior2 ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      Test_var server2 = Test::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;
      if (check_for_nil (server2.in (), "server2") == -1)
        return -1;

      // Check that test objects are configured with SERVER_DECLARED
      // PriorityModelPolicy, and get their server priorities.

      // Test object 1.
      CORBA::Short server1_priority =
        check_policy (server1.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;

      if (server1_priority == -1)
        return -1;

      // Test object 2.
      CORBA::Short server2_priority =
        check_policy (server2.in () ACE_ENV_ARG_PARAMETER);
      ACE_TRY_CHECK;
      if (server2_priority == -1)
        return -1;

      // Testing: make several invocations on test objects.
      for (int i = 0; i < 1000; ++i)
        {
	  
	  //Testing the JPL method
	  if(whichIOR == 1)
	  {
		  //Lower priority with bigger array
		 /*
		  int array[100];
		  for(int i =0; i < 100; i++)
		  {
			  array[i] = i;
		  }
		  */
		  int length = 100;
		   MyArray *array;
		   array = new MyArray(length);

		   for (int i = 0; i < length; i++) {
			   (*array)[i] = (CORBA::Long)(i);
		   }
		   
		  server1->getMessage (1 , *array ACE_ENV_ARG_PARAMETER);
		  ACE_TRY_CHECK;
	  
		 // server1->test_method (server1_priority ACE_ENV_ARG_PARAMETER);
		 // ACE_TRY_CHECK;
		  printf(" Servant1 processing the request...\n");
	  }
	  else
	  {
		  //Higher priority with smaller array
		  /*
		  int array[100];
		  for(int i =0; i < 10; i++)
		  {
			  array[i] = i;
		  }
		  */
		   int length = 10;
		   MyArray *array;
		   array = new MyArray(length);

		   for (int i = 0; i < length; i++) {
			   (*array)[i] = (CORBA::Long)(i);
		   }
		   
		  server2->getMessage (2 , *array ACE_ENV_ARG_PARAMETER);
		  ACE_TRY_CHECK;

		  //server2->test_method (server2_priority ACE_ENV_ARG_PARAMETER);
		  //ACE_TRY_CHECK;
		  printf(" Servant2 processing the request...\n");
	  }
        }

      // Testing over. Shut down Server ORB.
      //server1->shutdown (ACE_ENV_SINGLE_ARG_PARAMETER);
      ACE_TRY_CHECK;
/*$$$
    }
  ACE_CATCHANY
    {
      ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
                           "Unexpected exception in Server_Declared test client:");
      return -1;
    }
  ACE_ENDTRY;

  return 0;
$$$*/
	}
 }
  ACE_CATCHANY
    {
      ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
                           "Unexpected exception in Server_Declared test client:");
      return -1;
    }
  ACE_ENDTRY;

  return 0;
	
}
