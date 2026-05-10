---
name: "yt-dlp"
description: "使用 yt-dlp 下载视频。当用户提供视频链接时调用此技能下载音频或视频。"
---

# yt-dlp 视频下载技能

## 功能说明

本技能使用 yt-dlp 命令行工具从 YouTube、Bilibili 等数千个网站下载视频或音频。

## 何时调用

- 用户提供视频链接（YouTube、Bilibili、抖音等）
- 用户要求下载视频或音频
- 用户需要提取视频的音频格式

## 使用方法

### 基本下载视频
```bash
yt-dlp "视频链接"
```

### 下载最佳质量
```bash
yt-dlp -f best "视频链接"
```

### 仅下载音频（转换为 MP3）
```bash
yt-dlp -x --audio-format mp3 "视频链接"
```

### 指定下载格式和分辨率
```bash
yt-dlp -f "bestvideo[height<=1080]+bestaudio/best" "视频链接"
```

### 下载播放列表
```bash
yt-dlp "播放列表链接"
```

### 指定保存位置
```bash
yt-dlp -o "downloads/%(title)s.%(ext)s" "视频链接"
```

## 常用选项

| 选项 | 说明 |
|------|------|
| `-f` | 指定格式 |
| `-x` | 提取音频 |
| `--audio-format` | 音频格式（mp3, wav, m4a 等） |
| `-o` | 输出文件名模板 |
| `--embed-subs` | 嵌入字幕 |
| `--write-thumbnail` | 下载缩略图 |
| `--limit-rate` | 限制下载速度（如 50K, 1M） |
| `-R` | 重试次数 |
| `--cookies` | 使用 cookie 文件 |

## 支持的网站

yt-dlp 支持数千个网站，包括但不限于：
- YouTube
- Bilibili
- 抖音/TikTok
- 推特/X
- Instagram
- Facebook
- Vimeo
- 爱奇艺
- 腾讯视频
- 优酷

完整支持网站列表：https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md

## 安装依赖

确保系统已安装：
1. Python 3.8+
2. yt-dlp: `pip install yt-dlp`
3. FFmpeg（用于音频转换和后期处理）

Windows 安装命令：
```bash
pip install yt-dlp
choco install ffmpeg  # 或者从 ffmpeg.org 下载
```

## 注意事项

1. 下载视频请遵守版权和网站服务条款
2. 某些网站可能需要登录或使用代理
3. 下载前检查视频是否受版权保护
4. 建议仅用于个人学习和研究目的
