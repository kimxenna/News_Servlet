package com.example.jaxrs_test.controller;

import com.example.jaxrs_test.model.News;
import com.example.jaxrs_test.model.NewsDAO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.BeanUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

@Log4j2
@WebServlet("*.nhn")
//뉴스 이미지 파일 업로드 처리를 위해 @MultipartConfig를 추가
//최대 파일 크기와 저장 위치를 지정.
@MultipartConfig(maxFileSize =  1024 * 1024 * 2, location = "c:/img")
public class NewController extends HttpServlet {
    private NewsDAO dao;
    private ServletContext ctx; //서버 로그 메시지 생성을 위해 선언

    //웹 리소스 기본 경로 지정
    private final String START_PAGE = "ver01/newsList.jsp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        dao = NewsDAO.getInstance();
        ctx = getServletContext();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        서블릿 요청시에 호출. 요청을 분석하여 doGet(), doPost()로 배분
        req.setCharacterEncoding("utf-8");
        String action = req.getParameter("action");


//        자바 리플렉션을 사용해 if, switch 없이 요청에 따라 구현 메서드가 실행되도록 구성
//        자바의 리플렉션 API를 사용해 자동으로 action에 지정된 이름과 동일한 메서드를 호출하는 구조
//        불필요한 if 처리를 줄여주고 구조 변경에 유리한 방법
        Method m;
        String view = null;

//        action 파라미터 없이 접근한 경우
//        컨트롤러 실행 시 자동으로 시작 페이지로 이동
        if (action == null){
            action = "listNews";
        }
        //1. 자바 리플렉션으로 action으로 전달된 이름의 메서드 자동으로 호출
        //* 자바 리플렉션 API
        //자바 클래스 구조를 프로그램에서 직접 제어할 수 있는 라이브러리로,
        //구체적인 클래스 타입을 모르더라도 문자열 이름으로 클래스를 참조하거나
        //클래스 메서드나 변수를 읽어와 실행하는 등의 작업이 가능
        try {
            //현재 클래스에서 action이름과 HttpServletRequest를 파라미터로 하는 메서드 찾음
            m = this.getClass().getMethod(action, HttpServletRequest.class);
            //this.getClass() : 현재 클래스 객체에 대한 참조
            //getMethod(): 첫번째 인자로 메서드 이름, 두번째 인자 타입을 메서드의 인자로 가지는 메서드 객체를 가져옴

            //메서드 실행 후 리턴값을 받아옴
            view = (String) m.invoke(this, req);
        }catch (NoSuchMethodException e){
            e.printStackTrace();;
            ctx.log("요청 action 없음!!"); // 톰캣의 콘솔에 로드 메시지를 남기는 메서드
            req.setAttribute("error", "action 파라미터가 잘못되었습니다.");
            view = START_PAGE;
        }catch (Exception e){
            e.printStackTrace();
        }
        //2. 뷰 이동
        // 요청 처리 메서드를 호출한 다음 리턴된 뷰 페이지로 이동

        //view 문자열이 "redirect:/" 문자열로 시작하는 경우 "redirect:/"이후의 경로만
        //들고와 resp.sendRedirect()를 이용해 페이지 이동.
        if (view.startsWith("redirect:/")){
            String rview = view.substring("redirect:/".length()); //redirect:/ 문자열 이후의 경로만 가져옴.
            resp.sendRedirect(rview);
        }
        else {
            RequestDispatcher requestDispatcher = req.getRequestDispatcher(view);
            requestDispatcher.forward(req, resp); // 지정된 뷰로 포워딩
        }
    }

    private  String getFilename(Part part){
//        Part 객체를 사용해 multipart 헤더에서 파일이름 추출
        String fileName = null;
//        파일 이름이 들어 있는 헤더 영역을 가져옴
//        파일 이름이 들어있는 헤더 영역을 가지고 옴
        String header = part.getHeader("content-disposition");
//        part.getHeader -> form-data; name="file"; filename="사진5.jpg"
        ctx.log("File Header : " + header);

//        파일 이름이 들어있는 속성 부분의 시작위치를 가져와 쌍따옴표 사이의 값 부분만 가지고옴
        int start = header.indexOf("filename=");
        fileName = header.substring(start + 10, header.length()-1);
        ctx.log("파일명 : " + fileName);
        return fileName;
    }

    public String addNews(HttpServletRequest request){
//        뉴스 기사 등록하기 위한 요청을 처리하는 메서드
        News news = new News();
        try {
//            이미지 파일 저장을 위해 request로 부터 Part 객체 참조
            Part part = request.getPart("file");
            String fileName = getFilename(part);
            if (fileName != null && !fileName.isEmpty()){
                part.write(fileName);//파일 이름이 있으면 파일 저장.
            }
//            BeanUtils.populate() : 파라미터로 전달된 name 속성과 일치하는 news클래스의 멤버 변수를 찾아 값을 전달
            BeanUtils.populate(news, request.getParameterMap());
//            이미지 파일 이름을 News 객체에도 저장
            news.setImg("/img/" + fileName);

            dao.addNews(news);
        }catch (Exception e){
            e.printStackTrace();
            ctx.log("뉴스 추가 과정에서 문제 발생!!");
            request.setAttribute("error", "뉴스가 정상적으로 등록되지 않았습니다.");
            return  listNews(request);
        }
        return  "redirect:/news.nhn?action=listNews";
    }

    public String listNews(HttpServletRequest request){
        /*newsList.jsp에서 뉴스 목록을 보여주기 위한 요청을 처리하는 메서드 */
        List<News> list;
        try {
            list = dao.getAll();
            request.setAttribute("newslist", list);
        }catch (Exception e){
            e.printStackTrace();
            ctx.log("뉴스 목록 생성 과정에서 문제 발생!!");
            request.setAttribute("error", "뉴스 목록이 정상적으로 처리되지 않았습니다.!");
        }
        return "./newsList.jsp";
    }

    public String getNews(HttpServletRequest request){
        int aid = Integer.parseInt(request.getParameter("aid"));
        try {
            News n = dao.getNews(aid);
            request.setAttribute("news",n);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.log("뉴스를 가져오는 과정에서 문제 발생 !!");
            request.setAttribute("error", "뉴스를 정상적으로 가져오지 못했습니다.!!");
        }
        return "./newsView.jsp";
    }

    public String deleteNews(HttpServletRequest request){
//        뉴스를 삭제하기 위한 메서드
        int aid = Integer.parseInt(request.getParameter("aid"));
        try {
            dao.delNews(aid);
        }catch (SQLException e){
            e.printStackTrace();
            ctx.log("뉴스 삭제 과정에서 문제 발생 !");
            request.setAttribute("error", "뉴스가 정상적으로 삭제되지 않았습니다.");
            return listNews(request);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return "redirect:/news.nhn?action=listNews";
    }
}
