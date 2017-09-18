package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.List;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.datamodel.DateHeading;
import de.aurora.mggvertretungsplan.datamodel.TimeTableElement;

/**
 * Created by Rico on 26.09.2016.
 */

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DAYONE = 1;
    private static final int TYPE_DAYTWO = 2;
    private static final int TYPE_NOINFO = 3;
    private int lastPosition = -1;
    private final List<TimeTableElement> dayOneList;
    private final List<TimeTableElement> dayTwoList;
    private final List<DateHeading> headingList;
    private final Context context;
    private final SharedPreferences sp;

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
        if (viewType == TYPE_DAYONE || viewType == TYPE_DAYTWO) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_time_table_card, parent, false);

            return new ClassInfoViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_date_heading, parent, false);

            return new HeadingsViewHolder(itemView);
        } else if (viewType == TYPE_NOINFO) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_no_info_card, parent, false);

            return new NoInfoViewHolder(itemView);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ClassInfoViewHolder) {
            ClassInfoViewHolder myholder = (ClassInfoViewHolder) holder;
            TimeTableElement timeTableElement;
            int viewType = getItemViewType(position);
            if (viewType == TYPE_DAYONE) {
                timeTableElement = dayOneList.get(position - 1); //TODO Bug
            } else if (viewType == TYPE_DAYTWO) {
                int dayOneListSize = 1;
                if (dayOneList.size() > 0) {
                    dayOneListSize = dayOneList.size();
                }
                timeTableElement = dayTwoList.get(position - headingList.size() - dayOneListSize);
            } else
                throw new RuntimeException("there is no matching type!");

            myholder.hour.setText(timeTableElement.getHour());
            myholder.title.setText(timeTableElement.getTitle());
            myholder.info.setText(timeTableElement.getInfo());
            myholder.raum.setText(timeTableElement.getRoom());
            myholder.neuRaum.setText(timeTableElement.getNewRoom());

            if (sp.getBoolean("listColors", true)) {
                switch (timeTableElement.getType()) {
                    case "Entfall":
                        myholder.cardView.setCardBackgroundColor(Color.parseColor("#FF6961"));
                        break;
                    case "Vertretung":
                        myholder.cardView.setCardBackgroundColor(Color.parseColor("#779ECB"));
                        break;
                }
            } else {
                myholder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            }

            setAnimation(myholder.cardView, position);

        } else if (holder instanceof HeadingsViewHolder) {
            HeadingsViewHolder myholder = (HeadingsViewHolder) holder;
            String dateString;
            try {
                DateHeading dateHeading = headingList.get(whichHeader(position));
                dateString = dateHeading.getWholeDate();
            } catch (Exception e) {
                e.printStackTrace();
                dateString = "Fehler!";
            }

            myholder.heading.setText(dateString);

            setAnimation(myholder.heading, position);
        } else if (holder instanceof NoInfoViewHolder) {
            NoInfoViewHolder myholder = (NoInfoViewHolder) holder;
            myholder.noInfo.setText(context.getResources().getString(R.string.card_no_information));

            setAnimation(myholder.noInfo, position);
        }

    }

    public class ClassInfoViewHolder extends RecyclerView.ViewHolder {
        public final TextView title, hour, info, raum, neuRaum;
        public final CardView cardView;

        public ClassInfoViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            title = view.findViewById(R.id.info_title);
            hour = view.findViewById(R.id.info_hour);
            info = view.findViewById(R.id.info_text);
            raum = view.findViewById(R.id.info_room);
            neuRaum = view.findViewById(R.id.info_new_room);
        }
    }

    public class HeadingsViewHolder extends RecyclerView.ViewHolder {
        public final TextView heading;

        public HeadingsViewHolder(View view) {
            super(view);
            heading = view.findViewById(R.id.heading_textView);
        }
    }

    public class NoInfoViewHolder extends RecyclerView.ViewHolder {
        public final TextView noInfo;

        public NoInfoViewHolder(View view) {
            super(view);
            noInfo = view.findViewById(R.id.no_info_textview);
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


        throw new RuntimeException("No matching type! Position = " + position + ", dayOneList.size() = " +
                dayOneList.size() + ", dayTwoList.size() = " + dayTwoList.size() +
                ", headingsList.size() = " + headingList.size() + ";");
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
