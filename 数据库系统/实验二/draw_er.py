# -*- coding: utf-8 -*-
"""
企业员工管理系统 E-R 图绘制脚本（优化版）
减少留白、放大文字、宽屏比例
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, Ellipse
import numpy as np

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'SimSun']
plt.rcParams['axes.unicode_minus'] = False

fig, ax = plt.subplots(1, 1, figsize=(20, 10))

# ===== 颜色方案 =====
COLOR_ENTITY = '#4472C4'
COLOR_ENTITY_TEXT = 'white'
COLOR_ATTR = '#D6E4F0'
COLOR_ATTR_TEXT = '#1F3864'
COLOR_PK = '#FFC000'
COLOR_RELATION = '#70AD47'
COLOR_RELATION_TEXT = 'white'

def draw_entity(ax, x, y, name, w=2.6, h=1.0):
    """绘制实体（矩形）"""
    rect = FancyBboxPatch((x - w/2, y - h/2), w, h,
                          boxstyle="round,pad=0.04",
                          facecolor=COLOR_ENTITY, edgecolor='#2F5597', linewidth=2.5,
                          zorder=3)
    ax.add_patch(rect)
    ax.text(x, y, name, ha='center', va='center',
            fontsize=16, fontweight='bold', color=COLOR_ENTITY_TEXT, zorder=4)
    return (x, y)

def draw_attribute(ax, x, y, name, is_pk=False, w=1.5, h=0.55):
    """绘制属性（椭圆）"""
    color = COLOR_PK if is_pk else COLOR_ATTR
    edge_color = '#BF8F00' if is_pk else '#8DB4E2'
    ellipse = Ellipse((x, y), w, h, facecolor=color,
                      edgecolor=edge_color, linewidth=2, zorder=3)
    ax.add_patch(ellipse)
    fontweight = 'bold' if is_pk else 'normal'
    text_color = '#7F6000' if is_pk else COLOR_ATTR_TEXT
    ax.text(x, y, name, ha='center', va='center',
            fontsize=11, fontweight=fontweight, color=text_color, zorder=4)

def draw_relation(ax, x, y, name, size=0.6):
    """绘制联系（菱形）"""
    diamond = plt.Polygon([
        (x, y + size),
        (x + size * 1.3, y),
        (x, y - size),
        (x - size * 1.3, y)
    ], closed=True, facecolor=COLOR_RELATION, edgecolor='#4C7A2B',
        linewidth=2.5, zorder=3)
    ax.add_patch(diamond)
    ax.text(x, y, name, ha='center', va='center',
            fontsize=13, fontweight='bold', color=COLOR_RELATION_TEXT, zorder=4)
    return (x, y)

def draw_line(ax, p1, p2):
    """绘制连接线"""
    ax.plot([p1[0], p2[0]], [p1[1], p2[1]],
            color='#555555', linewidth=1.8, zorder=1)

def draw_line_with_label(ax, p1, p2, label, offset=(0, 0)):
    """绘制带标签的连线"""
    ax.plot([p1[0], p2[0]], [p1[1], p2[1]],
            color='#555555', linewidth=1.8, zorder=1)
    mx = (p1[0] + p2[0]) / 2 + offset[0]
    my = (p1[1] + p2[1]) / 2 + offset[1]
    ax.text(mx, my, label, ha='center', va='center',
            fontsize=12, fontweight='bold', color='#C00000',
            bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                     edgecolor='#C00000', alpha=0.95, linewidth=1.2), zorder=5)

# ===== 坐标范围 =====
ax.set_xlim(-11, 11)
ax.set_ylim(-5.5, 5.5)
ax.axis('off')

# ===== 实体位置（紧凑水平布局） =====
dept_pos    = draw_entity(ax, -7.5,  3.0, '部门\nDepartment')
emp_pos     = draw_entity(ax,  0,   0.5, '员工\nEmployee')
pos_ent_pos = draw_entity(ax, -7.5, -2.5, '岗位\nPosition')
sal_pos     = draw_entity(ax,  7.5,  3.0, '薪资\nSalary')
att_pos     = draw_entity(ax,  7.5, -2.5, '考勤\nAttendance')

# ===== 联系位置 =====
rel_dept_emp = draw_relation(ax, -3.8,  1.8, '属于')
rel_pos_emp  = draw_relation(ax, -3.8, -1.3, '担任')
rel_emp_sal  = draw_relation(ax,  3.8,  1.8, '拥有')
rel_emp_att  = draw_relation(ax,  3.8, -1.3, '记录')

# ===== 实体→联系连线 + 基数 =====
draw_line_with_label(ax, dept_pos, rel_dept_emp, '1', (0.5, 0.3))
draw_line_with_label(ax, emp_pos, rel_dept_emp, 'N', (-0.5, -0.3))

draw_line_with_label(ax, pos_ent_pos, rel_pos_emp, '1', (0.5, -0.3))
draw_line_with_label(ax, emp_pos, rel_pos_emp, 'N', (-0.5, 0.3))

draw_line_with_label(ax, emp_pos, rel_emp_sal, '1', (0.5, 0.3))
draw_line_with_label(ax, sal_pos, rel_emp_sal, 'N', (-0.5, -0.3))

draw_line_with_label(ax, emp_pos, rel_emp_att, '1', (0.5, -0.3))
draw_line_with_label(ax, att_pos, rel_emp_att, 'N', (-0.5, 0.3))

# ===== 部门属性（上方，紧凑排列） =====
dept_attrs = [
    ('dept_id', True,   (-9.8, 4.8)),
    ('dept_name', False,(-8.2, 4.8)),
    ('description', False,(-6.5, 4.8)),
    ('manager', False,  (-5.0, 4.8)),
]
for name, is_pk, (ax_, ay_) in dept_attrs:
    draw_attribute(ax, ax_, ay_, name, is_pk=is_pk)
    draw_line(ax, dept_pos, (ax_, ay_))

# ===== 岗位属性（下方，紧凑排列） =====
pos_attrs = [
    ('pos_id', True,   (-9.8, -4.3)),
    ('pos_name', False,(-8.2, -4.3)),
    ('description', False,(-6.5, -4.3)),
    ('salary_level', False,(-5.0, -4.3)),
]
for name, is_pk, (ax_, ay_) in pos_attrs:
    draw_attribute(ax, ax_, ay_, name, is_pk=is_pk)
    draw_line(ax, pos_ent_pos, (ax_, ay_))

# ===== 员工属性（中心，围绕四周展开） =====
emp_attrs = [
    ('emp_id', True,   (-1.5, 2.8)),   # 正上方偏左
    ('emp_name', False, (0.2, 3.0)),    # 正上方
    ('gender', False,   (1.8, 2.8)),    # 正上方偏右
    ('birth_date', False,(-2.5, -1.0)), # 左下方
    ('phone', False,    (-0.8, -1.8)),   # 正下方
    ('email', False,    (0.8, -1.8)),    # 正下方
    ('hire_date', False, (2.0, -1.0)),   # 右下方
]
for name, is_pk, (ax_, ay_) in emp_attrs:
    draw_attribute(ax, ax_, ay_, name, is_pk=is_pk)
    draw_line(ax, emp_pos, (ax_, ay_))

# ===== 薪资属性（上方，紧凑排列） =====
sal_attrs = [
    ('salary_id', True,  (5.2, 4.8)),
    ('emp_id', False,   (6.5, 4.8)),
    ('base_salary', False,(8.0, 4.8)),
    ('performance', False,(9.5, 4.8)),
    ('bonus', False,    (5.8, 4.0)),
    ('pay_month', False,(7.2, 4.0)),
]
for name, is_pk, (ax_, ay_) in sal_attrs:
    draw_attribute(ax, ax_, ay_, name, is_pk=is_pk)
    draw_line(ax, sal_pos, (ax_, ay_))

# ===== 考勤属性（下方，紧凑排列） =====
att_attrs = [
    ('attend_id', True, (5.2, -4.3)),
    ('emp_id', False,  (6.5, -4.3)),
    ('attend_date', False,(8.0, -4.3)),
    ('status', False,  (9.5, -4.3)),
    ('remark', False,  (6.8, -3.5)),
]
for name, is_pk, (ax_, ay_) in att_attrs:
    draw_attribute(ax, ax_, ay_, name, is_pk=is_pk)
    draw_line(ax, att_pos, (ax_, ay_))

# ===== 图例（右下角，紧凑排列） =====
legend_x, legend_y = 9.0, -0.8

# 实体图例
rect_legend = FancyBboxPatch((legend_x, legend_y + 0.6), 0.9, 0.4,
                              boxstyle="round,pad=0.03",
                              facecolor=COLOR_ENTITY, edgecolor='#2F5597', linewidth=1.5, zorder=3)
ax.add_patch(rect_legend)
ax.text(legend_x + 1.2, legend_y + 0.8, '实体', fontsize=11, va='center', color='#333333', fontweight='bold')

# 属性图例
ell_legend = Ellipse((legend_x + 0.45, legend_y), 0.9, 0.4,
                      facecolor=COLOR_ATTR, edgecolor='#8DB4E2', linewidth=1.5, zorder=3)
ax.add_patch(ell_legend)
ax.text(legend_x + 1.2, legend_y, '属性', fontsize=11, va='center', color='#333333', fontweight='bold')

# 主键属性图例
ell_pk = Ellipse((legend_x + 0.45, legend_y - 0.5), 0.9, 0.4,
                  facecolor=COLOR_PK, edgecolor='#BF8F00', linewidth=1.5, zorder=3)
ax.add_patch(ell_pk)
ax.text(legend_x + 1.2, legend_y - 0.5, '主键', fontsize=11, va='center', color='#333333', fontweight='bold')

# 联系图例
diamond_legend = plt.Polygon([
    (legend_x + 0.45, legend_y - 1.0),
    (legend_x + 0.9, legend_y - 1.2),
    (legend_x + 0.45, legend_y - 1.4),
    (legend_x, legend_y - 1.2)
], closed=True, facecolor=COLOR_RELATION, edgecolor='#4C7A2B', linewidth=1.5, zorder=3)
ax.add_patch(diamond_legend)
ax.text(legend_x + 1.2, legend_y - 1.2, '联系', fontsize=11, va='center', color='#333333', fontweight='bold')

# ===== 标题 =====
ax.text(0, 5.3, '企业员工管理系统 E-R 图', ha='center', va='center',
        fontsize=24, fontweight='bold', color='#1F3864')

plt.tight_layout(pad=0.2)
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\er_diagram.pdf',
            dpi=300, bbox_inches='tight', format='pdf')
plt.savefig(r'E:\严浩睿的珍藏学习资料\数据库系统\实验二\er_diagram.png',
            dpi=200, bbox_inches='tight', format='png')
print("E-R diagram (optimized) saved successfully!")
