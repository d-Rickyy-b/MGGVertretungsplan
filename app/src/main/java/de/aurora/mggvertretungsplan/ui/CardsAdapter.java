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

import java.util.List;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.datamodel.DateHeading;
import de.aurora.mggvertretungsplan.datamodel.TimeTableElement;
import de.aurora.mggvertretungsplan.viewholder.ClassInfoViewHolder;
import de.aurora.mggvertretungsplan.viewholder.HeadingsViewHolder;
import de.aurora.mggvertretungsplan.viewholder.NoInfoViewHolder;

/**
 * Created by Rico on 26.09.2016.
 */

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOINFO = 1;
    private static final int TYPE_CANCELLATION = 2;
    private static final int TYPE_DAYONE = 3;
    private static final int TYPE_DAYTWO = 4;
    private final List<TimeTableElement> dayOneList;
    private final List<TimeTableElement> dayTwoList;
    private final List<DateHeading> headingList;
    private final Context context;
    private final SharedPreferences sp;
    private int lastPosition = -1;

    public CardsAdapter(List<TimeTableElement> dayOneList, List<TimeTableElement> dayTwoList, List<DateHeading> headingList, Context context) {
        this.dayOneList = dayOneList;
        this.dayTwoList = dayTwoList;
        this.headingList = headingList;
        this.context = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //Wenn RecyclerView erstellt wird, werden Layouts inflated und gecached.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DAYONE:
            case TYPE_DAYTWO:
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_time_table_card, parent, false);

                return new ClassInfoViewHolder(itemView);

            case TYPE_HEADER:
                View itemView1 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_date_heading, parent, false);

                return new HeadingsViewHolder(itemView1);

            default:
            case TYPE_NOINFO:
                View itemView2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_no_info_card, parent, false);

                return new NoInfoViewHolder(itemView2);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_CANCELLATION:
                ClassInfoViewHolder classInfoViewHolder = (ClassInfoViewHolder) holder;
                TimeTableElement timeTableElement;
                int viewType = getItemViewType(position);
                //int viewType = holder.getItemViewType();

                switch (viewType) {
                    case TYPE_DAYONE:
                        timeTableElement = dayOneList.get(position - 1); //TODO Bug
                        break;
                    case TYPE_DAYTWO:
                        int dayOneListSize = 1;
                        if (dayOneList.size() > 0) {
                            dayOneListSize = dayOneList.size();
                        }
                        timeTableElement = dayTwoList.get(position - headingList.size() - dayOneListSize);
                        break;
                    default:
                        throw new RuntimeException("There is no matching type!");
                }

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
                break;

            case TYPE_HEADER:
                HeadingsViewHolder headingsViewHolder = (HeadingsViewHolder) holder;
                String dateString;
                try {
                    DateHeading dateHeading = headingList.get(whichHeader(position));
                    dateString = dateHeading.getWholeDate();
                } catch (Exception e) {
                    e.printStackTrace();
                    dateString = "Error!";
                }

                headingsViewHolder.heading.setText(dateString);

                setAnimation(headingsViewHolder.heading, position);
                break;

            case TYPE_NOINFO:
                NoInfoViewHolder noInfoViewHolder = (NoInfoViewHolder) holder;
                noInfoViewHolder.noInfo.setText(context.getResources().getString(R.string.card_no_information));

                setAnimation(noInfoViewHolder.noInfo, position);
                break;
        }

    }

    @Override
    public int getItemCount() {
        int dayOneListSize = 1;
        int dayTwoListSize = 1;

        if (dayOneList.size() > 0) {
            dayOneListSize = dayOneList.size();
        }

        if (dayTwoList.size() > 0) {
            dayTwoListSize = dayTwoList.size();
        }
        return dayOneListSize + dayTwoListSize + headingList.size();
    }

    private boolean isPositionHeader(int position) {
        if (dayOneList.size() > 0) {
            return position == 0 || position == dayOneList.size() + 1;
        } else {
            return position == 0 || position == 2;
        }
    }

    private int whichHeader(int position) {
        if (position == 0)
            return 0;
        return 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (isPositionHeader(position))
            return TYPE_HEADER;
        else if (dayOneList.size() == 0 && position == 1)
            return TYPE_NOINFO;
        else if (position <= dayOneList.size())
            return TYPE_DAYONE;
        else if ((dayTwoList.size() == 0 && position >= (dayOneList.size() + headingList.size())))
            return TYPE_NOINFO;
        else if (position >= (dayOneList.size() + headingList.size()))
            return TYPE_DAYTWO;


        throw new RuntimeException(String.format("No matching type! Position = %s, dayOneList.size() = %s, " +
                "dayTwoList.size() = %s, headingsList.size() = %s", position, dayOneList.size(), dayTwoList.size(), headingList.size()));
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
