package de.aurora.mggvertretungsplan.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 22.09.2017.
 */
public class HeadingsViewHolder extends RecyclerView.ViewHolder {
    public final TextView heading;

    public HeadingsViewHolder(View view) {
        super(view);
        heading = view.findViewById(R.id.heading_textView);
    }
}
