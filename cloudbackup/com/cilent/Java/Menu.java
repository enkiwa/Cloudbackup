package cilent.Java;

import java.util.Scanner;

public class Menu {

    FileUpdata fu = new FileUpdata();
    public Menu(){
        System.out.println("******************************************");
        System.out.println("********** 欢迎使用云备份服务:) **********");
        System.out.println("******************************************");
    }
    public void Hello(){
        System.out.print("按任意键进入菜单界面：");
        Scanner menuhello = new Scanner(System.in);
        if(menuhello.hasNext()){
            menuui();
        }
        menuhello.close();
    }
    public void menuui(){
        Scanner select = new Scanner(System.in);
        int s=0;
        System.out.println("1.上传更新文件");
        System.out.println("2.下载备份文件");
        System.out.println("3.删除备份文件");
        System.out.println("4.查看备份文件");
        System.out.println("0.退出服务");
        System.out.print("请输入你的选择（0`4）:");
        if(select.hasNextInt()){
            s=select.nextInt();
        }else {
            System.out.println("请输入正确格式");
            return;
        }
        if(s<0||s>4){
            System.out.println("请输入正确数字");
            return;
        }
        switch (s){
            case 1:up();Hello();break;
            case 2:down();Hello();break; //获取文件列表
            case 3:delete();Hello();break;
            case 4:query();Hello();break;
            case 0:break;
            default:break;
        }
        select.close();
    }
    public void up(){
        fu.setFileList(); //获取文件列表
        fu.upFile(); //将待备份文件信息存入服务器端数据库并将文件上传
    }
    public void down(){
        Scanner ds = new Scanner(System.in);
        System.out.println("请输入要下载的文件名：");
        String filename = ds.next();
        System.out.println("准备下载文件："+filename);
        fu.downFile(filename);
    }
    public void delete(){
        Scanner de = new Scanner(System.in);
        System.out.println("请输入要删除的文件名：");
        String filename = de.next();
        System.out.println("准备删除文件："+filename);
        fu.deFile(filename);
    }
    public void query(){
        fu.queFile(); //查询整表
    }
}