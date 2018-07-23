package io.evall.greenbike;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UserdataAdapter extends RecyclerView.Adapter<UserdataAdapter.MyViewHolder> {

    private List<Userdata> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView rank, username, dist, cycletime, speed, energy, tree, gas;
        public ConstraintLayout row;

        public MyViewHolder(View view) {
            super(view);
            row = (ConstraintLayout) view.findViewById(R.id.txtr_back);
            rank = (TextView) view.findViewById(R.id.txtr_rank);
            username = (TextView) view.findViewById(R.id.txtr_user);
            dist = (TextView) view.findViewById(R.id.txtr_dist);
            cycletime = (TextView) view.findViewById(R.id.txtr_time);
            speed = (TextView) view.findViewById(R.id.txtr_speed);
            energy = (TextView) view.findViewById(R.id.txtr_energy);
            tree = (TextView) view.findViewById(R.id.txtr_tree);
            gas = (TextView) view.findViewById(R.id.txtr_co2);

        }
    }


    public UserdataAdapter(List<Userdata> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rcard_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Userdata data = dataList.get(position);
        if(data.getOwner() == 1) {
            holder.row.setBackgroundColor(Color.argb(80, 255, 139, 103));
        }
        holder.rank.setText(data.getRank());
        holder.username.setText(data.getUsername());
        holder.dist.setText(data.getDist());
        holder.cycletime.setText(data.getCycletime());
        holder.speed.setText(data.getSpeed());
        holder.energy.setText(data.getEnergy());
        holder.tree.setText(data.getTree());
        holder.gas.setText(data.getGas());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}