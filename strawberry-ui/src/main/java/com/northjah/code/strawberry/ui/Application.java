package com.northjah.code.strawberry.ui;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;
import com.northjah.code.strawberry.ui.frame.MainFrame;
import com.northjah.code.strawberry.ui.util.ApplicationUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class Application {

    static void main(String[] args) {

        //flatlaf
        init();


        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Application.class)
                .headless(false)
                .run(args);

        ctx.getBean(MainFrame.class);


    }


    private static void init() {
        //https://www.formdev.com/flatlaf/components/titlebar/
        UIManager.put("TitlePane.buttonSize", new Dimension(50 ,50));
        // 1. 必须设置为 true，否则图标不会显示
        UIManager.put("TitlePane.showIcon", true);

// 2. 设置图标的大小 (默认通常是 16x16)
        UIManager.put("TitlePane.iconSize", new Dimension(32, 32));
// 这里的 24 就是字体大小，你可以根据需要改成 30, 40 等
       UIManager.put("TitlePane.font", new Font("sans-serif", Font.BOLD, 15));
// 3. 设置图标的边距 (上, 左, 下, 右)
    //    UIManager.put("TitlePane.iconMargins", new Insets(5, 10, 5, 5));

// 4. (可选) 如果你想让图标紧挨着标题文字（而不是在最左角），开启这个：
       // UIManager.put("TitlePane.showIconBesideTitle", true);
        if (ApplicationUtils.isAppDarkMode()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

}
