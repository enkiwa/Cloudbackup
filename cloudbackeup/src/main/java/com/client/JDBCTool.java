package main.java.com.client;

import java.sql.*;

public class JDBCTool {
    private static String DRIVER =null;
    private static String URL =null;
    private static String USERNMAE =null;
    private static String PASSWORD =null;

    static{
        try {
            DRIVER = "com.mysql.cj.jdbc.Driver";
            URL = "jdbc:mysql://8.140.101.185:3306/db_aliyun?userUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=UTC";
            USERNMAE = "root";
            PASSWORD = "root";
            Class.forName(DRIVER);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    } //加载驱动

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL,USERNMAE,PASSWORD);
    } //获取连接

    public static void relase(Connection con, Statement st, ResultSet rs) throws SQLException{
        if(rs!=null){rs.close();}
        if(st!=null){st.close();}
        if(con!=null){con.close();}
    } //释放连接
}
