package cn.ymcd.ods.db.base.enums;

import java.beans.PropertyEditorSupport;

/**
 * 
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年9月12日 上午11:39:30
 * @version 1.0
 */
public class CommonStatusEnumEditor extends PropertyEditorSupport {

    @Override
    public void setValue(Object value) {
        if(value == null){
            super.setValue(null);
            return ;
        }
        int intVal = Integer.parseInt(value.toString());
        super.setValue(CommonStatusEnum.getByValue(intVal));
    }
    
    @Override
    public void setAsText(String text) {
        if(text == null){
            super.setValue(null);
        }
        int intVal = Integer.parseInt(text.toString());
        super.setValue(CommonStatusEnum.getByValue(intVal));
    }
    
}
