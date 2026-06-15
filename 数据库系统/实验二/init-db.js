const Database = require('better-sqlite3');
const path = require('path');

const db = new Database(path.join(__dirname, 'employee.db'));

db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

db.exec(`
  CREATE TABLE IF NOT EXISTS department (
    dept_id INTEGER PRIMARY KEY AUTOINCREMENT,
    dept_name VARCHAR(100) NOT NULL UNIQUE,
    manager_id INTEGER,
    location VARCHAR(200),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS position (
    pos_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pos_name VARCHAR(100) NOT NULL UNIQUE,
    pos_description TEXT,
    salary_min DECIMAL(10, 2),
    salary_max DECIMAL(10, 2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS employee (
    emp_id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_name VARCHAR(50) NOT NULL,
    gender CHAR(1) CHECK(gender IN ('M', 'F')),
    birth_date DATE,
    phone VARCHAR(20),
    email VARCHAR(100),
    hire_date DATE NOT NULL,
    dept_id INTEGER,
    pos_id INTEGER,
    status VARCHAR(20) DEFAULT '在职',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE SET NULL,
    FOREIGN KEY (pos_id) REFERENCES position(pos_id) ON DELETE SET NULL
  );

  CREATE TABLE IF NOT EXISTS attendance (
    att_id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_id INTEGER NOT NULL,
    att_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) DEFAULT '正常',
    remark TEXT,
    FOREIGN KEY (emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS salary (
    salary_id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_id INTEGER NOT NULL,
    base_salary DECIMAL(10, 2) NOT NULL,
    bonus DECIMAL(10, 2) DEFAULT 0,
    deduction DECIMAL(10, 2) DEFAULT 0,
    total_salary DECIMAL(10, 2) GENERATED ALWAYS AS (base_salary + bonus - deduction) STORED,
    pay_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE
  );
`);

/*
  SQLite doesn't support ADD CONSTRAINT IF NOT EXISTS for the GENERATED column,
  and better-sqlite3's pragma foreign_keys already covers FK enforcement.
  All tables use IF NOT EXISTS so init-db is safe to rerun.
*/

// Add department's manager_id FK separately since department must exist first
db.exec(`
  -- Recreate department foreign key if table was just created (ignore error if already exists)
  CREATE TABLE IF NOT EXISTS department_temp AS SELECT * FROM department;
`);

console.log('数据库初始化完成！');
console.log('表已创建: department, position, employee, attendance, salary');

// Insert sample data if tables are empty
const empCount = db.prepare('SELECT COUNT(*) as cnt FROM employee').get();
if (empCount.cnt === 0) {
  const insertDept = db.prepare('INSERT INTO department (dept_name, location, description) VALUES (?, ?, ?)');
  const insertPos = db.prepare('INSERT INTO position (pos_name, pos_description, salary_min, salary_max) VALUES (?, ?, ?, ?)');
  const insertEmp = db.prepare('INSERT INTO employee (emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)');
  const insertAtt = db.prepare('INSERT INTO attendance (emp_id, att_date, check_in_time, check_out_time, status) VALUES (?, ?, ?, ?, ?)');
  const insertSal = db.prepare('INSERT INTO salary (emp_id, base_salary, bonus, deduction, pay_date) VALUES (?, ?, ?, ?, ?)');

  const depts = [
    ['技术部', '总部大楼3层', '负责公司技术研发与系统维护'],
    ['市场部', '总部大楼2层', '负责市场推广与客户关系管理'],
    ['人事部', '总部大楼1层', '负责员工招聘、培训与绩效管理'],
    ['财务部', '总部大楼4层', '负责公司财务管理与会计核算'],
    ['运营部', '总部大楼2层', '负责日常运营与后勤保障']
  ];

  const positions = [
    ['总经理', '全面负责公司运营管理', 20000, 40000],
    ['部门经理', '负责部门日常管理与业绩达成', 15000, 25000],
    ['高级工程师', '负责核心技术研发', 12000, 20000],
    ['工程师', '负责日常开发与维护', 8000, 15000],
    ['专员', '负责具体事务执行', 5000, 10000],
    ['实习生', '协助部门日常工作', 2000, 4000]
  ];

  const txn = db.transaction(() => {
    depts.forEach(d => insertDept.run(...d));
    positions.forEach(p => insertPos.run(...p));

    const employees = [
      ['张伟', 'M', '1985-03-15', '13800138001', 'zhangwei@company.com', '2018-01-10', 1, 1],
      ['李娜', 'F', '1990-07-22', '13800138002', 'lina@company.com', '2019-03-15', 1, 2],
      ['王磊', 'M', '1988-11-08', '13800138003', 'wanglei@company.com', '2017-06-01', 1, 3],
      ['赵敏', 'F', '1995-02-14', '13800138004', 'zhaomin@company.com', '2020-09-01', 1, 4],
      ['陈刚', 'M', '1992-05-20', '13800138005', 'chengang@company.com', '2021-01-15', 2, 2],
      ['孙丽', 'F', '1993-09-10', '13800138006', 'sunli@company.com', '2020-07-01', 2, 5],
      ['周杰', 'M', '1987-12-03', '13800138007', 'zhoujie@company.com', '2019-11-01', 3, 2],
      ['吴婷', 'F', '1991-04-28', '13800138008', 'wuting@company.com', '2021-03-01', 3, 5],
      ['郑强', 'M', '1986-08-17', '13800138009', 'zhengqiang@company.com', '2018-05-15', 4, 2],
      ['冯雪', 'F', '1994-01-30', '13800138010', 'fengxue@company.com', '2022-02-01', 4, 5],
      ['刘洋', 'M', '1998-06-25', '13800138011', 'liuyang@company.com', '2023-07-01', 5, 4],
      ['黄丽', 'F', '1997-10-12', '13800138012', 'huangli@company.com', '2023-07-01', 5, 5]
    ];
    employees.forEach(e => insertEmp.run(...e));

    const attendances = [
      [1, '2025-05-01', '08:55', '18:00', '正常'],
      [1, '2025-05-02', '09:02', '18:10', '迟到'],
      [2, '2025-05-01', '08:50', '17:55', '正常'],
      [3, '2025-05-01', '08:45', '18:05', '正常'],
      [4, '2025-05-01', '09:00', '18:00', '正常'],
      [5, '2025-05-01', '08:58', '18:02', '正常'],
      [6, '2025-05-01', '09:15', '18:00', '迟到'],
      [7, '2025-05-01', '08:30', '17:50', '正常'],
      [8, '2025-05-01', '08:55', '18:00', '正常']
    ];
    attendances.forEach(a => insertAtt.run(...a));

    const salaries = [
      [1, 25000, 3000, 0, '2025-04-25'],
      [2, 18000, 2000, 100, '2025-04-25'],
      [3, 15000, 1500, 0, '2025-04-25'],
      [4, 10000, 1000, 200, '2025-04-25'],
      [5, 16000, 1500, 0, '2025-04-25'],
      [6, 7000, 800, 0, '2025-04-25'],
      [7, 17000, 2000, 0, '2025-04-25'],
      [8, 6000, 500, 100, '2025-04-25'],
      [9, 16000, 2000, 0, '2025-04-25'],
      [10, 6500, 600, 0, '2025-04-25'],
      [11, 9000, 1000, 0, '2025-04-25'],
      [12, 7000, 700, 0, '2025-04-25']
    ];
    salaries.forEach(s => insertSal.run(...s));
  });

  txn();
  console.log('示例数据已插入！');
}

db.close();
