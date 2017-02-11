package com.southernbox.inf.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.southernbox.inf.R;
import com.southernbox.inf.entity.Option;
import com.southernbox.inf.pager.ItemViewPager;
import com.southernbox.inf.util.RequestServes;
import com.southernbox.inf.util.ServerAPI;
import com.southernbox.inf.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created SouthernBox on 2016/3/27.
 * 主页
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context mContext;
    private Toolbar mToolbar;
    private DrawerLayout drawer;
    private List<Option> optionList;
    public static ArrayList<ItemViewPager> viewPagers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolBar();
        initDrawerLayout();
        initNavigationView();

        mContext = this;

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        final Realm mRealm = Realm.getInstance(realmConfig);

        // 优先加载本地缓存数据
        optionList = mRealm.where(Option.class).findAll();
        if (optionList != null) {
            initViewPager();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL + "/")
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
//                //增加返回值为Gson的支持(以实体类返回)
//                .addConverterFactory(GsonConverterFactory.create())
//                //增加返回值为Oservable<T>的支持
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        RequestServes requestServes = retrofit.create(RequestServes.class);//这里采用的是Java的动态代理模式
        Call<String> call = requestServes.getOption();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String responseString = response.body();
                Gson gson = new Gson();
                optionList = gson.fromJson(responseString, new TypeToken<List<Option>>() {
                }.getType());
                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(optionList);
                mRealm.commitTransaction();
                if (optionList != null) {
                    initViewPager();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ToastUtil.toastShow(mContext, "网络连接失败");
            }
        });
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDrawerLayout() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initViewPager() {
        viewPagers = new ArrayList<>();
        int size = optionList.size();
        for (int i = 0; i < size; i++) {
            viewPagers.add(new ItemViewPager(mContext, optionList.get(i)));
        }
        if (optionList.size() > 0) {
            viewPagers.get(0).initData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int position = 0;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_person) {
            position = 0;
        } else if (id == R.id.nav_house) {
            position = 1;
        } else if (id == R.id.nav_history) {
            position = 2;
        } else if (id == R.id.nav_castles) {
            position = 3;
        }

        viewPagers.get(position).initData();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                ToastUtil.toastShow(this, "再按一次退出");
                mExitTime = System.currentTimeMillis();
            } else {
                ToastUtil.toastCancel();
                finish();
            }
        }
        return true;
    }
}