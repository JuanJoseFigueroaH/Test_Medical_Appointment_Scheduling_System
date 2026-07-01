package com.medisalud.domain;

public final class BusinessConstants {
    
    private BusinessConstants() {
    }
    
    public static final int SLOT_DURATION_MINUTES = 30;
    public static final int MAX_DAYS_IN_ADVANCE = 90;
    
    public static final int WEEKDAY_START_HOUR = 8;
    public static final int WEEKDAY_END_HOUR = 18;
    
    public static final int SATURDAY_START_HOUR = 8;
    public static final int SATURDAY_END_HOUR = 13;
    
    public static final int LATE_CANCELLATION_HOURS = 2;
    public static final int MAX_PENALTIES_BEFORE_BLOCK = 3;
    public static final int PENALTY_WINDOW_DAYS = 30;
    
    public static final int MARK_AS_ATTENDED_TOLERANCE_MINUTES = 30;
    public static final int MIN_ADVANCE_HOURS = 1;
}
