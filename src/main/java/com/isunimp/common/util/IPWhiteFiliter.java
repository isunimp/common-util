package com.isunimp.common.util;

import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * com.isunimp.common.util.IPWhiteFiliter class
 *
 * @author renguiquan
 * @date 2019/8/5
 */
public final class IPWhiteFiliter {

    static class Node {
        volatile Integer val = 0;
    }

    private static final sun.misc.Unsafe unsafe;
    private static final int base;
    private static final int shift;

    private final static int ADDRESS_BITS_PER_WORD = 5;
    private final static String IP_CHECKSUM_REGEX = "\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}";
    private final Node[] table = (Node[]) Array.newInstance(Node.class, dataArrayLength());

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            base = unsafe.arrayBaseOffset(Node[].class);
            int scale = unsafe.arrayIndexScale(Node[].class);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            shift = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static int dataArrayLength() {
        return dataIndex(0xffffffffL) + 1;
    }

    private static int dataIndex(long bitIndex) {
        return Long.valueOf(bitIndex >> ADDRESS_BITS_PER_WORD).intValue();
    }

    private static Node nodeAt(Node[] tab, int i) {
        return (Node) unsafe.getObjectVolatile(tab, ((long) i << shift) + base);
    }

    private static void casNodeAt(Node[] tab, int i, Node c, Node v) {
        unsafe.compareAndSwapObject(tab, ((long) i << shift) + base, c, v);
    }

    private static long ipStrToLong(String ipString) {
        if (!ipString.matches(IP_CHECKSUM_REGEX))
            throw new IllegalArgumentException(ipString + " is not ip address");

        String[] values = ipString.split("[.]");
        long result = 0L;
        for (int idx = 0; idx < 4; ++idx)
            result = (Long.valueOf(values[idx]) << 8 * (3 - idx)) | result;

        if (result > 0xffffffffL)
            throw new IllegalArgumentException(ipString + " is not ip address");
        return result;
    }

    private void set(long bitIndex) {
        int dataIndex = dataIndex(bitIndex);
        if (nodeAt(table, dataIndex) == null)
            casNodeAt(table, dataIndex, null, new Node());
        Node node = nodeAt(table, dataIndex);
        synchronized (node) {
            node.val |= (1 << bitIndex);
        }
    }

    private void clear(long bitIndex) {
        int dataIndex = dataIndex(bitIndex);
        if (nodeAt(table, dataIndex) == null)
            return;
        Node node = nodeAt(table, dataIndex);
        synchronized (node) {
            node.val &= ~(1 << bitIndex);
        }
    }

    private boolean get(long bitIndex) {
        int dataIndex = dataIndex(bitIndex);
        if (nodeAt(table, dataIndex) == null)
            return false;
        return ((nodeAt(table, dataIndex).val & (1 << bitIndex)) != 0);
    }

    public boolean addWhiteIpAddress(String ip) {
        long bitIndex = ipStrToLong(ip);
        set(bitIndex);
        return true;
    }

    public boolean isWhiteIpAddress(String ip) {
        long bitIndex = ipStrToLong(ip);
        return get(bitIndex);
    }

    public void remove(String ip) {
        long bitIndex = ipStrToLong(ip);
        clear(bitIndex);
    }

    static public void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        IPWhiteFiliter ipWhiteList = new IPWhiteFiliter();

        System.out.println("isWhite: " + ipWhiteList.isWhiteIpAddress(ip));
        System.out.println("addWhite ");
        ipWhiteList.addWhiteIpAddress(ip);
        System.out.println("isWhite: " + ipWhiteList.isWhiteIpAddress(ip));
        System.out.println("remove");
        ipWhiteList.remove(ip);
        System.out.println("isWhite: " + ipWhiteList.isWhiteIpAddress(ip));
    }
}
