package de.aurora.mggvertretungsplan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class vertretungsplanArrayAdapter extends ArrayAdapter<Vertretungen> {

    public vertretungsplanArrayAdapter(Context context, int textViewResourceId, List<Vertretungen> objects) {
        super(context, textViewResourceId, objects);
    }

    public View getView(int position, View view, ViewGroup viewGroup) {

        View updateView;
        ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            updateView = inflater.inflate(R.layout.list_view_item, null);

            viewHolder = new ViewHolder();

            viewHolder.StundeView = (TextView) updateView.findViewById(R.id.stunde);
            viewHolder.RaumView = (TextView) updateView.findViewById(R.id.raum);
            viewHolder.FachView = (TextView) updateView.findViewById(R.id.fach);
            viewHolder.NeuRaumView = (TextView) updateView.findViewById(R.id.neuraum);
            viewHolder.BemerkungView = (TextView) updateView.findViewById(R.id.bemerkung);
            viewHolder.DatumView = (TextView) updateView.findViewById(R.id.datum);
            viewHolder.TagView = (TextView) updateView.findViewById(R.id.tag);

            updateView.setTag(viewHolder);
        } else {
            updateView = view;
            viewHolder = (ViewHolder) updateView.getTag();
        }

        Vertretungen ver = (Vertretungen) getItem(position);

        viewHolder.StundeView.setText(ver.getStunde());
        viewHolder.RaumView.setText(ver.getRaum());
        viewHolder.NeuRaumView.setText(ver.getNeuRaum());
        viewHolder.FachView.setText(ver.getFach());
        viewHolder.BemerkungView.setText(ver.getBemerkung());
        viewHolder.DatumView.setText(ver.getDatum());
        viewHolder.TagView.setText(ver.getTag());

        return updateView;
    }


    private static class ViewHolder {
        public TextView StundeView;
        public TextView RaumView;
        public TextView FachView;
        public TextView NeuRaumView;
        public TextView BemerkungView;
        public TextView DatumView;
        public TextView TagView;
    }
}

