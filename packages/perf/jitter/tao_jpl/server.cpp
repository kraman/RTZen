// server.cpp,v 1.11 2003/04/16 17:57:34 irfan Exp

#include "testS.h"
#include "ace/Get_Opt.h"
#include "tao/ORB_Core.h"
#include "ace/Task.h"
#include "tao/RTCORBA/RTCORBA.h"
#include "tao/RTPortableServer/RTPortableServer.h"
#include "check_supported_priorities.cpp"
//#include "tao/RTCORBA/Linear_Priority_Mapping.h" //Enable the linear priority mapping in TAO
//#include "tao/RTCORBA/rtcorba_typedefs.h"
//#include "tao/RTCORBA/Priority_Mapping.h"
//#include "tao/RTCORBA/Priority_Mapping_Manager.h"

class Test_i : public POA_Test
{
    // = TITLE
    //   An implementation for the Test interface in test.idl
    //
    public:
        Test_i (CORBA::ORB_ptr orb);
        // ctor

        //New method implementation for JPL Test
        int getMessage (int id  ,
                const MyArray &array
                ACE_ENV_ARG_DECL_NOT_USED)
            ACE_THROW_SPEC ((CORBA::SystemException));


        void test_method (CORBA::Short priority
                ACE_ENV_ARG_DECL_NOT_USED)
            ACE_THROW_SPEC ((CORBA::SystemException));

        void shutdown (ACE_ENV_SINGLE_ARG_DECL_NOT_USED)
            ACE_THROW_SPEC ((CORBA::SystemException));

    private:
        CORBA::ORB_var orb_;
        // The ORB
};

    Test_i::Test_i (CORBA::ORB_ptr orb)
:  orb_ (CORBA::ORB::_duplicate (orb))
{
}


int
Test_i::getMessage (int id  ,
        const MyArray & array
        //const MyArray  array
        ACE_ENV_ARG_DECL)
ACE_THROW_SPEC ((CORBA::SystemException))
{
    //printf("Testing %d, %d , %d\n",array[0],array[1],array[2]);
    //printf("Request is processing is the server, id number is: %d \n",id);
    return id;
}


void
Test_i::test_method (CORBA::Short priority
        ACE_ENV_ARG_DECL)
ACE_THROW_SPEC ((CORBA::SystemException)) //Actually this is not needed in the jpl demo test
{
    // Use RTCurrent to find out the CORBA priority of the current
    // thread.

    CORBA::Object_var obj =
        this->orb_->resolve_initial_references ("RTCurrent" ACE_ENV_ARG_PARAMETER);
    ACE_CHECK;

    RTCORBA::Current_var current =
        RTCORBA::Current::_narrow (obj.in () ACE_ENV_ARG_PARAMETER);
    ACE_CHECK;

    if (CORBA::is_nil (obj.in ()))
        ACE_THROW (CORBA::INTERNAL ());

    CORBA::Short servant_thread_priority =
        current->the_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
    ACE_CHECK;

    // Print out the info.
    if (servant_thread_priority != priority)
        ACE_DEBUG ((LM_DEBUG,
                    "ERROR: servant thread priority is not equal"
                    "to method argument.\n"));

    ACE_DEBUG ((LM_DEBUG,
                "Server_Declared priority: %d  "
                "Servant thread priority: %d\n",
                priority, servant_thread_priority));
}

    void
    Test_i::shutdown (ACE_ENV_SINGLE_ARG_DECL)
ACE_THROW_SPEC ((CORBA::SystemException)) //Actually this is not needed in the jpl demo test
{
    this->orb_->shutdown (0 ACE_ENV_ARG_PARAMETER);
}

//*************************************************************************

const char *ior_output_file = "test.ior";
const char *ior_output_file1 = "test1.ior";
const char *ior_output_file2 = "test2.ior";
CORBA::Short poa_priority = 0; //The default priority of low priority task
CORBA::Short object_priority = 32767; //The default priority of high priority task
//mode {client_propagated , server_declared}
int mode = 0;

// Parse command-line arguments.
    int
parse_args (int argc, char *argv[])
{
    ACE_Get_Opt get_opts (argc, argv, "m:l:h:");
    int c, result;

    while ((c = get_opts ()) != -1)
	    switch (c)
	    {

		    case 'm':
			    {
				    char *whichMode = get_opts.opt_arg ();
				    if(whichMode[0] == 'c' && whichMode[1] == 'p')
				    {
					    mode = 0;
					    printf(" Client_Propagated mode\n");
				    }
				    else
				    {
					    if(whichMode[0] == 's' && whichMode[1] == 'd' )
					    {
						    mode = 1;
						    printf(" Server_Declared mode\n");
					    }
					    else{
						    ACE_ERROR_RETURN ((LM_ERROR,
									    "usage:  %s "
									    "-m sd|cp Priority model(pm) of client-propagated(cp) or server declared(sd).\n"
									    "-l <poa_priority> The priority value for low priority task. The default value is 0\n"
									    "-h <object_priority> The priority value for high priority task. The default value is 32767\n"
									    "\n",
									    argv [0]),
								    -1);

					    }
				    } 
				    break;
			    }

		    case 'l':
			    {
				    result = ::sscanf (get_opts.opt_arg (),
						    "%hd",
						    &poa_priority);
				    if (result == 0 || result == EOF)
					    ACE_ERROR_RETURN ((LM_ERROR,
								    "Unable to process <-l> option"),
							    -1);
                                    printf("The low priority has been set to %d", poa_priority);
				    break;
			    }

		    case 'h':
			    {
				    result = ::sscanf (get_opts.opt_arg (),
						    "%hd",
						    &object_priority);
				    if (result == 0 || result == EOF)
					    ACE_ERROR_RETURN ((LM_ERROR,
								    "Unable to process <-h> option"),
							    -1);
                                    printf("The high priority has been set to %d", object_priority);
				    break;
			    }

		    case '?':
		    default:              
			    ACE_ERROR_RETURN ((LM_ERROR,
						    "usage:  %s "
						    "-m sd|cp Priority model(pm) of client-propagated(cp) or server declared(sd).\n"
						    "-l <poa_priority> The priority value for low priority task. The default value is 0\n"
						    "-h <object_priority> The priority value for high priority task. The default value is 32767\n"
                            "\n",
                            argv [0]),
                        -1);
        }
    return 0;
}

    int
check_for_nil (CORBA::Object_ptr obj, const char *msg)
{
    if (CORBA::is_nil (obj))
        ACE_ERROR_RETURN ((LM_ERROR,
                    "ERROR: Object reference <%s> is nil\n",
                    msg),
                -1);
    else
        return 0;
}

int
create_object (RTPortableServer::POA_ptr poa,
        CORBA::ORB_ptr orb,
        Test_i *server_impl,
        CORBA::Short priority,
        const char *filename
        ACE_ENV_ARG_DECL)
{
    // Register with poa.
    PortableServer::ObjectId_var id;

    if (priority > -1)
        id = poa->activate_object_with_priority (server_impl,
                priority
                ACE_ENV_ARG_PARAMETER);
    else
        id = poa->activate_object (server_impl ACE_ENV_ARG_PARAMETER);

    ACE_CHECK_RETURN (-1);

    CORBA::Object_var server =
        poa->id_to_reference (id.in ()
                ACE_ENV_ARG_PARAMETER);
    ACE_CHECK_RETURN (-1);

    // Print out the IOR.
    CORBA::String_var ior =
        orb->object_to_string (server.in () ACE_ENV_ARG_PARAMETER);
    ACE_CHECK_RETURN (-1);

    ACE_DEBUG ((LM_DEBUG, "<%s>\n\n", ior.in ()));

    // Print ior to the file.
    if (filename != 0)
    {
        FILE *output_file= ACE_OS::fopen (filename, "w");
        if (output_file == 0)
            ACE_ERROR_RETURN ((LM_ERROR,
                        "Cannot open output file for writing IOR: %s",
                        filename),
                    -1);
        ACE_OS::fprintf (output_file, "%s", ior.in ());
        ACE_OS::fclose (output_file);
    }

    return 0;
}

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
	    
	    
	    
        if(mode == 0) //Client Propagated Mode
        {


            //ACE_TRY_NEW_ENV
            //    {
            CORBA::Object_var object =
                this->orb_->resolve_initial_references("RootPOA" ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            PortableServer::POA_var root_poa =
                PortableServer::POA::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            if (CORBA::is_nil (root_poa.in ()))
                ACE_ERROR_RETURN ((LM_ERROR,
                            "ERROR: Panic <RootPOA> is nil\n"),
                        -1);

            PortableServer::POAManager_var poa_manager =
                root_poa->the_POAManager (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            object = this->orb_->resolve_initial_references ("RTORB" ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            RTCORBA::RTORB_var rt_orb = RTCORBA::RTORB::_narrow (object.in ()
                    ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            object =
                this->orb_->resolve_initial_references ("RTCurrent" ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            RTCORBA::Current_var current =
                RTCORBA::Current::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
	    
	    // Create POA with CLIENT_PROPAGATED PriorityModelPolicy,
            // and register Test object with it.
            CORBA::PolicyList poa_policy_list;
            poa_policy_list.length (1);
            poa_policy_list[0] =
                rt_orb->create_priority_model_policy (RTCORBA::CLIENT_PROPAGATED,
                        0
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            PortableServer::POA_var child_poa =
                root_poa->create_POA ("Child_POA",
                        poa_manager.in (),
                        poa_policy_list
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            Test_i server_impl (this->orb_.in ());

            PortableServer::ObjectId_var id =
                child_poa->activate_object (&server_impl ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            CORBA::Object_var server =
                child_poa->id_to_reference (id.in ()
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            // Print Object IOR.
            CORBA::String_var ior =
                this->orb_->object_to_string (server.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            ACE_DEBUG ((LM_DEBUG, "Activated as <%s>\n\n", ior.in ()));

            if (ior_output_file != 0)
            {
                FILE *output_file= ACE_OS::fopen (ior_output_file, "w");
                if (output_file == 0)
                    ACE_ERROR_RETURN ((LM_ERROR,
                                "Cannot open output file for writing IOR: %s",
                                ior_output_file),
                            -1);
                ACE_OS::fprintf (output_file, "%s", ior.in ());
                ACE_OS::fclose (output_file);
            }

            // Get the initial priority of the current thread.
            CORBA::Short initial_thread_priority =
                current->the_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            // Run ORB Event loop.
            poa_manager->activate (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            this->orb_->run (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            ACE_DEBUG ((LM_DEBUG, "Server ORB event loop finished\n"));

            // Get the final priority of the current thread.
            CORBA::Short final_thread_priority =
                current->the_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            if (final_thread_priority != initial_thread_priority)
                ACE_DEBUG ((LM_DEBUG,
                            "ERROR: Priority of the servant thread "
                            "has been permanently changed!\n"
                            "Initial priority: %d  Final priority: %d\n",
                            initial_thread_priority, final_thread_priority));
            else
                ACE_DEBUG ((LM_DEBUG,
                            "Final priority of the servant thread"
                            " = its initial priority\n"));
            /*$$$
              }
              ACE_CATCHANY
              {
              ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
              "Exception caught:");
              return -1;
              }
              ACE_ENDTRY;
              return 0;
              $$$*/
        }
        else //Server Declared Mode
        {
            // ACE_TRY_NEW_ENV
            //   {
            // RTORB.
            CORBA::Object_var object =
                this->orb_->resolve_initial_references ("RTORB" ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            RTCORBA::RTORB_var rt_orb = RTCORBA::RTORB::_narrow (object.in ()
                    ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (check_for_nil (rt_orb.in (), "RTORB") == -1)
                return -1;

            // RootPOA.
            object =
                this->orb_->resolve_initial_references("RootPOA" ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            PortableServer::POA_var root_poa =
                PortableServer::POA::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (check_for_nil (root_poa.in (), "RootPOA") == -1)
                return -1;

            // POAManager.
            PortableServer::POAManager_var poa_manager =
                root_poa->the_POAManager (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            // Create child POA with SERVER_DECLARED PriorityModelPolicy,
            // and MULTIPLE_ID id uniqueness policy (so we can use one
            // servant to create several objects).
            CORBA::PolicyList poa_policy_list;
            poa_policy_list.length (2);
            poa_policy_list[0] =
                rt_orb->create_priority_model_policy (RTCORBA::SERVER_DECLARED,
                        poa_priority
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            poa_policy_list[1] =
                root_poa->create_id_uniqueness_policy (PortableServer::MULTIPLE_ID
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            PortableServer::POA_var child_poa =
                root_poa->create_POA ("Child_POA",
                        poa_manager.in (),
                        poa_policy_list
                        ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            RTPortableServer::POA_var rt_poa =
                RTPortableServer::POA::_narrow (child_poa.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (check_for_nil (rt_poa.in (), "RTPOA") == -1)
                return -1;

            // Servant.
            Test_i server_impl (this->orb_.in ());

            // Create object 1 (it will inherit POA's priority).
            int result;
            ACE_DEBUG ((LM_DEBUG, "\nActivated object one as "));
            result = create_object (rt_poa.in (), this->orb_.in (), &server_impl,
                    -1, ior_output_file1 ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (result == -1)
                return -1;

            // Create object 2 (override POA's priority).
            ACE_DEBUG ((LM_DEBUG, "\nActivated object two as "));
            result = create_object (rt_poa.in (), this->orb_.in (), &server_impl,
                    object_priority, ior_output_file2 ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (result == -1)
                return -1;

            // Activate POA manager.
            poa_manager->activate (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            // Start ORB event loop.
            this->orb_->run (ACE_ENV_SINGLE_ARG_PARAMETER);
            ACE_TRY_CHECK;

            ACE_DEBUG ((LM_DEBUG, "Server ORB event loop finished\n\n"));
        }     
        }
        ACE_CATCHANY
        {
            ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION,
                    "Unexpected exception caught in Server_Declared test server:");
            return -1;
        }
        ACE_ENDTRY;
        return 0;		
    }

    int main (int argc, char *argv[])
        {

	    if (parse_args (argc, argv) != 0)
			return -1;
            ACE_TRY_NEW_ENV
            {
		    //Both Server declared Mode and Client Propagated Mode have same initialization code here

                    //ACE_TRY_NEW_ENV
                    //    {
                    // Standard initialization:
                    // parse arguments and get all the references (ORB,
                    // RootPOA, RTORB, RTCurrent, POAManager).
                    CORBA::ORB_var orb =
                        CORBA::ORB_init (argc, argv, "" ACE_ENV_ARG_PARAMETER);
                    ACE_TRY_CHECK;

                   /* 
		    
		    //Enable the linear priority mapping in TAO so that the priority range is increased from 0-127 to 0-32767
	            CORBA::Object_var object = orb->resolve_initial_references ("PriorityMappingManager");
	            //RTCORBA::PriorityMappingManager_var mapping_manager = RTCORBA::PriorityMappingManager::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
		    RTCORBA::PriorityMappingManager_var mapping_manager = RTCORBA::PriorityMappingManager::_narrow (object.in () ACE_ENV_ARG_PARAMETER);  
		    ACE_TRY_CHECK;	    
	            RTCORBA::PriorityMapping *pm = new TAO_Linear_Priority_Mapping (SCHED_OTHER); 
                    //RTCORBA::PriorityMapping *pm = new TAO_Linear_Priority_Mapping (SCHED_FIFO);
	            mapping_manager->mapping (pm); 
*/
                    
	            

                    //if (parse_args (argc, argv) != 0)
                    //    return -1;

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
	    }
                
            ACE_CATCHANY
            {
                ACE_PRINT_EXCEPTION (ACE_ANY_EXCEPTION, "Exception caught");
                return -1;
            }
            ACE_ENDTRY;
            return 0;

        }
