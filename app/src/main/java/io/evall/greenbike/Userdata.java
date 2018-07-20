package io.evall.greenbike;

public class Userdata {
    private String username, dist, cycletime, speed, energy, cycle, tree, gas;

    public Userdata() {
    }

    public Userdata(String username, String dist, String cycletime,
                    String speed, String energy, String cycle, String tree, String gas) {
        this.username = username;
        this.dist = dist;
        this.cycletime = cycletime;
        this.speed = speed;
        this.energy = energy;
        this.cycle = cycle;
        this.tree = tree;
        this.gas = gas;
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

    public String getCycle() {
        return cycle;
    }
    public void setCycle(String cycle) {
        this.cycle = cycle;
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
