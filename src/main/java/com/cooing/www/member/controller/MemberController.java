package com.cooing.www.member.controller;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cooing.www.album.vo.Category;
import com.cooing.www.common.vo.FileLimit;
import com.cooing.www.member.dao.MemberDAO;
import com.cooing.www.member.vo.Member;


@Controller
public class MemberController {
	private static final Logger logger = LoggerFactory.getLogger(MemberController.class); 
	private static String strFilePath = "/FileSave/Member";
	
	@Autowired
	MemberDAO memberDAO;
	
	@RequestMapping(value="/member_post" , method = RequestMethod.POST)
	public String member_post(Member member , @RequestParam(value="hobby" , required =false) String[] hobby
			,MultipartFile upload , HttpSession session){	
		logger.info("member_post__jinsu");
		if(upload != null && !upload.isEmpty()){
			String savefile = FileLimit.FileSave(upload, strFilePath);
			member.setMember_picture(savefile);
		} else {
			member.setMember_picture("default_person.png");
		}
		if(memberDAO.insertMember(member)){
			if(hobby != null && hobby.length != 0){
				for(int i = 0; i < hobby.length; i++){
					memberDAO.insertCategory(new Category(0 , member.getMember_id() , Integer.parseInt(hobby[i])));
				}				
			}
			Member member_check= memberDAO.selectMember(member.getMember_id());
			session.setAttribute("Login", true);
			session.setAttribute("Member", member_check);
		}
		
		return "redirect:/";
	}
	
	@ResponseBody
	@RequestMapping(value="/member_check" , method = RequestMethod.POST)
	public String member_check(Member member){
		logger.info("member_check__jinsu");
		char[] cban ={ '/' , '&' , '<' , '>' , '|'};
		//검사
		if(member.getMember_id() != null){
			if(member.getMember_pw() != null){
				for(int i = 0; i < member.getMember_pw().length(); i++){
					for(int j = 0; j < cban.length; j++){
						if(member.getMember_pw().charAt(i) == cban[j]){
							return "fail";
						}
					}
				}
				if(member.getMember_pw().length() < 6 || 12 < member.getMember_pw().length()){
					return "fail";
				}
			}
			return "success";
		}
		else{
			return "fail";
		}		
	}
	
	@ResponseBody
	@RequestMapping(value="/select_friend" , method = RequestMethod.POST)
	public ArrayList<Member> select_friend(HttpSession session){
		logger.info("select_friend__jinsu");
		Member member = (Member)session.getAttribute("Member");
		return memberDAO.selectfriend(member.getMember_id());
	}
	
	@ResponseBody
	@RequestMapping(value="/search_friend_id" , method = RequestMethod.POST)
	public ArrayList<Member> search_friend_id(String text , HttpSession session){
		logger.info("search_friend_id__jinsu");
		Member member = (Member)session.getAttribute("Member");
		Map<String,String> map = new HashMap<String,String>(); 
		map.put("friendid", text);
		map.put("myid", member.getMember_id());
		return memberDAO.searchId(map);				
	}
	
	@ResponseBody
	@RequestMapping(value="/search_allid" , method = RequestMethod.POST)
	public ArrayList<String> search_id(String text , HttpSession session){
		logger.info("search_id__jinsu");
		return memberDAO.searchallId(text);				
	}
	
	@ResponseBody
	@RequestMapping(value="/search_user_id" , method = RequestMethod.POST)
	public ArrayList<Member> search_user_id(String text ,  HttpSession session){
		logger.info("search_user_id__jinsu");
		Member member = (Member)session.getAttribute("Member");
		Map<String,String> map = new HashMap<String,String>(); 
		map.put("friendid", text);
		map.put("myid", member.getMember_id());
		return  memberDAO.searchUser(map);			
	}
	
	@ResponseBody
	@RequestMapping(value="/id_check" , method = RequestMethod.POST)
	public String id_check(String id){
		logger.info("id_check__jinsu");
		Member member_check= memberDAO.selectMember(id);
		if(member_check != null){
			return "fail";
		}
		return "success";
	}
	
	@RequestMapping(value="/login_get" , method = RequestMethod.GET)
	public String login_get(){		
		logger.info("login_get__jinsu");
		return "/login";
	}
	
	@RequestMapping(value="/logout_get" , method = RequestMethod.GET)
	public String logout_get(HttpSession session){
		logger.info("logout_get__jinsu");
		Member member = (Member)session.getAttribute("Member");
		session.removeAttribute("Login");
		session.removeAttribute("Member");
		memberDAO.updateTimeMember(member.getMember_id());
		return "redirect:/";
	}
	
	@ResponseBody
	@RequestMapping(value="/login_post" , method = RequestMethod.POST)
	public String login_post(Member member,HttpSession session){
		logger.info("login_post__jinsu");
		Member member_check= memberDAO.selectMember(member.getMember_id());
		if(member_check != null){
			if(member_check.getMember_pw().equals(member.getMember_pw())){
				session.setAttribute("Login", true);
				session.setAttribute("Member", member_check);
				memberDAO.updateTimeMember(member_check.getMember_id());
				return "success";
			}
			else{
				return "fail";
			}
		}
		else{
			return "fail";
		}				
	}
	
	@RequestMapping(value = "img_member", method = RequestMethod.GET)
	public String img(HttpServletResponse response , HttpSession session ) {
		logger.info("img__jinsu");
		Member member = (Member)session.getAttribute("Member");
		String fullpath = strFilePath + "/" + member.getMember_picture();
		if( member.getMember_picture() != null &&!member.getMember_picture().isEmpty() && member.getMember_picture().length() != 0){
			FileInputStream filein = null;
			ServletOutputStream fileout = null;
			try {
				filein = new FileInputStream(fullpath);
				fileout = response.getOutputStream();
				FileCopyUtils.copy(filein, fileout);			
				filein.close();
				fileout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	@RequestMapping(value = "memberimg", method = RequestMethod.GET)
	public String memberimg(String strurl,HttpServletResponse response) {
		logger.info("memberimg__jinsu");
		String fullpath = strFilePath + "/" + strurl;
		if(strurl != null && !strurl.isEmpty() && strurl.length() != 0){
			FileInputStream filein = null;
			ServletOutputStream fileout = null;
			try {
				filein = new FileInputStream(fullpath);
				fileout = response.getOutputStream();
				FileCopyUtils.copy(filein, fileout);			
				filein.close();
				fileout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
