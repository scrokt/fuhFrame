package cn.ymcd.ods.service.id;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import cn.ymcd.ods.db.base.dao.SimpleDAO;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.ods.util.StringUtil;

@Service
public class IdServiceImpl implements IdService {

    private static Long SERIAL_HIGH_WEIGHT = 0l;
    private static String HIGH_FLAG = "";

    @Value(value = "${serialhigh:0}")
    private int serialhigh;

    @PostConstruct
    public void init() {
        if (serialhigh / 10 == 0) {
            HIGH_FLAG = "0" + serialhigh;
        } else {
            HIGH_FLAG = "" + serialhigh;
        }
        SERIAL_HIGH_WEIGHT = serialhigh * 1000000000000l;
    }

    @Resource(name = "defaultdb")
    private SimpleDAO simpleDAO;

    @Transactional(value = "tm")
    @Override
    public long getLongId(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return -1L;
        }
        List<Long> serialNumberList = simpleDAO.getJdbcTemplate().queryForList(
                "select serialnumber from SM_SERIALNUM where lower(TABLENAME)=?", Long.class, tableName.toLowerCase());
        Long serialNumber = 0L;
        if (CollectionUtils.isEmpty(serialNumberList)) {
            serialNumber = 1L;
            simpleDAO.getJdbcTemplate().update("insert into sm_serialnum(tablename,serialnumber) values(?,?)",
                    tableName, 1);
            return SERIAL_HIGH_WEIGHT + serialNumber;
        }
        if (serialNumberList.size() == 1) {
            serialNumber = serialNumberList.get(0);
            Long updateNum = serialNumber + 1;
            int update = simpleDAO.getJdbcTemplate().update(
                    "update sm_serialnum set serialnumber=? where lower(tablename)=? and serialnumber=?", updateNum,
                    tableName.toLowerCase(), serialNumber);
            if (update == 0) {
                // 值被其它程序读取
                return getLongId(tableName);
            } else {
                return SERIAL_HIGH_WEIGHT + updateNum;
            }
        } else {
            // 如果大于2，存在并存的情况
            for (Long num : serialNumberList) {
                if (num > serialNumber) {
                    serialNumber = num;
                }
            }

            Long updateNum = serialNumber + 1;
            int update = simpleDAO.getJdbcTemplate().update(
                    "update sm_serialnum set serialnumber=? where lower(tablename)=? and serialnumber=?", updateNum,
                    tableName.toLowerCase(), serialNumber);
            if (update == 0) {
                // 值被其它程序读取
                return getLongId(tableName);
            } else {
                simpleDAO.getJdbcTemplate().update("update sm_serialnum set serialnumber=? where lower(tablename)=?",
                        updateNum, tableName.toLowerCase());
                return SERIAL_HIGH_WEIGHT + updateNum;
            }
        }
    }

    @Override
    public String getRowKey() {
        return HIGH_FLAG + String.valueOf(Long.MAX_VALUE - System.currentTimeMillis()) + GetRandom() + getCount();
    }

    private String GetRandom() {
        int random = (int)(Math.random() * 1000000);
        return String.valueOf(random);
    }

    private static Integer count = 0;
    private static long currTime = System.currentTimeMillis();

    private static String getCount() {
        synchronized (count) {
            count = count + 1;
            if (count == 9999 || System.currentTimeMillis() - currTime > 1) {
                count = 0;
                currTime = System.currentTimeMillis();
            }
            String str = String.valueOf(count);
            int length = str.length();
            for (int i = 0;i < 4 - length;i++) {
                str = "0" + str;
            }
            return str;
        }
    }

    @Override
    public long getLongId(Class<?> dtoClass) {
        String tableName = PojoUtils.getTableName(dtoClass);
        return getLongId(tableName);
    }

}
