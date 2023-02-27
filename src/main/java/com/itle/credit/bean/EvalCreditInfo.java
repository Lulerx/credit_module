package com.itle.credit.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 修改日期    修改人    修改原因
 * 2021-11-10	fangxi	字段规范重命名 beginDate
 * 
 * @类名 com.post.scms.credit.vo.EvalCreditInfo
 * @创建日期 2020年11月10日
 * @创建人 liukun
 * @模块用途 征信pdf解析数据实体类
 *
 */
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "DEMO_EVAL_CREDIT_INFO")
public class EvalCreditInfo {
	/** 主键ID */
	@ExcelProperty(value = "信贷记录id")
	@TableId(type = IdType.AUTO,value = "CREDIT_ID")
	private String creditId;
	/** 用户姓名 */
	@ExcelProperty(value = "姓名")
	@TableField("USER_NAME")
	private String userName;
	@ExcelProperty(value = "机构全称")
	@TableField("DEPT_ALLNAME")
	private String deptAllName;
	/** 拉链表开始时间 */
	@ExcelProperty(value = "解析日期")
	@TableField("BEGIN_DATE")
	private Date beginDate;
	/** 拉链表结束时间 */
	@ExcelProperty(value = "到期日期")
	@TableField("END_DATE")
	private Date endDate;
	/** 身份证ID */
	@ExcelProperty(value = "身份证号码")
	@TableField("ID_CARD")
	private String idCard;
	/** 婚姻状况，1-已婚，2-离婚，3-其他 */
	@ExcelProperty(value = "婚姻状况")
	@TableField("IS_MARRY")
	private String isMarry;
	/** 信用卡激活张数 */
	@ExcelProperty(value = "信用卡激活张数")
	@TableField("CREDIT_ACT_NUM")
	private int creditActNum;
	/** 个人消费贷笔数 */
	@ExcelProperty(value = "个人消费贷笔数")
	@TableField("LOANS_CONS_NUM")
	private int loansConsNum;
	/** 个人消费贷未结清笔数 */
	@ExcelProperty(value = "个人消费贷未结清笔数")
	@TableField("LOANS_CONS_WJQ")
	private int loansConsWjq;
	/** 个人汽车贷款笔数 */
	@ExcelProperty(value = "个人汽车贷款笔数")
	@TableField("LOANS_CAR_NUM")
	private int loansCarNum;
	/** 个人汽车贷款未结清笔数 */
	@ExcelProperty(value = "个人汽车贷款未结清笔数")
	@TableField("LOANS_CAR_WJQ")
	private int loansCarWjq;
	/** 购房贷款账户数 */
	@ExcelProperty(value = "购房贷款账户数")
	@TableField("LOANS_HOUSE_NUM")
	private int loansGfdkNum;
	/** 购房贷款发生过90天以上逾期的账户数 */
	@TableField("LOANS_GFDK90YQ_NUM")
	@ExcelProperty(value = "购房贷款发生过90天以上逾期的账户数")
	private int loansGfdk90yqNum;
	/** 购房贷款发生过逾期的账户数 */
	@ExcelProperty(value = "购房贷款发生过逾期的账户数")
	@TableField("LOANS_GFDKYQ_NUM")
	private int loansGfdyyqNum;
	/** 购房贷款未结清/未销户账户数 */
	@ExcelProperty(value = "购房贷款未结清/未销户账户数")
	@TableField("LOANS_GFDK_WJQ_NUM")
	private int loansGfdkWjqNum;
	/** 信用卡账户数 */
	@ExcelProperty(value = "信用卡账户数")
	@TableField("CREDIT_EFF_NUM")
	private int creditEffNum;
	/** 信用卡未结清/未销户账户数 */
	@ExcelProperty(value = "信用卡未结清/未销户账户数")
	@TableField("CREDIT_WJQ_NUM")
	private int creditWjqNum;
	/** 信用卡发生过逾期的账户数 */
	@ExcelProperty(value = "信用卡发生过逾期的账户数")
	@TableField("CREDIT_OVERDUE_NUM")
	private int creditOverdueNum;
	/** 信用卡发生过90天以上逾期的账户数 */
	@ExcelProperty(value = "信用卡发生过90天以上逾期的账户数")
	@TableField("CREDIT_OVERDUE90_NUM")
	private int creditOverdue90Num;


	/** 其他业务账户数 */
	@ExcelProperty(value = "其他业务账户数")
	@TableField("QTYW_NUM")
	private int qtywNum;
	/** 其他业务未结清/未销户账户数 */
	@ExcelProperty(value = "其他业务未结清账户数")
	@TableField("LOANS_QTYW_NUM")
	private int loansQtywNum ;
	/** 其他业务发生过逾期的账户数 */
	@ExcelProperty(value = "其他业务发生过逾期的账户数")
	@TableField("QTYW_YQ_NUM")
	private int qtyhYqNum ;
	/** 其他业务发生过90天以上逾期的账户数 */
	@ExcelProperty(value = "其他业务发生过90天以上逾期的账户数")
	@TableField("QTYW_90YQ_NUM")
	private int qtyh90yqNum;

	/** 其他贷款账户数 */
	@ExcelProperty(value = "其他贷款账户数")
	@TableField("LOANS_OTHER_NUM")
	private int loansOtherNum;
	/** 其他贷款未结清/未销户账户数 */
	@ExcelProperty(value = "其他贷款未结清/未销户账户数")
	@TableField("LOANS_QTDK_NUM")
	private int loansQtdkNum;
	/** 其他贷款发生过逾期的账户数 */
	@ExcelProperty(value = "其他贷款发生过逾期的账户数")
	@TableField("LOANS_QTYQ_NUM")
	private int loansQtyqNum;
	/** 其他贷款发生过90天以上逾期的账户数 */
	@ExcelProperty(value = "其他贷款发生过90天以上逾期的账户数")
	@TableField("LOANS_QTDK90YQ_NUM")
	private int loansQtdk90yqNum;


	/** 强制执行次数 */
	@ExcelProperty(value = "强制执行次数")
	@TableField("ENFORCE_NUM")
	private int enforceNum;
	/** 担保次数 */
	@ExcelProperty(value = "担保次数")
	@TableField("DB_NUM")
	private int dbNum;
	/** 总次数 */
	@ExcelProperty(value = "查询总次数")
	@TableField("QUERY_ALL_NUM")
	private int queryAllNum;
	/** 本人查询次数 */
	@ExcelProperty(value = "本人查询次数")
	@TableField("QUERY_SELF_NUM")
	private int querySelfNum;
	/** 机构查询次数 */
	@ExcelProperty(value = "机构查询次数")
	@TableField("QUERY_ORG_NUM")
	private int queryOrgNum;
	/** 贷后管理 */
	@ExcelProperty(value = "贷后管理次数")
	@TableField("QUERY_LOANS_NUM")
	private int queryLoansNum;
	/** 贷款审批 */
	@ExcelProperty(value = "贷款审批次数")
	@TableField("QUERY_APPLY_NUM")
	private int queryApplyNum;
	/** 信用卡审批 */
	@ExcelProperty(value = "信用卡审批次数")
	@TableField("QUERY_CREDIT_APPLY_NUM")
	private int queryCreditApplyNum;
	/** 担保资格审查 */
	@ExcelProperty(value = "担保资格审查次数")
	@TableField("QUERY_CONTRI_NUM")
	private int queryContriNum;


	/** 相关还款责任账户数为个人 */
	@ExcelProperty(value = "相关还款责任账户数为个人")
	@TableField("CREDIT_ZRR_NUM")
	private int creditZrrNum;
	/** 相关还款责任账户数为企业 */
	@ExcelProperty(value = "相关还款责任账户数为企业")
	@TableField("LOANS_QYZH_NUM")
	private int loansQyzhNum;
//	/** 附件ID */
//	@ExcelProperty(value = "主表ID")
//	@TableField("FILE_ID")
//	private String fileId;
	@ExcelIgnore
	@TableField("CREDIT_DATE")
	private String creditDate;
}
