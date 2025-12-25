import javax.swing.*;
import java.awt.*;

public class SideBottomBlueGlowPanel extends JPanel {

    private static final int EDGE = 6;
    private static final Color RED = Color.RED;

    public SideBottomBlueGlowPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false); // 中间透明
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // 左边
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, EDGE, h);

        // 右边
        g2.fillRect(w - EDGE, 0, EDGE, h);

        // 底部
        g2.fillRect(0, h - EDGE, w, EDGE);

        g2.dispose();
    }
}
