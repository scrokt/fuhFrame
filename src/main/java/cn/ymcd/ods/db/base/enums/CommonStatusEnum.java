package cn.ymcd.ods.db.base.enums;

import cn.ymcd.ods.db.base.enums.NameValueEnum;


/**
 * 通用状态枚举；
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年9月12日 上午11:38:06
 * @version 1.0
 */
public enum CommonStatusEnum implements NameValueEnum<Integer>{
    NOMAL(1, "正常"), DELETED(-1, "已删除"), EXCEPTION(4, "异常");

    private CommonStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private int value;

    private String name;
    
    @Override
    public Integer getValue() {
        return value;
    }
    @Override
    public String getName() {
        return name;
    }
    
    public static CommonStatusEnum  getByValue(int value){
        CommonStatusEnum []  enums = CommonStatusEnum.values();
        for (int i = 0; i < enums.length; i++) {
            if(value == enums[i].getValue()){
                return enums[i];
            }
        }
        return null;
    }
}
