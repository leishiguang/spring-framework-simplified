package simplified.spring.jdbc;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.Autowired;
import simplified.spring.annotation.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据操作的 sql
 * 这个类本质上和 mybatis 是相同的，没有复杂的功能~
 *
 * @author leishiguang
 * @since v1.0
 */
@Service
@Slf4j
public class JdbcTemplate {

	@Autowired
	TransactionManager transactionManager;

	/**
	 * 执行数据库方法
	 * @param sql 要执行的语句
	 */
	public void execute(String sql) throws SQLException{
		Connection connection = transactionManager.getConnection();
		//事务基于连接去控制，于是在这里关闭自动提交
		connection.setAutoCommit(false);
		Statement statement = connection.createStatement();
		try {
			statement.execute(sql);
			connection.commit();
		} catch (Exception e) {
			connection.rollback();
			log.error("事务中捕获异常，进行回滚",e);
		}finally {
			//关闭连接 or 放回连接池
			connection.close();
		}

	}
}
