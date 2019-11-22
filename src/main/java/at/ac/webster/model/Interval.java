package at.ac.webster.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Interval {
    private int interval;
    private TimeUnit timeUnit;
    private int repeatTimes;
}
