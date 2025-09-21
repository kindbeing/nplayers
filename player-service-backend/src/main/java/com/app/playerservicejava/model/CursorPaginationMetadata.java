package com.app.playerservicejava.model;

public class CursorPaginationMetadata {
    private String nextCursor;
    private String previousCursor;
    private int limit;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements; // Optional: for display purposes

    public CursorPaginationMetadata() {}

    public CursorPaginationMetadata(String nextCursor, String previousCursor, int limit,
                                   boolean hasNext, boolean hasPrevious, long totalElements) {
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.limit = limit;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.totalElements = totalElements;
    }

    // Getters and setters
    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public String getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(String previousCursor) {
        this.previousCursor = previousCursor;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
