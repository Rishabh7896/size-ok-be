package com.sizeok.sizechart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "size_options")
public class SizeOption {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "size_chart_id", nullable = false, length = 64)
    private String sizeChartId;

    @Column(name = "size_label", nullable = false, length = 32)
    private String sizeLabel;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public SizeOption() {
    }

    public SizeOption(String id, String sizeChartId, String sizeLabel, int sortOrder) {
        this.id = id;
        this.sizeChartId = sizeChartId;
        this.sizeLabel = sizeLabel;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public String getSizeChartId() {
        return sizeChartId;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
