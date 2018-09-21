package com.app.dlike.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.dlike.R;
import com.app.dlike.fragments.DraftsFragment;

/**
 * Created by moses on 9/12/18.
 */

public class UserAccountViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String[] pageTitles = new String[]{
            "Posts",
            "Drafts",
            "Bookmarks",
            "Boards",
            "Activities",
            "Messages",
            "Comments & Replies"
    };

    public UserAccountViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TestFragment();
            case 1:
                return new DraftsFragment();
            case 2:
                return new TestFragment();
            case 3:
                return new TestFragment();
            case 4:
                return new TestFragment();
            case 5:
                return new TestFragment();
            case 6:
                return new TestFragment();
            default:
                return new TestFragment();
        }
    }

    @Override
    public int getCount() {
        return pageTitles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    public static class TestFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.test_layout, container, false);
        }
    }
}
