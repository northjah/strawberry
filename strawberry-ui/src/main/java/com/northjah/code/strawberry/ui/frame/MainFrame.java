package com.northjah.code.strawberry.ui.frame;

import com.northjah.code.strawberry.api.exception.ApplicationException;
import com.northjah.code.strawberry.ui.handler.jcef.AlertDialogHandler;
import com.northjah.code.strawberry.ui.util.ApplicationUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MainFrame extends JFrame {

    private CefBrowser browser;
    private CefClient client;
    private CefApp cefApp;

    // 将配置常量化
    private static final String START_URL = "file:///static/index.html";
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 750;


    private static final String CACHE_DIR = "cache";
    private static final String INSTALL_DIR = "V8";

    public MainFrame() {
        // 1. 初始化基础窗体设置 (非耗时操作)
        setupFrameSettings();
    }

    private void setupFrameSettings() {
        setTitle("Strawberry Desktop - JCEF");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        List<Image> icons = new ArrayList<>();
        icons.add(new ImageIcon(new File("static/icon/16.png").getAbsolutePath()).getImage());
        icons.add(new ImageIcon(new File("static/icon/32.png").getAbsolutePath()).getImage());
        icons.add(new ImageIcon(new File("static/icon/48.png").getAbsolutePath()).getImage());
        icons.add(new ImageIcon(new File("static/icon/64.png").getAbsolutePath()).getImage());
        icons.add(new ImageIcon(new File("static/icon/128.png").getAbsolutePath()).getImage());
        icons.add(new ImageIcon(new File("static/icon/256.png").getAbsolutePath()).getImage());
        setIconImages(icons);
    }

    /**
     * Spring 完成依赖注入后自动调用
     * 在这里处理耗时的 JCEF 初始化
     */
    @PostConstruct
    public void init() {
        // 使用 SwingUtilities 确保在事件分发线程中构建 UI
        SwingUtilities.invokeLater(() -> {
            try {
                if (initJcefInfrastructure()) {
                    createBrowserAndMount();
                    setVisible(true);
                }
            } catch (UnsupportedPlatformException | CefInitializationException | IOException | InterruptedException e) {
                throw new ApplicationException(e);
            }
        });
    }

    private boolean initJcefInfrastructure() throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        CefAppBuilder builder = new CefAppBuilder();
        builder.setProgressHandler(new ConsoleProgressHandler());
        builder.getCefSettings().windowless_rendering_enabled = Boolean.FALSE;

        builder.getCefSettings().cache_path = ApplicationUtils.getCurrentDir() + File.separator + CACHE_DIR;
        builder.setSkipInstallation(Boolean.TRUE);
        builder.setInstallDir(new File(INSTALL_DIR));



        // 设置 AppHandler
        builder.setAppHandler(new me.friwi.jcefmaven.MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(CefApp.CefAppState state) {
                if (state == CefApp.CefAppState.TERMINATED) {
                    System.exit(0);
                }
            }
        });

        // 这里的 build() 包含下载和解压 native 库，是耗时操作
        cefApp = builder.build();
        client = cefApp.createClient();

        AlertDialogHandler dialogHandler = new AlertDialogHandler();
        client.addJSDialogHandler(dialogHandler);

        return true;
    }

    private void createBrowserAndMount() {
        // 创建浏览器实例
        browser = client.createBrowser(START_URL, false, false);

        // 获取 UI 组件并添加到 Center
        java.awt.Component uiComponent = browser.getUIComponent();
        add(uiComponent, BorderLayout.CENTER);

        // 刷新 UI
        revalidate();
        repaint();
    }

    @PreDestroy
    public void cleanup() {
        log.info("cleanup...");
        // 按照 浏览器 -> 客户端 -> App 的顺序释放
        if (browser != null) {
            browser.close(true);
        }
        if (client != null) {
            client.dispose();
        }
        if (cefApp != null) {
            // 注意：某些环境下 CefApp.dispose() 可能会阻塞
            cefApp.dispose();
        }
    }
}