package com.yq.controller.category;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.change.controller.base.BaseController;

import org.change.util.PageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yq.service.category.CategoryManager;

/**
 * 说明：分类 创建人：易钱科技 创建时间：2016-12-17
 */
@Controller
public class CategoryController extends BaseController {

	String menuUrl = "category/list.do"; // 菜单地址(权限用)
	@Resource(name = "categoryService")
	private CategoryManager categoryService;

	private Gson gson = new GsonBuilder().serializeNulls().create();
	private Logger log = Logger.getLogger(this.getClass());
	/**
	 * 保存
	 * 
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value = "/category/save")
	public ModelAndView save() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("CATEGORY_ID", this.get32UUID()); // 主键
		categoryService.save(pd);
		mv.addObject("msg", "success");
		mv.setViewName("save_result");
		return mv;
	}

	/**
	 * 列表
	 * 
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value = "/category/list")
	public ModelAndView list() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData category = new PageData();
		String super_id = pd.getString("super_id");
		if(StringUtils.isEmpty(super_id)){
			super_id = "0" ;
			pd.put("super_id", super_id);
		}
		List<PageData>	categoryList = categoryService.listAll(pd);
		for (int i = 0; i < categoryList.size(); i++) {
			pd.put("super_id", categoryList.get(i).getString("category_id"));
			List<PageData>	childcategory = categoryService.listAll(pd);//分类
			category.put("childcategory"+i, childcategory);
		}
		category.put("categoryList", categoryList);
		mv.addObject("category", category);
		mv.setViewName("category/index");
		mv.addObject("pd", pd);
		return mv;
	}
	@ResponseBody
	@RequestMapping(value = "/app/categoryList", produces = "application/json;charset=UTF-8")
	public String applist() throws Exception {
		PageData pd = new PageData();
		pd = this.getPageData();
		List<PageData> categoryList = categoryService.listAll(pd); // 列出Category列表
		return gson.toJson(categoryList);
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format, true));
	}
}
