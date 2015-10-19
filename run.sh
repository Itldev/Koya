#!/bin/bash
#
# Koya is an alfresco module that provides a corporate orientated dataroom.
#
# Copyright (C) Itl Developpement 2014
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
#


OPTS="-Xms512m -Xmx4096m -XX:MaxPermSize=512m "
PROFILES="run"
MVNOPTS="-DskipTests=true"

#
# Arguments processing. Possible values are :
#
# - debug : activates java debug options
# - rad   : activates jrebel if jrebel lib exists in $JREBEL_LIB
# - mysql : use mysql database instead of H2 
#           You MUST give -Denv.dbrootpassword=*******
#           AND Have an alfresco database in your mysql server (or modify alfresco pom profile)
#


for var in "$@"
do
    if [ "$var" == "debug" ];then
        OPTS="$OPTS -Xrunjdwp:transport=dt_socket,address=4001,server=y,suspend=n"
    fi

    if [ "$var" == "jrebel" ];then
        if [ -n "$JREBEL_LIB" ];then
            if [ -f $JREBEL_LIB ];then
                OPTS="$OPTS -javaagent:$JREBEL_LIB"
                PROFILES="$PROFILES,rad"
            fi
        fi        
    fi

    #multiThread compilation option
    if [ "$var" == "mt" ];then
        MT_OPT="-T 1C"
    fi

    if [ "$var" == "mysql" ];then
        PROFILES="$PROFILES,mysql"
    fi

    #env var added directly in options
    if [[ $var == -D* ]];then
        MVNOPTS="$MVNOPTS $var"    
    fi

done

if [ -f alfresco/src/main/properties/dev/alfresco-global.properties ];then
	MVNOPTS="$MVNOPTS -Denv=dev"
	echo "ENV = DEV"
fi

# Define Alfresco Home that is used as log dir base
#
#
if [ -n "$ALFRESCO_HOME" ];then
        OPTS="$OPTS -Dalfresco.home=$ALFRESCO_HOME/"
else
        OPTS="$OPTS -Dalfresco.home="
fi
echo "MAVEN_OPTS=$OPTS mvn $MT_OPT clean install -P $PROFILES $MVNOPTS"
MAVEN_OPTS="$OPTS" mvn $MT_OPT clean install -P $PROFILES $MVNOPTS

