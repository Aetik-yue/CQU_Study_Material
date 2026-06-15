const express = require('express');
const Database = require('better-sqlite3');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

function getDb() {
  const db = new Database(path.join(__dirname, 'employee.db'));
  db.pragma('journal_mode = WAL');
  db.pragma('foreign_keys = ON');
  return db;
}

// ==================== Dashboard API ====================
app.get('/api/dashboard', (req, res) => {
  const db = getDb();
  const empCount = db.prepare('SELECT COUNT(*) as cnt FROM employee').get();
  const deptCount = db.prepare('SELECT COUNT(*) as cnt FROM department').get();
  const posCount = db.prepare('SELECT COUNT(*) as cnt FROM position').get();
  const attToday = db.prepare("SELECT COUNT(*) as cnt FROM attendance WHERE att_date = date('now')").get();
  db.close();
  res.json({ empCount: empCount.cnt, deptCount: deptCount.cnt, posCount: posCount.cnt, attToday: attToday.cnt });
});

// ==================== Department APIs ====================
app.get('/api/departments', (req, res) => {
  const db = getDb();
  const depts = db.prepare(`
    SELECT d.*, e.emp_name as manager_name
    FROM department d
    LEFT JOIN employee e ON d.manager_id = e.emp_id
    ORDER BY d.dept_id
  `).all();
  db.close();
  res.json(depts);
});

app.post('/api/departments', (req, res) => {
  const { dept_name, manager_id, location, description } = req.body;
  const db = getDb();
  const result = db.prepare(
    'INSERT INTO department (dept_name, manager_id, location, description) VALUES (?, ?, ?, ?)'
  ).run(dept_name, manager_id || null, location, description);
  db.close();
  res.json({ id: result.lastInsertRowid, message: '部门添加成功' });
});

app.put('/api/departments/:id', (req, res) => {
  const { id } = req.params;
  const { dept_name, manager_id, location, description } = req.body;
  const db = getDb();
  db.prepare(
    'UPDATE department SET dept_name = ?, manager_id = ?, location = ?, description = ? WHERE dept_id = ?'
  ).run(dept_name, manager_id || null, location, description, id);
  db.close();
  res.json({ message: '部门更新成功' });
});

app.delete('/api/departments/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  db.prepare('DELETE FROM department WHERE dept_id = ?').run(id);
  db.close();
  res.json({ message: '部门删除成功' });
});

// ==================== Position APIs ====================
app.get('/api/positions', (req, res) => {
  const db = getDb();
  const positions = db.prepare('SELECT * FROM position ORDER BY pos_id').all();
  db.close();
  res.json(positions);
});

app.post('/api/positions', (req, res) => {
  const { pos_name, pos_description, salary_min, salary_max } = req.body;
  const db = getDb();
  const result = db.prepare(
    'INSERT INTO position (pos_name, pos_description, salary_min, salary_max) VALUES (?, ?, ?, ?)'
  ).run(pos_name, pos_description, salary_min, salary_max);
  db.close();
  res.json({ id: result.lastInsertRowid, message: '职位添加成功' });
});

app.put('/api/positions/:id', (req, res) => {
  const { id } = req.params;
  const { pos_name, pos_description, salary_min, salary_max } = req.body;
  const db = getDb();
  db.prepare(
    'UPDATE position SET pos_name = ?, pos_description = ?, salary_min = ?, salary_max = ? WHERE pos_id = ?'
  ).run(pos_name, pos_description, salary_min, salary_max, id);
  db.close();
  res.json({ message: '职位更新成功' });
});

app.delete('/api/positions/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  db.prepare('DELETE FROM position WHERE pos_id = ?').run(id);
  db.close();
  res.json({ message: '职位删除成功' });
});

// ==================== Employee APIs ====================
app.get('/api/employees', (req, res) => {
  const db = getDb();
  const employees = db.prepare(`
    SELECT e.*, d.dept_name, p.pos_name
    FROM employee e
    LEFT JOIN department d ON e.dept_id = d.dept_id
    LEFT JOIN position p ON e.pos_id = p.pos_id
    ORDER BY e.emp_id
  `).all();
  db.close();
  res.json(employees);
});

app.get('/api/employees/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  const employee = db.prepare(`
    SELECT e.*, d.dept_name, p.pos_name
    FROM employee e
    LEFT JOIN department d ON e.dept_id = d.dept_id
    LEFT JOIN position p ON e.pos_id = p.pos_id
    WHERE e.emp_id = ?
  `).get(id);
  db.close();
  if (employee) res.json(employee);
  else res.status(404).json({ message: '员工不存在' });
});

app.post('/api/employees', (req, res) => {
  const { emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id, status } = req.body;
  const db = getDb();
  const result = db.prepare(
    'INSERT INTO employee (emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)'
  ).run(emp_name, gender, birth_date, phone, email, hire_date, dept_id || null, pos_id || null, status || '在职');
  db.close();
  res.json({ id: result.lastInsertRowid, message: '员工添加成功' });
});

app.put('/api/employees/:id', (req, res) => {
  const { id } = req.params;
  const { emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id, status } = req.body;
  const db = getDb();
  db.prepare(
    'UPDATE employee SET emp_name = ?, gender = ?, birth_date = ?, phone = ?, email = ?, hire_date = ?, dept_id = ?, pos_id = ?, status = ? WHERE emp_id = ?'
  ).run(emp_name, gender, birth_date, phone, email, hire_date, dept_id || null, pos_id || null, status || '在职', id);
  db.close();
  res.json({ message: '员工更新成功' });
});

app.delete('/api/employees/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  db.prepare('DELETE FROM employee WHERE emp_id = ?').run(id);
  db.close();
  res.json({ message: '员工删除成功' });
});

// ==================== Attendance APIs ====================
app.get('/api/attendances', (req, res) => {
  const db = getDb();
  const rows = db.prepare(`
    SELECT a.*, e.emp_name, d.dept_name
    FROM attendance a
    JOIN employee e ON a.emp_id = e.emp_id
    LEFT JOIN department d ON e.dept_id = d.dept_id
    ORDER BY a.att_date DESC, a.att_id
  `).all();
  db.close();
  res.json(rows);
});

app.post('/api/attendances', (req, res) => {
  const { emp_id, att_date, check_in_time, check_out_time, status, remark } = req.body;
  const db = getDb();
  const result = db.prepare(
    'INSERT INTO attendance (emp_id, att_date, check_in_time, check_out_time, status, remark) VALUES (?, ?, ?, ?, ?, ?)'
  ).run(emp_id, att_date, check_in_time, check_out_time, status || '正常', remark);
  db.close();
  res.json({ id: result.lastInsertRowid, message: '考勤记录添加成功' });
});

app.put('/api/attendances/:id', (req, res) => {
  const { id } = req.params;
  const { emp_id, att_date, check_in_time, check_out_time, status, remark } = req.body;
  const db = getDb();
  db.prepare(
    'UPDATE attendance SET emp_id = ?, att_date = ?, check_in_time = ?, check_out_time = ?, status = ?, remark = ? WHERE att_id = ?'
  ).run(emp_id, att_date, check_in_time, check_out_time, status, remark, id);
  db.close();
  res.json({ message: '考勤记录更新成功' });
});

app.delete('/api/attendances/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  db.prepare('DELETE FROM attendance WHERE att_id = ?').run(id);
  db.close();
  res.json({ message: '考勤记录删除成功' });
});

// ==================== Salary APIs ====================
app.get('/api/salaries', (req, res) => {
  const db = getDb();
  const rows = db.prepare(`
    SELECT s.*, e.emp_name, d.dept_name
    FROM salary s
    JOIN employee e ON s.emp_id = e.emp_id
    LEFT JOIN department d ON e.dept_id = d.dept_id
    ORDER BY s.pay_date DESC, s.salary_id
  `).all();
  db.close();
  res.json(rows);
});

app.post('/api/salaries', (req, res) => {
  const { emp_id, base_salary, bonus, deduction, pay_date } = req.body;
  const db = getDb();
  const result = db.prepare(
    'INSERT INTO salary (emp_id, base_salary, bonus, deduction, pay_date) VALUES (?, ?, ?, ?, ?)'
  ).run(emp_id, base_salary, bonus || 0, deduction || 0, pay_date);
  db.close();
  res.json({ id: result.lastInsertRowid, message: '薪资记录添加成功' });
});

app.put('/api/salaries/:id', (req, res) => {
  const { id } = req.params;
  const { emp_id, base_salary, bonus, deduction, pay_date } = req.body;
  const db = getDb();
  db.prepare(
    'UPDATE salary SET emp_id = ?, base_salary = ?, bonus = ?, deduction = ?, pay_date = ? WHERE salary_id = ?'
  ).run(emp_id, base_salary, bonus || 0, deduction || 0, pay_date, id);
  db.close();
  res.json({ message: '薪资记录更新成功' });
});

app.delete('/api/salaries/:id', (req, res) => {
  const { id } = req.params;
  const db = getDb();
  db.prepare('DELETE FROM salary WHERE salary_id = ?').run(id);
  db.close();
  res.json({ message: '薪资记录删除成功' });
});

// ==================== Statistics APIs ====================
app.get('/api/statistics/dept-emp-count', (req, res) => {
  const db = getDb();
  const rows = db.prepare(`
    SELECT d.dept_name, COUNT(e.emp_id) as emp_count
    FROM department d
    LEFT JOIN employee e ON d.dept_id = e.dept_id
    GROUP BY d.dept_id, d.dept_name
    ORDER BY emp_count DESC
  `).all();
  db.close();
  res.json(rows);
});

app.listen(PORT, () => {
  console.log(`企业员工管理系统已启动: http://localhost:${PORT}`);
  console.log('按 Ctrl+C 停止服务器');
});
