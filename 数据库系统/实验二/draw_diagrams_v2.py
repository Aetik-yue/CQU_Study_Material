# -*- coding: utf-8 -*-
"""
重绘需求分析图和功能模块图 - 专业美观版
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import numpy as np

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei']
plt.rcParams['axes.unicode_minus'] = False

# ================================================================
# 图1: 需求分析图 - 放射状布局，更协调
# ================================================================
fig1, ax1 = plt.subplots(figsize=(16, 10))
ax1.set_xlim(0, 16)
ax1.set_ylim(0, 10)
ax1.axis('off')
ax1.set_facecolor('#FAFBFC')

# 中心圆
center_circle = plt.Circle((8, 5), 1.2, facecolor='#1F4E79', edgecolor='#0D2F4F', linewidth=3, zorder=3)
ax1.add_patch(center_circle)
ax1.text(8, 5.15, '企业员工', ha='center', va='center', fontsize=16, fontweight='bold', color='white', zorder=4)
ax1.text(8, 4.75, '管理系统', ha='center', va='center', fontsize=16, fontweight='bold', color='white', zorder=4)

# 5个需求模块 - 围绕中心均匀分布（上3下2）
requirements = [
    ('员工信息管理', 2.5, 8.2, '#2E75B6', '基本信息录入\n修改、删除、查询\n多条件筛选'),
    ('部门信息管理', 8, 8.2, '#2E75B6', '部门创建与修改\n部门人员统计\n组织架构维护'),
    ('岗位信息管理', 13.5, 8.2, '#2E75B6', '岗位设置与调整\n岗位人员分配\n职责描述管理'),
    ('薪资信息管理', 4.5, 1.8, '#2E75B6', '薪资录入与调整\n月度薪资查询\n统计报表生成'),
    ('考勤信息管理', 11.5, 1.8, '#2E75B6', '出勤记录录入\n考勤统计分析\n异常考勤处理'),
]

for name, x, y, color, desc in requirements:
    # 外框（带阴影效果）
    shadow = FancyBboxPatch((x - 1.6, y - 1.1), 3.2, 2.2, boxstyle="round,pad=0.1",
                             facecolor='#E8E8E8', edgecolor='none', zorder=1)
    ax1.add_patch(shadow)
    # 主框
    box = FancyBboxPatch((x - 1.65, y - 1.15), 3.2, 2.2, boxstyle="round,pad=0.1",
                          facecolor=color, edgecolor='#1A4A7A', linewidth=2.5, zorder=2)
    ax1.add_patch(box)
    # 标题
    ax1.text(x, y + 0.55, name, ha='center', va='center',
             fontsize=14, fontweight='bold', color='white', zorder=3)
    # 分隔线
    ax1.plot([x - 1.2, x + 1.2], [y + 0.2, y + 0.2], color='white', linewidth=1.5, alpha=0.5, zorder=3)
    # 描述
    ax1.text(x, y - 0.3, desc, ha='center', va='center',
             fontsize=11, color='#D6E4F0', linespacing=1.6, zorder=3)
    # 连线（从中心到模块）
    dx = x - 8
    dy = y - 5
    dist = np.sqrt(dx**2 + dy**2)
    nx, ny = dx/dist, dy/dist
    start_x = 8 + nx * 1.2
    start_y = 5 + ny * 1.2
    end_x = x - nx * 1.65
    end_y = y - ny * 1.15 if y > 5 else y + ny * 1.15
    ax1.annotate('', xy=(end_x, end_y), xytext=(start_x, start_y),
                 arrowprops=dict(arrowstyle='->', color='#8DB4E2', lw=2.5, connectionstyle='arc3,rad=0.1'),
                 zorder=0)

# 标题
ax1.text(8, 9.5, '企业员工管理系统需求分析图', ha='center', va='center',
         fontsize=22, fontweight='bold', color='#1F3864')

# 底部说明
ax1.text(8, 0.3, '基于 OpenGauss 数据库  |  2025-2026学年第2学期', ha='center', va='center',
         fontsize=11, color='#666666')

plt.tight_layout()
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\req_analysis.png',
            dpi=200, bbox_inches='tight')
plt.close()
print("需求分析图 saved!")

# ================================================================
# 图2: 功能模块图 - 清晰树状结构
# ================================================================
fig2, ax2 = plt.subplots(figsize=(18, 10))
ax2.set_xlim(0, 18)
ax2.set_ylim(0, 10)
ax2.axis('off')
ax2.set_facecolor('#FAFBFC')

# 顶层：系统标题
root_box = FancyBboxPatch((5.5, 8.3), 7, 1.2, boxstyle="round,pad=0.12",
                           facecolor='#1F4E79', edgecolor='#0D2F4F', linewidth=3, zorder=3)
ax2.add_patch(root_box)
ax2.text(9, 8.9, '企业员工管理系统', ha='center', va='center',
         fontsize=20, fontweight='bold', color='white', zorder=4)

# 5个一级模块
modules = [
    ('M1 员工管理', 2, 6.0, '#2E75B6'),
    ('M2 部门管理', 5.5, 6.0, '#2E75B6'),
    ('M3 岗位管理', 9, 6.0, '#2E75B6'),
    ('M4 薪资管理', 12.5, 6.0, '#2E75B6'),
    ('M5 考勤管理', 16, 6.0, '#2E75B6'),
]

for name, x, y, color in modules:
    box = FancyBboxPatch((x - 1.3, y - 0.55), 2.6, 1.1, boxstyle="round,pad=0.08",
                          facecolor=color, edgecolor='#1A4A7A', linewidth=2.5, zorder=3)
    ax2.add_patch(box)
    ax2.text(x, y, name, ha='center', va='center',
             fontsize=14, fontweight='bold', color='white', zorder=4)
    # 连线到顶层
    ax2.plot([9, x], [8.3, y + 0.55], color='#8DB4E2', linewidth=2.5, zorder=0)

# 二级功能（每个模块下方）
sub_functions = {
    2: ['录入', '修改', '删除', '查询'],
    5.5: ['创建', '修改', '删除', '统计'],
    9: ['设置', '调整', '查询', '分配'],
    12.5: ['录入', '调整', '查询', '报表'],
    16: ['录入', '统计', '报表', '导出'],
}

y2 = 3.5
for mx, funcs in sub_functions.items():
    n = len(funcs)
    spacing = 2.6 / (n + 1)
    for i, func in enumerate(funcs):
        fx = mx - 1.3 + spacing * (i + 1)
        box = FancyBboxPatch((fx - 0.55, y2 - 0.4), 1.1, 0.8, boxstyle="round,pad=0.06",
                              facecolor='#70AD47', edgecolor='#4C7A2B', linewidth=2, zorder=3)
        ax2.add_patch(box)
        ax2.text(fx, y2, func, ha='center', va='center',
                 fontsize=12, fontweight='bold', color='white', zorder=4)
        # 连线
        ax2.plot([mx, fx], [5.45, y2 + 0.4], color='#A9D18E', linewidth=2, zorder=0)

# 标题
ax2.text(9, 9.6, '企业员工管理系统功能模块图', ha='center', va='center',
         fontsize=22, fontweight='bold', color='#1F3864')

# 图例
legend_y = 1.5
ax2.text(1.5, legend_y, '图例：', fontsize=12, fontweight='bold', color='#333')
# 系统层
sys_box = FancyBboxPatch((2.5, legend_y - 0.2), 1.2, 0.4, boxstyle="round,pad=0.05",
                          facecolor='#1F4E79', edgecolor='#0D2F4F', linewidth=1.5)
ax2.add_patch(sys_box)
ax2.text(4, legend_y, '系统层', fontsize=11, va='center', color='#333')
# 模块层
mod_box = FancyBboxPatch((5, legend_y - 0.2), 1.2, 0.4, boxstyle="round,pad=0.05",
                          facecolor='#2E75B6', edgecolor='#1A4A7A', linewidth=1.5)
ax2.add_patch(mod_box)
ax2.text(6.5, legend_y, '功能模块', fontsize=11, va='center', color='#333')
# 子功能层
sub_box = FancyBboxPatch((8, legend_y - 0.2), 1.2, 0.4, boxstyle="round,pad=0.05",
                          facecolor='#70AD47', edgecolor='#4C7A2B', linewidth=1.5)
ax2.add_patch(sub_box)
ax2.text(9.5, legend_y, '子功能', fontsize=11, va='center', color='#333')

plt.tight_layout()
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\func_modules.png',
            dpi=200, bbox_inches='tight')
plt.close()
print("功能模块图 saved!")
