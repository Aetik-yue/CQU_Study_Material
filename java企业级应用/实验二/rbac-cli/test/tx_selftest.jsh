// 事务模板自测（jshell 脚本）：验证 TransactionTemplate 的提交与回滚语义
import com.cqu.rbac.config.DataSourceManager;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.TransactionTemplate;

DataSourceManager dsm = DataSourceManager.getInstance();
javax.sql.DataSource ds = dsm.getDataSource();
JdbcTemplate jt = new JdbcTemplate(ds);
TransactionTemplate tx = new TransactionTemplate(ds);

long before = jt.count("SELECT COUNT(*) FROM sys_audit_log");
System.out.println("起始记录数: " + before);

// 1) 回滚验证：事务内插入后抛异常 → 应整体回滚
try {
    tx.execute(() -> {
        jt.update("INSERT INTO sys_audit_log (module, operation, result) VALUES (?, ?, ?)",
                "自测", "ROLLBACK_TEST", "FAILURE");
        throw new RuntimeException("模拟业务异常触发回滚");
    });
    System.out.println("[FAIL] 未抛出异常");
} catch (RuntimeException e) {
    System.out.println("[OK] 异常已按原类型抛出: " + e.getMessage());
}
long afterRollback = jt.count("SELECT COUNT(*) FROM sys_audit_log");
System.out.println((afterRollback == before ? "[PASS] 回滚成功（记录数不变）" : "[FAIL] 回滚失败")
        + " before=" + before + " after=" + afterRollback);

// 2) 提交验证：事务正常结束 → 应持久化
tx.execute(() -> {
    jt.update("INSERT INTO sys_audit_log (module, operation, result) VALUES (?, ?, ?)",
            "自测", "COMMIT_TEST", "SUCCESS");
    return null;
});
long afterCommit = jt.count("SELECT COUNT(*) FROM sys_audit_log");
System.out.println((afterCommit == before + 1 ? "[PASS] 提交成功（+1 条）" : "[FAIL] 提交异常")
        + " after=" + afterCommit);

// 3) 清理自测数据
int cleaned = jt.update("DELETE FROM sys_audit_log WHERE module = ?", "自测");
System.out.println("[OK] 已清理自测数据 " + cleaned + " 条");
long finalCount = jt.count("SELECT COUNT(*) FROM sys_audit_log");
System.out.println((finalCount == before ? "[PASS] 清理完成，记录数恢复" : "[WARN] 记录数=" + finalCount));

dsm.close();
System.out.println("=== 事务自测结束 ===");
