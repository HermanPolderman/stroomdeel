package hello;

public class TimeFrameEnergy {

    public Double energy;
    public String measuredBy;
    public String unit;
    public LifetimeEnergy startLifetimeEnergy;
    public LifetimeEnergy endLifetimeEnergy;

    @Override
    public String toString() {
        return "TimeFrameEnergy{" +
                "energy=" + energy +
                ", measuredBy='" + measuredBy + '\'' +
                ", unit='" + unit + '\'' +
                ", startLifetimeEnergy=" + startLifetimeEnergy +
                ", endLifetimeEnergy=" + endLifetimeEnergy +
                '}';
    }
}
