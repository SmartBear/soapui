/*
 * PLSQLTokenMarker.java - Oracle PL/SQL token marker
 * Copyright (C) 2002 Oliver Henning
 * 
 * adapted from:
 * plsqlTokenMarker.java - Transact-SQL token marker
 * Copyright (C) 1999 mike dillon
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * Oracle PL-SQL token marker.
 *
 * @author oliver henning
 * @version $Id: PLSQLTokenMarker.java,v 1.9 1999/12/13 03:40:30 sp Exp $
 */
public class PLSQLTokenMarker extends SQLTokenMarker
{
	// public members
	public PLSQLTokenMarker()
	{
		super(getKeywordMap(), true);
	}

	public static KeywordMap getKeywordMap()
	{
		if (plsqlKeywords == null) {
			plsqlKeywords = new KeywordMap(true);
			addKeywords();
			addDataTypes();
			addSystemFunctions();
			addOperators();
	  		addSystemStoredProcedures();
			addSystemTables();
		}
		return plsqlKeywords;
	}	

	private static void addKeywords()
	{
		plsqlKeywords.add("ABORT",Token.KEYWORD1);
		plsqlKeywords.add("ACCESS",Token.KEYWORD1);
		plsqlKeywords.add("ADD",Token.KEYWORD1);
		plsqlKeywords.add("ALTER",Token.KEYWORD1);
		plsqlKeywords.add("ARRAY",Token.KEYWORD1);
		plsqlKeywords.add("ARRAY_LEN",Token.KEYWORD1);
		plsqlKeywords.add("AS",Token.KEYWORD1);
		plsqlKeywords.add("ASC",Token.KEYWORD1);
		plsqlKeywords.add("ASSERT",Token.KEYWORD1);
		plsqlKeywords.add("ASSIGN",Token.KEYWORD1);
		plsqlKeywords.add("AT",Token.KEYWORD1);
		plsqlKeywords.add("AUDIT",Token.KEYWORD1);
		plsqlKeywords.add("AUTHORIZATION",Token.KEYWORD1);
		plsqlKeywords.add("AVG",Token.KEYWORD1);
		plsqlKeywords.add("BASE_TABLE",Token.KEYWORD1);
		plsqlKeywords.add("BEGIN",Token.KEYWORD1);
		plsqlKeywords.add("BODY",Token.KEYWORD1);
		plsqlKeywords.add("CASE",Token.KEYWORD1);
		plsqlKeywords.add("CHAR",Token.KEYWORD1);
		plsqlKeywords.add("CHAR_BASE",Token.KEYWORD1);
		plsqlKeywords.add("CHECK",Token.KEYWORD1);
		plsqlKeywords.add("CLOSE",Token.KEYWORD1);
		plsqlKeywords.add("CLUSTER",Token.KEYWORD1);
		plsqlKeywords.add("CLUSTERS",Token.KEYWORD1);
		plsqlKeywords.add("COLAUTH",Token.KEYWORD1);
		plsqlKeywords.add("COLUMN",Token.KEYWORD1);
		plsqlKeywords.add("COMMENT",Token.KEYWORD1);
		plsqlKeywords.add("COMMIT",Token.KEYWORD1);
		plsqlKeywords.add("COMPRESS",Token.KEYWORD1);
		plsqlKeywords.add("CONSTANT",Token.KEYWORD1);
		plsqlKeywords.add("CONSTRAINT",Token.KEYWORD1);
		plsqlKeywords.add("COUNT",Token.KEYWORD1);
		plsqlKeywords.add("CREATE",Token.KEYWORD1);
		plsqlKeywords.add("CURRENT",Token.KEYWORD1);
		plsqlKeywords.add("CURRVAL",Token.KEYWORD1);
		plsqlKeywords.add("CURSOR",Token.KEYWORD1);
		plsqlKeywords.add("DATABASE",Token.KEYWORD1);
		plsqlKeywords.add("DATA_BASE",Token.KEYWORD1);
		plsqlKeywords.add("DATE",Token.KEYWORD1);
		plsqlKeywords.add("DBA",Token.KEYWORD1);
		plsqlKeywords.add("DEBUGOFF",Token.KEYWORD1);
		plsqlKeywords.add("DEBUGON",Token.KEYWORD1);
		plsqlKeywords.add("DECLARE",Token.KEYWORD1);
		plsqlKeywords.add("DEFAULT",Token.KEYWORD1);
		plsqlKeywords.add("DEFINITION",Token.KEYWORD1);
		plsqlKeywords.add("DELAY",Token.KEYWORD1);
		plsqlKeywords.add("DELETE",Token.KEYWORD1);
		plsqlKeywords.add("DESC",Token.KEYWORD1);
		plsqlKeywords.add("DIGITS",Token.KEYWORD1);
		plsqlKeywords.add("DISPOSE",Token.KEYWORD1);
		plsqlKeywords.add("DISTINCT",Token.KEYWORD1);
		plsqlKeywords.add("DO",Token.KEYWORD1);
		plsqlKeywords.add("DROP",Token.KEYWORD1);
		plsqlKeywords.add("DUMP",Token.KEYWORD1);
		plsqlKeywords.add("ELSE",Token.KEYWORD1);
		plsqlKeywords.add("ELSIF",Token.KEYWORD1);
		plsqlKeywords.add("END",Token.KEYWORD1);
		plsqlKeywords.add("ENTRY",Token.KEYWORD1);
		plsqlKeywords.add("EXCEPTION",Token.KEYWORD1);
		plsqlKeywords.add("EXCEPTION_INIT",Token.KEYWORD1);
		plsqlKeywords.add("EXCLUSIVE",Token.KEYWORD1);
		plsqlKeywords.add("EXIT",Token.KEYWORD1);
		plsqlKeywords.add("FALSE",Token.KEYWORD1);
		plsqlKeywords.add("FETCH",Token.KEYWORD1);
		plsqlKeywords.add("FILE",Token.KEYWORD1);
		plsqlKeywords.add("FOR",Token.KEYWORD1);
		plsqlKeywords.add("FORM",Token.KEYWORD1);
		plsqlKeywords.add("FROM",Token.KEYWORD1);
		plsqlKeywords.add("FUNCTION",Token.KEYWORD1);
		plsqlKeywords.add("GENERIC",Token.KEYWORD1);
		plsqlKeywords.add("GOTO",Token.KEYWORD1);
		plsqlKeywords.add("GRANT",Token.KEYWORD1);
		plsqlKeywords.add("GREATEST",Token.KEYWORD1);
		plsqlKeywords.add("GROUP",Token.KEYWORD1);
		plsqlKeywords.add("HAVING",Token.KEYWORD1);
		plsqlKeywords.add("IDENTIFIED",Token.KEYWORD1);
		plsqlKeywords.add("IDENTITYCOL",Token.KEYWORD1);
		plsqlKeywords.add("IF",Token.KEYWORD1);
		plsqlKeywords.add("IMMEDIATE",Token.KEYWORD1);
		plsqlKeywords.add("INCREMENT",Token.KEYWORD1);
		plsqlKeywords.add("INDEX",Token.KEYWORD1);
		plsqlKeywords.add("INDEXES",Token.KEYWORD1);
		plsqlKeywords.add("INDICATOR",Token.KEYWORD1);
		plsqlKeywords.add("INITIAL",Token.KEYWORD1);
		plsqlKeywords.add("INSERT",Token.KEYWORD1);
		plsqlKeywords.add("INTERFACE",Token.KEYWORD1);
		plsqlKeywords.add("INTO",Token.KEYWORD1);
		plsqlKeywords.add("IS",Token.KEYWORD1);
		plsqlKeywords.add("LEAST",Token.KEYWORD1);
		plsqlKeywords.add("LEVEL",Token.KEYWORD1);
		plsqlKeywords.add("LIMITED",Token.KEYWORD1);
		plsqlKeywords.add("LOCK",Token.KEYWORD1);
		plsqlKeywords.add("LONG",Token.KEYWORD1);
		plsqlKeywords.add("LOOP",Token.KEYWORD1);
		plsqlKeywords.add("MAX",Token.KEYWORD1);
		plsqlKeywords.add("MAXEXTENTS",Token.KEYWORD1);
		plsqlKeywords.add("MIN",Token.KEYWORD1);
		plsqlKeywords.add("MINUS",Token.KEYWORD1);
		plsqlKeywords.add("MLSLABEL",Token.KEYWORD1);
		plsqlKeywords.add("MOD",Token.KEYWORD1);
		plsqlKeywords.add("MORE",Token.KEYWORD1);
		plsqlKeywords.add("NEW",Token.KEYWORD1);
		plsqlKeywords.add("NEXTVAL",Token.KEYWORD1);
		plsqlKeywords.add("NOAUDIT",Token.KEYWORD1);
		plsqlKeywords.add("NOCOMPRESS",Token.KEYWORD1);
		plsqlKeywords.add("NOWAIT",Token.KEYWORD1);
		plsqlKeywords.add("NULL",Token.KEYWORD1);
		plsqlKeywords.add("NUMBER_BASE",Token.KEYWORD1);
		plsqlKeywords.add("OF",Token.KEYWORD1);
		plsqlKeywords.add("OFFLINE",Token.KEYWORD1);
		plsqlKeywords.add("ON",Token.KEYWORD1);
		plsqlKeywords.add("OFF",Token.KEYWORD1);
		plsqlKeywords.add("ONLINE",Token.KEYWORD1);
		plsqlKeywords.add("OPEN",Token.KEYWORD1);
		plsqlKeywords.add("OPTION",Token.KEYWORD1);
		plsqlKeywords.add("ORDER",Token.KEYWORD1);
		plsqlKeywords.add("OTHERS",Token.KEYWORD1);
		plsqlKeywords.add("OUT",Token.KEYWORD1);
		plsqlKeywords.add("PACKAGE",Token.KEYWORD1);
		plsqlKeywords.add("PARTITION",Token.KEYWORD1);
		plsqlKeywords.add("PCTFREE",Token.KEYWORD1);
		plsqlKeywords.add("PRAGMA",Token.KEYWORD1);
		plsqlKeywords.add("PRIVATE",Token.KEYWORD1);
		plsqlKeywords.add("PRIVILEGES",Token.KEYWORD1);
		plsqlKeywords.add("PROCEDURE",Token.KEYWORD1);
		plsqlKeywords.add("PUBLIC",Token.KEYWORD1);
		plsqlKeywords.add("QUOTED_IDENTIFIER",Token.KEYWORD1);
		plsqlKeywords.add("RAISE",Token.KEYWORD1);
		plsqlKeywords.add("RANGE",Token.KEYWORD1);
		plsqlKeywords.add("RECORD",Token.KEYWORD1);
		plsqlKeywords.add("REF",Token.KEYWORD1);
		plsqlKeywords.add("RELEASE",Token.KEYWORD1);
		plsqlKeywords.add("REMR",Token.KEYWORD1);
		plsqlKeywords.add("RENAME",Token.KEYWORD1);
		plsqlKeywords.add("RESOURCE",Token.KEYWORD1);
		plsqlKeywords.add("RETURN",Token.KEYWORD1);
		plsqlKeywords.add("REVERSE",Token.KEYWORD1);
		plsqlKeywords.add("REVOKE",Token.KEYWORD1);
		plsqlKeywords.add("ROLLBACK",Token.KEYWORD1);
		plsqlKeywords.add("ROW",Token.KEYWORD1);
		plsqlKeywords.add("ROWLABEL",Token.KEYWORD1);
		plsqlKeywords.add("ROWNUM",Token.KEYWORD1);
		plsqlKeywords.add("ROWS",Token.KEYWORD1);
		plsqlKeywords.add("ROWTYPE",Token.KEYWORD1);
		plsqlKeywords.add("RUN",Token.KEYWORD1);
		plsqlKeywords.add("SAVEPOINT",Token.KEYWORD1);
		plsqlKeywords.add("SCHEMA",Token.KEYWORD1);
		plsqlKeywords.add("SELECT",Token.KEYWORD1);
		plsqlKeywords.add("SEPERATE",Token.KEYWORD1);
		plsqlKeywords.add("SESSION",Token.KEYWORD1);
		plsqlKeywords.add("SET",Token.KEYWORD1);
		plsqlKeywords.add("SHARE",Token.KEYWORD1);
		plsqlKeywords.add("SPACE",Token.KEYWORD1);
		plsqlKeywords.add("SQL",Token.KEYWORD1);
		plsqlKeywords.add("SQLCODE",Token.KEYWORD1);
		plsqlKeywords.add("SQLERRM",Token.KEYWORD1);
		plsqlKeywords.add("STATEMENT",Token.KEYWORD1);
		plsqlKeywords.add("STDDEV",Token.KEYWORD1);
		plsqlKeywords.add("SUBTYPE",Token.KEYWORD1);
		plsqlKeywords.add("SUCCESSFULL",Token.KEYWORD1);
		plsqlKeywords.add("SUM",Token.KEYWORD1);
		plsqlKeywords.add("SYNONYM",Token.KEYWORD1);
		plsqlKeywords.add("SYSDATE",Token.KEYWORD1);
		plsqlKeywords.add("TABAUTH",Token.KEYWORD1);
		plsqlKeywords.add("TABLE",Token.KEYWORD1);
		plsqlKeywords.add("TABLES",Token.KEYWORD1);
		plsqlKeywords.add("TASK",Token.KEYWORD1);
		plsqlKeywords.add("TERMINATE",Token.KEYWORD1);
		plsqlKeywords.add("THEN",Token.KEYWORD1);
		plsqlKeywords.add("TO",Token.KEYWORD1);
		plsqlKeywords.add("TRIGGER",Token.KEYWORD1);
		plsqlKeywords.add("TRUE",Token.KEYWORD1);
		plsqlKeywords.add("TYPE",Token.KEYWORD1);
		plsqlKeywords.add("UID",Token.KEYWORD1);
		plsqlKeywords.add("UNION",Token.KEYWORD1);
		plsqlKeywords.add("UNIQUE",Token.KEYWORD1);
		plsqlKeywords.add("UPDATE",Token.KEYWORD1);
		plsqlKeywords.add("UPDATETEXT",Token.KEYWORD1);
		plsqlKeywords.add("USE",Token.KEYWORD1);
		plsqlKeywords.add("USER",Token.KEYWORD1);
		plsqlKeywords.add("USING",Token.KEYWORD1);
		plsqlKeywords.add("VALIDATE",Token.KEYWORD1);
		plsqlKeywords.add("VALUES",Token.KEYWORD1);
		plsqlKeywords.add("VARIANCE",Token.KEYWORD1);
		plsqlKeywords.add("VIEW",Token.KEYWORD1);
		plsqlKeywords.add("VIEWS",Token.KEYWORD1);
		plsqlKeywords.add("WHEN",Token.KEYWORD1);
		plsqlKeywords.add("WHENEVER",Token.KEYWORD1);
		plsqlKeywords.add("WHERE",Token.KEYWORD1);
		plsqlKeywords.add("WHILE",Token.KEYWORD1);
		plsqlKeywords.add("WITH",Token.KEYWORD1);
		plsqlKeywords.add("WORK",Token.KEYWORD1);
		plsqlKeywords.add("WRITE",Token.KEYWORD1);
		plsqlKeywords.add("XOR",Token.KEYWORD1);
		
		plsqlKeywords.add("ABS",Token.KEYWORD2);
		plsqlKeywords.add("ACOS",Token.KEYWORD2);
		plsqlKeywords.add("ADD_MONTHS",Token.KEYWORD2);
		plsqlKeywords.add("ASCII",Token.KEYWORD2);
		plsqlKeywords.add("ASIN",Token.KEYWORD2);
		plsqlKeywords.add("ATAN",Token.KEYWORD2);
		plsqlKeywords.add("ATAN2",Token.KEYWORD2);
		plsqlKeywords.add("CEIL",Token.KEYWORD2);
		plsqlKeywords.add("CHARTOROWID",Token.KEYWORD2);
		plsqlKeywords.add("CHR",Token.KEYWORD2);
		plsqlKeywords.add("CONCAT",Token.KEYWORD2);
		plsqlKeywords.add("CONVERT",Token.KEYWORD2);
		plsqlKeywords.add("COS",Token.KEYWORD2);
		plsqlKeywords.add("COSH",Token.KEYWORD2);
		plsqlKeywords.add("DECODE",Token.KEYWORD2);
		plsqlKeywords.add("DEFINE",Token.KEYWORD2);
		plsqlKeywords.add("FLOOR",Token.KEYWORD2);
		plsqlKeywords.add("HEXTORAW",Token.KEYWORD2);
		plsqlKeywords.add("INITCAP",Token.KEYWORD2);
		plsqlKeywords.add("INSTR",Token.KEYWORD2);
		plsqlKeywords.add("INSTRB",Token.KEYWORD2);
		plsqlKeywords.add("LAST_DAY",Token.KEYWORD2);
		plsqlKeywords.add("LENGTH",Token.KEYWORD2);
		plsqlKeywords.add("LENGTHB",Token.KEYWORD2);
		plsqlKeywords.add("LN",Token.KEYWORD2);
		plsqlKeywords.add("LOG",Token.KEYWORD2);
		plsqlKeywords.add("LOWER",Token.KEYWORD2);
		plsqlKeywords.add("LPAD",Token.KEYWORD2);
		plsqlKeywords.add("LTRIM",Token.KEYWORD2);
		plsqlKeywords.add("MOD",Token.KEYWORD2);
		plsqlKeywords.add("MONTHS_BETWEEN",Token.KEYWORD2);
		plsqlKeywords.add("NEW_TIME",Token.KEYWORD2);
		plsqlKeywords.add("NEXT_DAY",Token.KEYWORD2);
		plsqlKeywords.add("NLSSORT",Token.KEYWORD2);
		plsqlKeywords.add("NSL_INITCAP",Token.KEYWORD2);
		plsqlKeywords.add("NLS_LOWER",Token.KEYWORD2);
		plsqlKeywords.add("NLS_UPPER",Token.KEYWORD2);
		plsqlKeywords.add("NVL",Token.KEYWORD2);
		plsqlKeywords.add("POWER",Token.KEYWORD2);
		plsqlKeywords.add("RAWTOHEX",Token.KEYWORD2);
		plsqlKeywords.add("REPLACE",Token.KEYWORD2);
		plsqlKeywords.add("ROUND",Token.KEYWORD2);
		plsqlKeywords.add("ROWIDTOCHAR",Token.KEYWORD2);
		plsqlKeywords.add("RPAD",Token.KEYWORD2);
		plsqlKeywords.add("RTRIM",Token.KEYWORD2);
		plsqlKeywords.add("SIGN",Token.KEYWORD2);
		plsqlKeywords.add("SOUNDEX",Token.KEYWORD2);
		plsqlKeywords.add("SIN",Token.KEYWORD2);
		plsqlKeywords.add("SINH",Token.KEYWORD2);
		plsqlKeywords.add("SQRT",Token.KEYWORD2);
		plsqlKeywords.add("SUBSTR",Token.KEYWORD2);
		plsqlKeywords.add("SUBSTRB",Token.KEYWORD2);
		plsqlKeywords.add("TAN",Token.KEYWORD2);
		plsqlKeywords.add("TANH",Token.KEYWORD2);
		plsqlKeywords.add("TO_CHAR",Token.KEYWORD2);
		plsqlKeywords.add("TO_DATE",Token.KEYWORD2);
		plsqlKeywords.add("TO_MULTIBYTE",Token.KEYWORD2);
		plsqlKeywords.add("TO_NUMBER",Token.KEYWORD2);
		plsqlKeywords.add("TO_SINGLE_BYTE",Token.KEYWORD2);
		plsqlKeywords.add("TRANSLATE",Token.KEYWORD2);
		plsqlKeywords.add("TRUNC",Token.KEYWORD2);
		plsqlKeywords.add("UPPER",Token.KEYWORD2);
		
		plsqlKeywords.add("VERIFY",Token.KEYWORD1);
		plsqlKeywords.add("SERVEROUTPUT",Token.KEYWORD1);
		plsqlKeywords.add("PAGESIZE",Token.KEYWORD1);
		plsqlKeywords.add("LINESIZE",Token.KEYWORD1);
		plsqlKeywords.add("ARRAYSIZE",Token.KEYWORD1);
		plsqlKeywords.add("DBMS_OUTPUT",Token.KEYWORD1);
		plsqlKeywords.add("PUT_LINE",Token.KEYWORD1);
		plsqlKeywords.add("ENABLE",Token.KEYWORD1);

	}

	private static void addDataTypes()
	{

		plsqlKeywords.add("binary",Token.KEYWORD1);
		plsqlKeywords.add("bit",Token.KEYWORD1);
		plsqlKeywords.add("blob",Token.KEYWORD1);
		plsqlKeywords.add("boolean",Token.KEYWORD1);
		plsqlKeywords.add("char",Token.KEYWORD1);
		plsqlKeywords.add("character",Token.KEYWORD1);
		plsqlKeywords.add("DATE",Token.KEYWORD1);
		plsqlKeywords.add("datetime",Token.KEYWORD1);
		plsqlKeywords.add("DEC",Token.KEYWORD1);
 		plsqlKeywords.add("decimal",Token.KEYWORD1);
		plsqlKeywords.add("DOUBLE PRECISION",Token.KEYWORD1);
		plsqlKeywords.add("float",Token.KEYWORD1);
		plsqlKeywords.add("image",Token.KEYWORD1);
		plsqlKeywords.add("int",Token.KEYWORD1);
		plsqlKeywords.add("integer",Token.KEYWORD1);
		plsqlKeywords.add("money",Token.KEYWORD1);
		plsqlKeywords.add("name",Token.KEYWORD1);
		plsqlKeywords.add("NATURAL",Token.KEYWORD1);
		plsqlKeywords.add("NATURALN",Token.KEYWORD1);
		plsqlKeywords.add("NUMBER",Token.KEYWORD1);
		plsqlKeywords.add("numeric",Token.KEYWORD1);
		plsqlKeywords.add("nchar",Token.KEYWORD1);
		plsqlKeywords.add("nvarchar",Token.KEYWORD1);
		plsqlKeywords.add("ntext",Token.KEYWORD1);
		plsqlKeywords.add("pls_integer",Token.KEYWORD1);
		plsqlKeywords.add("POSITIVE",Token.KEYWORD1);
		plsqlKeywords.add("POSITIVEN",Token.KEYWORD1);
		plsqlKeywords.add("RAW",Token.KEYWORD1);
		plsqlKeywords.add("real",Token.KEYWORD1);
		plsqlKeywords.add("ROWID",Token.KEYWORD1);
		plsqlKeywords.add("SIGNTYPE",Token.KEYWORD1);
		plsqlKeywords.add("smalldatetime",Token.KEYWORD1);
		plsqlKeywords.add("smallint",Token.KEYWORD1);
		plsqlKeywords.add("smallmoney",Token.KEYWORD1);
		plsqlKeywords.add("text",Token.KEYWORD1);
		plsqlKeywords.add("timestamp",Token.KEYWORD1);
		plsqlKeywords.add("tinyint",Token.KEYWORD1);
		plsqlKeywords.add("uniqueidentifier",Token.KEYWORD1);
		plsqlKeywords.add("UROWID",Token.KEYWORD1);
		plsqlKeywords.add("varbinary",Token.KEYWORD1);
		plsqlKeywords.add("varchar",Token.KEYWORD1);
		plsqlKeywords.add("varchar2",Token.KEYWORD1);


	}

	private static void addSystemFunctions()
	{
		plsqlKeywords.add("SYSDATE",Token.KEYWORD2);

	}

	private static void addOperators()
	{
		plsqlKeywords.add("ALL",Token.OPERATOR);
		plsqlKeywords.add("AND",Token.OPERATOR);
		plsqlKeywords.add("ANY",Token.OPERATOR);
		plsqlKeywords.add("BETWEEN",Token.OPERATOR);
		plsqlKeywords.add("BY",Token.OPERATOR);
		plsqlKeywords.add("CONNECT",Token.OPERATOR);
		plsqlKeywords.add("EXISTS",Token.OPERATOR);
		plsqlKeywords.add("IN",Token.OPERATOR);
		plsqlKeywords.add("INTERSECT",Token.OPERATOR);
		plsqlKeywords.add("LIKE",Token.OPERATOR);
		plsqlKeywords.add("NOT",Token.OPERATOR);
		plsqlKeywords.add("NULL",Token.OPERATOR);
		plsqlKeywords.add("OR",Token.OPERATOR);
		plsqlKeywords.add("START",Token.OPERATOR);
		plsqlKeywords.add("UNION",Token.OPERATOR);
		plsqlKeywords.add("WITH",Token.OPERATOR);

	}

	private static void addSystemStoredProcedures()
	{
		plsqlKeywords.add("sp_add_agent_parameter",Token.KEYWORD3);
	}

	private static void addSystemTables()
	{
		plsqlKeywords.add("backupfile",Token.KEYWORD3);
	}

	private static KeywordMap plsqlKeywords;
}
