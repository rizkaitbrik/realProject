package org.rti.zcore.utils.database;

import java.util.ArrayList;

import org.cidrz.webapp.dynasite.Constants;


/**
 * Enables cross-database compatibility
 * @author ckelley
 * @date   Feb 9, 2010
 */
public class DatabaseCompatability {


	/**
	 * Assembles a concat string for SQL statements based on different db's implementation of concatenation.
	 * @param items
	 * @param functionName - mysql only - CONCAT or CONCAT_SW. CONCAT if null.
	 * @param databaseType
	 * @param separator - MySQL CONCAT_WS take a separator string.
	 * @return
	 */
	public static String concat(ArrayList<String> items, String functionName, String databaseType, String separator) {
		String results = null;
		String concatOperator = "";
		String functionOpen = "";
		String functionClose = "";

		if (databaseType == null) {
			databaseType = Constants.DATABASE_TYPE;
		}

		if (databaseType.equals("derby")) {
			functionName = "";
			concatOperator = "||";
			functionOpen = "";
			functionClose = "";
		} else if (databaseType.equals("mysql")) {
			if (functionName == null) {
				functionName = "CONCAT";
			}
			concatOperator = ",";
			functionOpen = "(";
			functionClose = ")";
		} else if (databaseType.equals("mssql")) {
			if (functionName == null) {
				functionName = "";
			}
			concatOperator = "+";
			functionOpen = "";
			functionClose = "";
		}
		int i=0;

		results = functionName.concat(functionOpen);
		if (separator != null) {
			results = results.concat(separator).concat(concatOperator);
		}
		for (String item : items) {
			i++;
			results = results.concat(item);
			if (i < items.size()) {
				results = results.concat(concatOperator);
			}
		}
		results = results.concat(functionClose);
		return results;
	}


	/**
	 * Concats strings
	 * @param functionName - mysql only - CONCAT or CONCAT_SW. CONCAT if null.
	 * @param databaseType null if you want to use default value from Constants
	 * @param separator TODO
	 * @param args
	 * @return
	 */
	public static String concat(String functionName, String databaseType, String separator, String... args) {
		String results = null;
		ArrayList<String> items = new ArrayList<String>();
		for(String arg:args) {
			items.add(arg);
		}
    	results = DatabaseCompatability.concat(items, functionName, databaseType, null);
    	return results;
	}

	/**
	 * Creates string for ageCalc used in patient search on home page. Calculated the difference between the current date and the datefield.
	 * @param dateField birth date field
	 * @return age in years.
	 */
	public static String ageCalc(String dateField) {
		String results = null;
		if (dateField == null) {
			dateField = "pr.birth_date";
		}
		if (Constants.DATABASE_TYPE.equals("derby")) {
			results = "integer(floor({fn TIMESTAMPDIFF(SQL_TSI_YEAR, " + dateField + ", CURRENT_DATE)}))";
		} else if (Constants.DATABASE_TYPE.equals("mysql")) {
			results = "integer(floor({fn TIMESTAMPDIFF(SQL_TSI_YEAR, " + dateField + ", CURRENT_DATE)}))";
		} else if (Constants.DATABASE_TYPE.equals("mssql")) {
			results = "floor(DATEDIFF(YEAR, " + dateField + ", GETDATE()))";
		}
		return results;
	}

	/**
	 * Appends time string to a date string. ISO 8601 formatting for mssql.
	 * @param value
	 * @return
	 */
	public static String fixDateValues(String value) {
		String results = null;
		if (Constants.DATABASE_TYPE.equals("derby")) {
			results = value.concat(" 00:00:00");
		} else if (Constants.DATABASE_TYPE.equals("mysql")) {
			results = value.concat(" 00:00:00");
		} else if (Constants.DATABASE_TYPE.equals("mssql")) {
			// ISO 8601 formatting.
			results = value.concat("T00:00:00");
		}
		return results;
	}

	/**
	 * Creates last autogenerated identity string.
	 * @param databaseType TODO
	 *
	 * @return
	 */
	public static String insertLastIdentityString(String databaseType) {
		String results = null;
		if (databaseType.equals("derby")) {
			results = "IDENTITY_VAL_LOCAL()";
		} else if (databaseType.equals("mysql")) {
			results = "LAST_INSERT_ID()";
		} else if (databaseType.equals("mssql")) {
			//results = "?";
			results = "SCOPE_IDENTITY() AS [IDENTITY]()";
		}
		return results;
	}

	/**
	 * Creates select statement to find last identity string created by the database.
	 * @param databaseType
	 * @param tableName - if databaseType == mssql, specify tableName if you want table-level granularity.
	 * @return
	 */
	public static String selectLastIdentityString(String databaseType, String tableName) {
		String results = null;
		if (databaseType.equals("derby")) {
			results = "VALUES " + insertLastIdentityString(databaseType);
		} else if (databaseType.equals("mysql")) {
			results = "SELECT " + insertLastIdentityString(databaseType);
		} else if (databaseType.equals("mssql")) {
			if (tableName != null) {
				results = "SELECT IDENT_CURRENT('" + tableName + "')";
			} else {
				results = "SELECT " + insertLastIdentityString(databaseType);
			}
		}
		return results;
	}

	/**
	 * Adds different permutations of "Select id " statement for use when generating pager sql
	 * @param database
	 * @param tableName TODO
	 * @param orderBy
	 * @return
	 */
	public static String insertPagerSelect(String database, String maxRows, String tableName) {
		String results = null;
		if (tableName != null) {
			if (database.equals("derby")) {
				results =  "SELECT " + tableName  + ".id, ";
			} else if (database.equals("mysql")) {
				results =  "SELECT " + tableName  + ".id, ";
			} else if (database.equals("mssql")) {
				// SELECT TOP " + maxRows + "
				results = "id, ";
			}
		} else {
			if (database.equals("derby")) {
				results =  "SELECT id, ";
			} else if (database.equals("mysql")) {
				results =  "SELECT id, ";
			} else if (database.equals("mssql")) {
				// SELECT TOP " + maxRows + "
				results = "id, ";
			}
		}
		return results;
	}

	/**
	 * Used for generating pager sql
	 * @param database
	 * @param orderBy
	 * @return
	 */
	public static String insertPagerFrom(String database, String orderBy) {
		String results = null;
		if (database.equals("derby")) {
			results =  "";
		} else if (database.equals("mysql")) {
			results =  "";
		} else if (database.equals("mssql")) {
			// "FROM (SELECT  ROW_NUMBER() OVER (ORDER BY u.updated DESC) AS Row,"
			results = " FROM (SELECT  ROW_NUMBER() OVER (ORDER BY " + orderBy + ") AS Row, ";
		}
		return results;
	}


	/**
	 * Used for generating pager sql
	 * @param database
	 * @param orderBy
	 * @return
	 */
	public static String insertPagerEnd(String database, String orderBy, Integer offset, Integer maxRows) {
		String results = null;
		if (database.equals("derby")) {
			//  ORDER BY id asc OFFSET " + offset + " ROWS FETCH NEXT " + maxRows + " ROWS ONLY"
			results =  " ORDER BY " + orderBy + " OFFSET " + offset + " ROWS FETCH NEXT " + maxRows + " ROWS ONLY";
		} else if (database.equals("mysql")) {
//			results =  " ORDER BY " + orderBy + " OFFSET " + offset + " ROWS FETCH NEXT " + maxRows + " ROWS ONLY";
			results =  " ORDER BY " + orderBy + " LIMIT " + maxRows + " OFFSET " + offset;
		} else if (database.equals("mssql")) {
			// ") AS LogWithRowNumbers WHERE Row >= " + offset + " AND Row <= " + (offset + maxRows)
			results = ") AS LogWithRowNumbers WHERE Row >= " + offset + " AND Row <= " + maxRows;
		}
		return results;
	}

}