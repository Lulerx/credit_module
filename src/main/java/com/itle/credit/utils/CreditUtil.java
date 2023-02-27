package com.itle.credit.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.LogFactory;
// import org.apache.commons.text.StringEscapeUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfDocumentInformation;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.xmp.XmpMetadata;
/**
 * 修改日期    修改人    修改原因
 * 2021-11-10 fangxi	按业务要求修复解析规则
 *
 * @类名 com.post.util.CreditUtils
 * @创建日期 2020年11月10日
 * @创建人 zhaowei
 * @模块用途 征信报告数字化
 */
public class CreditUtil {
	public static final org.apache.commons.logging.Log log = LogFactory.getLog(CreditUtil.class);
	public static String raw; // 经过清洗后的文本
	public static String sep = ("Linux".equals(System.getProperty("os.name"))) ? "\n" : "\\r\\n";
	/**
	 * 查询文件元数据
	 * @param filePath
	 * @return
	 */
	public static String creditDocInfo(String filePath) {
		if(filePath == null || "".equals(filePath)) {
			return "";
		}
		PdfDocument doc = new PdfDocument();
		PdfDocumentInformation docInfo = null;
		Map<String, String> meta = new HashMap<>();
		File file = new File(filePath);
		try {
			doc.loadFromFile(filePath);
			docInfo = doc.getDocumentInformation();
			XmpMetadata  xmd = docInfo.getMetaData();
			meta.put("PDF版本", String.valueOf(doc.getFileInfo().getVersion().getName())); 
			meta.put("文件创建时间", String.valueOf(xmd.getCreateDate()));
			meta.put("文件修改时间", String.valueOf(xmd.getModifyDate()));
			meta.put("页数", String.valueOf(doc.getPages().getCount()));
			meta.put("制作者", String.valueOf(xmd.getProducer()));
			meta.put("文件大小", String.valueOf(file.length()/1024));
		} catch (Exception e) {
			log.error(e);
		} finally {
			doc.close();
		}
		return JSON.toJSONString(meta);
	}
	public static String creditScore(String filePath, String deptAllName) {
		
		Map<String, Object> resultMap = new HashMap<>(); 
		if(filePath == null || "".equals(filePath)) {
			return null;
		}
		
		// 1. 读取PDF，提取文本并清洗
		String info = readpdfSpire(filePath);		//读取pdf文本，转化为string
		info = deletePage(info);					//去掉“第*页，共*页”
		info = replaceSpace(info);					//去空格空行
		setRaw(info);

		// 2. 按报告结构，对文本进行切割
		Map<String, Object> topicInfoMap = new HashMap<>();
		topicInfoMap = matchTopicInfoReg(info); //获取 一级信息
		/**
		 * 个人信息
		 */
		String personInfoStr = MapUtils.getString(topicInfoMap, "个人信息", "");
		Map<String, Object> personInfo = matchPersonInfo(personInfoStr);
		resultMap.put("姓名", MapUtils.getString(personInfo, "姓名", ""));
		resultMap.put("机构代码", deptAllName);
		resultMap.put("证件号", MapUtils.getString(personInfo, "证件号", ""));
		resultMap.put("婚姻状况", MapUtils.getString(personInfo, "婚姻状况", ""));
		resultMap.put("报告时间", MapUtils.getString(personInfo, "报告时间", ""));
		/**
		 * 信贷记录
		 */
		Map<String, Object> accountInfo = getCreditInfo(MapUtils.getString(topicInfoMap,"信贷记录"));
		resultMap.put("信贷记录", accountInfo);
		/**
		 * 查询次数
		 */
		Map<String, Object> queryNumMap = getQueryNum(MapUtils.getString(topicInfoMap, "查询记录")); 
		resultMap.put("查询记录", queryNumMap);
		/**
		 * 机构查询记录明细
		 */
		List<Map<String, Object>> deptQueryList = getDeptQueryList(MapUtils.getString(topicInfoMap, "查询记录"));
		resultMap.put("deptQueryDtl", deptQueryList);
		/**
		 * 强制执行信息
		 */
		String enforceInfoStr = MapUtils.getString(topicInfoMap, "公共记录", "");
		List<String> enforceList = enforceRecordList(enforceInfoStr);
		List<Map> enforceInfoMapList = enforceInfoMapList(enforceList);
		Map<String, Object> enforceMap = new HashMap<>();
		enforceMap.put("强制执行记录", enforceInfoMapList);
		enforceMap.put("强制执行次数", enforceInfoMapList.size());
		resultMap.put("公共记录", enforceMap);

		return JSON.toJSONString(resultMap);
	}
	public static String getRaw() {
		return raw;
	}
	public static void setRaw(String raw) {
		CreditUtil.raw = raw;
	}

	/**
	 * 相关还款责任信息（担保明细） [担保日期 被担保人 责任人类型 还款责任金额 贷款余额]
	 * 
	 * @param info
	 * @return
	 */
	public static Map<String, Object> getContributoryMap(String info) {
		Map<String, Object> result = new HashMap<>();
		if ("".equals(info)) {
			result.put("担保次数", 0);
			return result;
		}
		String[] guaranStrTemp = info.split("\\.");
		List<String> guaranStrList = new ArrayList<String>();
		if (guaranStrTemp.length <= 0) {
			return null;
		}
		for (int j = 1; j < guaranStrTemp.length; j++) {
			String temp = guaranStrTemp[j].trim();
			if ("".equals(temp) || temp == null) {
				continue;
			}
			guaranStrList.add(temp);
		}

		result.put("担保次数", guaranStrList.size());
		if (guaranStrList.size() > 0) {
			List<Map<String, Object>> contriMapList = new ArrayList<Map<String, Object>>();
			for (String str : guaranStrList) {
				Map<String, Object> strMap = new HashMap<>();
				String guaranDate = matchCNDate(str);
				String guaranEntity = matchStringMidByREG(str, "为", "（证件类型");
				String contriType = matchStringMidByREG(str, "责任人类型为", "，相关还款责任金额");
				String contriMoney = matchStringMidByREG(str.replaceAll(sep, ""), "，相关还款责任金额", "。");// （保证合同编号:B1000XXX）
				String guaranLeft = matchStringMidByREG(str, "余额", "。");
				guaranLeft = guaranLeft.replaceAll(sep, "");
				guaranLeft = guaranLeft.replaceAll("\\s*", "");
				guaranLeft = matchMoney(guaranLeft);
				contriMoney = matchMoney(contriMoney);
				strMap.put("担保日期", guaranDate);
				strMap.put("被担保人", guaranEntity);
				strMap.put("责任人类型", contriType);
				strMap.put("责任金额", contriMoney);
				strMap.put("担保余额", guaranLeft);
				contriMapList.add(strMap);
			}
			result.put("担保人明细", contriMapList);
		} else {
			result.put("担保人明细", "");
		}
		return result;
	}
	/**
	 * 查询机构和个人查询次数
	 * @param info
	 * @return
	 */
	public static Map<String, Object> getQueryNum(String info) {

		Map<String, Object> result = new HashMap<>();
		int allNum = matchInfoNum("月", info);
		int personNum = matchInfoNum("本人查询", info);
		int afterLoanNum = matchInfoNum("贷后管理", info);
		int applyLoanNum = matchInfoNum("贷款审批", info);
		int applyCreditNum = matchInfoNum("信用卡审批", info);
		int contriNum = matchInfoNum("担保资格审查", info);
		result.put("QUERY_ORG_NUM", allNum - personNum);
		result.put("QUERY_LOANS_NUM", afterLoanNum);
		result.put("QUERY_APPLY_NUM", applyLoanNum);
		result.put("QUERY_CREDIT_APPLY_NUM", applyCreditNum);
		result.put("QUERY_CONTRI_NUM", contriNum);
		result.put("QUERY_SELF_NUM", personNum);
		result.put("QUERY_ALL_NUM", allNum);
		
		return result;
	}

	/**
	 * 查询机构查询记录明细
	 * @param info
	 * @return
	 */
	public static List<Map<String, Object>> getDeptQueryList(String info) {
		log.info("开始解析征信机构查询===================start===");
		String deptQuery = matchStringMidByREG(info, "查询原因", "个人查询记录明细");
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		if (deptQuery == null){
			return list;
		}
		String[] split = deptQuery.split(sep);
		for (String s : split) {
			if (s.length() < 1) continue;
			int i = s.indexOf('日');
			if (i == -1) continue;

			Map<String, Object> map = new HashMap<>();
			String dept = s;
			String date = s;
			String reason = s;
			//获取查询机构
			i = s.indexOf('日');
			dept = dept.substring(i + 1).trim();
			int j = dept.indexOf(" ");
			dept = dept.substring(0, j + 1).trim();
			map.put("deptName", dept);

			//获取查询日期
			i = date.indexOf('年');
			date = date.substring(i - 4, i + 7);
			map.put("queryDate", date);

			//获取查询原因
			reason = reason.trim();
			i = reason.lastIndexOf(' ');
			reason = reason.substring(i + 1);
			map.put("queryReason", reason);

			if (!map.isEmpty())
				list.add(map);
		}
		if (list.size() == 0){
			Map<String, Object> map = new HashMap<>();
			map.put("","");
			list.add(map);
		}
		log.info("机构查询解析end====================");
		return list;
	}
	/**
	 * 获取信用卡、贷款相关明细数据,返回用于计分
	 * 
	 * @author David.Z
	 * @param info：信贷记录
	 * @return 信贷记录相关账户张数
	 */
	public static Map<String, Object> getCreditInfo(String info) {
		Map<String, Object> creditRecordMap = new HashMap<>();
 		Map<String, Object> resultMap = new HashMap<>();
		if(matchInfo("系统中没有您的信用卡、贷款和其他信贷记录",info)) {
			resultMap.put("信用概要", matchCreditAccountNum(""));
			resultMap.put("信用卡激活张数", 0);
			resultMap.put("个人消费贷笔数", 0);
			resultMap.put("其他贷款笔数", 0);
			resultMap.put("信用卡明细", "");
			resultMap.put("贷款明细", "");
			resultMap.put("相关还款责任人明细", getContributoryMap(""));
			return resultMap;
		}
		creditRecordMap = matchCreditRecordTitleReg(info);
		//信用卡-信息概要
		String creditAccountStr = creditRecordMap.get("信息概要")==null || 
				"".equals(creditRecordMap.get("信息概要")) ? "" : creditRecordMap.get("信息概要").toString().trim();
		String creditCardInfoStr = creditRecordMap.get("信用卡").toString();
		String loanAccountInfoStr = creditRecordMap.get("贷款").toString();

		String contributoryStr = creditRecordMap.get("相关还款责任信息")==null ||
				"".equals(creditRecordMap.get("相关还款责任信息")) ? "" : creditRecordMap.get("相关还款责任信息").toString().trim();
		
		//信息概要的map获取
		Map creditAccountMap = matchCreditAccountNum(creditAccountStr);
		/*信用卡激活账户数*/
		int activatedCreditAccountNum = matchInfoNum("尚未激活", creditCardInfoStr);//尚未激活信用卡张数
		String activatedCreditNumStr = creditAccountMap.get("信用卡未结清销户账户数").toString().trim();		
		int activatedCreditNum = 0;		//未销户信用卡张数
		if(activatedCreditNumStr == "--" || "--".equals(activatedCreditNumStr) || 
				activatedCreditNumStr == null || "".equals(activatedCreditNumStr)) {
			activatedCreditNum = 0;
		} else {
			activatedCreditNum = Integer.parseInt(activatedCreditNumStr);
		}
		int usedCreditAccountNum = activatedCreditNum - activatedCreditAccountNum;	//激活信用卡张数
		resultMap.put("信用卡激活张数", usedCreditAccountNum);
		resultMap.put("信用概要", creditAccountMap);

		//信用卡明细
		List<Map<String, Object>> creditRecordList = new ArrayList<>();
		creditRecordList = matchCreditCardRecordMap(creditCardInfoStr);
		resultMap.put("信用卡明细", creditRecordList);
		
		//***贷款相关账户数***/
		int consumeLoanNumStr = matchInfoNum("个人消费贷款",loanAccountInfoStr);
		resultMap.put("个人消费贷笔数", consumeLoanNumStr);
		int restLoanNumStr = matchInfoNum("其他贷款",loanAccountInfoStr);
		resultMap.put("其他贷款笔数", restLoanNumStr);
		//贷款明细
		List<Map<String, Object>> loanRecordList = new ArrayList<>();
		loanRecordList = matchLoanRecordMap(loanAccountInfoStr);
		resultMap.put("贷款明细", loanRecordList);
		//相关还款
		Map<String, Object> contributoryPersonMap = getContributoryMap(contributoryStr);
		resultMap.put("相关还款责任人明细", contributoryPersonMap);
//		return JSON.toJSONString(resultMap);
		return resultMap;
	}
	/**
	 * 信用卡明细数据 [ 信用额度 信用卡使用额度 开卡日期 信用卡发放公司 信用卡账户币种]
	 * 
	 * @author David.Z
	 * @param info 信用卡标题下所有文本
	 * @return
	 */
	public static List<Map<String, Object>> matchCreditCardRecordMap(String info) {
	            if (matchInfo("发生过逾期的贷记卡账户明细如下：", info)) {
                info = info.replaceAll("发生过逾期的贷记卡账户明细如下：", "");
		}
		if (matchInfo("从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下：", info)) {
			info = info.replaceAll("从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下：", "");
		}
		if (!matchInfo(".", info)) {
			return null;
		}
		info = normalStr(info);
		List<Map<String, Object>> creditMapList = new ArrayList<>();
		int cancelAccountNum = 0; // 销户数量
		int notActivedAccountNum = 0; // 未激活账户数
		int badDebtNum = 0; // 呆账数量
		// int creditLimitNum = 0; //信用额度超十万账户数
		// 断句

		String[] creditRecordStrTemp = info.split("\\.");
		List<String> creditRecordStrList = new ArrayList<String>();
		if (creditRecordStrTemp.length <= 0) {
			return null;
		}
		for (int j = 1; j < creditRecordStrTemp.length; j++) {
			String temp = creditRecordStrTemp[j].trim();
			if ("".equals(temp) || temp == null) {
				continue;
			}
			creditRecordStrList.add(temp);
		}
		for (int i = 0; i < creditRecordStrList.size(); i++) {
			Map<String, Object> creditMap = new HashMap<>();
			String listStrTemp = creditRecordStrList.get(i);
			if (listStrTemp == null || "".equals(listStrTemp)) {
				continue;
			}
			String creditLimitStr = ""; // 信用额度
			String accountMoneyType = ""; // 账户类型
			String balance = ""; // 余额
			String accountTypeStatus = "";
			// 对于销户或未激活的卡片，不记录明细
			if (matchInfo("销户", listStrTemp)) {
				cancelAccountNum++;
				continue;
			}
			if (matchInfo("尚未激活", listStrTemp)) {
				notActivedAccountNum++;
				continue;
			}

			// 信用卡开卡日期
			String creditDate = matchCNDate(listStrTemp);
			creditMap.put("开卡日期", creditDate);
			// 账户币种类型提取
			accountMoneyType = matchStringMidByREG(listStrTemp, "发放的.*（", "）。截至.*", "账户");
			creditMap.put("卡片尾号", matchItemCardNum(accountMoneyType));
			creditMap.put("信用卡账户币种", matchItemMoneyType(accountMoneyType));
			// 公司名称提取
			String companyName = "";
			companyName = listStrTemp.split("。")[0];
			companyName = matchStringMidByREG(companyName, "日", "发放的");
			creditMap.put("信用卡发放公司", companyName);

			if (matchInfo("呆账", listStrTemp)) {
				accountTypeStatus = "呆账";
				badDebtNum++;
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，已变成呆账");
				balance = matchStringMidByREG(listStrTemp, "余额", "。");
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("余额", balance);
				creditMap.put("已使用额度", balance);
				creditMap.put("账户状态", accountTypeStatus);
			} else if (matchInfo("没有发生过90天以上逾期", listStrTemp)) {
				accountTypeStatus = "没有发生过90天以上逾期";
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，余额");
				if (matchInfo("当前无逾期", listStrTemp) || matchInfo("当前有逾期", listStrTemp)) {
					balance = matchStringMidByREG(listStrTemp, "余额", "，当前");
				} else {
					balance = matchStringMidByREG(listStrTemp, "余额", "。最近");
				}
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("余额", balance);
				creditMap.put("已使用额度", balance);
				creditMap.put("账户状态", accountTypeStatus);
			} else if (matchInfo("逾期超过90天", listStrTemp)) {
				accountTypeStatus = "逾期超过90天";
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，余额");
				if (matchInfo("当前无逾期", listStrTemp) || matchInfo("当前有逾期", listStrTemp)) {
					balance = matchStringMidByREG(listStrTemp, "余额", "，当前");
				} else {
					balance = matchStringMidByREG(listStrTemp, "余额", "。最近");
				}
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("余额", balance);
				creditMap.put("已使用额度", balance);
				creditMap.put("账户状态", accountTypeStatus);
			} else if (matchInfo("已使用额度", listStrTemp)) {
				accountTypeStatus = "从未逾期过的贷记卡";
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，已使用额度");
				balance = matchStringMidByREG(listStrTemp, "已使用额度", "[。*]");
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("已使用额度", balance);
				creditMap.put("账户状态", accountTypeStatus);
			} else if (matchInfo("透支余额", listStrTemp)) {
				accountTypeStatus = "透支未超过60天的准贷记卡";
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，透支余额");
				balance = matchStringMidByREG(listStrTemp, "透支余额", "。");
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("透支余额", balance);
				creditMap.put("账户状态", accountTypeStatus);
			} else{
				accountTypeStatus = "";
				creditLimitStr = matchStringMidByREG(listStrTemp, "信用额度", "，余额");
				balance = matchStringMidByREG(listStrTemp, "余额", "。");
				balance = matchMoney(balance);
				creditMap.put("信用额度", creditLimitStr);
				creditMap.put("余额", balance);
				creditMap.put("已使用额度", balance);
				creditMap.put("账户状态", accountTypeStatus);
			}
			creditMapList.add(creditMap);
		}
		return creditMapList;
	}

	/**
	 * 贷款明细数据 [ 贷款发放日期 贷款发放机构 贷款余额 贷款类型 贷款状态 贷款账户币种 贷款金额 贷款结清日期]
	 * 
	 * @param info
	 * @return
	 */
	public static List<Map<String, Object>> matchLoanRecordMap(String info) {
		if (matchInfo("从未发生过逾期的账户明细如下：", info)) {
			info = info.replaceAll("从未发生过逾期的账户明细如下：", "");
		}
		if (matchInfo("发生过逾期的账户明细如下：", info)) {
			info = info.replaceAll("发生过逾期的账户明细如下：", "");
		}
		if (!matchInfo(".", info)) {
			return null;
		}
		info = normalStr(info);
		List<Map<String, Object>> loanMapList = new ArrayList<>();
		String[] loanRecordStrTemp = info.split("\\.");
		List<String> loanRecordStrList = new ArrayList<String>();
		if (loanRecordStrTemp.length <= 0) {
			return null;
		}
		for (int j = 1; j < loanRecordStrTemp.length; j++) {
			String temp = loanRecordStrTemp[j].trim();
			if (temp == null || "".equals(temp)) {
				continue;
			}
			// temp = temp.substring(0, temp.length() - 1);
			// 1行的时候会去掉关键句号，故注释
			loanRecordStrList.add(temp);
		}
		// 添加规则，正则解析获得每行的明细
		for (int i = 0; i < loanRecordStrList.size(); i++) {
			Map<String, Object> loanMap = new HashMap<>();
			String listStrTemp = loanRecordStrList.get(i);
			if (listStrTemp == null || "".equals(listStrTemp)) {
				loanRecordStrList.remove(i);
				continue;
			}
			// 贷款发放日期
			String loanDate = matchCNDate(listStrTemp);
			loanMap.put("贷款发放日期", loanDate);
			// 账户币种类型提取
			String accountMoneyType = matchStringMidByREG(listStrTemp, "发放的.*（", "）");
			loanMap.put("贷款账户币种", accountMoneyType);
			// 公司名称提取
			String companyName = "";
			companyName = listStrTemp.split("。")[0];
			companyName = matchStringMidByREG(listStrTemp, "日", "发放的");
			loanMap.put("贷款发放机构", companyName);
			// 贷款相关
			String loanMoney = matchStringMidByREG(listStrTemp, "发放的", "元");
			if (loanMoney == null || "".equals(loanMoney)) {
				loanMap.put("贷款金额", loanMoney);
			} else {
				loanMap.put("贷款金额", loanMoney);
			}
			String loanType = matchStringMidByREG(listStrTemp, "发放的.*）", "贷款");
			loanMap.put("贷款类型", loanType);
			String balance = "";
			if (matchInfo("已结清", listStrTemp)) {
				loanMap.put("贷款状态", "已结清");
				loanMap.put("贷款结清日期", matchStringMidByREG(listStrTemp, "，", "已结清"));
			} else if (matchInfo("余额", listStrTemp)) {
				loanMap.put("贷款状态", "未结清");
				balance = matchStringMidByREG(listStrTemp, "余额[为]*", "。");
				loanMap.put("贷款余额", matchMoney(balance));
			}
			loanMapList.add(loanMap);
		}
		return loanMapList;
	}
	/**
	 * 利用规则，全文分割一级标题
	 * 
	 * @param info
	 * @return
	 */
	public static Map<String, Object> matchTopicInfoReg(String info) {
		// log.info("当前平台换行符" + File.separator);
		Map<String, Object> topicInfoMap = new HashMap<>();
		String topicInfo[] = null;
		StringBuffer sb = new StringBuffer();
		
		sb.append("个人信用报告" + sep + "|");
		sb.append("信贷记录" + sep + "|");
		sb.append("非信贷交易记录" + sep + "|");
		sb.append("公共记录" + sep + "|");
		sb.append("查询记录" + sep + "|");
		sb.append("说明" + sep);
		// log.info(StringEscapeUtils.escapeJava(sb.toString()));
		topicInfo = info.split(sb.toString());
		log.info("切分后长度" + topicInfo.length);
		// log.info(StringEscapeUtils.escapeJava(info));
		topicInfoMap.put("个人信息", topicInfo[1]);
		topicInfoMap.put("信贷记录", topicInfo[2]);
		topicInfoMap.put("非信贷交易记录", topicInfo[3]);
		topicInfoMap.put("公共记录", topicInfo[4]);
		topicInfoMap.put("查询记录", topicInfo[5]);
		return topicInfoMap;
	}
	/** 个人信息正则匹配计算
	 * @return
	 */
	public static Map<String, Object> matchPersonInfo(String str) {
		Map<String, Object> infoMap = new HashMap<>();
		str = normalStr(str);
		//报告编号：
		String infoNo = matchStringMidByREG(str,"报告编号：","报告时间");
		infoMap.put("报告编号", infoNo);
		//报告时间：
		String infoTime = matchStringMidByREG(str,"报告时间：","姓名");
		infoTime = infoTime.substring(0,10)+ " " + infoTime.substring(10);
		infoMap.put("报告时间", infoTime);
		//姓名：
		String name = matchStringMidByREG(str,"姓名：","证件类型");
		infoMap.put("姓名", name);
		//证件类型：
		String idType = matchStringMidByREG(str,"证件类型：","证件号码");
		infoMap.put("证件类型", idType);
		//证件号码：
		String idNum = "";
		String marryInfo = "";
		String marryInfos[] = {"已婚", "未婚", "--", "单身", "离婚"};
		for (String c: marryInfos){
			if(matchInfo(c, str)) {
				idNum = matchStringMidByREG(str,"证件号码：", c);
				marryInfo = c;
			}
		}
		infoMap.put("证件号", idNum);
		infoMap.put("婚姻状况", marryInfo);
		return infoMap;
	}
	/**
	 * 信息概要正则匹配计算
	 * @return
	 */
	public static Map matchCreditAccountNum(String str) {
		Map creditInfoMap = new HashMap();
		if (str == null) {
			str = "";
		}
		Pattern p = Pattern.compile("\\s+"); 
		Matcher m = p.matcher(str); 
		String s= m.replaceAll(" "); 
		//信用卡账户：
		String s1 = "购房 其他 账户数";
		String s2 = "购房贷款";
		String creditAccount01 = matchStringMidByREG(s,s1,s2);
		String[] creditAccountNum01;
		if (creditAccount01 == null || "".equals(creditAccount01)) {
			creditAccountNum01 = new String[] {"0","0","0","0"};
		} else {
			creditAccount01 = creditAccount01.trim();
			creditAccount01 = creditAccount01.replaceAll("\\s", ",");
			creditAccountNum01= creditAccount01.split(",");
			for(int i=0; i<creditAccountNum01.length; i++) {
				if (creditAccountNum01[i].contains("--")) {
					creditAccountNum01[i] = "0";
				}
			}
		}
		creditInfoMap.put("信用卡账户数", creditAccountNum01[0]);
		creditInfoMap.put("购房贷款账户数", creditAccountNum01[1]);
		creditInfoMap.put("其他贷款账户数", creditAccountNum01[2]);
		creditInfoMap.put("其他业务账户数", creditAccountNum01[3]);
		//未结清、未销户账户：
		String s3 = "未销户账户数";
		String s4 = "金贷款";
		String creditAccount02 = matchStringMidByREG(s,s3,s4);
		String[] creditAccountNum02;
		if (creditAccount02 == null || "".equals(creditAccount02)) {
			creditAccountNum02 = new String[] {"0","0","0","0"};
		} else {
			creditAccount02 = creditAccount02.trim();
			creditAccount02 = creditAccount02.replaceAll("\\s", ",");
			creditAccountNum02 = creditAccount02.split(",");
			for(int i=0; i<creditAccountNum02.length; i++) {
				if (creditAccountNum02[i].contains("--")) {
					creditAccountNum02[i] = "0";
				}
			}
		}
		creditInfoMap.put("信用卡未结清销户账户数", creditAccountNum02[0]);
		creditInfoMap.put("购房贷款未结清销户账户数", creditAccountNum02[1]);
		creditInfoMap.put("其他贷款未结清销户账户数", creditAccountNum02[2]);
		creditInfoMap.put("其他业务未结清销户账户数", creditAccountNum02[3]);
		//发生过逾期的账户：
		String s5 = "发生过逾期的账户数";
		String s6 = "发生过逾期的信用卡账户";
		String creditAccount03 = matchStringMidByREG(s,s5,s6);
		String[] creditAccountNum03;
		if (creditAccount03 == null || "".equals(creditAccount03)) {
			creditAccountNum03 = new String[] {"0","0","0","0"};
		} else {
			creditAccount03 = creditAccount03.trim();
			creditAccount03 = creditAccount03.replaceAll("\\s", ",");
			creditAccountNum03 = creditAccount03.split(",");
			for(int i=0; i<creditAccountNum03.length; i++) {
				if (creditAccountNum03[i].contains("--")) {
					creditAccountNum03[i] = "0";
				}
			}
		}
		creditInfoMap.put("信用卡发生过逾期的账户数", creditAccountNum03[0]);
		creditInfoMap.put("购房贷款发生过逾期的账户数", creditAccountNum03[1]);
		creditInfoMap.put("其他贷款发生过逾期的账户数", creditAccountNum03[2]);
		creditInfoMap.put("其他业务发生过逾期的账户数", creditAccountNum03[3]);
		//发生过90天以上逾期的账户数：
		String s7 = "发生过90天以上逾期的账户数";
		String s8 = "超过60天";
		String creditAccount04 = matchStringMidByREG(s,s7,s8);
		String[] creditAccountNum04;
		if (creditAccount04 == null || "".equals(creditAccount04)) {
			creditAccountNum04 = new String[] {"0","0","0","0"};
		} else {
			creditAccount04 = creditAccount04.trim();
			creditAccount04 = creditAccount04.replaceAll("\\s", ",");
			creditAccountNum04 = creditAccount04.split(",");
			for(int i=0; i<creditAccountNum04.length; i++) {
				if (creditAccountNum04[i].contains("--")) {
					creditAccountNum04[i] = "0";
				}
			}
		}
		creditInfoMap.put("信用卡发生过90天以上逾期的账户数", creditAccountNum04[0]);
		creditInfoMap.put("购房贷款发生过90天以上逾期的账户数", creditAccountNum04[1]);
		creditInfoMap.put("其他贷款发生过90天以上逾期的账户数", creditAccountNum04[2]);
		creditInfoMap.put("其他业务发生过90天以上逾期的账户数", creditAccountNum04[3]);
		//相关还款责任账户
		String creditAccount05 = matchStringMidByREG(s,"相关还款责任账户数","$");
		if (creditAccount05 == null || "".equals(creditAccount05)) {
			creditInfoMap.put("相关还款责任账户数为个人", "0");
			creditInfoMap.put("相关还款责任账户数为企业", "0");
		} else {
			creditAccount05 = creditAccount05.trim().replaceAll("\\s", ",");
			String creditAccountNum05[] = creditAccount05.split("\\,");
			for(int i=0; i<creditAccountNum05.length; i++) {
				if (creditAccountNum05[i].contains("--")) {
					creditAccountNum05[i] = "0";
				}
			}
			creditInfoMap.put("相关还款责任账户数为个人", creditAccountNum05[0]);
			creditInfoMap.put("相关还款责任账户数为企业", creditAccountNum05[1]);
		}
		return creditInfoMap;
	}
	/**
	 * 利用规则，信贷记录二级标题及文本明细
	 * @param info
	 * @return
	 */
	public static Map matchCreditRecordTitleReg(String info) {
		Map titleInfoMap = new HashMap();
		if( !matchInfo("信息概要", info) ){
			return null;
		}
		String infoAbsEnd = "的准贷记卡账户。";
		if (matchInfo("发生过逾期的贷记卡账户明细如下", info)) {
			infoAbsEnd = "发生过逾期的贷记卡账户明细如下";
		} else if (matchInfo("从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下", info)) {
			infoAbsEnd = "从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下";
		}
		String titleInfo1 = matchStringMidByREG(info, "信息概要", infoAbsEnd);
		titleInfoMap.put("信息概要", titleInfo1);
		Map titleInfo1Map = matchCreditAccountNum(titleInfo1);
		//信用卡账户数", "购房贷款账户数", "其他贷款账户数", "相关还款责任账户数"
		int creditNum = Integer.parseInt(titleInfo1Map.get("信用卡账户数").toString());
		int loanNum = Integer.parseInt(titleInfo1Map.get("购房贷款账户数").toString()) + 
				Integer.parseInt(titleInfo1Map.get("其他贷款账户数").toString()) ;
		int contriNum = Integer.parseInt(titleInfo1Map.get("相关还款责任账户数为个人").toString())
				+ Integer.parseInt(titleInfo1Map.get("相关还款责任账户数为企业").toString());
		StringBuffer sb = new StringBuffer();
		String spliStr = "";
		String titleInfo[] = new String[10];
		titleInfoMap.put("信用卡", "");
		titleInfoMap.put("贷款", "");
		titleInfoMap.put("相关还款责任信息", "");
		titleInfoMap.put("信用卡明细", "");
		titleInfoMap.put("贷款明细", "");
		if (creditNum > 0 && loanNum > 0 && contriNum > 0) {
			sb.append("信用卡"+ sep + "|");
			sb.append("贷款"+ sep + "|");
			sb.append("相关还款责任信息");
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr,4);
			titleInfoMap.put("信用卡", titleInfo[1]);
			titleInfoMap.put("贷款", titleInfo[2]);
			if (titleInfo.length > 3){
				titleInfoMap.put("相关还款责任信息", titleInfo[3]);
			}
			titleInfoMap.put("信用卡明细", matchCreditCardTitleInfoReg(titleInfo[1]));
			titleInfoMap.put("贷款明细", matchLoanTitleInfoReg(titleInfo[2]));
		}
		if (creditNum > 0 && loanNum > 0 && contriNum <= 0) {
			sb.append("信用卡"+ sep +"|");
			sb.append("贷款" + sep);
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("信用卡", titleInfo[1]);
			titleInfoMap.put("贷款", titleInfo[2]);
			titleInfoMap.put("信用卡明细", matchCreditCardTitleInfoReg(titleInfo[1]));
			titleInfoMap.put("贷款明细", matchLoanTitleInfoReg(titleInfo[2]));
		}
		if (creditNum <= 0 && loanNum > 0 && contriNum > 0) {
			sb.append("贷款" + sep + "|");
			sb.append("相关还款责任信息");
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("贷款", titleInfo[1]);
			titleInfoMap.put("相关还款责任信息", titleInfo[2]);
			titleInfoMap.put("贷款明细", matchLoanTitleInfoReg(titleInfo[1]));
		}
		if (creditNum > 0 && loanNum <= 0 && contriNum > 0) {
			sb.append("信用卡" + sep + "|");
			sb.append("相关还款责任信息");
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("信用卡", titleInfo[1]);
			titleInfoMap.put("相关还款责任信息", titleInfo[2]);
			titleInfoMap.put("信用卡明细", matchCreditCardTitleInfoReg(titleInfo[1]));
		}
		if (creditNum > 0 && loanNum <= 0 && contriNum <= 0) {
			sb.append("信用卡" + sep);
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("信用卡", titleInfo[1]);
			titleInfoMap.put("信用卡明细", matchCreditCardTitleInfoReg(titleInfo[1]));
		}
		if (creditNum <= 0 && loanNum > 0 && contriNum <= 0) {
			sb.append("贷款" + sep);
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("贷款", titleInfo[1]);
			titleInfoMap.put("贷款明细", matchLoanTitleInfoReg(titleInfo[1]));
		}
		if (creditNum <= 0 && loanNum <= 0 && contriNum > 0) {
			sb.append("相关还款责任信息");
			spliStr = sb.toString();
			String otherInfo = info.split("的准贷记卡账户。")[1];
			titleInfo = otherInfo.split(spliStr);
			titleInfoMap.put("相关还款责任信息", titleInfo[1]);
		}
		return titleInfoMap;
	}

	/**
	 * @author David.Z
	 *  信用卡下级分段
	 * @param info
	 * @return
	 */
	public static Map matchCreditCardTitleInfoReg(String info) {
		Map  cardTitleInfo = new HashMap();
		String cardOverdueStr = "发生过逾期的贷记卡账户明细如下：";			//信用卡逾期str
		String cardOverdue60Str = "从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下：";		//信用卡逾期未超60天str
		String cardOverdueInfo = "";
		String cardOverdue60Info ="";
		if (info == null || "".equals(info)) {
			return null;
		}
		if (matchInfo(cardOverdueStr, info)) {
			if(matchInfo(cardOverdue60Str, info)) {
				//信用卡发生过逾期+未逾期及未超60天
				cardOverdueInfo = matchStringMidByREG(info, cardOverdueStr, cardOverdue60Str);
				cardOverdue60Info =  matchStringMidByREG(info, cardOverdue60Str, "$");
			} else {
				//信用卡发生过逾期
				cardOverdueInfo = matchStringMidByREG(info, cardOverdueStr, "$");
			}
		} else {
			if(matchInfo(cardOverdue60Str, info)) {
				//信用卡未发生逾期+未逾期及超60天
				cardOverdue60Info =  matchStringMidByREG(info, cardOverdue60Str, "$");
			} 
		}
		cardTitleInfo.put("发生过逾期的贷记卡账户明细如下", cardOverdueInfo);
		cardTitleInfo.put("从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下", cardOverdue60Info);
		return cardTitleInfo;
	}
	/**
	 * @author David.Z
	 *   贷款信息分段
	 * @param info
	 * @return
	 */
	
	public static Map matchLoanTitleInfoReg(String info) {
		Map  loanTitleInfo = new HashMap();
		String loanOverdueStr = "发生过逾期的账户明细如下：";			//贷款逾期str
		String loanOverdue60Str = "从未发生过逾期的账户明细如下：";		//贷款逾期未超60天str
		String loanOverdueInfo = "";
		String loanOverdue60Info = "";
		if (info == null || "".equals(info)) {
			return null;
		}
		int b = 0;
		Pattern p = Pattern.compile(loanOverdueStr);
        Matcher m = p.matcher(info);
        if(m.find()) {
        	b++;
        } 
		if(b == 2) {
			if (matchInfo(loanOverdueStr, info)) {
				if(matchInfo(loanOverdue60Str, info)) {
					//贷款发生过逾期+未逾期及未超60天
					loanOverdueInfo = matchStringMidByREG(info, loanOverdueStr, loanOverdue60Str);
					//loanOverdueInfo = loanOverdueInfo.substring(loanOverdueStr.length(), 
					//		loanOverdueInfo.length() - loanOverdue60Str.length()).trim();
					loanOverdue60Info =  matchStringMidByREG(info, loanOverdue60Str, "$");
					//loanOverdue60Info = loanOverdue60Info.substring(loanOverdue60Str.length()).trim();
				} else {
					//贷款发生过逾期
					loanOverdueInfo = matchStringMidByREG(info, loanOverdueStr, "$");
					//loanOverdueInfo = loanOverdueInfo.substring(loanOverdueStr.length()).trim();
				}
			} else {
				if(matchInfo(loanOverdue60Str, info)) {
					//贷款未发生逾期+未逾期及超60天
					loanOverdue60Info =  matchStringMidByREG(info, loanOverdue60Str, "$");
					//loanOverdue60Info = loanOverdue60Info.substring(loanOverdue60Str.length()).trim();
				} 
		}
		} else if(b == 1 && matchInfo(loanOverdue60Str, info)){
			loanOverdue60Info =  matchStringMidByREG(info, loanOverdue60Str, "$");
			//loanOverdue60Info = loanOverdue60Info.substring(loanOverdue60Str.length()).trim();
		} else if (b == 1 && matchInfo(loanOverdueStr, info)) {
			loanOverdueInfo = matchStringMidByREG(info, loanOverdueStr, "$");
			//loanOverdueInfo = loanOverdueInfo.substring(loanOverdueStr.length()).trim();
		}
		loanTitleInfo.put("发生过逾期的账户明细如下", loanOverdueInfo);
		loanTitleInfo.put("从未发生过逾期的账户明细如下", loanOverdue60Info);
		return loanTitleInfo;
	}
	/**
	 * @author David.Z
	 * @description 查询记录信息
	 * @param info
	 * @return
	 */
	public static Map queryRecordInfo(String info) {
		Map queryRecordMap = new HashMap();
		String instQueryStr = "机构查询记录明细";
		String personQueryStr = "个人查询记录明细";
		String instQueryInfo = "";
		String personQueryInfo ="";
		if(matchInfo(instQueryStr, info)) {
			if(matchInfo(personQueryStr, info)) {
				//机构查询记录+个人查询记录
				instQueryInfo = matchStringMidByREG(info, instQueryStr, personQueryStr);
				//instQueryInfo.substring(instQueryStr.length(), instQueryInfo.length() - personQueryStr.length()).trim();
				personQueryInfo = matchStringMidByREG(info, personQueryStr, "$");
				//personQueryInfo.substring(personQueryStr.length()).trim();
			} else {
				//机构查询记录
				instQueryInfo = matchStringMidByREG(info, instQueryStr, "$");
				//instQueryInfo.substring(instQueryStr.length()).trim();
			} 
		} else {
			if(matchInfo(personQueryStr, info)) {
				//个人查询记录
				personQueryInfo = matchStringMidByREG(info, personQueryStr, "$");
				//personQueryInfo.substring(personQueryStr.length()).trim();
			} 
		}
		queryRecordMap.put("机构查询记录明细", instQueryInfo);
		queryRecordMap.put("个人查询记录明细", personQueryInfo);
		return queryRecordMap;
	}
	/**
	 * @author David.Z
	 *   强制执行记录list匹配
	 * @param enforceRecordList
	 * @return
	 */
	public static List<Map> enforceInfoMapList(List<String> enforceRecordList) {
		List<Map> enforceInfoList = new ArrayList<Map>();
		if (enforceRecordList.size() > 0) {
			for (int i=0; i<enforceRecordList.size(); i++) {
				enforceInfoList.add(enforceInfoMap(enforceRecordList.get(i)));
			}
		}
		return enforceInfoList;
	}
	/**
	 * 强制执行信息List
	 * @param info
	 * @return
	 */
	public static List<String> enforceRecordList(String info) {
		List<String> list = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		info = replaceSpace(info);
		if (matchInfo("强制执行记录", info)) {
			sb.append("强制执行记录|");
		}
		sb.append("执行法院");
		String str = sb.toString();
		String enforceStr[] = null;
		enforceStr = info.split(str);
		if(enforceStr.length >1) {
			for (int i=0; i<enforceStr.length; i++) {
				if(matchInfo("案号", enforceStr[i])) {
					String s = "执行法院" + enforceStr[i];
					s = replaceSpace(s);
					list.add(s);
				}
			}
		}
		return list;
	}
	
	/**
	 * @author David.Z
	 *   强制执行信息匹配
	 * @param info
	 * @return
	 */
	public static Map enforceInfoMap(String info) {
		Map<String, String> enforceInfoMap = new HashMap<String, String>();
		info = normalStr(info);
		String court = matchStringMidByREG(info, "执行法院：", "案号").trim();
		//court = court.substring("执行法院：".length(), court.length() - "案号".length()).trim();
		enforceInfoMap.put("执行法院", court);
		String caseNum = matchStringMidByREG(info, "案号：", "执行案由").trim();
		//caseNum = caseNum.substring("案号：".length(), caseNum.length() - "执行案由".length()).trim();
		enforceInfoMap.put("案号", caseNum);
		String enforceReason = matchStringMidByREG(info, "执行案由：", "结案方式").trim();
		//enforceReason = enforceReason.substring("执行案由：".length(), enforceReason.length() - "结案方式".length()).trim();
		enforceInfoMap.put("执行案由", enforceReason);
		String closeCaseMethod = matchStringMidByREG(info, "结案方式：", "立案日期").trim();
		//closeCaseMethod = closeCaseMethod.substring("结案方式：".length(), closeCaseMethod.length() - "立案日期".length()).trim();
		enforceInfoMap.put("结案方式", closeCaseMethod);
		String caseDate = matchStringMidByREG(info, "立案日期：", "案件状态").trim();
		//caseDate = caseDate.substring("立案日期：".length(), caseDate.length() - "案件状态".length()).trim();
		enforceInfoMap.put("立案日期", caseDate);
		String caseState = matchStringMidByREG(info, "案件状态：", "申请执行标的：").trim();
		//caseState = caseState.substring("案件状态：".length(), caseState.length() - "申请执行标的：".length()).trim();
		enforceInfoMap.put("案件状态", caseState);
		String applyEnforcement = matchStringMidByREG(info, "申请执行标的：", "已执行标的：").trim();
		//applyEnforcement = applyEnforcement.substring("申请执行标的：".length(), applyEnforcement.length() - "已执行标的：".length()).trim();
		enforceInfoMap.put("申请执行标的", applyEnforcement);
		String enforcement = matchStringMidByREG(info, "已执行标的：", "申请执行标的金额").trim();
		//enforcement = enforcement.substring("已执行标的：".length(), enforcement.length() - "申请执行标的金额".length()).trim();
		enforceInfoMap.put("已执行标的", enforcement);
		String applyEnforcementFee = matchStringMidByREG(info, "申请执行标的金额：", "已执行标的金额").trim();
		//applyEnforcementFee = applyEnforcementFee.substring("申请执行标的金额：".length(), applyEnforcementFee.length() - "已执行标的金额".length()).trim();
		enforceInfoMap.put("申请执行标的金额", applyEnforcementFee);
		String enforcementFee = matchStringMidByREG(info, "已执行标的金额：", "结案日期").trim();
		//enforcementFee = enforcementFee.substring("已执行标的金额：".length(), enforcementFee.length() - "结案日期".length()).trim();
		enforceInfoMap.put("已执行标的金额", enforcementFee);
		String caseCloseDate = matchStringMidByREG(info, "结案日期：","$").trim();
		//caseCloseDate = caseCloseDate.substring("结案日期：".length()).trim();
		enforceInfoMap.put("结案日期", caseCloseDate);
		return enforceInfoMap;
	}
	/*-------------------公共方法-------------------------*/
	/**
	 * 归一化（去空）
	 * @param str
	 * @return
	 */
	public static String normalStr(String str) {
		Pattern p1 = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m1 = p1.matcher(str);
		str = m1.replaceAll("");
		return str;
	}
	public static String replaceXStr(String str, String info) {
		String result = "";
		if(matchInfo(str, info)) {
			 result = info.replaceAll(str, "");
		}
		return result;
	}

	/**
	 * 去空及去换行
	 * 
	 * @param input
	 */
	public static String replaceSpace(String input) {
		if (input == null){
			return "";
		}
		input = input.replaceAll("(?m)^\\s*$(\n|\n)", "");
		input = input.replaceAll("(?m)(?:^\\s*$(?:\n|\n))", "");
		input.replaceAll("\\s{2,}", " ");
		return input;
	}
	/**
	 * 预处理，数据归一化，根据正则匹配是否含有某标题，返回标题数据数组
	 * @param info	
	 * @param splitStr
	 * @param str
	 * @return
	 */
	public static String matchSplitStr(String info, String splitStr, String str) {
		Pattern p = Pattern.compile( str );
        Matcher m = p.matcher(info);
        if(m.find()) {
        	splitStr = splitStr + "|" + str;
        } 
        return splitStr;
	}

	/**
	 * 正则匹配，含中文、英文、数字及特殊字符，根据pos实现灵活匹配
	 * 
	 * @param parent 整段文字
	 * @param child1 匹配句前
	 * @param child2 匹配句后
	 * @return
	 */
	public static List<String> matchStringByREG(String parent, String child1, String child2, Integer pos) {
		String reg = "[\\u4e00-\\u9fa5_a-zA-Z0-9 @#$%^&*||()-。,，：:（）.\\t\r\\s\n\\x22]+";
		String child = child1 + reg + child2;
		Pattern p = Pattern.compile(child);
		Matcher m = p.matcher(parent);
		List<String> alist = new ArrayList<>();
		while (m.find()) {
			alist.add(m.group(pos));
		}

		return alist;
	}	
	
	
	/**
	 * 正则尽可能少匹配，含中文、英文、数字及特殊字符
	 * 
	 * @param parent 整段文字
	 * @param child1 匹配句前
	 * @param child2 匹配句后
	 * @return
	 */
	public static List<String> matchStringLazyByREG(String parent, String child1, String child2) {
		String reg = "([\\u4e00-\\u9fa5_a-zA-Z0-9 @#$%^&*||()-。,，：:（）.\\t\r\\s\n\\x22]+?)";
		String child = child1 + reg + child2;
		Pattern p = Pattern.compile(child);
		Matcher m = p.matcher(parent);
		List<String> alist = new ArrayList<>();
		while (m.find()) {
			alist.add(m.group(1));
		}
		return alist;
	}

	/**
	 * 返回匹配文本中间字段，去前后str
	 * 
	 * @param info       整段
	 * @param str1       前str
	 * @param str2       后str
	 * @param containStr 包含的字符串
	 * @return
	 */
	public static String matchStringMidByREG(String info, String str1, String str2, String containStr) {
		List<String> resList = new ArrayList<String>();
		resList = matchStringLazyByREG(info, str1, str2);
		if (resList.size() <= 0) {
			return null;
		}
		if (containStr != null)
			for (String resStr : resList) {
				if (resStr.contains(containStr))
					return resStr;
			}
		return resList.get(0);
	}
	
	/**
	 * 返回匹配文本中间字段，去前后str
	 * @param info 整段
	 * @param str1 前str
	 * @param str2 后str
	 * @return
	 */
	public static String matchStringMidByREG(String info, String str1, String str2) {
		return matchStringMidByREG(info, str1, str2, "");
	}
	/**
	 * 判断info里是否有str
	 * @param str: 匹配字段; info：整段
	 * @return
	 */
	public static boolean matchInfo(String str, String info) {
		boolean bool = false;
		Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(info);
        if(m.find()) {
        	bool = true;
        } 
		return bool;
	}
	/**
	 * 整段info里出现str的次数
	 * @param str: 匹配字段; info：整段
	 * @return
	 */
	public static int matchInfoNum(String str, String info) {
		int i = 0;
		Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(info);
        while( m.find() )
        {
        	i++;
        }
		return i;
	}

	/**
	 * 匹配开卡日期 '2021年11月23日'
	 * 增加判空逻辑
	 * @param str 信用卡明细中的一行文本
	 * @return str
	 * 
	 */
	public static String matchCNDate(String str){
		if (str == null) {
			return "";
		}
		String pattern = "^\\d{4}年\\d{1,2}月\\d{1,2}日";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find()){
			return m.group();
		} else {
			return null;
		}
	}
	/**
	 * 匹配担保明细 贷款余额 999,000
	 * @param str 担保明细中的文本
	 * @return str
	 * 
	 */
	public static String matchMoney(String str){
		if (str == null) {
			return "";
		}
		String pattern = "(([0-9]+|[0-9]{1,3})(,[0-9]{3})*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find()){
			return m.group();
		} else {
			return null;
		}
	}
	
	/**
	 * 匹配账户币种 “人民币账户，卡片尾号：5737”
	 * @param itemMoneyType
	 * @return
	 */
	public static String matchItemMoneyType(String itemMoneyType) {
		if (itemMoneyType == null) {
			return "";
		}
		String pattern = "^[\\u4e00-\\u9fa5]{0,}";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(itemMoneyType);
		if (m.find()){
			return m.group();
		} else {
			return null;
		}
	}
	/**
	 * 匹配卡片尾号 “人民币账户，卡片尾号：5737”
	 * @param itemCardNum
	 * @return
	 */
	public static String matchItemCardNum(String itemCardNum) {
		if (itemCardNum == null) {
			return "";
		}
		String pattern = "\\d{4}";
		Pattern r = Pattern.compile(pattern);
 		Matcher m = r.matcher(itemCardNum);
		if (m.find()){
			return m.group();
		} else {
			return "";
		}
	}
	
	/**
	 * “信用额度A，余额B” 余额 等于 已使用额度
	 * @deprecated
	 * @param balance
	 */
	public static String calItemUseAmt(String creditLimitStr, String balance) {
		Integer a = creditLimitStr != null ? Integer.parseInt(creditLimitStr.replaceAll(",", "")) : 0;
		Integer b = creditLimitStr != null ? Integer.parseInt(balance.replaceAll(",", "")) : 0;
		Integer itemUserAmt = a - b > 0 ? a - b : 0;
		return itemUserAmt.toString();
	}
	/**
	 * 带业务逻辑的方法（见注释） 通过JSONPath调整字段
	 * 
	 * @param result
	 * @return
	 */
	public static String ruleTransform(String result) {
		JSONObject twk = JSON.parseObject(result);
		Integer ccNonRmb = 0;
		Integer ccRmb = 0;
		String ccWjqNumStr = (String) JSONPath.eval(twk, "$.信贷记录.信用概要.信用卡未结清销户账户数");
		// 1. 存在信用卡明细时 未结清的账户数量应该减去非人民币账户
		if (ccWjqNumStr != null && !"0".equals(ccWjqNumStr)) {
			Integer ccWjqNum = Integer.parseInt(ccWjqNumStr);
			log.info("原有信用卡未结清销户账户数: " + ccWjqNum);
			if (JSONPath.eval(twk, "$.信贷记录.信用卡明细") instanceof JSONArray) {
				Integer ccNum = JSONPath.size(twk, "$.信贷记录.信用卡明细");
				ccNonRmb = JSONPath.size(twk, "$.信贷记录.信用卡明细[信用卡账户币种!='人民币账户']");
				ccRmb = JSONPath.size(twk, "$.信贷记录.信用卡明细[信用卡账户币种='人民币账户']");
			}
			Integer ccModNum = (ccWjqNum - ccNonRmb) > 0 ? (ccWjqNum - ccNonRmb) : 0;
			JSONPath.set(twk, "$.信贷记录.信用概要.信用卡未结清销户账户数", ccModNum);
			JSONPath.set(twk, "$.信贷记录.信用卡激活张数", ccRmb);
		}

		// 2.1 增加 “个人消费贷 未结清”字段
		// 2.2 增加 “个人汽车消费贷款 ”字段
		// 2.3 增加 “个人汽车消费贷款 未结清”字段
		Integer conLoanWjq = 0;
		Integer carLoanWjq = 0;
		Integer carLoanNum = 0;
		if (JSONPath.eval(twk, "$.信贷记录.贷款明细") instanceof JSONArray) {
			conLoanWjq = JSONPath.size(twk, "$.信贷记录.贷款明细[贷款状态='未结清'][贷款类型='其他个人消费']");
			carLoanWjq = JSONPath.size(twk, "$.信贷记录.贷款明细[贷款状态='未结清'][贷款类型='个人汽车消费']");
			carLoanNum = JSONPath.size(twk, "$.信贷记录.贷款明细[贷款类型='个人汽车消费']");
		}
		JSONPath.set(twk, "$.信贷记录.信用概要.个人消费贷款未结清销户账户数", conLoanWjq);
		JSONPath.set(twk, "$.信贷记录.信用概要.个人汽车贷款未结清销户账户数", carLoanWjq);
		JSONPath.set(twk, "$.信贷记录.信用概要.个人汽车贷款笔数", carLoanNum);
		return twk.toJSONString();
	}
	/**
	 * spire读取pdf文件
	 * @param filePath
	 */
	public static String readpdfSpire(String filePath){
//		String filePath2 = System.getProperty("user.dir");
		PdfDocument doc = new PdfDocument();
		try {
			doc.loadFromFile(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		PdfPageBase page;
		for (int i = 0; i < doc.getPages().getCount(); i++) {
			//获取每一行的page对象
			page = doc.getPages().get(i);
			sb.append(page.extractText(true));
		}
		doc.close();
		return sb.toString();
	}
	
	
	/**
	 * 删除 第*页，共*页
	 * @param str
	 * @return
	 */
	public static String deletePage(String str) {
		return str.replaceAll("第[0-9\\t\r\\s\n\\x22]+页，共[0-9\\t\r\\s\n\\x22]+页", "");
	}


	/**
	 * 截取字符串中的机构号
	 * @param str 格式：广州市天河区_21008822
	 * @return 21008822
	 */
	public static String parseDeptId(String str) {
		return str.substring(str.indexOf("_") + 1);
	}

}
