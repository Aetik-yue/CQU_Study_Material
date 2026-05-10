---
name: "vscode-office"
description: "在 VS Code 中预览 PDF、Word、Excel 等办公文档。当用户需要查看或处理办公文档时调用此技能。"
---

# vscode-office 办公文档查看技能

## 功能说明

本技能基于 vscode-office 扩展，让 VS Code/Trae IDE 支持预览多种办公文件格式，无需离开编辑器即可查看 PDF、Word、Excel 等文档。

## 何时调用

- 用户需要查看 PDF 文档内容
- 用户需要预览 Word (.docx) 文件
- 用户需要查看 Excel 表格
- 用户需要浏览压缩文件内容
- 用户需要在 IDE 中处理办公文档

## 支持的格式

### 文档格式
- **PDF**: `.pdf` - 使用 Mozilla pdf.js 渲染
- **Word**: `.docx` - 使用 docxjs 渲染
- **Excel**: `.xls`, `.xlsx`, `.csv` - 使用 SheetJS 和 x-spreadsheet
- **SVG**: `.svg` - 矢量图形预览
- **Markdown**: `.md`, `.markdown` - 使用 vditor 编辑器

### 字体文件
- `.ttf`, `.otf`, `.woff`, `.woff2`

### 压缩文件
- `.zip`, `.jar`, `.vsix`, `.rar`

### 其他
- **HTTP 请求**: `.http` - 使用 REST Client
- **Windows 注册表**: `.reg`

## 使用方法

### 在 VS Code/Trae 中安装

1. 打开扩展面板 (`Ctrl+Shift+X`)
2. 搜索 "Office Viewer" 或 "vscode-office"
3. 点击安装
4. 重启编辑器

或直接访问：
- GitHub: https://github.com/cweijan/vscode-office
- VS Code Marketplace: 搜索 "cweijan.vscode-office"

### 打开文档

**方法 1：直接双击**
- 在文件资源管理器中双击文件即可自动打开

**方法 2：右键菜单**
- 右键点击文件 → "Open With" → "Office Viewer"

**方法 3：命令面板**
- `Ctrl+Shift+P` → "View: Reopen Editor With" → "Office Viewer"

### 快捷键

基于 Vditor 编辑器的快捷键：

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+Alt+I` / `⌘^I` | 上移列表 |
| `Ctrl+Alt+J` / `⌘^J` | 下移列表 |
| `Ctrl+Alt+E` / `⌘^E` | 在 VS Code 中编辑 |
| `Ctrl+Shift+V` | 打开 HTML 实时预览 |
| `Ctrl/Cmd + 鼠标滚轮` | 调整编辑器大小 |
| `Ctrl/Cmd + 点击` | 打开超链接 |
| `双击` | 打开超链接 |

## 功能特性

### PDF 查看
- ✅ 文档导航（上一页/下一页）
- ✅ 文本搜索
- ✅ 缩放控制（适应页面/适应宽度）
- ✅ 缩略图视图
- ✅ 结构视图（目录）
- ✅ 演示模式

### Word 文档查看
- ✅ 完整格式保留
- ✅ 表格显示
- ✅ 图片渲染
- ✅ 列表和引用
- ✅ 代码块显示

### Excel 表格查看
- ✅ 多工作表支持
- ✅ 公式解析
- ✅ 图表显示
- ✅ 条件格式
- ✅ CSV 文件支持

### Markdown 编辑
- ✅ 所见即所得编辑
- ✅ 实时预览
- ✅ 主题切换
- ✅ 表格编辑
- ✅ 数学公式支持

## 配置选项

### 恢复默认 Markdown 编辑器

vscode-office 会将默认 Markdown 编辑器改为 vditor。如果想使用原始编辑器，在 `settings.json` 中添加：

```json
{
    "workbench.editorAssociations": {
        "*.md": "default",
        "*.markdown": "default"
    }
}
```

### 自定义设置

在 `settings.json` 中可以配置：

```json
{
    "officeviewer.pdf.zoom": "auto",
    "officeviewer.excel.showFormulas": false,
    "officeviewer.word.theme": "auto"
}
```

## 技术实现

### 核心依赖库

- **PDF 渲染**: [mozilla/pdf.js](https://github.com/mozilla/pdf.js)
- **DOCX 渲染**: [VolodymyrBaydalka/docxjs](https://github.com/VolodymyrBaydalka/docxjs)
- **XLSX 解析**: [SheetJS/sheetjs](https://github.com/SheetJS/sheetjs)
- **XLSX 渲染**: [myliang/x-spreadsheet](https://github.com/myliang/x-spreadsheet)
- **Markdown 编辑**: [Vanessa219/vditor](https://github.com/Vanessa219/vditor)
- **HTTP 请求**: REST Client
- **图标主题**: PKief/vscode-material-icon-theme

## 使用场景

### 数学实验文档处理
- 查看数学论文 PDF
- 预览实验报告 Word 文档
- 查看数据 Excel 表格
- 快速浏览参考文献

### 开发场景
- 查看项目文档
- 预览 API 文档
- 查看数据导出文件
- 浏览压缩的依赖包

### 学习场景
- 查看电子教材
- 预览作业文档
- 查看笔记和资料

## 注意事项

1. **Markdown 编辑器**：vditor 已不再积极维护，如需使用原始 Markdown 编辑器，请修改配置
2. **大文件性能**：超大 PDF 或 Excel 文件可能影响性能
3. **字体支持**：某些特殊字体可能无法正确显示
4. **编辑功能**：主要用于查看，编辑功能有限
5. **超链接**：支持 `Ctrl/Cmd+点击` 或双击打开

## 替代方案

如果需要更专业的单一格式查看器：

### 专业 PDF 查看
- **vscode-pdf**: https://github.com/mathematic-inc/vscode-pdf
- 更轻量，性能更好

### 专业 Word 查看
- **Docx-Viewer**: https://github.com/skfrost19/Docx-Viewer
- 更现代化的 UI，更好的文档大纲

## 相关资源

- **GitHub 仓库**: https://github.com/cweijan/vscode-office
- **问题反馈**: https://github.com/cweijan/vscode-office/issues
- **VS Code 市场**: 搜索 "Office Viewer"

## 开发者信息

- **作者**: cweijan
- **许可**: MIT License
- **维护状态**: 活跃维护中

---

**提示**：此技能主要用于在 IDE 中快速查看办公文档，如需深度编辑，建议使用专业的 Office 软件。
