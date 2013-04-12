package MultithreadingSandboxPackage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class TestApp
{

    static class Friend
    {

        private final String name;
        private final Lock lock = new ReentrantLock();

//        private final Lock lock = new UnfairLock(false);
//        private final Lock lock = new UnfairLock(true);
//        private final Lock lock = new FairLock(false);
//        private final Lock lock = new FairLock(true);
        public Friend(String name)
        {
            this.name = name;
        }

        private String getName()
        {
            return this.name;
        }

        private boolean impendingBow(Friend bower)
        {
            Boolean myLock = false;
            Boolean yourLock = false;
            try
            {
                myLock = lock.tryLock();
                yourLock = bower.lock.tryLock();
            }
            finally
            {
                //if (!(myLock && yourLock))
                if (myLock == false || yourLock == false)
                {
                    if (myLock)
                    {
                        lock.unlock();
                    }
                    if (yourLock)
                    {
                        bower.lock.unlock();
                    }
                }
            }
            return myLock && yourLock;
        }

        public void bow(Friend bower)
        {
            if (impendingBow(bower))
            {
                try
                {
                    System.out.format("%s: %s has bowed to me!%n", this.name, bower.getName());
                    bower.bowBack(this);
                }
                finally
                {
                    lock.unlock();
                    bower.lock.unlock();
                }
            }
            else
            {
                System.out.format("%s: %s started to bow to me, but saw that I was already bowing to him.%n", this.name, bower.getName());
            }
        }

        private void bowBack(Friend bower)
        {
            System.out.format("%s: %s has bowed back to me!%n", this.name, bower.getName());
        }
    }

    static class BowLoop implements Runnable
    {

        private Friend bower;
        private Friend bowee;

        public BowLoop(Friend bower, Friend bowee)
        {
            this.bower = bower;
            this.bowee = bowee;
        }

        @Override
        public void run()
        {
            Random random = new Random();
            //for (;;)
            for (int i = 0; i < 100; ++i)
            {
                try
                {
                    Thread.sleep(random.nextInt(10));
                }
                catch (InterruptedException ex)
                {
                }
                bowee.bow(bower);
            }
        }
    }

    public static void main(String[] args)
    {
        if (false)
        {
            Friend A = new Friend("A");
            Friend B = new Friend("B");
            new Thread(new BowLoop(A, B)).start();
            new Thread(new BowLoop(B, A)).start();
        }
        else
        {
            Friend A = new Friend("A");
            Friend B = new Friend("B");
            Friend C = new Friend("C");
            Friend D = new Friend("D");
            SimpleThreadPool simpleThreadPool = new SimpleThreadPool(2);
            simpleThreadPool.execute(new BowLoop(A, B));
            simpleThreadPool.execute(new BowLoop(B, A));
            simpleThreadPool.execute(new BowLoop(C, D));
            simpleThreadPool.execute(new BowLoop(D, C));
        }
    }
}
