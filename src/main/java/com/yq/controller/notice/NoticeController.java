package com.yq.controller.notice;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.AppUtil;
import org.change.util.PageData;
import org.change.util.Tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yq.service.notice.NoticeManager;

/** 
 * 说明：消息通知
 * 创建人：壹仟科技 qq 357788906
 * 创建时间：2017-03-01
 */
@Controller
public class NoticeController extends BaseController {
	
	@Resource(name="noticeService")
	private NoticeManager noticeService;
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/app/notice/list", produces = "application/json;charset=UTF-8")
	public String applist(Page page) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		page.setPd(pd);
		List<PageData>	noticeList = noticeService.list(page);	//列出Notice列表
		map.put("page", page);
		map.put("noticeList", noticeList);
		return gson.toJson(map);
	}
	
	/**详情
	 * @param page
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/app/notice/notice", produces = "application/json;charset=UTF-8")
	public String appnotice() throws Exception{
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData notice= noticeService.findById(pd);//列出Notice列表
		return gson.toJson(notice);
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	
	@RequestMapping(value = "/noticelist")
	public ModelAndView list(Page page) throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		page.setPd(pd);
		List<PageData>	noticeList = noticeService.list(page);	//列出Notice列表
	
		mv.setViewName("notice/index");
		mv.addObject("page", page);
		mv.addObject("noticeList", noticeList);
		return mv;
	}
	
	/**详情
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value = "/notice")
	public ModelAndView notice() throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData notice= noticeService.findById(pd);//列出Notice列表
		mv.setViewName("notice/content");
		mv.addObject("notice", notice);
		return mv;
	}
	
}
