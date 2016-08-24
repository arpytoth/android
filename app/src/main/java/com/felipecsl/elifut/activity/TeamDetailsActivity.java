package com.felipecsl.elifut.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.felipecsl.elifut.R;
import com.felipecsl.elifut.adapter.ViewPagerAdapter;
import com.felipecsl.elifut.animations.AnimationUtil;
import com.felipecsl.elifut.fragment.TeamDetailsFragment;
import com.felipecsl.elifut.fragment.TeamSquadFragment;
import com.felipecsl.elifut.models.Club;
import com.felipecsl.elifut.models.League;
import com.felipecsl.elifut.models.Nation;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;

import static com.felipecsl.elifut.util.ColorUtils.colorizeTabsAndHeader;
import static com.google.common.base.Preconditions.checkNotNull;

public class TeamDetailsActivity extends ElifutActivity implements TabbedActivity {
  private static final String EXTRA_CLUB = "EXTRA_CLUB";

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.viewpager) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;

  @State Club club;
  @State Nation nation;
  @State League league;

  public static Intent newIntent(Context context, Club team) {
    return new Intent(context, TeamDetailsActivity.class)
        .putExtra(EXTRA_CLUB, team);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // See setupViewPager() below for why this exists
    ActivityCompat.postponeEnterTransition(this);
    AnimationUtil.setupActivityTransition(this);
    setContentView(R.layout.app_bar_team_details);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    if (savedInstanceState == null) {
      club = getIntent().getParcelableExtra(EXTRA_CLUB);
      nation = userPreferences.nation();
      league = userPreferences.league();
    }

    ActionBar actionBar = checkNotNull(getSupportActionBar());
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setTitle(club.shortName());
    setupViewPager();
  }

  private void setupViewPager() {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(TeamDetailsFragment.newInstance(club, "?", nation, league),
        getString(R.string.infos));
    adapter.addFragment(TeamSquadFragment.newInstance(club), getString(R.string.players));
    viewPager.setAdapter(adapter);
    tabLayout.setupWithViewPager(viewPager);
    // We need to delay the enter transition until after the tabLayout has finished adding the tabs
    // from the ViewPager adapter, which is when the team image will be available on the screen and
    // we can finally start the animation.
    // Ideally we should use a callback to know when that work is done, but since there doesn't
    // seem to exist one, we're using this ugly postDelayed hack below /shrug :)
    tabLayout.postDelayed(() -> ActivityCompat.startPostponedEnterTransition(this), 500);
  }

  @Override public void setToolbarColor(int primaryColor, int secondaryColor) {
    colorizeTabsAndHeader(this, toolbar, tabLayout, primaryColor, secondaryColor);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      ActivityCompat.finishAfterTransition(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
