package controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sist.msk.Action;

import auth.LoginFailException;
import auth.LoginService;
import auth.LogoutService;
import invent.user.DuplicateIdException;
import invent.user.InventUser;
import invent.user.InventUserDao;
import invent.user.InventUserDeleteService;
import invent.user.InventUserInsertService;
import invent.user.InventUserUpdateService;
import invent.user.InventUserValidate;

public class InventController extends Action {
	
	public String home(HttpServletRequest req, HttpServletResponse res) throws Exception{
		return "/view/loginHome.jsp";
	}
	
	public String login(HttpServletRequest req, HttpServletResponse res) throws Exception{
		System.out.println("# LoginHandler processSubmit #");
		//loginHome.jsp 의 FORM 입력 값 받아오기
		String id = (req.getParameter("userid").trim());
		String password = (req.getParameter("password").trim());
		
		//FORM 의 입력 값 확인
		Map<String, Boolean> errors = new HashMap<>();
		req.setAttribute("errors", errors);
		
		if (id == null || id.isEmpty()) errors.put("id", Boolean.TRUE);
		
		if (password == null || password.isEmpty()) errors.put("password", Boolean.TRUE);
		
		if (!errors.isEmpty()) {
			return "/view/loginHome.jsp";
		}
		
		// 입력한 id, password 가 INVENTUSER Table 에 있는지loginService.login 으로 확인
		try {
			LoginService loginService = new LoginService();
			InventUser inventuser = loginService.login(id, password); // 로그인 로직 수행.
			req.getSession().setAttribute("authUser",  inventuser); // VIEW 페이지에서 사용할 값 지정.
			
			System.out.println("# LoginHandler processSubmit  END #");

			return "/view/inventIndex.jsp"; // VIEW 로 사용할 페이지 지정하여 리턴.
		
		}catch (LoginFailException e) {
			errors.put("idOrPwNotMatch", Boolean.TRUE);
			return "/view/loginHome.jsp";
		}
	}
	
	public String inventUserInsert(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 
		
		if (request.getMethod().equalsIgnoreCase("GET")) {
			return "/view/user/inventUserInsertForm.jsp";
		} 
		else if (request.getMethod().equalsIgnoreCase("POST")) {
				
			System.out.println("# InventController inventUserInsert #");
			
			InventUserInsertService inventUserInsertService = new InventUserInsertService();
			
			int login=0, uno=0;
			
			InventUserValidate userValidate = new InventUserValidate();
			
			userValidate.inventuser.setUno(uno);// service 에서 새로운 값 생성함.
			userValidate.inventuser.setUname(request.getParameter("uname"));
			userValidate.inventuser.setUserid(request.getParameter("userid"));
			userValidate.inventuser.setPassword(request.getParameter("password"));
			userValidate.inventuser.setDeptno(request.getParameter("deptno"));
			userValidate.inventuser.setGrade(Integer.parseInt(request.getParameter("grade")));
			userValidate.inventuser.setLogin(login);
			userValidate.inventuser.setConfirmpassword(request.getParameter("confirmPassword"));
			
			Map<String, Boolean> errors = new HashMap<>();
			request.setAttribute("errors",  errors);
			
			userValidate.validate(errors);
			
			if (!errors.isEmpty()) {return "/view/user/inventUserInsertForm.jsp";}
			
			int userinsertok = 0;
			
			inventUserInsertService.inventUserInsert(userValidate);
				
			userinsertok=1;
				
			request.setAttribute("userinsertok",userinsertok);
			request.setAttribute("userid",request.getParameter("userid"));
			request.setAttribute("uname",request.getParameter("uname"));
			
			System.out.println("# UserInsertHandler processSubmit END #");
		}
		return "/view/user/inventUserInsertForm.jsp";
	}
	
	public String inventUserList(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 

		int pageSize = 10;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss");
		String datetime = sdf.format(cal.getTime());

		String pageNum = request.getParameter("pageNum");
		if (pageNum == null || pageNum == "") {
			pageNum = "1";
		}
			
		int currentPage = Integer.parseInt(pageNum);
		int startRow = (currentPage - 1) * pageSize + 1;
		int endRow = currentPage * pageSize;
		int count = 0;
		int number = 0;
			
		List inventUserList = null;
		InventUserDao userDao = new InventUserDao();
				
		count = userDao.getUserCount();
				
		if (count > 0) {
			inventUserList = userDao.getUsers(startRow, endRow);
		}
				
		number = count - (currentPage - 1) * pageSize;
				
		int bottomLine = 3;
		int pageCount = count / pageSize + (count % pageSize == 0 ? 0 : 1);
		int startPage = 1 + (currentPage - 1) / bottomLine * bottomLine;
		int endPage = startPage + bottomLine - 1;
		if (endPage > pageCount) endPage = pageCount;
				
		request.setAttribute("userList", inventUserList);
		request.setAttribute("pageCount",pageCount);
		request.setAttribute("endPage",endPage);
		request.setAttribute("bottomLine",bottomLine);
		request.setAttribute("startPage",startPage);
		request.setAttribute("currentPage",currentPage);
		request.setAttribute("number",number);
		request.setAttribute("count",count);
		request.setAttribute("datetime",datetime);
				
		return "/view/user/inventUserList.jsp";
	}
	
	public String inventUserUpdate(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 
		
		if (request.getMethod().equalsIgnoreCase("GET")) {
			List unoArray = null;
			InventUserDao userDao = new InventUserDao();
			
			unoArray = userDao.getUnos();
				
			request.setAttribute("unoList", unoArray);
			request.setAttribute("unoSize", unoArray.size());
				
			System.out.println("UpdateHandler processForm unoSize "+request.getAttribute("unoSize"));

			return "/view/user/inventUserUpdateForm.jsp";
		} 
		else if (request.getMethod().equalsIgnoreCase("POST")) {
			InventUserValidate userValidate = new InventUserValidate();
			InventUserUpdateService inventUserUpdateService = new InventUserUpdateService();
			
			userValidate.inventuser.setUno(Integer.parseInt(request.getParameter("uno")));
			userValidate.inventuser.setUname(request.getParameter("uname"));
			userValidate.inventuser.setUserid(request.getParameter("userid"));
			userValidate.inventuser.setPassword(request.getParameter("password"));
			userValidate.inventuser.setDeptno(request.getParameter("deptno"));
			userValidate.inventuser.setGrade(Integer.parseInt(request.getParameter("grade")));
			userValidate.inventuser.setLogin(0);
			userValidate.inventuser.setConfirmpassword(request.getParameter("password"));
			
			Map<String, Boolean> errors = new HashMap<>();
			request.setAttribute("errors",  errors);
			
			userValidate.validate(errors);
			
			if (!errors.isEmpty()) {
				System.out.println("validate");
				return "/view/user/inventUserUpdateForm.jsp";
			}
			
			int userupdateok = 0;

			inventUserUpdateService.inventUserUpdate(userValidate);
				
			userupdateok=1;
				
			request.setAttribute("userupdateok",userupdateok);
			request.setAttribute("uno",userValidate.inventuser.getUno());
			request.setAttribute("uname",userValidate.inventuser.getUname());
			
		}
		return "/view/user/inventUserUpdateForm.jsp";
	}
	
	public String inventUserGetUserByUno(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 
		System.out.println("# InventUserGetUserByUnoHandler processSubmit #");
		
		int uno = Integer.parseInt(request.getParameter("name"));
		
		// Service Logic
		InventUserDao userDao = new InventUserDao();
		
		InventUser inventUser = userDao.getUser(uno); //check?
			
		request.setAttribute("uno", inventUser.getUno());
		request.setAttribute("uname", inventUser.getUname());
		request.setAttribute("userid", inventUser.getUserid());
		request.setAttribute("password", inventUser.getPassword());
		request.setAttribute("deptno", inventUser.getDeptno());
		request.setAttribute("grade", inventUser.getGrade());
		request.setAttribute("login", inventUser.getLogin());	
			
		System.out.println("# GetUserByUnoHandler process userid # "+inventUser.getUserid());

		return "/view/user/getUserByUno.jsp";
	}
	
	public String inventUserDelete(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 
		if (request.getMethod().equalsIgnoreCase("GET")) {
			List unoArray = null;
			InventUserDao userDao = new InventUserDao();
			
			unoArray = userDao.getUnos();
				
			request.setAttribute("unoList", unoArray);
			request.setAttribute("unoSize", unoArray.size());

			return "/view/user/inventUserDeleteForm.jsp";
		} 
		else if (request.getMethod().equalsIgnoreCase("POST")) {
			InventUserValidate userValidate = new InventUserValidate();
			InventUserDeleteService inventUserDeleteService = new InventUserDeleteService();
		
			userValidate.inventuser.setUno(Integer.parseInt(request.getParameter("uno")));
			userValidate.inventuser.setUname(request.getParameter("uname"));
			userValidate.inventuser.setUserid(request.getParameter("userid"));
			userValidate.inventuser.setPassword(request.getParameter("password"));
			userValidate.inventuser.setDeptno(request.getParameter("deptno"));
			userValidate.inventuser.setGrade(Integer.parseInt(request.getParameter("grade")));
			userValidate.inventuser.setLogin(0);
			userValidate.inventuser.setConfirmpassword(request.getParameter("password"));
			
			Map<String, Boolean> errors = new HashMap<>();
			request.setAttribute("errors",  errors);
			
			userValidate.validate(errors);
			
			if (!errors.isEmpty()) {
				System.out.println("validate");
				return "/view/user/inventUserUpdateForm.jsp";
			}
			
			int userdeleteok = 0;

			inventUserDeleteService.inventUserDelete(userValidate);
				
			userdeleteok=1;
				
			request.setAttribute("userdeleteok",userdeleteok);
			request.setAttribute("uno",userValidate.inventuser.getUno());
			request.setAttribute("uname",userValidate.inventuser.getUname());			
		}
		return "/view/user/inventUserUpdateForm.jsp";
	}
	
	public String inventIndex(HttpServletRequest request, HttpServletResponse response)  throws Throwable { 
			 return  " "; 
	}
}
