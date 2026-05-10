#include <stdint.h>
#include <stdio.h>
#ifdef _WIN32
#include <windows.h>
#endif

typedef uint32_t u32;
typedef uint64_t u64;

typedef struct {
    int sign;
    int exp;
    u32 sig;
    int zero;
    int inf;
    int nan;
} FloatParts;

static u64 shift_right_jam(u64 v, int dist) {
    if (dist <= 0) return v;
    if (dist < 64) {
        u64 mask = ((u64)1 << dist) - 1;
        return (v >> dist) | ((v & mask) != 0);
    }
    return v != 0;
}

static u32 pack_nan(void) {
    return 0x7FC00000u;
}

static u32 pack_inf(int sign) {
    return ((u32)sign << 31) | 0x7F800000u;
}

static FloatParts unpack(u32 a) {
    FloatParts p;
    u32 exp = (a >> 23) & 0xFFu;
    u32 frac = a & 0x7FFFFFu;
    p.sign = (int)(a >> 31);
    p.exp = 0;
    p.sig = 0;
    p.zero = 0;
    p.inf = 0;
    p.nan = 0;

    if (exp == 0xFFu) {
        p.inf = (frac == 0);
        p.nan = (frac != 0);
        return p;
    }
    if (exp == 0 && frac == 0) {
        p.zero = 1;
        p.exp = -126;
        return p;
    }
    if (exp == 0) {
        p.exp = -126;
        p.sig = frac;
        while ((p.sig & 0x800000u) == 0) {
            p.sig <<= 1;
            p.exp--;
        }
        return p;
    }
    p.exp = (int)exp - 127;
    p.sig = 0x800000u | frac;
    return p;
}

static u32 round_pack(int sign, int exp, u64 sig_extra) {
    u64 rounded;
    u32 round_bits;

    if (exp < -126) {
        sig_extra = shift_right_jam(sig_extra, -126 - exp);
        exp = -126;
    }

    round_bits = (u32)(sig_extra & 0xFFu);
    rounded = sig_extra >> 8;
    if (round_bits > 0x80u || (round_bits == 0x80u && (rounded & 1u))) {
        rounded++;
    }
    if (rounded >= 0x1000000u) {
        rounded >>= 1;
        exp++;
    }
    if (exp > 127) {
        return pack_inf(sign);
    }
    if (rounded == 0) {
        return (u32)sign << 31;
    }
    if (exp == -126 && rounded < 0x800000u) {
        return ((u32)sign << 31) | (u32)rounded;
    }
    return ((u32)sign << 31) | ((u32)(exp + 127) << 23) | ((u32)rounded & 0x7FFFFFu);
}

static u32 rational_to_float(int sign, u64 num, u64 den) {
    u64 n, d, rem;
    u32 sig;
    int exp;
    int bit;

    if (num == 0) return (u32)sign << 31;
    if (den == 0) return pack_inf(sign);

    n = num;
    d = den;
    exp = 0;
    while (d <= (UINT64_MAX >> 1) && n >= (d << 1)) {
        d <<= 1;
        exp++;
    }
    while (n < d && n <= (UINT64_MAX >> 1)) {
        n <<= 1;
        exp--;
    }

    sig = 0x800000u;
    rem = n - d;
    for (bit = 22; bit >= 0; bit--) {
        rem <<= 1;
        if (rem >= d) {
            sig |= (u32)1 << bit;
            rem -= d;
        }
    }
    rem <<= 1;
    if (rem > d || (rem == d && (sig & 1u))) {
        sig++;
    }
    if (sig == 0x1000000u) {
        sig >>= 1;
        exp++;
    }
    if (exp > 127) return pack_inf(sign);
    if (exp < -149) return (u32)sign << 31;
    if (exp < -126) {
        return round_pack(sign, exp, (u64)sig << 8);
    }
    return ((u32)sign << 31) | ((u32)(exp + 127) << 23) | (sig & 0x7FFFFFu);
}

static u32 parse_decimal(const char *s) {
    int sign = 0;
    u64 int_part = 0;
    u64 frac_part = 0;
    u64 scale = 1;
    int frac_digits = 0;

    if (*s == '-') {
        sign = 1;
        s++;
    } else if (*s == '+') {
        s++;
    }

    while (*s >= '0' && *s <= '9') {
        if (int_part <= UINT64_MAX / 10) {
            int_part = int_part * 10 + (u64)(*s - '0');
        }
        s++;
    }
    if (*s == '.') {
        s++;
        while (*s >= '0' && *s <= '9') {
            if (frac_digits < 9) {
                frac_part = frac_part * 10 + (u64)(*s - '0');
                scale *= 10;
                frac_digits++;
            }
            s++;
        }
    }
    if (int_part > (UINT64_MAX - frac_part) / scale) {
        return pack_inf(sign);
    }
    return rational_to_float(sign, int_part * scale + frac_part, scale);
}

static u32 fadd_soft(u32 a, u32 b) {
    FloatParts x = unpack(a);
    FloatParts y = unpack(b);
    u64 xs, ys, res;
    int exp, sign;

    if (x.nan || y.nan) return pack_nan();
    if (x.inf || y.inf) {
        if (x.inf && y.inf && x.sign != y.sign) return pack_nan();
        return x.inf ? pack_inf(x.sign) : pack_inf(y.sign);
    }
    if (x.zero) return b;
    if (y.zero) return a;

    xs = (u64)x.sig << 8;
    ys = (u64)y.sig << 8;
    exp = x.exp;
    if (x.exp > y.exp) {
        ys = shift_right_jam(ys, x.exp - y.exp);
    } else if (y.exp > x.exp) {
        xs = shift_right_jam(xs, y.exp - x.exp);
        exp = y.exp;
    }

    if (x.sign == y.sign) {
        res = xs + ys;
        sign = x.sign;
        if (res & ((u64)1 << 32)) {
            res = shift_right_jam(res, 1);
            exp++;
        }
    } else {
        if (xs == ys) return 0;
        if (xs > ys) {
            res = xs - ys;
            sign = x.sign;
        } else {
            res = ys - xs;
            sign = y.sign;
        }
        while ((res & ((u64)1 << 31)) == 0) {
            res <<= 1;
            exp--;
        }
    }
    return round_pack(sign, exp, res);
}

static u32 fsub_soft(u32 a, u32 b) {
    return fadd_soft(a, b ^ 0x80000000u);
}

static u32 fmul_soft(u32 a, u32 b) {
    FloatParts x = unpack(a);
    FloatParts y = unpack(b);
    u64 prod, sig_extra;
    int sign = x.sign ^ y.sign;
    int exp = x.exp + y.exp;

    if (x.nan || y.nan) return pack_nan();
    if (x.inf || y.inf) {
        if (x.zero || y.zero) return pack_nan();
        return pack_inf(sign);
    }
    if (x.zero || y.zero) return (u32)sign << 31;

    prod = (u64)x.sig * (u64)y.sig;
    if (prod & ((u64)1 << 47)) {
        sig_extra = shift_right_jam(prod, 16);
        exp++;
    } else {
        sig_extra = shift_right_jam(prod, 15);
    }
    return round_pack(sign, exp, sig_extra);
}

static u32 fdiv_soft(u32 a, u32 b) {
    FloatParts x = unpack(a);
    FloatParts y = unpack(b);
    u64 numerator, q, r;
    int sign = x.sign ^ y.sign;
    int exp = x.exp - y.exp;

    if (x.nan || y.nan) return pack_nan();
    if (x.inf && y.inf) return pack_nan();
    if (x.zero && y.zero) return pack_nan();
    if (x.inf) return pack_inf(sign);
    if (y.inf) return (u32)sign << 31;
    if (y.zero) return pack_inf(sign);
    if (x.zero) return (u32)sign << 31;

    if (x.sig >= y.sig) {
        numerator = (u64)x.sig << 31;
    } else {
        numerator = (u64)x.sig << 32;
        exp--;
    }
    q = numerator / y.sig;
    r = numerator % y.sig;
    if (r != 0) q |= 1u;
    return round_pack(sign, exp, q);
}

#ifndef SOFTFLOAT_NO_CONSOLE_MAIN
static void print_binary(u32 v) {
    int i;
    for (i = 31; i >= 0; i--) {
        putchar((v & ((u32)1 << i)) ? '1' : '0');
        if (i == 31 || i == 23) putchar(' ');
    }
}

static void show_bits(const char *name, u32 v) {
    printf("%s HEX : 0x%08X\n", name, v);
    printf("%s BIN : ", name);
    print_binary(v);
    printf("\n");
}

static void read_word(const char *prompt, char *buf, int len) {
    printf("%s", prompt);
    scanf("%63s", buf);
    (void)len;
}

static void init_console_charset(void) {
#ifdef _WIN32
    SetConsoleOutputCP(CP_UTF8);
    SetConsoleCP(CP_UTF8);
#endif
}

int main(void) {
    int choice;
    char sa[64], sb[64];
    u32 a, b, r;

    init_console_charset();

    for (;;) {
        printf("\nIEEE 754 单精度软件仿真（仅整数运算）\n");
        printf("1. 十进制转 IEEE754 表示\n");
        printf("2. 加法\n");
        printf("3. 减法\n");
        printf("4. 乘法\n");
        printf("5. 除法\n");
        printf("0. 退出\n");
        printf("请选择: ");
        if (scanf("%d", &choice) != 1) return 0;
        if (choice == 0) break;

        if (choice == 1) {
            read_word("输入十进制实数: ", sa, sizeof(sa));
            a = parse_decimal(sa);
            show_bits("结果", a);
        } else if (choice >= 2 && choice <= 5) {
            read_word("输入第一个十进制实数: ", sa, sizeof(sa));
            read_word("输入第二个十进制实数: ", sb, sizeof(sb));
            a = parse_decimal(sa);
            b = parse_decimal(sb);
            if (choice == 2) r = fadd_soft(a, b);
            else if (choice == 3) r = fsub_soft(a, b);
            else if (choice == 4) r = fmul_soft(a, b);
            else r = fdiv_soft(a, b);
            show_bits("操作数A", a);
            show_bits("操作数B", b);
            show_bits("运算结果", r);
        } else {
            printf("无效选择。\n");
        }
    }
    return 0;
}
#endif
