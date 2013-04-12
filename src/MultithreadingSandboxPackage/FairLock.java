package MultithreadingSandboxPackage;

import java.util.concurrent.TimeUnit; // void tryLock(long time, TimeUnit unit)
import java.util.concurrent.locks.Condition; // Condition newCondition()
import java.util.concurrent.locks.Lock; // interface
import java.util.List;
import java.util.LinkedList;

public class FairLock implements Lock
{
    private Thread currentThread;
    private int state;
    private boolean isReentrant;
    private List<Boolean> threadsQueue;
    
    public FairLock(boolean reentrant)
    {
        currentThread = null;
        state = 0;
        isReentrant = reentrant;
        threadsQueue = new LinkedList<>();
    }
    
    private boolean isLocked()
    {
        return threadsQueue.size() > 0 ||
               currentThread != null && (isReentrant == false || state > 0);
    }
    
    // IMPLEMENTED:
    
    @Override
    public void lock()
    {
        // thread gets a token
        Boolean isNotified = new Boolean(false);        
        boolean isLocked = true;
        
        synchronized(this)
        {
            threadsQueue.add(isNotified);
        }

        while(isLocked)
        {
            synchronized(this)
            {
                isLocked = currentThread != null && (isReentrant == false || state > 0) || // somebody got the lock
                           threadsQueue.size() > 0 && threadsQueue.get(0) != isNotified; // we are not the first one in queue
                    
                if(isLocked == false) // no lock AND we are first
                {
                    threadsQueue.remove(isNotified);
                    currentThread = Thread.currentThread();
                    
                    if(isReentrant)
                    {
                        ++state;
                    }
                    
                    return;
                }
            }
            
            try
            {
                while(isNotified == false) // waiting for .set(0, true);
		{
                    wait();
		}		
		isNotified = false;
            }
            catch(InterruptedException e)
            {
                synchronized(this)
                {
                    threadsQueue.remove(isNotified);
                }
            }
        }
    }
    
    @Override
    public synchronized boolean tryLock()
    {
        if(isLocked())
            return false;
        
        lock();
        return true;
    }

    @Override
    public synchronized void unlock()
    {     
        if (currentThread != Thread.currentThread())
            return;
        
        if(isReentrant && --state > 0)
            return;
        
        currentThread = null;

        // Wakes up a single thread
        if(threadsQueue.isEmpty())
            return;
        
        threadsQueue.set(0, true);
        notify();
    }
    
    // NOT IMPLEMENTED:
    
    @Override
    public void lockInterruptibly() throws InterruptedException
    {
        throw new InterruptedException();
    }
    
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
    {
        throw new InterruptedException();
    }
    
    @Override
    public Condition newCondition()
    {
        return null;
    }
}