package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notice")
public class NoticeController {

	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("message", "NoticeController/notice/index");
		return "/notice/index";
	}
}