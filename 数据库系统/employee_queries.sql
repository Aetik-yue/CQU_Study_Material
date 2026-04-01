
-- 1、查询年龄在20-25岁的员工姓名
SELECT emp_name
FROM emp
WHERE age BETWEEN 20 AND 25;
-- 等价写法: WHERE age >=20 AND age <=25


-- 2、查询每个部门的男员工人数
-- 版本1：仅显示部门ID
SELECT dept_id, COUNT(*) AS male_count
FROM emp
WHERE gender = '男'
GROUP BY dept_id;

-- 版本2：显示部门名称（关联部门表）
SELECT d.dept_name, COUNT(e.emp_id) AS male_count
FROM emp e
JOIN dept d ON e.dept_id = d.dept_id
WHERE e.gender = '男'
GROUP BY d.dept_id, d.dept_name;


-- 3、按照从小到大的顺序显示每个部门的工资总额
-- 版本1：仅显示部门ID
SELECT dept_id, SUM(salary) AS total_salary
FROM emp
GROUP BY dept_id
ORDER BY total_salary; -- 默认升序ASC可省略

-- 版本2：显示部门名称（关联部门表）
SELECT d.dept_name, SUM(e.salary) AS total_salary
FROM emp e
JOIN dept d ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name
ORDER BY total_salary;


-- 4、查找每个部门最高工资的员工信息
-- 写法1：关联子查询（全数据库兼容，考研答题首选）
SELECT e.*
FROM emp e
WHERE e.salary = (
    SELECT MAX(salary) 
    FROM emp 
    WHERE dept_id = e.dept_id
);

-- 写法2：窗口函数（SQL新标准，简洁高效）
SELECT * FROM (
    SELECT *,
           ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rank_num
    FROM emp
) t
WHERE rank_num = 1;

