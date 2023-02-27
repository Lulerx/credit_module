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

/**
 * 修改日期    修改人    修改原因
 * 2021-11-24 fangxi	补充贷款字段
 * 
 * 类名: com.post.scms.credit.vo.EvalLoansItem
 * 创建时间:2020-11-10
 * 创建人:liukun
 * 功能描述:EVAL_LOANS_ITEM业务对象实体类
 */

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "DEMO_LOANS_ITEM")
public class EvalLoansItem {
	/** 主键ID */
	@ExcelIgnore
	@TableId(type = IdType.AUTO,value = "LOANS_ID")
	private String loansId;
	/** 附件ID */
	@ExcelProperty(value = "信贷记录id")
	@TableField("FILE_ID")
	private String fileId;
	/** 贷款金额 */
	@ExcelProperty(value = "贷款金额")
	@TableField("LOANS_AMT")
	private int loansAmt;
	/** 贷款余额 */
	@ExcelProperty(value = "贷款余额")
	@TableField("LOANS_MONEY")
	private int loansMoney;
	/** 贷款发放机构 */
	@ExcelProperty(value = "贷款发放机构")
	@TableField("LOANS_COMP")
	private String loansComp;
	/** 贷款账户币种 */
	@ExcelProperty(value = "贷款账户币种")
	@TableField("LOANS_MON_TYPE")
	private String loansMonType;
	/** 贷款状态 */
	@ExcelProperty(value = "结清/未结清")
	@TableField("LOANS_STATUS")
	private String loansStatus;
	/** 贷款类型 */
	@ExcelProperty(value = "贷款类型")
	@TableField("LOANS_TYPE")
	private String loansType;
	/** 贷款发放日期 */
	@ExcelProperty(value = "贷款发放日期")
	@TableField("LOANS_RD")
	private String loansRd;
	/** 贷款结清日期 */
	@ExcelProperty(value = "贷款结清日期")
	@TableField("LOANS_CLR_DATE")
	private String loansClrDate;

}
