# $Id: makefile 4069 2021-05-18 11:40:25Z yicong $
#
# Copyright (C) 2008 by The Regents of the University of California
#
# Redistribution of this file is permitted under the terms of the BSD
# license
#
# Date: 05/15/2008
#
# Author: Rares Vernica <rares (at) ics.uci.edu>
# Maintainer: Yicong Huang <yicongh1 (at) uci.edu>
#

CC=gcc
#CFLAGS=-fPIC -Wall -O3 -I/usr/include/mysql
CFLAGS=-fPIC -Wall -O3 -I"C:\Program Files\MySQL\MySQL Server 8.0\include"
LDFLAGS=-L. -ledrec

#all: libed.so libedth.so libedrec.so
all: libed.dll libedth.dll libedrec.dll

#libed.so: ed.c
libed.dll: ed.c
	${CC} ${CFLAGS} -shared -o $@ $?

#libedth.so: edth.c
libedth.dll: edth.c
	${CC} ${CFLAGS} -shared -o $@ $?

#libedrec.so: edrec.c
libedrec.dll: edrec.c
	${CC} ${CFLAGS} -shared -o $@ $?

#cat ed.sql edth.sql edrec.sql | mysql -u mytestuser -p
.PHONY: test
test:
	powershell -Command "Get-Content ed.sql, edth.sql, edrec.sql | mysql -u mytestuser -p"

#	-rm unittest *.so *.o *~
.PHONY: clean
clean:
	-del /Q unittest.exe *.dll *.o *~ 2>nul

#unittest: unittest.o libedrec.so
unittest: unittest.o libedrec.dll

unittest.o: edrec.h
