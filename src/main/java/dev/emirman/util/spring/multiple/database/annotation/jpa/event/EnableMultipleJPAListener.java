package dev.emirman.util.spring.multiple.database.annotation.jpa.event;

import java.util.logging.Logger;

/*@Aspect
@Component*/
public class EnableMultipleJPAListener {

    Logger logger = Logger.getLogger(EnableMultipleJPAListener.class.getName());

    /*@Around(value = "execution(public * *(..))")
    public Object listen(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        if (method == null) {
            return point.proceed();
        }
        EnableMultipleJPA enableMultipleJPA = method.getAnnotation(EnableMultipleJPA.class);
        if (enableMultipleJPA != null) {
            String[] scanPackages = enableMultipleJPA.scanPackages();
            MultipleDBContextHolder.scanPackages(scanPackages);
            logger.info("Scan packages: " + Arrays.toString(scanPackages));
        }
        return point.proceed();
    }*/
}
