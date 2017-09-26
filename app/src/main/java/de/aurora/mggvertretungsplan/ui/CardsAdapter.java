package de.aurora.mggvertretungsplan.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.Date;

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
        classInfoViewHolder.info.setText(timeTableElement.getInfo());
        classInfoViewHolder.room.setText(timeTableElement.getRoom());
        classInfoViewHolder.newRoom.setText(timeTableElement.getNewRoom());

        if (sp.getBoolean("listColors", true)) {
            classInfoViewHolder.cardView.setCardBackgroundColor(timeTableElement.getColor());
        } else {
            classInfoViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        setAnimation(classInfoViewHolder.cardView, position);
    }

    private void configureHeaderViewholder(RecyclerView.ViewHolder holder, int position) {
        HeadingsViewHolder headingsViewHolder = (HeadingsViewHolder) holder;

        DateHeading dateHeading = (DateHeading) items.get(position);
        String dateString = dateHeading.getWholeDate();

        headingsViewHolder.heading.setText(dateString);

        setAnimation(headingsViewHolder.heading, position);
    }

    private void configureNoTypeViewholder(RecyclerView.ViewHolder holder, int position) {
        NoInfoViewHolder noInfoViewHolder = (NoInfoViewHolder) holder;
        noInfoViewHolder.noInfo.setText(context.getResources().getString(R.string.card_no_information));

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

        //TODO if (items.isEmpty()) || if - issue #29
    }

    public void addDay(TimeTableDay ttd) {
        Date date = ttd.getDate();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayCurrentDay = sp.getBoolean("displayCurrentDay", true);

        Date currentDate = new Date();
        int sixteenHrsInMillisecs = 60 * 60 * 16 * 1000;
        long secondsDiff = ((date.getTime() + sixteenHrsInMillisecs) - currentDate.getTime()) / 1000; // Difference between today and future date. If negative: date in the past. If positive: date in the future

        // Displays the current day only when the setting is active
        // OR when it's not set, but it's before 16:00
        // If the setting for displaying old days is deactivated, they will be removed here.
        if (!displayCurrentDay && (secondsDiff < 0)) {
            return;
        }

        ArrayList<TimeTableElement> timeTableElements = ttd.getElements();

        DateHeading dateHeading = new DateHeading(date);
        items.add(dateHeading);

        // TODO should be improved in the future
        if (timeTableElements.isEmpty()) {
            items.add(new TimeTableElement());
            return;
        }

        for (TimeTableElement tte : timeTableElements) {
            items.add(tte);
        }
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
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}
