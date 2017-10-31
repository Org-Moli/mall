package com.yq.controller.navigation;

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
import com.yq.service.navigation.NavigationManager;

/** 
 * 说明：首页导航
 * 创建人：摩里科技 qq
 * 创建时间：2016-12-29
 */
@Controller
@RequestMapping(value="/navigation")
public class NavigationController extends BaseController {
	
	String menuUrl = "navigation/list.do"; //菜单地址(权限用)
	@Resource(name="navigationService")
	private NavigationManager navigationService;
	

	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
//		page.setPd(pd);
		List<PageData>	varList = navigationService.listAll(pd);	//列出Navigation列表
		mv.setViewName("navigation/navigation_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		return mv;
	}

}
