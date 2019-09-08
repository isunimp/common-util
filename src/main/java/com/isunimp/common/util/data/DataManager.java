package com.isunimp.common.util.data;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * com.isunimp.common.util.datamanager.DataManager class
 *
 * @author isunimp
 * @date 2019/9/6
 */
public class DataManager {

    private static final Logger LOG = LoggerFactory.getLogger(DataManager.class);

    static private final String INSERT_INTO_TEMPLATE = "INSERT INTO TABLE_NAME (FIELDS) VALUES (VALUES_DATA)";
    static private final String TABLE_NAME_KEYWORD = "TABLE_NAME";
    static private final String FIELDS_KEYWORD = "FIELDS";
    static private final String VALUES_KEYWORD = "VALUES_DATA";

    public static class DataNode {
        String table;
        Map<String, Object> values;

        DataNode(String table, Map<String, Object> values) {
            this.table = table;
            this.values = values;
        }
    }

    private final HikariDataSource dataSource;
    private final DataManagerConfig config;

    private final ExecutorService executorService;
    private final ScheduledThreadPoolExecutor scheduledExecutor;
    private ScheduledFuture scheduledFuture;
    private final LinkedBlockingQueue queue;
    private volatile boolean shutdowned = false;

    public DataManager(DataManagerConfig config) {
        this.dataSource = new HikariDataSource(config);
        this.config = config;
        this.executorService = new ThreadPoolExecutor(config.getMaximumPoolSize(), config.getMaximumPoolSize(),
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "commit-pool" + "-thread-" + threadNumber.getAndIncrement());
                t.setDaemon(false);
                return t;
            }
        });
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);
        this.queue = new LinkedBlockingQueue();
        scheduled();
    }

    private void scheduled() {
        if (scheduledFuture != null)
            scheduledFuture.cancel(false);

        this.scheduledFuture = scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                if (commit() == 0)
                    scheduled();
            }
        }, config.getCommitTimeThresholds(), TimeUnit.MILLISECONDS);
    }

    public void add(String table, List<Map> data) {
        if (shutdowned)
            throw new RuntimeException("the manager has been shut down");
        synchronized (queue) {
            data.stream()
                    .map(item -> new DataNode(table, item))
                    .forEach(queue::add);
        }
        if (queue.size() >= config.getCommitCountThresholds())
            commit();
    }

    private int commit() {
        List<DataNode> dataNodes = null;
        synchronized (queue) {
            dataNodes = new ArrayList<>(queue);
            queue.clear();
        }

        if (dataNodes.size() == 0)
            return 0;

        for (int batch = 0; ; batch++) {
            int from = batch * config.getCommitCountThresholds();
            int to = (batch + 1) * config.getCommitCountThresholds();
            if (to > dataNodes.size())
                to = dataNodes.size();
            executorService.execute(new BatchRunnable(dataNodes.subList(from, to)));
            if (to == dataNodes.size())
                break;
        }
        return dataNodes.size();
    }

    class BatchRunnable implements Runnable {
        private final List<DataNode> dataNodes;

        public BatchRunnable(List<DataNode> dataNodes) {
            this.dataNodes = dataNodes;
        }

        @Override
        public void run() {
            try {
                commitImpl(dataNodes);
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private int commitImpl(List<DataNode> dataNodes) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            for (DataNode dataNode : dataNodes) {
                ArrayList<String> fieldList = new ArrayList<>(dataNode.values.keySet());
                PreparedStatement preparedStatement = connection.prepareStatement(processSql(dataNode));
                for (Map.Entry<String, Object> entry : dataNode.values.entrySet()) {
                    Integer index = fieldList.indexOf(entry.getKey()) + 1;
                    Object value = entry.getValue();

                    if (value instanceof String) {
                        preparedStatement.setString(index, (String) value);
                    } else if (value instanceof Number) {
                        preparedStatement.setLong(index, Long.valueOf(value.toString()));
                    } else if (value instanceof Date) {
                        preparedStatement.setTimestamp(index, new java.sql.Timestamp(((Date) value).getTime()));
                    } else {
                        throw new RuntimeException("this data type is not supported");
                    }
                }
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            connection.rollback();
        } finally {
            connection.commit();
            connection.close();
            scheduled();
        }
        return dataNodes.size();
    }

    private String processSql(DataNode dataNodes) {
        String[] valuePlaceholder = new String[dataNodes.values.size()];
        Arrays.fill(valuePlaceholder, "?");
        String table = dataNodes.table;
        String fields = String.join(",", dataNodes.values.keySet());
        String values = String.join(",", valuePlaceholder);
        return INSERT_INTO_TEMPLATE.replace(TABLE_NAME_KEYWORD, table)
                .replace(FIELDS_KEYWORD, fields)
                .replace(VALUES_KEYWORD, values);
    }

    public void shutdown() {
        if (!shutdowned)
            shutdowned = true;
        this.scheduledExecutor.shutdown();
        this.executorService.shutdown();
    }

    public List<Runnable> shutdownNow() {
        if (!shutdowned)
            shutdowned = true;
        List<Runnable> tasks = this.executorService.shutdownNow();
        this.scheduledExecutor.shutdownNow();
        this.dataSource.close();
        return tasks;
    }

    public static void main(String[] args) throws InterruptedException, SQLException {
        DataManagerConfig config = new DataManagerConfig();
        config.setJdbcUrl("jdbc:mysql://192.168.124.131:3306/test");
        config.setUsername("root");
        config.setPassword("root");
        config.setCommitCountThresholds(100);
        DataManager dataManager = new DataManager(config);

        short id = 0;
        long strat = System.currentTimeMillis();
        int sum = 0;
        for (; ; ) {
            List list = new ArrayList();
            for (short i = 0; i < 10000; ++i)
                list.add(createData(id++));
            sum += 10000;
            dataManager.add("Persons", list);

            Thread.sleep(30);

            if (System.currentTimeMillis() - strat >= 1000 * 60)
                break;
        }

        List task = dataManager.shutdownNow();
        System.out.println(sum);
        System.out.println(task.size());
    }

    public static Map createData(int id) {
        Map map = new HashMap();
        map.put("id", id);
        return map;
    }

}
