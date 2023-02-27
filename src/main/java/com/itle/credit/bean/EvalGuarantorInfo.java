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
 * 2021-11-24 fangxi	补充担保字段
 * 
 * 类名: com.post.scms.credit.vo.EvalGuarantorInfo
 * 创建时间:2020-11-10
 * 创建人:liukun
 * 功能描述:EVAL_GUARANTOR_INFO业务对象实体类
 */

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "DEMO_GUARANTOR_INFO")
public class EvalGuarantorInfo {
	/** 主键ID */
	@ExcelIgnore
	@TableId(type = IdType.AUTO,value = "GUA_ID")
	private String guaId;
	/** 附件ID */
	@ExcelProperty(value = "信贷记录id")
	@TableField("FILE_ID")
	private String fileId;
	/** 责任人类型 */
	@ExcelProperty(value = "责任人类型")
	@TableField("GUA_PER_TYPE")
	private String guaPerType;
	/** 责任金额 */
	@ExcelProperty(value = "责任金额")
	@TableField("GUA_AMT")
	private int guaAmt;
	/** 担保日期 */
	@ExcelProperty(value = "担保日期")
	@TableField("GUA_DATE")
	private String guaDate;
	/** 被担保人 */
	@ExcelProperty(value = "被担保人")
	@TableField("GUA_PER")
	private String guaPer;
	/** 担保余额 */
	@ExcelProperty(value = "担保余额")
	@TableField("GUA_LEFT")
	private int guaLeft;

}
