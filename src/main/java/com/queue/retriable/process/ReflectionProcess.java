package com.queue.retriable.process;

import com.queue.retriable.annotation.RetriableQueueMethod;
import com.queue.retriable.dto.AnnotationResult;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Service
public class ReflectionProcess {

    public Method getMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Class<?> methodClass = joinPoint.getTarget().getClass();
        Class<?>[] argsTypes = new Class[joinPoint.getArgs().length];
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            argsTypes[i] = joinPoint.getArgs()[i].getClass();
        }
        try {
            return methodClass.getMethod(methodName, argsTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getMainArg(JoinPoint joinPoint, Class<?> aClass) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if(arg == null) continue;
            if(arg.getClass().getTypeName().equals(aClass.getTypeName())) {
                return arg;
            }
        }
        throw new RuntimeException("Object "+aClass.getTypeName()+" not found in method " + getMethod(joinPoint).getName());
    }

    public AnnotationResult getAnnotationResult(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if(RetriableQueueMethod.class.equals(annotation.annotationType())) {
                RetriableQueueMethod retriableQueueAnnotation = (RetriableQueueMethod) annotation;
                return new AnnotationResult(
                        retriableQueueAnnotation.maxAttempents(),
                        retriableQueueAnnotation.messageOnException(),
                        retriableQueueAnnotation.messageOnMaxExecutions(),
                        retriableQueueAnnotation.onMaxAttempentsSendToQueue(),
                        retriableQueueAnnotation.onAttempentsSendToQueue()
                );
            }
        }
        throw new RuntimeException("Annotation RetriableQueueMethod not found in method " + method.getName());
    }
}
