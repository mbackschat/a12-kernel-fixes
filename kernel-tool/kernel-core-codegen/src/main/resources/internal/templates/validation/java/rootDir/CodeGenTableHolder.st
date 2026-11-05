CodeGenTableHolder(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.CsvTableParser;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.ITableCache;

public class  T_<t.name> implements ITableCache {
	private static final T_<t.name> INSTANCE = new T_<t.name>();
	private static final Set\<String\> KNOWN_TABLES = Set.of(<t.tableNames:{n | "<n>"}; separator=", ">);

	private Map\<String, Map\<String, List\<String\>\>\> tableCache = new ConcurrentHashMap\<\>();

	public static T_<t.name> getInstance() {
		return INSTANCE;
	}

	public Map\<String, List\<String\>\> getTableData(String tableName) {
		return tableCache.computeIfAbsent(tableName, this::parseTable);
	}

	private Map\<String, List\<String\>\> parseTable(String tableName) {
		if (!KNOWN_TABLES.contains(tableName)) {
			throw new IllegalArgumentException("Unknown table requested: " + tableName);
		}
		InputStream in = getClass().getResourceAsStream(tableName + ".csv");
		if (in == null) {
			throw new IllegalStateException("Bundled table resource missing: " + tableName + ".csv");
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			return CsvTableParser.parseCsv(tableName, r);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

>>