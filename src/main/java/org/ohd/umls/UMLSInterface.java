/*
 * UMLSInterface.java
 *
 * Created on October 15, 2006, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ohd.umls;

/**
 *
 * @author Administrator
 */

import gov.nih.nlm.nls.lvg.Api.LuiNormApi;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UMLSInterface implements Closeable {
	private static String driverName_ = null;
	private static String connStr_ = null;
	private static Connection conn_ = null;
	private Statement stmt;
	LuiNormApi myLui;

	public UMLSInterface(Properties config) {

		String driverName = config.getProperty("MT_DB_DRIVER");
		String hostName = config.getProperty("MT_DB_HOST");
		String dbName = config.getProperty("MT_DB_NAME");
		String userName = config.getProperty("MT_DB_USERNAME");
		String passwd = config.getProperty("MT_DB_PASSWORD");
		String connStr = "jdbc:mysql://" + hostName + "/" + dbName + "?user="
				+ userName + "&password=" + passwd;

		driverName_ = driverName;
		connStr_ = connStr;

		LoadDbDriver();
		OpenConnection();
		try {
			stmt = conn_.createStatement();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		makeLui(config);
	}

	private void makeLui(Properties config) {
		Hashtable<String, String> prop = new Hashtable<String, String>();
		prop.put("DB_TYPE", config.getProperty("SL_DB_TYPE"));
		prop.put("DB_DRIVER", config.getProperty("SL_DB_DRIVER"));
		prop.put("DB_NAME", config.getProperty("SL_DB_NAME"));
		prop.put("DB_HOST", config.getProperty("SL_DB_HOST"));
		prop.put("DB_USERNAME", config.getProperty("SL_DB_USERNAME"));
		prop.put("DB_PASSWORD", config.getProperty("SL_DB_PASSWORD"));
		// prop.put("LVG_DIR", config.getProperty("LVG_DIR"));
		// prop.put("LVG_STOP_WORD_FILE",
		// config.getProperty("LVG_STOP_WORD_FILE"));
		// prop.put("LVG_NONINFO_WORD_FILE",
		// config.getProperty("LVG_NONINFO_WORD_FILE"));
		// prop.put("LVG_CONJ_WORD_FILE",
		// config.getProperty("LVG_CONJ_WORD_FILE"));
		// prop.put("LVG_REMOVE_S_FILE",
		// config.getProperty("LVG_REMOVE_S_FILE"));
		// prop.put("LVG_DIACRITICS_FILE",
		// config.getProperty("LVG_DIACRITICS_FILE"));
		// prop.put("LVG_LIGATURES_FILE",
		// config.getProperty("LVG_LIGATURES_FILE"));
		// prop.put("LVG_UNICODE_SYNONYM_FILE",
		// config.getProperty("LVG_UNICODE_SYNONYM_FILE"));
		// prop.put("LVG_UNICODE_SYMBOL_FILE",
		// config.getProperty("LVG_UNICODE_SYMBOL_FILE"));
		// prop.put("LVG_UNICODE_FILE", config.getProperty("LVG_UNICODE_FILE"));
		// prop.put("LVG_NON_STRIP_MAP_UNICODE_FILE",
		// config.getProperty("LVG_NON_STRIP_MAP_UNICODE_FILE"));
		// prop.put("MIN_TERM_LENGTH", config.getProperty("MIN_TERM_LENGTH"));
		// prop.put("MAX_RULE_UNINFLECTED_TERMS",
		// config.getProperty("MAX_RULE_UNINFLECTED_TERMS"));
		// prop.put("MAX_METAPHONE", config.getProperty("MAX_METAPHONE"));
		// prop.put("CGI_EOP", config.getProperty("CGI_EOP"));
		// prop.put("NO_OUTPUT", config.getProperty("NO_OUTPUT"));
		// prop.put("TRUNCATED_RESULTS",
		// config.getProperty("TRUNCATED_RESULTS"));
		// prop.put("LVG_PROMPT", config.getProperty("LVG_PROMPT"));
		// prop.put("DIR_TRIE_STEM_LENGTH",
		// config.getProperty("DIR_TRIE_STEM_LENGTH"));
		// prop.put("START_TAG", config.getProperty("START_TAG"));
		// prop.put("END_TAG", config.getProperty("END_TAG"));
		myLui = new LuiNormApi(getBaseConfigFile(), prop);
	}

	private String getBaseConfigFile() {
		// This is BS because LVG must have a config file
		URL cURL = this.getClass().getClassLoader()
				.getResource("config.properties");
		return cURL.getFile();
	}

	public Connection GetConnection() {
		if (conn_ == null) {
			LoadDbDriver();
			OpenConnection();
		}

		return conn_;
	}

	// DDL: data Definition Language
	public int ExecuteDdl(String query) {
		try {
			// get data from table
			// Statement stmt = conn_.createStatement();
			int rws = stmt.executeUpdate(query);

			// Clean up
			// stmt.close();
			return rws;
		} catch (SQLException e) {
			System.err.println("Query: " + query);
			System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState:     " + e.getSQLState());
			System.err.println("VendorError:  " + e.getErrorCode());

			// System.exit(2);
			return 0;
		}
	}

	public ResultSet ExecuteSql(String query) {
		try {
			// get data from table
			// Statement stmt = conn_.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			// stmt.close();
			return rs;
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState:     " + e.getSQLState());
			System.out.println("VendorError:  " + e.getErrorCode());

			System.exit(2); // SCR-1, lvg.2004
		}

		return null;
	}

	public void CloseConnection() {
		try {
			stmt.close();
			conn_.close();
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState:     " + e.getSQLState());
			System.out.println("VendorError:  " + e.getErrorCode());
		}
	}

	public String normalize(String str) {
		try {
			return myLui.Mutate(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return str;
		}
	}

	/**
	 * Returns a list of CUIs from a string
	 * 
	 * @param nstr
	 *            A string
	 * @return A <code>List</code> containing the CUI(s) as <code>Strings</code>
	 */
	public List<String> getCUIs(String nstr) {

		nstr = normalize(nstr);

		List<String> l = new LinkedList<String>();
		String sql = "SELECT DISTINCT a.CUI FROM MRCONSO a, MRXNS_ENG b "
				+ "WHERE a.cui = b.cui and nstr = '" + nstr
				+ "' and a.sui = b.sui " + "ORDER BY a.cui";
		ResultSet rs = this.ExecuteSql(sql);
		try {
			while (rs.next()) {
				l.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}

	public List<CodingSystem> getCodingSystem() {
		String sql = "SELECT DISTINCT rsab, son, sver FROM umls.mrsab m ORDER BY son";

		try {
			ResultSet rs = this.ExecuteSql(sql);
			ArrayList<CodingSystem> csl = new ArrayList<CodingSystem>();
			while (rs.next()) {
				csl.add(new CodingSystem(rs.getString("rsab"), rs
						.getString("son"), rs.getString("sver")));
			}
			rs.close();
			return csl;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public Code getCode(String cui, CodingSystem cs) {
		// String sql =
		// "SELECT code, str FROM mrconso WHERE cui = '"+cui+"' AND sab = '"+cs.getId()+"' AND ispref = 'Y' AND mrconso.ts = 'P'";
		String sql = "SELECT code, str FROM mrconso WHERE cui = '" + cui
				+ "' AND sab = '" + cs.getId() + "'"; // Widen the apeture
														// RXNorm missing with
														// ispref
		try {
			ResultSet rs = this.ExecuteSql(sql);
			if (rs.next()) {
				return new Code(cs, rs.getString("code"), rs.getString("str"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(UMLSInterface.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	public String getPreferredTerm(String cui) {
		String sql = "SELECT str FROM UMLS.mrconso WHERE cui = '" + cui
				+ "' AND ispref='Y' LIMIT 1";
		try {
			ResultSet rs = this.ExecuteSql(sql);
			if (rs.next()) {
				return rs.getString("str");
			} else {
				return "";
			}
		} catch (SQLException ex) {
			Logger.getLogger(UMLSInterface.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return "";
	}

	private void LoadDbDriver() {
		try {
			Class.forName(driverName_).newInstance();
		} catch (Exception e) {
			System.err.println("** Error: Unable to load driver.");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void OpenConnection() {
		try {
			conn_ = DriverManager.getConnection(connStr_);
			conn_.setAutoCommit(true);
		} catch (Exception e) {
			System.err.println("** Error: Can't Open DB connection.");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		try {
			conn_.close();
			myLui.CleanUp();
		} catch (SQLException ex) {
			Logger.getLogger(UMLSInterface.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

}