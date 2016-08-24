package com.felipecsl.elifut.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.felipecsl.elifut.R;
import com.felipecsl.elifut.adapter.ViewPagerAdapter;
import com.felipecsl.elifut.fragment.LeagueProgressFragment;
import com.felipecsl.elifut.fragment.LeagueStandingsFragment;
import com.felipecsl.elifut.models.League;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;

import static com.felipecsl.elifut.util.ColorUtils.colorizeTabsAndHeader;
import static com.google.common.base.Preconditions.checkNotNull;

public class LeagueDetailsActivity extends NavigationActivity {
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.viewpager) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;
  @BindView(R.id.fab) FloatingActionButton fab;

  @State League league;

  public static Intent newIntent(Context context) {
    return new Intent(context, LeagueDetailsActivity.class);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    if (savedInstanceState == null) {
      league = userPreferences.league();
    }

    navigationView.setCheckedItem(R.id.nav_league);

    ActionBar actionBar = checkNotNull(getSupportActionBar());
    actionBar.setTitle(league.name());
    setupViewPager();
  }

  private void setupViewPager() {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(LeagueProgressFragment.newInstance(), getString(R.string.schedule));
    adapter.addFragment(LeagueStandingsFragment.newInstance(), getString(R.string.standings));
    viewPager.setAdapter(adapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override protected int layoutId() {
    return R.layout.activity_league_details;
  }

  public void setToolbarColor(int primaryColor, int secondaryColor) {
    colorizeTabsAndHeader(this, toolbar, tabLayout, primaryColor, secondaryColor);
  }
}
