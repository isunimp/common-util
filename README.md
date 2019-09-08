# common-util

解决实际问题中实现的一些工具包，需要请自取，也欢迎提出不足或需求，我会尽量解决问题。

## 目录

### [IP白名单过滤器](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/IPWhiteFiliter.java)

IP白名单，也可以当成黑名单使用，内存动态增长，存储所有IP地址只需要512M。

### [计算器](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/Calculator.java)

简易计算器，支持 +，-，*，/，() 运算符。

### [CLH自旋锁的实现](https://github.com/isunimp/common-util/blob/master/src/main/java/com/isunimp/common/util/CLHLock.java)

CLH自旋锁，非我原创，参考[博文](https://coderbee.net/index.php/concurrent/20131115/577)
稍加完善。

### [数据缓存](https://github.com/isunimp/common-util/tree/master/src/main/java/com/isunimp/common/util/data)

将任意 key-value 数据映射提交到数据库，实现可配置定时批量提交和定量批量提交。