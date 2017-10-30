package com.yq.controller.index;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.PageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yq.service.banner.BannerManager;
import com.yq.service.goods.GoodsManager;
import com.yq.service.navigation.NavigationManager;
import com.yq.service.news.NewsManager;

@Controller
public class IndexController extends BaseController{
	@Resource(name="bannerService")
	private BannerManager bannerService;
	
	@Resource(name="navigationService")
	private NavigationManager navigationService;
	
	@Resource(name="newsService")
	private NewsManager newsService;
	
	@Resource(name="goodsService")
	private GoodsManager goodsService;
	
	private Gson gson = new GsonBuilder().serializeNulls().create();
	private Logger log = Logger.getLogger(this.getClass());
	@RequestMapping(value = "/index")
	public ModelAndView index(Page page) throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		page.setPd(pd);
		List<PageData>	bannerlist = bannerService.listAll(pd);	//列出Banner列表
		List<PageData>	navigationlist = navigationService.listAll(pd);// 列出导航图标列表
		List<PageData> newslist = newsService.list(page); // 列出新闻列表
		pd.put("tuijian", "1");
		List<PageData> list = goodsService.listAll(pd); 
		List<PageData> tuijianlist = new LinkedList<PageData>();
		for (int i = 0; i < list.size(); i++) {
			PageData goods =  list.get(i);
			String pic =goods.getString("goods_pic");
			if(StringUtils.isNotEmpty(pic)){
				if(pic.contains(",")){
					pic = pic.split(",")[0];
				}
			}
			goods.put("goods_pic", pic);
			tuijianlist.add(goods);
		}
		
		pd.put("tuijian", "");
		page.setPd(pd);
		List<PageData> glist = goodsService.list(page); 
		List<PageData> goodslist = new LinkedList<PageData>();
		for (int i = 0; i < glist.size(); i++) {
			PageData goods =  glist.get(i);
			String pic =goods.getString("goods_pic");
			if(StringUtils.isNotEmpty(pic)){
				if(pic.contains(",")){
					pic = pic.split(",")[0];
				}
			}
			goods.put("goods_pic", pic);
			goodslist.add(goods);
		}
		mv.addObject("tuijianlist", tuijianlist);
		mv.addObject("goodslist", goodslist);
		mv.addObject("newslist", newslist);
		mv.addObject("bannerlist", bannerlist);
		mv.addObject("navigationlist", navigationlist);
		mv.setViewName("index/index");
		return mv ;
	}
	@ResponseBody
	@RequestMapping(value = "/app/index", produces = "application/json;charset=UTF-8")
	public String appindex(Page page) throws Exception{
		return "";
	}
	
}
