# common-util

解决实际问题中实现的一些工具包，每一个包都有`main`方法，有完整的使用示例，需要请自取，也欢迎提出不足或需求，我会尽量解决问题。

## 目录

### [IP白名单过滤器](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/IPWhiteFiliter.java)

线程安全的IP白名单，也可以当成黑名单使用，内存动态增长，存储所有IPv4地址只需要512M。

### [计算器](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/Calculator.java)

简易计算器，支持小数，支持 +，-，*，/，() 运算符。

### [CLH自旋锁的实现](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/CLHLock.java)

CLH自旋锁，非我原创，参考[博文](https://coderbee.net/index.php/concurrent/20131115/577)
稍加完善。

### [CSVReader](https://github.com/isunimp/common-util/tree/master/src/main/java/com/isunimp/common/util/csv)

解决POI读取大文件OOM的问题，将数据映射成 key-value 。
