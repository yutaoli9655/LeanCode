#!/usr/bin/bash
#set -x
# Author: RayLee
# Date: 2017-12-19 14:34
# Version: V0.1
# note:
# modify by raylee 2017-12-26 11:29:24 修改逻辑结构
#       1、参数： 跑行内数据；跑财汇数据+综合去重
#       2、不用通知接口：失败或者成功 flag 信号 0/1/2
#       3、顺序先 ETL , 再预警处理


# 设置DEBUG标识
DEBUG_FLAG="1"

# 标识不成功
BREAKFLAG=0

# 获取运行频率
FREQ="$1"

# 分隔符
DELIMITER=":"

# 当前日期
CURRDATE=`date +%Y%m%d`

# 当前执行的目录
# BASE_PATH=$(dirname "$PWD")

# 选项列表
CHOICEARR=("QUARTERLY","HALF","DAILY","WEEKLY","MONTHLY","ADHOC")
[ `echo "${CHOICEARR[@]}" | grep ${FREQ} | wc -l` -eq 0 ] && echo "The parameter must be in 'QUATERLY', 'HALF','DAILY','WEEKLY','MONTHLY','ADHOC'" && BREAKFLAG=1
# [ `echo "${CHOICEARR[@]}" | grep ${FREQ} | wc -l` -eq 0 ] && usage && BREAKFLAG=1

echo ${BREAKFLAG}

if [ "${BREAKFLAG}" -eq "0" ]
then
   if [ "${FREQ}" == "QUARTERLY" ]
   then
      BASE_PATH="/home/chinacscs/caihui/sysnc-1.0-SNAPSHOT"
   elif [ "${FREQ}" == "DAILY" ]
   then
      BASE_PATH="/home/chinacscs/etltostg/estg-1.0-SNAPSHOT"
   else
      BASE_PATH=$(dirname "$PWD")
   fi
fi
[ "${BREAKFLAG}" == "0" ] && [ -d "${BASE_PATH}" ] || BREAKFLAG=1

# 文件夹清单
LOG_PATH="${BASE_PATH}/log"
[ "${BREAKFLAG}" == "0" ] && [ -d "${LOG_PATH}" -a -x "${LOG_PATH}" ] || BREAKFLAG=1

# 数据库用户
CONFIG_CONN_USER='cs_master_stg'
EXEC_CONN_USER='cs_master_tgt'
# 数据库登录密码
CONFIG_CONN_PAWD='abc123'
EXEC_CONN_PAWD='abc123'
# 登录数据库的连接串
CONN_STR='10.100.45.10:1521/orcl'

# shell运行日志文件
LOGFILE="${LOG_PATH}/etllog_${FREQ}_${CURRDATE}.log"

# 调度文件清单
DATAFILE="${LOG_PATH}/JOBLIST_${FREQ}_${CURRDATE}.dat"

# LOCK file
LOCKFILE="${LOG_PATH}/LOCK_${FREQ}.lock"

# 是否加锁
ISADDLOCK="1"

# 检查变量设置情况
if [ "${DEBUG_FLAG}" == "0" ]
then
        echo "EXEC_FILENAME:" `basename $0`
        echo "EXEC_PATH:" `pwd`
        echo "EXEC_FREQ:" ${FREQ}
        echo "CONN_STR: ${CONN_STR}"
        echo "SRC_PATH: ${SRC_PATH}"
        echo "BAK_PATH: ${BAK_PATH}"
        echo "DST_PATH: ${DST_PATH}"
        echo "LOG_PATH: ${LOG_PATH}"
        echo "LOGFILE: ${LOGFILE}"
        echo "DATAFILE: ${DATAFILE}"
fi

# 检查对象是否存在
check_file()
{
        # 设置的是目录检查
        if [ $1 = "d" ]; then
                # 如果目录不存在则创建目录并设置权限
                [ ! -d $2 ] && mkdir $2 && chmod 755 $2 || chmod 755 $2
        # 设置的是文件检查
        elif [ $1 = "f" ]; then
                # 如果文件不存在则创建文件并设置权限,如果存在则清空文件
                [ ! -f $2 ] && touch $2 && chmod 755 $2 || chmod 755 $2
        fi
}

# 定义写日志功能
logit()
{
        echo -e  "***"`whoami` `date "+%Y-%m-%d %H:%M:%S"` " : $1" >> $LOGFILE
}





logit "The Job List Generate!"
logit "--------------------------------------------------------------------------------------"

if [[ "${BREAKFLAG}" -eq "0" ]]; then
        sqlplus -s /nolog <<EOF
            connect $CONFIG_CONN_USER/$CONFIG_CONN_PAWD@$CONN_STR
            whenever sqlerror exit sql.sqlcode rollback
                set pagesize 0
                set linesize 10000
            set feedback off
                set echo off
                set heading off
                set termout off
                set trimout on
                set trimspool on
                spool ${DATAFILE};
                select upper(trim(t.frequency)) || '${DELIMITER}' || t.file_nm
                  from etl_cmb_process t
                 where etl_step <> '1'
                   and upper(trim(t.frequency)) = '${FREQ}'
                   and active_flag = '1'
                 order by run_order asc;
                spool off;
                exit sql.sqlcode;
EOF
        BREAKFLAG=`echo $?`
fi


if [ "${BREAKFLAG}" -eq "0" -a "${ISADDLOCK}" -eq "0" -a -f ${LOCKFILE} ]
then
   logit "JOB LOCKED"
else
   [ "${BREAKFLAG}" -eq "0" ] && [ "${ISADDLOCK}" -eq "0" ] && check_file f ${LOCKFILE} && logit "Add lock"
        if [ "${BREAKFLAG}" -eq "0" ]
        then
            for jobname in `cat ${DATAFILE} | grep ${FREQ} | cut -d"${DELIMITER}" -f2`
           do
              logit "Begin Execute ${jobname}"
              ret=`sqlplus -s /nolog <<EOF
               connect $EXEC_CONN_USER/$EXEC_CONN_PAWD@$CONN_STR
               whenever sqlerror exit sql.sqlcode rollback
               exec ${jobname};
               exit sql.sqlcode;
EOF`

              BREAKFLAG=`echo $?` && logit "${ret}" && [ "${BREAKFLAG}" == "0" ] && logit "End Execute ${jobname}" || break
           done
             echo ${ret}
#            BREAKFLAG=`echo $?`
        fi
   [ "${BREAKFLAG}" -eq "0" ] && [ "${ISADDLOCK}" -eq "0" -a -f ${LOCKFILE} ] && rm -fr ${LOCKFILE} && logit "Release lock" || logit "Released"
fi

echo ${BREAKFLAG}
exit ${BREAKFLAG}
