package gaml.extension.database.utils.sql.postgres;

import java.util.HashMap;
import java.util.Map;

import gama.core.runtime.IScope;
import gaml.extension.database.utils.sql.ISqlConnector;
import gaml.extension.database.utils.sql.SqlConnection;

public class PostgresConnector implements ISqlConnector {

	@Override
	public SqlConnection connection(final IScope scope, String venderName, String url, String port, String dbName, String userName,
			String password, Boolean transformed) {
		return new PostgresConnection(scope, venderName, url, port, dbName, userName, password, transformed);
	}

	@Override
	public Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database,
			String user, String passwd) {

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", host);
        params.put("port", port);
        params.put("schema", "public");
        params.put("database", database);
        params.put("user", user);
        params.put("passwd", passwd);
		
		return params;
	}

}
