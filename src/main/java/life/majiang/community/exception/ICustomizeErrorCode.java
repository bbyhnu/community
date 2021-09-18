package life.majiang.community.exception;

/**
 * Created by codedrinker on 2019/5/28.
 * 不同的业务都能来实现这个接口，来处理属于自己业务中的异常
 */
public interface ICustomizeErrorCode {
    String getMessage() ;
    Integer getCode();
}
