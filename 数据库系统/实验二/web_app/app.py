# -*- coding: utf-8 -*-
"""
企业员工管理系统 - Flask Web 应用
基于 OpenGauss 数据库
"""
from flask import Flask, render_template, request, redirect, url_for, flash
import psycopg2
from psycopg2.extras import RealDictCursor
from datetime import datetime
from functools import wraps

app = Flask(__name__)
app.secret_key = 'employee_management_2026'

# ===== 数据库配置 =====
# 请根据实际情况修改以下配置
DB_CONFIG = {
    'host': 'localhost',      # 数据库服务器地址
    'port': 26000,            # OpenGauss 端口（默认 26000）
    'database': 'employee_db',
    'user': 'omm',
    'password': ''            # 数据库密码
}

def get_db():
    """获取数据库连接"""
    return psycopg2.connect(**DB_CONFIG)

def check_db_connection(f):
    """检查数据库连接的装饰器"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            conn = get_db()
            conn.close()
            return f(*args, **kwargs)
        except psycopg2.Error as e:
            return render_template('error.html', error=str(e))
    return decorated_function

# ===== 首页/仪表盘 =====
@app.route('/')
@check_db_connection
def index():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)

    # 统计信息
    cur.execute("SELECT COUNT(*) as total FROM Employee")
    emp_count = cur.fetchone()['total']

    cur.execute("SELECT COUNT(*) as total FROM Department")
    dept_count = cur.fetchone()['total']

    cur.execute("SELECT COUNT(*) as total FROM Position")
    pos_count = cur.fetchone()['total']

    # 各部门人数
    cur.execute("""
        SELECT d.dept_name, COUNT(e.emp_id) as count
        FROM Department d
        LEFT JOIN Employee e ON d.dept_id = e.dept_id
        GROUP BY d.dept_name
        ORDER BY count DESC
    """)
    dept_stats = cur.fetchall()

    cur.close()
    conn.close()

    return render_template('index.html',
                          emp_count=emp_count,
                          dept_count=dept_count,
                          pos_count=pos_count,
                          dept_stats=dept_stats)

# ===== 员工管理 =====
@app.route('/employees')
def employees():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("""
        SELECT e.*, d.dept_name, p.pos_name
        FROM Employee e
        LEFT JOIN Department d ON e.dept_id = d.dept_id
        LEFT JOIN Position p ON e.pos_id = p.pos_id
        ORDER BY e.emp_id
    """)
    emps = cur.fetchall()
    cur.close()
    conn.close()
    return render_template('employees.html', employees=emps)

@app.route('/employee/add', methods=['POST'])
def add_employee():
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO Employee (emp_name, gender, birth_date, phone, email, hire_date, dept_id, pos_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """, (
            request.form['emp_name'],
            request.form['gender'],
            request.form['birth_date'],
            request.form['phone'],
            request.form['email'],
            request.form['hire_date'],
            request.form['dept_id'],
            request.form['pos_id']
        ))
        conn.commit()
        flash('员工添加成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'添加失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('employees'))

@app.route('/employee/<int:emp_id>/edit', methods=['POST'])
def edit_employee(emp_id):
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            UPDATE Employee SET emp_name=%s, gender=%s, birth_date=%s,
            phone=%s, email=%s, hire_date=%s, dept_id=%s, pos_id=%s
            WHERE emp_id=%s
        """, (
            request.form['emp_name'],
            request.form['gender'],
            request.form['birth_date'],
            request.form['phone'],
            request.form['email'],
            request.form['hire_date'],
            request.form['dept_id'],
            request.form['pos_id'],
            emp_id
        ))
        conn.commit()
        flash('员工信息更新成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'更新失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('employees'))

@app.route('/employee/<int:emp_id>/delete', methods=['POST'])
def delete_employee(emp_id):
    conn = get_db()
    cur = conn.cursor()
    try:
        # 先删除关联数据
        cur.execute("DELETE FROM Salary WHERE emp_id=%s", (emp_id,))
        cur.execute("DELETE FROM Attendance WHERE emp_id=%s", (emp_id,))
        cur.execute("DELETE FROM Employee WHERE emp_id=%s", (emp_id,))
        conn.commit()
        flash('员工删除成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'删除失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('employees'))

# ===== 部门管理 =====
@app.route('/departments')
def departments():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("""
        SELECT d.*, COUNT(e.emp_id) as emp_count
        FROM Department d
        LEFT JOIN Employee e ON d.dept_id = e.dept_id
        GROUP BY d.dept_id
        ORDER BY d.dept_id
    """)
    depts = cur.fetchall()
    cur.close()
    conn.close()
    return render_template('departments.html', departments=depts)

@app.route('/department/add', methods=['POST'])
def add_department():
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO Department (dept_name, description, manager)
            VALUES (%s, %s, %s)
        """, (request.form['dept_name'], request.form['description'], request.form['manager']))
        conn.commit()
        flash('部门添加成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'添加失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('departments'))

@app.route('/department/<int:dept_id>/delete', methods=['POST'])
def delete_department(dept_id):
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("DELETE FROM Department WHERE dept_id=%s", (dept_id,))
        conn.commit()
        flash('部门删除成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'删除失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('departments'))

# ===== 岗位管理 =====
@app.route('/positions')
def positions():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("SELECT * FROM Position ORDER BY pos_id")
    positions = cur.fetchall()
    cur.close()
    conn.close()
    return render_template('positions.html', positions=positions)

@app.route('/position/add', methods=['POST'])
def add_position():
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO Position (pos_name, description, salary_level)
            VALUES (%s, %s, %s)
        """, (request.form['pos_name'], request.form['description'], request.form['salary_level']))
        conn.commit()
        flash('岗位添加成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'添加失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('positions'))

# ===== 薪资管理 =====
@app.route('/salaries')
def salaries():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("""
        SELECT s.*, e.emp_name
        FROM Salary s
        JOIN Employee e ON s.emp_id = e.emp_id
        ORDER BY s.pay_month DESC, e.emp_name
    """)
    salaries = cur.fetchall()
    cur.close()
    conn.close()
    return render_template('salaries.html', salaries=salaries)

@app.route('/salary/add', methods=['POST'])
def add_salary():
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO Salary (emp_id, base_salary, perf_bonus, bonus, pay_month)
            VALUES (%s, %s, %s, %s, %s)
        """, (
            request.form['emp_id'],
            request.form['base_salary'],
            request.form['perf_bonus'],
            request.form['bonus'],
            request.form['pay_month']
        ))
        conn.commit()
        flash('薪资记录添加成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'添加失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('salaries'))

# ===== 考勤管理 =====
@app.route('/attendances')
def attendances():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("""
        SELECT a.*, e.emp_name
        FROM Attendance a
        JOIN Employee e ON a.emp_id = e.emp_id
        ORDER BY a.attend_date DESC, e.emp_name
    """)
    attendances = cur.fetchall()
    cur.close()
    conn.close()
    return render_template('attendances.html', attendances=attendances)

@app.route('/attendance/add', methods=['POST'])
def add_attendance():
    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO Attendance (emp_id, attend_date, status, remark)
            VALUES (%s, %s, %s, %s)
        """, (
            request.form['emp_id'],
            request.form['attend_date'],
            request.form['status'],
            request.form['remark']
        ))
        conn.commit()
        flash('考勤记录添加成功！', 'success')
    except Exception as e:
        conn.rollback()
        flash(f'添加失败：{e}', 'danger')
    finally:
        cur.close()
        conn.close()
    return redirect(url_for('attendances'))

# ===== 查询页面 =====
@app.route('/search')
def search():
    query = request.args.get('q', '')
    results = []
    if query:
        conn = get_db()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("""
            SELECT e.*, d.dept_name, p.pos_name
            FROM Employee e
            LEFT JOIN Department d ON e.dept_id = d.dept_id
            LEFT JOIN Position p ON e.pos_id = p.pos_id
            WHERE e.emp_name LIKE %s OR e.email LIKE %s OR e.phone LIKE %s
            ORDER BY e.emp_id
        """, (f'%{query}%', f'%{query}%', f'%{query}%'))
        results = cur.fetchall()
        cur.close()
        conn.close()
    return render_template('search.html', results=results, query=query)

if __name__ == '__main__':
    print("=" * 50)
    print("企业员工管理系统启动中...")
    print("访问地址：http://localhost:5000")
    print("=" * 50)
    app.run(debug=True, host='0.0.0.0', port=5000)
