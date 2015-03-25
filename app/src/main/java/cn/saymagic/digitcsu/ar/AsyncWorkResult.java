package cn.saymagic.digitcsu.ar;

/**
 * Created by saymagic on 15/3/16.
 */
public class AsyncWorkResult {

    private int what;
    private Object[] args;
    private AsyncTaskWork srcTask;

    public AsyncWorkResult(int what, Object... args) {
        this.setWhat(what);
        this.setArgs(args);
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public AsyncTaskWork getSrcTask() {
        return srcTask;
    }

    public void setSrcTask(AsyncTaskWork srcTask) {
        this.srcTask = srcTask;
    }
}
