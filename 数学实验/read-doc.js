const fs = require('fs');

// 读取 .doc 文件的二进制内容
const filePath = "c:\\Users\\yanha\\Desktop\\数学实验\\实验课\\严浩睿的实验报告模版.doc";
const buffer = fs.readFileSync(filePath);

// 尝试提取所有可打印的 Unicode 字符
let text = '';

// 方法1: 提取所有可能的文本段
for (let i = 0; i < buffer.length - 1; i++) {
    // 查找中文字符 (UTF-16 LE 编码)
    if (buffer[i] >= 0x00 && buffer[i] <= 0xFF && buffer[i+1] >= 0x4E && buffer[i+1] <= 0x9F) {
        try {
            const char = buffer.toString('utf16le', i, i + 2);
            if (/[\u4e00-\u9fa5]/.test(char)) {
                text += char;
            }
        } catch (e) {}
    }
}

console.log("=== 提取的中文内容 ===");
console.log(text);
console.log("\n=== 原始文本搜索 ===");

// 方法2: 查找所有连续的可打印ASCII字符
let asciiText = '';
for (let i = 0; i < buffer.length; i++) {
    const byte = buffer[i];
    // 可打印ASCII或常见标点
    if ((byte >= 32 && byte <= 126) || byte === 0x0A || byte === 0x0D) {
        asciiText += String.fromCharCode(byte);
    } else if (asciiText.length > 0) {
        // 遇到不可打印字符，输出之前积累的文本
        if (asciiText.length > 3) {
            console.log(asciiText);
        }
        asciiText = '';
    }
}

// 方法3: 尝试UTF-16LE解码整个文件
console.log("\n=== UTF-16LE 解码尝试 ===");
try {
    const utf16Text = buffer.toString('utf16le');
    // 过滤出有效字符
    const cleanText = utf16Text.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F]/g, '');
    console.log(cleanText.substring(0, 5000)); // 只显示前5000字符
} catch (e) {
    console.log("UTF-16LE 解码失败:", e.message);
}
