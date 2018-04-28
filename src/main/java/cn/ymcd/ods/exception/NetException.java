package cn.ymcd.ods.exception;

/**
 * 网络方面异常
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年12月21日 上午10:40:29
 * @version 1.0
 */
public class NetException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NetException() {
        super();
    }

    public NetException(String message) {
        super(message);
    }

    public NetException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetException(Throwable cause) {
        super(cause);
    }

}
