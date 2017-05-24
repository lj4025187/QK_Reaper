package com.fighter.reaper;

import android.os.HandlerThread;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.common.PriorityTaskDaemon;
import com.fighter.common.PriorityTaskDaemon.NotifyPriorityTask;
import com.fighter.common.PriorityTaskDaemon.PriorityTask;
import com.fighter.common.PriorityTaskDaemon.TaskNotify;
import com.fighter.common.PriorityTaskDaemon.TaskRunnable;
import com.fighter.common.PriorityTaskDaemon.TaskTiming;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

/**
 * Created by haitengwang on 23/05/2017.
 */

@RunWith(AndroidJUnit4.class)
public class PriorityTaskDaemonTest {


    @Test
    public void testRunMultiTasks() {
        final String str = new String("Hello I'm PriorityTaskDaemon!!!");


        final CountDownLatch latch = new CountDownLatch(1);

        final PriorityTaskDaemon daemon = new PriorityTaskDaemon();
        daemon.start();

        HandlerThread testThread = new HandlerThread("test thread") {
            @Override
            public void onLooperPrepared() {
                for (int i = 0; i <= PriorityTask.PRI_LAST - PriorityTask.PRI_FIRST; ++i) {

                    daemon.postTask(new NotifyPriorityTask(PriorityTask.PRI_FIRST + i,
                            new TaskRunnable() {
                                @Override
                                public Object doSomething() {
                                    Log.d("daemon", "doSomething" + ", thread:" + Thread.currentThread().getId());
                                    int sum = 1;
                                    for (int i = 1; i < 100000; ++i)
                                        sum += i;
                                    Log.d("daemon", "sum: " + sum);
                                    return str;
                                }
                            },
                            new TaskNotify() {
                                @Override
                                public void onResult(NotifyPriorityTask task, Object result, TaskTiming timing) {
                                    Log.d("daemon", "task[" + task.getPriority() + "] used: " + timing.elapsed + " ms");
                                    assertEquals(str, result);
                                    if (task.getPriority() == PriorityTask.PRI_LAST) {
                                        daemon.requestExitAndWait();
                                        Log.d("daemon", "daemon exited");
                                        latch.countDown();
                                    }
                                }
                            }));
                }
                daemon.postTaskInFront(new NotifyPriorityTask(PriorityTask.PRI_FIRST,
                        new TaskRunnable() {
                            @Override
                            public Object doSomething() {
                                return null;
                            }
                        },
                        new TaskNotify() {
                            @Override
                            public void onResult(NotifyPriorityTask task, Object result, TaskTiming timing) {
                                Log.d("daemon", "task[" + task.getPriority() + "] used: " + timing.elapsed + " ms");
                            }
                        }));
            }
        };
        testThread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testThread.getLooper().quit();

        Log.d("daemon", "thread - looper:" + Thread.currentThread().getId());
    }
}
