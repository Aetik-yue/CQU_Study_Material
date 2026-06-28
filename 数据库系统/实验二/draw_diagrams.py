# -*- coding: utf-8 -*-
"""
绘制需求分析图、功能模块图、关系模型图
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch
import numpy as np

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei']
plt.rcParams['axes.unicode_minus'] = False

# ================================================================
# 图1: 需求分析图
# ================================================================
fig1, ax1 = plt.subplots(figsize=(14, 8))
ax1.set_xlim(0, 14)
ax1.set_ylim(0, 8)
ax1.axis('off')

# 中心系统框
center = FancyBboxPatch((5.2, 3.2), 3.6, 1.6, boxstyle="round,pad=0.1",
                         facecolor='#1F4E79', edgecolor='#0D2F4F', linewidth=2.5)
ax1.add_patch(center)
ax1.text(7, 4.0, '企业员工管理\n系统', ha='center', va='center',
         fontsize=18, fontweight='bold', color='white')

# 5个需求模块
modules = [
    ('员工信息',   1.2, 5.5, '#2E75B6', '基本信息录入\n修改 删除 查询'),
    ('部门信息',   4.2, 5.5, '#2E75B6', '部门创建 修改\n删除 人员统计'),
    ('岗位信息',   7.2, 5.5, '#2E75B6', '岗位设置 调整\n查询 人员分配'),
    ('薪资信息',  10.2, 5.5, '#2E75B6', '薪资录入 调整\n查询 统计报表'),
    ('考勤信息',   1.2, 0.8, '#2E75B6', '出勤记录录入\n统计 报表生成'),
    ('',          4.2, 0.8, '#2E75B6', ''),  # placeholder to skip
]

# 实际5个模块位置（上下两排）
req_items = [
    ('员工信息管理', 1.0, 5.8, '#2E75B6', '姓名 性别 出生日期\n联系方式 入职日期'),
    ('部门信息管理', 4.2, 5.8, '#2E75B6', '部门创建 修改\n部门人员统计'),
    ('岗位信息管理', 7.4, 5.8, '#2E75B6', '岗位设置 调整\n岗位人员分配'),
    ('薪资信息管理', 10.6, 5.8, '#2E75B6', '薪资录入 调整\n查询统计'),
    ('考勤信息管理', 7.4, 0.8, '#2E75B6', '出勤记录录入\n统计 报表生成'),
]

for name, x, y, color, desc in req_items:
    # 模块框
    box = FancyBboxPatch((x - 1.1, y - 0.7), 2.2, 1.4, boxstyle="round,pad=0.08",
                          facecolor=color, edgecolor='#1A4A7A', linewidth=2)
    ax1.add_patch(box)
    ax1.text(x, y + 0.15, name, ha='center', va='center',
             fontsize=13, fontweight='bold', color='white')
    ax1.text(x, y - 0.35, desc, ha='center', va='center',
             fontsize=9, color='#D6E4F0', linespacing=1.4)
    # 连线
    ax1.plot([7, x], [3.2, y - 0.7 if y > 4 else y + 0.7],
             color='#8DB4E2', linewidth=2, linestyle='--', zorder=0)

# 标题
ax1.text(7, 7.5, '企业员工管理系统需求分析图', ha='center', va='center',
         fontsize=20, fontweight='bold', color='#1F3864')

plt.tight_layout()
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\req_analysis.png',
            dpi=200, bbox_inches='tight')
plt.close()
print("需求分析图 saved!")

# ================================================================
# 图2: 功能模块图（树状结构）
# ================================================================
fig2, ax2 = plt.subplots(figsize=(16, 9))
ax2.set_xlim(0, 16)
ax2.set_ylim(0, 9)
ax2.axis('off')

# 顶层：系统
root = FancyBboxPatch((5.5, 7.2), 5, 1.2, boxstyle="round,pad=0.1",
                       facecolor='#1F4E79', edgecolor='#0D2F4F', linewidth=2.5)
ax2.add_patch(root)
ax2.text(8, 7.8, '企业员工管理系统', ha='center', va='center',
         fontsize=18, fontweight='bold', color='white')

# 5个一级模块
level1 = [
    ('M1 员工管理', 1.5, 5.0, '#2E75B6'),
    ('M2 部门管理', 4.5, 5.0, '#2E75B6'),
    ('M3 岗位管理', 7.5, 5.0, '#2E75B6'),
    ('M4 薪资管理', 10.5, 5.0, '#2E75B6'),
    ('M5 考勤管理', 13.5, 5.0, '#2E75B6'),
]

for name, x, y, color in level1:
    box = FancyBboxPatch((x - 1.2, y - 0.5), 2.4, 1.0, boxstyle="round,pad=0.06",
                          facecolor=color, edgecolor='#1A4A7A', linewidth=2)
    ax2.add_patch(box)
    ax2.text(x, y, name, ha='center', va='center',
             fontsize=13, fontweight='bold', color='white')
    # 连线到顶层
    ax2.plot([8, x], [7.2, y + 0.5], color='#8DB4E2', linewidth=2, zorder=0)

# 二级功能
level2 = [
    # M1
    ('录入', 0.6, 2.8, '#70AD47'), ('修改', 1.5, 2.8, '#70AD47'),
    ('删除', 2.4, 2.8, '#70AD47'), ('查询', 3.3, 2.8, '#70AD47'),
    # M2
    ('创建', 3.6, 2.8, '#70AD47'), ('修改', 4.5, 2.8, '#70AD47'),
    ('删除', 5.4, 2.8, '#70AD47'), ('统计', 6.3, 2.8, '#70AD47'),
    # M3
    ('设置', 6.6, 2.8, '#70AD47'), ('调整', 7.5, 2.8, '#70AD47'),
    ('查询', 8.4, 2.8, '#70AD47'), ('分配', 9.3, 2.8, '#70AD47'),
    # M4
    ('录入', 9.6, 2.8, '#70AD47'), ('调整', 10.5, 2.8, '#70AD47'),
    ('查询', 11.4, 2.8, '#70AD47'), ('报表', 12.3, 2.8, '#70AD47'),
    # M5
    ('录入', 12.6, 2.8, '#70AD47'), ('统计', 13.5, 2.8, '#70AD47'),
    ('报表', 14.4, 2.8, '#70AD47'),
]

for name, x, y, color in level2:
    box = FancyBboxPatch((x - 0.45, y - 0.35), 0.9, 0.7, boxstyle="round,pad=0.05",
                          facecolor=color, edgecolor='#4C7A2B', linewidth=1.5)
    ax2.add_patch(box)
    ax2.text(x, y, name, ha='center', va='center',
             fontsize=11, fontweight='bold', color='white')

# 一级→二级连线
connections = [
    (1.5, 4.5, [0.6, 1.5, 2.4, 3.3]),
    (4.5, 4.5, [3.6, 4.5, 5.4, 6.3]),
    (7.5, 4.5, [6.6, 7.5, 8.4, 9.3]),
    (10.5, 4.5, [9.6, 10.5, 11.4, 12.3]),
    (13.5, 4.5, [12.6, 13.5, 14.4]),
]

for px, py, children in connections:
    for cx in children:
        ax2.plot([px, cx], [py - 0.5, 2.8 + 0.35],
                 color='#A9D18E', linewidth=1.5, zorder=0)

# 标题
ax2.text(8, 8.6, '企业员工管理系统功能模块图', ha='center', va='center',
         fontsize=20, fontweight='bold', color='#1F3864')

plt.tight_layout()
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\func_modules.png',
            dpi=200, bbox_inches='tight')
plt.close()
print("功能模块图 saved!")

# ================================================================
# 图3: 关系模型图
# ================================================================
fig3, ax3 = plt.subplots(figsize=(16, 10))
ax3.set_xlim(0, 16)
ax3.set_ylim(0, 10)
ax3.axis('off')

# 表头颜色
HEADER_COLOR = '#1F4E79'
ROW_COLOR = '#D6E4F0'
PK_COLOR = '#FFF2CC'
FK_COLOR = '#E2EFDA'

def draw_table(ax, x, y, title, columns, w=3.2, h=0.45):
    """绘制关系表"""
    n = len(columns)
    total_h = h * (n + 1)
    # 表头
    header = FancyBboxPatch((x, y), w, h, boxstyle="square,pad=0",
                             facecolor=HEADER_COLOR, edgecolor='#0D2F4F', linewidth=1.5)
    ax.add_patch(header)
    ax.text(x + w/2, y + h/2, title, ha='center', va='center',
            fontsize=12, fontweight='bold', color='white')
    # 列
    for i, (name, is_pk, is_fk) in enumerate(columns):
        cy = y - (i + 1) * h
        if is_pk:
            bg = PK_COLOR
            border = '#BF8F00'
        elif is_fk:
            bg = FK_COLOR
            border = '#70AD47'
        else:
            bg = ROW_COLOR
            border = '#8DB4E2'
        row = FancyBboxPatch((x, cy), w, h, boxstyle="square,pad=0",
                              facecolor=bg, edgecolor=border, linewidth=1)
        ax.add_patch(row)
        label = name
        if is_pk:
            label = f'[PK] {name}'
            ax.text(x + 0.15, cy + h/2, label, ha='left', va='center',
                    fontsize=9, color='#7F6000', fontweight='bold')
        elif is_fk:
            label = f'[FK] {name}'
            ax.text(x + 0.15, cy + h/2, label, ha='left', va='center',
                    fontsize=9, color='#375623', fontweight='bold')
        else:
            ax.text(x + w/2, cy + h/2, name, ha='center', va='center',
                    fontsize=10, color='#1F3864')

# 表位置
tables = [
    ('Department 部门表', 0.5, 7.5, [
        ('dept_id', True, False), ('dept_name', False, False),
        ('description', False, False), ('manager', False, False)
    ]),
    ('Position 岗位表', 0.5, 3.8, [
        ('pos_id', True, False), ('pos_name', False, False),
        ('description', False, False), ('salary_level', False, False)
    ]),
    ('Employee 员工表', 5.0, 6.0, [
        ('emp_id', True, False), ('emp_name', False, False),
        ('gender', False, False), ('birth_date', False, False),
        ('phone', False, False), ('email', False, False),
        ('hire_date', False, False), ('dept_id', False, True),
        ('pos_id', False, True)
    ]),
    ('Salary 薪资表', 10.0, 7.0, [
        ('salary_id', True, False), ('emp_id', False, True),
        ('base_salary', False, False), ('perf_bonus', False, False),
        ('bonus', False, False), ('pay_month', False, False)
    ]),
    ('Attendance 考勤表', 10.0, 3.2, [
        ('attend_id', True, False), ('emp_id', False, True),
        ('attend_date', False, False), ('status', False, False),
        ('remark', False, False)
    ]),
]

for title, x, y, cols in tables:
    draw_table(ax3, x, y, title, cols, w=3.2, h=0.42)

# 外键连线
# Employee.dept_id -> Department.dept_id
ax3.annotate('', xy=(0.5, 7.5 - 0.42), xytext=(5.0 + 3.2, 6.0 - 8*0.42),
             arrowprops=dict(arrowstyle='->', color='#C00000', lw=2))
# Employee.pos_id -> Position.pos_id
ax3.annotate('', xy=(0.5, 3.8 - 0.42), xytext=(5.0 + 3.2, 6.0 - 9*0.42),
             arrowprops=dict(arrowstyle='->', color='#C00000', lw=2))
# Salary.emp_id -> Employee.emp_id
ax3.annotate('', xy=(5.0 + 3.2, 6.0 - 0.42), xytext=(10.0, 7.0 - 2*0.42),
             arrowprops=dict(arrowstyle='->', color='#C00000', lw=2))
# Attendance.emp_id -> Employee.emp_id
ax3.annotate('', xy=(5.0 + 3.2, 6.0 - 1*0.42), xytext=(10.0, 3.2 - 2*0.42),
             arrowprops=dict(arrowstyle='->', color='#C00000', lw=2))

# 图例
legend_y = 0.5
ax3.text(0.5, legend_y + 0.3, '图例：', fontsize=11, fontweight='bold', color='#333')
pk_box = FancyBboxPatch((1.5, legend_y), 0.8, 0.3, boxstyle="square",
                         facecolor=PK_COLOR, edgecolor='#BF8F00', linewidth=1)
ax3.add_patch(pk_box)
ax3.text(2.5, legend_y + 0.15, '主键 (PK)', fontsize=10, va='center', color='#333')

fk_box = FancyBboxPatch((4.0, legend_y), 0.8, 0.3, boxstyle="square",
                         facecolor=FK_COLOR, edgecolor='#70AD47', linewidth=1)
ax3.add_patch(fk_box)
ax3.text(5.0, legend_y + 0.15, '外键 (FK)', fontsize=10, va='center', color='#333')

ax3.plot([6.5, 7.5], [legend_y + 0.15, legend_y + 0.15], color='#C00000', lw=2)
ax3.annotate('', xy=(7.5, legend_y + 0.15), xytext=(6.5, legend_y + 0.15),
             arrowprops=dict(arrowstyle='->', color='#C00000', lw=2))
ax3.text(7.8, legend_y + 0.15, '外键关联', fontsize=10, va='center', color='#333')

# 标题
ax3.text(8, 9.5, '企业员工管理系统关系模型图', ha='center', va='center',
         fontsize=20, fontweight='bold', color='#1F3864')

plt.tight_layout()
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\rel_model.png',
            dpi=200, bbox_inches='tight')
plt.close()
print("关系模型图 saved!")
