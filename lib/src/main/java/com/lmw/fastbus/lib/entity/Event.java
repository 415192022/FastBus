package com.lmw.fastbus.lib.entity;

public class Event {

    private String tag;
    private Object data;

    public Event(String tag, Object data) {
        this.tag = tag;
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
