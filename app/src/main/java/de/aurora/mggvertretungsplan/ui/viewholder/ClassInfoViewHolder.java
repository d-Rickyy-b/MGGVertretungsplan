package de.aurora.mggvertretungsplan.ui.viewholder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 22.09.2017.
 */
public class ClassInfoViewHolder extends RecyclerView.ViewHolder {
    public final TextView title, hour, info, room, newRoom;
    public final CardView cardView;

    public ClassInfoViewHolder(View view) {
        super(view);
        cardView = view.findViewById(R.id.card_view);
        title = view.findViewById(R.id.info_title);
        hour = view.findViewById(R.id.info_hour);
        info = view.findViewById(R.id.info_text);
        room = view.findViewById(R.id.info_room);
        newRoom = view.findViewById(R.id.info_new_room);
    }
}
