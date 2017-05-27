package com.fighter.common;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by haitengwang on 23/05/2017.
 */

/**
 *
 * This daemon can process various task that has different priority with high performance.
 *
 * Usage:
 *
 * PriorityTaskDaemon daemon = new PriorityTaskDaemon();
 * daemon.start();
 *
 * daemon.postTask(new NotifyPriorityTask(PriorityTask.PRI_FIRST + i,
 *     new TaskRunnable() {  // It's running in the daemon of background task thread
 *         @Override
 *         public Object doSomething() {
 *             // TODO ...
 *             return null;
 *         }
 *     },
 *     new TaskNotify() { // It's running in the looper thread of caller
 *         @Override
 *         public void onResult(NotifyPriorityTask task, Object result, TaskTiming timing) {
 *             // TODO ...
 *         }
 *     }));
 *
 * daemon.postTaskInFront(...);
 *
 * daemon.requestExitAndWait();
 *
 * //...
 */
public final class PriorityTaskDaemon extends Thread {

    private PriorityBlockingQueue mTaskQueue;
    private boolean mRequestExit = false;

    @SuppressLint("NewApi")
    public PriorityTaskDaemon() {
        super();
        mTaskQueue = new PriorityBlockingQueue<PriorityTask>(20, new Comparator<PriorityTask>() {
            @Override
            public int compare(PriorityTask lhs, PriorityTask rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        });
    }

    @Override
    public void run() {
        PriorityTask currentTask = null;
        while (!hasExitRequest()) {
            try {
                currentTask = (PriorityTask) mTaskQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentTask == null)
                continue;
            currentTask.run();
        }
    }

    private boolean hasExitRequest() {
        return mRequestExit;
    }

    /**
     * Request daemon to exit without blocking
     */
    public void requestExitAsync() {
        mRequestExit = true;
    }

    /**
     * Request daemon to exit until it's stop
     */
    public void requestExitAndWait() {
        mRequestExit = true;
        // Queue has inner lock
        mTaskQueue.offer(new PriorityTask(PriorityTask.PRI_LAST) {
            @Override
            public Object doSomethingInThread() {
                return null;
            }
        });
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Post a priority task to daemon that will do
     *
     * @param task
     * @return
     */
    public boolean postTask(PriorityTask task) {
        if (task == null)
            return false;
        if (!task.willLeaveLooper())
            return false;
        if (task.getPriority() != PriorityTask.PRI_SUPPER
                && (task.getPriority() < PriorityTask.PRI_FIRST
            || task.getPriority() > PriorityTask.PRI_LAST)) {
            // bad task, wrong priority
            return false;
        }
        mTaskQueue.offer(task);
        return true;
    }

    /**
     * Post a highest priority task to daemon that will be trigger firstly.
     *
     * @param task
     * @return
     */
    public boolean postTaskInFront(PriorityTask task) {
        task.mPriority = PriorityTask.PRI_SUPPER;
        return postTask(task);
    }

    /**
     * Remove a task that not running
     *
     * @param task
     */
    public void removeTask(PriorityTask task) {
        if (!task.isRunning()) {
            mTaskQueue.remove(task);
        }
    }

    /**
     * Get the rest task count of queue
     * @return
     */
    public int getRestTaskCount() {
        return mTaskQueue.size();
    }

    /**
     * Timing task process
     */
    public static class TaskTiming {

        public long start = 0;
        public long end = 0;
        public long elapsed = 0;

        private TaskTiming() {
            start = SystemClock.currentThreadTimeMillis();
        }

        private void end() {
            end = SystemClock.currentThreadTimeMillis();
            elapsed = end - start;
        }
    }

    /**
     * Priority task instance
     */
    public static abstract class PriorityTask {

        private static final int PRI_SUPPER = -0x10;
        public static final int PRI_FIRST = 0x00;
        public static final int PRI_LAST = 0x10;

        private int mPriority;
        private int mCreatedTime;
        private boolean mRunning;

        PriorityTask(int priority) {
            mPriority = priority;
            mRunning = false;
        }

        public int getPriority() {
            return mPriority;
        }

        public void setPriority(int priority) {
            mPriority = priority;
        }

        public boolean isRunning() {
            return mRunning;
        }

        private void run() {
            onPrepareInThread();
            mRunning = true;
            TaskTiming timing = new TaskTiming();
            Object obj = doSomethingInThread();
            timing.end();
            mRunning = false;
            onFinishInThread(obj, timing);
        }

        public void onPrepareInThread() {}
        public void onFinishInThread(Object obj, TaskTiming timing) {}
        public abstract Object doSomethingInThread();
        public boolean willLeaveLooper() { return true; }
    }

    /**
     * Notify instance will be trigger when task end
     */
    public interface TaskNotify {
        void onResult(NotifyPriorityTask task, Object result, TaskTiming timing);
    }

    private static class NotifyHandler extends Handler {
        private static final int MSG_NOTIFY = 0xa1;
        private NotifyPriorityTask mTask;
        private TaskNotify mNotify;

        public TaskNotify getNotify() {
            return mNotify;
        }

        NotifyHandler(Looper looper, NotifyPriorityTask task, TaskNotify notify) {
            super(looper);
            mTask = task;
            mNotify = notify;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NOTIFY: {
                    mNotify.onResult(mTask, mTask.mResult, mTask.mTiming);
                }
            }
        }
    }

    /**
     * Put you work in task runnable
     */
    public abstract static class TaskRunnable {
        private NotifyPriorityTask mTask = null;

        public abstract Object doSomething();
        public NotifyPriorityTask createNewTask(int priority, TaskRunnable runnable, TaskNotify notify) {
            return new NotifyPriorityTask(priority, runnable, notify, mTask.mNotifyHandler.getLooper());
        }
    }

    public static class NotifyPriorityTask extends PriorityTask {

        private Handler mNotifyHandler;
        private Object mResult = null;
        private TaskTiming mTiming = null;
        private TaskRunnable mRunnable;

        private TaskRunnable getRunnable() {
            return mRunnable;
        }

        private TaskNotify getNotify() {
            return ((NotifyHandler)mNotifyHandler).getNotify();
        }

        private Handler getHandle() {
            return mNotifyHandler;
        }

        public NotifyPriorityTask(NotifyPriorityTask task) {
            this(task.getPriority(), task.getRunnable(), task.getNotify(), task.getHandle().getLooper());
        }

        public NotifyPriorityTask(int priority, TaskRunnable runnable, TaskNotify notify) {
            this(priority, runnable, notify, Looper.myLooper());
        }

        private NotifyPriorityTask(int priority, TaskRunnable runnable, TaskNotify notify, Looper looper) {
            super(priority);
            mNotifyHandler = new NotifyHandler(looper, this, notify);
            mRunnable = runnable;
            mRunnable.mTask = this;
        }

        @Override
        public void onPrepareInThread() {
            super.onPrepareInThread();

        }

        @Override
        public Object doSomethingInThread() {
            return mRunnable != null ? mRunnable.doSomething() : null;
        }

        @Override
        public void onFinishInThread(Object obj, TaskTiming timing) {
            super.onFinishInThread(obj, timing);
            // send messages
            mResult = obj;
            mTiming = timing;
            mNotifyHandler.sendEmptyMessage(NotifyHandler.MSG_NOTIFY);
        }

        @Override
        public boolean willLeaveLooper() {
            super.willLeaveLooper();
            if (mNotifyHandler.getLooper() == null) {
                return false;
            }
            return true;
        }

        public NotifyPriorityTask createNewTask(int priority, TaskRunnable runnable, TaskNotify notify) {
            return new NotifyPriorityTask(priority, runnable, notify, mNotifyHandler.getLooper());
        }
    }

}
