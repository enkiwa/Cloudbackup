package main.java.com.client;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FileUpdata {

    private String filespath = "C:\\Users\\Shinelon\\IdeaProjects\\cloudbackeup\\src\\Main\\java\\com\\client\\files";
    private String downpath = "C:\\Users\\Shinelon\\IdeaProjects\\cloudbackeup\\src\\Main\\java\\com\\client\\downfiles";
    private File filelist[];
    private int fileSize = 20;
    private String hostname = "8.140.101.185";
    private int port =21;
    private  String username = "ftp1";
    private  String password = "Gdavid1234!";
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;

    public void setFileList() {
        //获取文件列表
        File f = new File(this.filespath);
        if (!f.exists()) {
            System.out.println(" not exists");
            return;
        }
        this.filelist = f.listFiles();

    }

    public void upFile(){
        FTPClient ftp = new FTPClient();
        try {
            //获取链接，创建sql执行对象
            con=JDBCTool.getConnection();
            st=con.createStatement();

            //ftp用户登录
            ftp.connect("8.140.101.185", 21);
            boolean loginS = ftp.login("ftp1", "Gdavid1234!");
            if (!loginS) {
                System.out.println("ftp登录失败，用户名或密码错误");
                return;
            }

            for (int i = 0; i < filelist.length; i++) {
                Files f = new Files(filelist[i]);
                int x =i+1;
                //mysql查询文件是否存在
                String quesql = "select `name`,`flag` from db_aliyun.tb_files where(`id` = '"+x+"') ;";
                st.executeQuery(quesql);
                rs = st.getResultSet();
                int ex=0;
                while(rs.next()){
                    String myname = rs.getString(1);
                    boolean myflag = rs.getBoolean(2);
                    if(myflag&&(myname.equals(f.getName()))){
                        ex=1;
                        break;
                    }
                }
                //若文件信息不在数据库中，则上传文件及信息
                if(ex==0){
                    //mysql传入文件信息
                    System.out.println("导入文件id："+x);
                    String upsql = "UPDATE `db_aliyun`.`tb_files` SET `name` = '"+f.getName()+"', `size` = '"+f.getSize()+"', `flag` = '"+1+"' WHERE (`id` = '"+x+"');";
                    st.executeUpdate(upsql);
                    f.setFlog(true);
                    //ftp上传文件
                    String file = filespath+f.getName();
                    FileInputStream input = new FileInputStream(file);
                    ftp.setFileType(FTPClient.BINARY_FILE_TYPE);//以二进制的方式传输文件
                    if (!ftp.storeFile(new File(file).getName(), input)) {
                        System.out.println("失败，服务器返回:" + ftp.getReplyString());//获取上传失败的原因
                     } else {
                        System.out.println("文件:" + new File(file).getName() + " 上传成功");
                    }
                    input.close();
                }
            }
            ftp.logout();

            if(filelist.length<fileSize){
                for(int i =filelist.length+1;i<=fileSize;i++){
                    String sql = "UPDATE `db_aliyun`.`tb_files` SET `name` = '"+1+"', `size` = '"+1+"', `flag` = '"+0+"' WHERE (`id` = '"+i+"');";
                    st.executeUpdate(sql);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                JDBCTool.relase(con,st,rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void downFile(String fileName){
        FTPClient ftp = new FTPClient();
        try {
            //ftp用户登录
            ftp.connect(hostname,port);
            boolean loginS = ftp.login(username,password);
            if (!loginS) {
                System.out.println("ftp登录失败，用户名或密码错误");
                return;
            }
            //ftp下载文件
            int flag=0;  //是否找到文件
            FTPFile[] fs=ftp.listFiles();
            for(FTPFile ff:fs){
                if(ff.getName().equals(fileName)){
                    File localFile = new File(downpath+"\\"+ff.getName());
                    flag =1;
                    OutputStream is = new FileOutputStream(localFile);
                    //ftp.retrieveFile(ff.getName(),is);
                    if (!ftp.retrieveFile(ff.getName(),is)) {
                        System.out.println("下载失败");
                     } else {
                        System.out.println("文件:" + ff.getName() + " 下载成功");
                    }
                    is.close();
                }
            }
            if(flag==0){
                System.out.println("未找到相应文件！");
            }
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
     }

    public void deFile(String fileName){
        FTPClient ftp = new FTPClient();
        try {
            //获取链接，创建sql执行对象
            con=JDBCTool.getConnection();
            st=con.createStatement();
            //ftp用户登录
            ftp.connect(hostname,port);
            boolean loginS = ftp.login(username,password);
            if (!loginS) {
                System.out.println("ftp登录失败，用户名或密码错误");
                return;
            }
            FTPFile[] fs=ftp.listFiles();
            int flag=0;  //是否找到文件
            for(FTPFile ff:fs){
                if(ff.getName().equals(fileName)){
                    flag=1;
                    if (!ftp.deleteFile(fileName)) {
                        System.out.println("删除失败");
                    } else {
                        System.out.println("文件:" + fileName + " 删除成功");
                    }
                    //同时删除数据库中对应的文件信息行
                    String desql = "delete from db_aliyun.tb_files where name ='"+fileName+"';";
                    st.executeUpdate(desql);
                    //整理数据库中数据
                    String sortsql = "ALTER TABLE `tb_files` DROP `id`;\n" +
                            "ALTER TABLE `tb_files` ADD `id` int NOT NULL FIRST;\n" +
                            "ALTER TABLE `tb_files` MODIFY COLUMN `id` int NOT NULL AUTO_INCREMENT,ADD PRIMARY KEY(id);";
                    st.executeUpdate(sortsql);
                    //补充一个空白项,保持表大小不变
                    String mksql = "INSERT INTO `db_aliyun`.`tb_files` (`id`, `name`, `size`, `flag`) VALUES ('"+fileSize+"', '1', '1', '0');";
                    st.executeUpdate(mksql);
                }
            }
            if(flag==0){
                System.out.println("未找到相应文件！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                JDBCTool.relase(con,st,rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void queFile(){
        try {
            //获取链接，创建sql执行对象
            con=JDBCTool.getConnection();
            st=con.createStatement();
            String quesql = "SELECT * FROM db_aliyun.tb_files;";
            rs=st.executeQuery(quesql);
            System.out.println("id:\t\tname:\t\tsize:\t\tflag:");
            while(rs.next()){
                System.out.println(rs.getInt(1)+"\t\t"+rs.getString(2)+"\t\t"+rs.getLong(3)+"\t\t"+rs.getBoolean(4));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                JDBCTool.relase(con,st,rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}


