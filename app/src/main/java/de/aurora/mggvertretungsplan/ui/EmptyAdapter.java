package de.aurora.mggvertretungsplan.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 01.10.2017.
 */

public class EmptyAdapter extends RecyclerView.Adapter<EmptyAdapter.ViewHolder> {

    private String mMessage;

    public EmptyAdapter(String message){
        mMessage = message;
    }

    @Override
    public EmptyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_empty, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        if(mMessage != null){
            viewHolder.mMessageView.setText(mMessage);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EmptyAdapter.ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mMessageView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mMessageView = view.findViewById(R.id.empty_item_message);
        }
    }
}
