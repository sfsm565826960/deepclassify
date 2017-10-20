#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python pick_kw_from_url.py rcgzz.from_table_name[多个表用英文,隔开] rcgzz.create_table_name write_type
#**前提：将HiveDecodeUri.jar放到udfPath中，最好是hdfs里
#**功能描述：从URI里获取中文字符串
#**输入参数：源数据表名(要求含有url和msisdn字段)[多个表用英文,隔开] 新建表名(要求不存在同名表) 首次写入方式(create,insert,overwrite)
#**输出参数：无
#**返回值：无
#**创建者：陈东成
#**创建日期：20170925
#**修改日志：
#**
#**************************************************************************
##导入配置信息
import os
import sys
import time
sys.path.append('/home/rcgzz/cdc/py/')
os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

if __name__ == '__main__':

    reload(sys)
    sys.setdefaultencoding('utf-8')

    ##计时
    TIME_STAET = time.clock()

    ##定义kw正则表达式
    r_other = '[A-Za-z0-9+-_]{0,}'
    regexp = '([a-zA-Z0-9_]+)=((' + r_other + '(%[A-F0-9]{2}){6,}' + r_other + ')+)'
    zw_regexp = '([^\\x00-\\xff]+)'
    print 'KW正则表达式：' + regexp

    ##定义自定义函数
    udfPath = '/home/rcgzz/cdc/HiveDecodeUri.jar'
    udf = 'add jar ' + udfPath + ';'\
        + 'drop temporary function decode_uri;'\
        + 'create temporary function decode_uri as \'hiveDecodeUri.HiveDecodeUri\';'
    print '自定义函数：' + udf

    ##定义字符集
    reload(sys)
    sys.setdefaultencoding('utf-8')

    ##读取参数
    from_table_names = sys.argv[1].split(',')
    create_table_name = sys.argv[2]
    write_type = 'create table ' + create_table_name + ' as'

    if len(sys.argv) > 3:
        if (sys.argv[3] == 'insert'):
            write_type = 'insert into table ' + create_table_name
        elif (sys.argv[3] == 'overwrite'):
            write_type = 'insert overwrite table ' + create_table_name

    print '数据源表：' + ','.join(from_table_names)
    print '创建新表：' + create_table_name
    print '创建方式：' + write_type

    ##创建表
    sql1 = ''
    for from_table_name in from_table_names:
        sql1 = sql1 + '''%s 
                select * from 
                  (select url.msisdn, url.k, decode_uri(url.v) as v, url.domainname from 
                      ( select msisdn 
                      , regexp_extract(url,'%s',1) as k 
                      , regexp_extract(url,'%s',2) as v 
                      , domainname 
                      from %s) url 
                  where length(url.v) >= 9) kw 
                where kw.v is not null and length(regexp_extract(kw.v, '%s', 1)) > 0;''' % \
                  (write_type,regexp,regexp,from_table_name, zw_regexp)
        write_type = 'insert into table ' + create_table_name
    #print sql1

    ##删除临时函数
    remove_udf = 'drop temporary function decode_uri;'

    ##输出SQL
    sql = udf + sql1 + remove_udf
    # sql_file = open(os.path.basename(__file__).split('.')[0] + '.sql', 'w')
    # sql_file.write(sql)
    # sql_file.close()
    print sql

    ##执行语句
    os.system('hive -e "%s"' % (sql))

    ##执行结束
    print '\n[Finish] ' + str(time.clock() - TIME_STAET) + 'ms'