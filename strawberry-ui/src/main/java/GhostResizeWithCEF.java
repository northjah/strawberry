import com.formdev.flatlaf.FlatLightLaf;
import com.northjah.code.strawberry.ui.util.ApplicationUtils;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GhostResizeWithCEF extends JFrame {
    static {
/*#include <windows.h>
#include <windowsx.h>
#include <jni.h>
#include <jawt_md.h>

        WNDPROC oldWndProc = nullptr;
        LONG originalStyle = 0;
const int TITLE_HEIGHT = 36; // Java 标题栏高度

        LRESULT CALLBACK CustomWndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
            switch (msg) {
                case WM_NCCALCSIZE:
                    return 0; // 去掉系统边框
                case WM_NCHITTEST: {
                    POINT pt = { GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam) };
                    ScreenToClient(hwnd, &pt);

                    if (pt.y >= 2 && pt.y < TITLE_HEIGHT) {
                        return HTCAPTION; // 只有标题栏触发 Snap
                    }
                    return HTCLIENT; // 其他区域交给 Java 幽灵窗口
                }
            }
            return CallWindowProc(oldWndProc, hwnd, msg, wParam, lParam);
        }

        extern "C" {

            // 鼠标按住标题栏 → 强制追加 Snap 样式
            JNIEXPORT void JNICALL Java_GhostResizeWithCEF_attachSnap(JNIEnv* env, jobject obj, jobject frame) {
                JAWT awt;
                JAWT_DrawingSurface* ds;
                JAWT_DrawingSurfaceInfo* dsi;
                JAWT_Win32DrawingSurfaceInfo* dsi_win;
                jint lock;

                awt.version = JAWT_VERSION_9;
                if (JAWT_GetAWT(env, &awt) == JNI_FALSE) return;

                ds = awt.GetDrawingSurface(env, frame);
                if (!ds) return;

                lock = ds->Lock(ds);
                if (!(lock & JAWT_LOCK_ERROR)) {
                    dsi = ds->GetDrawingSurfaceInfo(ds);
                    dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
                    HWND hwnd = dsi_win->hwnd;
                    jclass systemClass = env->FindClass("java/lang/System");
                    jfieldID outField = env->GetStaticFieldID(systemClass, "out", "Ljava/io/PrintStream;");
                    jobject outObj = env->GetStaticObjectField(systemClass, outField);

                    // 2. 获取 PrintStream.println(String) 方法ID
                    jclass printStreamClass = env->FindClass("java/io/PrintStream");
                    jmethodID printlnMethod = env->GetMethodID(printStreamClass, "println", "(Ljava/lang/String;)V");

                    // 3. 创建字符串
                    jstring msg = env->NewStringUTF("attachSnap called from JNI!");

                    // 4. 调用 println
                    env->CallVoidMethod(outObj, printlnMethod, msg);

                    // 5. 释放局部引用
                    env->DeleteLocalRef(msg);
                    if (hwnd) {
                        // 保存原始样式
                        originalStyle = GetWindowLong(hwnd, GWL_STYLE);

                        // 强制追加 Snap 支持
                        LONG style = originalStyle;
                        style |= WS_CAPTION | WS_THICKFRAME | WS_MAXIMIZEBOX | WS_MINIMIZEBOX;
                        SetWindowLong(hwnd, GWL_STYLE, style);


                        // 设置自定义 WndProc
                        oldWndProc = (WNDPROC)SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)CustomWndProc);

                        // 更新窗口样式
                        SetWindowPos(hwnd, NULL, 0, 0, 0, 0,
                                SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER);


                    }

                    ds->FreeDrawingSurfaceInfo(dsi);
                    ds->Unlock(ds);
                }

                awt.FreeDrawingSurface(ds);
            }

        }*/
        System.load("C:\\Users\\xiaoyi\\Desktop\\project\\Project1\\x64\\Release\\Project1.dll");
    }

    // native 方法，直接接收当前的 JFrame 对象
    private native void attachSnap(JFrame frame);

    private native void restoreSnap(JFrame frame);
    // ===== 窗口参数 =====
    private static final int BORDER = 6;
    private static final int TITLE_HEIGHT = 36;
    private static final int MIN_W = 600;
    private static final int MIN_H = 400;

    // ===== 状态 =====
    private ResizeDir dir = ResizeDir.NONE;
    private Rectangle startBounds;
    private Point startMouse;
    private GhostWindow ghost;

    // ===== CEF =====
    private CefBrowser browser;
    private Component browserUI;

    public GhostResizeWithCEF() {
        setUndecorated(true);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        /* ================= 标题栏 ================= */
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(10, TITLE_HEIGHT));
        titleBar.setBackground(new Color(45, 45, 45));

        JLabel title = new JLabel("  Ghost Resize + JCEF（完整示例）");
        title.setForeground(Color.WHITE);
        titleBar.add(title, BorderLayout.WEST);


        add(titleBar, BorderLayout.NORTH);

        /* ================= 内容区 ================= */
        JPanel contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        /* ================= 初始化 JCEF ================= */

        CefAppBuilder builder = new CefAppBuilder();
        builder.setProgressHandler(new ConsoleProgressHandler());
        builder.getCefSettings().windowless_rendering_enabled = Boolean.FALSE;

        builder.getCefSettings().cache_path = ApplicationUtils.getCurrentDir() + File.separator + "cache";
        builder.setSkipInstallation(Boolean.TRUE);
        builder.setInstallDir(new File("v8"));

        CefApp cefApp = null;
        CefClient client;
        try {
            cefApp= builder.build();
        }catch (Exception e){
            e.printStackTrace();
        }

         client = cefApp.createClient();

        browser = client.createBrowser(
                "https://www.baidu.com",
                false,   // window rendering
                false
        );

        JPanel contentRoot = new JPanel(new BorderLayout());
        contentRoot.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        add(contentRoot, BorderLayout.CENTER);

        JPanel cefHolder = new JPanel(new BorderLayout());
        contentRoot.add(cefHolder, BorderLayout.CENTER);

        browserUI = browser.getUIComponent();
        cefHolder.add(browserUI, BorderLayout.CENTER);



        /* ================= 鼠标控制 ================= */

/**
 * 统一处理窗口拖动和大小调整的鼠标事件
 * - mouseMoved: 改变鼠标指针样式
 * - mousePressed: 开始拖动或调整大小
 * - mouseDragged: 实时更新幽灵边框
 * - mouseReleased: 拖动结束，应用新的窗口边界
 */
        MouseAdapter adapter = new MouseAdapter() {

            /**
             * 鼠标移动事件
             * - 根据当前鼠标位置判断方向
             * - 更新鼠标指针样式
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(cursorFor(detectDir(e.getPoint())));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dir = detectDir(e.getPoint());
                startBounds = getBounds();
                startMouse = e.getLocationOnScreen();

                // 只有边缘拖动才创建幽灵窗口
                if (dir != ResizeDir.NONE && dir != ResizeDir.MOVE) {
                    ghost = new GhostWindow();
                    ghost.update(startBounds);
                    ghost.setVisible(true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getLocationOnScreen();
                int dx = p.x - startMouse.x;
                int dy = p.y - startMouse.y;

                if (dir == ResizeDir.MOVE) {
                    // 标题栏拖动：直接移动窗口，不创建幽灵
                    setLocation(startBounds.x + dx, startBounds.y + dy);
                    return;
                }

                if (ghost == null) return; // 没有幽灵窗口说明不在调整大小

                Rectangle r = new Rectangle(startBounds);
                switch (dir) {
                    case E -> r.width = Math.max(MIN_W, startBounds.width + dx);
                    case S -> r.height = Math.max(MIN_H, startBounds.height + dy);
                    case W -> resizeLeft(r, dx);
                    case N -> resizeTop(r, dy);
                    case SE -> { r.width = Math.max(MIN_W, startBounds.width + dx); r.height = Math.max(MIN_H, startBounds.height + dy); }
                    case SW -> { resizeLeft(r, dx); r.height = Math.max(MIN_H, startBounds.height + dy); }
                    case NE -> { resizeTop(r, dy); r.width = Math.max(MIN_W, startBounds.width + dx); }
                    case NW -> { resizeLeft(r, dx); resizeTop(r, dy); }
                }
                ghost.update(r);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (ghost != null) {
                    Rectangle r = ghost.getBounds();
                    ghost.dispose();
                    ghost = null;
                    setBounds(r);
                    browserUI.revalidate();
                    browserUI.repaint();
                }
                dir = ResizeDir.NONE;
            }

        };




        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        titleBar.addMouseListener(adapter);
        titleBar.addMouseMotionListener(adapter);

        setVisible(true);
        attachSnap(this);
    }

    /* ================= Resize 修正算法 ================= */
    /**
     * 调整窗口左边缘的大小
     *
     * 原理：
     * - 当鼠标向左拖动时，窗口左边缘随鼠标移动
     * - 宽度 = 原始宽度 - 鼠标水平移动距离 dx
     * - 同时更新窗口的 x 坐标，使右边界保持不动
     * - 如果达到最小宽度 MIN_W，固定宽度并调整 x 坐标，防止窗口跳动
     *
     * @param r 目标矩形，表示窗口的新边界（会被修改）
     * @param dx 鼠标在屏幕上的水平偏移量（当前位置 - 拖动起点）
     */
    private void resizeLeft(Rectangle r, int dx) {
        int newW = startBounds.width - dx; // 计算新宽度
        if (newW < MIN_W) {                // 达到最小宽度限制
            r.width = MIN_W;               // 固定最小宽度
            r.x = startBounds.x + (startBounds.width - MIN_W); // 调整 x，右边界保持不动
        } else {
            r.width = newW;                // 更新宽度
            r.x = startBounds.x + dx;      // x 坐标随鼠标移动
        }
    }

    /**
     * 调整窗口上边缘的大小
     *
     * 原理：
     * - 当鼠标向上拖动时，窗口上边缘随鼠标移动
     * - 高度 = 原始高度 - 鼠标垂直移动距离 dy
     * - 同时更新窗口的 y 坐标，使下边界保持不动
     * - 如果达到最小高度 MIN_H，固定高度并调整 y 坐标，防止窗口跳动
     *
     * @param r 目标矩形，表示窗口的新边界（会被修改）
     * @param dy 鼠标在屏幕上的垂直偏移量（当前位置 - 拖动起点）
     */
    private void resizeTop(Rectangle r, int dy) {
        int newH = startBounds.height - dy; // 计算新高度
        if (newH < MIN_H) {                  // 达到最小高度限制
            r.height = MIN_H;                // 固定最小高度
            r.y = startBounds.y + (startBounds.height - MIN_H); // 调整 y，保持下边界不动
        } else {
            r.height = newH;                 // 更新高度
            r.y = startBounds.y + dy;        // y 坐标随鼠标移动
        }
    }



    /* ================= 方向判断 ================= */

    /**
     * 根据鼠标在窗口中的位置，判断用户意图：
     * - 移动窗口
     * - 调整窗口大小（哪个方向）
     *
     * @param p 鼠标在窗口坐标系中的位置
     * @return ResizeDir 枚举，表示拖动/调整的方向
     */
    private ResizeDir detectDir(Point p) {
        int w = getWidth();
        int h = getHeight();

        // 判断鼠标是否靠近各边缘（BORDER 范围内）
        boolean left = p.x < BORDER;
        boolean right = p.x > w - BORDER;
        boolean top = p.y < BORDER;
        boolean bottom = p.y > h - BORDER;

        // 优先角落方向（角落可以同时调整水平和垂直大小）
        if (top && left) return ResizeDir.NW;  // 左上角
        if (top && right) return ResizeDir.NE; // 右上角
        if (bottom && left) return ResizeDir.SW; // 左下角
        if (bottom && right) return ResizeDir.SE; // 右下角

        // 边缘方向
        if (left) return ResizeDir.W;   // 左边缘
        if (right) return ResizeDir.E;  // 右边缘
        if (top) return ResizeDir.N;    // 上边缘
        if (bottom) return ResizeDir.S; // 下边缘

        // 标题栏拖动判断（非边缘但在标题栏高度内）
        if (p.y < TITLE_HEIGHT) return ResizeDir.MOVE; // 移动窗口

        // 默认：不在拖动或调整区域
        return ResizeDir.NONE;
    }

    /**
     * 根据 ResizeDir 枚举返回对应的鼠标指针
     * - 方便用户看到当前鼠标操作的反馈
     *
     * @param d ResizeDir 枚举方向
     * @return Cursor 对象，用于 setCursor
     */
    private Cursor cursorFor(ResizeDir d) {
        return switch (d) {
            case N -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);   // 上
            case S -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);   // 下
            case W -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);   // 左
            case E -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);   // 右
            case NW -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR); // 左上角
            case NE -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR); // 右上角
            case SW -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR); // 左下角
            case SE -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR); // 右下角
           // case MOVE -> Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);    // 移动窗口
            default -> Cursor.getDefaultCursor();                           // 默认箭头
        };
    }


    /* ================= 枚举 ================= */

    /**
     * 窗口拖动/调整大小的方向枚举
     *
     * 每个值表示鼠标在窗口上的位置或用户的操作意图，用于决定
     * 拖动时是移动窗口还是改变窗口尺寸，以及改变哪个方向。
     */
    private enum ResizeDir {
        /** 没有拖动或不在边缘，鼠标经过普通区域 */
        NONE,

        /** 鼠标在标题栏，拖动意味着移动整个窗口 */
        MOVE,

        /** 鼠标靠近上边缘，垂直向上调整窗口高度 */
        N,  // North

        /** 鼠标靠近下边缘，垂直向下调整窗口高度 */
        S,  // South

        /** 鼠标靠近左边缘，水平向左调整窗口宽度 */
        W,  // West

        /** 鼠标靠近右边缘，水平向右调整窗口宽度 */
        E,  // East

        /** 鼠标靠近左上角，水平和垂直同时调整窗口大小 */
        NW, // North-West

        /** 鼠标靠近右上角，水平和垂直同时调整窗口大小 */
        NE, // North-East

        /** 鼠标靠近左下角，水平和垂直同时调整窗口大小 */
        SW, // South-West

        /** 鼠标靠近右下角，水平和垂直同时调整窗口大小 */
        SE  // South-East
    }


    /* ================= 桌面幽灵窗口 ================= */

    private static class GhostWindow extends JWindow {
        GhostWindow() {
            setBackground(new Color(0, 0, 0, 0));
            setAlwaysOnTop(true);
        }

        void update(Rectangle r) {
            setBounds(r);
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(
                    2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[]{6},
                    0
            ));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        }
    }

    /* ================= 入口 ================= */

    public static void main(String[] args) {
      //  FlatLightLaf.setup();
        SwingUtilities.invokeLater(GhostResizeWithCEF::new);
    }
}
