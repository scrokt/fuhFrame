package cn.ymcd.ods.exception;

/**
 * 业务处理异常
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年12月21日 上午10:40:29
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

}
