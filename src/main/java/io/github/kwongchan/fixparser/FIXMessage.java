package io.github.kwongchan.fixparser;

public interface FIXMessage {
    boolean hasTag(int tag);

    CharSequence getString(int tag);

    int getInt(int tag);

    long getLong(int tag);

    double getDouble(int tag);

    boolean getBoolean(int tag);

    long getDateTimeEpochMillis(int tag);
}
