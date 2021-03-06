package de.aurora.mggvertretungsplan.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.datamodel.DateHeading;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.datamodel.TimeTableElement;
import de.aurora.mggvertretungsplan.ui.viewholder.ClassInfoViewHolder;
import de.aurora.mggvertretungsplan.ui.viewholder.HeadingsViewHolder;
import de.aurora.mggvertretungsplan.ui.viewholder.NoInfoViewHolder;

/**
 * Created by Rico on 26.09.2016.
 */

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CANCELLATION = 1;
    private static final int TYPE_NOINFO = 2;
    private final Context context;
    private final SharedPreferences sp;
    private final ArrayList<Object> items = new ArrayList<>();
    private int lastPosition = -1;

    public CardsAdapter(Context context) {
        this.context = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // When the RecyclerView gets created, Layouts are inflated and cached.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_CANCELLATION:
                View v1 = inflater.inflate(R.layout.view_time_table_card, parent, false);
                return new ClassInfoViewHolder(v1);

            case TYPE_HEADER:
                View v2 = inflater.inflate(R.layout.view_date_heading, parent, false);
                return new HeadingsViewHolder(v2);

            case TYPE_NOINFO:
            default:
                View v3 = inflater.inflate(R.layout.view_no_info_card, parent, false);
                return new NoInfoViewHolder(v3);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_CANCELLATION:
                configureCancellationViewholder(holder, position);
                break;

            case TYPE_HEADER:
                configureHeaderViewholder(holder, position);
                break;

            case TYPE_NOINFO:
                configureNoTypeViewholder(holder, position);
                break;
        }
    }

    // Methods to configure the used viewholders
    private void configureCancellationViewholder(RecyclerView.ViewHolder holder, int position) {
        ClassInfoViewHolder classInfoViewHolder = (ClassInfoViewHolder) holder;
        TimeTableElement timeTableElement = (TimeTableElement) items.get(position);

        classInfoViewHolder.hour.setText(timeTableElement.getHour());
        classInfoViewHolder.title.setText(timeTableElement.getSubject());
        classInfoViewHolder.info.setText(timeTableElement.getInfoForDisplay());
        classInfoViewHolder.room.setText(timeTableElement.getRoom());
        classInfoViewHolder.newRoom.setText(timeTableElement.getNewRoom());

        int color;
        int themeID = sp.getInt("Theme", 0);

        if (sp.getBoolean("listColors", true)) {

            switch (timeTableElement.getType()) {
                case TimeTableElement.SUBSTITUTION:
                    if (themeID == 5) {
                        color = context.getResources().getColor(R.color.cardSubstitutionDark);
                    } else {
                        color = context.getResources().getColor(R.color.cardSubstitution);
                    }
                    break;
                case TimeTableElement.CANCELLATION:
                default:
                    if (themeID == 5) {
                        color = context.getResources().getColor(R.color.cardCancellationDark);
                    } else {
                        color = context.getResources().getColor(R.color.cardCancellation);
                    }
            }

        } else {
            if (themeID == 5) {
                // If using dark theme, use dark card color
                color = context.getResources().getColor(R.color.cardNoColorDark);
            } else {
                color = context.getResources().getColor(R.color.cardNoColor);
            }
        }

        classInfoViewHolder.cardView.setCardBackgroundColor(color);

        setAnimation(classInfoViewHolder.cardView, position);
    }

    private void configureHeaderViewholder(RecyclerView.ViewHolder holder, int position) {
        HeadingsViewHolder headingsViewHolder = (HeadingsViewHolder) holder;

        DateHeading dateHeading = (DateHeading) items.get(position);
        String dateString = dateHeading.getWholeDate();

        headingsViewHolder.heading.setText(dateString);
        headingsViewHolder.tag_text.setText(dateHeading.getWeek().toString());
        boolean showWeekIndicator = sp.getBoolean("showWeekIndicator", true);
        int visibility;

        if (showWeekIndicator) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.INVISIBLE;
        }

        headingsViewHolder.tag_layout.setVisibility(visibility);

        setAnimation(headingsViewHolder.heading_layout, position);
    }

    private void configureNoTypeViewholder(RecyclerView.ViewHolder holder, int position) {
        NoInfoViewHolder noInfoViewHolder = (NoInfoViewHolder) holder;
        noInfoViewHolder.noInfo.setText(context.getResources().getString(R.string.card_no_information));

        int color;
        if (sp.getInt("Theme", 0) == 5) {
            color = context.getResources().getColor(R.color.cardNoColorDark);
        } else {
            color = context.getResources().getColor(R.color.cardNoColor);
        }

        noInfoViewHolder.cardView.setCardBackgroundColor(color);
        setAnimation(noInfoViewHolder.noInfo, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clearItems() {
        items.clear();
    }

    public void addDays(TimeTable timeTable) {
        for (TimeTableDay ttd : timeTable.getAllDays())
            addDay(ttd);
    }

    public void addDay(TimeTableDay ttd) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayPastDays = sp.getBoolean("displayPastDays", true);

        // Displays the current day only when the setting is active
        // OR when it's not set, but it's before 16:00
        // If the setting for displaying old days is deactivated, they will be removed here.
        if (!displayPastDays && !ttd.isInFuture()) {
            return;
        }

        String className = sp.getString("KlasseGesamt", "5a");
        ArrayList<TimeTableElement> timeTableElements = ttd.getElements(className);

        DateHeading dateHeading = new DateHeading(ttd.getDate(), ttd.getWeek());
        items.add(dateHeading);

        // TODO should be improved in the future
        if (timeTableElements.isEmpty()) {
            items.add(new TimeTableElement());
            return;
        }

        items.addAll(timeTableElements);
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof TimeTableElement) {
            if (((TimeTableElement) items.get(position)).getType() == TimeTableElement.EMPTY) {
                return TYPE_NOINFO;
            }
            return TYPE_CANCELLATION;
        } else if (items.get(position) instanceof DateHeading)
            return TYPE_HEADER;
        else
            return 0;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}
