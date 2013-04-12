package MultithreadingSandboxPackage;

import java.util.concurrent.TimeUnit; // void tryLock(long time, TimeUnit unit)
import java.util.concurrent.locks.Condition; // Condition newCondition()
import java.util.concurrent.locks.Lock; // interface

public class UnfairLock implements Lock
{
    private Thread currentThread;
    private int state;
    private boolean isReentrant;
    
    public UnfairLock(boolean reentrant)
    {
        currentThread = null;
        state = 0;
        isReentrant = reentrant;
    }
    
    private boolean isLocked()
    {
        return currentThread != null &&
                (isReentrant == false || state > 0);
    }
          
    // IMPLEMENTED:
    
    @Override
    public synchronized void lock()
    {
        while (isLocked()) 
        {
            try { wait(); }
            catch (InterruptedException ex) { }
        }

        currentThread = Thread.currentThread();
        
        if(isReentrant)
        {
            ++state;
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