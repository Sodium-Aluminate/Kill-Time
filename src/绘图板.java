import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class 绘图板 {

    private static final int defPenStyleId = 0;

    private static int penStyleId = 0;

//    private static JPanel buttonGroup;
//    private static boolean penStyleChooserPrepared = false;
    private static JFrame jFrame;
    private static JLabel currentColor;

    public static void main(String[] AlPt) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());


    }

    private static class CheckButtonStatus extends Thread{ // 此线程用于测试获得当前按钮（划掉） // 此线程用于获得当前按钮并甩到一个全局变量里。

        private JPanel buttonGroup;
        public CheckButtonStatus(JPanel buttonGroup){
            this.buttonGroup =buttonGroup;
        }
        @Override
        public void run() {

            while (true) {
                Component[] components = buttonGroup.getComponents();
                for (int i = 0; i < components.length; i++) {
                    try {
                        JRadioButton a = (JRadioButton) components[i];
                        if(a.isSelected()) penStyleId = i;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                fSleep(100);  // 懒得写监听，干脆轮寻。
            }
        }
    }

    private static class AutoRepaint extends Thread{
        private JPanel[] jPanels;
        public AutoRepaint(JPanel... jPanels){
            this.jPanels=jPanels;
        }

        @Override
        public void run() {
            while (true){

                for (int i = 0; i < jPanels.length; i++) {
                    jPanels[i].repaint();
                }
                fSleep(200);  // 要不要 16.66? （误）
            }
        }
    }


    private static void createAndShowGUI() {
        // 确保一个漂亮的外观风格  <--这句话我觉得真的很有槽点——因为在我的电脑上这 UI 丑爆…… Oracle 怕不是嫌弃 Linux?
        //                      ^这个箭头不是 Lambda.
//        JFrame.setDefaultLookAndFeelDecorated(true);

        // ^ 我发现注释掉，用原生的窗口反而看起来更愉悦了。


        // 初始化窗口
        jFrame = new JFrame("绘图板");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 所以这段接口实现时是不是 java 还没有发布枚举变量啊???
        jFrame.setLocation(300,170);
        jFrame.setSize(700,400);
        jFrame.setResizable(false);

        // 布局
//        currentColor = new JLabel("",JLabel.LEFT);
        Container contentPane = jFrame.getContentPane();
        contentPane.setLayout(null);
//        contentPane.add(currentColor);

        // 单选框
        JPanel penStyleChooser;
        {
            String[] penStyleName = {"直线", "比较丑的", "最丑的"};
            JRadioButton penStyles[] = new JRadioButton[3];
            ButtonGroup penStyleButtonGroup = new ButtonGroup();
            penStyleChooser = new JPanel();
            penStyleChooser.setLayout(null);
            penStyleChooser.setBounds(30, 30, 100, 66);
            for (int i = 0; i < 3; i++) {
                penStyles[i] = new JRadioButton(penStyleName[i]);
                penStyles[i].setBounds(0, i * 22, 100, 22);
                penStyleChooser.add(penStyles[i]);
                penStyleButtonGroup.add(penStyles[i]);
            }
            penStyles[defPenStyleId].setSelected(true);
            contentPane.add(penStyleChooser);
            new CheckButtonStatus(penStyleChooser).start(); // 此线程用于测试获得当前按钮
        }

        // 清空按钮 - 准备
        JButton clearBtn;
        {
            clearBtn = new JButton("clear");
            clearBtn.setBounds(20,320,80,30);
            contentPane.add(clearBtn);
        }

        // 随机颜色按钮 - 准备
        JButton randomColor;
        {
            randomColor = new JButton("Color");
            randomColor.setBounds(20,270,80,30);
            randomColor.setForeground(DrawsCollector.getCurrentColor());
            contentPane.add(randomColor);
        }

        // 随机粗细按钮 - 准备
        JButton randomStroke;
        {
            randomStroke = new JButton("Stroke");
            randomStroke.setBounds(20,220,80,30);
            contentPane.add(randomStroke);
        }

        // 绘图版本体
        JPanel mainJPanel;
        {
            mainJPanel = new JPanel(){
                public void paint(Graphics graphics){


                    super.paint(graphics);

                    Graphics2D g2 = (Graphics2D) graphics;
                    // graphics.drawLine(100,0,1100,new Random().nextInt(100)+200);
                    int[] xs;
                    int[] ys;
                    for (int i = 0; i < DrawsCollector.lines.size(); i++) {


                        g2.setStroke(DrawsCollector.strokes.get(i));
                        g2.setColor(DrawsCollector.colors.get(i));

                        ArrayList<Point> line = DrawsCollector.lines.get(i);
                        int lineDotSize = line.size();
                        if(lineDotSize<2)continue;
                        xs = new int[lineDotSize];
                        ys = new int[lineDotSize];
                        for (int j = 0; j < lineDotSize; j++) {
                            xs[j] = line.get(j).x;
                            ys[j] = line.get(j).y;
                        }
                        graphics.drawPolyline(xs,ys,lineDotSize);
                    }
                }
            };

            mainJPanel.setBounds(0,0,1000,1000);
            contentPane.add(mainJPanel);

            // 测试刷新指令

//            new AutoRepaint(mainJPanel, buttonGroup).start();
        }

        // 清空按钮监听器
        {
            clearBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DrawsCollector.clear();
                    mainJPanel.repaint();
                }
            });
        }

        // 随机颜色监听器
        {
            randomColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Random random = new Random();
                    DrawsCollector.setColor(new Color(
                            random.nextInt(256),
                            random.nextInt(256),
                            random.nextInt(256)));
                    randomColor.setForeground(DrawsCollector.getCurrentColor());
                }
            });
        }

        // 随机粗细监听器
        {
            randomStroke.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Random random = new Random();
                    float b = random.nextFloat()*4+1;
                    DrawsCollector.setStroke(new BasicStroke(b));
                    randomStroke.setText(new DecimalFormat(".00").format(b));
                }
            });
        }

        // 鼠标事件监听器
        {
            // 移动监听
            {
                MouseMotionListener mouseMotionListener = new MouseMotionListener() {

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        DrawsCollector.addPoint(e.getPoint());
                        mainJPanel.repaint();
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if(e.getPoint().x<150&&e.getPoint().y<120) { // penStyle
                            penStyleChooser.repaint();
                        }else if(e.getPoint().x<150&&e.getPoint().y>315&&e.getPoint().y<355){ // clear
                            clearBtn.repaint();
                        }else if(e.getPoint().x<150&&e.getPoint().y>265&&e.getPoint().y<305){ // random color
                            randomColor.repaint();
                        }else if(e.getPoint().x<150&&e.getPoint().y>215&&e.getPoint().y<255){ // random stroke
                            randomStroke.repaint();
                        } else if(DrawsCollector.lines.size()>0){
                            mainJPanel.repaint();
                        }
                    }


                };
                mainJPanel.addMouseMotionListener(mouseMotionListener);
            }
            // 点击监听
            {
                MouseListener mouseListener = new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        DrawsCollector.addPoint(e.getPoint());
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        DrawsCollector.newLine();
                        if(e.getPoint().x<150&&e.getPoint().y<120) {
                            penStyleChooser.repaint();
                        }
                    }


                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                };
                mainJPanel.addMouseListener(mouseListener);
            }
        }
        jFrame.setVisible(true);
        mainJPanel.repaint();

    }


    public static class DrawsCollector {
        public static ArrayList<ArrayList<Point>> lines = new ArrayList<>();
        private static ArrayList<Point> currentLine;
        public static ArrayList<Color> colors = new ArrayList<>();
        private static Color currentColor = new Color(0x0099C5);
        public static Color getCurrentColor() {
            return currentColor;
        }
        public static ArrayList<Stroke> strokes = new ArrayList<>();
        private static Stroke currentStroke = new BasicStroke(1f);
        private static boolean isNewLine = true;
        public static void addPoint(Point p){
            if(isNewLine){
                currentLine = new ArrayList<>();
                lines.add(currentLine);
                colors.add(currentColor);
                strokes.add(currentStroke);
                isNewLine = false;
            }
            currentLine.add(p);

            if(penStyleId == 1){
                if(currentLine.size()==5){
                    newLine();
                }
            }

            if(penStyleId == 2){
                if(currentLine.size()==2){
                    newLine();
                }
            }
        }
        public static void newLine(){
            isNewLine = true;
        }
        public static void clear(){
            lines = new ArrayList<>();
            colors = new ArrayList<>();
            strokes = new ArrayList<>();
            currentColor = new Color(0x0099C5);
            currentStroke = new BasicStroke(1f);
            currentLine = null;
            newLine();
        }

        public static void setStroke(Stroke stroke){
            currentStroke = stroke;
        }
        public static void setColor(Color color){
            currentColor = color;
        }
    }

    private static boolean fSleep(long ms){
        try {
            Thread.sleep(ms);
        }catch (Exception e){}
        return true;
    }
}