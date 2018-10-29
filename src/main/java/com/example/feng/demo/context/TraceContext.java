package com.example.feng.demo.context;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.feng.demo.util.MD5Util;

public class TraceContext {

    private Logger logger = LoggerFactory.getLogger(TraceContext.class);

    private String traceId;

    public String getTraceId() {
        if (traceId == null || "".equals(traceId)) {

            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                String currentIpAddress = InetAddress.getByName(hostName).getHostAddress();
                traceId = MD5Util.MD5Encode(Thread.currentThread().getName() + currentIpAddress + System.nanoTime());
            } catch (UnknownHostException e) {
                logger.error("TraceContext.getTraceId error: ", e);
            }

        }
        return traceId;
    }

    public void clear() {
        traceId = null;
    }

    // 此处用InheritableThreadLocal保证可以在子线程得到相同的traceId
    public static ThreadLocal<TraceContext> ctx = new InheritableThreadLocal<TraceContext>() {
        @Override
        protected TraceContext initialValue() {
            return new TraceContext();
        }
    };
}
