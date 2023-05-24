package com.example.jaxrs_test.rest;

import com.example.jaxrs_test.model.News;
import com.example.jaxrs_test.model.NewsDAO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/news")
public class NewsAPIService {
    NewsDAO dao;

    public  NewsAPIService(){
        dao = NewsDAO.getInstance();
    }
    //뉴스 목록
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<News>getNewsList(){
        List<News> newsList = null;

        try {
            newsList = dao.getAll();
        }catch (Exception e){
            e.printStackTrace();
        }
        return newsList;
    }

    //뉴스 삭제
    @DELETE
    @Path("{aid}") //뉴스 삭제를 위해서는 삭제할 뉴스의 aid가 전달되어야 하는데 여기서는 경로 파라미터를 이용해서 삭제할 아이디 값 전달.
    public String delNews(@PathParam("aid") int aid){
        try {
            dao.delNews(aid);
        }catch (Exception e){
            e.printStackTrace();
            return "News API : 뉴스 삭제 실패!! -" + aid;
        }
        return "News API : 뉴스 삭제 됨!! -" + aid;
    }

    //뉴스 등록
    @POST
    @Consumes(MediaType.APPLICATION_JSON)//클라이언트 요청에 포함된 미디어 타입을 지정. JSON을 사용
    public String addNews(News news){
        try {
            dao.addNews(news); //@Consumes 설정에 따라 HTTP Body에 포함된 JSON문자열이 자동으로 News로 변환
            //이를 위해서 JSON문자열의 키와 New 객체의 멤버변수명이 동일해야함
        } catch (Exception e){
            e.printStackTrace();
            return "News API : 뉴스 등록 실패!!";
        }
        return "News API : 뉴스 등록됨!!";
    }
    //상세보기
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{aid}")
    public News getNews(@PathParam("aid") int aid) {
        News news = null;
        try {
            news = dao.getNews(aid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return news;
    }
}

