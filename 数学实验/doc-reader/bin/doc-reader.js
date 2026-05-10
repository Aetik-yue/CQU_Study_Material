#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

function showHelp() {
    console.log(`
Usage: doc-reader <file.doc> [options]

Options:
  -h, --help       Show help information
  -o, --output     Save output to file
  -e, --encoding   Output encoding (utf8, utf16le) [default: utf8]
  -l, --length     Maximum length of output [default: 10000]

Examples:
  doc-reader document.doc
  doc-reader document.doc -o output.txt
  doc-reader document.doc -l 5000
`);
}

function extractTextFromDoc(filePath, options = {}) {
    const buffer = fs.readFileSync(filePath);
    const maxLength = options.maxLength || 10000;
    
    let result = {
        chinese: '',
        ascii: [],
        utf16: ''
    };
    
    // 方法1: 提取中文字符 (UTF-16 LE 编码)
    for (let i = 0; i < buffer.length - 1; i++) {
        if (buffer[i+1] >= 0x4E && buffer[i+1] <= 0x9F) {
            try {
                const char = buffer.toString('utf16le', i, i + 2);
                if (/[\u4e00-\u9fa5]/.test(char)) {
                    result.chinese += char;
                }
            } catch (e) {}
        }
    }
    
    // 方法2: 查找所有连续的可打印ASCII字符
    let asciiText = '';
    for (let i = 0; i < buffer.length; i++) {
        const byte = buffer[i];
        if ((byte >= 32 && byte <= 126) || byte === 0x0A || byte === 0x0D) {
            asciiText += String.fromCharCode(byte);
        } else if (asciiText.length > 0) {
            if (asciiText.length > 3) {
                result.ascii.push(asciiText);
            }
            asciiText = '';
        }
    }
    
    // 方法3: UTF-16LE解码
    try {
        const utf16Text = buffer.toString('utf16le');
        result.utf16 = utf16Text.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F]/g, '').substring(0, maxLength);
    } catch (e) {
        result.utf16 = 'UTF-16LE 解码失败: ' + e.message;
    }
    
    return result;
}

function main() {
    const args = process.argv.slice(2);
    
    if (args.length === 0 || args.includes('-h') || args.includes('--help')) {
        showHelp();
        process.exit(0);
    }
    
    const filePath = args[0];
    
    if (!fs.existsSync(filePath)) {
        console.error(`Error: File not found: ${filePath}`);
        process.exit(1);
    }
    
    const ext = path.extname(filePath).toLowerCase();
    if (ext !== '.doc') {
        console.warn(`Warning: File extension is ${ext}, expected .doc`);
    }
    
    // Parse options
    let outputFile = null;
    let maxLength = 10000;
    
    for (let i = 1; i < args.length; i++) {
        if ((args[i] === '-o' || args[i] === '--output') && args[i + 1]) {
            outputFile = args[i + 1];
            i++;
        }
        if ((args[i] === '-l' || args[i] === '--length') && args[i + 1]) {
            maxLength = parseInt(args[i + 1]) || 10000;
            i++;
        }
    }
    
    console.log(`Reading: ${filePath}\n`);
    
    const result = extractTextFromDoc(filePath, { maxLength });
    
    let output = '';
    
    // 输出中文内容
    if (result.chinese.length > 0) {
        output += '=== 中文内容 ===\n';
        output += result.chinese.substring(0, maxLength) + '\n\n';
    }
    
    // 输出ASCII文本
    if (result.ascii.length > 0) {
        output += '=== ASCII 文本 ===\n';
        result.ascii.slice(0, 50).forEach(text => {
            output += text + '\n';
        });
        output += '\n';
    }
    
    // 输出UTF-16LE解码内容
    output += '=== 完整文本 (UTF-16LE) ===\n';
    output += result.utf16 + '\n';
    
    if (outputFile) {
        fs.writeFileSync(outputFile, output, 'utf8');
        console.log(`Output saved to: ${outputFile}`);
    } else {
        console.log(output);
    }
}

main();
