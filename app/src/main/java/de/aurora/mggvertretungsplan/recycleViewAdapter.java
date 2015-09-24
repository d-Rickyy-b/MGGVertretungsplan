package de.aurora.mggvertretungsplan;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class recycleViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{

	private List<Vertretungen> mData = Collections.emptyList();
	private LayoutInflater inflater;
	
	public recycleViewAdapter(Context context, List<Vertretungen> data) {
		inflater=LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public void onBindViewHolder(RecyclerViewHolder viewHolder, int position) {
        viewHolder.StundeView.setText(mData.get(position).toString());
        System.out.println("---------------- " + mData.get(position).toString() + " -------------");
    }

	@Override
	public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.list_view_item, viewGroup, false);
        return new RecyclerViewHolder(itemView);
	}
	
	public void addItem(int position, Object data) {
	    //mData.add(position, data);
	    notifyItemInserted(position);
	}
	
	public void removeItem(int position) {
	    mData.remove(position);
	    notifyItemRemoved(position);
	}

	
	
	


}
