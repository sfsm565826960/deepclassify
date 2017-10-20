#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python kw_output.py kw_table_name sex_table_name output_csv
#**功能描述：将kw表按序输出
#**输入参数：kw表名 sex表名 输出文件（默认kw_output.csv）
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
import glob
sys.path.append('/home/rcgzz/cdc/py/')
os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

if __name__ == '__main__':

    reload(sys)
    sys.setdefaultencoding('utf-8')

    ##计时
    TIME_STAET = time.clock()

    ##读取参数
    kw_table_name = sys.argv[1]
    print '数据源表：' + kw_table_name

    sex_table_name = sys.argv[2]
    print '性别源表：' + sex_table_name

    output_csv = 'kw_output.csv'
    if len(sys.argv) > 3:
        output_csv = sys.argv[3]
    print '输出CSV：' + output_csv

    output_path = os.path.join(sys.path[0], output_csv.split('.')[0])

    ##SQL语句
    sqls = []

    ##联合性别，并按序排练
    sqls.append(
        '''create table %s_output as 
            select msisdn, sex, age, v from 
              (select user.msisdn, user.sex, user.age, kw.domainname, kw.k, kw.v from 
                (select * from %s) kw 
              join 
                (select * from %s) user 
              on kw.msisdn = user.msisdn) out 
            group by sex, age, msisdn, domainname, v;''' %
        (kw_table_name, kw_table_name, sex_table_name)
    )

    ##输出数据
    sqls.append(
        '''insert overwrite local directory '%s' 
            row format delimited fields terminated by '\t' 
            select msisdn, sex, age, v from %s_output;''' %
        (output_path, kw_table_name)
    )

    ##输出SQL
    sql = '\n'.join(sqls)
    # sql_file = open(os.path.basename(__file__).split('.')[0] + '.sql', 'w')
    # sql_file.write(sql)
    # sql_file.close()
    print sql

    ##执行语句
    os.system('hive -e "%s"' % (sql))

    ##合并CSV文件
    output_file_list = glob.glob(os.path.join(output_path, '*'))
    if len(output_file_list) > 1:
        fIndex = 0
        suffix = '/' + str(len(output_file_list)) + ')'
        csv_file = open(os.path.join(sys.path[0], output_csv), 'w')
        for file in output_file_list:
            print '合并文件中...(' + str(fIndex) + suffix
            for line in open(file):
                csv_file.writelines(line)
            csv_file.write('\n')
            os.remove(file)
            fIndex += 1
        csv_file.close()
        print '合并完成'
    else:
        os.rename(output_file_list[0], os.path.join(sys.path[0], output_csv))

    os.removedirs(output_path)

    ##执行结束
    print '\n[Finish] ' + str(time.clock() - TIME_STAET) + 'ms'