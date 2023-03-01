package io.github.casuallc.admq.v2.annotation;

import io.github.casuallc.admq.v2.core.MQFactory;
import io.github.casuallc.admq.v2.listener.adapter.MessageListenerAdapter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * 解析Listener注解，注册消费者信息.
 */
@Slf4j
@Component
public class MQListenerBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    private BeanFactory beanFactory;

    @Autowired
    private MQFactory factory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
//        if (beanFactory instanceof ConfigurableListableBeanFactory) {
//            this.resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
//            this.expressionContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory,
//                    this.listenerScope);
//        }
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            // 类注解
            final List<Method> multiMethods = new ArrayList<>();
            Collection<MQListener> classLevelListeners = findListenerAnnotations(targetClass);
            final boolean hasClassLevelListeners = classLevelListeners.size() > 0;
            if (hasClassLevelListeners) {
                Set<Method> methodsWithHandler = MethodIntrospector.selectMethods(targetClass,
                        (ReflectionUtils.MethodFilter) method ->
                                AnnotationUtils.findAnnotation(method, MQHandler.class) != null);
                multiMethods.addAll(methodsWithHandler);
            }

            // 方法注解
            Map<Method, Set<MQListener>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<Set<MQListener>>) method -> {
                        Set<MQListener> listenerMethods = findListenerAnnotations(method);
                        return (!listenerMethods.isEmpty() ? listenerMethods : null);
                    });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(bean.getClass());
                if (log.isTraceEnabled()) {
                    log.trace("No @MQListener annotations found on bean type: " + bean.getClass());
                }
            } else {
                // Non-empty set of methods
                for (Map.Entry<Method, Set<MQListener>> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    for (MQListener listener : entry.getValue()) {
                        processListener(listener, method, bean, beanName);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug(annotatedMethods.size() + " @MQListener methods processed on bean '"
                            + beanName + "': " + annotatedMethods);
                }
            }
            if (hasClassLevelListeners) {
                // TODO
//                processMultiMethodListeners(classLevelListeners, multiMethods, bean, beanName);
            }
        }
        return bean;
    }

    /**
     * Resolve the specified value if possible.
     * @param value the value to resolve
     * @return the resolved value
     * @see ConfigurableBeanFactory#resolveEmbeddedValue
     */
    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }

    private Collection<MQListener> findListenerAnnotations(Class<?> clazz) {
        Set<MQListener> listeners = new HashSet<>();
        MQListener ann = AnnotationUtils.findAnnotation(clazz, MQListener.class);
        if (ann != null) {
            listeners.add(ann);
        }
        return listeners;
    }

    private Set<MQListener> findListenerAnnotations(Method method) {
        Set<MQListener> listeners = new HashSet<>();
        MQListener ann = AnnotatedElementUtils.findMergedAnnotation(method, MQListener.class);
        if (ann != null) {
            listeners.add(ann);
        }
        return listeners;
    }

    protected void processListener(MQListener listener, Method method, Object bean, String beanName) {
        Method methodToUse = checkProxy(method, bean);
//        String containerFactoryBeanName = resolve(listener.getListener());
        MessageListenerAdapter adapter = new MessageListenerAdapter();
        adapter.setBean(bean);
        adapter.setListenerType(listener.autoAck() ? 1 : 2);
        adapter.setHandleMethod(methodToUse);
        factory.registerConsumer(listener, adapter);
    }

    private Method checkProxy(Method methodArg, Object bean) {
        Method method = methodArg;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @KafkaListener method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iface : proxiedInterfaces) {
                    try {
                        method = iface.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    }
                    catch (NoSuchMethodException noMethod) {
                    }
                }
            }
            catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
            catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                        "@KafkaListener method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(),
                        method.getDeclaringClass().getSimpleName()), ex);
            }
        }
        return method;
    }
}
