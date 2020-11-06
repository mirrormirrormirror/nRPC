package service;

import annotation.Impl;
import annotation.RpcServiceStartConfig;
import exception.NonRpcScanException;
import utils.ClassScannerUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mirror
 */
public class ServiceContextManager {
    private static Map<String, Class<?>> nRpcContext = new ConcurrentHashMap<>();

    /**
     * 扫描有 @Impl 的类，初始化 key-value  ->  接口类路径-实现类字节码
     * @param serverClass
     * @throws NonRpcScanException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void init(Class<?> serverClass) throws NonRpcScanException, IOException, ClassNotFoundException {
        RpcServiceStartConfig rpcStartConfig = serverClass.getAnnotation(RpcServiceStartConfig.class);
        String packagePath = rpcStartConfig.packagePath();
        Map<Class<? extends Annotation>, Collection<Class<?>>> scanClass = ClassScannerUtil.scan(packagePath, Impl.class);
        Collection<Class<?>> implClasses = scanClass.get(Impl.class);
        if (implClasses.isEmpty()) {
            throw new NonRpcScanException("没有配置实现类.");
        }
        for (Class<?> implClass : implClasses) {
            nRpcContext.put(implClass.getAnnotation(Impl.class).servicePath(), implClass);
        }
    }


    public static Class<?> getClass(String key) {
        return nRpcContext.get(key);
    }

}
