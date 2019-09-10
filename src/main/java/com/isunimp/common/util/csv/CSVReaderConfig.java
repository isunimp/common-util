package com.isunimp.common.util.csv;

/**
 * CSVReaderConfig class
 *
 * @author renguiquan
 * @date 2019/9/5
 */
public class CSVReaderConfig {

    private int everyRow = 30;

    public int getEveryRow() {
        return everyRow;
    }

    public void setEveryRow(int everyRow) {
        this.everyRow = everyRow;
    }
}
