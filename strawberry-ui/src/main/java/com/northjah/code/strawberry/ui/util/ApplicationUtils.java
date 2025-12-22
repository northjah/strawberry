package com.northjah.code.strawberry.ui.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationUtils {
    public static String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    //0 true暗色 1 false亮色
    public static boolean isAppDarkMode() {
        String path = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        String key = "AppsUseLightTheme";

        try {
            int result = Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_CURRENT_USER, path, key
            );

            return (result == 0);

        } catch (Exception e) {
            log.info("ApplicationUtils->isAppDarkMode 发生异常: ", e);
            //获取失败 亮色
            return Boolean.FALSE;
        }
    }

}
