import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Main {

    private static JFrame frame;
    private static JLabel helloWorld;

    public static void main(String[] AlPt) {
//        System.out.println("Hello World!");

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });


//         for fun
        TestTimer testTimer = new TestTimer();
        {
            testTimer.run();
        }


        RunningText runningText = new RunningText();
        runningText.start();




    }

    public static class TestTimer extends Thread{
        @Override
        public void run() {
            Random stop = new Random();
            if(stop.nextInt(10)==0) { // 有 10% 的概率窗口瞎晃，没错, just fou fun.
                for (int i = 0; ; i++) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    while (frame==null){
                        try { // （轮询）
                            this.wait(100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                    Random random = new Random();
                    Dimension dimension = new Dimension();
                    dimension.height = 400 + (random.nextInt() % 30);
                    dimension.width = 700 + (random.nextInt() % 10);
                    frame.setSize(dimension);
                }
            }
        }
    }

    public static class RunningText extends Thread{
        @Override
        public void run() {

            // 等待主程序把字符 JLabel 搞出来（轮询）
            while (helloWorld==null){
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            long startTime = System.currentTimeMillis();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            ;// new Date()为获取当前系统时间


            while(true){
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long curTime = System.currentTimeMillis();
                helloWorld.setText(df.format(new Date()));
                int position = (int) ((curTime-startTime)/5)%900;

//                helloWorld.setBounds(position-200,30+position/7,200,100);
                helloWorld.setLocation(position-200,30+position/15);
            }


        }
    }

    private static void createAndShowGUI() {
        // 确保一个漂亮的外观风格
//        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        frame = new JFrame("Running text with a picture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        javax.swing.Icon icon = new ImageIcon("res/icon1.png");

        // 添加 "Hello World" 标签
        helloWorld = new JLabel("Hello World",icon,JLabel.LEFT);
        Container contentPane = frame.getContentPane();
        contentPane.add(helloWorld);

        // 炸了布局管理器，这样可以控制位置。
        contentPane.setLayout(null);
        helloWorld.setBounds(0,30,300,100);

        // 窗口大小和位置
        frame.setLocation(300,170);
        frame.setSize(700,400);

        // 显示窗口
        frame.setVisible(true);
    }


}

