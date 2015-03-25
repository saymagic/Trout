package cn.saymagic.digitcsu.ar;

import java.util.concurrent.Callable;

import cn.saymagic.digitcsu.listener.NotifyListener;

/**
 * Created by saymagic on 15/3/16.
 */
public class AsyncTaskWarpper  {

    private static AsyncTaskWarpper warpper;

    public static synchronized AsyncTaskWarpper getInstance() {
        if (null == warpper) {
            warpper = new AsyncTaskWarpper();
        }
        return warpper;
    }

    private AsyncTaskWarpper() {
    }

    public AsyncTaskWork doAsyncWork(final Runnable work,
                                 NotifyListener cancelListener, NotifyListener finishListener) {
        AsyncTaskWork asyncWork = new AsyncTaskWork(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                work.run();
                return null;
            }
        }, true, cancelListener, finishListener);
        asyncWork.executeTask();
        return asyncWork;
    }

    public AsyncTaskWork doAsyncWork(final Callable<Object> work,
                                 NotifyListener cancelListener, NotifyListener finishListener) {
        AsyncTaskWork asyncWork = new AsyncTaskWork(work, true,
                cancelListener, finishListener);
        asyncWork.executeTask();
        return asyncWork;
    }

    public AsyncTaskWork doNotCancelableAsyncWork(final Callable<Object> work,
                                              NotifyListener finishListener) {
        AsyncTaskWork asyncWork = new AsyncTaskWork(work, false, finishListener);
        asyncWork.executeTask();
        return asyncWork;
    }

    public AsyncTaskWork doAsyncWork(final Runnable work,
                                 NotifyListener cancelListener, NotifyListener progresslListener,
                                 NotifyListener finishListener) {
        AsyncTaskWork asyncWork = new AsyncTaskWork(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                work.run();
                return null;
            }
        }, true, cancelListener, progresslListener, finishListener);
        asyncWork.executeTask();
        return asyncWork;
    }

    public AsyncTaskWork doAsyncWork(final Callable<Object> work,
                                 NotifyListener cancelListener, NotifyListener progresslListener,
                                 NotifyListener finishListener) {
        AsyncTaskWork asyncWork = new AsyncTaskWork(work, true,
                cancelListener, progresslListener, finishListener);
        asyncWork.executeTask();
        return asyncWork;
    }
}

