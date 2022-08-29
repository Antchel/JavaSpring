package com.agolovenko.jspring.OSM.TimeTrack;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AspectJobExecutor {
    @Around("@annotation(TimeTracker)")
    public Object executionTime(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.nanoTime();
        Object object = point.proceed();
        long endTime = System.nanoTime();
        log.info("Class Name: "
                + point.getSignature().getDeclaringTypeName()
                + ". Method Name: " + point.getSignature().getName()
                + ". Time taken for Execution is : "
                + (endTime - startTime)/1000 + "mcs");
        return object;
    }
}