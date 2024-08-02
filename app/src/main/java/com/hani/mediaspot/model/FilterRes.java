package com.hani.mediaspot.model;

import java.util.List;

public class FilterRes {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    private List<Spot> items;

    public FilterRes(List<Spot> items) {
        this.items = items;
    }

    public List<Spot> getItems() {
        return items;
    }

    public void setItems(List<Spot> items) {
        this.items = items;
    }
}