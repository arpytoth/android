package com.felipecsl.elifut.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

import com.felipecsl.elifut.R;
import com.felipecsl.elifut.activity.TeamDetailsActivity;
import com.felipecsl.elifut.adapter.ClubsAdapter.HeaderViewHolder;
import com.felipecsl.elifut.adapter.ClubsAdapter.ViewHolder;
import com.felipecsl.elifut.models.Club;
import com.felipecsl.elifut.models.ClubStats;

import java.util.Collections;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ClubsAdapter
    extends RecyclerViewHeaderListAdapter<Club, Void, ViewHolder, HeaderViewHolder> {
  private final Club selectedClub;

  public ClubsAdapter(Club selectedClub) {
    super(Collections.emptyList(), null);
    this.selectedClub = checkNotNull(selectedClub);
  }

  @Override protected BaseViewHolder.Factory<HeaderViewHolder> headerFactory() {
    return (parent, viewType) -> new HeaderViewHolder(parent);
  }

  @Override protected BaseViewHolder.Factory<ViewHolder> itemFactory() {
    return (parent, viewType) -> new ViewHolder(parent);
  }

  class ViewHolderImpl {
    @BindView(R.id.layout) ViewGroup layout;
    @BindView(R.id.txt_position) TextView position;
    @BindView(R.id.txt_club_name) TextView clubName;
    @BindView(R.id.txt_points) TextView points;
    @BindView(R.id.txt_wins) TextView wins;
    @BindView(R.id.txt_draws) TextView draws;
    @BindView(R.id.txt_losses) TextView losses;
    @BindView(R.id.txt_goals_difference) TextView goalsDifference;
  }

  class ViewHolder extends BaseViewHolder<Club> {
    private final ViewHolderImpl views = new ViewHolderImpl();

    ViewHolder(ViewGroup parent) {
      super(parent, R.layout.club_item);
      ButterKnife.bind(views, itemView);
    }

    @Override public void bind(Club club) {
      ClubStats stats = club.nonNullStats();
      int typeface = selectedClub.nameEquals(club) ? Typeface.BOLD : Typeface.NORMAL;
      views.position.setText(String.valueOf(getAdapterPosition() + 1));
      views.points.setTypeface(null, typeface);
      views.points.setText(String.valueOf(stats.points()));
      views.clubName.setText(club.abbrev_name());
      views.clubName.setTypeface(null, typeface);
      views.wins.setTypeface(null, typeface);
      views.wins.setText(String.valueOf(stats.wins()));
      views.draws.setTypeface(null, typeface);
      views.draws.setText(String.valueOf(stats.draws()));
      views.losses.setTypeface(null, typeface);
      views.losses.setText(String.valueOf(stats.losses()));
      views.goalsDifference.setTypeface(null, typeface);
      views.goalsDifference.setText(String.valueOf(stats.goals()));
      Context context = itemView.getContext();
      views.layout.setOnClickListener(view -> context.startActivity(
          TeamDetailsActivity.newIntent(context, club)));
    }
  }

  class HeaderViewHolder extends BaseViewHolder<Void> {
    private final ViewHolderImpl views = new ViewHolderImpl();

    @BindColor(android.R.color.transparent) int transparent;
    @BindColor(R.color.club_table_header_bg) int headerBg;
    @BindColor(R.color.club_table_header_text_color) int headerTextColor;

    HeaderViewHolder(ViewGroup parent) {
      super(parent, R.layout.club_item);
      ButterKnife.bind(views, itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override public void bind(Void unused) {
      views.layout.setBackgroundColor(headerBg);
      views.clubName.setText(R.string.team);
      views.clubName.setTextColor(headerTextColor);
      views.clubName.setBackgroundColor(transparent);
      views.points.setText("P");
      views.points.setTextColor(headerTextColor);
      views.points.setBackgroundColor(transparent);
      views.wins.setText("W");
      views.wins.setTextColor(headerTextColor);
      views.wins.setBackgroundColor(transparent);
      views.draws.setText("D");
      views.draws.setTextColor(headerTextColor);
      views.draws.setBackgroundColor(transparent);
      views.losses.setText("L");
      views.losses.setTextColor(headerTextColor);
      views.losses.setBackgroundColor(transparent);
      views.goalsDifference.setText("G");
      views.goalsDifference.setTextColor(headerTextColor);
      views.goalsDifference.setBackgroundColor(transparent);
    }
  }
}
