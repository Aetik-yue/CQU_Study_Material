import numpy as np
import matplotlib.pyplot as plt
from matplotlib import rcParams

# 设置中文字体
rcParams['font.sans-serif'] = ['SimHei']  # 黑体
rcParams['axes.unicode_minus'] = False  # 解决负号显示问题

# 创建图表保存目录
import os
save_dir = r'c:\Users\yanha\Desktop\数学实验\figures'
os.makedirs(save_dir, exist_ok=True)

# ============ 图 1: 问题一与问题二遮蔽时间对比 ============
fig1, ax1 = plt.subplots(figsize=(10, 6))

methods = ['问题一\n(固定参数)', '问题二\n(优化后)']
cover_times = [2.4, 3.8]
colors = ['#5B9BD5', '#ED7D31']

bars = ax1.bar(methods, cover_times, color=colors, edgecolor='black', linewidth=1.5, width=0.5)

# 添加数值标签
for bar, value in zip(bars, cover_times):
    ax1.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.1, 
            f'{value}s', ha='center', va='bottom', fontsize=12, fontweight='bold')

# 计算提升比例
improvement = ((3.8 - 2.4) / 2.4) * 100
ax1.text(1, 2.5, f'提升{improvement:.0f}%', fontsize=10, color='red', 
        bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

ax1.set_ylabel('有效遮蔽时长 (s)', fontsize=12)
ax1.set_title('问题一与问题二遮蔽效果对比', fontsize=14, fontweight='bold')
ax1.set_ylim(0, 5)
ax1.grid(axis='y', alpha=0.3, linestyle='--')

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig1_comparison.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 1 已保存：问题一与问题二对比图')

# ============ 图 2: 飞行速度对遮蔽时间的影响（折线图） ============
fig2, ax2 = plt.subplots(figsize=(10, 6))

speeds = [70, 90, 110, 130, 140]
cover_times_speed = [2.8, 3.1, 3.4, 3.6, 3.8]

ax2.plot(speeds, cover_times_speed, marker='o', linewidth=2.5, markersize=8, 
        color='#0070C0', markerfacecolor='white', markeredgewidth=2)

# 添加数值标签
for speed, time in zip(speeds, cover_times_speed):
    ax2.text(speed, time + 0.05, f'{time}s', ha='center', va='bottom', fontsize=10)

ax2.set_xlabel('飞行速度 (m/s)', fontsize=12)
ax2.set_ylabel('有效遮蔽时长 (s)', fontsize=12)
ax2.set_title('飞行速度对遮蔽时间的影响', fontsize=14, fontweight='bold')
ax2.grid(True, alpha=0.3, linestyle='--')
ax2.set_xlim(60, 150)
ax2.set_ylim(2.5, 4.0)

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig2_speed_sensitivity.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 2 已保存：飞行速度灵敏度分析图')

# ============ 图 3: 起爆延迟对遮蔽时间的影响（折线图） ============
fig3, ax3 = plt.subplots(figsize=(10, 6))

delays = [2.0, 3.0, 4.0, 4.2, 5.0]
cover_times_delay = [2.5, 3.2, 3.7, 3.8, 3.5]

ax3.plot(delays, cover_times_delay, marker='s', linewidth=2.5, markersize=8, 
        color='#70AD47', markerfacecolor='white', markeredgewidth=2)

# 添加数值标签
for delay, time in zip(delays, cover_times_delay):
    ax3.text(delay, time + 0.05, f'{time}s', ha='center', va='bottom', fontsize=10)

# 标记最优值
optimal_idx = np.argmax(cover_times_delay)
ax3.plot(delays[optimal_idx], cover_times_delay[optimal_idx], 'r*', markersize=20, label='最优值')
ax3.legend(fontsize=11)

ax3.set_xlabel('起爆延迟 (s)', fontsize=12)
ax3.set_ylabel('有效遮蔽时长 (s)', fontsize=12)
ax3.set_title('起爆延迟对遮蔽时间的影响', fontsize=14, fontweight='bold')
ax3.grid(True, alpha=0.3, linestyle='--')
ax3.set_xlim(1.5, 5.5)
ax3.set_ylim(2.3, 4.0)

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig3_delay_sensitivity.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 3 已保存：起爆延迟灵敏度分析图')

# ============ 图 4: 问题三与问题四遮蔽效果对比（条形图） ============
fig4, ax4 = plt.subplots(figsize=(10, 6))

methods = ['问题三\n(单机三弹)', '问题四\n(三机各一弹)']
cover_times_multi = [10.5, 10.4]
colors_multi = ['#4472C4', '#ED7D31']

bars = ax4.bar(methods, cover_times_multi, color=colors_multi, edgecolor='black', linewidth=1.5, width=0.5)

# 添加数值标签
for bar, value in zip(bars, cover_times_multi):
    ax4.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.15, 
            f'{value}s', ha='center', va='bottom', fontsize=12, fontweight='bold')

ax4.set_ylabel('总遮蔽时长 (s)', fontsize=12)
ax4.set_title('多弹/多机协同遮蔽效果对比', fontsize=14, fontweight='bold')
ax4.set_ylim(0, 12)
ax4.grid(axis='y', alpha=0.3, linestyle='--')

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig4_multi_comparison.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 4 已保存：多弹/多机对比图')

# ============ 图 5: 三枚导弹遮蔽时间分配（扇形图） ============
fig5, ax5 = plt.subplots(figsize=(10, 8))

missiles = ['M1', 'M2', 'M3']
cover_times_missiles = [17, 15, 8.8]
colors_pie = ['#FF6B6B', '#4ECDC4', '#45B7D1']
explode = [0.05, 0.05, 0.05]

wedges, texts, autotexts = ax5.pie(cover_times_missiles, labels=missiles, 
                                    autopct='%1.1f%%', 
                                    colors=colors_pie,
                                    explode=explode,
                                    startangle=90,
                                    textprops={'fontsize': 12})

# 设置百分比文本颜色为白色
for autotext in autotexts:
    autotext.set_color('white')
    autotext.set_fontweight('bold')

# 添加图例
legend_labels = [f'{m}: {t}s' for m, t in zip(missiles, cover_times_missiles)]
ax5.legend(wedges, legend_labels, title='导弹', loc='center left', bbox_to_anchor=(1, 0, 0.5, 1))

ax5.set_title('问题五：三枚导弹遮蔽时间分配', fontsize=14, fontweight='bold', pad=20)

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig5_missile_allocation.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 5 已保存：导弹遮蔽时间分配扇形图')

# ============ 图 6: 问题五资源分配（扇形图） ============
fig6, ax6 = plt.subplots(figsize=(10, 8))

# 弹量分配
ammo_allocation = [5, 5, 3]
colors_ammo = ['#FF9999', '#66B2FF', '#99FF99']

wedges, texts, autotexts = ax6.pie(ammo_allocation, labels=missiles, 
                                    autopct='%1.0f枚', 
                                    colors=colors_ammo,
                                    explode=explode,
                                    startangle=90,
                                    textprops={'fontsize': 12})

for autotext in autotexts:
    autotext.set_color('black')
    autotext.set_fontweight('bold')

legend_labels = [f'{m}: {a}枚弹' for m, a in zip(missiles, ammo_allocation)]
ax6.legend(wedges, legend_labels, title='导弹', loc='center left', bbox_to_anchor=(1, 0, 0.5, 1))

ax6.set_title('问题五：烟幕弹资源分配', fontsize=14, fontweight='bold', pad=20)

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig6_ammo_allocation.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 6 已保存：烟幕弹资源分配扇形图')

# ============ 图 7: 遮蔽时间热力图（时间维度） ============
fig7, ax7 = plt.subplots(figsize=(12, 6))

# 创建遮蔽时间数据（问题三：三枚弹）
# 每枚弹的遮蔽区间
bomb1_cover = [4.9, 8.7]
bomb2_cover = [9.5, 13.0]
bomb3_cover = [14.2, 17.4]

# 创建时间轴
time_axis = np.arange(0, 20, 0.1)
heat_data = np.zeros((3, len(time_axis)))

# 填充遮蔽状态
for i, t in enumerate(time_axis):
    if bomb1_cover[0] <= t <= bomb1_cover[1]:
        heat_data[0, i] = 1
    if bomb2_cover[0] <= t <= bomb2_cover[1]:
        heat_data[1, i] = 1
    if bomb3_cover[0] <= t <= bomb3_cover[1]:
        heat_data[2, i] = 1

# 计算总遮蔽状态（任意弹遮蔽则为 1）
total_cover = np.clip(np.sum(heat_data, axis=0), 0, 1)

# 绘制热力图
im = ax7.imshow(heat_data, aspect='auto', cmap='YlOrRd', extent=[0, 20, 0.5, 3.5], 
                vmin=0, vmax=1, alpha=0.7)

# 添加遮蔽区间标注
ax7.axvspan(bomb1_cover[0], bomb1_cover[1], alpha=0.3, color='red', label='弹 1')
ax7.axvspan(bomb2_cover[0], bomb2_cover[1], alpha=0.3, color='green', label='弹 2')
ax7.axvspan(bomb3_cover[0], bomb3_cover[1], alpha=0.3, color='blue', label='弹 3')

# 添加文字标注
ax7.text((bomb1_cover[0] + bomb1_cover[1])/2, 1, f'{bomb1_cover[1]-bomb1_cover[0]:.1f}s', 
        ha='center', va='center', fontsize=10, fontweight='bold')
ax7.text((bomb2_cover[0] + bomb2_cover[1])/2, 2, f'{bomb2_cover[1]-bomb2_cover[0]:.1f}s', 
        ha='center', va='center', fontsize=10, fontweight='bold')
ax7.text((bomb3_cover[0] + bomb3_cover[1])/2, 3, f'{bomb3_cover[1]-bomb3_cover[0]:.1f}s', 
        ha='center', va='center', fontsize=10, fontweight='bold')

ax7.set_xlabel('时间 (s)', fontsize=12)
ax7.set_ylabel('烟幕弹编号', fontsize=12)
ax7.set_title('问题三：三枚烟幕弹遮蔽时间分布热力图', fontsize=14, fontweight='bold')
ax7.set_yticks([1, 2, 3])
ax7.set_yticklabels(['弹 1', '弹 2', '弹 3'])
ax7.set_xlim(0, 20)

# 添加总遮蔽时间标注
ax7.text(10, 0.3, f'总遮蔽时间：10.5s', ha='center', va='center', 
        fontsize=12, fontweight='bold', color='red',
        bbox=dict(boxstyle='round', facecolor='yellow', alpha=0.7))

plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig7_heatmap.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 7 已保存：遮蔽时间热力图')

# ============ 图 8: 综合对比图（多子图） ============
fig8, axes = plt.subplots(2, 2, figsize=(14, 10))

# 子图 1: 单弹优化效果
ax = axes[0, 0]
methods = ['问题一', '问题二']
cover_times = [2.4, 3.8]
colors = ['#5B9BD5', '#ED7D31']
bars = ax.bar(methods, cover_times, color=colors, edgecolor='black', linewidth=1.2)
for bar, value in zip(bars, cover_times):
    ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.08, 
            f'{value}s', ha='center', va='bottom', fontsize=10, fontweight='bold')
ax.set_ylabel('遮蔽时长 (s)', fontsize=11)
ax.set_title('(a) 单弹优化效果对比', fontsize=12, fontweight='bold')
ax.set_ylim(0, 4.5)
ax.grid(axis='y', alpha=0.3, linestyle='--')

# 子图 2: 多弹/多机对比
ax = axes[0, 1]
methods = ['单机三弹', '三机各一弹']
cover_times = [10.5, 10.4]
colors = ['#4472C4', '#ED7D31']
bars = ax.bar(methods, cover_times, color=colors, edgecolor='black', linewidth=1.2)
for bar, value in zip(bars, cover_times):
    ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.15, 
            f'{value}s', ha='center', va='bottom', fontsize=10, fontweight='bold')
ax.set_ylabel('总遮蔽时长 (s)', fontsize=11)
ax.set_title('(b) 多弹/多机协同效果', fontsize=12, fontweight='bold')
ax.set_ylim(0, 12)
ax.grid(axis='y', alpha=0.3, linestyle='--')

# 子图 3: 灵敏度分析（速度）
ax = axes[1, 0]
speeds = [70, 90, 110, 130, 140]
cover_times_speed = [2.8, 3.1, 3.4, 3.6, 3.8]
ax.plot(speeds, cover_times_speed, marker='o', linewidth=2, markersize=6, color='#0070C0')
ax.set_xlabel('飞行速度 (m/s)', fontsize=11)
ax.set_ylabel('遮蔽时长 (s)', fontsize=11)
ax.set_title('(c) 飞行速度灵敏度分析', fontsize=12, fontweight='bold')
ax.grid(True, alpha=0.3, linestyle='--')

# 子图 4: 导弹遮蔽分配
ax = axes[1, 1]
missiles = ['M1', 'M2', 'M3']
cover_times_missiles = [17, 15, 8.8]
colors_pie = ['#FF6B6B', '#4ECDC4', '#45B7D1']
ax.pie(cover_times_missiles, labels=missiles, autopct='%1.0f%%', 
       colors=colors_pie, startangle=90, textprops={'fontsize': 10})
ax.set_title('(d) 三枚导弹遮蔽时间分配', fontsize=12, fontweight='bold')

plt.suptitle('烟幕干扰弹投放策略优化结果汇总', fontsize=16, fontweight='bold', y=1.02)
plt.tight_layout()
plt.savefig(os.path.join(save_dir, 'fig8_summary.png'), dpi=300, bbox_inches='tight')
plt.close()

print('图 8 已保存：综合对比图')

print(f'\n所有图表已保存到：{save_dir}')
print('共生成 8 个图表文件')
