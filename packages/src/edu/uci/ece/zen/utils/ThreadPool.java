package edu.uci.ece.zen.utils;

import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.orb.transport.iiop.AcceptorRunnable;
import edu.uci.ece.zen.poa.HandleRequestRunnable;

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
            short defaultPriority, boolean allowRequestBuffering,
            int maxBufferedRequests, int requestBufferSize, ORB orb,
            AcceptorRunnable acceptorRunnable , int threadPoolId ) {
        //stackSize; //KLUDGE: ignored
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
            acceptorRunnable.init( orb , lanes[i].lane_priority , threadPoolId );
            orb.setUpORBChildRegion( acceptorRunnable );
            this.lanes[i] = new Lane(stackSize, lanes[i].static_threads,
                    lanes[i].dynamic_threads, lanes[i].lane_priority, this,
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

    public void execute(RequestMessage task, short minPriority, short maxPriority) {
        statCount++;
        if (statCount % ZenBuildProperties.MEM_STAT_COUNT == 0) {
            edu.uci.ece.zen.utils.Logger.printMemStats(ZenBuildProperties.dbgTransportScopeId);
        }

        //TODO: Have to improve performance here
        for (int i = 0; i < lanes.length; i++) {
            if (lanes[i].getPriority() >= minPriority && lanes[i].getPriority() <= maxPriority) 
		lanes[i].execute(task);
        }
    }
}

class Lane {
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

    private void newThread() {
        ThreadSleepRunnable r = new ThreadSleepRunnable(this);
        NoHeapRealtimeThread thr = new NoHeapRealtimeThread(null, null, null, RealtimeThread.getCurrentMemoryArea(), null, r);
        thr.setPriority(priority);
        r.setThread(thr);
        r.setNativePriority(priority);
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
