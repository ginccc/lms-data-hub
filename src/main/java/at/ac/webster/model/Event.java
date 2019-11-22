package at.ac.webster.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Event {
    private String id;
    private Type type;
    private Date startAt;
    private Date endAt;
    private String cronSchedule;
    private Interval interval;
    private String studentGroupId;

    public enum Type {
        oneTime,
        reoccurring
    }
}
