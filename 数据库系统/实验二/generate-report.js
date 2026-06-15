const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, LevelFormat, HeadingLevel,
  BorderStyle, WidthType, ShadingType, PageNumber, PageBreak
} = require('docx');

const border = { style: BorderStyle.SINGLE, size: 1, color: '333333' };
const borders = { top: border, bottom: border, left: border, right: border };
const cellMargins = { top: 60, bottom: 60, left: 100, right: 100 };

function tblCell(text, opts = {}) {
  return new TableCell({
    borders,
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
    shading: opts.shading ? { fill: opts.shading, type: ShadingType.CLEAR } : undefined,
    margins: cellMargins,
    verticalAlign: opts.vAlign || 'center',
    children: [new Paragraph({
      alignment: opts.align || AlignmentType.CENTER,
      children: [new TextRun({ text: String(text), bold: !!opts.bold, font: 'Arial', size: opts.fontSize || 20 })]
    })]
  });
}

function p(text, opts = {}) {
  return new Paragraph({
    spacing: { before: opts.before || 80, after: opts.after || 80, line: 360 },
    indent: opts.indent ? { firstLine: 480 } : undefined,
    alignment: opts.align || AlignmentType.LEFT,
    children: [new TextRun({ text, font: 'Arial', size: opts.size || 24, bold: !!opts.bold })]
  });
}

function heading(text, level) {
  return new Paragraph({
    heading: level,
    children: [new TextRun({ text, font: 'Arial', bold: true, size: level === HeadingLevel.HEADING_1 ? 32 : level === HeadingLevel.HEADING_2 ? 28 : 24 })],
    spacing: { before: 240, after: 120 }
  });
}

const doc = new Document({
  styles: {
    default: { document: { run: { font: 'Arial', size: 24 } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 32, bold: true, font: 'Arial', color: '1a73e8' },
        paragraph: { spacing: { before: 360, after: 200 }, outlineLevel: 0 } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 28, bold: true, font: 'Arial', color: '333333' },
        paragraph: { spacing: { before: 280, after: 160 }, outlineLevel: 1 } },
    ]
  },
  numbering: {
    config: [
      { reference: 'bullets', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
      { reference: 'numbers', levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
      { reference: 'numbers2', levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
      }
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: '1a73e8', space: 4 } },
          children: [new TextRun({ text: '数据库系统 Project 实验报告', font: 'Arial', size: 18, color: '888888', italics: true })]
        })]
      })
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: '第 ', font: 'Arial', size: 18 }), new TextRun({ children: [PageNumber.CURRENT], font: 'Arial', size: 18 }), new TextRun({ text: ' 页', font: 'Arial', size: 18 })]
        })]
      })
    },
    children: [
      // ==================== COVER PAGE ====================
      new Paragraph({ spacing: { before: 2400 } }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 400, after: 200 },
        children: [new TextRun({ text: '数据库系统 Project 实验报告', font: 'Arial', size: 52, bold: true, color: '1a73e8' })]
      }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 100, after: 100 },
        border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: '1a73e8', space: 4 } },
        children: [new TextRun({ text: '企业员工管理系统', font: 'Arial', size: 36, color: '333333' })]
      }),
      new Paragraph({ spacing: { before: 600 } }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 200, after: 100 },
        children: [new TextRun({ text: '课程名称：CST21118 数据库系统', font: 'Arial', size: 24, color: '555555' })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 100, after: 100 },
        children: [new TextRun({ text: '实验类型：综合性实验', font: 'Arial', size: 24, color: '555555' })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 100, after: 100 },
        children: [new TextRun({ text: '学期：2025-2026 学年第 2 学期', font: 'Arial', size: 24, color: '555555' })] }),
      new Paragraph({ spacing: { before: 400 } }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 100, after: 60 },
        children: [new TextRun({ text: '小组成员', font: 'Arial', size: 26, bold: true, color: '333333' })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '严浩睿  20240395', font: 'Arial', size: 22, color: '555555' })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '马沛霖  20240640', font: 'Arial', size: 22, color: '555555' })] }),

      // Page break
      new Paragraph({ children: [new PageBreak()] }),

      // ==================== SECTION 0: 小组分工 ====================
      heading('小组分工', HeadingLevel.HEADING_1),
      p('本项目由严浩睿和马沛霖二人合作完成。具体分工如下：', { indent: true }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [1500, 3763, 3763],
        rows: [
          new TableRow({ children: [
            tblCell('序号', { width: 1500, shading: '1a73e8', bold: true, fontSize: 20, align: AlignmentType.CENTER }),
            tblCell('严浩睿 (20240395)', { width: 3763, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('马沛霖 (20240640)', { width: 3763, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [
            tblCell('1', { width: 1500, align: AlignmentType.CENTER }),
            tblCell('系统需求分析与 E-R 模型设计', { width: 3763 }),
            tblCell('关系模型转换与优化', { width: 3763 }),
          ]}),
          new TableRow({ children: [
            tblCell('2', { width: 1500, align: AlignmentType.CENTER }),
            tblCell('数据库建表与初始化脚本编写', { width: 3763 }),
            tblCell('Web 前端界面设计与开发', { width: 3763 }),
          ]}),
          new TableRow({ children: [
            tblCell('3', { width: 1500, align: AlignmentType.CENTER }),
            tblCell('后端 API 开发（Employee、Salary 模块）', { width: 3763 }),
            tblCell('后端 API 开发（Department、Position、Attendance 模块）', { width: 3763 }),
          ]}),
          new TableRow({ children: [
            tblCell('4', { width: 1500, align: AlignmentType.CENTER }),
            tblCell('系统测试与调试', { width: 3763 }),
            tblCell('实验报告撰写与排版', { width: 3763 }),
          ]}),
        ]
      }),

      p('两人在整个开发过程中密切协作，通过代码审查和定期讨论确保系统设计的一致性和代码质量。', { indent: true }),

      // ==================== SECTION 1: 背景分析 ====================
      heading('1. 企业员工管理系统背景分析', HeadingLevel.HEADING_1),

      heading('1.1 系统开发背景', HeadingLevel.HEADING_2),
      p('随着企业规模的不断扩大和组织结构的日益复杂，传统的人工管理方式已经难以满足现代企业对人力资源管理效率的要求。企业员工管理系统可以帮助企业实现员工信息的集中管理、部门结构的清晰规划、考勤数据的自动化记录以及薪资计算的精准处理。一个设计良好的员工管理系统能够显著提升企业运营效率、降低管理成本，并为企业决策提供数据支持。', { indent: true }),

      heading('1.2 系统需求分析', HeadingLevel.HEADING_2),
      p('本系统的核心功能需求包括：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '员工信息管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持员工基本信息的增、删、改、查操作，包括姓名、性别、出生日期、联系方式、入职日期等', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '部门管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '管理企业组织架构，支持部门的创建、修改、删除以及部门负责人的指派', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '职位管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '定义企业中的各类职位及其薪资范围，为员工分配职位', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '考勤管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '记录员工的每日出勤情况，包括签到/签退时间，支持迟到、早退、缺勤等状态标记', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '薪资管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '管理员工薪资信息，包括基本工资、奖金、扣款，自动计算实发工资', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '数据统计：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '提供仪表盘概览，展示各部门员工分布等统计信息', font: 'Arial', size: 24 })] }),

      heading('1.3 技术选型', HeadingLevel.HEADING_2),
      p('系统采用 B/S 架构，前端基于 HTML + CSS + JavaScript 构建单页应用 (SPA)，后端使用 Node.js + Express 框架提供 RESTful API 接口。数据库选用 SQLite，其轻量级、零配置的特性适合开发和小规模部署场景。数据库操作通过 better-sqlite3 库实现，SQL 语法兼容 OpenGauss/PostgreSQL 标准。', { indent: true }),

      // ==================== SECTION 2: 概念设计 ====================
      new Paragraph({ children: [new PageBreak()] }),
      heading('2. 企业员工管理系统概念设计', HeadingLevel.HEADING_1),

      heading('2.1 E-R 模型设计', HeadingLevel.HEADING_2),
      p('根据需求分析，系统包含以下 5 个实体：员工（Employee）、部门（Department）、职位（Position）、考勤（Attendance）和薪资（Salary）。', { indent: true }),

      heading('2.1.1 实体及属性定义', HeadingLevel.HEADING_2),

      p('（1）部门实体 (Department)', { bold: true, before: 120 }),
      p('部门实体描述企业组织结构的基本单位。包含以下属性：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '部门编号 (dept_id)：主键，唯一标识每个部门', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '部门名称 (dept_name)：部门的唯一名称，不能为空', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '负责人编号 (manager_id)：外键，引用员工实体', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '位置 (location)：部门办公地点', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '描述 (description)：部门职能描述', font: 'Arial', size: 24 })] }),

      p('（2）职位实体 (Position)', { bold: true, before: 120 }),
      p('职位实体定义企业中的岗位类型及对应的薪资范围。包含以下属性：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '职位编号 (pos_id)：主键，唯一标识每个职位', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '职位名称 (pos_name)：职位的名称，不能为空', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '职位描述 (pos_description)：职位职责描述', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '最低薪资 (salary_min)：该职位的最低薪资范围', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '最高薪资 (salary_max)：该职位的最高薪资范围', font: 'Arial', size: 24 })] }),

      p('（3）员工实体 (Employee)', { bold: true, before: 120 }),
      p('员工实体是系统的核心实体，记录企业所有员工的基本信息。包含以下属性：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '员工编号 (emp_id)：主键，唯一标识每个员工', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '姓名 (emp_name)：员工姓名，不能为空', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '性别 (gender)：男 (M) 或女 (F)', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '出生日期 (birth_date)：员工的出生日期', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '电话 (phone)：联系电话', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '邮箱 (email)：电子邮箱地址', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '入职日期 (hire_date)：入职日期，不能为空', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '部门编号 (dept_id)：外键，引用部门实体', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '职位编号 (pos_id)：外键，引用职位实体', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '状态 (status)：在职或离职', font: 'Arial', size: 24 })] }),

      p('（4）考勤实体 (Attendance)', { bold: true, before: 120 }),
      p('考勤实体记录员工每日的考勤情况。包含以下属性：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '考勤编号 (att_id)：主键，唯一标识每条考勤记录', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '员工编号 (emp_id)：外键，引用员工实体', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '日期 (att_date)：考勤日期', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '签到时间 (check_in_time)：上班打卡时间', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '签退时间 (check_out_time)：下班打卡时间', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '考勤状态 (status)：正常、迟到、早退、缺勤、请假', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '备注 (remark)：补充说明', font: 'Arial', size: 24 })] }),

      p('（5）薪资实体 (Salary)', { bold: true, before: 120 }),
      p('薪资实体记录员工的工资发放信息。包含以下属性：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '薪资编号 (salary_id)：主键，唯一标识每条薪资记录', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '员工编号 (emp_id)：外键，引用员工实体', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '基本工资 (base_salary)：员工的基本工资', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '奖金 (bonus)：绩效奖金或额外奖励', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '扣除 (deduction)：缺勤扣款或其他扣除项', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '实发工资 (total_salary)：计算列，base_salary + bonus - deduction', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '发放日期 (pay_date)：工资发放日期', font: 'Arial', size: 24 })] }),

      // E-R diagram representation
      heading('2.1.2 实体间联系', HeadingLevel.HEADING_2),
      p('系统中的实体间存在以下联系：', { indent: true }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '归属联系 (Department - Employee)：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '一个部门可以有多个员工，一个员工只能属于一个部门。（1:N 联系）', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '担任联系 (Position - Employee)：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '一个职位可以被多个员工担任，一个员工只能有一个职位。（1:N 联系）', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '管理联系 (Employee - Department)：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '一个部门有一个负责人（经理），该负责人本身也是一个员工。（1:1 联系）', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '考勤联系 (Employee - Attendance)：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '一个员工可以有多条考勤记录，每条考勤记录只属于一个员工。（1:N 联系）', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '薪资联系 (Employee - Salary)：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '一个员工可以有多条薪资发放记录，每条薪资记录只属于一个员工。（1:N 联系）', font: 'Arial', size: 24 })] }),

      // E-R diagram visualization using text
      heading('2.1.3 E-R 图', HeadingLevel.HEADING_2),
      p('以下为系统的 E-R 图（使用文本表示）：', { indent: true }),

      new Paragraph({ spacing: { before: 120, after: 60 }, children: [
        new TextRun({ text: '┌──────────────────┐     1     N     ┌──────────────────┐', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│    Department     │◇───────────────│    Employee       │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '├──────────────────┤                 ├──────────────────┤', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│ PK  dept_id      │                 │ PK  emp_id       │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     dept_name     │                 │     emp_name      │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     location      │                 │     gender        │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     description   │                 │     birth_date    │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│ FK  manager_id    │◄────────────────│     phone         │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '└──────────────────┘    1:1 管理      │     email         │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                                      │     hire_date     │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '┌──────────────────┐     1     N     │ FK  dept_id       │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│    Position       │◇───────────────│ FK  pos_id        │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '├──────────────────┤                 │     status         │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│ PK  pos_id       │                 └────────┬─────────┘', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     pos_name      │                 ｜1                 ｜1', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     pos_desc      │                 ｜                  ｜', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     salary_min    │                 │N                 │N', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '│     salary_max    │          ┌──────┴──────┐   ┌──────┴──────┐', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '└──────────────────┘          │  Attendance  │   │   Salary     │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              ├──────────────┤   ├──────────────┤', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              │ PK  att_id   │   │ PK  salary_id│', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              │ FK  emp_id   │   │ FK  emp_id   │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              │     att_date  │   │ base_salary  │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              │     status    │   │ bonus        │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              │     remark    │   │ deduction    │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 60 }, children: [
        new TextRun({ text: '                              └──────────────┘   │ total_salary │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: '                                                 │ pay_date     │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 120 }, children: [
        new TextRun({ text: '                                                 └──────────────┘', font: 'Courier New', size: 18 })
      ]}),
      p('图 1. 企业员工管理系统 E-R 图', { align: AlignmentType.CENTER, before: 0, after: 200 }),

      // ==================== SECTION 3: 关系模型设计 ====================
      new Paragraph({ children: [new PageBreak()] }),
      heading('3. 企业员工管理系统的关系模型及优化', HeadingLevel.HEADING_1),

      heading('3.1 E-R 图向关系模型的转换', HeadingLevel.HEADING_2),
      p('根据 E-R 图向关系模型的转换规则，将上述 5 个实体直接转换为 5 个关系模式（表），实体间的 1:N 联系通过在 N 端引入外键来实现，1:1 联系通过在部门表中引入 manager_id 外键来实现。', { indent: true }),

      heading('3.2 关系模式定义', HeadingLevel.HEADING_2),
      p('（1）部门关系模式', { bold: true }),
      p('Department (dept_id, dept_name, manager_id, location, description)', { indent: true, before: 40, after: 40 }),
      p('主键：dept_id', { indent: true, before: 40 }),
      p('外键：manager_id 引用 Employee(emp_id)', { indent: true }),

      p('（2）职位关系模式', { bold: true }),
      p('Position (pos_id, pos_name, pos_description, salary_min, salary_max)', { indent: true, before: 40, after: 40 }),
      p('主键：pos_id', { indent: true, before: 40 }),

      p('（3）员工关系模式', { bold: true }),
      p('Employee (emp_id, emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id, status)', { indent: true, before: 40, after: 40 }),
      p('主键：emp_id', { indent: true, before: 40 }),
      p('外键：dept_id 引用 Department(dept_id)，pos_id 引用 Position(pos_id)', { indent: true }),

      p('（4）考勤关系模式', { bold: true }),
      p('Attendance (att_id, emp_id, att_date, check_in_time, check_out_time, status, remark)', { indent: true, before: 40, after: 40 }),
      p('主键：att_id', { indent: true, before: 40 }),
      p('外键：emp_id 引用 Employee(emp_id)', { indent: true }),

      p('（5）薪资关系模式', { bold: true }),
      p('Salary (salary_id, emp_id, base_salary, bonus, deduction, total_salary, pay_date)', { indent: true, before: 40, after: 40 }),
      p('主键：salary_id', { indent: true, before: 40 }),
      p('外键：emp_id 引用 Employee(emp_id)', { indent: true }),
      p('计算列：total_salary = base_salary + bonus - deduction', { indent: true }),

      heading('3.3 关系模型的规范化与优化', HeadingLevel.HEADING_2),
      p('本系统设计已达到第三范式（3NF）要求，不存在非主属性对码的部分函数依赖和传递函数依赖：', { indent: true }),

      new Paragraph({ numbering: { reference: 'numbers2', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '第一范式（1NF）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '所有属性都是不可分的基本数据项。每个表的主键唯一标识一条记录，每列只包含单一值。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers2', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '第二范式（2NF）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '每个非主属性都完全函数依赖于主键。所有表的主键都是单属性码，不存在部分依赖问题。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers2', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '第三范式（3NF）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '不存在非主属性对码的传递函数依赖。例如，在 Employee 表中，部门名称（dept_name）不直接存储在 Employee 表中，而是通过 dept_id 外键关联到 Department 表，避免了数据冗余和更新异常。', font: 'Arial', size: 24 })] }),

      p('优化措施：', { bold: true, before: 120 }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '在 Attendance 表上建立 (emp_id, att_date) 的联合索引，加速按员工和日期查询考勤记录', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '在 Salary 表中使用计算列（GENERATED ALWAYS AS）自动计算实发工资，保证数据一致性', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '设置外键约束的级联删除策略：删除员工时自动删除其考勤和薪资记录（ON DELETE CASCADE），删除部门/职位时将员工对应字段设为 NULL（ON DELETE SET NULL），防止孤立数据', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: 'Employee 表的 dept_id 和 pos_id 字段允许 NULL，支持新员工在分配部门和职位前的临时状态', font: 'Arial', size: 24 })] }),

      // System function module diagram
      heading('3.4 系统功能模块图', HeadingLevel.HEADING_2),
      p('系统采用模块化设计，主要功能模块如下：', { indent: true }),

      new Paragraph({ spacing: { before: 80, after: 40 }, children: [
        new TextRun({ text: '┌─────────────────────────────────────────────────────────┐', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '│              企业员工管理系统                            │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '├────────────┬───────────┬──────────┬──────────┬─────────┤', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '│ 仪表盘模块  │ 员工管理   │ 部门管理  │ 职位管理  │ 考勤管理  │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '├────────────┼───────────┼──────────┼──────────┼─────────┤', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '│ 薪资管理    │ 数据统计   │ 搜索过滤  │ CRUD API  │ 数据库层  │', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: '└────────────┴───────────┴──────────┴──────────┴─────────┘', font: 'Courier New', size: 18 })
      ]}),
      p('图 2. 系统功能模块图', { align: AlignmentType.CENTER, before: 0 }),

      // ==================== SECTION 4: 物理设计 ====================
      new Paragraph({ children: [new PageBreak()] }),
      heading('4. 企业员工管理系统数据库物理设计', HeadingLevel.HEADING_1),

      heading('4.1 数据库选择', HeadingLevel.HEADING_2),
      p('本系统基于 OpenGauss 数据库标准进行设计，开发阶段使用 SQLite 作为实现数据库。SQLite 是一款轻量级的关系数据库，支持标准 SQL 语法，其 DDL/DML 语句与 OpenGauss 高度兼容。系统的所有 SQL 语句均遵循 SQL 标准，可无缝迁移至 OpenGauss 或 PostgreSQL 环境。', { indent: true }),

      heading('4.2 数据表物理结构', HeadingLevel.HEADING_2),
      p('以下是各数据表的详细物理结构定义：', { indent: true }),

      p('表 1. department（部门表）', { bold: true, align: AlignmentType.CENTER, before: 160 }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [2000, 1200, 1200, 1163, 1163, 2300],
        rows: [
          new TableRow({ children: [
            tblCell('字段名', { width: 2000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('类型', { width: 1200, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('约束', { width: 1200, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('主键', { width: 1163, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('外键', { width: 1163, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('说明', { width: 2300, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [tblCell('dept_id', { width: 2000 }), tblCell('INTEGER', { width: 1200 }), tblCell('NOT NULL', { width: 1200 }), tblCell('PRI', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('部门编号，自增', { width: 2300 })] }),
          new TableRow({ children: [tblCell('dept_name', { width: 2000 }), tblCell('VARCHAR(100)', { width: 1200 }), tblCell('NOT NULL, UNIQUE', { width: 1200 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('部门名称', { width: 2300 })] }),
          new TableRow({ children: [tblCell('manager_id', { width: 2000 }), tblCell('INTEGER', { width: 1200 }), tblCell('', { width: 1200 }), tblCell('', { width: 1163 }), tblCell('FK', { width: 1163 }), tblCell('负责人编号，引用 employee', { width: 2300 })] }),
          new TableRow({ children: [tblCell('location', { width: 2000 }), tblCell('VARCHAR(200)', { width: 1200 }), tblCell('', { width: 1200 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('办公地点', { width: 2300 })] }),
          new TableRow({ children: [tblCell('description', { width: 2000 }), tblCell('TEXT', { width: 1200 }), tblCell('', { width: 1200 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('部门描述', { width: 2300 })] }),
        ]
      }),
      p(' ', { before: 40, after: 40 }),

      p('表 2. position（职位表）', { bold: true, align: AlignmentType.CENTER, before: 160 }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [2000, 1300, 1000, 1163, 1163, 2400],
        rows: [
          new TableRow({ children: [
            tblCell('字段名', { width: 2000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('类型', { width: 1300, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('约束', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('主键', { width: 1163, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('外键', { width: 1163, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('说明', { width: 2400, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [tblCell('pos_id', { width: 2000 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1000 }), tblCell('PRI', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('职位编号，自增', { width: 2400 })] }),
          new TableRow({ children: [tblCell('pos_name', { width: 2000 }), tblCell('VARCHAR(100)', { width: 1300 }), tblCell('NOT NULL, UNIQUE', { width: 1000 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('职位名称', { width: 2400 })] }),
          new TableRow({ children: [tblCell('pos_description', { width: 2000 }), tblCell('TEXT', { width: 1300 }), tblCell('', { width: 1000 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('职位描述', { width: 2400 })] }),
          new TableRow({ children: [tblCell('salary_min', { width: 2000 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('', { width: 1000 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('最低薪资', { width: 2400 })] }),
          new TableRow({ children: [tblCell('salary_max', { width: 2000 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('', { width: 1000 }), tblCell('', { width: 1163 }), tblCell('', { width: 1163 }), tblCell('最高薪资', { width: 2400 })] }),
        ]
      }),
      p(' ', { before: 40, after: 40 }),

      p('表 3. employee（员工表）', { bold: true, align: AlignmentType.CENTER, before: 160 }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [1800, 1300, 1100, 1000, 1000, 2826],
        rows: [
          new TableRow({ children: [
            tblCell('字段名', { width: 1800, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('类型', { width: 1300, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('约束', { width: 1100, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('主键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('外键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('说明', { width: 2826, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [tblCell('emp_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('PRI', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('员工编号，自增', { width: 2826 })] }),
          new TableRow({ children: [tblCell('emp_name', { width: 1800 }), tblCell('VARCHAR(50)', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('员工姓名', { width: 2826 })] }),
          new TableRow({ children: [tblCell('gender', { width: 1800 }), tblCell('CHAR(1)', { width: 1300 }), tblCell('CHECK', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('性别 M/F', { width: 2826 })] }),
          new TableRow({ children: [tblCell('birth_date', { width: 1800 }), tblCell('DATE', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('出生日期', { width: 2826 })] }),
          new TableRow({ children: [tblCell('phone', { width: 1800 }), tblCell('VARCHAR(20)', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('联系电话', { width: 2826 })] }),
          new TableRow({ children: [tblCell('email', { width: 1800 }), tblCell('VARCHAR(100)', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('电子邮箱', { width: 2826 })] }),
          new TableRow({ children: [tblCell('hire_date', { width: 1800 }), tblCell('DATE', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('入职日期', { width: 2826 })] }),
          new TableRow({ children: [tblCell('dept_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('FK', { width: 1000 }), tblCell('部门编号，引用 department', { width: 2826 })] }),
          new TableRow({ children: [tblCell('pos_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('FK', { width: 1000 }), tblCell('职位编号，引用 position', { width: 2826 })] }),
          new TableRow({ children: [tblCell('status', { width: 1800 }), tblCell('VARCHAR(20)', { width: 1300 }), tblCell('DEFAULT 在职', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('员工状态', { width: 2826 })] }),
        ]
      }),
      p(' ', { before: 40, after: 40 }),

      p('表 4. attendance（考勤表）', { bold: true, align: AlignmentType.CENTER, before: 160 }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [1800, 1300, 1100, 1000, 1000, 2826],
        rows: [
          new TableRow({ children: [
            tblCell('字段名', { width: 1800, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('类型', { width: 1300, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('约束', { width: 1100, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('主键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('外键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('说明', { width: 2826, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [tblCell('att_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('PRI', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('考勤编号，自增', { width: 2826 })] }),
          new TableRow({ children: [tblCell('emp_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('FK', { width: 1000 }), tblCell('员工编号，引用 employee', { width: 2826 })] }),
          new TableRow({ children: [tblCell('att_date', { width: 1800 }), tblCell('DATE', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('考勤日期', { width: 2826 })] }),
          new TableRow({ children: [tblCell('check_in_time', { width: 1800 }), tblCell('TIME', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('签到时间', { width: 2826 })] }),
          new TableRow({ children: [tblCell('check_out_time', { width: 1800 }), tblCell('TIME', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('签退时间', { width: 2826 })] }),
          new TableRow({ children: [tblCell('status', { width: 1800 }), tblCell('VARCHAR(20)', { width: 1300 }), tblCell('DEFAULT 正常', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('考勤状态', { width: 2826 })] }),
          new TableRow({ children: [tblCell('remark', { width: 1800 }), tblCell('TEXT', { width: 1300 }), tblCell('', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('备注', { width: 2826 })] }),
        ]
      }),
      p(' ', { before: 40, after: 40 }),

      p('表 5. salary（薪资表）', { bold: true, align: AlignmentType.CENTER, before: 160 }),

      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [1800, 1300, 1100, 1000, 1000, 2826],
        rows: [
          new TableRow({ children: [
            tblCell('字段名', { width: 1800, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('类型', { width: 1300, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('约束', { width: 1100, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('主键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('外键', { width: 1000, shading: '1a73e8', bold: true, fontSize: 20 }),
            tblCell('说明', { width: 2826, shading: '1a73e8', bold: true, fontSize: 20 }),
          ]}),
          new TableRow({ children: [tblCell('salary_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('PRI', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('薪资编号，自增', { width: 2826 })] }),
          new TableRow({ children: [tblCell('emp_id', { width: 1800 }), tblCell('INTEGER', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('FK', { width: 1000 }), tblCell('员工编号，引用 employee', { width: 2826 })] }),
          new TableRow({ children: [tblCell('base_salary', { width: 1800 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('基本工资', { width: 2826 })] }),
          new TableRow({ children: [tblCell('bonus', { width: 1800 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('DEFAULT 0', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('奖金', { width: 2826 })] }),
          new TableRow({ children: [tblCell('deduction', { width: 1800 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('DEFAULT 0', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('扣除项', { width: 2826 })] }),
          new TableRow({ children: [tblCell('total_salary', { width: 1800 }), tblCell('DECIMAL(10,2)', { width: 1300 }), tblCell('GENERATED', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('实发工资（计算列）', { width: 2826 })] }),
          new TableRow({ children: [tblCell('pay_date', { width: 1800 }), tblCell('DATE', { width: 1300 }), tblCell('NOT NULL', { width: 1100 }), tblCell('', { width: 1000 }), tblCell('', { width: 1000 }), tblCell('发放日期', { width: 2826 })] }),
        ]
      }),

      heading('4.3 索引设计', HeadingLevel.HEADING_2),
      p('为优化查询性能，设计了以下索引：', { indent: true }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '主键索引：所有表的主键自动创建唯一索引，保证主键查询效率', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '外键索引：employee 表的 dept_id、pos_id 字段，attendance 和 salary 表的 emp_id 字段为频繁 JOIN 操作提供加速', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '唯一约束：dept_name、pos_name 创建唯一索引，保证名称不重复', font: 'Arial', size: 24 })] }),

      heading('4.4 安全性设计', HeadingLevel.HEADING_2),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '使用参数化查询（Prepared Statements）防止 SQL 注入攻击', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '外键约束确保数据引用完整性', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: 'CHECK 约束限制 gender 字段只能为 M 或 F', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
        children: [new TextRun({ text: '删除部门/职位时使用 SET NULL 策略保留员工数据，删除员工时使用 CASCADE 策略清理关联数据', font: 'Arial', size: 24 })] }),

      // ==================== SECTION 5: 程序设计与数据库连接 ====================
      new Paragraph({ children: [new PageBreak()] }),
      heading('5. 程序设计语言与数据库的连接、操作', HeadingLevel.HEADING_1),

      heading('5.1 系统架构', HeadingLevel.HEADING_2),
      p('本系统采用经典的 B/S（Browser/Server）三层架构：', { indent: true }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '表现层（View）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '基于 HTML5 + CSS3 + JavaScript 构建的单页应用（SPA），提供响应式用户界面。采用 Fetch API 与后端进行异步数据交互，实现无刷新页面更新。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '业务逻辑层（Controller）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '使用 Node.js + Express 框架构建 RESTful API 服务。Express 中间件处理 JSON 解析、静态文件服务和路由分发。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '数据访问层（Model）：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '通过 better-sqlite3 库连接 SQLite 数据库，所有数据操作均使用 Prepared Statement 方式，防止 SQL 注入并提升执行效率。', font: 'Arial', size: 24 })] }),

      heading('5.2 数据库连接实现', HeadingLevel.HEADING_2),
      p('系统通过 better-sqlite3 库实现与数据库的连接。每次请求时，getDb() 函数创建新的数据库连接，并启用 WAL 日志模式和强制外键约束。关键代码如下：', { indent: true }),

      new Paragraph({ spacing: { before: 120, after: 40 }, children: [
        new TextRun({ text: 'function getDb() {', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '  const db = new Database(path.join(__dirname, \'employee.db\'));', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '  db.pragma(\'journal_mode = WAL\');', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: '  db.pragma(\'foreign_keys = ON\');', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: '  return db;', font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: '}', font: 'Courier New', size: 18 })
      ]}),

      heading('5.3 CRUD 操作实现', HeadingLevel.HEADING_2),
      p('系统为每个实体（Employee、Department、Position、Attendance、Salary）实现了完整的 CRUD API 接口。以下以员工管理为例展示核心操作实现：', { indent: true }),

      p('（1）查询操作（Read）', { bold: true, before: 120 }),
      p('使用 LEFT JOIN 查询员工的同时关联获取部门名称和职位名称：', { indent: true }),
      new Paragraph({ spacing: { before: 80, after: 40 }, children: [
        new TextRun({ text: "app.get('/api/employees', (req, res) => {", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const db = getDb();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const employees = db.prepare(`", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    SELECT e.*, d.dept_name, p.pos_name", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    FROM employee e", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    LEFT JOIN department d ON e.dept_id = d.dept_id", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    LEFT JOIN position p ON e.pos_id = p.pos_id", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    ORDER BY e.emp_id", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  `).all();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.close();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  res.json(employees);", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: "});", font: 'Courier New', size: 18 })
      ]}),

      p('（2）增加操作（Create）', { bold: true, before: 120 }),
      p('使用 INSERT 语句添加新员工，返回自增 ID：', { indent: true }),
      new Paragraph({ spacing: { before: 80, after: 40 }, children: [
        new TextRun({ text: "app.post('/api/employees', (req, res) => {", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const { emp_name, gender, ... } = req.body;", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const db = getDb();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const result = db.prepare(", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    'INSERT INTO employee (...) VALUES (...)'", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  ).run(emp_name, gender, ...);", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.close();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  res.json({ id: result.lastInsertRowid });", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: "});", font: 'Courier New', size: 18 })
      ]}),

      p('（3）更新操作（Update）', { bold: true, before: 120 }),
      p('使用 UPDATE 语句修改员工信息：', { indent: true }),
      new Paragraph({ spacing: { before: 80, after: 40 }, children: [
        new TextRun({ text: "app.put('/api/employees/:id', (req, res) => {", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const { id } = req.params;", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const db = getDb();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.prepare(", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "    'UPDATE employee SET ... WHERE emp_id = ?'", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  ).run(..., id);", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.close();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: "});", font: 'Courier New', size: 18 })
      ]}),

      p('（4）删除操作（Delete）', { bold: true, before: 120 }),
      p('使用 DELETE 语句删除员工，级联删除关联数据：', { indent: true }),
      new Paragraph({ spacing: { before: 80, after: 40 }, children: [
        new TextRun({ text: "app.delete('/api/employees/:id', (req, res) => {", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const { id } = req.params;", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  const db = getDb();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.prepare('DELETE FROM employee WHERE emp_id = ?').run(id);", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 40 }, children: [
        new TextRun({ text: "  db.close();", font: 'Courier New', size: 18 })
      ]}),
      new Paragraph({ spacing: { before: 0, after: 80 }, children: [
        new TextRun({ text: "});", font: 'Courier New', size: 18 })
      ]}),

      heading('5.4 前端页面与操作说明', HeadingLevel.HEADING_2),
      p('系统提供了 6 个主要页面：', { indent: true }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '仪表盘：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '展示系统概览统计数据，包括员工总数、部门数量、职位类型、今日考勤人数，以及各部门员工分布的柱状图。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '员工管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持员工的增删改查，按姓名搜索和按部门筛选。表单包含完整的员工信息字段。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '部门管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持部门的增删改查，可为部门指定负责人。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '职位管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持职位的增删改查，定义各职位的薪资范围。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '考勤管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持考勤记录的增删改查，记录签到/签退时间和考勤状态。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '薪资管理：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '支持薪资记录的增删改查，自动计算实发工资。', font: 'Arial', size: 24 })] }),

      heading('5.5 系统运行说明', HeadingLevel.HEADING_2),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '环境要求：Node.js v14.0 或以上版本，npm 包管理器', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '安装依赖：执行 npm install 安装 better-sqlite3、express 等依赖包', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '初始化数据库：执行 node init-db.js 创建数据表和示例数据', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '启动系统：执行 node server.js 启动 Web 服务器', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '访问地址：打开浏览器访问 http://localhost:3000', font: 'Arial', size: 24 })] }),

      // ==================== SECTION 6: 总结 ====================
      new Paragraph({ children: [new PageBreak()] }),
      heading('6. 总结与体会', HeadingLevel.HEADING_1),
      p('通过本次数据库系统 Project，我们完成了企业员工管理系统的完整设计与实现，收获如下：', { indent: true }),

      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '数据库设计能力提升：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '从需求分析出发，掌握了 E-R 模型设计方法，理解了实体、属性、联系的建模过程，学会了如何将 E-R 图正确转换为关系模式，并运用规范化理论优化数据库结构。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: 'SQL 语言熟练运用：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '通过实现 CRUD 操作，深入理解了 INSERT、SELECT、UPDATE、DELETE 语句以及 JOIN 查询、聚合函数等高级 SQL 特性。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '工程实践能力增强：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '使用 Node.js + Express 构建了完整的 Web 应用后端，掌握了 RESTful API 设计、前后端数据交互、数据库连接管理等工程技能。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '安全意识培养：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '学会了使用参数化查询防止 SQL 注入攻击，理解了外键约束和级联操作对数据完整性的保护作用。', font: 'Arial', size: 24 })] }),
      new Paragraph({ numbering: { reference: 'numbers', level: 0 }, spacing: { before: 80, after: 80 },
        children: [new TextRun({ text: '团队协作经验：', font: 'Arial', size: 24, bold: true }), new TextRun({ text: '通过合理的分工协作，两人充分发挥各自优势，在系统设计、前后端开发、报告撰写等方面密切配合，提升了团队协作能力。', font: 'Arial', size: 24 })] }),
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(path.join(__dirname, '实验二_企业员工管理系统_实验报告.docx'), buffer);
  console.log('实验报告已生成: 实验二_企业员工管理系统_实验报告.docx');
});
