package MultithreadingSandboxPackage;

import java.util.LinkedList;
import java.util.concurrent.Executor;

public class SimpleThreadPool implements Executor
{
    private PoolWorker[] threads;
    private LinkedList queue;

    public SimpleThreadPool(int threadsNum)
    {
        queue = new LinkedList();
        threads = new PoolWorker[threadsNum];

        for (int i = 0; i < threadsNum; ++i)
        {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }
    
    @Override
    public void execute(Runnable command)
    {
        synchronized (queue)
        {
            queue.addLast(command);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread
    {
        @Override
        public void run()
        {
            Runnable runnable;

            while (true)
            {
                synchronized (queue)
                {
                    while (queue.isEmpty())
                    {
                        try { queue.wait(); }
                        catch (InterruptedException ex) { }
                    }

                    runnable = (Runnable) queue.removeFirst();
                }

                try { runnable.run(); }
                catch (RuntimeException ex) { }
            }
        }
    }
}