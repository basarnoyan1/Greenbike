package io.evall.greenbike;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UserdataAdapter extends RecyclerView.Adapter<UserdataAdapter.MyViewHolder> {

    private List<Userdata> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username, cycledate, dist, cycletime, speed, energy, cycle, tree, gas;

        public MyViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.txtr_user);
            cycledate = (TextView) view.findViewById(R.id.txtr_date);
            dist = (TextView) view.findViewById(R.id.txtr_time);
            cycletime = (TextView) view.findViewById(R.id.txtr_dist);
            speed = (TextView) view.findViewById(R.id.txtr_co2);
            energy = (TextView) view.findViewById(R.id.txtr_speed);
            cycle = (TextView) view.findViewById(R.id.txtr_tree);
            tree = (TextView) view.findViewById(R.id.txtr_rev);
            gas = (TextView) view.findViewById(R.id.txtr_energy);
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
        holder.username.setText(data.getUsername());
        holder.cycledate.setText(data.getCycledate());
        holder.dist.setText(data.getDist());
        holder.cycletime.setText(data.getCycletime());
        holder.speed.setText(data.getSpeed());
        holder.energy.setText(data.getEnergy());
        holder.cycle.setText(data.getCycle());
        holder.tree.setText(data.getTree());
        holder.gas.setText(data.getGas());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}