-- 实验报告1：openGauss完整实验脚本（按题目顺序）
-- 使用方式：
-- 1) gsql -d postgres -p 26000 -U omm -r
-- 2) \i /path/to/实验报告1_完整实验.sql

-- ===== 0. 环境准备 =====
DROP SCHEMA IF EXISTS exp1 CASCADE;
CREATE SCHEMA exp1;
SET search_path TO exp1, public;

-- ===== 1. 基本表操作 =====
-- 1) 创建四个表（teacher/student/course/sc）
CREATE TABLE teacher (
  tid    INTEGER PRIMARY KEY,
  tname  VARCHAR(50) NOT NULL,
  dept   VARCHAR(50) NOT NULL,
  salary NUMERIC(10,2) NOT NULL CHECK (salary > 0)
);

CREATE TABLE student (
  sid    VARCHAR(10) PRIMARY KEY,
  sname  VARCHAR(50) NOT NULL,
  dept   VARCHAR(50) NOT NULL,
  age    INTEGER NOT NULL CHECK (age BETWEEN 1 AND 120),
  gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F'))
);

CREATE TABLE course (
  cid      VARCHAR(10) PRIMARY KEY,
  cname    VARCHAR(50) NOT NULL,
  dept     VARCHAR(50) NOT NULL,
  credits  NUMERIC(3,1) NOT NULL CHECK (credits > 0),
  teacher  VARCHAR(50) NOT NULL
);

CREATE TABLE sc (
  sid   VARCHAR(10) NOT NULL,
  cid   VARCHAR(10) NOT NULL,
  grade INTEGER NOT NULL CHECK (grade BETWEEN 0 AND 100),
  PRIMARY KEY (sid, cid),
  CONSTRAINT fk_sc_sid FOREIGN KEY (sid) REFERENCES student(sid),
  CONSTRAINT fk_sc_cid FOREIGN KEY (cid) REFERENCES course(cid)
);

-- 2) 插入 teacher
INSERT INTO teacher VALUES
(14001, 'Katz', 'CS', 75000),
(14002, 'Crick', 'Biology', 72000),
(14003, 'Gold', 'Physics', 87000),
(14004, 'Einstein', 'Physics', 95000),
(14005, 'Kim', 'CS', 65000),
(14006, 'Wu', 'Finance', 90000),
(14007, 'Brandt', 'CS', 65000),
(14008, 'Singh', 'Finance', 80000);

-- 3) 插入 student
INSERT INTO student VALUES
('S1', 'Wangfeng', 'Physics', 20, 'M'),
('S2', 'Liu fang', 'Physics', 19, 'M'),
('S3', 'Chen yun', 'CS', 22, 'M'),
('S4', 'Wu kai', 'Finance', 19, 'M'),
('S5', 'Liu li', 'CS', 21, 'F'),
('S6', 'Dongqing', 'Finance', 18, 'F'),
('S7', 'Li', 'CS', 19, 'F'),
('S8', 'Chen', 'CS', 21, 'F'),
('S9', 'Zhang', 'Physics', 19, 'M'),
('S10', 'Yang', 'CS', 22, 'F'),
('S11', 'Wang', 'CS', 19, 'F');

-- 4) 插入 course
INSERT INTO course VALUES
('C1', 'DB', 'CS', 2.0, 'Li'),
('C2', 'maths', 'Mathematics', 2.0, 'Ma'),
('C3', 'chemistry', 'Chemistry', 2.5, 'Zhou'),
('C4', 'physics', 'Physics', 1.5, 'Shi'),
('C5', 'OS', 'CS', 2.0, 'Wen'),
('C6', 'Database', 'CS', 2.0, 'Katz'),
('C7', 'Algorithm', 'CS', 2.5, 'Gold'),
('C8', 'Java', 'CS', 1.5, 'Einstein'),
('C9', 'Marketing', 'Finance', 2.0, 'Wu');

-- 5) 插入 sc
INSERT INTO sc VALUES
('S1', 'C1', 70),
('S1', 'C3', 81),
('S2', 'C4', 92),
('S2', 'C2', 85),
('S3', 'C1', 65),
('S3', 'C5', 57),
('S4', 'C1', 87),
('S5', 'C4', 83);

-- 6) 修改 student：姓名为 Zhang 且系信息错填为 Physics，改为 CS
UPDATE student
SET dept = 'CS'
WHERE sname = 'Zhang' AND dept = 'Physics';

-- 7) 删除 teacher 中 Finance 学院教师
DELETE FROM teacher
WHERE dept = 'Finance';

-- 8) teacher 薪资调整
UPDATE teacher
SET salary = CASE
  WHEN salary <= 70000 THEN salary * 1.10
  ELSE salary * 1.05
END;

-- ===== 2. 基本数据查询 =====
-- Q1 物理系和生物系教师姓名、工资
SELECT tname, salary
FROM teacher
WHERE dept IN ('Physics', 'Biology');

-- Q2 列出所有系名（去重）
SELECT DISTINCT dept
FROM teacher;

-- Q3 若每位教师工资提高20%后的姓名和工资
SELECT tname, salary * 1.20 AS salary_after_20pct
FROM teacher;

-- Q4 CS系学生所选课程信息（姓名、所在系、课程名、学分）
SELECT s.sname, s.dept, c.cname, c.credits
FROM student s
JOIN sc ON s.sid = sc.sid
JOIN course c ON sc.cid = c.cid
WHERE s.dept = 'CS';

-- ===== 3. 复杂数据查询 =====
-- Q1 全体学生姓名、年龄
SELECT sname, age FROM student;

-- Q2 所有选修过课的学生学号
SELECT DISTINCT sid FROM sc;

-- Q3 成绩低于60分的学生学号
SELECT DISTINCT sid FROM sc WHERE grade < 60;

-- Q4 年龄在20至23之间的学生姓名、性别、年龄
SELECT sname, gender, age
FROM student
WHERE age BETWEEN 20 AND 23;

-- Q5 姓 liu 的学生学号、姓名、年龄（不区分大小写）
SELECT sid, sname, age
FROM student
WHERE lower(sname) LIKE 'liu%';

-- Q6 学习 C1 课程学生的最高分
SELECT MAX(grade) AS max_grade_c1
FROM sc
WHERE cid = 'C1';

-- Q7 各课程号及选课人数
SELECT cid, COUNT(*) AS course_student_count
FROM sc
GROUP BY cid
ORDER BY cid;

-- Q8 选修 C3 课程学生姓名
SELECT s.sname
FROM student s
JOIN sc ON s.sid = sc.sid
WHERE sc.cid = 'C3';

-- Q9 每门课程平均成绩
SELECT cid, AVG(grade)::NUMERIC(5,2) AS avg_grade
FROM sc
GROUP BY cid
ORDER BY cid;

-- ===== 4. 选做：学生只能选本学院课程，删除错选记录 =====
DELETE FROM sc
WHERE (sid, cid) IN (
  SELECT sc.sid, sc.cid
  FROM sc
  JOIN student s ON sc.sid = s.sid
  JOIN course c ON sc.cid = c.cid
  WHERE s.dept <> c.dept
);

-- 删除后核验：应该无跨学院选课数据
SELECT sc.sid, s.sname, s.dept AS student_dept, sc.cid, c.cname, c.dept AS course_dept
FROM sc
JOIN student s ON sc.sid = s.sid
JOIN course c ON sc.cid = c.cid
WHERE s.dept <> c.dept;
