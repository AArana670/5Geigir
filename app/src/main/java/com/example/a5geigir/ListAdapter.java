package com.example.a5geigir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5geigir.activities.MeasurementActivity;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.util.Collections;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

    private final List<Measurement> measurementList;
    private final Context context;

    public ListAdapter(List<Measurement> measurementList, Context context) {
        this.measurementList = measurementList;
        Collections.reverse(this.measurementList);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String moment = measurementList.get(position).moment;
        holder.signalDate.setText(moment.split(" ")[0]);
        holder.signalTime.setText(moment.split(" ")[1]);
        holder.signalDBm.setText(measurementList.get(position).meanDBm+" dBm");
        holder.signalBar.setProgress(measurementList.get(position).meanDBm);
        setProgressColor(holder.signalBar);

        holder.signalPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, MeasurementActivity.class);
                i.putExtra("moment",measurementList.get(holder.getAdapterPosition()).moment+"");
                i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        });
    }

    private void setProgressColor(ProgressBar bar){
        if (bar.getProgress() < -50) {
            bar.getProgressDrawable().setColorFilter(  //https://stackoverflow.com/questions/2020882/how-to-change-progress-bars-progress-color-in-android
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        }else if (bar.getProgress() < -30){
            bar.getProgressDrawable().setColorFilter(
                    Color.rgb(255,88,53), android.graphics.PorterDuff.Mode.SRC_IN);
        }else{
            bar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return measurementList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView signalDate;
        private final TextView signalTime;
        private final TextView signalDBm;
        private final ProgressBar signalBar;
        private final View signalPanel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            signalDate = (TextView) itemView.findViewById(R.id.signal_date);
            signalTime = (TextView) itemView.findViewById(R.id.signal_time);
            signalDBm = (TextView) itemView.findViewById(R.id.signal_dBm);
            signalBar = (ProgressBar) itemView.findViewById(R.id.signal_bar);
            signalPanel = itemView.findViewById(R.id.signal_panel);
        }
    }

}
