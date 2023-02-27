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
 * 2021-11-24 fangxi	增加开卡日期
 * 
 * 类名: com.post.scms.credit.vo.EvalCreditItem
 * 创建时间:2020-11-10
 * 创建人:liukun
 * 功能描述:EVAL_CREDIT_ITEM业务对象实体类
 */

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "DEMO_CREDIT_ITEM")
public class EvalCreditItem {

	@ExcelIgnore
	@TableId(type = IdType.AUTO,value = "ITEM_ID")
	private String itemId;
	@ExcelProperty(value = "信贷记录id")
	@TableField("FILE_ID")
	private String fileId;
	@ExcelProperty(value = "信用额度")
	@TableField("ITEM_CREDIT_AMT")
	private int itemCreditAmt;
	@ExcelProperty(value = "发卡机构")
	@TableField("ITEM_COMP_NAME")
	private String itemCompName;
	@ExcelProperty(value = "已用额度")
	@TableField("ITEM_USE_AMT")
	private int itemUseAmt;
	@ExcelProperty(value = "人民币/外币")
	@TableField("ITEM_MONEY_TYPE")
	private String itemMoneyType;
	@ExcelProperty(value = "卡目前状态")
	@TableField("ITEM_ACC_TYPE")
	private String itemAccType;
	@ExcelProperty(value = "超额额度")
	@TableField("ITEM_OVER_AMT")
	private int itemOverAmt;
	@ExcelProperty(value = "发卡日期")
	@TableField("ITEM_RD")
	private String itemRd;
	@ExcelProperty(value = "卡尾号")
	@TableField("ITEM_CARD_NUM")
	private String itemCardNum;

}
