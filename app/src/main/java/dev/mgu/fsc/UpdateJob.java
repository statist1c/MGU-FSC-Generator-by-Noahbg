package dev.mgu.fsc;
import android.app.job.*;
public class UpdateJob extends JobService {
    private Thread worker;
    @Override public boolean onStartJob(JobParameters p){worker=new Thread(()->{try{Updater.check(this);}catch(Exception ignored){/* Retain last successful result while offline. */}finally{jobFinished(p,false);}},"github-update-check");worker.start();return true;}
    @Override public boolean onStopJob(JobParameters p){if(worker!=null)worker.interrupt();return true;}
}
