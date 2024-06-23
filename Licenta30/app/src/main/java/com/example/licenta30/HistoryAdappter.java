package com.example.licenta30;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class HistoryAdappter extends FirebaseRecyclerAdapter<OpeningModel, HistoryAdappter.myViewHolder> {
    public HistoryAdappter(@NonNull FirebaseRecyclerOptions<OpeningModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int i, @NonNull OpeningModel openingModel) {
        holder.door.setText(openingModel.getDoor());
        holder.time_stamp.setText(openingModel.getTime_stamp());
        holder.user.setText(openingModel.getUser());

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        TextView door;
        TextView time_stamp;
        TextView user;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            door = (TextView)itemView.findViewById(R.id.door_name);
            time_stamp = (TextView)itemView.findViewById(R.id.time_stamp);
            user = (TextView)itemView.findViewById(R.id.user);
        }
    }
}
