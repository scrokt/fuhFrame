package cn.ymcd.ods.service.id;

/**
 * 主键生成服务接口
 * @author fuh
 * @since 1.0.1
 */
public interface IdService {

    /**
     * 获取long型主键，从sm_serialnum查询序列号
     * @param tableName 表名
     * @return
     */
    long getLongId(String tableName);
    
    long getLongId(Class<?> dtoClass);
    
    /**
     * 获取rowKey主键，rowKey算法参见实现
     * @return
     */
    String getRowKey();
}
