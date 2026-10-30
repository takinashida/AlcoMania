package net.takinashida.moonshine.capability;

import java.util.Objects;

public class DrunkennessInstance {
    private int value;

    public DrunkennessInstance() {
        this.value = 0;
    }

    public DrunkennessInstance(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        // Cap the value between 0 and 100 (until we decide how death is handled exactly, but we'll cap for the progress bar logic)
        this.value = Math.max(0, Math.min(100, value));
    }
    
    // Helper for death logic where value might exceed 100
    public void setValueUncapped(int value) {
        this.value = value;
    }
}
