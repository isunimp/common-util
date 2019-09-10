package com.isunimp.common.util.csv;

import java.io.*;

/**
 * CSVReader class
 *
 * @author renguiquan
 * @date 2019/9/5
 */
public class CSVReader {

    public interface ICSVReaderListener {
        void rows(String[][] data);
    }

    final private BufferedReader bufferedReader;
    final private ICSVReaderListener listener;

    CSVReaderConfig config;

    public CSVReader(File file, ICSVReaderListener listener, CSVReaderConfig config) throws FileNotFoundException {
        this(new FileReader(file), listener, config);
    }

    public CSVReader(String path, ICSVReaderListener listener, CSVReaderConfig config) throws FileNotFoundException {
        this(new FileReader(path), listener, config);
    }

    public CSVReader(InputStream in, ICSVReaderListener listener, CSVReaderConfig config) {
        this(new InputStreamReader(in), listener, config);
    }

    public CSVReader(InputStreamReader fileReader, ICSVReaderListener listener, CSVReaderConfig config) {
        this.bufferedReader = new BufferedReader(fileReader);
        this.listener = listener;
        if (config == null)
            this.config = new CSVReaderConfig();
        else this.config = config;
    }

    public String[][] readLine(int num) throws IOException {
        String[][] rows = new String[num][];
        for (int idx = 0; idx < num; ++idx) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            String[] colItem = line.split(",");
            rows[idx] = colItem;
        }
        return rows;
    }

    public void readUntilEnd() throws IOException {
        for (; ; ) {
            String[][] rows = readLine(config.getEveryRow());
            this.listener.rows(rows);
            if (rows[rows.length - 1] == null) return;
        }
    }

    public CSVReaderConfig getConfig() {
        return config;
    }
}
