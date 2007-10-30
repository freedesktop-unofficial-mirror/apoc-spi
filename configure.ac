AC_INIT(apoc-spi, 1.0)

AM_INIT_AUTOMAKE
AC_JAVA_OPTIONS

AC_EXPAND(datadir, NONE, EXPANDED_DATADIR)

APOCDIR=$EXPANDED_DATADIR'/lib/apoc'

AC_ARG_WITH(apoc-path,
AC_HELP_STRING( [--with-apoc-path=DIR],
		[specify a path to install the apoc libraries (DATADIR/lib/apoc)]),
[
	if test "$withval" != "no"
	then
		APOCDIR=$withval
	fi
])

LDAPJDK=$APOCDIR/ldapjdk.jar

AC_ARG_WITH(ns-ldap-path,
AC_HELP_STRING( [--with-ns-ldap-path=DIR],
		[specify the Netscape's LDAP JDK path or jar file (APOCDIR/ldapjdk.jar)]),
[
	if test "$withval" != "no"
	then
		LDAPJDK=$withval
	fi
])

CLASSPATH="$APOCDIR:$LDAPJDK:.:$CLASSPATH"
export CLASSPATH

AC_PROG_JAVA
AC_PROG_JAR
AC_CHECK_CLASS(netscape.ldap.LDAPConnection, [], [
echo
AC_MSG_ERROR(Mozilla/Netscape LDAP JDK not found in Classpath.
If it is already installed please use --with-ns-ldap-path=DIR to point to the path or jar file.)
])


AC_CHECK_PROG(ANT, ant, ant, no)
if test $ANT == "no"
then
	AC_MSG_ERROR(Apache's Ant was not found in path.)
fi

AC_SUBST(APOCDIR)
AC_SUBST(LDAPJDK)


AC_OUTPUT([
Makefile
src/Makefile
src/com/Makefile
src/com/sun/Makefile
src/com/sun/apoc/Makefile
src/com/sun/apoc/spi/Makefile
src/com/sun/apoc/spi/build.xml
])
