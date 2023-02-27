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
 * create by lushengle on 2022/6/2
 *
 * @description 征信机构查询记录明细实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "DEMO_DEPT_QUERY")
public class EvalDeptQuery {
    /** 主键 */
    @ExcelIgnore
    @TableId(type = IdType.AUTO,value = "ID")
    private Long id;
    /** 信贷记录id */
    @ExcelProperty(value = "信贷记录id")
    @TableField("FILE_ID")
    private String fileId;
    /** 查询时间 */
    @ExcelProperty(value = "查询时间")
    @TableField("QUERY_DATE")
    private String queryDate;
    /** 查询机构的名称 */
    @ExcelProperty(value = "查询机构名称")
    @TableField("DEPT_NAME")
    private String deptName;
    /** 查询原因 */
    @ExcelProperty(value = "查询原因")
    @TableField("QUERY_REASON")
    private String queryReason;

}
