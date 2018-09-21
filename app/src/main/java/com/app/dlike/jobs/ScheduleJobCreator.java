package com.app.dlike.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by moses on 9/18/18.
 */

public class ScheduleJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SchedulePostJob.TAG:
                return new SchedulePostJob();
            default:
                return null;
        }
    }
}
