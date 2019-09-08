package com.isunimp.common.util.data;

import com.zaxxer.hikari.HikariConfig;

/**
 * DataManagerConfig class
 *
 * @author isunimp
 * @date 2019/9/6
 */
public class DataManagerConfig extends HikariConfig {

    private int commitCountThresholds = 3000;
    private long commitTimeThresholds = 1000 * 60;

    public int getCommitCountThresholds() {
        return commitCountThresholds;
    }

    public void setCommitCountThresholds(int commitCountThresholds) {
        this.commitCountThresholds = commitCountThresholds;
    }

    public long getCommitTimeThresholds() {
        return commitTimeThresholds;
    }

    public void setCommitTimeThresholds(long commitTimeThresholds) {
        this.commitTimeThresholds = commitTimeThresholds;
    }
}
