import javax.swing.*;
import java.awt.*;

public class FramelessWindowDemo extends JFrame {
    static {

        System.load("C:\\Users\\xiaoyi\\Desktop\\project\\Project1\\x64\\Release\\Project1.dll");
   }

    // native 方法，直接接收当前的 JFrame 对象
    private native void attachSnap(JFrame frame);

    public FramelessWindowDemo() {
        setUndecorated(true);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 你的自定义标题栏面板
        JPanel titleBar = new JPanel();
        titleBar.setBackground(Color.DARK_GRAY);
        titleBar.setPreferredSize(new Dimension(800, 35));
        add(titleBar, BorderLayout.NORTH);

        // 核心：窗口可见后，句柄才真正创建，此时调用 native
        setVisible(true);
        attachSnap(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FramelessWindowDemo::new);
    }
}