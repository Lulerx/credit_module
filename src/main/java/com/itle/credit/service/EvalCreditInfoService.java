package com.itle.credit.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itle.credit.bean.EvalCreditInfo;
import com.itle.credit.mapper.EvalCreditInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * create by Luler on 2023/2/24 10:02
 *
 * @description
 */
@Service
public class EvalCreditInfoService {

    @Resource
    private EvalCreditInfoMapper evalCreditInfoMapper;


    public Page findPage(int current, int size){
        Page<EvalCreditInfo> page = new Page<>(current, size);
        Page<EvalCreditInfo> page1 = evalCreditInfoMapper.selectPage(page, null);
        return page1;
    }


    public void writeExcel(String fileName) {
        fileName = fileName + "\\信贷概要表.xlsx";
        // 分页数
        int pageNo = 1;
        // 每页查询数量
        int pageSize = 500;
        // 这里 需要指定写用哪个class去写
        try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
        // 这里注意 如果同一个sheet只要创建一次
        WriteSheet writeSheet = EasyExcel.writerSheet("信贷概要表").head(EvalCreditInfo.class).build();

        //先写入第一页，并且得到总条数
        Page page = findPage(pageNo, pageSize);
        excelWriter.write(page.getRecords(), writeSheet);
        // 没有数据或只有一页数据
        if (page.getTotal() <= 0 || page.getTotal() <= pageSize) {
            excelWriter.finish();
            return ;
        }
        //总页数
        long pageNum = page.getTotal() / pageSize + 1;
        //循环写入
        for (int i = 2; i <= pageNum; i++) {
            // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
            Page resPage = findPage(i, pageSize);
            excelWriter.write(resPage.getRecords(), writeSheet);
        }
        excelWriter.finish();
        }
    }

}
