package de.aurora.mggvertretungsplan.ui.viewholder;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 22.09.2017.
 */
public class NoInfoViewHolder extends RecyclerView.ViewHolder {
    public final TextView noInfo;
    public final CardView cardView;

    public NoInfoViewHolder(View view) {
        super(view);
        cardView = view.findViewById(R.id.card_view);
        noInfo = view.findViewById(R.id.no_info_textview);
    }
}
