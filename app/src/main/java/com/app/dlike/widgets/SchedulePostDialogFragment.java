package com.app.dlike.widgets;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.dlike.R;

import java.util.Calendar;

/**
 * Created by moses on 9/11/18.
 */

public class SchedulePostDialogFragment extends BottomSheetDialogFragment {

    private FrameLayout mainView;
    private View timeLayout, dateLayout;
    private Button nextButton, previousButton, finishButton;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private TimeChooseListener timeChooseListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_sheet_schedule_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainView = (FrameLayout) view;
        timeLayout = view.findViewById(R.id.timeLayout);
        dateLayout = view.findViewById(R.id.dateLayout);
        nextButton = view.findViewById(R.id.nextButton);
        previousButton = view.findViewById(R.id.previousButton);
        finishButton = view.findViewById(R.id.finishButton);

        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);

        init();
    }

    private Calendar getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, datePicker.getYear());
        calendar.set(Calendar.MONTH, datePicker.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

        calendar.set(Calendar.HOUR, timePicker.getCurrentHour() % 12);
        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, timePicker.getCurrentHour() > 12 ? Calendar.PM : Calendar.AM);

        return calendar;
    }

    private void init() {
        datePicker.setMinDate(System.currentTimeMillis());

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(mainView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    TransitionManager.beginDelayedTransition(mainView, new Slide());
                }
                dateLayout.setVisibility(View.GONE);
                timeLayout.setVisibility(View.VISIBLE);
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(mainView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    TransitionManager.beginDelayedTransition(mainView, new Slide());
                }
                dateLayout.setVisibility(View.VISIBLE);
                timeLayout.setVisibility(View.GONE);
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Calendar.getInstance().after(getDate())) {
                    Toast.makeText(v.getContext(), "The chosen time is in the past!!", Toast.LENGTH_SHORT).show();
                } else {
                    if (timeChooseListener != null) {
                        timeChooseListener.timeChosen(getDate());
                    }
                    dismiss();
                }
            }
        });
    }

    public void setTimeChooseListener(TimeChooseListener timeChooseListener) {
        this.timeChooseListener = timeChooseListener;
    }

    public TimeChooseListener getTimeChooseListener() {
        return timeChooseListener;
    }

    public interface TimeChooseListener {
        void timeChosen(Calendar time);
    }
}
