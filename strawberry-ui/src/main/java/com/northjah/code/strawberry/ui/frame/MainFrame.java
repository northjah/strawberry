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
import java.awt.event.ActionEvent;
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


        // 创建菜单栏
        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 创建菜单：File
        JMenu fileMenu = new JMenu("File");

        // 创建菜单项
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener((ActionEvent e) -> System.out.println("Open clicked"));

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener((ActionEvent e) -> System.out.println("Save clicked"));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((ActionEvent e) -> System.exit(0));

        // 将菜单项添加到File菜单
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();  // 分隔线
        fileMenu.add(exitItem);

        // 创建菜单：Edit
        JMenu editMenu = new JMenu("Edit");

        // 创建菜单项
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.addActionListener((ActionEvent e) -> System.out.println("Cut clicked"));

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener((ActionEvent e) -> System.out.println("Copy clicked"));

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener((ActionEvent e) -> System.out.println("Paste clicked"));

        // 将菜单项添加到Edit菜单
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        // 创建菜单：Help
        JMenu helpMenu = new JMenu("Help");

        // 创建菜单项
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener((ActionEvent e) -> System.out.println("About clicked"));

        // 将菜单项添加到Help菜单
        helpMenu.add(aboutItem);

        // 将所有菜单添加到菜单栏
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        // 将菜单栏设置到窗口
        setJMenuBar(menuBar);

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
        browser = client.createBrowser("https://www.baidu.com", false, false);



        client.addLifeSpanHandler(new org.cef.handler.CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, org.cef.browser.CefFrame frame, String target_url, String target_frame_name) {
                // 核心逻辑：拦截弹出请求，让当前浏览器加载目标 URL
                browser.loadURL(target_url);
                // 返回 true 表示我们已经处理了该请求，阻止 CEF 创建新窗口
                return true;
            }
        });

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