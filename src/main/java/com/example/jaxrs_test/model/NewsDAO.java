package com.example.jaxrs_test.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NewsDAO {



    private static NewsDAO instance;
    private NewsDAO(){

    }
    public static NewsDAO getInstance(){
        if (instance == null){
            instance = new NewsDAO();
        }
        return instance;
    }

    final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    final String JDBC_URL = "jdbc:mysql://localhost:3306/news";
    final String JDBC_USER = "root";
    final String JDBC_PASSWORD = "alswn121121!";

    public Connection open(){
        Connection con = null;
        try {
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(JDBC_URL,JDBC_USER,JDBC_PASSWORD);
        }catch (Exception e){
            e.printStackTrace();
        }
        return con;
    }

    public void addNews(News news)throws Exception{
        /*뉴스를 추가하는 메서드*/
        Connection con = open();

        String sql = "INSERT INTO `news` (`title`, `img`, `date`, `content`)VALUES(?, ?, now(), ?)";
        PreparedStatement pstmt = con.prepareStatement(sql);

        //try-with-resource 기법이 적용된 부분으로 해당 리소스를 자동으로 close
        try (con;pstmt){
            pstmt.setString(1,news.getTitle());
            pstmt.setString(2,news.getImg());
            pstmt.setString(3, news.getContent());
            pstmt.executeUpdate();
        }
    }

    public List<News> getAll()throws Exception{
//        뉴스 기사 목록 전체를 가지고 오기 위한 메서드
        Connection con = open();
        List<News> newsList = new ArrayList<>();

        String sql = "SELECT `aid`, `title`, `date` FROM `news`";
        PreparedStatement pstmt = con.prepareStatement(sql);
        ResultSet resultSet = pstmt.executeQuery();

        try(con;pstmt;resultSet) {
            while (resultSet.next()){
                News news = new News();
                news.setAid(resultSet.getInt("aid"));
                news.setTitle(resultSet.getString("title"));
                news.setDate(resultSet.getString("date"));
                newsList.add(news);
            }
        }
        return newsList;
    }

    public News getNews(int aid)throws Exception{
//        뉴스 목록에서 뉴스를 선택했을 때 특정 뉴스 기사의 세부 내용을 보여주기 위한 메서드
        Connection con = open();

        News news = new News();
        String sql = "SELECT * FROM news WHERE aid = ? ORDER BY aid DESC ";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, aid);
        ResultSet resultSet = pstmt.executeQuery();
        resultSet.next();

        try(con;pstmt;resultSet) {
            news.setAid(resultSet.getInt("aid"));
            news.setTitle(resultSet.getString("title"));
            news.setImg(resultSet.getString("img"));
            news.setDate(resultSet.getString("date"));
            news.setContent(resultSet.getString("content"));
            return news;
        }
    }

    public void delNews(int aid)throws Exception {
//        뉴스 삭제
        Connection con = open();

        String sql = "DELETE FROM news WHERE aid = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        try (con; pstmt) {
            pstmt.setInt(1, aid);
//            삭제된 뉴스 기사가 없을 경우
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("DB에러");
            }
        }
    }
}
