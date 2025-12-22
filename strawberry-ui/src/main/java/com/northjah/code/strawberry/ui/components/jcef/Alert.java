package com.northjah.code.strawberry.ui.components.jcef;

import org.cef.CefApp;
import org.cef.CefBrowserSettings;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Alert extends JDialog {

    // 保持三个入参不变：parent, title, message
    public Alert(JFrame parent, String title, String message) {
        super(parent, true);
        setUndecorated(true);
        setBackground(Color.BLACK);
        // 1. 外部装饰容器
        JPanel shadowContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制阴影，Math.max 防止 Alpha 越界
                for (int i = 0; i < 6; i++) {
                    int alpha = Math.max(0, 5 - i);
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRoundRect(8 - i, 8 - i, getWidth() - (8 - i) * 2, getHeight() - (8 - i) * 2, 12, 12);
                }

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 12, 12);
                g2.dispose();
            }
        };
        shadowContainer.setOpaque(false);

        shadowContainer.setBorder(new EmptyBorder(25, 30, 10, 30));

        // 2. JCEF 渲染
        CefClient client = CefApp.getInstance().createClient();


        String html = "<html>" + "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { background-color:#FFFFFF;margin: 0; padding: 0; background: transparent; font-family: Console; overflow: hidden; }" +
                "    .message { color: #3C4043; font-size: 14px; line-height: 1.6; word-wrap: break-word; " +
                "               max-height: 180px; overflow-y: auto; text-align: left; }" +
                "    ::-webkit-scrollbar { width: 4px; } ::-webkit-scrollbar-thumb { background: #dadce0; border-radius: 4px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  " +
                "  <div class='message'>" + message + "</div>" +
                "</body></html>";

        String encodedHtml = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
        CefBrowser browser = client.createBrowser("data:text/html;base64," + encodedHtml, false, false);

        // 3. 原生按钮区域
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        footer.setOpaque(false);

        JButton btnOk = new JButton("确定");
        btnOk.setPreferredSize(new Dimension(85, 32));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
       btnOk.setBackground(new Color(26, 115, 232));
       btnOk.setForeground(Color.WHITE);

        // 获取当前字号并增加 2px，同时加粗
        float newSize = UIManager.getFont("defaultFont").getSize() + 2.0f;
        btnOk.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, newSize));
        // btnOk.setFont();
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnOk.addActionListener(e -> dispose());
        footer.add(btnOk);

        // 4. 组装布局
        shadowContainer.setBackground(Color.BLACK);
        setBackground(Color.BLACK);
        shadowContainer.add(browser.getUIComponent(), BorderLayout.CENTER);
        shadowContainer.add(footer, BorderLayout.SOUTH);

        add(shadowContainer);

        // 5. 窗口大小与定位
        setSize(400, 150);
        if (parent != null && parent.isShowing()) {
            Point p = parent.getContentPane().getLocationOnScreen();
            int x = p.x + (parent.getContentPane().getWidth() - getWidth()) / 2;
            int y = p.y + 20;
            setLocation(x, y);
        }
    }
}