// client1.cpp,v 1.10 2005/03/28 04:09:34 Hojjat Jafarpour
// client1.cpp,v 1.10 2005/03/28 04:09:34 Yue Zhang
// This is the high priority task 

#include "testC.h"
#include "ace/Task.h"
#include "tao/RTCORBA/RTCORBA.h"
#include "Client_ORBInitializer.h"
#include "tao/RTCORBA/RTCORBA.h"
#include "tao/RTCORBA/Priority_Mapping_Manager.h"
#include "ace/Get_Opt.h"
#include "check_supported_priorities.cpp"
#include <unistd.h>
#include "tao/RTCORBA/Linear_Priority_Mapping.h" //Enable the linear priority mapping in TAO
#include "tao/RTCORBA/rtcorba_typedefs.h"
#include "tao/RTCORBA/Priority_Mapping.h"
#include "tao/RTCORBA/Priority_Mapping_Manager.h"
#include <stdio.h>


const char *ior = "file://test.ior"; //The ior file for both high and low priority task if it's client declared
const char *ior1 = "file://test1.ior"; //The ior file for low priority task if it's server declared
const char *ior2 = "file://test2.ior"; //The ior file for high priority task if it's server declared
const int sleep_time = 666666000; //The unit is microsecond
const int iteration = 10000;
const int array_size = 10; //The size of the array for high priority task
const int warm_up = 3000;
int client_priority = 32767; //The default priority for high task is 0
const int size_of_record = 100000; //The size of timestamp array
//int whichIOR = 1;
//mode {client_propagated , server_declared}
int mode = 0;

typedef struct _Record{
  int pos;
  double time_stamp;
}Record;

Record record_list[size_of_record];
int index_id = 0;  

struct timeval begin_time, start_time, end_time; //Ed Pla's timestamping can't be used on Solaris

int parse_args (int argc, char *argv[])
{
    //mode {Client propagated, Server declared}

    ACE_Get_Opt get_opts (argc, argv, "m:p:");
    int c;

    while ((c = get_opts ()) != -1)
        switch (c)
        {
            case 'm': //mode {cp=Client propagated, sd=Server declared}
{
                char *whichMode = get_opts.opt_arg ();
                if(whichMode[0] == 'c' && whichMode[1] == 'p')
                {                   
                    mode = 0;
                    printf(" Client_Propagated mode\n");
                }
                else
                {
                    if(whichMode[0] == 's' && whichMode[1] == 'd')
                    {
                        mode = 1;
                        printf(" Server_Declared mode\n");
                    }
                    else{
                        ACE_ERROR_RETURN ((LM_ERROR,
                                    "usage:  %s \n"
                                    "-m sd|cp Priority model(pm) of client-propagated(cp) or server declared(sd).\n"
                                    "-p 0-32767 Priority of low-priority task. Defaults to CORBA min if not specified.\n"                            
                                    "\n",
                                    argv [0]),
                                -1);
                    }
                }
                break;
}
            case 'p': //p: priority for Client Propagated, the default value is 0
{
                char *in_priority = get_opts.opt_arg ();
                client_priority = atoi(in_priority);
                printf(" Using %s with priority %d\n", ior , client_priority);
                break;            
}
            case '?':
            default:
                ACE_ERROR_RETURN ((LM_ERROR,
                            "\nusage:  %s \n"
                            "-m sd|cp Priority model(pm) of client-propagated(cp) or server declared(sd).\n"
                            "-p 0-32767 Priority of low-priority task. Defaults to CORBA min if not specified.\n",              

                            argv [0]),
                        -1);
        }
    return 0;
}

    int check_for_nil (CORBA::Object_ptr obj, const char *msg) {
        if (CORBA::is_nil (obj) )
            ACE_ERROR_RETURN ((LM_ERROR,
                        "ERROR: Object reference <%s> is nil\n",
                        msg),
                    -1);
        else
            return 0;
    }
//Check if the policy of object is set to server declared
CORBA::Short check_sd_policy (Test_ptr server ACE_ENV_ARG_DECL)
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
//Check if the policy of object is set to client declared
CORBA::Short check_cp_policy (Test_ptr server ACE_ENV_ARG_DECL)
{
	// Check that the object is configured with CLIENT_PROPAGATED
        // PriorityModelPolicy.
        CORBA::Policy_var policy =
            server->_get_policy (RTCORBA::PRIORITY_MODEL_POLICY_TYPE
                    ACE_ENV_ARG_PARAMETER);
        ACE_CHECK_RETURN (-1);

        RTCORBA::PriorityModelPolicy_var priority_policy =
            RTCORBA::PriorityModelPolicy::_narrow (policy.in () ACE_ENV_ARG_PARAMETER);
        ACE_CHECK_RETURN (-1);

        if (CORBA::is_nil (priority_policy.in ()))
            ACE_ERROR_RETURN ((LM_ERROR,
                        "ERROR: Priority Model Policy not exposed!\n"),
                    -1);

        RTCORBA::PriorityModel priority_model =
            priority_policy->priority_model (ACE_ENV_SINGLE_ARG_PARAMETER);
        ACE_CHECK_RETURN (-1);

        if (priority_model != RTCORBA::CLIENT_PROPAGATED)
            ACE_ERROR_RETURN ((LM_ERROR,
                        "ERROR: priority_model != "
                        "RTCORBA::CLIENT_PROPAGATED!\n"),
                    -1);
		    
	return priority_policy->server_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
}


//*******************************************************
//For Client propagated only
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

int Task::svc (void)
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
	
	check_cp_policy(server.in () ACE_ENV_ARG_PARAMETER);

        // Make several invocation, changing the priority of this thread
        // for each.
        object =
            this->orb_->resolve_initial_references ("RTCurrent" ACE_ENV_ARG_PARAMETER);
        ACE_TRY_CHECK;
        RTCORBA::Current_var current =
            RTCORBA::Current::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
        ACE_TRY_CHECK;
        /*
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

        //current->the_priority (desired_priority ACE_ENV_ARG_PARAMETER);*/
        current->the_priority (client_priority ACE_ENV_ARG_PARAMETER);
        ACE_TRY_CHECK;

        CORBA::Short priority =
            current->the_priority (ACE_ENV_SINGLE_ARG_PARAMETER);
        ACE_TRY_CHECK;        

        if (client_priority != priority)
            ACE_ERROR_RETURN ((LM_ERROR,
                        "ERROR: Unable to set thread "
                        "priority to %d\n", client_priority),
                    -1);           

        MyArray *array;
        array = new MyArray(array_size);

        for (int i = 0; i < array_size; i++) {
            (*array)[i] = (CORBA::Long)(i);
        }
	
	gettimeofday(&begin_time, NULL);
	

        printf("------Warm Up2------\n");
        for(int i=0;i<warm_up;i++){

            if((i%500)==0){
                printf("High Priority---%d\n",i);
            }
	    
	    gettimeofday(&start_time, NULL);
            server->getMessage (2 , *array ACE_ENV_ARG_PARAMETER);
	    gettimeofday(&end_time, NULL);	    
            ACE_TRY_CHECK;
	    record_list[index_id].pos = 21;
            record_list[index_id++].time_stamp = (start_time.tv_sec-begin_time.tv_sec)*1000000 + (start_time.tv_usec-begin_time.tv_usec);
          
            record_list[index_id].pos = 21;
            record_list[index_id++].time_stamp = (end_time.tv_sec-begin_time.tv_sec)*1000000 + (end_time.tv_usec-begin_time.tv_usec);
	    
            //server->test_method (priority ACE_ENV_ARG_PARAMETER);
           // ACE_TRY_CHECK;
            usleep(sleep_time);

        }
        printf("------Performance test2------\n");
        for(int i=0;i<iteration;i++){

            if((i%500)==0){
                printf("High Priority---%d\n",i);
            }

            gettimeofday(&start_time, NULL);
            server->getMessage (2 , *array ACE_ENV_ARG_PARAMETER);
	    gettimeofday(&end_time, NULL);	    
            ACE_TRY_CHECK;
	    record_list[index_id].pos = 22;
            record_list[index_id++].time_stamp = (start_time.tv_sec-begin_time.tv_sec)*1000000 + (start_time.tv_usec-begin_time.tv_usec);
          
            record_list[index_id].pos = 22;
            record_list[index_id++].time_stamp = (end_time.tv_sec-begin_time.tv_sec)*1000000 + (end_time.tv_usec-begin_time.tv_usec);
            ACE_TRY_CHECK;
            //server->test_method (priority ACE_ENV_ARG_PARAMETER);
            //ACE_TRY_CHECK;
            usleep(sleep_time);

        }


        //desired_priority++;
        // Shut down Server ORB.
        //server->shutdown (ACE_ENV_SINGLE_ARG_PARAMETER);
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

int main (int argc, char *argv[])
{
    if (parse_args (argc, argv) != 0)
        return -1;
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
	    
	        //Enable the linear priority mapping in TAO so that the priority range is increased from 0-127 to 0-32767
	         CORBA::Object_var object_mapping = orb->resolve_initial_references ("PriorityMappingManager");
	            RTCORBA::PriorityMappingManager_var mapping_manager = RTCORBA::PriorityMappingManager::_narrow (object_mapping.in () ACE_ENV_ARG_PARAMETER);  
		    ACE_TRY_CHECK;	    
	            RTCORBA::PriorityMapping *pm = new TAO_Linear_Priority_Mapping (SCHED_OTHER); 
	            mapping_manager->mapping (pm); 

            // if (parse_args (argc, argv) != 0)
            // return -1;

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
	    
	        //Enable the linear priority mapping in TAO so that the priority range is increased from 0-127 to 0-32767
	           CORBA::Object_var object_mapping = orb->resolve_initial_references ("PriorityMappingManager");
	            RTCORBA::PriorityMappingManager_var mapping_manager = RTCORBA::PriorityMappingManager::_narrow (object_mapping.in () ACE_ENV_ARG_PARAMETER);  
		    ACE_TRY_CHECK;	    
	            RTCORBA::PriorityMapping *pm = new TAO_Linear_Priority_Mapping (SCHED_OTHER); 
	            mapping_manager->mapping (pm); 

            // Parse arguments.
            //if (parse_args (argc, argv) != 0)
            // return -1;

            // Test object 2.
            CORBA::Object_var object =
                orb->string_to_object (ior2 ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            Test_var server2 = Test::_narrow (object.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;
            if (check_for_nil (server2.in (), "server1") == -1)
                return -1;
            // Test object 2.
            CORBA::Short server2_priority =
                check_sd_policy (server2.in () ACE_ENV_ARG_PARAMETER);
            ACE_TRY_CHECK;

            if (server2_priority == -1)
                return -1;
            
            // Testing: make several invocations on test objects.
            MyArray *array;

            array = new MyArray(array_size);

            for (int i = 0; i < array_size; i++) {
                (*array)[i] = (CORBA::Long)(i);
            }
	    
	    gettimeofday(&begin_time, NULL);
	   

            printf("------Warm Up2------\n");
            for (int i = 0; i < warm_up; ++i)
            {
                if((i%500)==0){
                    printf("High Priority---%d\n",i);
                }
                gettimeofday(&start_time, NULL);
            server2->getMessage (2 , *array ACE_ENV_ARG_PARAMETER);
	    gettimeofday(&end_time, NULL);	    
            ACE_TRY_CHECK;
	    record_list[index_id].pos = 21;
            record_list[index_id++].time_stamp = (start_time.tv_sec-begin_time.tv_sec)*1000000 + (start_time.tv_usec-begin_time.tv_usec);
          
            record_list[index_id].pos = 21;
            record_list[index_id++].time_stamp = (end_time.tv_sec-begin_time.tv_sec)*1000000 + (end_time.tv_usec-begin_time.tv_usec);
            
		//server2->test_method (server2_priority ACE_ENV_ARG_PARAMETER);
                //ACE_TRY_CHECK;
                usleep(sleep_time);	
                
            }       
            printf("------Perfromance Test2------\n"); 
            for (int i = 0; i < iteration; ++i)
            {              
                if((i%500)==0){
                    printf("High Priority---%d\n",i);
                }
		
		gettimeofday(&start_time, NULL);
            server2->getMessage (2 , *array ACE_ENV_ARG_PARAMETER);
	    gettimeofday(&end_time, NULL);	    
            ACE_TRY_CHECK;
	    record_list[index_id].pos = 22;
            record_list[index_id++].time_stamp = (start_time.tv_sec-begin_time.tv_sec)*1000000 + (start_time.tv_usec-begin_time.tv_usec);
          
            record_list[index_id].pos = 22;
            record_list[index_id++].time_stamp = (end_time.tv_sec-begin_time.tv_sec)*1000000 + (end_time.tv_usec-begin_time.tv_usec);
            ACE_TRY_CHECK;

                
                usleep(sleep_time);
                
            }

            // Testing over. Shut down Server ORB.
            //server1->shutdown (ACE_ENV_SINGLE_ARG_PARAMETER);
            //ACE_TRY_CHECK;
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
    
    FILE* _file = fopen("timeRecords.1.2.2.128.txt","w+");

    for(int i=0; i<index_id; i++)
    {
          fprintf(_file, "%d,%d,%f\n", i, record_list[i].pos, record_list[i].time_stamp);
    }
    fclose(_file);
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
