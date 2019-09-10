package com.isunimp.common.util.data;

import com.zaxxer.hikari.HikariConfig;

/**
 * DataManagerConfig class
 *
 * @author renguiquan
 * @date 2019/9/6
 */
public class DataManagerConfig extends HikariConfig {

    // 定量提交条数
    private int commitCountThresholds = 3000;
    // 定时提交时间，MILLISECONDS
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
