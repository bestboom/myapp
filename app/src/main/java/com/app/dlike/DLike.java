package com.app.dlike;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.app.dlike.jobs.ScheduleJobCreator;
import com.evernote.android.job.JobManager;

/**
 * Created by moses on 8/22/18.
 */

public class DLike extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        JobManager.create(this).addJobCreator(new ScheduleJobCreator());
    }
}
