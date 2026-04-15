CodeGenTableHolder(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.CsvTableParser;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.ITableCache;

public class  T_<t.name> implements ITableCache {
	private static final T_<t.name> INSTANCE = new T_<t.name>();

	private Map\<String, Map\<String, List\<String\>\>\> tableCache = new ConcurrentHashMap\<\>();

	public static T_<t.name> getInstance() {
		return INSTANCE;
	}

	public Map\<String, List\<String\>\> getTableData(String tableName) {
		return tableCache.computeIfAbsent(tableName, this::parseTable);
	}

	private Map\<String, List\<String\>\> parseTable(String tableName) {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(tableName + ".csv"), StandardCharsets.UTF_8))) {
			return CsvTableParser.parseCsv(tableName, r);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

>>