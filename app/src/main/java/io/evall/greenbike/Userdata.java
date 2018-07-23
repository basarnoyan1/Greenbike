package io.evall.greenbike;

public class Userdata {
    private String rank, username, dist, cycletime, speed, energy, tree, gas;

    public Userdata() {
    }

    public Userdata(String rank, String username, String dist, String cycletime,
                    String speed, String energy, String tree, String gas) {
        this.rank = rank;
        this.username = username;
        this.dist = dist;
        this.cycletime = cycletime;
        this.speed = speed;
        this.energy = energy;
        this.tree = tree;
        this.gas = gas;
    }

    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getDist() {
        return dist;
    }
    public void setDist(String dist) {
        this.dist = dist;
    }

    public String getCycletime() {
        return cycletime;
    }
    public void setCycletime(String cycletime) {
        this.cycletime = cycletime;
    }

    public String getSpeed() {
        return speed;
    }
    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getEnergy() {
        return energy;
    }
    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getTree() {
        return tree;
    }
    public void setTree(String tree) {
        this.tree = tree;
    }

    public String getGas() {
        return gas;
    }
    public void setGas(String gas) {
        this.gas = gas;
    }
}
