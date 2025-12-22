package com.northjah.code.strawberry.ui.handler.jcef;


import com.northjah.code.strawberry.ui.components.jcef.Alert;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandlerAdapter;
import org.cef.misc.BoolRef;

import javax.swing.*;
import java.awt.*;

public class AlertDialogHandler extends CefJSDialogHandlerAdapter {

    @Override
    public boolean onJSDialog(CefBrowser browser,
                              String origin_url,
                              JSDialogType dialog_type,
                              String message_text,
                              String default_prompt_text,
                              CefJSDialogCallback callback,
                              BoolRef suppress_message) {


        // 动态寻找父窗口
        Window window = SwingUtilities.getWindowAncestor(browser.getUIComponent());
        JFrame mainFrame = (window instanceof JFrame) ? (JFrame) window : null;


        // 2. 使用 Java Swing 组件替换原生浏览器弹窗
        SwingUtilities.invokeLater(() -> {
            boolean handled;
            String userInput = null;

            handled = switch (dialog_type) {
                case JSDIALOGTYPE_ALERT -> {
                   JOptionPane.showMessageDialog(null, message_text, "来自页面的消息", JOptionPane.INFORMATION_MESSAGE);
                    yield true;
                   /* Alert alert = new Alert(mainFrame, null, message_text);
                    alert.setVisible(true);
                    yield true;*/
                }
                case JSDIALOGTYPE_CONFIRM -> {
                    int res = JOptionPane.showConfirmDialog(null, message_text, "请确认", JOptionPane.YES_NO_OPTION);
                    yield (res == JOptionPane.YES_OPTION);
                }
                case JSDIALOGTYPE_PROMPT -> {
                    userInput = JOptionPane.showInputDialog(null, message_text, default_prompt_text);
                    yield (userInput != null);
                }
            };

            callback.Continue(handled, userInput);
        });

        // 返回 true 表示我们已经接管了该弹窗，浏览器不再弹出默认对话框
        return true;
    }
}