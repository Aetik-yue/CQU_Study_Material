#define SOFTFLOAT_NO_CONSOLE_MAIN
#include "softfloat_ieee754.c"

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <string.h>

#define IDC_INPUT_A 101
#define IDC_INPUT_B 102
#define IDC_OUTPUT  103
#define IDC_CONVERT 201
#define IDC_ADD     202
#define IDC_SUB     203
#define IDC_MUL     204
#define IDC_DIV     205

static HWND g_input_a;
static HWND g_input_b;
static HWND g_output;
static HFONT g_font;

static void get_ascii_text(HWND hwnd, char *buf, int size) {
    wchar_t wbuf[128];
    int i;
    int n;

    if (size <= 0) return;
    n = GetWindowTextW(hwnd, wbuf, 128);
    for (i = 0; i < n && i < size - 1; i++) {
        wchar_t ch = wbuf[i];
        buf[i] = (ch < 128) ? (char)ch : ' ';
    }
    buf[i] = '\0';
}

static void binary_string(u32 v, char *buf, int size) {
    int i;
    int pos = 0;

    if (size <= 0) return;
    for (i = 31; i >= 0 && pos < size - 1; i--) {
        buf[pos++] = (v & ((u32)1 << i)) ? '1' : '0';
        if ((i == 31 || i == 23) && pos < size - 1) {
            buf[pos++] = ' ';
        }
    }
    buf[pos] = '\0';
}

static void append_bits(char *out, int size, const char *name, u32 v) {
    char bin[40];
    int len;

    binary_string(v, bin, sizeof(bin));
    len = (int)strlen(out);
    snprintf(out + len, size - len, "%s HEX : 0x%08X\r\n%s BIN : %s\r\n", name, v, name, bin);
}

static void set_result_text(const char *text) {
    wchar_t wtext[2048];
    MultiByteToWideChar(CP_UTF8, 0, text, -1, wtext, 2048);
    SetWindowTextW(g_output, wtext);
}

static void calculate(int mode) {
    char sa[128];
    char sb[128];
    char out[2048];
    u32 a;
    u32 b;
    u32 r;

    out[0] = '\0';
    get_ascii_text(g_input_a, sa, sizeof(sa));
    a = parse_decimal(sa);

    if (mode == IDC_CONVERT) {
        append_bits(out, sizeof(out), "结果", a);
        set_result_text(out);
        return;
    }

    get_ascii_text(g_input_b, sb, sizeof(sb));
    b = parse_decimal(sb);
    if (mode == IDC_ADD) {
        r = fadd_soft(a, b);
    } else if (mode == IDC_SUB) {
        r = fsub_soft(a, b);
    } else if (mode == IDC_MUL) {
        r = fmul_soft(a, b);
    } else {
        r = fdiv_soft(a, b);
    }

    append_bits(out, sizeof(out), "操作数A", a);
    append_bits(out, sizeof(out), "操作数B", b);
    append_bits(out, sizeof(out), "运算结果", r);
    set_result_text(out);
}

static void apply_font(HWND hwnd) {
    SendMessageW(hwnd, WM_SETFONT, (WPARAM)g_font, TRUE);
}

static HWND add_child(HWND parent, const wchar_t *class_name, const wchar_t *text, DWORD style,
                      int x, int y, int w, int h, int id) {
    HWND hwnd = CreateWindowExW(
        0, class_name, text, WS_CHILD | WS_VISIBLE | style,
        x, y, w, h, parent, (HMENU)(INT_PTR)id, GetModuleHandleW(NULL), NULL);
    apply_font(hwnd);
    return hwnd;
}

static LRESULT CALLBACK window_proc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
    (void)lp;

    switch (msg) {
    case WM_CREATE:
        g_font = CreateFontW(
            18, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE, DEFAULT_CHARSET,
            OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, CLEARTYPE_QUALITY,
            DEFAULT_PITCH | FF_SWISS, L"Microsoft YaHei UI");

        add_child(hwnd, L"STATIC", L"IEEE 754 单精度浮点数软件仿真", 0, 20, 16, 520, 28, 0);
        add_child(hwnd, L"STATIC", L"输入 A：", 0, 20, 58, 80, 24, 0);
        g_input_a = add_child(hwnd, L"EDIT", L"1.5", WS_BORDER | ES_AUTOHSCROLL, 100, 54, 180, 28, IDC_INPUT_A);
        add_child(hwnd, L"STATIC", L"输入 B：", 0, 300, 58, 80, 24, 0);
        g_input_b = add_child(hwnd, L"EDIT", L"2.25", WS_BORDER | ES_AUTOHSCROLL, 380, 54, 180, 28, IDC_INPUT_B);

        add_child(hwnd, L"BUTTON", L"转换 A", 0, 20, 102, 96, 34, IDC_CONVERT);
        add_child(hwnd, L"BUTTON", L"A + B", 0, 128, 102, 82, 34, IDC_ADD);
        add_child(hwnd, L"BUTTON", L"A - B", 0, 222, 102, 82, 34, IDC_SUB);
        add_child(hwnd, L"BUTTON", L"A × B", 0, 316, 102, 82, 34, IDC_MUL);
        add_child(hwnd, L"BUTTON", L"A ÷ B", 0, 410, 102, 82, 34, IDC_DIV);

        g_output = add_child(hwnd, L"EDIT", L"", WS_BORDER | ES_MULTILINE | ES_READONLY | WS_VSCROLL,
                             20, 158, 540, 210, IDC_OUTPUT);
        calculate(IDC_CONVERT);
        return 0;

    case WM_COMMAND:
        switch (LOWORD(wp)) {
        case IDC_CONVERT:
        case IDC_ADD:
        case IDC_SUB:
        case IDC_MUL:
        case IDC_DIV:
            calculate(LOWORD(wp));
            return 0;
        }
        break;

    case WM_DESTROY:
        if (g_font) DeleteObject(g_font);
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProcW(hwnd, msg, wp, lp);
}

int WINAPI WinMain(HINSTANCE hinst, HINSTANCE prev, LPSTR cmd, int show) {
    WNDCLASSW wc;
    HWND hwnd;
    MSG msg;

    (void)prev;
    (void)cmd;

    memset(&wc, 0, sizeof(wc));
    wc.lpfnWndProc = window_proc;
    wc.hInstance = hinst;
    wc.lpszClassName = L"SoftFloatGuiClass";
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);

    if (!RegisterClassW(&wc)) return 1;

    hwnd = CreateWindowExW(
        0, wc.lpszClassName, L"IEEE 754 单精度浮点数软件仿真",
        WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX,
        CW_USEDEFAULT, CW_USEDEFAULT, 600, 430,
        NULL, NULL, hinst, NULL);
    if (!hwnd) return 1;

    ShowWindow(hwnd, show);
    UpdateWindow(hwnd);

    while (GetMessageW(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }
    return (int)msg.wParam;
}
