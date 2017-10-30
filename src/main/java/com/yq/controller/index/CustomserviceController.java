package com.yq.controller.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomserviceController {
	
	@RequestMapping(value="/app/customservice")
	public ModelAndView cssc(){
		return new ModelAndView("redirect:https://qanpai.qiyukf.com/client?k=f7dc0f9e5b2b4e5e3acbffeb7326f107&wp=1");
	}
}
