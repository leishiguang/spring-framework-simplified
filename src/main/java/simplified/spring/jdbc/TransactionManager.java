package simplified.spring.jdbc;

import simplified.spring.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理类
 * 注意 Spring 中，bean 的默认实现是单例
 *
 * @author leishiguang
 * @since v1.0
 */
//@Service
public class TransactionManager {

	@Autowired
	private DataSource dataSource;

	/**
	 * 每个线程独有的连接
	 */
	private ThreadLocal<Connection> connection = new ThreadLocal<>();

	public Connection getConnection() {
		if (connection.get() == null) {
			try {
				connection.set(dataSource.getConnection());
			} catch (SQLException e) {
				throw new RuntimeException("无法创建数据库连接",e);
			}
		}
		return connection.get();
	}

	public void closeConnection(){
		connection.remove();
	}
}
