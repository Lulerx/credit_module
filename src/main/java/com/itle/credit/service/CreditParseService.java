package com.itle.credit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itle.credit.bean.*;
import com.itle.credit.enums.BaseInfoEnum;
import com.itle.credit.enums.CreditCardEnum;
import com.itle.credit.enums.GuaranteeEnum;
import com.itle.credit.enums.LoansDetailEnum;
import com.itle.credit.utils.CreditUtil;
import com.itle.credit.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * create by Luler on 2023/2/3 11:50
 *
 * @description
 */
@Service
public class CreditParseService {

    private static final Logger log = LoggerFactory.getLogger(CreditParseService.class);

    @Resource
    private CreditCurdService creditCurdService;

    @Resource
    private EvalCreditInfoService evalCreditInfoService;
    @Resource
    private EvalCreditItemService creditItemService;
    @Resource
    private EvalDeptQueryService deptQueryService;
    @Resource
    private EvalGuarantorInfoService guarantorInfoService;
    @Resource
    private EvalLoansItemService loansItemService;

//    @Value("${local.path}")
//    private String filePath; //本地文件的路径，在 yml 配置文件中进行配置


    @PostConstruct  //项目启动时执行该方法
    public void run() {
        String creditPath = properties();
        getAllFile(creditPath);

        File file = new File("result");
        file.mkdir();
        String filePath = file.getAbsolutePath();
        evalCreditInfoService.writeExcel(filePath);
        creditItemService.writeExcel(filePath);
        deptQueryService.writeExcel(filePath);
        guarantorInfoService.writeExcel(filePath);
        loansItemService.writeExcel(filePath);
    }


    public String properties() {
        try {
            File file = new File("filePath.properties");
            Properties properties = new Properties();

            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            String path = properties.getProperty("path");
            path=new String(path.getBytes("ISO-8859-1"), "utf-8");
            inputStream.close();
            return path;

        } catch (IOException e) {
            log.info("读取 properties 配置文件过程出异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取所有的文件和文件夹
     *
     * @param filepath 路径
     */
    public void getAllFile(String filepath) {
        List<File> allFiles = new ArrayList<>();
        findFolder(new File(filepath), allFiles);
        List<String> failList = new ArrayList<>();

        for (File file : allFiles) {
            System.out.println("文件名：" + file.getName());
            System.out.println("文件父目录：" + file.getParentFile().getName());
            String parentName = file.getParentFile().getName();
            String deptId = CreditUtil.parseDeptId(parentName);
            String deptAllName;
            if (StringUtil.isNotEmpty(deptId)) {
                String res = creditCurdService.findDeptName(deptId);
                deptAllName = res == null ? parentName : res;
            } else {
                deptAllName = parentName;
            }

            try {
                String resJson = parsePdf(file.getAbsolutePath(), deptAllName);
                saveJsonPdf(resJson);
            } catch (Exception e) {
                failList.add(file.getAbsolutePath());
            }
        }
        log.info("======================================================================");
        log.info("======================================================================\n");
        log.info("一共解析{}个文件，失败{}个。", allFiles.size(), failList.size());
        if (failList.size() > 0) {
            log.info("失败的文件如下：");
            for (String s : failList) {
                System.out.println(s);
            }

        }
        log.info("======================================================================\n");
    }

    /**
     * 递归获取所有文件
     *
     * @param file     路径
     * @param allFiles list
     */
    private void findFolder(File file, List<File> allFiles) {
        if (file.isDirectory()) {
//            allFiles.add(file);
            File[] files = file.listFiles();
            for (File f : files) {
                findFolder(f, allFiles);
            }
        } else {
            allFiles.add(file);
        }
    }


    /**
     * 判断路径是文件还是压缩包，压缩包则解压提取
     *
     * @param filePath 文件路径
     */
    public void parsePath(String filePath, String deptAllName) {
        if (filePath.contains(".pdf")) { //是pdf文件，直接解析
            String resJson = parsePdf(filePath, deptAllName);
            saveJsonPdf(resJson);
        } else if (filePath.contains(".zip")) {  //压缩包，依次提取再进行解析
            log.info("开始提取压缩包文件 ================>");
            try {
                ZipFile zf = new ZipFile(filePath, Charset.forName("GBK"));
                ZipEntry entry;
                InputStream is;
                Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    entry = (ZipEntry) e.nextElement();
                    if (entry.isDirectory()) {
                        log.info("检测到压缩包内有文件夹，跳过");
                        continue;
                    }
                    if (!entry.getName().contains(".pdf")) {
                        log.info("{} 不是pdf文件，跳过！", entry.getName());
                        continue;
                    }
                    //读取文件，复制文件到本地
                    is = zf.getInputStream(entry);
                    int index;
                    byte[] bytes = new byte[1024];
                    String localFilePath = "E:\\工作文档\\A-人员合规系统\\第4期需求\\单机版征信\\credit\\all\\" + entry.getName();
                    FileOutputStream fos = new FileOutputStream(localFilePath);
                    while ((index = is.read(bytes)) != -1) {
                        fos.write(bytes, 0, index);
                        fos.flush();
                    }
                    fos.close();

                    log.info("开始解析压缩包内文件：{}", entry.getName());
                    parsePath(localFilePath, deptAllName);
                    //删除复制的文件
                    boolean delete = new File(localFilePath).delete();
                }
            } catch (IOException e) {
                log.error("解析压缩包出异常==========> {}", e.getMessage());
            }
        }
    }


    /**
     * 解析 pdf 文件的入口
     *
     * @param filePath PDF文件路径
     * @return 解析后的 json
     */
    public String parsePdf(String filePath, String deptAllName) {

        String ccJson = CreditUtil.creditScore(filePath, deptAllName);
        ccJson = CreditUtil.ruleTransform(ccJson);
        //将 key 转成字母命名格式
        String res = convertEn(ccJson);

        return res;
    }


    /**
     * 将 json 数据依次存库
     *
     * @param pdfJson 解析的json数据
     */
    public void saveJsonPdf(String pdfJson) {
        JSONObject main = JSON.parseObject(pdfJson);
        // 信贷记录
        JSONObject xdjl = main.getJSONObject("xdjl");
        // .getJSONObject("psbc") 错误的key 不会抛出异常 返回null
        // 相关还款责任人明细
        JSONObject xghkzrrmx = xdjl != null ? xdjl.getJSONObject("xghkzrrmx") : null;
        // 担保人明细 from xdil
        JSONArray dbrmx = xghkzrrmx != null ? xghkzrrmx.getJSONArray("dbrmx") : null;
        // 信用卡明细 from xdjl
        JSONArray xykmx = xdjl != null ? xdjl.getJSONArray("xykmx") : null;
        // 贷款明细 from xdjl
        JSONArray dkmx = xdjl != null ? xdjl.getJSONArray("dkmx") : null;
        // 机构查询明细
        JSONArray deptQueryDtl = main.getJSONArray("deptQueryDtl");

        EvalCreditInfo evalCreditInfo = this.parseCreditInfo(main);


        // 信用卡明细集合
        List<EvalCreditItem> creditItemList = null;
        if (xykmx != null) {
            log.info("信用卡明细解析============>" + xykmx);
            creditItemList = this.getItemList(evalCreditInfo.getCreditId(), xykmx);
        }
//        System.out.println(creditItemList);

        // 贷款明细
        List<EvalLoansItem> loansList = null;
        if (dkmx != null) {
            log.info("贷款明细解析============>" + dkmx);
            loansList = this.getLoansList(evalCreditInfo.getCreditId(), dkmx);
        }
//        System.out.println(loansList);

        // 担保人明细集合
        List<EvalGuarantorInfo> guaInfoList = null;
        if (dbrmx != null) {
            log.info("担保人明细解析============>" + dbrmx);
            guaInfoList = this.getGuaList(evalCreditInfo.getCreditId(), dbrmx);
        }
//        System.out.println(guaInfoList);

        // 机构查询记录明细
        List<EvalDeptQuery> deptQueryList = null;
        if (deptQueryDtl != null) {
            log.info("机构查询记录明细解析============>" + deptQueryDtl);
            deptQueryList = this.getDeptQueryList(evalCreditInfo.getCreditId(), deptQueryDtl);
        }
//        System.out.println(deptQueryList);


        // 保存各明细数据
        log.info("保存主表实体类evalCreditInfo ===================>");
        creditCurdService.saveEvalCreditInfo(evalCreditInfo);

        if (creditItemList != null) {
            log.info("信用卡明细大小:" + creditItemList.size());
            log.info("开始批量保存信用卡明细 =================>");
            creditCurdService.batchSaveEvalCreditItem(creditItemList);
        }
        if (guaInfoList != null) {
            log.info("担保明细大小:" + guaInfoList.size());
            log.info("开始批量保存担保明细 =================>");
            creditCurdService.batchSaveEvalGuarantorInfo(guaInfoList);
        }
        if (loansList != null) {
            log.info("贷款明细大小:" + loansList.size());
            log.info("开始批量保存贷款明细 =================>");
            creditCurdService.batchSaveEvalLoansItem(loansList);
        }
        if (deptQueryList != null) {
            log.info("机构查询记录明细条数:" + deptQueryList.size());
            log.info("开始批量保存机构查询记录明细 =================>");
            creditCurdService.batchSaveEvalDeptQuery(deptQueryList);
        }
        log.info("============= 保存数据库完成！");
    }


    /**
     * 将中文的转成英文属性
     *
     * @param ccJson
     * @return
     */
    public String convertEn(String ccJson) {
        for (BaseInfoEnum enums : BaseInfoEnum.values()) {
            ccJson = ccJson.replaceAll("\"" + enums.getKey() + "\"", "\"" + enums.getValue() + "\"");
        }
        for (CreditCardEnum enums : CreditCardEnum.values()) {
            ccJson = ccJson.replaceAll("\"" + enums.getKey() + "\"", "\"" + enums.getValue() + "\"");
        }
        for (LoansDetailEnum enums : LoansDetailEnum.values()) {
            ccJson = ccJson.replaceAll("\"" + enums.getKey() + "\"", "\"" + enums.getValue() + "\"");
        }
        for (GuaranteeEnum enums : GuaranteeEnum.values()) {
            ccJson = ccJson.replaceAll("\"" + enums.getKey() + "\"", "\"" + enums.getValue() + "\"");
        }
        return ccJson;
    }

    /**
     * 信用卡明细集合
     *
     * @param fileId
     * @param xykmx
     * @return
     */
    public List<EvalCreditItem> getItemList(String fileId, JSONArray xykmx) {
        List<EvalCreditItem> list = new ArrayList<>();
        for (Object xyk : xykmx) {
            // fastjson Hack: 自动将逗号标识法改成了自然整数
            EvalCreditItem vo = JSON.parseObject(xyk.toString(), EvalCreditItem.class);
            vo.setFileId(fileId);
            list.add(vo);
        }
        return list;
    }

    /**
     * 贷款明细
     *
     * @param fileId
     * @param
     * @return
     */
    public List<EvalLoansItem> getLoansList(String fileId, JSONArray dkmxList) {
        List<EvalLoansItem> list = new ArrayList<EvalLoansItem>();
        for (Object dkmx : dkmxList) {
            EvalLoansItem vo = JSON.parseObject(dkmx.toString(), EvalLoansItem.class);
            vo.setFileId(fileId);
            list.add(vo);
        }
        return list;
    }

    /**
     * 担保人明细集合
     *
     * @param fileId
     * @param
     * @return
     */
    public List<EvalGuarantorInfo> getGuaList(String fileId, JSONArray dbrmxList) {
        List<EvalGuarantorInfo> list = new ArrayList<EvalGuarantorInfo>();
        for (Object dbrmx : dbrmxList) {
            EvalGuarantorInfo vo = JSON.parseObject(dbrmx.toString(), EvalGuarantorInfo.class);
            vo.setFileId(fileId);
            list.add(vo);
        }
        return list;
    }

    public List<EvalDeptQuery> getDeptQueryList(String fileId, JSONArray deptQueryList) {
        ArrayList<EvalDeptQuery> list = new ArrayList<>();
        for (Object dq : deptQueryList) {
            EvalDeptQuery vo = JSON.parseObject(dq.toString(), EvalDeptQuery.class);
            vo.setFileId(fileId);
            list.add(vo);
        }
        return list;
    }


    /**
     * 信用卡主表 实体组装
     * 仍然有NPE漏洞 xdjl 可能为空
     *
     * @param main
     * @return
     */
    private EvalCreditInfo parseCreditInfo(JSONObject main) {
        JSONObject xdjl = main.getJSONObject("xdjl"); // 信贷记录
        JSONObject xghkzrrmx = xdjl.getJSONObject("xghkzrrmx"); // 相关还款责任人明细
        EvalCreditInfo info = new EvalCreditInfo();
        info.setBeginDate(new Date());
        info.setEndDate(new Date(8099, 11, 30));

        JSONObject xygy = xdjl.getJSONObject("xygy"); // 信用概要
        JSONObject cxjl = main.getJSONObject("cxjl"); // 查询记录
        JSONObject ggjl = main.getJSONObject("ggjl"); // 公共记录

        info.setUserName(main.getString("USER_NAME")); // getString("userName") 不抛出异常，返回NULL值
        info.setDeptAllName(main.getString("DEPT_ALLNAME"));
        info.setIdCard(main.getString("ID_CARD"));
        info.setIsMarry(main.getString("IS_MARRY"));
        info.setCreditDate(main.getString("CREDIT_DATE"));
        info.setCreditActNum(formatInt(xdjl.getString("CREDIT_ACT_NUM")));
        info.setLoansConsNum(formatInt(xdjl.getString("LOANS_CONS_NUM")));
        info.setCreditOverdue90Num(formatInt(xygy.getString("CREDIT_OVERDUE90_NUM")));
        info.setCreditOverdueNum(formatInt(xygy.getString("CREDIT_OVERDUE_NUM")));
        info.setCreditWjqNum(formatInt(xygy.getString("CREDIT_WJQ_NUM")));
        info.setCreditEffNum(formatInt(xygy.getString("CREDIT_EFF_NUM")));
        info.setQtyh90yqNum(formatInt(xygy.getString("QTYW_90YQ_NUM")));
        info.setQtyhYqNum(formatInt(xygy.getString("QTYW_YQ_NUM")));
        info.setLoansQtywNum(formatInt(xygy.getString("LOANS_QTYW_NUM")));
        info.setQtywNum(formatInt(xygy.getString("QTYW_NUM")));
        info.setLoansQtdk90yqNum(formatInt(xygy.getString("LOANS_QTDK90YQ_NUM")));
        info.setLoansQtyqNum(formatInt(xygy.getString("LOANS_QTYQ_NUM")));
        info.setLoansQtdkNum(formatInt(xygy.getString("LOANS_QTDK_NUM")));
        info.setLoansOtherNum(formatInt(xygy.getString("LOANS_OTHER_NUM")));
        info.setEnforceNum(formatInt(ggjl.getString("ENFORCE_NUM")));
        info.setQueryAllNum(formatInt(cxjl.getString("QUERY_ALL_NUM")));
        info.setDbNum(formatInt(xghkzrrmx.getString("DB_NUM")));
        info.setQuerySelfNum(formatInt(cxjl.getString("QUERY_SELF_NUM")));
        info.setQueryOrgNum(formatInt(cxjl.getString("QUERY_ORG_NUM")));
        info.setQueryApplyNum(formatInt(cxjl.getString("QUERY_APPLY_NUM")));
        info.setQueryContriNum(formatInt(cxjl.getString("QUERY_CONTRI_NUM")));
        info.setQueryCreditApplyNum(formatInt(cxjl.getString("QUERY_CREDIT_APPLY_NUM")));
        info.setQueryLoansNum(formatInt(cxjl.getString("QUERY_LOANS_NUM")));
        info.setCreditZrrNum(formatInt(xygy.getString("CREDIT_ZRR_NUM")));
        info.setLoansQyzhNum(formatInt(xygy.getString("LOANS_QYZH_NUM")));
        info.setLoansGfdk90yqNum(formatInt(xygy.getString("LOANS_GFDK90YQ_NUM")));
        info.setLoansGfdyyqNum(formatInt(xygy.getString("LOANS_GFDKYQ_NUM")));
        info.setLoansGfdkWjqNum(formatInt(xygy.getString("LOANS_GFDK_WJQ_NUM")));
        info.setLoansGfdkNum(formatInt(xygy.getString("LOANS_GFDK_NUM")));
        info.setLoansConsWjq(formatInt(xygy.getString("LOANS_CONS_WJQ")));
        info.setLoansCarNum(formatInt(xygy.getString("LOANS_CAR_NUM")));
        info.setLoansCarWjq(formatInt(xygy.getString("LOANS_CAR_WJQ")));
        // 异常情况1 formatInt(null) xygy.getString("LOANS_CONS_Wjq") formatInt(xygy.getString("LOANS_CONS_Wjq"))
        return info;
    }


    /**
     * 字符串转成数字
     * NULL 视为 0
     *
     * @param number
     * @return
     */
    public Integer formatInt(String number) {
        Integer target = 0;
        if (StringUtil.isEmpty(number)) {
            return 0;
        } else if ("null".equals(number)) {
            return 0;
        } else {
            number = number.replaceAll(",", "");
            number = number.replace("，", "");
        }
        try {
            target = Integer.parseInt(number);
        } catch (Exception e) {
            log.info("字符串转成数字发生异常============= {}", e);
            return target;
        }
        return target;
    }


}
