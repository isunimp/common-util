package com.isunimp.common.util.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CSVUtil class
 *
 * @author renguiquan
 * @date 2019/9/5
 */
public class CSVUtil implements CSVReader.ICSVReaderListener {

    public interface CSVUtilListener {
        void callback(Collection<Map> data);
    }

    final private CSVUtilListener listener;
    final private CSVReader reader;

    private List<String> header;

    public CSVUtil(File file, CSVUtilListener listener) throws FileNotFoundException {
        this.reader = new CSVReader(file, this, null);
        this.listener = listener;
    }

    public CSVUtil(String path, CSVUtilListener listener) throws FileNotFoundException {
        this.reader = new CSVReader(path, this, null);
        this.listener = listener;
    }

    public void start() throws IOException {
        String[][] header = reader.readLine(1);
        if (header.length == 0) return;
        this.header = Arrays.asList(header[0]);
        reader.readUntilEnd();
    }

    @Override
    public void rows(String[][] data) {
        List<Map> combina = new ArrayList<>(reader.getConfig().getEveryRow());
        for (String[] datum : data) {
            if (datum == null) break;
            Map<String, String> combinaRow = new HashMap<>(datum.length);
            for (int idx = 0; idx < datum.length; idx++) {
                combinaRow.put(this.header.get(idx), datum[idx]);
            }
            combina.add(combinaRow);
        }
        this.listener.callback(combina);
    }

    public static void main(String[] args) throws IOException {
        AtomicInteger num = new AtomicInteger();
        System.out.println(System.currentTimeMillis());
        CSVUtil util = new CSVUtil("C:\\Users\\Administrator\\Desktop\\xxx.csv", new CSVUtilListener() {
            @Override
            public void callback(Collection<Map> data) {
                num.addAndGet(data.size());
                data.forEach(item->{
                    System.out.println(item.get("企业名称"));
                });
            }
        });
        util.start();
        System.out.println(System.currentTimeMillis());
        System.out.println(num.get());
    }
}
