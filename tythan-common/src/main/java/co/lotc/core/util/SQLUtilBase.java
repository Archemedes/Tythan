package co.lotc.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.lotc.core.CoreLog;

public final class SQLUtilBase {

    private SQLUtilBase() {}

    public static class ExtensionFilenameFilter implements FilenameFilter {
        private final String ext;

        public ExtensionFilenameFilter(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(ext);
        }
    }

    public static String mysqlTextEscape(String untrusted) {
        if (untrusted == null) return null;
        return untrusted.replace("\\", "\\\\").replace("'", "\\'");
    }
    
    public static String giveOptionalWhere(Map<String, Object> criteria) {
        return (criteria == null ? "" : " WHERE " + formatWhereClause(criteria));
    }

    public static String formatWhereClause(Map<String, Object> val) {
  		return val.entrySet().stream()
  				.map(e-> (e.getKey() +"=" + getAsSQLInsert(e.getValue())) )
  				.collect(Collectors.joining(" AND "));
    }

    public static String formatSetClause(Map<String, Object> val) {
    		return val.entrySet().stream()
    				.map(e-> (e.getKey() +"=" + getAsSQLInsert(e.getValue())) )
    				.collect(Collectors.joining(","));
    }
    
    
    public static String getPreferredColumnType(Class<?> c) {
    	if(c.isPrimitive()) c = fromPrimitive(c);
    		
    	if(is(c, Number.class)) {
    		if(is(c, Float.class) || is(c, Double.class))
    			return "REAL";
    		else
    			return "INT";
    	} else if (is(c,Timestamp.class)) {
    		return "TIMESTAMP";
    	} else if (is(c, java.util.Date.class)) {
    		return "DATETIME(3)";
    	} else if (is(c, Boolean.class)) {
    		return "BOOLEAN";
    	} else { //Catch-all
    		return "VARCHAR(255)";
    	}
    }
    
    public static String getAsSQLInsert(Object o) {
    	if(o == null) {
    		return "NULL";
    	} else if(o instanceof java.util.Date){
    		return String.valueOf(((java.util.Date) o).getTime());
    	} else if(o instanceof Number || o instanceof Boolean || o instanceof Syntax) {
    		return String.valueOf(o);
    	} else if (o instanceof Inventory) {
    		String ser = serialize((Inventory) o);
    		return '\''+ mysqlTextEscape(ser) + '\'';
    	} else if (o instanceof ConfigurationSerializable) {
    		String ser = serialize((ConfigurationSerializable) o);
    		return '\''+ mysqlTextEscape(ser) + '\'';
    	} else {
    		return '\'' + mysqlTextEscape(o.toString()) + '\'';
    	}
    }
    
    private static Class<?> fromPrimitive(Class<?> c){
    	if( c == int.class) return Integer.class;
    	if( c == byte.class) return Byte.class;
    	if( c == short.class) return Short.class;
    	if( c == long.class) return Long.class;
    	
    	if( c == float.class) return Float.class;
    	if( c == double.class) return Double.class;
    	
    	if( c == boolean.class) return Boolean.class;
    	if( c == char.class) return Character.class;
    	
    	throw new IllegalArgumentException();
    }
    
    private static boolean is(Class<?> c, Class<?> assignableFrom) {
    	return assignableFrom.isAssignableFrom(c);
    }
    
    public static void setValue(PreparedStatement statement, int i, Object o) throws SQLException {
    	if (o instanceof Number) { //Numbers in order of decreasing likelihood
    		if (o instanceof Integer) {
    			statement.setInt(i, (Integer) o);
    		} else if (o instanceof Double) {
    			statement.setDouble(i, (Double) o);
    		} else if (o instanceof Long) {
    			statement.setLong(i, (Long) o);
    		} else if (o instanceof Float) {
    			statement.setFloat(i, (Float) o);
    		} else if (o instanceof Short) {
    			statement.setShort(i, (Short) o);
    		} else if (o instanceof Byte) {
    			statement.setByte(i, (Byte) o);
    		} else {
    			statement.setInt(i, ((Number) o).intValue());
    			CoreLog.warning("unhandled Number implementation being used: " + o.getClass().getName() + ". Int assumed");
    		}
    	} else if (o instanceof java.util.Date) {
    		if (o instanceof Timestamp) {
    			statement.setTimestamp(i, (Timestamp) o);
    		} else {
    			Date d = o instanceof Date? (Date) o : new Date(((java.util.Date) o).getTime());
	    		statement.setDate(i, d);
    		}
    	} else if (o instanceof Boolean) {
    		statement.setBoolean(i, (Boolean) o);
    	} else if (o instanceof Inventory) {
        statement.setString(i, serialize((Inventory) o));
    	} else if (o instanceof ConfigurationSerializable) {
    		statement.setString(i, serialize((ConfigurationSerializable) o));
    	} else { //String, enums, uuid
    		statement.setString(i, o == null ? null : o.toString());
    	}
    }
    
    private static String serialize(Inventory i) {
      YamlConfiguration config = new YamlConfiguration();
      ItemStack[] contents = i.getContents();
      List<Map<String, Object>> contentslist = Lists.newArrayList();
      for (ItemStack j : contents) {
          if (j == null) contentslist.add(null);
          else contentslist.add(j.serialize());
      }
      config.set("v", contentslist);
      return config.saveToString();
    }
    
    private static String serialize(ConfigurationSerializable c) {
  		YamlConfiguration config = new YamlConfiguration();
  		config.set("v", c);
  		return config.saveToString();
    }

    public static void closeStatement(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                rs.getStatement().close();
                rs.getStatement().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet closable) {
        if (closable != null) {
            try {
            	if(!closable.isClosed()) closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement closable) {
        if (closable != null) {
            try {
                if(!closable.isClosed()) closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection closable) {
        if (closable != null) {
            try {
            	if(!closable.isClosed()) closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
