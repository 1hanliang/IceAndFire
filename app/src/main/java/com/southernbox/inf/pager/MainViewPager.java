package com.southernbox.inf.pager;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.southernbox.inf.R;
import com.southernbox.inf.activity.MainActivity;
import com.southernbox.inf.adapter.MainFragmentPagerAdapter;
import com.southernbox.inf.entity.TabDTO;
import com.southernbox.inf.fragment.MainFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SouthernBox on 2016/3/28.
 * 首页ViewPager
 */

public class MainViewPager {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ArrayList<MainFragment> fragments;
    private String title;
    private String[] tabTitles;
    private List<TabDTO> tabList;
    private MainActivity mainActivity;

    public MainViewPager(Context mContext, String title, List<TabDTO> tabList) {
        this.title = title;
        this.tabList = tabList;
        mainActivity = (MainActivity) mContext;
        initView();
    }

    private void initView() {
        mToolbar = (Toolbar) mainActivity.findViewById(R.id.main_toolbar);
        mTabLayout = (TabLayout) mainActivity.findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) mainActivity.findViewById(R.id.view_pager);
        mViewPager.removeAllViews();
    }

    public void initData() {
        initFragment();
        mToolbar.setTitle(title);
        mViewPager.setAdapter(new MainFragmentPagerAdapter(
                mainActivity.getSupportFragmentManager(),
                fragments, tabTitles));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initFragment() {
        fragments = new ArrayList<>();
        int size = tabList.size();
        tabTitles = new String[size];
        for (int i = 0; i < size; i++) {
            TabDTO tab = tabList.get(i);
            tabTitles[i] = tab.getTitle();
            MainFragment fragment = MainFragment
                    .newInstance(tab.getFirstType(), tab.getSecondType());
            fragments.add(fragment);
        }
    }

    public void refreshUI() {
        for (MainFragment fragment : fragments) {
            fragment.refreshUI();
        }
    }
}
