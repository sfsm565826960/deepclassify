create table rcgzz.zh_4g_kw_kw_output as 
            select msisdn, sex, age, domainname, v from 
              (select user.msisdn, user.sex, user.age, kw.domainname, kw.k, kw.v from 
                (select * from rcgzz.zh_4g_kw) kw 
              join 
                (select * from rcgzz.zh_sex_age) user 
              on kw.msisdn = user.msisdn) out 
            group by sex, age, msisdn, domainname, v;
insert overwrite local directory 'C:\Users\bimen\Desktop\人才交流中心\作业4-深度学习分类\DeepClassifier_java\python\kw_output' 
            row format delimited fields terminated by ',' 
            select * from rcgzz.zh_4g_kw_kw_output;