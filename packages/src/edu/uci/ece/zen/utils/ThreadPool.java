package edu.uci.ece.zen.utils;

import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import javax.realtime.InaccessibleAreaException;
import javax.realtime.MemoryArea;
import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.GetProfilesRunnable1;
import edu.uci.ece.zen.orb.GetProfilesRunnable2;
import edu.uci.ece.zen.orb.PriorityMappingImpl;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.orb.transport.iiop.AcceptorRunnable;
import edu.uci.ece.zen.orb.transport.Acceptor;
import edu.uci.ece.zen.poa.HandleRequestRunnable;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHelper;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import org.omg.RTCORBA.ThreadpoolLane;
import org.omg.RTCORBA.ThreadpoolLanesHelper;


public class ThreadPool {
    Lane lanes[];

    boolean allowRequestBuffering;

    int maxBufferedRequests;

    int requestBufferSize;

    boolean allowBorrowing;

    ORB orb;

    AcceptorRunnable acceptorRunnable;

    int threadPoolId;

    public ThreadPool(int stackSize, int staticThreads, int dynamicThreads,
            short defaultPr, boolean allowRequestBuffering,
            int maxBufferedRequests, int requestBufferSize, ORB orb,
            AcceptorRunnable acceptorRunnable , int threadPoolId ) {
        //stackSize; //KLUDGE: ignored
        short defaultPriority = defaultPr;//PriorityMappingImpl.toNative(defaultPr);
        this.allowRequestBuffering = allowRequestBuffering;
        this.maxBufferedRequests = maxBufferedRequests;
        this.requestBufferSize = requestBufferSize;
        this.allowBorrowing = false;
        this.acceptorRunnable = acceptorRunnable;

        this.lanes = new Lane[1];
        acceptorRunnable.init( orb , defaultPriority , threadPoolId );
        orb.setUpORBChildRegion( acceptorRunnable );
        this.lanes[0] = new Lane(stackSize, staticThreads, dynamicThreads,
                defaultPriority, this, allowBorrowing, allowRequestBuffering,
                maxBufferedRequests, acceptorRunnable.acceptorArea);
        this.lanes[0].setLaneId(0);
        this.orb = orb;
        this.threadPoolId = threadPoolId;
    }

    public ThreadPool(int stackSize, boolean allowRequestBuffering,
            int maxBufferedRequests, int requestBufferSize,
            CDROutputStream out, boolean allowBorrowing,
            ORB orb, AcceptorRunnable acceptorRunnable , int threadPoolId ) {
        //stackSize; //KLUDGE: ignored
        this.allowRequestBuffering = allowRequestBuffering;
        this.maxBufferedRequests = maxBufferedRequests;
        this.requestBufferSize = requestBufferSize;
        this.allowBorrowing = allowBorrowing;
        this.acceptorRunnable = acceptorRunnable;

        CDRInputStream in = (CDRInputStream)(out.create_input_stream());
        int length = in.read_ulong();
        this.lanes = new Lane[length];
        for (int i = 0; i < length; i++){
            short lane_priority = in.read_short();//PriorityMappingImpl.toNative(in.read_short());
            int static_threads = in.read_ulong();
            int dynamic_threads = in.read_ulong();
            if (ZenBuildProperties.dbgTP) ZenProperties.logger.log(
                    "Lane" + i + 
                    " created with priority: " + lane_priority + 
                    " static_threads: " + static_threads + 
                    " dynamic_threads: " + dynamic_threads);            
            acceptorRunnable.init( orb , lane_priority , threadPoolId );
            orb.setUpORBChildRegion( acceptorRunnable );
            this.lanes[i] = new Lane(stackSize, static_threads,
                    dynamic_threads, lane_priority, this,
                    allowBorrowing, allowRequestBuffering, maxBufferedRequests,
                    acceptorRunnable.acceptorArea);
            orb.setUpORBChildRegion(acceptorRunnable);
        }
        in.free();
        java.util.Arrays.sort(this.lanes, 0, this.lanes.length);
        for (int i = 0; i < this.lanes.length; i++)
            this.lanes[i].setLaneId(i);
        this.orb = orb;
        this.threadPoolId = threadPoolId;
    }    
    
    public ThreadPool(int stackSize, boolean allowRequestBuffering,
            int maxBufferedRequests, int requestBufferSize,
            org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allowBorrowing,
            ORB orb, AcceptorRunnable acceptorRunnable , int threadPoolId ) {
        //stackSize; //KLUDGE: ignored
        this.allowRequestBuffering = allowRequestBuffering;
        this.maxBufferedRequests = maxBufferedRequests;
        this.requestBufferSize = requestBufferSize;
        this.allowBorrowing = allowBorrowing;
        this.acceptorRunnable = acceptorRunnable;

        this.lanes = new Lane[lanes.length];
        for (int i = 0; i < lanes.length; i++){
            short lane_priority = lanes[i].lane_priority;//PriorityMappingImpl.toNative(lanes[i].lane_priority );
            acceptorRunnable.init( orb , lane_priority, threadPoolId );
            orb.setUpORBChildRegion( acceptorRunnable );
            this.lanes[i] = new Lane(stackSize, lanes[i].static_threads,
                    lanes[i].dynamic_threads, 
                    lane_priority, this,
                    allowBorrowing, allowRequestBuffering, maxBufferedRequests,
                    acceptorRunnable.acceptorArea);
            orb.setUpORBChildRegion(acceptorRunnable);
        }
        java.util.Arrays.sort(this.lanes, 0, lanes.length);
        for (int i = 0; i < this.lanes.length; i++)
            this.lanes[i].setLaneId(i);
        this.orb = orb;
        this.threadPoolId = threadPoolId;
    }

    public boolean borrowThreadAndExecute(RequestMessage task, int laneId) {
        if (laneId != 0) return lanes[laneId - 1].getLeaderAndExecute(task,
                true);
        return false;
    }

    private int statCount = 0;

    // TODO this method should indicate if we have an error.
    public void execute(RequestMessage task, short minPr, short maxPr) {
        short minPriority = minPr;//PriorityMappingImpl.toNative(minPr);
        short maxPriority = maxPr;//PriorityMappingImpl.toNative(maxPr);
        
        statCount++;
        if (statCount % ZenBuildProperties.MEM_STAT_COUNT == 0) {
            edu.uci.ece.zen.utils.Logger.printMemStats(ZenBuildProperties.dbgTransportScopeId);
        }

        //TODO: Have to improve performance here
        boolean laneFound = false;
        int i = 0;
        for (; i < lanes.length; i++) {
            short lanePriority = lanes[i].getPriority();
            if (ZenBuildProperties.dbgTP) ZenProperties.logger.log(
                        "TP min pr: " + minPriority + 
                        ", TP max pr: " + maxPriority + 
                        ", lane pr: " + lanePriority);
            if ( lanePriority >= minPriority && lanePriority <= maxPriority) {
                laneFound = true;
                break;
            }
        }

        if (laneFound) {
            lanes[i].execute(task);
        }
        else {
            ZenProperties.logger.log("No lane matched the request priority. Will execute at lowest priority lane.");
            lanes[0].execute(task);
        }
    }

    public Lane[] getLanes(){
        return lanes;
    }

    /**
     * Function to return all transport profiles for acceptors stored in this
     * registry. The profile objects are created in the client area that is
     * passed in.
     *
     * @param objKey
     *            The object key to embed in the profile.
     * @param clientArea
     *            The memory area to create the profiles in.
     * @return A array containing the list of transport profiles.
     *///TODO Leaks mem
    public /*TaggedProfile[]*/ void getProfiles(FString objKey, MemoryArea clientArea,
                POA poa, CDROutputStream out)
            {
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("-----TP.getProfiles0");
        
        GetProfilesRunnable1 gpr1 = GetProfilesRunnable1.instance();//new GetProfilesRunnable1(); //TODO -- static? per TP?
        //out.write_ulong(lanes.length);
        out.setProfileLengthMemento();
        //System.out.println("prof: " + out.toString());
        try{
            for(int i = 0; i < lanes.length; ++i){
                ScopedMemory accArea = lanes[i].getAcceptorArea();
                gpr1.init(i, accArea, objKey, clientArea, poa, out);
                orb.executeInORBRegion(gpr1);
            }
        }catch(Exception e){
            e.printStackTrace();//TODO better exception handling
        }
        //System.out.println("prof: " + out.toString());
        
        //System.out.println("prof: " + out.toString());
        //if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("-----TP.getProfiles2:  " + out.toString());
    }
    
    public static CDROutputStream marshalLanes(ThreadpoolLane[] lanes, ORB orb){
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        ThreadpoolLanesHelper.write(out, lanes);
        return out;
    }
}

class Lane implements Comparable{
    int stackSize; //ignored. No such provision in RTSJ 2.0

    int maxStaticThreads;

    int maxDynamicThreads;

    int numThreads;

    int laneId;

    short priority;

    boolean allowBorrowing;

    boolean allowRequestBuffering;

    int maxBufferedRequests;

    ThreadPool tp;

    Queue threads;

    Queue requestBuffer;

    int numBuffered;

    ScopedMemory acceptorArea;

    public Lane(int stackSize, int numStaticThreads, int numDynamicThreads,
            short priority, ThreadPool tp, boolean allowBorrowing,
            boolean allowRequestBuffering, int maxBufferedRequests,
            ScopedMemory acceptorArea) {
        this.stackSize = stackSize;
        this.maxStaticThreads = numStaticThreads;
        this.maxDynamicThreads = numDynamicThreads;
        this.priority = priority;
        this.tp = tp;
        this.allowBorrowing = allowBorrowing;
        this.threads = new Queue();
        this.requestBuffer = new Queue();
        this.numBuffered = 0;
        this.allowRequestBuffering = allowRequestBuffering;
        this.maxBufferedRequests = maxBufferedRequests;
        this.acceptorArea = acceptorArea;

        for (numThreads = 0; numThreads < maxStaticThreads; numThreads++) {
            newThread();
        }
    }

    public void setLaneId(int id) {
        laneId = id;
    }

    public int getLaneId() {
        return laneId;
    }

    public short getPriority() {
        return priority;
    }

    public ScopedMemory getAcceptorArea(){
        return acceptorArea;
    }

    private void newThread() {
        ThreadSleepRunnable r = new ThreadSleepRunnable(this);
        NoHeapRealtimeThread thr = new NoHeapRealtimeThread(null, null, null, RealtimeThread.getCurrentMemoryArea(), null, r);
        short pr = PriorityMappingImpl.toNative(priority);
        thr.setPriority(pr);
        r.setThread(thr);
        r.setNativePriority(pr);
        numThreads++;
        thr.start();
    }

    public synchronized boolean getLeaderAndExecute(RequestMessage task, boolean forBorrowing) {
    ThreadSleepRunnable thr = (ThreadSleepRunnable) threads.dequeue();
        if ( thr == null ) {
            //try to get a thread from somewhere else
            if (numThreads >= maxStaticThreads + maxDynamicThreads - 1) {
                //already at max thread limit, try borrowing
                if (allowBorrowing) {
                    boolean ret = tp.borrowThreadAndExecute(task, laneId);
                    if (!ret && allowRequestBuffering) {
                        synchronized (requestBuffer) {
                            if (numBuffered < maxBufferedRequests && !forBorrowing) {
                                requestBuffer.enqueue(task);
                                numBuffered++;
                            }
                            return true;
                        }
                    }
                    return ret;
                }
            } else {
                newThread();
            }
        }

        //still couldnt get a thread, wait for one to return
        try {
        if( thr == null ){
            while (((thr = (ThreadSleepRunnable) threads.dequeue()) == null)) {
                    synchronized (this) {
                        this.wait();
                    }
                }
        }
            return thr.execute(task);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "getLeaderAndExecute", e);
        e.printStackTrace();
            return false;
        }

    }

    public boolean execute(RequestMessage task) {
    boolean b = getLeaderAndExecute(task, false);
    //System.out.println( "Task executed with status: " + b );
    return b;
    }

    private boolean checkRequestBuffer() {
        //TODO: Fix this, it wont work for buffered requests this way.
        ThreadSleepRunnable runnable = null;
        synchronized (requestBuffer) {
            if (requestBuffer.isEmpty()) return false;
            else {
                runnable = (ThreadSleepRunnable) requestBuffer.dequeue();
                numBuffered--;
            }
        }

        runnable.run();
        returnToPool(runnable);
        return false;
    }

    public void returnToPool(ThreadSleepRunnable runnable) {
        if (!runnable.getLane().checkRequestBuffer()) {
            runnable.getLane().threads.enqueue(runnable);
        }
        synchronized (this) {
            this.notify();
        }
    }

    public void shutdown(boolean waitForCompletion) {
        while (!threads.isEmpty()) {
            ThreadSleepRunnable r = (ThreadSleepRunnable) threads.dequeue();
            r.shutdown(waitForCompletion);
        }
    }

    public int compareTo(Object e2) {
        if(!(e2 instanceof Lane))
            ZenProperties.logger.log("NOT instance of Lane!!");
        return priority - ((Lane) e2).priority;
    }
}

class ThreadSleepRunnable implements Runnable {
    private boolean isActive;

    private RequestMessage task;

    private EventVariable taskAvailableEvent;

    private Thread myThread;

    public Thread getThread() {
        return myThread;
    }

    public void setThread(Thread myThread) {
        this.myThread = myThread;
    }

    private short nativePriority;

    public short getNativePriority() {
        return nativePriority;
    }

    public void setNativePriority(short p) {
        nativePriority = p;
    }

    private Lane lane;

    public Lane getLane() {
        return lane;
    }

    ThreadSleepRunnable(Lane lane) {
        isActive = false;
        task = null;
        taskAvailableEvent = new EventVariable();
        this.lane = lane;
    }

    public boolean execute(RequestMessage task) {
        if (!isActive) return false;
        this.task = task;
        taskAvailableEvent.signal();
        return true;
    }

    public void run() {
        isActive = true;
        ExecuteInRunnable eir = new ExecuteInRunnable();
        HandleRequestRunnable ir = new HandleRequestRunnable();

        try {
            while (isActive) {
                lane.returnToPool(this);
                taskAvailableEvent.stall();

                //process the task in the portal of the scoped region
                ir.init(task);
                //System.out.println( "HandleRequestRunnable finished in
                // ThreadPool" );
                //System.out.println( task.getAssociatedPOA() );
                eir.init(ir, ((edu.uci.ece.zen.poa.POA) task.getAssociatedPOA()).poaMemoryArea);
                //System.out.println( "Calling executeInArea on
                // HandleRequestRunnable" );
                try {
                    lane.tp.orb.orbImplRegion.executeInArea(eir);
                } catch (Exception e) {
                    ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
                }
                //System.out.println( "Returned executeInArea on
                // HandleRequestRunnable" );
                task = null;
            }
        } catch (InterruptedException e) {
            ZenProperties.logger.log(Logger.INFO,
                    getClass(), "run",
                    "Recieved an Interrupt exception. Shutting down.");
            //Ignore. Expected while shutting down.
        } catch (Throwable e1) {
        //e1.printStackTrace();
            ZenProperties.logger.log(Logger.WARN, getClass(), "run", e1);
        }
    }

    public void shutdown(boolean waitForCompletion) {
        if (waitForCompletion) isActive = false;
        else myThread.interrupt();
    }
}
