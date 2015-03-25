package cn.saymagic.digitcsu.ar;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import cn.saymagic.digitcsu.exception.ExceptionWarpper;
import cn.saymagic.digitcsu.listener.NotifyListener;

/**
 * Created by saymagic on 15/3/16.
 */
public class AsyncTaskWork  extends AdaptiveAsyncTask<Void, Object, Object> {
    static enum State {
        /** PENDING */
        PENDING,
        /** RUNNING */
        RUNNING,
        /** DONE */
        DONE,
        /** ABORT */
        ABORT
    }

    /** state */
    private State state = State.PENDING;

    /** finishListener */
    protected NotifyListener finishListener;

    /** cancleListener */
    protected NotifyListener cancleListener;

    /** progressListener */
    protected NotifyListener progressListener;

    /** work */
    private final Callable<Object> work;

    /** workChainHead */
    private WeakReference<AsyncTaskWork> workChainHead;

    /** allowCancel */
    private boolean allowCancel = true;

    public AsyncTaskWork(Callable<Object> work, boolean allowCancel,
                     NotifyListener finishListener) {
        this(work, allowCancel, null, null, finishListener);
    }

    public AsyncTaskWork(Callable<Object> work, boolean allowCancel,
                     NotifyListener cancelListener, NotifyListener finishListener) {
        this(work, allowCancel, cancelListener, null, finishListener);
    }

    public AsyncTaskWork(Callable<Object> work, boolean allowCancel,
                     NotifyListener cancelListener, NotifyListener progressListener,
                     NotifyListener finishListener) {
        if (null == work) {
            throw new NullPointerException();
        }
        this.work = work;
        this.allowCancel = allowCancel;
        this.cancleListener = cancelListener;
        this.finishListener = finishListener;
        this.progressListener = progressListener;
        workChainHead = new WeakReference<AsyncTaskWork>(this);
    }
    /* (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Object doInBackground(Void... params) {
        if (null != work) {
            try {
                changeState(State.RUNNING);
                Object data = work.call();
                Log.e("data",data.toString());
                AsyncWorkResult r = new AsyncWorkResult(ExceptionWarpper.NO_ERROR, data);
                r.setSrcTask(this);
                return r;
            } catch (Exception e) {
                if (e instanceof ExceptionWarpper) {
                    return new AsyncWorkResult(((ExceptionWarpper) e).getmExceptionType(), e);
                } else {
                    return new AsyncWorkResult(ExceptionWarpper.UNKNOWN_ERROR, e);
                }
            }
        }
        return null;
    }

    public boolean isDone() {
        return State.DONE.equals(state);
    }

    public boolean isAbort() {
        return State.ABORT.equals(state);
    }

    public boolean isRunning() {
        return State.RUNNING.equals(state);
    }

    protected void changeState(State state) {
        synchronized (state) {
            if (isAbort() || isDone()) {
                return;
            }
            this.state = state;
        }
    }


    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        changeState(State.DONE);
        if (isDone()) {
            if (null != finishListener) {
                finishListener.onNotify(result);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (null != progressListener && !isDone()) {
            progressListener.onNotify(values);
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        changeState(State.ABORT);
        if (isAbort()) {
            if (null != cancleListener) {
                cancleListener.onNotify(null);
            }
        }
    }

    /**
     * 如果一个流程中包含多个task，利用该方法把task串成task链，取消正在执行的task，则后续的都不会执行。
     * @param work
     */
    public void setWorkTask(AsyncTaskWork work) {
        this.workChainHead = new WeakReference<AsyncTaskWork>(work);
    }

    public boolean abort() {
        synchronized (state) {
            AsyncTaskWork aw = workChainHead.get();
            if (null != aw) {
                if (aw.allowCancel) {
                    if (!isDone()) {
                        changeState(State.ABORT);
                    }
                    if (this == aw) {
                        aw.cancel(true);
                        return isAbort();
                    } else {
                        return aw.abort();
                    }
                } else {
                    return false;
                }
            }
            return false;
        }
    }
}