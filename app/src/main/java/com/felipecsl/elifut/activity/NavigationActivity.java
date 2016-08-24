package com.felipecsl.elifut.activity;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.felipecsl.elifut.AppInitializer;
import com.felipecsl.elifut.R;
import com.felipecsl.elifut.Util;
import com.felipecsl.elifut.animations.SimpleAnimatorListener;
import com.felipecsl.elifut.match.LeagueRoundExecutor;
import com.felipecsl.elifut.models.Club;
import com.felipecsl.elifut.models.LeagueRound;
import com.felipecsl.elifut.preferences.LeagueDetails;
import com.felipecsl.elifut.preferences.UserPreferences;
import com.felipecsl.elifut.util.AndroidVersion;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import rx.Subscription;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class NavigationActivity extends ElifutActivity
    implements NavigationView.OnNavigationItemSelectedListener {
  @Inject UserPreferences userPreferences;
  @Inject LeagueDetails leagueDetails;
  @Inject LeagueRoundExecutor roundExecutor;
  @Inject AppInitializer appInitializer;

  @Nullable @BindView(R.id.circular_reveal_overlay) View circularRevealOverlay;
  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view) NavigationView navigationView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @Nullable @BindView(R.id.fab) FloatingActionButton fab;
  @BindDimen(R.dimen.fab_margin) int fabMargin;

  private Subscription coinsSubscription;
  private final Action1<? super Long> coinsObserver = new Action1<Long>() {
    @Override public void call(Long aLong) {
      headerViewHolder.txtCoins.setText(String.format(
          Locale.getDefault(), "%d", userPreferences.coins()));
    }
  };

  static class NavigationHeaderViewHolder {
    @BindView(R.id.text_coach_name) TextView txtCoachName;
    @BindView(R.id.text_team_name) TextView txtTeamName;
    @BindView(R.id.img_club_logo) ImageView imgClubLogo;
    @BindView(R.id.text_coins) TextView txtCoins;
    @BindView(R.id.nav_header_layout) LinearLayout navHeaderLayout;
  }

  private final SimpleTarget clubLogoTarget = new SimpleTarget() {
    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
      headerViewHolder.imgClubLogo.setImageBitmap(bitmap);
      Palette.from(bitmap).generate(palette -> {
        ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
          @Override public Shader resize(int width, int height) {
            return new LinearGradient(0, 0, width, height,
                new int[] { palette.getDarkVibrantColor(0xFF81C784),
                    palette.getLightVibrantColor(0xFF2E7D32) }, null, Shader.TileMode.CLAMP);
          }
        };
        PaintDrawable paintDrawable = new PaintDrawable();
        paintDrawable.setShape(new RectShape());
        paintDrawable.setShaderFactory(shaderFactory);
        LayerDrawable background = new LayerDrawable(new Drawable[] { paintDrawable });
        //noinspection deprecation
        headerViewHolder.navHeaderLayout.setBackgroundDrawable(background);
      });
    }
  };

  private final NavigationHeaderViewHolder headerViewHolder = new NavigationHeaderViewHolder();

  @LayoutRes protected abstract int layoutId();

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(layoutId());
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
    daggerComponent().inject(this);

    Club club = checkNotNull(userPreferences.clubPreference().get());

    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    ButterKnife.bind(headerViewHolder, navigationView.getHeaderView(0));

    headerViewHolder.txtCoachName.setText(userPreferences.coach());
    headerViewHolder.txtTeamName.setText(club.name());
    coinsSubscription = userPreferences.coinsPreference().asObservable().subscribe(coinsObserver);

    Picasso.with(this)
        .load(club.large_image())
        .into(clubLogoTarget);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    coinsSubscription.unsubscribe();
  }

  @Override public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        drawerLayout.openDrawer(GravityCompat.START);
        return true;
      case R.id.action_abandon:
        new AlertDialog.Builder(this)
            .setMessage(R.string.abandon_confirmation)
            .setTitle(R.string.are_you_sure)
            .setPositiveButton(R.string.yes, (d, w) -> abandon(d))
            .setNegativeButton(R.string.no, (d, w) -> d.dismiss())
            .create()
            .show();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void abandon(DialogInterface d) {
    d.dismiss();
    appInitializer.clearData();
    finish();
    startActivity(MainActivity.newIntent(this));
  }

  @Override public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.nav_team:
        Club club = userPreferences.club();
        startActivity(CurrentTeamDetailsActivity.newIntent(this, club));
        finish();
        break;
      case R.id.nav_league:
        startActivity(LeagueDetailsActivity.newIntent(this));
        finish();
        break;
      case R.id.nav_online_friendly:
        startActivity(OnlineFriendlyActivity.Factory.newIntent(this));
        finish();
        break;
    }

    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Optional @OnClick(R.id.fab) public void onClickFab() {
    LeagueRound round = leagueDetails.executeRound(leagueDetails.nextRound());
    //noinspection ConstantConditions
    circularRevealOverlay.setVisibility(View.VISIBLE);
    int distFromEdge = fabMargin + (fab.getWidth() / 2);
    int cx = drawerLayout.getWidth() - distFromEdge;
    int cy = drawerLayout.getHeight() - distFromEdge;
    float finalRadius = Math.max(drawerLayout.getWidth(), drawerLayout.getHeight());
    if (AndroidVersion.isAtLeastLollipop()) {
      Animator circularReveal =
          ViewAnimationUtils.createCircularReveal(circularRevealOverlay, cx, cy, 0, finalRadius)
              .setDuration(400);
      circularReveal.addListener(new SimpleAnimatorListener() {
        @Override public void onAnimationEnd(Animator animation) {
          startMatchProgressActivity(round);
        }
      });
      circularReveal.start();
    } else {
      startMatchProgressActivity(round);
    }
  }

  private void startMatchProgressActivity(LeagueRound round) {
    startActivity(MatchProgressActivity.newIntent(NavigationActivity.this, round));
    //noinspection ConstantConditions
    circularRevealOverlay.postDelayed(() -> circularRevealOverlay.setVisibility(View.GONE), 1000);
    Util.defer(() -> roundExecutor.execute(round.matches()));
  }
}
