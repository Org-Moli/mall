package com.yq.controller.banner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.PageData;
import com.google.gson.Gson;
import com.yq.service.banner.BannerManager;

/** 
 * 说明：轮播图片
 * 创建时间：2016-12-12
 */
@Controller
@RequestMapping(value="/banner")
public class BannerController extends BaseController {
	
	String menuUrl = "banner/list.do"; //菜单地址(权限用)
	@Resource(name="bannerService")
	private BannerManager bannerService;
	private Gson gson = new Gson();

	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list() throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		List<PageData>	varList = bannerService.listAll(pd);	//列出Banner列表
		mv.setViewName("banner/banner_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		logger.info(gson.toJson(varList));
		return mv;
	}
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
