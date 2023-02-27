package com.itle.credit.service;

import com.itle.credit.bean.*;
import com.itle.credit.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * create by Luler on 2023/2/9 17:57
 *
 * @description 征信相关数据的保存操作
 */
@Service
public class CreditCurdService {
    private static final Logger log = LoggerFactory.getLogger(CreditCurdService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    /**
     * 根据机构 ID 获取机构名全称
     * @param deptId
     * @return
     */
    public String findDeptName(String deptId) {
        String sql = "SELECT DEPT_ALLNAME FROM DEMO_TB_DEPT_ALLNAME WHERE DEPT_ID = ? ";
        List<String> list = jdbcTemplate.queryForList(sql, new Object[]{deptId}, String.class);
        return list.size() > 0 ? list.get(0) : null;
    }


    /**
     * 保存主信息 CreditInfo
     * @param evalCreditInfo
     */
    public void saveEvalCreditInfo(EvalCreditInfo evalCreditInfo) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(evalCreditInfo);
        String sql = "INSERT INTO DEMO_EVAL_CREDIT_INFO \n" +
                "(CREDIT_ID, USER_NAME, DEPT_ALLNAME, BEGIN_DATE, END_DATE, ID_CARD, IS_MARRY, CREDIT_EFF_NUM, LOANS_CONS_NUM, LOANS_HOUSE_NUM, CREDIT_OVERDUE90_NUM, CREDIT_OVERDUE_NUM, CREDIT_MONTHLY_NUM, CREDIT_WJQ_NUM, CREDIT_ACT_NUM, QTYW_90YQ_NUM, QTYW_YQ_NUM, LOANS_QTYW_NUM, QTYW_NUM, LOANS_QTDK90YQ_NUM, LOANS_QTYQ_NUM, LOANS_QTDK_NUM, LOANS_OTHER_NUM, ENFORCE_NUM, QUERY_ALL_NUM, DB_NUM, QUERY_SELF_NUM, QUERY_ORG_NUM, CREDIT_ZRR_NUM, LOANS_QYZH_NUM, LOANS_GFDK90YQ_NUM, LOANS_GFDKYQ_NUM, LOANS_GFDK_WJQ_NUM, LOANS_GFDK_NUM,  QUERY_CREDIT_APPLY_NUM, QUERY_CONTRI_NUM, QUERY_LOANS_NUM, QUERY_APPLY_NUM, LOANS_CONS_WJQ, LOANS_CAR_NUM, LOANS_CAR_WJQ, CREDIT_DATE) \n" +
                "VALUES(SEQ_DEMO_EVAL_CREDIT_INFO.NEXTVAL, :userName, :deptAllName, :beginDate, :endDate, :idCard, :isMarry, :creditEffNum, :loansConsNum, :loansGfdkNum,:creditOverdue90Num,:creditOverdueNum,null,:creditWjqNum, :creditActNum,  :qtyh90yqNum,  :qtyhYqNum,  :loansQtywNum,  :qtywNum, :loansQtdk90yqNum,  :loansQtyqNum,  :loansQtdkNum,  :loansOtherNum,  :enforceNum, :queryAllNum,  :dbNum, :querySelfNum,  :queryOrgNum,  :creditZrrNum,  :loansQyzhNum,  :loansGfdk90yqNum,  :loansGfdyyqNum,  :loansGfdkWjqNum,  :loansGfdkNum,      :queryCreditApplyNum,   :queryContriNum,  :queryLoansNum,  :queryApplyNum,  :loansConsNum,  :loansCarNum,  :loansCarWjq, :creditDate)";
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }


    /**
     * 批量保存
     * @param list
     */
    public void batchSaveEvalCreditItem(List<EvalCreditItem> list) {
        Integer fileId = jdbcTemplate.queryForObject("SELECT SEQ_DEMO_EVAL_CREDIT_INFO.CURRVAL FROM dual", Integer.class);
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        String sql = "INSERT INTO DEMO_CREDIT_ITEM \n" +
                "(ITEM_ID, ITEM_CREDIT_AMT, ITEM_COMP_NAME, ITEM_USE_AMT, ITEM_MONEY_TYPE, ITEM_ACC_TYPE, ITEM_OVER_AMT, FILE_ID, ITEM_RD, ITEM_CARD_NUM)\n" +
                "VALUES(SEQ_DEMO_CREDIT_ITEM.NEXTVAL, :itemCreditAmt, :itemCompName, :itemUseAmt, :itemMoneyType, :itemAccType, :itemOverAmt, "+fileId+", :itemRd, :itemCardNum) ";
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    /**
     * 批量保存
     * @param list
     */
    public void batchSaveEvalDeptQuery(List<EvalDeptQuery> list) {
        Integer fileId = jdbcTemplate.queryForObject("SELECT SEQ_DEMO_EVAL_CREDIT_INFO.CURRVAL FROM dual", Integer.class);
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        String sql = "INSERT INTO DEMO_DEPT_QUERY \n" +
                "(ID, FILE_ID, QUERY_DATE, DEPT_NAME, QUERY_REASON)\n" +
                "VALUES(SEQ_DEMO_DEPT_QUERY.NEXTVAL, "+fileId+", :queryDate, :deptName, :queryReason)";
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    /**
     * 批量保存
     * @param list
     */
    public void batchSaveEvalGuarantorInfo(List<EvalGuarantorInfo> list) {
        Integer fileId = jdbcTemplate.queryForObject("SELECT SEQ_DEMO_EVAL_CREDIT_INFO.CURRVAL FROM dual", Integer.class);
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        String sql = "INSERT INTO DEMO_GUARANTOR_INFO \n" +
                "(GUA_ID, GUA_PER_TYPE, GUA_AMT, FILE_ID, GUA_DATE, GUA_PER, GUA_LEFT) \n" +
                "VALUES(SEQ_DEMO_GUARANTOR_INFO.NEXTVAL, :guaPerType, :guaAmt, "+fileId+", :guaDate, :guaPer, :guaLeft)";
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    /**
     * 批量保存
     * @param list
     */
    public void batchSaveEvalLoansItem(List<EvalLoansItem> list) {
        Integer fileId = jdbcTemplate.queryForObject("SELECT SEQ_DEMO_EVAL_CREDIT_INFO.CURRVAL FROM dual", Integer.class);
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        String sql = "INSERT INTO DEMO_LOANS_ITEM \n" +
                "(LOANS_ID, LOANS_MONEY, LOANS_COMP, LOANS_MON_TYPE, LOANS_STATUS, LOANS_TYPE, LOANS_AMT, FILE_ID, LOANS_RD, LOANS_CLR_DATE) \n" +
                "VALUES(SEQ_DEMO_LOANS_ITEM.NEXTVAL, :loansMoney, :loansComp, :loansMonType, :loansStatus, :loansType, :loansAmt, "+fileId+", :loansRd, :loansClrDate)";
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

}
