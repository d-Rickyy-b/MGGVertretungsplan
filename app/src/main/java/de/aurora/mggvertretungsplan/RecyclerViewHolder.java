package de.aurora.mggvertretungsplan;

import android.support.v7.widget.RecyclerView;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

//    public TextView title;
//    public ImageView icon;
    
    public TextView StundeView ;
	public TextView RaumView ;
	public TextView FachView ;
	public TextView NeuRaumView ;
	public TextView BemerkungView ;
	public TextView DatumView;
	public TextView TagView;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
//        title = (TextView) itemView.findViewById(R.id.title);
//        icon = (ImageView) itemView.findViewById(R.id.icon);
        StundeView = (TextView) itemView.findViewById(R.id.stunde);
        RaumView = (TextView) itemView.findViewById(R.id.raum);
        FachView = (TextView) itemView.findViewById(R.id.fach);
        NeuRaumView = (TextView) itemView.findViewById(R.id.neuraum);
        BemerkungView = (TextView) itemView.findViewById(R.id.bemerkung);
        DatumView = (TextView) itemView.findViewById(R.id.datum);
        TagView = (TextView) itemView.findViewById(R.id.tag);
    }
}
