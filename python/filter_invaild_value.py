#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python filter_invaild_value.py rcgzz.kw_table_name rcgzz.sex_table_name rcgzz.create_table_name min_user_count min_sex_rate kw_table_select_keys
#**功能描述：筛选和排除用户性别无明显偏向的值
#**输入参数：kw源数据表名(要求含有v和msisdn字段)
#          sex源数据表名(要求含有sex和msisdn字段)
#          新表名
#          [可选]性别阀值(0.1~0.5，默认0.1)，将过滤掉性别偏向绝对值小于该阀值的关键字
#          [可选]用户阀值(要求大于0，默认3)，将过滤掉用户量少于该阀值的关键字
#          [可选]指定获取kw表部分k字段的关键字，多字段用英文逗号隔开
#**输出参数：
#**返回值：无
#**创建者：陈东成
#**创建日期：20171017
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

    ##定义字符集
    reload(sys)
    sys.setdefaultencoding('utf-8')

    ##读取参数
    kw_table_name = sys.argv[1]
    print '数据源表：' + kw_table_name

    sex_table_name = sys.argv[2]
    print '性别源表：' + sex_table_name

    create_table_name = sys.argv[3]
    print '创建新表：' + create_table_name

    min_sex_rate = 0.1
    if len(sys.argv) > 4:
        min_sex_rate = float(sys.argv[4])
    print '性别阀值：' + str(min_sex_rate)

    min_user_count = 0
    if len(sys.argv) > 5:
        min_user_count = int(sys.argv[5])
    print '用户阀值：' + str(min_user_count)

    kw_select_keys = []
    if len(sys.argv) > 6:
        kw_select_keys = sys.argv[6].split(',')
        print '指定k值：' + sys.argv[6]

    ##自定义无效条件
    invaild_domainname = [
        'api.map.baidu.com', 'apis.map.qq.com', 'js.ub.fang.com'
    ]
    print '无效域名：' + ', '.join(invaild_domainname)

    invaild_key = [
        'carrier', 'operator', 'CarrierName', 'carrierName', 'net', 'isp', 'cp',
        'city',  'userCity', '_city', 'fromCity', 'CName', 'City', 'ct', 'strCity',
        'cityName', 'Cityname', 'cityname', '_cityName', 'city_name', 'appCityName',
        '_lbs_city', 'sel_city', 'acity', 'geo_city', 'user_city', 'userArea', 'userarea',
        '_province', 'province', 'country', 'actyName', 'area_name', 'areaname',
        'fromStation', 'location', 'address', 'loc', 'area', '_product', 'GetMothod'
    ]
    print '无效字段：' + ', '.join(invaild_key)

    invaild_value = []
    print '无效值：' + ', '.join(invaild_value)

    ##SQL语句
    sqls = []

    ##自定义过滤无效数据
    sqls.append(
        '''create table %s as 
              select msisdn, domainname, k, v from %s 
                where domainname not in ('%s') 
                and k not in ('%s') 
                and v is not null 
                and v not in ('%s')%s;''' %
           (create_table_name, kw_table_name
            , '\',\''.join(invaild_domainname)
            , '\',\''.join(invaild_key)
            , '\',\''.join(invaild_value)
            , '' if len(kw_select_keys) == 0
            else ' and k in (\'' + '\',\''.join(kw_select_keys) + '\')')
    )

    ##过滤无效符号
    invaild_regexp = r'[ \tΜδζτφροψωπεйиж:-_$=±#@%^&!{}()·,\\+\\*\\.\\?\\;\\[\\]"]+'
    sqls.append(
        '''insert overwrite table %s 
            select t1.msisdn, t1.domainname, t1.k, t1.v from 
                (select msisdn, domainname, k, regexp_replace(v,'^%s','') as v from %s) t1 
            where length(t1.v) > 0;''' %
        (create_table_name, invaild_regexp, create_table_name)
    )
    sqls.append(
        '''insert overwrite table %s 
            select t1.msisdn, t1.domainname, t1.k, t1.v from
                (select msisdn, domainname, k, regexp_replace(v,'%s$','') as v from %s) t1 
            where length(t1.v) > 0;''' %
        (create_table_name, invaild_regexp, create_table_name)
    )
    sqls.append(
        '''insert overwrite table %s 
            select t1.msisdn, t1.domainname, t1.k, t1.v from 
                (select msisdn, domainname, k, regexp_replace(v,'\\t',' ') as v from %s) t1 
            where length(t1.v) > 0;''' %
        (create_table_name, create_table_name)
    )

    ##根据搜索词和用户号码进行分组
    sqls.append(
        '''create table %s_tmp1 as 
            select v, msisdn 
            from %s 
            group by v, msisdn;''' %
        (create_table_name, create_table_name)
    )

    ##联合性别、用户量并忽略少于min_user_count的关键字
    sqls.append(
        '''create table %s_tmp2 as 
              select t0.v, t0.msisdn, sex.sex from 
                  (select v, count(*) as uc from %s_tmp1 group by v having count(*) >= %d) t1
                join 
                  (select v,msisdn from %s_tmp1) t0
                on t0.v = t1.v 
                join 
                  (select msisdn, sex from %s where sex in ('男','女')) sex 
                on sex.msisdn = t0.msisdn;''' %
           (create_table_name, create_table_name, min_user_count, create_table_name, sex_table_name)
    )

    sqls.append('drop table ' + create_table_name + '_tmp1;')

    ##按性别分类
    sqls.append(
        '''create table %s_tmp3 as 
              select v, sex, count(*) as sc 
              from %s_tmp2 
              group by v, sex;''' %
           (create_table_name, create_table_name)
    )

    sqls.append('drop table ' + create_table_name + '_tmp2;')

    ##计算性别偏向
    sqls.append(
        '''create table %s_invaild as 
              select * from 
                  ( select man.v
                  , man.sc as man_count
                  , woman.sc as woman_count
                  , man.sc / (man.sc + woman.sc) - 0.5 as sex_rate 
                  from 
                    (select v, sc from %s_tmp3 where sex = '男') man 
                  join 
                    (select v, sc from %s_tmp3 where sex = '女') woman 
                  on man.v = woman.v) invaild
              where abs(invaild.sex_rate) < %f;''' %
           (create_table_name, create_table_name, create_table_name, min_sex_rate)
    )

    sqls.append('drop table ' + create_table_name + '_tmp3;')

    # 过滤无明显偏向关键字，求差集
    sqls.append(
        '''insert overwrite table %s 
            select kw.msisdn, kw.domainname, kw.k, kw.v from 
                (select * from %s_invaild) invaild 
              right outer join 
                (select * from %s) kw 
              on invaild.v = kw.v 
            where invaild.v is null;''' %
        (create_table_name, create_table_name, create_table_name)
    )

    ##输出SQL
    sql = '\n'.join(sqls)
    # sql_file = open(os.path.basename(__file__).split('.')[0] + '.sql', 'w')
    # sql_file.write(sql)
    # sql_file.close()
    print sql

    ##执行语句
    os.system('hive -e "%s"' % (sql))

    ##执行结束
    print '\n[Finish] ' + str(time.clock() - TIME_STAET) + 'ms'