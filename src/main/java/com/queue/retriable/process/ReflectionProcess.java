package com.queue.retriable.process;

import com.queue.retriable.annotation.RetriableQueueMethod;
import com.queue.retriable.dto.AnnotationResult;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReflectionProcess {

    public Method getMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Class<?> methodClass = joinPoint.getTarget().getClass();
        List<Method> methodByName = getMethodByName(methodClass, methodName);

        if (methodByName.size() == 1) return methodByName.get(0);

        Class<?>[] argsTypes = new Class[joinPoint.getArgs().length];
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            argsTypes[i] = joinPoint.getArgs()[i].getClass();
        }

        return getMethodByNameAndArgs(methodClass, methodName, argsTypes);
    }

    public List<Method> getMethodByName(Class<?> aClass, String name) {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(name)) {
                methods.add(method);
            }
        }
        if (methods.isEmpty()) throw new RuntimeException("Method with name " + name + " not found.");
        return methods;
    }

    public Method getMethodByNameAndArgs(Class<?> aClass, String name, Class<?>... args) {
        try {
            return aClass.getMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getMainArg(JoinPoint joinPoint, Class<T> aClass) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) continue;
            if (arg.getClass().getTypeName().equals(aClass.getTypeName())) {
                return aClass.cast(arg);
            }
        }
        throw new RuntimeException("Object " + aClass.getTypeName() + " not found in method " + getMethod(joinPoint).getName());
    }

    public void verifyExistenceOfInArgs(JoinPoint joinPoint, Class<?>... classes) {
        boolean contains;
        for (Class<?> aClass : classes) {
            contains = false;
            for (Object arg : joinPoint.getArgs()) {
                if (arg.getClass().getTypeName().equals(aClass.getTypeName())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                throw new RuntimeException("Object " + aClass.getTypeName() + " not found in method " + getMethod(joinPoint).getName());
            }
        }
    }

    public AnnotationResult getAnnotationResult(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if(RetriableQueueMethod.class.equals(annotation.annotationType())) {
                RetriableQueueMethod retriableQueueAnnotation = (RetriableQueueMethod) annotation;
                return new AnnotationResult(
                        retriableQueueAnnotation.maxAttempts(),
                        retriableQueueAnnotation.messageOnException(),
                        retriableQueueAnnotation.messageOnMaxExecutions(),
                        retriableQueueAnnotation.onMaxAttemptsSendToQueue(),
                        retriableQueueAnnotation.onAttemptsSendToQueue()
                );
            }
        }
        throw new RuntimeException("Annotation RetriableQueueMethod not found in method " + method.getName());
    }
}
