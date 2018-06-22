package com.dlmol.name.chooser;

/**
 * Created by dmolineu on 8/6/16.
 */
public class Name implements Comparable {
    private String name;
    private int acceptCount = 0;
    private int rejectCount = 0;

    public Name(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getAcceptCount() {
        return acceptCount;
    }

    public void incrementAcceptCount() {
        this.acceptCount++;
    }

    public int getRejectCount() {
        return rejectCount;
    }

    public void incrementRejectCount() {
        this.rejectCount++;
    }

    public int getTotalCount() {
        return this.acceptCount + this.rejectCount;
    }

    public float getAcceptPercent() {
        if (getTotalCount() == 0)
            return  0f;
        else if (rejectCount == 0 && getTotalCount() > 0)
            return 1.0f;
        else
            return (float) acceptCount / ((float) getTotalCount());
    }

    public float getRejectPercent() {
        if (getTotalCount() == 0)
            return  0f;
        return (float) rejectCount / ((float) getTotalCount());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Name)) {
            return -1;
        } else if (this.getAcceptPercent() > ((Name) o).getAcceptPercent())
            return 1;
        else if (((Name) o).getAcceptPercent() == this.getAcceptPercent()) {
            if (this.getRejectPercent() > ((Name) o).getRejectPercent())
                return -1;
            else if (this.getRejectCount() < ((Name) o).getRejectCount())
                return 1;
            else return 0;
        } else
            return -1;
    }
}
