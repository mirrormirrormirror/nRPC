package utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * 说明：注解扫描工具类(基于Spring框架实现)
 *
 * @author mirror
 */
public class ClassScannerUtil {

    private static final String ALL_FILES = "**/*";
    private static final String ALL_CLASS_FILES = ALL_FILES + ".class";
    private static final String WILDCARD = "*";

    public static Map<Class<? extends Annotation>, Collection<Class<?>>> scan(String basePackage,
                                                                              Class<? extends Annotation>... annotations) throws IOException, ClassNotFoundException {
        return findClasses(parsePackages(basePackage), Collections.unmodifiableList(Arrays.asList(annotations)), null);
    }

    private static Set<String> parsePackages(final String packagesAsCsv) {
        final String[] values = packagesAsCsv.split(",");
        final Set<String> basePackages = new HashSet<String>(values.length);
        for (final String value : values) {
            final String trimmed = value.trim();
            if (trimmed.equals(WILDCARD)) {
                basePackages.clear();
                basePackages.add(trimmed);
                break;
            } else if (trimmed.length() > 0) {
                basePackages.add(trimmed);
            }
        }
        return basePackages;
    }

    private static Map<Class<? extends Annotation>, Collection<Class<?>>> findClasses(Collection<String> basePackages,
                                                                                      List<Class<? extends Annotation>> annotations, ClassLoader loader)
            throws IOException, ClassNotFoundException {

        ResourcePatternResolver resolver = getResolver(loader);
        MetadataReaderFactory factory = new CachingMetadataReaderFactory(resolver);

        final Map<Class<? extends Annotation>, Collection<Class<?>>> classes = new HashMap<Class<? extends Annotation>, Collection<Class<?>>>();
        final Map<Class<? extends Annotation>, Collection<String>> matchingInterfaces = new HashMap<Class<? extends Annotation>, Collection<String>>();
        final Map<String, String[]> nonMatchingClasses = new HashMap<String, String[]>();

        for (Class<? extends Annotation> annotation : annotations) {
            classes.put(annotation, new HashSet<Class<?>>());
            matchingInterfaces.put(annotation, new HashSet<String>());
        }

        if (basePackages == null || basePackages.isEmpty()) {
            return classes;
        }

        for (final String basePackage : basePackages) {
            final boolean scanAllPackages = basePackage.equals(WILDCARD);
            final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + (scanAllPackages ? "" : ClassUtils.convertClassNameToResourcePath(basePackage)) + ALL_CLASS_FILES;

            final Resource[] resources = resolver.getResources(packageSearchPath);

            for (final Resource resource : resources) {
                final MetadataReader reader = factory.getMetadataReader(resource);
                final AnnotationMetadata metadata = reader.getAnnotationMetadata();

                for (Class<? extends Annotation> annotation : annotations) {
                    boolean concreteClass = !metadata.isInterface() && !metadata.isAbstract();
                    if (metadata.isAnnotated(annotation.getName())) {
                        if (concreteClass) {
                            classes.get(annotation).add(loadClass(metadata.getClassName(), loader));
                        } else {
                            matchingInterfaces.get(annotation).add(metadata.getClassName());
                        }
                    } else if (concreteClass && metadata.getInterfaceNames().length > 0) {
                        nonMatchingClasses.put(metadata.getClassName(), metadata.getInterfaceNames());
                    }
                }
            }
        }
        if (!nonMatchingClasses.isEmpty()) {
            for (Map.Entry<Class<? extends Annotation>, Collection<String>> e1 : matchingInterfaces.entrySet()) {
                for (Map.Entry<String, String[]> e2 : nonMatchingClasses.entrySet()) {
                    for (String intName : e2.getValue()) {
                        if (e1.getValue().contains(intName)) {
                            classes.get(e1.getKey()).add(loadClass(e2.getKey(), loader));
                            break;
                        }
                    }
                }
            }
        }
        for (Map.Entry<Class<? extends Annotation>, Collection<String>> e : matchingInterfaces.entrySet()) {
            if (classes.get(e.getKey()).isEmpty()) {
                for (String intName : e.getValue()) {
                    classes.get(e.getKey()).add(loadClass(intName, loader));
                }
            }
        }

        return classes;
    }

    private static ResourcePatternResolver getResolver(ClassLoader loader) {
        return loader != null ? new PathMatchingResourcePatternResolver(loader)
                : new PathMatchingResourcePatternResolver();
    }

    private static Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        if (loader == null) {
            try {
                ClassLoader cl = getContextClassLoader();
                if (cl != null) {
                    return cl.loadClass(className);
                }
            } catch (ClassNotFoundException e) {
            }
            return loadClass2(className, ClassScannerUtil.class);
        } else {
            return loader.loadClass(className);
        }
    }

    private static ClassLoader getContextClassLoader() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    return loader != null ? loader : ClassLoader.getSystemClassLoader();
                }
            });
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null ? loader : ClassLoader.getSystemClassLoader();
    }

    private static Class<?> loadClass2(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            final ClassLoader loader = getClassLoader(callingClass);
            if (loader != null) {
                return loader.loadClass(className);
            } else {
                throw ex;
            }
        }
    }

    private static ClassLoader getClassLoader(final Class<?> clazz) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return clazz.getClassLoader();
                }
            });
        }
        return clazz.getClassLoader();
    }
}